---
<!-- @guidance: 
Create the Act page as a Project Information page for the project:
- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Create a separate section describing the action's interactive/non-interactive mode.
  - An action can be used as a non-interactive command to perform a predefined task without any additional data.
  - An action can be used interactively (as a chat). This is necessary when the user does not have full information about the desired action before initiating it.
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
---

# Act

An Act is a reusable task definition for Ghostwriter. It stores instructions, prompt templates, and runtime options in a TOML file so a task can be started by name instead of being rewritten each time.

In practice, an Act works like a prepared workflow. You choose an act, optionally add your own request text, and Ghostwriter uses the act definition together with the current project context to perform the task.

The feature is implemented by `ActProcessor`. This class loads Act files, resolves inheritance, applies configuration values, prepares prompts, supports multi-step execution, and then runs Ghostwriter on the selected files or directories.

## What an Act can define

An Act file can contain values such as:

- `description`: a short explanation of the act.
- `instructions`: the main guidance sent to the AI.
- `inputs`: the prompt template, or a list of prompts for multiple episodes.
- `prompt`: a default prompt used when the user does not supply one.
- `basedOn`: the parent act used for inheritance.
- `gw.*` settings such as scan scope, recursion, thread count, model, and interactive mode.
- other properties that are stored in the configurator and can be used later.

When an Act is started, Ghostwriter usually does the following:

1. Reads the requested act name.
2. Separates the act name from any additional user prompt text.
3. Loads the act from built-in resources, a custom act directory, or a direct TOML file path.
4. Resolves inheritance through `basedOn`.
5. Merges values from parent and child acts.
6. Applies prompt formatting and runtime options.
7. Processes the selected files or directories.

## Where Acts are loaded from

Ghostwriter supports several Act sources:

- built-in acts from the classpath under `src/main/resources/acts`,
- user-defined acts from a configured external directory,
- an absolute path or URL ending with `.toml`.

Important behavior:

- A built-in act and a user-defined act with the same name can both be loaded.
- The user-defined act can extend or override the built-in one.
- If the act name ends with `.toml`, it is treated as a direct file reference.
- If an absolute TOML file path is used, classpath hierarchy lookup is not used for that act name.

## Basic Act structure

A simple Act file can look like this:

```toml
description = '''
Short explanation of the act.
'''

instructions = '''
You are an assistant that performs a specific task.
'''

inputs = '''
# Task

%s
'''
```

In this structure:

- `instructions` defines how the AI should behave.
- `inputs` defines the user-facing request template.
- `%s` is the place where the active prompt text is inserted.

## Interactive and non-interactive mode

Acts support both interactive and non-interactive use.

### Non-interactive mode

In non-interactive mode, an Act behaves like a predefined command. You run it, Ghostwriter performs the task, and the workflow can complete without a chat-style conversation.

This is useful when:

- the task is already clearly defined,
- the act contains enough built-in guidance,
- you want repeatable automation,
- you want predictable file processing.

Examples include `code-doc`, `release-notes`, `unit-tests`, `grype-fix`, and `commit`.

### Interactive mode

In interactive mode, the Act is used more like a chat assistant. This is useful when the user does not yet have the full request ready before starting, or when the task benefits from back-and-forth clarification.

An Act enables this with:

```toml
gw.interactive = true
```

Built-in examples include:

- `help`, which is designed for question-and-answer usage,
- `task`, which is a general-purpose interactive act.

### Reserved command name

The act name or interactive command `exit` is reserved for terminating a process and should not be used as a custom act name.

## Using the `prompt` property

An Act can define a default prompt with the `prompt` property:

```toml
prompt = "Perform the default and special rules."
```

This value is used when the user starts the act without supplying their own prompt text.

`ActProcessor.setAct(String act)` first separates the act name from any extra user text. If no extra text is provided, the processor uses the current default prompt. During prompt preparation, `applayPrompt` replaces `%s` in `inputs` with the user prompt when present, or with the act's `prompt` value when the user did not provide one.

Example:

```toml
instructions = '''
You are an expert reviewer.
'''

inputs = '''
# Task

Review the following request:
%s
'''

prompt = "Perform the standard review."
```

If the act is started without additional request text, the final prompt becomes:

```text
# Task

Review the following request:
Perform the standard review.
```

This is useful for acts that should still perform a sensible default task when started without extra input. The built-in `sonar-fix` act uses this pattern.

## Using episodes

Acts can define multiple prompt steps by storing `inputs` as an array instead of a single string. In `ActProcessor`, these prompts are stored as separate episodes.

Example:

```toml
inputs = [
  "Step 1 prompt",
  "Step 2 prompt",
  "Step 3 prompt"
]
```

This allows one Act to perform a staged workflow instead of a single prompt.

