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

Instead of writing a long set of instructions every time, you select an act by name (for example, `help` or `unit-tests`). Ghostwriter loads that act’s configuration from a TOML file and uses it to build the final AI request.

Acts can be:

- **Built-in**: bundled with Ghostwriter under `src/main/resources/acts`.
- **User-defined**: stored in a directory or URL you provide via `gw.acts` (or by calling `ActProcessor#setActsLocation`).

## What an act contains

Acts are TOML files (usually named `<act-name>.toml`) with a small set of common keys:

- `description` (optional): a human-readable summary.
- `instructions`: the “system-style” instructions sent to the AI.
- `inputs`: a prompt template. Ghostwriter inserts your request text into this template using Java `String.format`.
- `basedOn` (optional): indicates the act should inherit settings from another act.
- `gw.*` options (optional): Ghostwriter settings such as scan directory, recursion, thread count, and exclusions.
- Any other keys: forwarded into the runtime configuration (the underlying `Configurator`).

## How Ghostwriter loads and runs an act

This behavior is implemented in `org.machanism.machai.gw.processor.ActProcessor`.

### Loading an act

When you run Ghostwriter in act mode, the processor:

1. Parses your `--act` value, which may contain:
   - an act name, and
   - optional request text after the first whitespace.
2. Loads the act definition from:
   - an optional user-defined location (`gw.acts`), and
   - the built-in classpath (`/acts/<name>.toml`).
3. Merges the values into a single map of properties.
4. Applies those properties to the processor (instructions, scan behavior, etc.).

If the act cannot be found in either place, Ghostwriter fails with an error.

### Turning your request into the final prompt

After loading, Ghostwriter builds the final prompt like this:

- The request text is inserted into the act’s `inputs` template:
  - `finalPrompt = String.format(inputsTemplate, requestText)`
- That `finalPrompt`, together with the act’s `instructions`, is then used to process matching project files.

If you do not provide request text after the act name, Ghostwriter uses the processor’s current default prompt.

## Inheritance and “inherited values” (how overrides work)

Acts support **inheritance** through two mechanisms that work together:

### 1) Act-to-act inheritance: `basedOn`

If an act contains a `basedOn = "parentAct"` property, Ghostwriter loads the parent act first (recursively), then loads the child.

- Parent values are placed into the properties map first.
- Child values are merged afterward.

This lets a child act extend or override a base act.

### 2) Value composition: formatting strings into other strings

Ghostwriter also supports “inheritance” by **formatting one string into another** using `String.format`. This happens in two places:

#### a) When multiple acts are merged (including custom-over-built-in)

While copying TOML values into the properties map (`setActData`):

- If a key already exists in the properties map and both values are strings, the new value is formatted into the previous one.
- Conceptually:
  - `properties[key] = String.format(existingValue, newValue)`

This is the key feature that enables:

- **Custom act wrapping a built-in act**: if both exist, both are loaded; whichever was loaded first provides the “outer” template, and the later one can be injected into it.
- **Parent/child `basedOn` composition**: parent strings can act as templates that include the child strings.

Important detail: this style of inheritance is template-based. It is most useful when the parent value contains a `%s` placeholder for the child.

#### b) When applying the act to the current configuration

When applying act data (`applyActData`), Ghostwriter checks whether the current runtime configuration already has a value for the same key.

- If it does, the act value is formatted using the existing configuration value.
- Conceptually:
  - `effectiveValue = String.format(actValue, existingConfigValue)`

This lets an act’s strings build on top of previously configured defaults.

### Practical inheritance example

If you want a base act that provides a standard wrapper and a child act that fills it in:

**base.toml**

```
instructions = "You are a helpful assistant.\n\n%s"
inputs = "# Task\n\n%s\n"
```

**child.toml**

```
basedOn = "base"

instructions = "Focus only on documentation tasks."
inputs = "Update the project documentation for: %s"
```

When loaded:

- `basedOn` causes `base` to load first.
- The child strings can be formatted into the base strings (if the base contains `%s`).
- The result is a composed `instructions` and `inputs` that keep the base structure while adding the child specialization.

## How to use acts

### Step-by-step

1. Pick an act name.
   - Built-in acts are located in `src/main/resources/acts`.
2. Run Ghostwriter with `--act`.
   - Syntax:

```
--act <name> [your request text]
```

3. Ghostwriter loads the act, builds the final prompt by inserting your request text into the act’s `inputs`, and then processes matching files.

### Example commands

Ask for help about acts:

```
--act help What is basedOn and how do overrides work?
```

Run a generic task against the project:

```
--act task Please review this module for readability issues.
```

## Built-in acts

The following acts are bundled with Ghostwriter in `src/main/resources/acts`.

### `help`

Purpose: Provides user-friendly guidance about the Act feature itself.

When to use it: Use this act when you want to list available acts, inspect an act’s properties, or understand inheritance, overrides, and how to build an act command.

### `task`

Purpose: A minimal, general-purpose template for running an arbitrary request in the current project context.

When to use it: Use this when you want Ghostwriter to do “whatever I asked” without any special workflow (no extra steps like building, scanning reports, etc.).

### `commit`

Purpose: Helps automate creating commit messages and performing commits by analyzing the working tree.

When to use it: Use this after you have made changes and want Ghostwriter to group them into logical commits, write messages, and run the appropriate VCS commands.

### `release-notes`

Purpose: Generates release notes from git commit history and writes them into `src/changes/changes.xml` (Maven changes format).

When to use it: Use this when preparing a release and you want a structured summary of changes between snapshot and release versions.

### `unit-tests`

Purpose: Improves test coverage by generating or enhancing unit tests, using Maven and JaCoCo to find coverage gaps.

When to use it: Use this when you need higher unit test coverage (the act targets 90%+) and want Ghostwriter to create or update tests under `src/test/java`.

### `sonar-fix`

Purpose: Fixes issues reported by SonarQube by retrieving a report (via a configured SonarQube URL/API) and applying minimal code corrections.

When to use it: Use this when you have SonarQube issues to address and want an automated workflow to fix them, with strict rules around `@SuppressWarnings`.

### `grype-fix`

Purpose: Fixes dependency vulnerabilities found by Grype by updating affected dependencies and rebuilding to verify.

When to use it: Use this when you have Grype scan output (or can run Syft/Grype locally) and want Ghostwriter to remediate dependency CVEs/GHSAs in build files.
