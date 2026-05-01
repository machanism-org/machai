---
<!-- @guidance: 
Create the Act page as a Project Information page for the project:
- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Create a separate section describing the action's interactive/non-interactive mode.
  - An action can be used as a non-interactive command to perform a predefined task without any additional data.
  - An action can be used interactively (as a chat). This is necessary when the user does not have full information about the desired action before initiating it.
  - Describe how it is activated and used.
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

An Act is a reusable task definition for Ghostwriter.
It is stored in a TOML file and gives Ghostwriter a prepared way to run a task without requiring the user to write the full setup each time.

An Act can define:

- a user-facing description;
- AI instructions;
- one prompt or multiple prompts;
- an optional default `prompt` value;
- Ghostwriter runtime settings such as scan scope, recursion, model, interactive mode, thread count, and excludes;
- inheritance through `basedOn`;
- values that are passed to the configurator for runtime use.

Acts are the main reusable workflow unit in Ghostwriter.
They make common tasks easier to repeat, such as documenting code, preparing commits, fixing security findings, generating release notes, fixing SonarQube issues, or creating tests.

## What the Act feature does

The Act feature lets Ghostwriter:

1. load an Act by name or by direct TOML file path;
2. read built-in Acts from `src/main/resources/acts`;
3. optionally read user-defined Acts from a configured Act location;
4. merge built-in, custom, and inherited values;
5. build the final prompt from the Act template and the user request;
6. apply Ghostwriter settings from the Act;
7. process files or project content using the resulting instructions and prompts.

This makes an Act the link between reusable configuration and real project work.

## Where Acts are defined

Built-in Act files in this project are stored in:

- `src/main/resources/acts`

The built-in Acts are:

- `code-doc`
- `commit`
- `grype-fix`
- `help`
- `release-notes`
- `sonar-fix`
- `task`
- `unit-tests`

The main processing logic is implemented in:

- `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`

## How Act processing works

`ActProcessor` loads and applies an Act in several steps.

### `setAct(String act)`

This is the main entry point for Act execution.
It:

- uses `help` when no Act name is provided;
- splits the input into the Act name and optional user prompt text;
- reads optional episode selection from brackets such as `name[1,2]`;
- loads the Act configuration;
- inserts the prompt into the `inputs` template when `inputs` is a string;
- applies the final Act data to the processor.

If the user does not provide text after the Act name, Ghostwriter falls back to the current default prompt.
If the Act defines its own `prompt`, that value can serve as the default prompt content when `%s` is inserted into `inputs`.

### `loadAct(String name, Map<String, Object> properties, String actsLocation)`

This method loads an Act from:

- a configured user-defined Act directory or URL;
- built-in classpath resources under `/acts`.

It also resolves inheritance through `basedOn` by loading the parent Act first.
If the Act cannot be found, an `ActNotFound` error is raised.

### `setActData(Map<String, Object> properties, TomlParseResult toml)`

This method copies TOML values into a working property map.
It supports:

- strings;
- booleans;
- integers;
- doubles;
- arrays.

For strings, it also supports value composition.
If a string key already exists, the older value is treated as a template and `%s` is replaced with the newer value.
This is how inherited string values can be extended instead of only replaced.

### `applyActData(Map<String, Object> properties)`

This method applies loaded values to Ghostwriter.
Depending on the key, it can:

- set instructions;
- set one prompt or multiple prompts;
- enable or disable interactive mode;
- set thread count;
- set excludes;
- disable recursive scanning;
- set the model;
- forward other values to the configurator.

If a configurator value already exists for the same key and the Act value contains `%s`, Ghostwriter replaces `%s` with the existing configurator value before applying it.

## Interactive and non-interactive mode

Acts can be used in two main modes.

### Non-interactive mode

A non-interactive Act behaves like a predefined command.
You start it and Ghostwriter runs the task directly using the Act instructions and prompts.

This is useful when:

- the task is already well defined;
- the Act already contains enough guidance;
- you want repeatable automation.

This mode is typically controlled by the absence of `gw.interactive = true`.
Examples from this project include:

- `code-doc`
- `commit`
- `grype-fix`
- `release-notes`
- `sonar-fix`
- `unit-tests`

Example:

```text
--act code-doc
```

