---
<!-- @guidance: 
Create the Act page as a Project Information page for the project:
- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Create a separate section describing the action's interactive/non-interactive mode.
  - An action can be used as a non-interactive command to perform a predefined task without any additional data.
  - An action can be used interactively (as a chat). This is necessary when the user does not have full information about the desired action before initiating it.
  - Describe how to use value of `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` and `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` to continue or terminate the act processing.
— Create a special section describing how to use the `prompt` property in the toml file to set a default value for the user's prompt. This will be used if the user doesn't provide a prompt.
— Create a dedicated section describing how to use the `episode` feature.
- Clearly describe how inherited values are processed within the file:
  - Explain the mechanism by which values can be inherited from parent sections, templates, or defaults.
  - Specify how and when these inherited values are applied or overridden in the context of the Act TOML configuration.
  - Provide examples if relevant, to illustrate the inheritance process. 
- Summarize the purpose of the Act feature, its key methods, and how it fits into the overall project.
- Provide easy-to-follow, step-by-step instructions or a practical example showing how to use the Act feature in real scenarios.
- Ensure the content is accessible and helpful for all users, including those new to the codebase or without a technical background.
- Analyze all act TOML files located in the `src/main/resources/acts` folder.
- For each act, create a section that includes:
  - The act's name.
  - A clear, concise description of the act's purpose and when it should be used.
- The act name or interactive command: `exit` is reserved for terminating a process.
- Describe the usage of placeholder variables, which should be used in the ${...} format. 
  Placeholders of this type are intended for dynamic substitution by functional tools at runtime. 
  The LLM must not alter, resolve, or modify these placeholders in any way. 
  They are designed to enable retrieval of parameters from the configurator, 
  such as environment variables, system properties, action properties, and similar sources.
- An absolute path to a TOML file can be used as the file name; in this case, hierarchy using  classpath resources on the is not supported.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/ghostwriter/act.html
title: Act
---

# Act

## Overview

An **Act** is a reusable, predefined prompt template that tells Ghostwriter
exactly what to do when you run a task. Acts are stored as small `.toml`
files and bundle together everything the AI needs to perform a job:

- the *system instructions* that describe the assistant's role,
- the *input template* (the prompt) that will be sent to the AI,
- optional *Ghostwriter settings* (such as which folders to scan, whether
  to run interactively, how many threads to use, and so on).

Instead of typing a long prompt every time, you simply pick an act by name.
Ghostwriter takes care of loading the template, filling in your request,
and running the task against your project.

