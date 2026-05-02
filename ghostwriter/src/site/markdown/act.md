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

An Act is a reusable task definition for Ghostwriter. It lets you save instructions, prompt text, and runtime options in a TOML file, then run that file by name instead of rewriting the same request every time.

In simple terms, an Act is a prepared workflow. You choose an act, optionally add your own request text, and Ghostwriter combines the act configuration with the current project context to perform the task.

Acts are loaded and executed by `ActProcessor`. This processor reads the TOML file, merges inherited values, applies configuration, prepares the final prompt, and then runs Ghostwriter on the selected files or directories.

## What an Act does

An Act can define:

- `description`: a short explanation of the act.
- `instructions`: the main system-style guidance sent to the AI.
- `inputs`: the user prompt template or a list of prompts.
- `prompt`: a default prompt value used when the user does not provide one.
- `basedOn`: inheritance from another act.
- `gw.*` properties such as scan scope, recursion, concurrency, and interactive mode.
- Additional properties that are passed into the runtime configurator for later use.

When an act is started, Ghostwriter typically follows this flow:

1. Read the requested act name.
2. Split the act command into the act name and optional user prompt.
3. Load the act from built-in resources, a custom act directory, or an absolute TOML file path.
4. Resolve inheritance with `basedOn`.
5. Merge values from parent acts, built-in acts, custom acts, and current configuration.
6. Build the final prompt from `inputs` and the provided or default prompt.
7. Apply Ghostwriter options such as scanning rules or interactive mode.
8. Process the matching files or directories.

## Where acts are loaded from

Ghostwriter can load acts from more than one place:

- Built-in acts on the classpath under `src/main/resources/acts`.
- A user-defined act directory configured through the runtime configuration.
- An absolute path to a `.toml` file.

The loading behavior is important:

- If a built-in act and a custom act use the same name, both may be loaded.
- The custom act can override or extend the built-in one.
- If an act name ends with `.toml`, it is treated as a direct file reference.
- When an absolute TOML file path is used as the act name, classpath hierarchy lookup is not used for that act name.

## Basic Act structure

A simple act file may look like this:

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

In this example:

- `instructions` tells the AI how to behave.
- `inputs` defines the request template.
- `%s` is the place where the user's prompt text is inserted.

## Interactive and non-interactive mode

Acts support both interactive and non-interactive use.

### Non-interactive mode

In non-interactive mode, an act behaves like a predefined command. You run it, Ghostwriter performs the task, and no chat-style conversation is required.

This is useful when:

- the task is well defined,
- the act already contains enough instructions,
- you want repeatable automation,
- you want to process files in a predictable way.

Examples include generating documentation, creating release notes, or running a focused remediation workflow.

### Interactive mode

In interactive mode, the act is used more like a chat assistant. This is useful when the user does not yet know the full request before starting, or when follow-up questions and clarification are needed.

An act enables this with:

```toml
gw.interactive = true
```

Built-in examples include:

- `help`, which is designed to answer questions about acts.
- `task`, which is a general-purpose interactive act.

### Reserved command name

The act name or interactive command `exit` is reserved for terminating a process and should not be used as a custom act name.

## Using the `prompt` property

An act can define a default prompt with the `prompt` property:

```toml
prompt = "Perform the default and special rules."
```

This value is used when the user starts the act without supplying their own prompt text.

This behavior comes from `ActProcessor.setAct(String act)`:

- If the user writes an act name followed by text, that text is used as the prompt input.
- If the user writes only the act name, Ghostwriter uses the processor's current default prompt.
- When `inputs` is a string template, Ghostwriter substitutes `%s` with the provided prompt, or with the act's `prompt` value when no user prompt was given.

A practical example:

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

If the user runs the act without extra text, the final prompt becomes:

```text
# Task

Review the following request:
Perform the standard review.
```

This is especially useful for acts that should do something sensible even when started without additional input.

## Using episodes

Acts can support multiple prompts in sequence by using `inputs` as an array instead of a single string. Each prompt in the array is treated as an episode.