Or with extra prompt text:

```text
--act release-notes Prepare the next release entry
```

### Interactive mode

An interactive Act starts a chat-style workflow.
This is useful when the user does not yet know the full request before starting, or wants to refine the request during the conversation.

Interactive mode is enabled in the TOML file with:

```toml
gw.interactive = true
```

Built-in interactive Acts in this project are:

- `help`
- `task`

Example:

```text
--act help
```

Or:

```text
--act task Help me plan a parser improvement
```

### Reserved command

The Act name or interactive command `exit` is reserved for terminating a process.
It must not be used as a normal Act name.

## Using the `prompt` property

An Act can define a `prompt` property to provide a default user prompt.
This is useful when the user starts the Act without adding extra text after the Act name.

Example from `sonar-fix.toml`:

```toml
prompt = "Perform the default and special rules."
```

This allows a command such as:

```text
--act sonar-fix
```

### How prompt handling works

When `setAct` runs:

- if the user provides text after the Act name, that text is used;
- otherwise, Ghostwriter uses the current default prompt;
- when `inputs` is a string, Ghostwriter replaces `%s` with the effective prompt value;
- if the effective prompt is empty, the Act's `prompt` value can supply the default text used during prompt composition.

Example template:

```toml
inputs = '''
# Task

%s
'''
```

If the user runs:

```text
--act task Explain the current module structure
```

Ghostwriter inserts the user text into `%s` and sends the final prompt to the AI.

## Episode support

Acts can use multiple prompts as episodes.
This is supported when `inputs` is a TOML array instead of a single string.

In that case, `ActProcessor` stores the prompts as an array and can move through them as separate episodes.
This is useful for multi-step workflows where one step leads to the next.

### How episodes work

- each item in the `inputs` array is one episode prompt;
- Ghostwriter tracks the active episode internally;
- the command can request specific episode numbers using a suffix such as `name[1,2]`;
- `MoveToEpisodeException` can move processing to another episode;
- when more than one episode exists, Ghostwriter logs the active episode during execution.

### Example usage

```text
--act my-act[1,3]
```

This requests selected episodes from a multi-episode Act.
Episode numbers are 1-based, so episode `1` means the first prompt.
If an invalid episode number is requested, Ghostwriter throws an error.

## Inheritance and overriding

Acts support inheritance through the `basedOn` property.
This allows one Act to reuse another and change only the values that need to be different.

### Basic inheritance

```toml
basedOn = "parent-act"
```

When this property is present, Ghostwriter loads the parent first and then applies the child values.

### Value sources that can affect the final Act

The final Act can combine values from:

1. built-in Act files;
2. user-defined Act files;
3. parent Acts loaded through `basedOn`;
4. existing configurator values already active at runtime.

### Loading order

According to `ActProcessor.loadAct`, Ghostwriter:

1. tries to load a custom Act;
2. tries to load a built-in Act;
3. reads `basedOn`, preferring the custom value when available;
4. recursively loads the parent Act first;
5. keeps merging values into one property map.

If the same Act exists in both locations, the custom Act can extend or override the built-in one.

### How inherited string values are merged

`setActData` supports template-style inheritance for strings.
If a key already exists and is a string, Ghostwriter uses the older value as the template and replaces `%s` with the newer value.

Example:

Parent:

```toml
instructions = "Base rules: %s"
```

Child:

```toml
basedOn = "parent"
instructions = "Add Java-specific rules."
```

Result:

```text
Base rules: Add Java-specific rules.
```

This mechanism can affect values such as:

- `instructions`
- `inputs`
- `description`
- `prompt`
- other string properties

### When values override instead of extend

If the newer value does not depend on `%s`, it acts as a replacement in practice.
This gives Act authors two common options:

- extend inherited text using `%s`;
- replace inherited text with a full new value.

### Runtime inheritance from the configurator

`applyActData` performs another inheritance step.
If the configurator already contains a value for the same key and the Act value includes `%s`, Ghostwriter replaces `%s` with the existing configurator value before applying the result.

This means inheritance can come not only from parent Acts, but also from already active runtime configuration.

## Placeholder variables in `${...}` format

Act values may also contain placeholder variables in the `${...}` format.
These placeholders are meant for runtime substitution by functional tools or configuration sources.

