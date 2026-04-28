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
It is stored in a TOML file and gives Ghostwriter a prepared way to work, instead of requiring the user to write the full setup every time.

In practice, an act can provide:

- a user-friendly description;
- AI instructions;
- a prompt template;
- optional default prompt text;
- Ghostwriter runtime settings such as scan scope, recursion, thread count, model, excludes, and interactive mode;
- inherited values from another act.

Acts are useful when the same kind of work needs to be repeated, such as writing code documentation, generating unit tests, preparing release notes, or reviewing quality issues.

## What the Act feature does

The Act feature lets Ghostwriter:

1. load a named TOML file;
2. optionally combine it with a parent act through inheritance;
3. merge built-in and custom act definitions;
4. build the final prompt from the act template and user input;
5. apply the act settings to the current Ghostwriter run;
6. process matching project files using the resulting instructions.

This makes acts the configurable task layer of the project.
They connect reusable task definitions with actual project processing.

## Where acts come from

The built-in acts in this project are stored in:

- `src/main/resources/acts`

The current built-in act files are:

- `code-doc.toml`
- `commit.toml`
- `grype-fix.toml`
- `help.toml`
- `release-notes.toml`
- `sonar-fix.toml`
- `task.toml`
- `unit-tests.toml`

Ghostwriter can also load acts from a user-defined act directory.
If a custom act has the same name as a built-in act, both can participate in the final result.
The custom act can override or extend the built-in one.

## How Act is processed

The main processing logic is implemented in:

- `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`

This class is responsible for parsing the act command, loading TOML data, handling inheritance, composing values, and applying the result to the processor.

### Main methods and responsibilities

#### `setAct(String act)`

This method starts act processing.
It:

- uses `help` when no act name is provided;
- splits the input into the act name and optional prompt text;
- loads the act data;
- builds the final `inputs` value;
- applies the act properties to the running processor.

#### `loadAct(String name, Map<String, Object> properties, String actsLocation)`

This method loads act data from:

- a user-defined directory, when available;
- the built-in classpath resources.

It also checks `basedOn` and loads parent acts first.

#### `setActData(Map<String, Object> properties, TomlParseResult toml)`

This method copies TOML values into the working property map.
It also performs string composition when a value already exists.
That is how inherited templates can be extended instead of simply replaced.

#### `applyActData(Map<String, Object> properties)`

This method applies the loaded values to Ghostwriter.
Depending on the property name, it can:

- set instructions;
- set the final default prompt;
- enable or disable interactive mode;
- change thread count;
- set excludes;
- disable recursion;
- set the model;
- forward other values into the configurator.

## Interactive and non-interactive mode

Acts can be used in two different ways.

### Non-interactive mode

A non-interactive act behaves like a ready-to-run command.
You launch it, Ghostwriter applies the act configuration, and the task runs without opening a chat workflow.

This is best when:

- the task is already clear;
- the act contains enough instructions by itself;
- repeatable automation is preferred.

Examples of mainly non-interactive acts in this project include:

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

Or with extra request text:

```text
--act release-notes Prepare the next release entry
```

### Interactive mode

An interactive act starts Ghostwriter in a chat-like mode.
This is useful when the user does not yet know the full request before starting or expects to refine the request during the conversation.

Interactive mode is enabled with:

```toml
gw.interactive = true
```

Built-in interactive acts include:

- `help`
- `task`

Example:

```text
--act help
```

Or:

```text
--act task Help me design a safer parser update
```

### Reserved command name

The act name or interactive command `exit` is reserved for terminating a process.
It must not be used as a normal act name.

## Using the `prompt` property

An act can define a `prompt` property to provide default prompt text.
This value is helpful when the user runs the act without adding any request text after the act name.

Example from `sonar-fix.toml`:

```toml
prompt = "Perform the default and special rules."
```

That allows this command to run with meaningful prompt content even when the user provides nothing more:

```text
--act sonar-fix
```

### How the prompt is chosen

In `setAct`, Ghostwriter first determines the user prompt text.

- If the user writes text after the act name, that text is used.
- If not, Ghostwriter uses the processor's current default prompt.
- When the act `inputs` template is built, the act-level `prompt` value is available as a fallback value.

This makes the `prompt` property a practical default for acts that should still do something useful even with no additional user text.

## How the final prompt is built

Most acts define an `inputs` property containing `%s`.
Ghostwriter replaces `%s` with the effective prompt text.

Example pattern:

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

Ghostwriter inserts the user text into `%s` and uses the result as the final prompt.

## Inheritance and overriding

Acts support inheritance through the `basedOn` property.
This allows one act to reuse another act and only change the parts that need to be different.

### Basic inheritance

Example:

```toml
basedOn = "parent-act"
```

When this appears, Ghostwriter loads `parent-act` first and then applies the child act.

### Sources involved in the final result

The final act data can be influenced by several sources:

1. built-in act values;
2. custom act values from the configured act directory;
3. parent act values loaded through `basedOn`;
4. existing configurator values already active in Ghostwriter.

### How loading order works

Based on `ActProcessor.loadAct`, Ghostwriter does the following:

