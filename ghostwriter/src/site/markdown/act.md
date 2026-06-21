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

An **Act** is a reusable Ghostwriter task definition stored in a TOML file.
It lets you save a workflow once and run it again by name, instead of writing
and repeating the same long prompt every time.

An act can describe:

- what the AI assistant should do,
- what prompt template should be used,
- which files Ghostwriter should scan,
- whether the act should run once or as an interactive conversation,
- whether the work should happen in one step or in multiple episodes.

In the codebase, the main implementation lives in
`src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`.
That class loads act definitions, merges inherited values, applies runtime
configuration, prepares the final prompt, and executes the requested workflow.

Built-in act examples are stored in `src/main/resources/acts`.
These files show how acts are written and how they can be reused for tasks such
as help, documentation, unit test generation, commits, dependency fixes, and
SonarQube issue remediation.

## What an Act Does

At a high level, an act helps Ghostwriter do the following:

1. **Load a named workflow** from a TOML file.
2. **Read the act definition** from built-in resources, a configured act
   directory, or a direct TOML file path.
3. **Merge inherited values** if the act is based on another act.
4. **Insert the user prompt** into the act's `inputs` template.
5. **Apply Ghostwriter settings** such as path, excludes, model, threads, and
   interactive mode.
6. **Run one or more episodes** in the proper order.

A typical command looks like this:

```text
gw --act <name> [your request text]
```

Examples:

```text
gw --act help
gw --act code-doc "Add missing API documentation"
gw --act task "Review this module and suggest improvements"
```

## How Acts Are Loaded

`ActProcessor.setAct(String)` is the main entry point.
It performs the following steps:

1. If no act name is provided, it uses `help`.
2. It separates the act name from the optional user prompt.
3. It checks whether the act name includes episode selection syntax such as
   `my-act#2` or `my-act#1,3!`.
4. It calls `ActProcessor.loadAct(...)` to load the act data.
5. It inserts the prompt into the `inputs` value by replacing `%s`.
6. It applies loaded properties to the processor.
7. It starts the configured episodes.

Acts can be loaded from:

- built-in classpath resources under `/acts/`,
- an external act directory configured by `gw.acts`,
- a direct TOML file path.

If the act name is an absolute TOML file path, Ghostwriter uses that file
reference directly. In that case, classpath act lookup is not used.

### Acts Location Definition

Acts are not always part of your project’s source code or repository. Instead, they are configuration files that define reusable workflows for Ghostwriter. 
The location where acts are stored and loaded from is controlled by the `gw.acts` parameter.

#### How Acts Location Works

- **Acts Directory (`gw.acts`):**  
  The `gw.acts` parameter specifies the directory where user-defined act TOML files are located.  
  - If `gw.acts` is an **absolute path**, Ghostwriter uses that directory directly.
  - If `gw.acts` is a **relative path**, Ghostwriter resolves it relative to the current user working directory (the directory from which you run the command), not the project directory.

- **Built-in Acts:**  
  Ghostwriter also includes built-in acts stored in the classpath under `/acts/`. These are always available, regardless of your project or working directory.

- **Acts Are External:**  
  Because acts can be stored outside your project, you can maintain a shared library of act definitions for multiple projects, teams, or environments. This makes acts flexible and reusable.

#### Example

Suppose your project is in `/home/user/myproject`, and you run Ghostwriter from `/home/user`:

- If you set `gw.acts=acts`, Ghostwriter will look for acts in `/home/user/acts`.
- If you set `gw.acts=/opt/shared/acts`, Ghostwriter will look for acts in `/opt/shared/acts`.

This allows you to organize acts separately from your project code, and to use different act libraries for different workflows.

#### Practical Tip

To avoid confusion, always check your current working directory and the value of `gw.acts` when running Ghostwriter.  
If you want acts to be part of your project, use an absolute path or ensure you run Ghostwriter from your project directory.

## Act File Structure

A simple act file can look like this:

```toml
description = '''
Short explanation of what this act does.
'''

instructions = '''
You are an expert assistant.
'''

inputs = '''
# Task

%s
'''

prompt = "Default prompt text"
# basedOn = "task"

[gw]
path = "glob:."
interactive = true
```

