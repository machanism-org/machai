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
---

# Act

An Act is a reusable task definition for Ghostwriter. It is stored as a TOML file and tells Ghostwriter what kind of job to run, what instructions to send to the AI, what prompt text to build, and whether the task should run once or continue as a conversation.

In simple terms, an Act is a named recipe for AI-assisted work inside a project. Instead of writing the full instructions every time, you can run a prepared Act such as documentation generation, unit test generation, SonarQube fixing, or general project help.

Ghostwriter includes built-in Acts in `src/main/resources/acts`. It can also load custom Act files from a configured directory, from a URL, or from an absolute TOML file path.

## What an Act does

When Ghostwriter runs an Act, it performs these main steps:

1. It reads the requested Act definition from a TOML file.
2. It optionally loads parent Act definitions through `basedOn`.
3. It merges inherited and local values.
4. It applies the user prompt, or the Act's default `prompt` value when no user prompt is given.
5. It configures Ghostwriter settings such as instructions, scan options, model, exclusions, recursion, and interactive mode.
6. It processes the selected files or folders using the composed instructions and inputs.
7. If the Act uses episodes, it can run one or more prompt steps in sequence.

This behavior is implemented mainly in `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`.

## Main Act properties

An Act file is a TOML document. The most important properties are:

- `description`: human-readable explanation of the Act.
- `instructions`: system-level AI instructions that explain how the AI should behave.
- `inputs`: the main prompt template or a list of prompt templates.
- `prompt`: a default user prompt value used when the user does not provide one.
- `basedOn`: inherits values from another Act.
- `gw.*`: Ghostwriter-specific execution options.
- any other property: forwarded into the underlying configuration.

Common `gw.*` options seen in the built-in Acts include:

- `gw.scanDir`: which files or folders should be processed.
- `gw.nonRecursive`: whether folder scanning should stop at the current level.
- `gw.interactive`: whether the Act should continue as a chat session.
- `gw.threads`: degree of concurrency.
- `gw.excludes`: comma-separated exclusions.
- `gw.model`: AI model override.

## How Ghostwriter loads an Act

Ghostwriter can load an Act from more than one place:

- built-in classpath resources under `/acts`
- a configured external Acts directory
- a URL when the configured Acts location is an HTTP or HTTPS base URL
- an absolute TOML file path

If a built-in Act and a custom Act have the same name, Ghostwriter loads both. In practice, the custom Act can override or extend the built-in one.

If you use an absolute TOML file path as the Act name, Ghostwriter treats it as a direct file reference. In that case, classpath hierarchy lookup is not used.

## Inheritance and value merging

Act inheritance is controlled by `basedOn`.

### How inheritance works

1. Ghostwriter loads the requested Act.
2. If the Act contains `basedOn`, Ghostwriter first loads the parent Act.
3. Parent values are placed into the property map.
4. Child values are then merged on top of the inherited values.
5. After loading, Ghostwriter may also merge some values with existing runtime configuration values from the configurator.

### How string values are merged

For string properties, Ghostwriter uses `%s` as the merge placeholder.

- If the parent value is a string and the child value is also a string, the child value is inserted into the parent's `%s` position.
- If the parent string has no `%s`, the parent value effectively remains unchanged by placeholder replacement behavior.
- If there is already a runtime configurator value for the same property, that runtime value can also be inserted into `%s` during final application.

Example:

```toml
# parent.toml
instructions = "Base instructions: %s"

# child.toml
basedOn = "parent"
instructions = "Add project-specific rules."
```

Resulting instructions:

```text
Base instructions: Add project-specific rules.
```

### How list values are merged

If `inputs` or another property is defined as a TOML array, Ghostwriter merges items by position.

- Existing inherited values are converted into a list if needed.
- Each child item is inserted into the parent's `%s` placeholder at the same index.
- If the child list is longer, new extra items are added.
- If the parent list is longer, unmatched parent items remain, with `%s` replaced by child values when available.

This makes it possible to create episode sequences that extend a base sequence.

### When inherited values are overridden

A child Act overrides or extends inherited values when it defines the same property key.

- String values usually extend by placeholder replacement.
- Array values merge by index.
- New keys are simply added.
- During final application, configurator values may still be injected into `%s` placeholders for matching keys.