1. try to load the custom act;
2. try to load the built-in act;
3. determine `basedOn`, preferring the custom act value when present;
4. recursively load the parent act first;
5. keep merging values into one shared property map.

If neither custom nor built-in data exists, the act is considered missing.

### How string inheritance works

`setActData` does more than plain replacement.
If the same string key already exists in the property map, the processor replaces `%s` inside the existing value with the new value.

That means a parent template can be extended by a child.

Example:

Parent act:

```toml
instructions = "Base instructions: %s"
```

Child act:

```toml
basedOn = "parent"
instructions = "Add Java-specific rules."
```

Result:

```text
Base instructions: Add Java-specific rules.
```

This can be used with:

- `instructions`
- `inputs`
- `description`
- `prompt`
- other string properties

### How configurator inheritance works during apply

`applyActData` performs another important step.
If the configurator already has a value for the same key, and the act value contains `%s`, Ghostwriter replaces `%s` with the existing configurator value.

So inheritance is not only between parent and child act files.
It can also include runtime defaults that already exist in the configurator.

### When values override instead of extend

If a child value does not rely on `%s`, it effectively replaces the older value.
This gives act authors both options:

- extend a parent value with `%s`;
- override a parent value with a complete replacement.

## Placeholder variables in `${...}` format

Some act values may contain placeholder variables in the `${...}` format.
These placeholders are meant for dynamic substitution at runtime by functional tools or configuration sources.

Typical uses include retrieving values from:

- environment variables;
- system properties;
- action properties;
- configurator-provided values;
- similar runtime sources.

Important rules:

- `${...}` must remain exactly as written.
- The LLM must not resolve, change, or rewrite these placeholders.
- They are intentionally preserved for runtime processing by the application.

This is different from `%s`:

- `%s` is used by Ghostwriter to compose prompts and inherited string templates.
- `${...}` is used for later dynamic substitution by the surrounding system.

## Using an absolute TOML path

An absolute path to a TOML file can be used as the act name.
When that happens, Ghostwriter loads that file directly.

In this case, hierarchy using classpath resources is not supported.
That means the file is treated as a direct act source instead of part of the normal built-in classpath act hierarchy.

## Built-in acts

### `code-doc`

Purpose: add or update documentation comments in source code.

Use this act when you want Ghostwriter to improve Javadoc, docstrings, or similar code comments without changing the code logic itself.
It focuses on clear documentation, language-appropriate comment style, and returning only the updated file content.

### `commit`

Purpose: analyze project changes and prepare or execute commits.

Use this act when you want Ghostwriter to inspect modified files, group related changes, create commit messages in the project's style, and run version control commands through the command tool.
It is intended for commit automation and structured commit preparation.

### `grype-fix`

Purpose: fix dependency vulnerabilities reported by Grype.

Use this act when you have Grype scan results and want Ghostwriter to update vulnerable dependencies, verify the Maven build, and document why the dependencies were changed.
It is especially useful for vulnerability remediation in Maven projects.

### `help`

Purpose: explain how acts work and help users inspect them.

Use this act when you want to list acts, understand act configuration, inspect a specific act, or learn how inheritance and overrides behave.
This act runs in interactive mode.

### `release-notes`

Purpose: generate release notes from commit history.

Use this act when you want Ghostwriter to collect commit messages, group them by change type, and add a new release entry to `src/changes/changes.xml`.
It is intended for release preparation work.

### `sonar-fix`

Purpose: review and fix SonarQube issues.

Use this act when you want Ghostwriter to process SonarQube report data, apply focused Java fixes, and use tightly controlled `@SuppressWarnings` handling only when necessary.
This act also defines a default `prompt` value.

### `task`

Purpose: provide a general project-aware assistant template.

Use this act when your request does not fit a more specialized act.
It is a minimal interactive act that passes the user's prompt into a simple task template.

### `unit-tests`

Purpose: generate or improve unit tests.

Use this act when you want Ghostwriter to build the project, analyze JaCoCo coverage, update existing tests, create missing tests, and improve overall test coverage for the project.

## Step-by-step usage examples

### Example 1: run a ready-made act

```text
--act code-doc
```

Ghostwriter loads the `code-doc` act, applies its documentation instructions, builds the final prompt from its `inputs`, and processes the matching files.

### Example 2: run an act with your own request text

```text
--act unit-tests Focus on parser edge cases and static helper methods
```

Ghostwriter inserts the extra request into the act's `%s` template and uses the result as the final prompt.

### Example 3: start an interactive act

```text
--act help Explain how basedOn works
```

Ghostwriter starts the `help` act in interactive mode and uses the supplied request as the starting question.

### Example 4: rely on an act default prompt

```text
--act sonar-fix
```

If no additional text is provided, the act can still proceed using its default `prompt` behavior together with its predefined `inputs` template.

## Summary

The Act feature gives Ghostwriter a flexible way to package reusable work into TOML files.
It lets the project define repeatable tasks in a form that is easy to version, extend, override, and run.

In this project, acts support important workflows such as:

- documentation updates;
- commit preparation;
- vulnerability remediation;
- release note generation;
- SonarQube issue fixing;
- unit test generation;
- interactive user guidance.

In short, acts are how Ghostwriter turns reusable configuration into practical project-aware actions.
