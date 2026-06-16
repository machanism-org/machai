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

An **Act** is a reusable task definition for Ghostwriter. It is stored in a
small TOML file and tells Ghostwriter how to run a predefined AI-assisted
workflow.

An act usually contains:

- a `description` for humans,
- `instructions` that define the AI assistant's role,
- an `inputs` template that becomes the prompt,
- optional Ghostwriter settings such as `gw.path`, `gw.interactive`,
  `gw.nonRecursive`, or `gw.threads`.

Acts make repeated work easier. Instead of rewriting the same long prompt each
time, you can run a named act and let Ghostwriter load the saved template for
you.

Acts are loaded by `ActProcessor`. This class reads the TOML file, merges
inherited values, applies act settings to the processor, prepares episode
execution, and then runs the prompt against the project.

## What an Act Does

At a high level, an act helps Ghostwriter do four things:

1. **Load a named workflow** from a TOML file.
2. **Prepare the final prompt** by combining the act template with the user
   request.
3. **Apply runtime settings** such as scan rules or interactive mode.
4. **Execute one or more episodes** in the configured order.

The normal command format is:

```text
gw --act <name> [your request text]
```

You can also point directly to a TOML file:

```text
gw --act C:\path\to\my-act.toml [your request text]
```

When an absolute TOML path is used as the act name, Ghostwriter uses that file
only. In that case, lookup through bundled classpath resources is not used.

## How Act Loading Works

`ActProcessor.setAct(String)` is the main entry point for act loading.

In simple terms, it does this:

1. If no act name is provided, Ghostwriter uses `help`.
2. It separates the act name from the optional user prompt.
3. It detects optional episode selection syntax such as `help#2` or `task#1,3!`.
4. It calls `ActProcessor.loadAct(...)` to load and merge act data.
5. It applies the user prompt into the `inputs` template.
6. It applies act properties to the processor.
7. It runs the selected episode flow.

## Act File Structure

A typical act file can look like this:

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
paths = "glob:."
interactive = true
```

Common top-level properties:

| Property | Meaning |
|---|---|
| `description` | Human-readable explanation of the act. |
| `instructions` | System-style instructions for the AI assistant. |
| `inputs` | Prompt template. It may be a string or a list of episode strings. |
| `prompt` | Default user prompt if no prompt is supplied on the command line. |
| `basedOn` | Parent act name used for inheritance. |
| `gw.*` | Ghostwriter runtime configuration values. |
| other keys | Additional configuration values passed into the configurator. |

Common `gw.*` properties used by the built-in acts include:

| Property | Meaning |
|---|---|
| `gw.path` | What files or folders Ghostwriter should scan. |
| `gw.excludes` | Excluded paths. |
| `gw.nonRecursive` | Prevents recursion into child projects or modules. |
| `gw.threads` | Number of worker threads. |
| `gw.interactive` | Enables interactive chat-style act processing. |
| `gw.model` | Overrides the configured AI model. |
| `gw.acts` | Location of user-defined act files. |

## Interactive and Non-Interactive Mode

Acts can run in two different styles.

### Non-interactive mode

In non-interactive mode, the act is executed once using the resolved prompt and
then finishes.

Use this mode when:

- you already know exactly what you want,
- the act should run as part of automation,
- you want a simple one-shot task.

Example:

```text
gw --act commit
```

### Interactive mode

If `gw.interactive = true`, Ghostwriter keeps the act open as a conversation.
This is useful when you want to refine the task step by step or when you do not
know the full request before starting.

While the act is running interactively, two special prompt commands are used:

| Constant | Value | Meaning |
|---|---|---|
| `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` | `.` | End the current act session. |
| `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` | `>` | Continue act processing without entering a new prompt. |

Practical usage:

- type `.` on its own line to stop the act,
- type `>` on its own line to continue to the next processing step or episode.

This is especially helpful for multi-step acts and guided workflows.

## Default Prompt with `prompt`

An act can define a default user prompt with the `prompt` property. This value
is used when the user runs the act without providing extra request text.

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

This is handled during `setAct(...)`, where the processor falls back to the
current default prompt when the command does not include one.

## Episodes

An act can contain one prompt or multiple prompts.

When `inputs` is a single string, the act has one main prompt.
When `inputs` is a TOML array of strings, each array item becomes a separate
**episode**.

Example:

```toml
inputs = [
    '''
# Analyze
Review the code and list the most important issues.

%s
''',
    '''
# Fix
Apply the approved changes.
'''
]
```

In this case, Ghostwriter runs a sequence of episodes rather than a single step.
The `Episodes` helper manages the order and provides episode information to the
processor.

### Episode names

If an episode starts with a first-level heading such as `# Analyze`, that label
is used as the episode display name.