`ActProcessor` stores these prompts and runs them one by one. This allows a multi-step workflow to be split into separate stages.

Conceptually, episodes are useful when you want one act to:

- perform several steps in order,
- ask for staged processing,
- repeat a step when needed,
- jump to another step during execution.

### How episodes work

If `inputs` contains multiple entries, Ghostwriter treats them as multiple episodes.

Example:

```toml
inputs = [
  "Step 1 prompt",
  "Step 2 prompt",
  "Step 3 prompt"
]
```

During execution:

- Ghostwriter starts with the first episode.
- It can continue to the next episode automatically.
- It can repeat the current episode.
- It can move to a specific episode.

The processor supports this through episode state such as the active episode ID, optional selected episode IDs, and explicit move or repeat handling.

### Selecting episodes explicitly

The act name can include episode selection using square brackets:

```text
my-act[1,3]
```

This means Ghostwriter should run only episodes 1 and 3, in that order.

Episode numbers are validated. They must be between 1 and the number of available prompts.

### Moving and repeating during execution

The processor handles:

- repeating the current episode,
- moving to a chosen episode,
- continuing until all selected episodes are complete.

This makes episodes useful for guided workflows and more advanced task automation.

## How inheritance works

Inheritance is controlled by the `basedOn` property.

Example:

```toml
basedOn = "task"
```

When Ghostwriter loads an act with `basedOn`:

1. It loads the child act.
2. It checks whether the child defines `basedOn`.
3. It loads the parent act recursively.
4. Parent properties are merged first.
5. Child properties are then applied on top.

Ghostwriter also supports a second level of override:

- built-in act values may be loaded,
- custom act values with the same name may then override or extend them.

### String inheritance and `%s`

String values are not only replaced. They can also be composed.

In `setActData`, if a property already exists and both old and new values are strings, Ghostwriter replaces `%s` in the existing value with the new value.

This means a parent act can define a reusable template and a child act can insert additional content into it.

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

Resulting effective value:

```text
Base rules:
Add Java-specific rules.
```

### Inheritance from current runtime configuration

After the act file is loaded, `applyActData` checks whether the configurator already has a value for the same key.

If it does, and the act value is a string containing `%s`, Ghostwriter replaces `%s` with the existing configured value.

This means effective values can come from several layers:

1. current runtime configuration,
2. built-in act,
3. custom act,
4. parent act inheritance,
5. child act overrides.

### Override behavior summary

The general rule is:

- parent values are loaded first,
- child values override or extend parent values,
- custom acts can override built-in acts,
- existing runtime configuration can be inserted into string templates using `%s`.

For non-string values:

- booleans, integers, and doubles are stored as string configuration values,
- arrays are stored as lists,
- `inputs` arrays become multi-episode prompt lists.

## Placeholder variables

Acts may use placeholder variables in the `${...}` format.

These placeholders are intended for dynamic substitution by the application and functional tools at runtime. They may refer to values such as:

- environment variables,
- system properties,
- action properties,
- other configurator-provided values.

Important rules:

- Placeholders must stay exactly in the `${...}` format.
- They must not be resolved, rewritten, renamed, or reformatted manually.
- The LLM must not alter these placeholders.
- They are meant for runtime substitution, not for documentation-time expansion.

Example:

```toml
some.property = "${MY_ENV_VAR}"
```

The value `${MY_ENV_VAR}` must remain unchanged in the act file until runtime processing uses it.

## Main processor behavior

`ActProcessor` is the core class behind this feature. Its key responsibilities include:

- parsing the requested act name and optional prompt,
- loading built-in and custom act files,
- supporting direct absolute TOML file paths,
- resolving `basedOn` inheritance recursively,
- merging act values into a final configuration map,
- applying settings such as instructions, prompts, concurrency, excludes, recursion, model, and interactive mode,
- managing multi-episode execution,
- processing project files with the configured prompt and instructions.

In the overall Ghostwriter project, the Act feature provides reusable automation on top of the normal file-processing workflow. Instead of crafting every request manually, users can package common tasks into named TOML definitions.

## Step-by-step usage example

