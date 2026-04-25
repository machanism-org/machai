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

The Act feature lets Ghostwriter run a named, reusable task from a TOML file.
An act bundles together instructions for the AI, a prompt template, and optional Ghostwriter settings such as scan scope, recursion, thread count, and interactive mode. This makes common work easier to repeat because users can launch a prepared workflow instead of rewriting the same request every time.

In simple terms, an act is a saved recipe for working with the project.
You choose the act by name, optionally add your own request text, and Ghostwriter combines that information with the act configuration before processing files.

## What an act does

An act can define:

- `description`: a short explanation of what the act is for.
- `instructions`: the system-level guidance sent to the AI.
- `inputs`: the prompt template used to build the final user request.
- `prompt`: a default prompt value used when the user does not provide one.
- `basedOn`: another act to inherit from.
- `gw.*` properties such as `gw.scanDir`, `gw.nonRecursive`, `gw.interactive`, `gw.threads`, and `gw.excludes`.
- Additional properties that are forwarded into the configurator for other parts of the application or functional tools.

Built-in acts are loaded from `src/main/resources/acts`.
Ghostwriter can also load acts from a user-defined location, and a custom act can override or extend a built-in act with the same name.

## How Act works in Ghostwriter

The main logic is implemented in `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`.
Its role is to load an act, merge inherited values, build the final prompt, and apply the act settings to the current run.

At a high level, the process is:

1. Ghostwriter receives an act command.
2. The act name and optional prompt text are separated.
3. Ghostwriter loads the act TOML data.
4. If the act inherits from another act through `basedOn`, the parent act is loaded first.
5. Built-in and custom values are merged.
6. The `inputs` template is combined with the user prompt or default prompt.
7. Ghostwriter applies act settings such as instructions, scan options, model, and interactive mode.
8. Files are processed using the final instructions and prompt.

## Interactive and non-interactive use

Acts support two main ways of working.

### Non-interactive mode

In non-interactive mode, the act behaves like a predefined command.
You run it once, Ghostwriter applies the act settings, and the task proceeds without entering a chat session.

This mode is useful when:

- the task is well defined in advance;
- the act already contains enough instructions;
- you want repeatable automation;
- no back-and-forth discussion is needed.

Typical examples are documentation generation, release note generation, vulnerability remediation, and unit test creation.

You activate this by using an act that does not enable `gw.interactive`, or by using configuration that keeps interactive mode disabled.

Example:

```text
--act code-doc
```

You can also add extra request text:

```text
--act release-notes Prepare notes for the next release
```

### Interactive mode

In interactive mode, the act is used as a chat-oriented helper.
This is useful when the user does not yet know the full request before starting, or when the task needs clarification while the conversation continues.

An interactive act enables `gw.interactive = true`.
Built-in examples include:

- `help`
- `task`

When interactive mode is active, the act gives Ghostwriter a starting role and prompt structure, but the user can continue the conversation and refine the request.

Example:

```text
--act help
```

or:

```text
--act task Help me plan a refactoring of the parser package
```

### Reserved interactive command

The name `exit` is reserved for terminating a process.
It must not be used as an act name or interactive command for normal act execution.

## Using the `prompt` property

An act can define a `prompt` property to supply a default prompt value.
This is used when the user starts the act without providing prompt text after the act name.

This is especially helpful when an act should perform a standard action even if the user gives no extra details.

For example, `src/main/resources/acts/sonar-fix.toml` contains:

```toml
prompt = "Perform the default and special rules."
```

That means a command such as:

```text
--act sonar-fix
```

can still run with a meaningful default request.

If the user does provide text, that user text takes priority.
If no text is provided, Ghostwriter falls back to the processor default prompt, and the act-level `prompt` value can then be used when building the final `inputs` content.

## How the final prompt is built

The `inputs` property is a template.
Most built-in acts include `%s` inside `inputs`.
Ghostwriter replaces `%s` with the effective prompt text.

That effective prompt text usually comes from one of these sources:

1. the prompt text entered by the user after the act name;
2. the processor's current default prompt;
3. the act's `prompt` property, when used as the fallback during prompt construction.

This allows one act file to stay reusable while still accepting task-specific details.

## Inheritance and overriding

Ghostwriter supports inheritance through the `basedOn` property.
This is one of the most important parts of the Act feature.

### How inheritance works

When an act contains:

```toml
basedOn = "parent-act"
```

Ghostwriter first loads `parent-act`, then loads the child act.
The child act can reuse the parent configuration and override only the values it wants to change.

### What can be inherited

Any TOML value handled by the processor can be inherited, including:

- `instructions`
- `inputs`
- `description`
- `prompt`
- `gw.*` values
- other forwarded configuration properties

### How values are merged

The merge behavior is not just simple replacement.
String values can be composed using `%s`.
This is important for templates.

The processor applies values in this general order:

1. load custom act data if available;
2. load built-in act data if available;
3. determine `basedOn` from the custom act first, otherwise from the built-in act;
4. if `basedOn` exists, load the parent recursively;
5. merge child values into the accumulated property map;
6. when applying the final properties, replace `%s` with inherited configurator values when relevant.

### String template inheritance

If a property already exists and a later string value is loaded for the same key, Ghostwriter combines them by replacing `%s` in the existing value with the new value.
This allows a child act to extend a parent template instead of replacing it completely.