Common properties:

| Property | Meaning |
|---|---|
| `description` | Human-readable explanation of the act. |
| `instructions` | Instructions that define the AI assistant's role and behavior. |
| `inputs` | Prompt template used to build the final request. It can be a string or a list of episode strings. |
| `prompt` | Default user prompt used when the command does not include one. |
| `basedOn` | Parent act name used for inheritance. |
| `gw.path` | Scan target for Ghostwriter. |
| `gw.excludes` | Excluded paths or patterns. |
| `gw.nonRecursive` | Prevents recursion into child projects or modules. |
| `gw.threads` | Number of worker threads. |
| `gw.interactive` | Enables interactive processing. |
| `gw.model` | Overrides the active AI model. |
| `gw.acts` | Directory that contains user-defined acts. |
| other keys | Additional configurator values forwarded to runtime configuration. |

## Interactive and Non-Interactive Mode

Acts support two execution styles.

### Non-interactive mode

In non-interactive mode, the act is executed once with the resolved prompt and
then ends.

Use this mode when:

- you already know exactly what should be done,
- the act should be used in automation or scripts,
- the task is a simple one-shot operation.

Example:

```text
gw --act code-doc "Document the public API"
```

### Interactive mode

If `gw.interactive = true`, the act behaves like a chat session.
This is useful when the user does not yet know the complete request, or when
multiple steps are needed before the work is finished.

Examples of built-in interactive acts include `help`, `task`, and `commit`.

During interactive processing, Ghostwriter uses two special commands defined by
`AIFileProcessor`:

| Constant | Value | Meaning |
|---|---|---|
| `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` | `.` | End the current act session. |
| `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` | `>` | Continue processing without entering a new prompt. |

Practical usage:

- type `.` on its own line to terminate the act,
- type `>` on its own line to continue to the next step or episode.

This is especially useful for guided, multi-step, or exploratory workflows.

## Default Prompt with `prompt`

An act can define a default prompt with the `prompt` property.
This value is used when the user runs the act without providing additional
request text.

Example:

```toml
description = "Simple project summary"
instructions = "You are a project analyst."
prompt = "Summarize this project for a new developer."
inputs = '''
# Task

%s
'''
```

Behavior:

- `gw --act describe "focus on the API layer"` uses `focus on the API layer`.
- `gw --act describe` uses `Summarize this project for a new developer.`

In `ActProcessor.setAct(...)`, Ghostwriter first reads the current default
prompt. If the command does not include a prompt, the processor falls back to
that default value.

## Episodes

An act can run as a single prompt or as multiple prompts called **episodes**.

- If `inputs` is a single string, the act has one prompt.
- If `inputs` is a TOML array of strings, each item becomes a separate episode.

Example:

```toml
inputs = [
    '''
# Analyze
Review the code and list the main issues.

%s
''',
    '''
# Fix
Apply the approved changes.
'''
]
```

In this case, Ghostwriter executes the act in more than one step.
The `Episodes` helper used by `ActProcessor` manages episode names, ordering,
selection, and transitions.

### Episode names

If an episode starts with a heading such as `# Analyze`, that heading is used
as the display name of the episode.

### Episode selection syntax

You can select episodes by adding `#...` after the act name.

Examples:

| Command | Meaning |
|---|---|
| `gw --act my-act` | Run all episodes in regular order. |
| `gw --act my-act#2` | Start with episode 2. |
| `gw --act my-act#1,3` | Request episodes 1 and 3 first, then continue in normal order. |
| `gw --act my-act#2!` | Run only episode 2 and stop afterward. |

Episode IDs are 1-based.

### Episode flow control

The implementation also supports moving through episodes programmatically.
During execution, Ghostwriter can:

- repeat the current episode,
- move to a specific episode by ID,
- move to a specific episode by name,
- continue with normal order,
- stop normal order when `!` is used.

This behavior is coordinated by the episode logic and related flow-control
classes such as `MoveToEpisodeException`.