### Run a built-in act

1. Choose an act name, for example `code-doc`.
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

### Run an act from an absolute TOML file

You can also use a direct file path as the act name:

```text
--act C:/path/to/custom-act.toml
```

In this case, Ghostwriter loads that TOML file directly. Classpath hierarchy lookup is not used for that file name.

## Built-in acts

The following built-in acts are available in `src/main/resources/acts`.

### `code-doc`

Purpose: Adds or updates documentation comments in source code.

Use it when: you want Ghostwriter to generate or improve Javadoc, docstrings, or similar code documentation without changing program logic.

Notable behavior:

- Focuses on documentation comments only.
- Emphasizes formatting consistency.
- Returns updated code content.

### `commit`

Purpose: Analyzes project changes, groups them into logical commit sets, generates commit messages, and helps perform commits.

Use it when: you want automated support for reviewing local changes and creating structured version control commits.

Notable behavior:

- Looks at changes in the current project.
- Groups changes by type such as feature, fix, refactor, docs, or chore.
- Includes functional-tool-related command configuration through `ft.command.denylist`.

### `grype-fix`

Purpose: Fixes dependency vulnerabilities reported by Grype.

Use it when: you want help updating vulnerable dependencies in a Maven project after a security scan.

Notable behavior:

- Guides SBOM generation with Syft.
- Uses Grype results to identify fixed versions.
- Verifies changes by rebuilding the project.
- Supports multi-module dependency remediation.

### `help`

Purpose: Explains Ghostwriter acts and helps users inspect available act definitions.

Use it when: you want to understand how acts work, list acts, or inspect a specific act.

Notable behavior:

- Designed for interactive use.
- Uses `gw.scanDir="."` and `gw.nonRecursive="true"`.
- Enables `gw.interactive = true`.
- Explains act structure, inheritance, and command usage.

### `release-notes`

Purpose: Generates release notes from commit history and writes them into `src/changes/changes.xml`.

Use it when: you need structured release documentation for a new version.

Notable behavior:

- Collects commits between release-related versions.
- Groups content by change type.
- Writes output in Maven changes XML format.
- Supports optional issue references.

### `sonar-fix`

Purpose: Reviews SonarQube issues and helps apply focused fixes in Java projects.

Use it when: you want to remediate code issues reported by SonarQube, including carefully documented suppressions when truly necessary.

Notable behavior:

- Strong focus on minimal and safe fixes.
- Includes strict suppression rules for `@SuppressWarnings`.
- Defines a default `prompt` value.
- Intended to be extended with environment-specific SonarQube access details.

### `task`

Purpose: Provides a minimal general-purpose act for project tasks.

Use it when: you want a simple interactive act that mainly follows the user's own request.

Notable behavior:

- Acts as a generic template.
- Uses `gw.interactive = true`.
- Good starting point for custom acts.

### `unit-tests`

Purpose: Generates and improves unit tests to increase test coverage.

Use it when: you want help analyzing coverage, updating existing tests, and creating new tests for insufficiently tested code.

Notable behavior:

- Builds the project first.
- Uses JaCoCo coverage analysis.
- Targets test generation and possible testability refactoring.
- Scans with `gw.scanDir="glob:**/test/java"`.

## Choosing the right act

If you are unsure which act to use:

- choose `task` for a general custom request,
- choose `help` for questions about acts,
- choose `code-doc` for documentation comments,
- choose `unit-tests` for test coverage work,
- choose `release-notes` for release documentation,
- choose `commit` for grouped commit preparation,
- choose `grype-fix` for dependency vulnerability remediation,
- choose `sonar-fix` for SonarQube issue remediation.

## Summary

The Act feature gives Ghostwriter a reusable, configurable way to run common tasks.

It fits into the project as a higher-level automation layer over the file processor:

- TOML files define reusable behavior,
- `ActProcessor` loads and merges that behavior,
- prompts and instructions are prepared automatically,
- project files are then processed according to the selected act.

This makes Ghostwriter easier to use for both new and experienced users, because repeated workflows can be saved once and run many times with consistent behavior.