This gives Acts two layers of reuse:

- Act-to-Act inheritance through `basedOn`
- runtime inheritance from current configuration values

## Prompt handling and the `prompt` property

Ghostwriter supports two prompt sources:

- the prompt text entered by the user when starting the Act
- the `prompt` value defined inside the Act TOML file

If the user starts an Act with extra text after the Act name, Ghostwriter uses that text.
If the user does not provide extra text, Ghostwriter uses the Act's default `prompt` value.

The `inputs` property is treated as a template. Ghostwriter replaces `%s` in `inputs` with the resolved prompt value.

Example:

```toml
prompt = "summarize recent changes"
inputs = "# Task\n\n%s"
```

Behavior:

- `--act my-act review logging` produces `review logging` inside `inputs`
- `--act my-act` uses `summarize recent changes`

This is useful when an Act should still work even if the user starts it without a detailed request.

## Interactive and non-interactive mode

Acts can run in two different styles.

### Non-interactive mode

In non-interactive mode, the Act behaves like a one-shot command.

- Ghostwriter builds the final prompt.
- It runs the task.
- The process ends without waiting for more conversation.

This is useful for predefined actions such as generating documentation or creating tests.

### Interactive mode

In interactive mode, the Act continues as a chat after the first response.

This is helpful when the user does not yet know the full request at the beginning, or when the task needs several back-and-forth steps.

Built-in examples that enable interactive mode include:

- `commit`
- `help`
- `task`

Interactive mode is controlled by `gw.interactive = true`.

### Continue and exit commands

Interactive processing supports two special prompt commands defined in `AIFileProcessor`:

- `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` = `>`
- `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` = `.`

How they work:

- Enter `>` to continue without sending a new prompt. This lets the process move on after the last response.
- Enter `.` to terminate the current Act processing.

Important note:

- the Act name or interactive command `exit` is reserved for terminating a process
- use `.` as the special runtime exit command in interactive mode

## Using episodes

An Act can contain a single `inputs` string or a list of input strings.

When `inputs` is a list, Ghostwriter treats it as episodes: multiple ordered prompt steps within one Act.

### What episodes are for

Episodes allow one Act to guide the AI through several stages, for example:

1. analyze a problem
2. propose a fix
3. generate tests
4. summarize changes

### How episode execution works

- each item in the `inputs` list becomes one episode
- Ghostwriter can run them in regular order
- specific episodes can be selected from the Act name
- selected episodes can run before returning to the normal order, unless normal order is disabled

### Selecting episodes

Ghostwriter uses `#` to separate the Act name from the episode selection.

Examples:

- `my-act#1` selects episode 1
- `my-act#1,3` selects episodes 1 and 3
- `my-act#1!` selects episode 1 and then stops without continuing normal order

The `!` suffix disables continuation of the normal episode sequence after the explicitly selected episodes run.

### Why episodes matter

Episodes make complex workflows easier to organize. Instead of creating many small Act files, one Act can hold a structured multi-step process.

## Placeholder variables in `${...}` format

Acts may contain placeholder variables such as `${...}`.

These placeholders are intended for dynamic substitution at runtime by the application or functional tools. They can refer to values such as:

- environment variables
- system properties
- action properties
- other configuration values

Important rules:

- placeholders must stay exactly in `${...}` format
- they must not be resolved, changed, renamed, or reformatted by the LLM
- they are meant for runtime substitution, not documentation-time replacement

Examples from built-in Acts include:

- `${sonar.host}`
- `${sonar.token}`
- `${sonar.qualities}`
- `${sonar.severity}`

## How an Act fits into Ghostwriter

The Act feature gives Ghostwriter a reusable way to package AI behavior.

It connects:

- project context
- reusable prompt templates
- runtime configuration
- interactive workflows
- file and folder scanning
- specialized task definitions

In practice, Acts are the main way to turn repeated project tasks into named, shareable commands.

## Step-by-step example

### Example 1: run a built-in Act

1. Choose an Act, for example `code-doc`.
2. Start Ghostwriter with the Act name.
3. Optionally add your own prompt text.
4. Ghostwriter loads the TOML file.
5. It builds the final instructions and prompt.
6. It scans the configured files and processes them.