## Inheritance and Value Merging

Acts can inherit from other acts by using the `basedOn` property.

Example:

```toml
basedOn = "task"
```

This allows you to reuse a base act and override only the parts that need to
change.

### How inheritance works

`ActProcessor.loadAct(...)` loads act definitions from two main places:

1. a user-defined act directory configured with `gw.acts`,
2. built-in classpath resources under `/acts/`.

If both versions exist for the same act name, both can participate in the
merged result.

The processor then checks `basedOn`.
If a parent act is defined, that parent is loaded first, recursively.
After that, the child values are applied.

### Effective override order

The effective merge order is:

1. parent built-in values,
2. parent user-defined values,
3. child built-in values,
4. child user-defined values.

Later values can override or extend earlier values.

### How string values are merged

When both parent and child define the same string property, Ghostwriter uses
`%s` replacement.
The existing value acts as a template, and the new value is inserted into it.

Example:

Parent:

```toml
instructions = '''
You are helping with %s.
'''
```

Child:

```toml
instructions = "documentation tasks"
```

Merged result:

```toml
instructions = '''
You are helping with documentation tasks.
'''
```

### How list values are merged

If a property such as `inputs` is a list, values are merged by position.

- matching child items replace `%s` in matching parent items,
- extra child items are appended,
- a parent string can also be merged into a child list,
- a child string can also be merged into a parent list.

This makes it possible for a base act to define a reusable prompt skeleton and
for a child act to expand that skeleton into multiple episodes.

### Runtime inheritance from configurator values

After loading the act, `applyActData(...)` also checks the current configurator.
If a property already exists there, Ghostwriter may inject that runtime value
into the act string by replacing `%s`.

That means an act can inherit values not only from another act, but also from
runtime configuration that already exists in the processor.

### Simple inheritance example

Parent act:

```toml
instructions = '''
You are working on the %s project.
'''
inputs = '''
# Task
%s
'''
```

Child act:

```toml
basedOn = "base"
instructions = "Ghostwriter"
inputs = '''
Create a summary.
%s
'''
```

Merged result:

```toml
instructions = '''
You are working on the Ghostwriter project.
'''
inputs = '''
# Task
Create a summary.
%s
'''
```

At runtime, the remaining `%s` in `inputs` is replaced with the actual user
prompt.

## Placeholder Variables in `${...}` Format

Act files may contain placeholders such as `${sonar.host}` or `${sonar.token}`.
These placeholders must remain exactly as written.

They are designed for dynamic substitution by functional tools or runtime
configuration later in the process.
They may come from sources such as:

- environment variables,
- Java system properties,
- act properties,
- configurator values,
- other runtime configuration sources.

Important rules:

- `${...}` placeholders are not resolved by the LLM,
- they must not be changed, expanded, renamed, or removed,
- they must stay unchanged until runtime substitution happens.

Example from `sonar-fix.toml`:

```text
${sonar.host}/api/issues/search?componentKeys=[COMPONENT_KEYS]&ps=20
```

Here, `${sonar.host}` must stay exactly as written in the act file.

## Using an Act Step by Step

A typical workflow looks like this:

1. **Start with help**

   ```text
   gw --act help
   ```

   This opens the built-in help act and explains available act behavior.

2. **Run a task-focused act**

   ```text
   gw --act code-doc "add missing API documentation"
   ```

   Ghostwriter loads `code-doc.toml`, inserts the request into `inputs`, and
   processes matching files.

3. **Use interactive mode when needed**

   For interactive acts such as `help`, `task`, or `commit`, you can continue
   the session, refine the request, enter `>` to continue, or enter `.` to end
   the act.

4. **Create a custom act**

   Put a file such as `my-act.toml` into the directory configured by `gw.acts`.
   Example:

   ```toml
   basedOn = "task"
   description = "My custom project workflow."
   prompt = "Analyze the current folder and suggest improvements."
   ```

5. **Run the custom act**

   ```text
   gw --act my-act
   ```

6. **Use a direct TOML path for one-off usage**

   ```text
   gw --act C:\temp\custom.toml "review this module"
   ```