They can be used to obtain values from sources such as:

- environment variables;
- system properties;
- action properties;
- configurator values;
- similar runtime providers.

Important rules:

- `${...}` must remain exactly unchanged;
- the LLM must not resolve, rewrite, or alter `${...}` placeholders;
- they are intended for dynamic runtime substitution by the application.

This is different from `%s`:

- `%s` is used by Ghostwriter for prompt composition and inherited string templates;
- `${...}` is reserved for separate runtime substitution.

## Using an absolute TOML file path

An absolute path to a TOML file can be used as the Act name.
When this happens, Ghostwriter loads that file directly.

In this case, hierarchy through classpath resources is not supported.
The file is treated as a direct Act source instead of part of the normal built-in Act hierarchy.

## Built-in Acts

### `code-doc`

Purpose: add or update documentation comments in source files.

Use this Act when you want Ghostwriter to improve Javadoc, docstrings, or similar documentation comments without changing code logic.
It focuses on accurate documentation, correct language-specific comment style, LF line endings, and returning only the updated file content.

### `commit`

Purpose: analyze local project changes and prepare or execute commits.

Use this Act when you want Ghostwriter to inspect modified files, group related changes, create commit messages that fit the project's style, and run version control commands through the command tool.
It is intended for commit preparation and automation.

### `grype-fix`

Purpose: fix dependency vulnerabilities reported by Grype.

Use this Act when you have Grype scan results and want Ghostwriter to update vulnerable dependencies, build the project, and document why the dependency versions were changed.
It is aimed at Maven-based vulnerability remediation.

### `help`

Purpose: explain Acts and help users inspect them.

Use this Act when you want to list available Acts, understand Act structure, inspect a specific Act, or learn how inheritance and overrides work.
This Act runs in interactive mode.

### `release-notes`

Purpose: generate release notes from commit history.

Use this Act when you want Ghostwriter to collect commit messages, group them by change type, and add a new release entry to `src/changes/changes.xml`.
It is intended for release preparation tasks.

### `sonar-fix`

Purpose: review and fix SonarQube issues in Java projects.

Use this Act when you want Ghostwriter to process SonarQube report data, apply focused Java fixes, and use `@SuppressWarnings` only when a direct code fix is not possible and properly justified.
This Act also defines a default `prompt` value.

### `task`

Purpose: provide a simple general-purpose project-aware assistant template.

Use this Act when your request does not fit a more specialized Act.
It is an interactive Act that passes the user's request into a minimal task template.

### `unit-tests`

Purpose: generate or improve unit tests.

Use this Act when you want Ghostwriter to build the project, analyze JaCoCo coverage, update existing tests, create missing tests, and improve meaningful test coverage.
It focuses on useful tests rather than only increasing coverage numbers.

## Step-by-step examples

### Example 1: run a predefined Act

```text
--act code-doc
```

Ghostwriter loads the `code-doc` Act, applies its documentation instructions, builds the final prompt from its `inputs`, and processes the matching files.

### Example 2: run an Act with your own request

```text
--act unit-tests Focus on parser edge cases and static helper methods
```

Ghostwriter inserts the extra request into the Act's `%s` template and uses the result as the final prompt.

### Example 3: start an interactive Act

```text
--act help Explain how basedOn works
```

Ghostwriter starts the `help` Act in interactive mode and uses the supplied request as the opening question.

### Example 4: rely on a default `prompt`

```text
--act sonar-fix
```

If no extra text is provided, the Act can still continue with its default `prompt` value together with its predefined `inputs` template.

### Example 5: use a direct TOML path

```text
--act C:/custom-acts/my-act.toml
```

Ghostwriter loads that file directly as the Act definition.
In that case, the normal classpath hierarchy is not used.

## Summary

The Act feature gives Ghostwriter a flexible way to package reusable work into TOML files.
It defines repeatable, project-aware tasks that are easy to store, reuse, extend, and override.

In this project, Acts support workflows such as:

- code documentation;
- commit preparation;
- dependency vulnerability remediation;
- release note generation;
- SonarQube issue fixing;
- unit test generation;
- interactive project guidance.

In short, Acts are how Ghostwriter turns reusable configuration into practical project actions.