### How episodes work

When multiple inputs are present:

- Ghostwriter starts with the first episode.
- Each episode uses its own prompt text.
- The processor can continue to the next episode automatically.
- The current episode can be repeated.
- Execution can move to a specific episode.

This behavior is supported by episode state in `ActProcessor`, including:

- `activeEpisodeId`,
- `episodeIds`,
- `requestedEpisodeId`,
- handling for repeat and move exceptions.

### Selecting episodes

`ActProcessor` uses `#` to separate an act name from episode selection.

Example:

```text
my-act#1,3
```

This means Ghostwriter should run episodes 1 and 3.

A trailing `!` disables the normal follow-up order after the selected episodes.

Example:

```text
my-act#2!
```

This means Ghostwriter runs episode 2 and then stops instead of continuing with the remaining episodes.

Episode numbers are validated and must be between 1 and the total number of prompts.

### Moving and repeating during execution

The processor supports:

- repeating the current episode,
- moving to a chosen episode,
- continuing through all available episodes or selected episodes.

This makes episodes useful for guided workflows and more advanced task automation.

## How inheritance works

Inheritance is controlled by the `basedOn` property.

Example:

```toml
basedOn = "task"
```

When Ghostwriter loads an Act with `basedOn`, `loadAct` does the following:

1. Loads the requested act from the custom directory and classpath if available.
2. Checks for `basedOn` in the loaded act data.
3. Loads the parent act recursively.
4. Applies parent values first.
5. Applies child values afterward.

This means the child act inherits from the parent and can override or extend it.

### Inheritance between built-in and custom acts

If both a built-in act and a user-defined act have the same name:

- the built-in act can provide the base definition,
- the user-defined act can add new values,
- the user-defined act can override built-in values.

This allows local customization without replacing the whole concept of the original act.

### String inheritance using `%s`

Inheritance is not only simple replacement. `setActData` also supports string composition.

If a property already exists and both old and new values are strings, the processor replaces `%s` in the existing value with the new value.

Example:

Parent act:

```toml
instructions = '''
Base rules:
%s
'''
```

Child act:

```toml
basedOn = "parent"

instructions = '''
Add Java-specific rules.
'''
```

Effective result:

```text
Base rules:
Add Java-specific rules.
```

This is how acts can extend parent templates instead of fully replacing them.

### Runtime inheritance from current configuration

After act data is loaded, `applyActData` checks whether the configurator already contains a value for the same key.

If it does, and the act value is a string containing `%s`, Ghostwriter replaces `%s` with the existing configured value.

This allows values from the current runtime configuration to be inserted into Act templates.

As a result, effective values can come from multiple layers:

1. current runtime configuration,
2. built-in act values,
3. user-defined act values,
4. parent act inheritance through `basedOn`,
5. final child act overrides.

### How non-string values are handled

`setActData` also processes non-string TOML values:

- booleans are converted to strings,
- integers are converted to strings,
- doubles are converted to strings,
- arrays are stored as lists,
- an `inputs` array becomes a list of prompt episodes.

## Placeholder variables

Acts may use placeholder variables in the `${...}` format.

These placeholders are intended for dynamic substitution by the application or functional tools at runtime. They can be used to access values such as:

- environment variables,
- system properties,
- action properties,
- configurator-provided values.

Important rules:

- Placeholders must remain exactly in the `${...}` format.
- They must not be resolved, renamed, reformatted, or otherwise changed manually.
- The LLM must not alter these placeholders.
- They are meant for runtime substitution, not for documentation-time expansion.

Example:

```toml
some.property = "${MY_ENV_VAR}"
```

The value `${MY_ENV_VAR}` must stay unchanged until runtime processing uses it.

## Main `ActProcessor` behavior

`ActProcessor` is the main class behind this feature. Its responsibilities include:

- parsing the requested act name and optional prompt,
- loading built-in and user-defined act files,
- supporting direct `.toml` file paths and URLs,
- resolving `basedOn` inheritance recursively,
- merging act values into a final configuration map,
- applying settings such as instructions, prompts, concurrency, excludes, recursion, model, and interactive mode,
- managing multi-episode execution,
- processing project files with the configured prompt and instructions.

In the overall Ghostwriter project, Acts provide a reusable automation layer on top of normal file processing. Instead of manually rewriting the same request each time, users can save common workflows as named TOML definitions.

## Step-by-step usage examples

### Run a built-in act

1. Choose an act name, such as `code-doc`.
2. Optionally add your own request text.
3. Start Ghostwriter with that act.
4. Ghostwriter loads the TOML file.
5. The act's `instructions` and `inputs` are combined.
6. Matching files are processed.

Conceptual command pattern:

```text
--act <name> [your request text]
```

Example:

```text
--act code-doc Add missing documentation to public APIs.
```

### Run an interactive act

Example:

```text
--act help How do acts inherit from each other?
```

Because `help` is interactive, it works well for question-and-answer usage.

### Run selected episodes

Example:

```text
--act my-act:1,2
```

This runs the selected episodes from a multi-step act.

### Run an act from a direct TOML file

Example:

```text
--act C:/path/to/custom-act.toml
```

In this case, Ghostwriter loads that TOML file directly. Classpath hierarchy lookup is not used for that file name.

## Built-in acts

The following built-in acts are available in `src/main/resources/acts`.

### `code-doc`

Purpose: Adds or updates documentation comments in source code files.

Use it when: you want Ghostwriter to generate or improve Javadoc, docstrings, XML comments, or similar code documentation without changing program logic.

Key characteristics:

- focuses only on documentation comments,
- keeps existing code logic and structure unchanged,
- requires output to contain only the updated code file content,
- explicitly requires LF line endings and ignoring `.machai`.

### `commit`

Purpose: Reviews project changes, groups them into logical commit sets, creates commit messages, and supports automated commit execution.

Use it when: you want help analyzing local changes and turning them into structured version control commits.

Key characteristics:

- analyzes modified files in the current project and subdirectories,
- groups changes by type such as feature, fix, refactor, docs, or chore,
- asks the AI to generate commit messages in the style of the project's historical commits,
- includes `ft.command.denylist`, which can inherit and extend command restrictions.

### `grype-fix`

Purpose: Fixes dependency vulnerabilities found by Grype.

Use it when: you want to update vulnerable dependencies in a Maven project after a security scan.

Key characteristics:

- explains how to generate an SBOM with Syft,
- explains how to obtain Grype scan results,
- updates only dependencies with available fixes,
- verifies the project build after changes,
- is designed for multi-module Maven remediation.

### `help`

Purpose: Explains Ghostwriter Acts and helps users inspect available act definitions.

Use it when: you want to understand how acts work, list available acts, or inspect a specific act.

Key characteristics:

- intended for interactive use,
- sets `gw.scanDir = "."`,
- sets `gw.nonRecursive = true`,
- sets `gw.interactive = true`,
- includes links to project guidance and Act documentation.

### `release-notes`

Purpose: Generates release notes from commit history and writes them into `src/changes/changes.xml`.

Use it when: you need structured release documentation for a new version.

Key characteristics:

- collects commits between release-related versions,
- groups changes by change type,
- creates content for Maven changes XML,
- conditionally includes an issue attribute only when `ISSUE_ID` is defined,
- writes the result as a new `<release>` element.

### `sonar-fix`

Purpose: Reviews SonarQube issue reports and helps apply focused fixes in Java projects.

Use it when: you want to remediate code issues reported by SonarQube, including carefully documented suppressions when truly necessary.

Key characteristics:

- emphasizes minimal and precise fixes,
- includes detailed rules for `@SuppressWarnings` usage,
- defines a default `prompt` value,
- expects environment-specific SonarQube access details to be provided by customization,
- is suitable as a base act for custom SonarQube integration.

### `task`

Purpose: Provides a simple general-purpose act for project tasks.

Use it when: you want a minimal interactive act that mainly follows the user's own request.

Key characteristics:

- acts as a generic template,
- places the user request directly into `inputs`,
- sets `gw.interactive = true`,
- is a good starting point for custom acts.

### `unit-tests`

Purpose: Generates and improves unit tests to increase coverage.

Use it when: you want help analyzing test coverage, improving existing tests, and creating new tests where coverage is too low.

Key characteristics:

- tells the workflow to build the project first,
- uses JaCoCo coverage analysis,
- reviews and updates existing tests,
- allows limited production refactoring when strongly needed for testability,
- scans with `gw.scanDir = "glob:**/test/java"`.

## Choosing the right Act

If you are unsure which act to use:

- choose `task` for a general project request,
- choose `help` for questions about acts,
- choose `code-doc` for documentation comments,
- choose `unit-tests` for test coverage work,
- choose `release-notes` for release note generation,
- choose `commit` for grouped commit creation,
- choose `grype-fix` for dependency vulnerability remediation,
- choose `sonar-fix` for SonarQube issue remediation.

## Summary

The Act feature gives Ghostwriter a reusable and configurable way to run common tasks.

It fits into the project as a higher-level automation layer over file processing:

- TOML files define reusable behavior,
- `ActProcessor` loads and merges that behavior,
- prompts and instructions are prepared automatically,
- project files are processed according to the selected act.

This makes Ghostwriter easier to use for both new and experienced users, because repeated workflows can be saved once and run consistently many times.