Acts can be shipped as **built-in templates** (bundled inside Ghostwriter,
under the classpath folder `/acts/`) or stored in a **user-defined
directory** that you configure via the `gw.acts` property. The same act
name in both locations is supported: the user-defined act *overrides* the
built-in one, and may also extend it through the `basedOn` mechanism (see
[Inheritance](#inheritance)).

## How an Act is Run

The basic command is:

```
gw --act <name> [your request text]
```

What happens next:

1. Ghostwriter loads the act file `<name>.toml` (first from the
   user-defined acts location, then from the classpath).
2. If the act declares `basedOn = "<parent>"`, the parent act is loaded
   first and its values are merged with the current act.
3. The `inputs` template is rendered: any `%s` placeholder is replaced by
   *your request text*. If you didn't provide request text, the act's own
   `prompt` property is used as the default (see
   [Default Prompt](#default-prompt)).
4. The composed prompt, together with the `instructions`, is sent to the
   AI provider, and the result is shown to you (or written back to files,
   depending on the act).

> **Tip.** The full path of an `.toml` file can be used in place of a
> short name (for example
> `gw --act C:\my-acts\custom.toml "do something"`). When an absolute
> path is used, the classpath resource hierarchy is **not** consulted —
> only the file you pointed to is loaded.

## Act File Structure

A typical act file looks like this:

```toml
description = '''
Short, human-readable summary of what the act does.
'''

instructions = '''
System instructions that define the AI assistant's role and rules.
'''

inputs = '''
# Task

%s
'''

# Optional: inherit from another act
# basedOn = "task"

# Optional: a default value for the user prompt
# prompt = "Describe the project structure."

# Optional Ghostwriter settings
[gw]
scanDir     = "glob:."
nonRecursive = true
interactive  = true
```

Recognised top-level keys:

| Key            | Purpose                                                                |
|----------------|------------------------------------------------------------------------|
| `description`  | Human-readable description of the act.                                 |
| `instructions` | System instructions for the AI.                                        |
| `inputs`       | Prompt template. May be a single string or a list of episode strings.  |
| `prompt`       | Default user prompt used when the user does not supply one.            |
| `basedOn`      | Name of a parent act to inherit values from.                           |
| `gw.*`         | Ghostwriter runtime options (see below).                               |
| any other key  | Forwarded to the configurator (available as a configuration property). |

Recognised `gw.*` options (managed by `GWConstants`):

| Property              | Description                                                |
|-----------------------|------------------------------------------------------------|
| `gw.scanDir`          | Path or glob pattern for files to scan (e.g. `"glob:."`).  |
| `gw.excludes`         | Comma-separated list of paths to exclude from scanning.    |
| `gw.nonRecursive`     | If `true`, do not recurse into modules/sub-projects.       |
| `gw.threads`          | Number of worker threads to use.                           |
| `gw.interactive`      | If `true`, the act runs in interactive (chat) mode.        |
| `gw.model`            | Override the AI model/provider for this act.               |
| `gw.acts`             | Path to the user-defined acts directory.                   |

## Interactive vs. Non-Interactive Mode

An act can be executed in two complementary ways.

### Non-Interactive Mode

This is the default. The act runs once, end to end, with the prompt that
was provided on the command line (or with the `prompt` default). It is
useful when:

- you already know exactly what you want done,
- the act is meant to be wired into a build, CI pipeline, or batch job,
- you want a single, reproducible execution.

Example:

```
gw --act commit
```

The `commit` act inspects the working copy, generates commit messages,
and stops.

### Interactive Mode

When the act file sets `gw.interactive = true` (or you enable interactive
mode on the command line), Ghostwriter starts a chat-style conversation.
After the first prompt is processed, you can keep typing new prompts and
refining the result without leaving the act. This is the right choice
when you do not have all the details up front and want to iterate with
the assistant.

While in interactive mode, two **special commands** are recognised in
the prompt input:

| Command (constant)                                 | Value | Effect                                                                       |
|----------------------------------------------------|-------|------------------------------------------------------------------------------|
| `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND`      | `.`   | Terminates the current act session and returns to the shell.                 |
| `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND`  | `>`   | Continues processing without sending a new prompt (e.g. moves to next step). |

Type a single dot (`.`) on its own to **exit** the act, or a single
greater-than sign (`>`) to **continue** to the next step or episode
without supplying additional input.

> **Reserved name.** The act name (and the interactive command) `exit`
> is reserved and is always interpreted as a request to terminate the
> current process.

## Default Prompt

The optional `prompt` property in the TOML file lets the act author
provide a sensible default for the user's prompt. It is used whenever
the user does not type any request text after the act name.

Example:

```toml
description = "Describe the current project."
instructions = '''
You are a senior developer. Summarise the project clearly.
'''
prompt = "Give me a high-level overview of this project."
inputs  = '''
# Task

%s
'''
```

Behaviour:

- `gw --act describe "focus on the build system"` → `%s` is replaced by
  `focus on the build system`.
- `gw --act describe` → `%s` is replaced by the value of `prompt`
  (`Give me a high-level overview of this project.`).

This way, an act always has something meaningful to do, even when called
with no arguments.

## Episodes

An act can be split into a sequence of **episodes** — multiple ordered
prompt steps that are executed one after another. To define episodes,
make `inputs` a TOML array of strings, where each string is a separate
prompt. The first line of each episode that starts with `# ` is treated
as the episode's display name.

Example:

```toml
description = "Two-step refactoring act."
instructions = "You are a refactoring expert."

inputs = [
    '''
# Analyse
Analyse the code below and list issues.

%s
''',
    '''
# Refactor
Apply the fixes you proposed in the previous step.
'''
]
```

When the act runs, Ghostwriter executes episode 1, then episode 2, and
so on. Each episode automatically receives an *Act Information* block
listing all episodes and the current episode ID.

### Selecting Specific Episodes

You can append `#<selection>` to the act name to control which episodes
are executed:

| Syntax                | Effect                                                                |
|-----------------------|-----------------------------------------------------------------------|
| `--act name`          | Run all episodes in normal order.                                     |
| `--act name#2`        | Run episode 2, then continue with the remaining episodes in order.    |
| `--act name#1,3`      | Run episodes 1 and 3 (in that order), then continue normally.         |
| `--act name#2!`       | Run only episode 2, then **stop** (`!` disables the normal order).    |

Episode IDs are 1-based. Invalid IDs cause an error.

### Episode Flow Control

From inside an episode, the AI (via function tools) can:

- **Repeat** the current episode (`RepeatEpisodeException`),
- **Move to** another episode by ID or by name
  (`MoveToEpisodeException`).

This makes it possible to build flexible, branching workflows.

## Inheritance (`basedOn`)

Acts support a simple inheritance model so that you can reuse and
extend existing templates instead of duplicating them.

The mechanism, implemented in `ActProcessor.loadAct`, works as follows:

1. The act `name` is loaded from the user-defined directory (if any) and
   from the classpath. Both copies are merged: the **user-defined** act
   takes precedence over the **built-in** one.
2. If either copy declares `basedOn = "<parent>"`, the parent act is
   loaded **recursively** in the same way, *before* the child's values
   are applied.
3. The child's values are then merged on top of the parent's, so the
   child can override or extend the parent.

### Merging Rules

- **Strings.** When the same string property exists in both parent and
  child, the child value is substituted into the parent value at the
  `%s` placeholder (or used directly if the parent has no `%s`).
- **Lists / arrays.** Lists are merged element by element: each child
  element replaces the `%s` placeholder of the corresponding parent
  element. Extra child elements extend the list.
- **Inputs vs. lists.** A string `inputs` in the parent can be combined
  with a list `inputs` in the child (each list item inherits from the
  parent string).
- **Configurator inheritance.** When applying a value, the current
  configurator is also consulted: any existing configuration value can
  be injected into the act value through `%s` substitution. This
  allows an act to "inherit" run-time configuration without hard-coding
  it.

### Example

Parent act `base.toml`:

```toml
instructions = '''
You are an AI assistant for the %s project.
'''
inputs = '''
# Task
%s
'''
```

Child act `report.toml`:

```toml
basedOn = "base"

instructions = "Ghostwriter"

inputs = '''
Generate a status report.
%s
'''
```

After loading, the merged act becomes equivalent to:

```toml
instructions = '''
You are an AI assistant for the Ghostwriter project.
'''
inputs = '''
# Task
Generate a status report.
%s
'''
```

The user's command-line prompt is finally substituted into the
remaining `%s` placeholder at run time.

## Placeholder Variables (`${...}`)

Inside any string value of an act file, you may use placeholders of the
form `${name}`, for example `${sonar.host}` or `${sonar.token}`.

These placeholders are **not** resolved by the AI. They are intended for
dynamic substitution by Ghostwriter's functional tools at runtime, and
they are filled from sources such as:

- environment variables,
- Java system properties,
- action / configurator properties,
- other configured key/value sources.

> **Important.** Functional tools must pass `${...}` placeholders
> through unchanged so that the runtime can substitute them. Do not
> resolve, modify, or remove them in your own code or prompts — even if
> you "see" their value, the substitution is meant to happen at the
> tool boundary, where credentials and other sensitive values are
> safely injected.

A typical use case (taken from `sonar-fix`):

```
${sonar.host}/api/issues/search?componentKeys=...
```

At runtime, `${sonar.host}` is replaced by the value configured in the
environment, and the actual API URL is sent to the tool.

## Step-by-Step: Using an Act

Below is a practical walk-through of the most common workflow.

1. **Discover available acts.**
   Run the bundled `help` act to learn what is available and how it
   works:

   ```
   gw --act help
   ```

2. **Pick an act and provide a request.**
   For example, ask Ghostwriter to add documentation to the code of the
   current project:

   ```
   gw --act code-doc "document the public APIs in the gw package"
   ```

3. **Use interactive mode for exploratory work.**
   Acts such as `task` or `help` have `gw.interactive = true`. Run them
   without arguments and chat with the assistant. When you are done,
   type `.` to leave, or `>` to skip directly to the next step.

4. **Create your own act.**
   Place a new file `my-act.toml` in your user acts directory (the path
   configured via `gw.acts`). The simplest version inherits from `task`:

   ```toml
   basedOn = "task"

   description = "My custom workflow."

   prompt = "Run my custom workflow on the current folder."
   ```

   Then run it:

   ```
   gw --act my-act
   ```

5. **Use an absolute path for one-off acts.**
   You can also point Ghostwriter directly at a file on disk:

   ```
   gw --act C:\path\to\my-act.toml "with this prompt"
   ```

   When an absolute file path is used, classpath lookup is **not**
   performed.

## Built-in Acts

The following acts are shipped with Ghostwriter (under
`src/main/resources/acts/`).

### `help`

A guided assistant for the Act feature itself. Use it to list available
acts, view detailed act definitions, and understand how acts work
(including inheritance and overrides). Runs in interactive mode and
limits scanning to the current directory.

**When to use.** You want to learn about acts, look up the contents of
a specific act, or get troubleshooting advice for using or authoring
acts.

### `task`

A minimal, generic template that simply forwards your prompt to the AI
within the current project context. It defines no prologue or epilogue,
no special scanning rules — just a clean starting point.

**When to use.** As a base for custom acts (`basedOn = "task"`), or
when you want to ask a one-off question or request a custom action and
let the AI rely solely on your prompt and the project environment.
Interactive mode is enabled by default.

### `code-doc`

Automates the generation and update of documentation comments
(Javadoc, Python docstrings, XML comments, etc.) in source files. It
analyses the code, produces clear and accurate documentation for
classes, methods and functions, and inserts or updates the comments in
the language-appropriate style — without changing program logic.

**When to use.** You need to add or improve in-source documentation
across a project quickly and consistently.

### `commit`

Reviews uncommitted changes in the current project, groups them by type
(feature, fix, refactor, docs, chore, ...), generates concise commit
messages that follow the project's historical style, and runs the
matching version-control commands automatically. It outputs plain text
results suitable for scripts and pipelines.

**When to use.** You have local changes and want Ghostwriter to commit
them in well-organised, well-described batches.

### `grype-fix`

Reads the output of a Grype vulnerability scan, updates affected
dependencies in your build files (e.g. `pom.xml`) to safe versions,
verifies that the project still builds, and documents every change
(including the corresponding CVE/GHSA reference). Designed for Maven
projects, including multi-module ones.

**When to use.** You want to remediate dependency vulnerabilities
reported by Grype with as little manual work as possible. Requires
**Syft** and **Grype** installed and available on the system PATH.

### `sonar-fix`

A Lead-Java-Security-Engineer-style act that fetches issues from your
SonarQube server (using `${sonar.host}`, `${sonar.token}`,
`${sonar.qualities}` and `${sonar.severity}` placeholders), fixes the
reported problems according to OWASP and CWE best practices, ensures
the project still compiles and that all unit tests pass, and saves the
list of changed files to the `UPDATED_FILES_REPORT` project context
variable.

**When to use.** You need an automated, security-focused pass over
SonarQube findings, with new or updated unit tests and no changes to
SonarQube configuration or quality gates.

### `unit-tests`

Improves test coverage for the project: builds it with Maven, generates
a JaCoCo coverage report, and creates new high-quality unit tests for
classes and methods that are uncovered or under-covered. The act aims
for at least 90% coverage of the relevant source folder, supports
static-method testing libraries, and may refactor production code only
when strictly required for testability.

**When to use.** You want to raise the quality and coverage of the test
suite without rewriting existing, passing tests.

## Summary

The Act feature is the recommended way to drive Ghostwriter:

- **Reusable.** Each act is a small, self-contained TOML file.
- **Composable.** Acts can inherit from each other through `basedOn`,
  and runtime configuration values can be injected via `${...}`
  placeholders.
- **Flexible.** Acts run either as one-shot, non-interactive commands
  or as interactive chat sessions, with `.` to exit and `>` to continue.
- **Structured.** Multi-step workflows are expressed as *episodes*,
  with explicit selection (`name#2`, `name#1,3!`) and flow control.
- **Extensible.** New acts can live in your own `gw.acts` directory or
  be referenced by absolute path, and they can override or extend the
  built-in templates.

Key entry points in the code:

- `ActProcessor.setAct(String)` — parses the `--act` argument, loads
  the act, applies its data and selects episodes.
- `ActProcessor.loadAct(...)` — implements lookup (user dir + classpath)
  and `basedOn` inheritance.
- `ActProcessor.applyActData(...)` — applies merged act data to the
  processor's runtime state and configuration.
- `Episodes` — orchestrates ordered, selected, repeated and redirected
  episode execution.
- `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` (`.`) and
  `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` (`>`) — control
  flow during interactive sessions.

Together, these pieces let you turn repetitive AI-assisted tasks into
named, shareable, version-controlled artefacts that any team member can
run with a single command.