### Episode selection syntax

You can select specific episodes by adding `#...` after the act name.

Examples:

| Command | Meaning |
|---|---|
| `gw --act my-act` | Run all episodes in normal order. |
| `gw --act my-act#2` | Start from episode 2. |
| `gw --act my-act#1,3` | Request episodes 1 and 3 first, then continue in normal order. |
| `gw --act my-act#2!` | Run only episode 2 and stop normal order afterwards. |

Episode IDs are 1-based.

### Episode flow control

The implementation also supports moving through episodes programmatically.
During processing, Ghostwriter can:

- repeat the current episode,
- move to another episode by ID,
- move to another episode by name,
- continue with regular order,
- stop regular order when `!` is used.

This behavior is coordinated by the episode logic used by `ActProcessor` and the
related exception-based flow control classes such as `MoveToEpisodeException`.

## Inheritance and Value Merging

Acts can inherit from other acts with the `basedOn` property.

Example:

```toml
basedOn = "task"
```

This allows one act to reuse another act and override only the parts that need
to change.

### How inheritance is processed

`ActProcessor.loadAct(...)` loads act data from two possible sources:

1. the user-defined acts directory configured by `gw.acts`,
2. the built-in classpath resources under `/acts/`.

If both exist for the same act name, Ghostwriter loads both and applies their
values into one merged act definition.

The code then checks `basedOn`.
If a parent act is defined, that parent is loaded first, recursively, and the
child values are applied afterward.

### Override order

The effective order is:

1. parent built-in values,
2. parent custom values,
3. child built-in values,
4. child custom values.

This means later values can override or extend earlier ones.

### How string values are merged

When both parent and child define the same string property, Ghostwriter uses the
existing value as a template and replaces `%s` with the newer value.

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

If `inputs` or another property is a list, items are merged position by
position.

- matching child items replace `%s` inside matching parent items,
- extra child items are appended,
- a parent string can also be merged into a child list.

This is what allows a base act to define a shared prompt skeleton and a child
act to turn it into several episodes.

### Inherited configurator values

After act loading, `applyActData(...)` also checks the current configurator.
If a property already exists there, Ghostwriter may inject that value into the
act string through `%s` replacement.

This means an act can inherit not only from another act, but also from runtime
configuration that already exists in the processor.

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
These placeholders must stay exactly as written.

They are intended for dynamic substitution by functional tools at runtime.
They can be resolved from sources such as:

- environment variables,
- Java system properties,
- action properties,
- configurator properties,
- other runtime configuration sources.

Important rules:

- `${...}` placeholders are not for the LLM to resolve,
- they must not be edited, expanded, renamed, or removed,
- they should be passed through unchanged until runtime substitution happens.

Example from the built-in `sonar-fix` act:

```text
${sonar.host}/api/issues/search?componentKeys=...
```

In this example, `${sonar.host}` is kept unchanged in the act definition and is
resolved later by the application environment.

## Using an Act Step by Step

A simple real-world workflow looks like this:

1. **Start with help**

   ```text
   gw --act help
   ```

   This opens the built-in help act and explains available act behavior.

2. **Run a task-oriented act**

   ```text
   gw --act code-doc "add missing API documentation"
   ```

   Ghostwriter loads `code-doc.toml`, injects the request into `inputs`, and
   processes matching files.

3. **Use interactive mode if needed**

   For acts such as `help`, `task`, or `commit`, you can continue the session,
   refine the request, type `>` to continue, or type `.` to finish.

4. **Create a custom act**

   Put a file such as `my-act.toml` into the directory configured by `gw.acts`.
   A simple example is:

   ```toml
   basedOn = "task"
   description = "My custom project workflow."
   prompt = "Analyze the current folder and suggest improvements."
   ```