A simple example:

Parent act:

```toml
instructions = "Base instructions: %s"
```

Child act:

```toml
basedOn = "parent"
instructions = "Add Java-specific rules."
```

Resulting instructions:

```text
Base instructions: Add Java-specific rules.
```

The same pattern can be used for `inputs` and other string properties.

### Configurator inheritance during apply

After act data is loaded, Ghostwriter applies it to the current configuration.
If the configurator already contains a value for the same key, and the act value contains `%s`, Ghostwriter replaces `%s` with the existing configurator value.
This allows current runtime defaults to flow into the act.

In practice, that means values can come from several places:

- built-in act files;
- custom act files;
- parent acts via `basedOn`;
- current configurator defaults already active in the processor.

The later applied value can override earlier values, or extend them through `%s`.

## Placeholders in `${...}` format

Some values may contain placeholder variables written as `${...}`.
These placeholders are for runtime substitution by functional tools or configuration sources.

Examples of what they may refer to include:

- environment variables;
- system properties;
- action properties;
- configurator-provided values;
- other runtime parameters.

Important rules:

- `${...}` placeholders must remain exactly as written.
- They must not be resolved, rewritten, or altered by the LLM.
- They are intentionally preserved so the application or tools can replace them later.

This placeholder format is different from `%s`.

- `%s` is used by Ghostwriter to compose inherited templates and inject prompt text.
- `${...}` is used for later dynamic substitution by the surrounding system.

## Built-in acts

Ghostwriter includes the following built-in act files in `src/main/resources/acts`.
Each one targets a specific type of work.

### `code-doc`

Purpose: add or update code documentation comments.

Use this act when you want Ghostwriter to write or improve Javadoc, docstrings, or similar code comments without changing program logic. It is useful for improving maintainability and helping future developers understand the code.

### `commit`

Purpose: analyze local changes and prepare or execute grouped commits.

Use this act when you want help reviewing modified files, grouping related changes, creating commit messages in the project's style, and running version control commands. It is aimed at automating commit preparation and execution.

### `grype-fix`

Purpose: fix dependency vulnerabilities found by Grype.

Use this act when you have Grype scan results and want Ghostwriter to update affected dependencies, verify the build, and document the changes. It is especially useful for Maven projects and dependency maintenance.

### `help`

Purpose: explain the Act feature itself.

Use this act when you want to list acts, inspect a specific act, understand act inheritance, or get user-friendly guidance on how act configuration works. This act is interactive.

### `release-notes`

Purpose: generate release notes from commit history.

Use this act when you want Ghostwriter to collect relevant commit messages, group changes, and add a new release entry to `src/changes/changes.xml`. It is intended for release preparation.

### `sonar-fix`

Purpose: review and fix SonarQube issues.

Use this act when you want Ghostwriter to analyze a SonarQube report, apply minimal code corrections, and use carefully documented `@SuppressWarnings` annotations only when necessary. It also defines a default `prompt` value.

### `task`

Purpose: provide a generic project-aware assistant.

Use this act when you want an interactive starting point for a custom request that does not fit one of the more specialized acts. It is a simple general-purpose act.

### `unit-tests`

Purpose: improve test coverage by generating or updating unit tests.

Use this act when you want Ghostwriter to analyze coverage, update existing tests, create missing tests, and help move the project toward stronger automated test coverage.

## Loading acts from different locations

Ghostwriter can load acts from more than one source.

### Built-in classpath acts

Built-in acts are loaded from the classpath under `/acts`, which in this project corresponds to `src/main/resources/acts`.

### User-defined acts

A separate acts directory can be configured.
If an act exists there, Ghostwriter loads it as a custom act.
A custom act can complement or override a built-in act with the same name.

### Absolute TOML path

An absolute path to a TOML file can also be used as the act name.
When the file name is an absolute TOML path, Ghostwriter loads that file directly.
In that case, hierarchy using classpath resources is not supported.

## Practical examples

### Example 1: run a predefined task

```text
--act code-doc
```

Ghostwriter loads the `code-doc` act, uses its documentation-focused instructions, builds the prompt from its `inputs`, and processes matching files.

### Example 2: run an act with extra request text

```text
--act unit-tests Focus on parser edge cases and static utility methods
```

Ghostwriter uses `unit-tests` and inserts the extra request into the `%s` slot in the act's `inputs` template.

### Example 3: ask for act help interactively

```text
--act help Explain how basedOn works
```

Ghostwriter starts the interactive help act and uses the provided question inside the act's prompt template.

### Example 4: use a default prompt from the act

```text
--act sonar-fix
```

If no extra prompt text is provided, the act can still proceed using its default `prompt` value together with its predefined `inputs` instructions.

## Why Act matters in this project

The Act feature gives Ghostwriter a reusable, configurable way to perform common project tasks.
Instead of hardcoding one workflow for every situation, the project uses TOML-based acts to define repeatable behavior in a form that is easy to read, version, customize, and extend.

This helps Ghostwriter fit many use cases, including:

- code documentation;
- unit test generation;
- commit support;
- release note generation;
- security and quality remediation;
- interactive project guidance.

In short, acts are the project's configurable task layer.
They connect project-aware AI processing with reusable instructions and runtime settings.