Conceptual command format:

```text
--act <name> [your request text]
```

Example:

```text
--act code-doc add missing Javadoc to public classes
```

### Example 2: run an Act without giving a prompt

If the Act defines `prompt`, Ghostwriter can use that default value.

```text
--act my-custom-act
```

Ghostwriter then inserts the Act's `prompt` value into the `inputs` template.

### Example 3: run selected episodes only

```text
--act my-act#2!
```

This runs episode 2 and stops without continuing the remaining normal order.

## Built-in Acts

### `code-doc`

Purpose:
Add or update code documentation comments such as Javadoc, docstrings, or equivalent language-specific comments.

When to use it:
Use this Act when source files need clearer developer documentation without changing program logic.

What it does:

- analyzes code elements that need documentation
- creates or improves documentation comments
- keeps the output limited to updated file content
- emphasizes LF line endings and no logic changes

### `commit`

Purpose:
Analyze working copy changes and help produce grouped commits with suitable commit messages and commands.

When to use it:
Use this Act when you want Ghostwriter to review current changes, group them logically, and support the commit process.

What it does:

- checks repository status
- groups changes by logical type
- prepares commit messages in project style
- provides version-control commands
- runs in interactive mode

### `grype-fix`

Purpose:
Fix vulnerabilities in project dependencies based on Grype scan results.

When to use it:
Use this Act when dependency vulnerabilities have been reported and available fixed versions should be applied.

What it does:

- generates or reads Grype scan results
- updates vulnerable dependencies where fixes exist
- verifies the build
- documents dependency updates and unresolved issues

### `help`

Purpose:
Provide user-friendly help about Ghostwriter Acts.

When to use it:
Use this Act when you want to understand what Acts are, inspect an Act, or learn how to build or run one.

What it does:

- explains the Act system in simple language
- summarizes Act structure and properties
- helps with inheritance and override behavior
- can retrieve and explain specific Act definitions
- runs in interactive mode

### `sonar-fix`

Purpose:
Review SonarQube issues and guide or automate safe fixes in code and tests.

When to use it:
Use this Act when a SonarQube report identifies bugs, code smells, security issues, or coverage problems that should be corrected.

What it does:

- queries SonarQube using configured placeholders
- identifies issues by file, line, rule, and message
- applies fixes and adds tests when needed
- validates builds and test results
- stores the list of changed files in the project context

This Act contains several `${...}` placeholders that must remain unchanged for runtime use.

### `task`

Purpose:
Provide a minimal general-purpose Act for custom project tasks.

When to use it:
Use this Act when you want a flexible starting point and do not need a more specialized built-in Act.

What it does:

- passes the user's request into a simple task template
- relies on the current project context
- enables interactive mode for follow-up conversation

### `unit-tests`

Purpose:
Generate or improve unit tests to raise coverage.

When to use it:
Use this Act when you want better automated test coverage for source code in the project.

What it does:

- builds the project first
- uses JaCoCo coverage data
- updates existing tests where needed
- generates new unit tests for uncovered logic
- aims for high coverage with meaningful assertions

## Practical tips for creating your own Act

A simple custom Act can look like this:

```toml
description = "Review a file and suggest improvements."
instructions = "You are a careful reviewer."
inputs = "# Task\n\n%s"
prompt = "review the selected file"

[gw]
interactive = true
scanDir = "."
```

Suggested workflow:

1. start with `description`, `instructions`, and `inputs`
2. add `prompt` if the Act should work without user-supplied text
3. add `gw.*` options to control scanning and behavior
4. use `basedOn` if you want to reuse an existing Act
5. use an `inputs` array if the task should run in episodes
6. keep `${...}` placeholders unchanged when you need runtime substitution

## Summary

The Act feature is Ghostwriter's reusable automation layer for AI-driven project work.

It allows you to:

- define named tasks in TOML
- reuse and inherit behavior
- support both simple commands and interactive conversations
- provide default prompt values
- organize multi-step workflows with episodes
- connect project configuration and runtime placeholders to AI processing

For new users, the easiest way to start is to use the built-in `help`, `task`, `code-doc`, or `unit-tests` Acts and then create custom Acts as your workflow becomes more specific.
