---
canonical: https://machai.machanism.org/ghostwriter/act.html
---
<!--
@guidance:
Create the Act page as a Project Information page for the project.

- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Clearly describe how inherited values are processed within the file:
  - Explain the mechanism by which values can be inherited from parent sections, templates, or defaults.
  - Specify how and when these inherited values are applied or overridden in the context of the Act TOML configuration.
  - Provide examples if relevant, to illustrate the inheritance process. 
- Summarize the purpose of the Act feature, its key methods, and how it fits into the overall project.
- Provide easy-to-follow, step-by-step instructions or a practical example showing how to use the Act feature in real scenarios.
- Ensure the content is accessible and helpful for all users, including those new to the codebase or without a technical background.
- Analyze all act TOML files located in the `src/main/resources/acts` folder.
- For each act, create a section that includes:
  - The act’s name.
  - A clear, concise description of the act’s purpose and when it should be used.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->

# Acts

An **Act** is a reusable “prompt template” that Ghostwriter can run against files in your project.

Instead of typing a long set of instructions each time, you select an act by name (for example, `help` or `unit-tests`). Ghostwriter loads that act’s configuration from a TOML file and uses it to build the final request it sends to the AI.

Acts can be loaded from:

- **Built-in acts**: bundled with Ghostwriter (in the classpath under `/acts`, which in the source tree is `src/main/resources/acts`).
- **User-defined acts**: stored in a directory path or URL that you configure (via `gw.acts`, or programmatically using `ActProcessor#setActsLocation`).

## What an act file contains

Each act is a TOML file (usually named `<act-name>.toml`). Common properties:

- `description` (optional): a short human-friendly summary.
- `instructions`: the “system-like” rules and expectations for the AI.
- `inputs`: a text template that becomes the final prompt. Ghostwriter uses Java `String.format` to insert your request into this template.
- `basedOn` (optional): makes this act inherit from another act.
- `gw.*` (optional): Ghostwriter runtime options such as scan directory, recursion, exclusions, and thread count.
- Any other dotted keys: forwarded into Ghostwriter’s configuration (`Configurator`).

## How Ghostwriter loads and runs an act

This behavior is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

### 1) Parsing `--act`

The `--act` value can contain:

- the act name, and
- optional request text after the first whitespace.

Examples:

- `--act help` (no request text)
- `--act task Please refactor this code` (includes request text)

If no act name is provided, Ghostwriter defaults to `help`.

### 2) Loading the TOML and merging values

Ghostwriter tries to load the act from:

1. a user-defined location (if `gw.acts` is set), and
2. the built-in classpath resource `/acts/<name>.toml`.

Both can exist. If neither exists, Ghostwriter fails with an error.

After loading, Ghostwriter merges the act values into a map and then applies them as the current defaults (instructions, scan options, and configuration properties).

### 3) Building the final prompt

Once the act is loaded, Ghostwriter creates the prompt sent to the AI like this:

- Take your request text and insert it into the act’s `inputs` template:

```
finalPrompt = String.format(inputsTemplate, requestText)
```

- The AI is then executed with:
  - the act’s `instructions`, and
  - the computed `finalPrompt`.

If you do not supply request text after the act name, Ghostwriter uses the processor’s current default prompt.

## Inheritance and “inherited values” (how overrides work)

Ghostwriter supports inheritance/overrides in three practical ways.

### A) Act-to-act inheritance with `basedOn`

If an act contains `basedOn = "parentAct"`, Ghostwriter:

1. loads the parent act first (recursively),
2. then loads the child act,
3. then merges the properties.

This lets a child act re-use a parent act’s configuration and only specify what it wants to change.

### B) Template-style inheritance when acts are merged

When Ghostwriter copies TOML keys into the act’s properties map (`setActData`), it has special behavior for strings:

- If a key is new, it is stored normally.
- If the key already exists and both old/new values are strings, the new string is formatted into the existing string using `String.format`.

Conceptually:

```
properties[key] = String.format(existingValue, newValue)
```

This is a powerful pattern, but it is also strict: it only works when the “existing” value contains a `%s` placeholder.

Why this matters:

- If both a built-in act and a custom act exist with the same name, Ghostwriter can combine them.
- If a parent act provides a wrapper template (for example, `"...%s..."`) and the child provides the content, Ghostwriter can compose them.

### C) Inheriting from current configuration defaults

When Ghostwriter applies act data to the running processor (`applyActData`), it checks whether the runtime configuration already has a value for the same key.

If so, the act value is formatted using the existing configuration value:

```
effectiveValue = String.format(actValue, existingConfigValue)
```

This allows acts to build on top of defaults defined elsewhere (for example, in config files or system properties).

### Practical inheritance example

A simple “base wrapper” plus “child specialization”:

**base.toml**

```
instructions = "You are a helpful assistant.\n\n%s"
inputs = "# Task\n\n%s\n"
```

**child.toml**

```
basedOn = "base"

instructions = "Focus only on documentation tasks."
inputs = "Update documentation for: %s"
```

Result (conceptually):

- The parent act is loaded first.
- The child act can override values, or (if the parent is written as a wrapper with `%s`) the child can be injected into it.

## How to use acts

### Step-by-step

1. Choose an act.
   - Built-in acts are under `src/main/resources/acts`.

2. Run Ghostwriter using the act name.

```
--act <name> [your request text]
```

3. Ghostwriter loads the act, inserts your request into the act’s `inputs` template, and then applies the act to the matching project files.

### Example commands

Ask how acts work:

```
--act help What does basedOn do and how do overrides work?
```

Run a general task:

```
--act task Please review this module for readability issues.
```

## Built-in acts

These acts are bundled with Ghostwriter in `src/main/resources/acts`.

### `help`

Purpose: Provides user-friendly guidance about the Act feature.

When to use it: Use this act when you want to understand how acts work, how to run them, how inheritance/overrides behave, or to list/inspect act definitions.

### `task`

Purpose: A minimal, general-purpose template for running an arbitrary request in the current project context.

When to use it: Use this act when you want Ghostwriter to perform a one-off task without any extra workflow (for example: explain, refactor, document, review, or implement something you describe).

### `commit`

Purpose: Helps automate creating commit messages and performing commits by analyzing the working tree and running VCS commands.

When to use it: Use this act after you have made changes and want Ghostwriter to group them into logical commits, write messages, and execute the appropriate git/svn commands.

### `release-notes`

Purpose: Generates release notes from git history and writes them into `src/changes/changes.xml` (Maven changes format).

When to use it: Use this act when preparing a release and you want a structured summary of changes between a snapshot update and the release.

### `unit-tests`

Purpose: Improves unit test coverage by building the project and using JaCoCo reports to find coverage gaps, then creating/updating tests.

When to use it: Use this act when you need higher unit test coverage and want Ghostwriter to generate or enhance tests under `src/test/java`.

### `sonar-fix`

Purpose: Fixes issues reported by SonarQube by retrieving a report (via a configured SonarQube URL/API) and applying minimal code corrections, with strict rules for `@SuppressWarnings`.

When to use it: Use this act when you have SonarQube issues to address and want an automated workflow to remediate them safely.

### `grype-fix`

Purpose: Fixes dependency vulnerabilities found by Grype by updating affected dependencies and rebuilding to verify.

When to use it: Use this act when you have Grype scan output (or can run Syft/Grype locally) and want Ghostwriter to remediate dependency CVEs/GHSAs in your build files.
