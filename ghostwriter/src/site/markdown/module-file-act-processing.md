# Module & Project File Processing during Act Steps

## Overview

This document describes how Ghostwriter traverses **modules** and **project files**
while performing Act steps (episodes). The behavior is implemented across
`AbstractFileProcessor`, `ProjectProcessor`, and `ActProcessor`.

Unlike a free-roaming autonomous agent that decides *what* to work on next,
Ghostwriter uses a **deterministic traversal** of the project tree combined with a
**scripted, multi-step interaction** (the Act and its episodes) applied to each
matched file. In other words: the *structure* of the work is fixed and predictable,
while the *reasoning* inside each step is delegated to the GenAI provider. This
separation is the central design idea behind the whole module/file processing
pipeline, and it is what most distinguishes Ghostwriter from typical AI agent
frameworks (see [Comparison with other AI agent strategies](#Comparison_with_other_AI_agent_strategies)).

[<img src="images/episodes-act-processing.png" alt="Click to show" width="800"/>](images/episodes-act-processing.png)

## Mental model: two orthogonal loops

It helps to think of the processing as two nested loops that are deliberately kept
independent:

1. **The traversal loop (outer, deterministic).** Walk the project — recurse into
   modules, list files, filter them. This loop is pure filesystem and project-layout
   logic. No model is involved in deciding *which* files exist or *whether* to visit
   a module; that is derived from `ProjectLayout` and configuration.
2. **The reasoning loop (inner, model-driven).** For each matched file, run the Act's
   episodes. Here the GenAI provider does the "thinking" — reading, editing,
   reviewing, or generating content — but always within the bounds of the current
   file and the episode's prompt.

Keeping these loops orthogonal makes runs **reproducible** (the same project produces
the same set of work items), **parallelizable** (modules are independent), and
**bounded** (the model cannot wander off to arbitrary parts of the repository unless
a tool explicitly allows it).

[<img src="images/module-file-act-processing.png" alt="Click to show" width="800"/>](images/module-file-act-processing.png)

## 1. Entry point — `scanFolder(projectDir)`

Processing begins in `AbstractFileProcessor.scanFolder(File projectDir)`. It first
resolves the project structure by calling `getProjectLayout(projectDir)`, which
returns a `ProjectLayout` describing the project's sources, tests, docs, and —
importantly — its **modules**.

The processor then decides whether to recurse into modules based on the
**non-recursive** flag:

- If **non-recursive mode is enabled**, module recursion is skipped entirely, and
  only the current project's own files are processed.
- Otherwise, it proceeds to module discovery.

## 2. Module discovery and recursion

The processor reads the module list via `projectLayout.getModules()`. If one or
more modules exist, they are processed using one of two strategies, controlled by
the `threads` setting:

- **Sequential (`threads <= 1`):** Each module is processed one at a time in a
  simple loop, calling `processModule(projectDir, module)`.
- **Multi-threaded (`threads > 1`):** `processModulesMultiThreaded()` submits a
  `processModule` task for every module to a fixed-size thread pool. It then waits
  for all `Future` results and safely shuts down the executor (bounded by
  `moduleThreadTimeoutMinutes`). Interruptions and execution failures are surfaced
  as `IllegalStateException`.

Crucially, **`processModule` recursively re-enters `scanFolder`** for each
sub-module directory. This means the entire logic (module discovery + file
processing) repeats for every module, producing a full recursive traversal of a
multi-module project tree.

**Why modules are the unit of parallelism.** Modules in a well-structured project
are largely independent (their own sources, tests, and build unit). Treating each
module as an isolated work item means concurrency is safe by construction — two
threads rarely touch the same file — without needing locks or a shared task queue.
This is a much simpler concurrency model than the task-graph schedulers used by many
autonomous multi-agent systems, and it maps cleanly onto real project topology.

## 3. Parent (non-module) file processing

After all modules are handled, `scanFolder` calls
`processParentFiles(projectLayout)`. The base class provides an empty hook, but
**`ActProcessor` overrides it** to perform the actual Act work on files that
belong directly to the current project (not to its modules).

The steps are:

1. **List candidate files** — `listFiles(projectDir)` recursively collects files,
   excluding known build/tooling directories, and sorts them **deepest-first**
   (by path depth).
2. **Filter the list** — entries are removed if they are **module directories**
   (`isModuleDir`) or if they fail the `match()` test.
3. **Fallback** — if no files match, the processor falls back to processing the
   **project directory itself** (so the act still runs once).

The `match()` method decides file inclusion using several rules:

- Rejects `null` files and any path under excluded directories
  (`ProjectLayout.isExcludedPath`).
- If a `pathMatcher` is configured, the file's project-relative path must match the
  glob/regex pattern; otherwise the file must equal the explicit scan `path`.
- Applies user-supplied `excludes[]` patterns (glob, regex, or exact path).

**Why deepest-first ordering matters.** Sorting files by descending path depth means
leaf files are handled before their parent directories. For documentation and
aggregation acts this is useful: a parent (e.g. a package `README` or an aggregator
`pom`) can be generated *after* its children exist, letting a later step summarize
work that earlier steps produced. It is a lightweight, filesystem-level form of
dependency ordering, achieved without a planner.

## 4. Per-file Act (episode) execution

For each surviving file, if it matches and a default prompt is present, the Act's
episodes are executed via the `Episodes` component:

- **Requested (selected) order** — when a subset of episodes was selected (e.g.
  `act#1,3`), `requestedOrder()` runs just those episodes in the requested
  sequence. If the `!` stop flag (`disableNormalOrder`) is set, processing of that
  file stops afterward; otherwise the normal order resumes from the next episode
  ID.
- **Regular order** — `regularOrder(startId, callback)` runs episodes
  sequentially. Each episode invokes `ActProcessor.process()`, which prepends the
  act-execution metadata JSON, resolves the enabled tools (including `auto` tool
  selection), and sends the prompt to the GenAI provider via `AIFileProcessor`.

Each episode's output is appended through `addResults(perform)`.

**Episodes as an explicit, inspectable plan.** In most agent frameworks the plan is
produced by the model at runtime and is therefore opaque and non-repeatable. In
Ghostwriter the plan *is the Act definition*: an ordered list of episodes written in
TOML, versioned in the repository, reviewable, and reusable. The model still reasons
freely inside an episode, but the sequence of steps, their instructions, and their
allowed tools are authored by a human. This makes Act execution closer to a
**declarative pipeline with LLM-powered stages** than to an autonomous agent.

## 5. Control-flow and termination

During episode execution, several signals influence the flow:

- **`RepeatEpisodeException`** — re-runs the current episode (iteration count
  increments).
- **`MoveToEpisodeException`** — jumps to another episode by numeric ID or by
  heading name.
- **`EndTaskException`** — records the message and **stops parent-file
  processing** for the current run.

When all modules and matching files have been processed, the collected outputs are
available through `getResults()`.

**Bounded, model-triggered control flow.** These exceptions give the model *some*
dynamic control — it can loop, jump, or stop — but only over a **finite, named set of
episodes** that already exist. This is a deliberate middle ground: it keeps the
adaptiveness that makes agents useful (retrying, branching on results) while removing
the unbounded, potentially runaway behavior of fully autonomous loops. The set of
reachable states is always known ahead of time.

## Comparison with other AI agent strategies

Ghostwriter's model is best understood by contrasting it with the common patterns
used to build AI agents and LLM applications.

### ReAct / tool-calling agents (e.g. LangChain, AutoGPT-style loops)

A ReAct agent runs a single open-ended loop: *think → call a tool → observe → think
again*, continuing until the model decides it is done. The plan emerges at runtime
and is stored only in the conversation.

- **Ghostwriter difference:** the outer loop is a **fixed project traversal**, not a
  model-driven loop. The model influences only the inner per-file episode order via a
  small, bounded set of signals (`RepeatEpisodeException`, `MoveToEpisodeException`,
  `EndTaskException`).
- **Trade-off:** less spontaneous exploration, but far more **predictability, cost
  control, and reproducibility**. You always know which files will be touched and how
  many model calls (roughly) will occur.

### Autonomous planner–executor agents (plan-and-execute, tree-of-thoughts)

These systems ask the model to first *generate a plan*, then execute (and often
re-plan) it. The plan is dynamic and opaque.

- **Ghostwriter difference:** the plan is **authored, not generated** — it is the Act
  (an ordered list of episodes) checked into the repository as TOML.
- **Trade-off:** less autonomy in deciding *what* to do, but the plan is
  **versioned, reviewable, reusable, and testable**, which matters a great deal for
  engineering workflows and CI pipelines.

### Multi-agent orchestration (crews, swarms, role-based agents)

Frameworks like multi-agent "crews" assign roles and let agents negotiate or delegate
tasks to one another, often via a scheduler or shared blackboard.

- **Ghostwriter difference:** parallelism is **structural** — one work item per
  module, executed on a plain thread pool — rather than emergent from agent
  negotiation. There is no inter-agent messaging.
- **Trade-off:** no dynamic role assignment, but concurrency is **safe by
  construction** (independent modules) and trivial to reason about, with no
  coordination overhead or deadlock risk.

### Retrieval-Augmented Generation (RAG) pipelines

RAG selects context by semantic similarity from a vector store and injects it into a
prompt. *Relevance* drives what the model sees.

- **Ghostwriter difference:** context selection is **structural and explicit** — the
  file being processed plus the episode's instructions and (optionally) referenced
  includes — rather than similarity-ranked chunks. The `auto` tool-selection step is
  the one place where a model-driven narrowing happens, and even that is scoped to a
  single episode.
- **Trade-off:** no fuzzy retrieval recall, but **deterministic, auditable context**:
  you can point to exactly which file and instructions produced an output.

### Map-reduce / batch document processing

Classic batch pipelines apply the same operation to every document, then optionally
aggregate. This is actually the *closest* analogue to Ghostwriter.

- **Ghostwriter similarity:** the traversal is essentially a **map** over files
  (per-file episodes), and `getResults()` / later episodes can act as a **reduce**.
- **Ghostwriter addition:** each "map" step is itself a **multi-episode mini-workflow**
  with bounded control flow, and the traversal is **project-aware** (modules,
  layout, exclusions) rather than a flat document list.

### Summary of positioning

| Dimension | Autonomous agent (ReAct / planner) | RAG pipeline | Ghostwriter (Act + traversal) |
|---|---|---|---|
| **What to work on** | Model decides at runtime | Retrieval by similarity | Deterministic project traversal |
| **Plan** | Generated, opaque, ephemeral | Implicit (query → chunks) | Authored TOML Act, versioned & reusable |
| **Context per step** | Accumulated conversation | Similarity-ranked chunks | The current file + episode instructions |
| **Parallelism** | Rare / complex to coordinate | Query-level | Per-module thread pool, safe by construction |
| **Model control flow** | Unbounded loop | None | Bounded: repeat / move / end over named episodes |
| **Reproducibility** | Low | Medium | High |
| **Best fit** | Open-ended exploration | Q&A over large corpora | Structured, repeatable project-wide operations |

**Bottom line.** Ghostwriter deliberately trades away open-ended autonomy for
**structure, determinism, and reviewability**. It is not trying to be a general agent
that figures out a task from scratch; it is a **project-aware execution engine** that
applies human-authored, LLM-powered workflows uniformly and predictably across every
module and file of a codebase.

## Summary of key rules

| Concern | Behavior |
|---|---|
| **Modules** | Discovered from `ProjectLayout.getModules()`, processed recursively (each re-enters `scanFolder`). |
| **Concurrency** | Sequential when `threads <= 1`; thread-pool based when `threads > 1`. |
| **Non-recursive mode** | Skips modules; processes only the current project's files. |
| **Parent files** | Exclude module directories and are filtered by `match()` (exclude dirs, `pathMatcher`, `excludes[]`). |
| **Empty match** | Falls back to processing the project directory itself. |
| **Per file** | Runs Act episodes (requested subset and/or regular order) against the GenAI provider. |
| **Termination** | `EndTaskException` halts parent-file processing. |

## Appendix: `@guidance` tags — a separate way of using Ghostwriter

> **Note.** `@guidance` tag processing is **not part of the Act functionality**
> described above. It is an *alternative, standalone way of using Ghostwriter*:
> instead of running an authored Act (an ordered list of episodes), you embed the
> instructions directly inside a source file and let Ghostwriter discover and apply
> them. It is documented here only for contrast; it does not participate in the
> Act/episode pipeline (`ActProcessor`, `Episodes`, `getResults()`, episode
> control-flow exceptions, etc.).

For full details, see: [Guidance Tag](guidance-tag.md)