5. **Run the custom act**

   ```text
   gw --act my-act
   ```

6. **Use a direct TOML file path for one-off usage**

   ```text
   gw --act C:\temp\custom.toml "review this module"
   ```

## Built-in Acts

Ghostwriter includes several built-in act files in `src/main/resources/acts`.

### `code-doc`

**Purpose:** Adds or updates documentation comments in source files.

**When to use it:** Use this act when you want Ghostwriter to generate or
improve Javadoc, docstrings, XML comments, or equivalent documentation comments
without changing program logic.

**Notable behavior:** It focuses on documentation quality, language-appropriate
comment style, and outputting only updated file content.

### `commit`

**Purpose:** Reviews local version-control changes and helps commit them in
logical groups.

**When to use it:** Use this act when you want Ghostwriter to inspect changed
files, group them by change type, generate commit messages, and perform the
required VCS commands automatically.

**Notable behavior:** It is interactive, scans the current project, and includes
an `ft.command.denylist` section that can inherit values using `%s`.

### `grype-fix`

**Purpose:** Fixes dependency vulnerabilities reported by Grype.

**When to use it:** Use this act when you want to update vulnerable
dependencies, rebuild the project, and document security-related dependency
changes.

**Notable behavior:** It is aimed at Maven projects, explains how to generate an
SBOM with Syft, how to run Grype, and how to document fixed vulnerabilities.

### `help`

**Purpose:** Explains Ghostwriter acts and helps users inspect or understand
act definitions.

**When to use it:** Use this act when you need guidance about available acts,
act structure, inheritance, or act usage.

**Notable behavior:** It runs in interactive mode, keeps scanning local to the
current directory, and is designed as a user-facing help assistant.

### `sonar-fix`

**Purpose:** Reviews SonarQube issues and applies code fixes based on security,
quality, and maintainability rules.

**When to use it:** Use this act when you want Ghostwriter to fetch SonarQube
issues, fix them in code, add or update tests, validate the build, and record
changed files.

**Notable behavior:** It uses runtime placeholders such as `${sonar.host}`,
`${sonar.token}`, `${sonar.qualities}`, and `${sonar.severity}`. These values
must remain unchanged in the TOML file and are resolved at runtime.

### `task`

**Purpose:** Provides a minimal general-purpose act template.

**When to use it:** Use this act when you want a simple prompt-driven workflow
or when you want to build your own custom act on top of a lightweight base.

**Notable behavior:** It enables interactive mode and contains only a minimal
prompt structure.

### `unit-tests`

**Purpose:** Improves project test coverage by generating or updating unit
tests.

**When to use it:** Use this act when you want Ghostwriter to build the project,
measure JaCoCo coverage, identify uncovered code, and create high-quality unit
tests.

**Notable behavior:** It targets test-related scanning, aims for strong
coverage, and allows limited production refactoring when required for
better testability.

## Key Methods and Project Role

The Act feature is a core way to use Ghostwriter. It turns repeatable AI tasks
into named, version-controlled definitions that can be shared and reused.

Important implementation points include:

- `ActProcessor.setAct(String)` parses the act command and applies prompt and
  episode selection.
- `ActProcessor.loadAct(...)` loads acts from custom and built-in sources and
  resolves `basedOn` inheritance.
- `ActProcessor.setActData(...)` and related merge helpers combine TOML values.
- `ActProcessor.applyActData(...)` transfers resolved values into runtime
  processor settings.
- `Episodes` manages episode naming, ordering, selection, repetition, and jumps.

Together, these parts let Ghostwriter run anything from a simple one-step prompt
to a guided multi-step workflow.

## Summary

The Act feature gives Ghostwriter a structured and reusable way to perform work.
It is useful because it combines:

- reusable prompt templates,
- optional inheritance,
- configurable runtime settings,
- interactive and non-interactive execution,
- multi-step episode workflows,
- support for runtime placeholders in `${...}` format,
- built-in and user-defined act definitions.

For new users, the easiest place to start is:

```text
gw --act help
```

From there, you can explore built-in acts, run them directly, or create your
own custom act files for repeatable project workflows.
