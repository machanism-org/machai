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

An Act is a reusable task definition for Ghostwriter. It stores instructions, prompt templates, and runtime options in a TOML file so you can start a task by name instead of rewriting the same request every time.

Acts are processed by `ActProcessor`. This class loads Act files, merges inherited values, applies configuration properties, prepares prompts, supports multi-step episode flows, and runs Ghostwriter with the resulting settings.

## What an Act does

An Act can define:

- `description`: a short explanation of the act.
- `instructions`: the main rules and behavior for the AI.
- `inputs`: the prompt template used to build the final request.
- `prompt`: a default user prompt used when no prompt text is supplied.
- `basedOn`: a parent act used for inheritance.
- `gw.*` properties such as scan directory, recursion, model, thread count, and interactive mode.
- other properties that are passed into the runtime configuration.

When you start an Act, Ghostwriter usually follows this flow:

1. Read the requested act name.
2. Separate the act name from any additional prompt text.
3. Load the Act from built-in resources, a custom act location, or a direct TOML file path or URL.
4. Resolve inheritance with `basedOn`.
5. Merge parent and child values.
6. Apply prompt formatting and runtime configuration.
7. Process the selected files or directories.

## Where Acts are loaded from

Ghostwriter can load Acts from:

- built-in classpath resources under `src/main/resources/acts`,
- a user-defined act directory or URL base,
- a direct TOML file reference.

Important details from `ActProcessor`:

- Built-in and user-defined acts with the same name can both participate in the final result.
- A user-defined act can extend or override a built-in act.
- If the requested name ends with `.toml`, Ghostwriter treats it as a direct TOML file reference.
- A direct TOML path or URL bypasses normal classpath hierarchy lookup for that act name.
- Remote TOML loading is supported for HTTP and HTTPS act locations.

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
- `inputs` defines the request template.
- `%s` marks where the active prompt text is inserted.

## Interactive and non-interactive mode

Acts support both interactive and non-interactive use.

### Non-interactive mode

In non-interactive mode, an Act behaves like a predefined command. You run it and Ghostwriter performs the task without needing a chat-style conversation.

This is useful when:

- the task is already well defined,
- the act already contains enough instructions,
- you want repeatable automation,
- you want predictable file processing.

Examples include `code-doc`, `commit`, `grype-fix`, `sonar-fix`, and `unit-tests`.

### Interactive mode

In interactive mode, an Act is used more like a chat assistant. This is useful when the user does not yet know the exact final request before starting.

An Act enables interactive mode with:

```toml
gw.interactive = true
```

Built-in examples include:

- `help`, which is meant for question-and-answer use,
- `task`, which is a general-purpose interactive act.

### Reserved command name

The act name or interactive command `exit` is reserved for terminating a process and should not be used as a custom act name.

## Using the `prompt` property

An Act can define a default prompt with the `prompt` property:

```toml
prompt = "Perform the default and special rules."
```

This value is used when the user starts the act without supplying extra prompt text.

In `ActProcessor.setAct(String act)`, Ghostwriter separates the act name from the user's additional text. If no prompt text is found, it falls back to the current default prompt. During prompt preparation, `applyPromptValues` and `applayPrompt` replace `%s` in `inputs` with the user prompt, or with the act's `prompt` value when the user did not provide one.

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

If the act is started without extra request text, the final prompt becomes:

```text
# Task

Review the following request:
Perform the standard review.
```

This is useful for acts that should still perform a sensible default task when started with no added prompt. The built-in `sonar-fix` act uses this pattern through its instructions and placeholder-driven configuration approach.

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

This allows one Act to run as a staged workflow instead of a single prompt.

### How episodes work

When multiple inputs are present:

- Ghostwriter starts with the first episode.
- Each episode uses its own prompt text.
- The processor can continue to the next episode automatically.
- The current episode can be repeated.
- Execution can move to a specific episode.

This behavior is managed in `ActProcessor` through state such as:

- `activeEpisodeId`,
- `episodeIds`,
- `requestedEpisodeId`,
- `disableNormalOrder`.

It also reacts to `RepeatEpisodeException` and `MoveToEpisodeException`.

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

This means Ghostwriter runs episode 2 and then stops instead of continuing with later episodes.

Episode numbers are validated and must be between 1 and the total number of prompts.

### Moving and repeating during execution

The processor supports:

- repeating the current episode,
- moving to a chosen episode,
- continuing through all available episodes or only selected ones.

This makes episodes useful for guided workflows and advanced task automation.

## How inheritance works

Inheritance is controlled by the `basedOn` property.

Example:

```toml
basedOn = "task"
```

When Ghostwriter loads an Act with `basedOn`, `loadAct` does the following:

1. Try loading the act from the custom location.
2. Try loading the act from built-in classpath resources.
3. Detect `basedOn` from the loaded TOML data.
4. Load the parent act recursively.
5. Apply parent values first.
6. Apply child values afterward.

This means the child act inherits parent values and can override or extend them.

### Built-in and custom inheritance together

If both a built-in act and a user-defined act have the same name:

- the built-in act can provide a base definition,
- the user-defined act can add more values,
- the user-defined act can override built-in values.

This allows local customization without replacing the whole built-in definition.

### String inheritance using `%s`

Inheritance is not just simple replacement. In `setActDataEntry` and `putStringActData`, if a property already exists and both values are strings, the existing value is treated like a template and `%s` is replaced with the new value.

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

This lets a child extend a parent template instead of fully replacing it.

### Array inheritance

If `inputs` or another array value is defined as a TOML array, `mergeTomlArrayValues` combines parent and child values by index.

This means:

- a child array item can fill `%s` in the parent item at the same position,
- additional child items can be appended when the child array is longer,
- a single string value can also be merged into a list.