## Built-in Acts

The project includes the following built-in acts in
`src/main/resources/acts`.

### `code-doc`

**Purpose:** Adds or updates documentation comments in source files.

**When to use it:** Use this act when you want Ghostwriter to generate or
improve Javadoc, docstrings, XML comments, or similar documentation comments
without changing application logic.

**Notable behavior:** It focuses on documentation quality, language-appropriate
comment style, LF line endings, and returning only the updated file content.

### `commit`

**Purpose:** Analyzes local version-control changes and helps commit them in
logical groups.

**When to use it:** Use this act when you want Ghostwriter to inspect changed
files, group related modifications, generate commit messages, and automate the
required Git or SVN commands.

**Notable behavior:** It runs in interactive mode, scans the current project,
and defines an `ft.command.denylist` value that can participate in `%s`-based
inheritance.

### `grype-fix`

**Purpose:** Fixes dependency vulnerabilities reported by Grype.

**When to use it:** Use this act when you want to update vulnerable
dependencies, rebuild the project, and document security-related dependency
changes.

**Notable behavior:** It is aimed at Maven projects, explains how to generate
an SBOM with Syft, how to run Grype, and how to document each vulnerability fix.

### `help`

**Purpose:** Explains Ghostwriter acts and helps users inspect or understand
act definitions.

**When to use it:** Use this act when you need guidance about available acts,
act structure, inheritance, prompts, or act usage.

**Notable behavior:** It runs in interactive mode, stays focused on the current
directory, and is designed as a user-facing support act.

### `sonar-fix`

**Purpose:** Reviews SonarQube issues and applies code fixes based on security,
quality, and maintainability rules.

**When to use it:** Use this act when you want Ghostwriter to fetch SonarQube
issues, fix them in code, add or update tests, validate the build, and save the
list of changed files.

**Notable behavior:** It uses runtime placeholders such as `${sonar.host}`,
`${sonar.token}`, `${sonar.qualities}`, and `${sonar.severity}`. These values
must remain unchanged and are resolved later at runtime.

### `task`

**Purpose:** Provides a minimal general-purpose act template.

**When to use it:** Use this act when you want a simple prompt-driven workflow
or when you want to build your own custom act on top of a lightweight base.

**Notable behavior:** It enables interactive mode and contains a minimal `# Task`
prompt structure with `%s` insertion for the user request.

### `unit-tests`

**Purpose:** Improves project test coverage by generating or updating unit
tests.

**When to use it:** Use this act when you want Ghostwriter to build the project,
measure JaCoCo coverage, identify uncovered code, and create strong unit tests.

**Notable behavior:** It targets test-related paths, aims for high coverage,
and allows limited production refactoring when required for better testability.

## Key Methods and Project Role

The Act feature is one of the core ways to use Ghostwriter.
It turns repeatable AI-assisted workflows into named, reusable, versioned
configuration files.

Important implementation points include:

- `ActProcessor.setAct(String)` parses the command, extracts the act name and
  prompt, and applies episode selection.
- `ActProcessor.loadAct(...)` loads acts from built-in and external sources and
  resolves `basedOn` inheritance.
- `ActProcessor.setActData(...)` merges TOML values into a combined property map.
- `ActProcessor.applyActData(...)` applies merged values to the processor and
  runtime configuration.
- `applyPromptValues(...)` inserts the user's prompt into `inputs`.
- the `Episodes` helper manages episode naming, order, selection, and movement
  between episodes.

Together, these pieces let Ghostwriter run anything from a simple one-step task
to a guided multi-step workflow.

## Summary

The Act feature gives Ghostwriter a structured way to define reusable work.
It combines:

- reusable prompt templates,
- optional inheritance,
- runtime configuration,
- interactive and non-interactive execution,
- episode-based workflows,
- support for runtime placeholders in `${...}` format,
- built-in and user-defined act definitions.

For new users, the easiest place to begin is:

```text
gw --act help
```

From there, you can explore built-in acts, run them directly, or create your
own custom TOML act files for repeatable project workflows.