This behavior is especially useful for episode-based acts.

### Runtime inheritance from current configuration

After act data is loaded, `applyActData` applies the values to the processor. Before a string property is applied, `resolveInheritedValue` checks whether the configurator already contains a value for the same key.

If it does, and the act value contains `%s`, Ghostwriter replaces `%s` with the existing configured value.

This allows the effective value to be built from several layers:

1. existing runtime configuration,
2. built-in act values,
3. user-defined act values,
4. parent act inheritance through `basedOn`,
5. final child overrides.

### How non-string values are handled

`ActProcessor` also converts TOML values when loading them:

- booleans are converted to strings,
- integers are converted to strings,
- doubles are converted to strings,
- arrays are stored as lists,
- an `inputs` array becomes the episode prompt list.

## Placeholder variables

Acts may use placeholder variables in the `${...}` format.

These placeholders are intended for dynamic substitution by the application or functional tools at runtime. They can be used to obtain values such as:

- environment variables,
- system properties,
- action properties,
- configurator-provided values.

Important rules:

- Placeholders must remain exactly in the `${...}` format.
- They must not be resolved, renamed, reformatted, or otherwise changed manually.
- The LLM must not alter these placeholders.
- They are meant for runtime substitution, not documentation-time expansion.

Example:

```toml
some.property = "${MY_ENV_VAR}"
```

The value `${MY_ENV_VAR}` must stay unchanged until runtime processing uses it.

## Main `ActProcessor` behavior

`ActProcessor` is the main class behind this feature. Its responsibilities include:

- parsing the requested act name and optional prompt,
- loading built-in and user-defined act files,
- supporting direct TOML paths and URLs,
- resolving `basedOn` inheritance recursively,
- merging string and array values,
- applying settings such as instructions, prompts, concurrency, excludes, recursion, model, and interactive mode,
- managing multi-episode execution,
- processing project files with the configured prompt and instructions.

In the overall Ghostwriter project, Acts provide a reusable automation layer on top of normal file processing. Instead of rewriting the same request every time, users can save common workflows as named TOML definitions.

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
--act my-act#1,2
```

This runs selected episodes from a multi-step act.

### Run an act from a direct TOML file

Example:

```text
--act C:/path/to/custom-act.toml
```

In this case, Ghostwriter loads that TOML file directly. Classpath hierarchy lookup is not used for that file name.

## Built-in acts

The following built-in acts are available in `src/main/resources/acts`.

### `code-doc`

Purpose: adds or updates documentation comments in source code files.

Use it when: you want Ghostwriter to generate or improve Javadoc, docstrings, XML comments, or similar code documentation without changing program logic.

Key points:

- focuses only on documentation comments,
- requires output to contain only the updated code file content,
- keeps code logic and structure unchanged,
- requires LF line endings and ignores `.machai`.

### `commit`

Purpose: analyzes project changes, groups them into logical commit sets, generates commit messages, and supports automated commit execution.

Use it when: you want help reviewing local changes and turning them into structured version control commits.

Key points:

- checks version control status in the current directory,
- groups changes by logical type such as feature, fix, refactor, docs, or chore,
- asks for commit messages that follow the style of the project's history,
- includes `ft.command.denylist`, which can inherit and extend command restrictions.

### `grype-fix`

Purpose: fixes dependency vulnerabilities found by Grype.

Use it when: you want to update vulnerable dependencies in a Maven project after a security scan.

Key points:

- explains how to create an SBOM with Syft,
- explains how to obtain Grype results,
- updates only dependencies with available fixes,
- verifies the build after changes,
- is designed for multi-module Maven vulnerability remediation.

### `help`

Purpose: explains Ghostwriter Acts and helps users inspect available act definitions.

Use it when: you want to understand how acts work, list available acts, or inspect a specific act.

Key points:

- intended for interactive use,
- sets `gw.scanDir = "."`,
- sets `gw.nonRecursive = true`,
- sets `gw.interactive = true`,
- includes links to guidance and Act documentation.

### `sonar-fix`

Purpose: reviews SonarQube issue reports and helps apply focused fixes in Java projects.

Use it when: you want to remediate code issues reported by SonarQube, including carefully controlled suppressions only when truly necessary.

Key points:

- emphasizes minimal, compliant, and testable fixes,
- contains detailed rules for coverage and `@SuppressWarnings` use,
- uses runtime placeholder variables such as `${sonar.host}`, `${sonar.token}`, `${sonar.qualities}`, and `${sonar.severity}`,
- stores the list of updated files in the `UPDATED_FILES_REPORT` project context variable,
- is suitable as a strong base for SonarQube-focused customization.

### `task`

Purpose: provides a minimal general-purpose act for project tasks.

Use it when: you want a simple interactive act that mainly follows the user's own request.

Key points:

- acts as a generic template,
- places the user request directly into `inputs`,
- sets `gw.interactive = true`,
- is a good starting point for custom acts.

### `unit-tests`

Purpose: generates and improves unit tests to increase coverage.

Use it when: you want help analyzing test coverage, improving existing tests, and creating new tests where coverage is too low.

Key points:

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
- choose `commit` for grouped commit creation,
- choose `grype-fix` for dependency vulnerability remediation,
- choose `sonar-fix` for SonarQube remediation.

## Summary

The Act feature gives Ghostwriter a reusable and configurable way to run common tasks.

It fits into the project as a higher-level automation layer over file processing:

- TOML files define reusable behavior,
- `ActProcessor` loads and merges that behavior,
- prompts and instructions are prepared automatically,
- project files are processed according to the selected act.

This makes Ghostwriter easier to use for both new and experienced users, because repeated workflows can be saved once and run consistently many times.
