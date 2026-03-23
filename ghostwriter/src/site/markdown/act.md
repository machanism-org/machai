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

An **Act** is a reusable set of instructions (a “prompt template”) that Ghostwriter can apply to files in your project.

Instead of re-typing the same guidance every time, you run Ghostwriter with an act name (for example `help` or `unit-tests`). Ghostwriter loads that act’s configuration from a TOML file, combines it with your request text, and then runs the AI using the act’s rules.

Acts are stored as TOML files and can come from:

- **Built-in acts**: shipped with Ghostwriter at `src/main/resources/acts` (available on the classpath under `/acts`).
- **User-defined acts**: stored in a folder or URL you configure (via `gw.acts` or programmatically with `ActProcessor#setActsLocation`).

## What an act TOML file contains

Each act is a TOML file (usually named `<act-name>.toml`). Common keys you will see:

- `description` (optional): a short summary shown to users.
- `instructions`: the “rules” for the AI (how to behave, what to focus on, what to avoid).
- `inputs`: a text template for the user request. Ghostwriter inserts your request into this template using `String.format`.
- `basedOn` (optional): inherit settings from another act.
- `gw.*` (optional): Ghostwriter runtime settings such as which directory to scan, recursion, exclusions, and thread count.
- Any other keys: forwarded into Ghostwriter’s configuration (`Configurator`).

## How Ghostwriter loads and runs an act

This is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

### 1) Reading the `--act` argument

The `--act` value supports this shape:

```
--act <name> [your request text]
```

Examples:

- `--act help`
- `--act task Please refactor this code`

If the act name is missing or blank, Ghostwriter defaults to `help`.

### 2) Loading the TOML

Ghostwriter tries to load the act definition from:

1. a **user-defined location** (if `gw.acts` is set), and
2. the **built-in classpath** resource `/acts/<name>.toml`.

Either one can exist, and both can exist at the same time.

### 3) Building the final prompt

After an act is loaded, Ghostwriter turns your request text into the final prompt like this:

```
finalPrompt = String.format(inputsTemplate, requestText)
```

Then Ghostwriter runs the AI using:

- the act’s `instructions`, and
- the computed `finalPrompt`.

If you do not provide request text after the act name, Ghostwriter uses the processor’s current default prompt.

## Inheritance and “inherited values” (how overrides work)

Acts can “inherit” values in three ways. This matters most when you use `basedOn`, or when you provide custom act files that extend built-in ones.

### A) Act-to-act inheritance (`basedOn`)

If an act contains `basedOn = "parentAct"`, Ghostwriter:

1. loads the parent act first (recursively),
2. then loads the child act,
3. and merges the properties.

If both built-in and custom versions exist, Ghostwriter checks `basedOn` in the custom act first; if it is not present there, it checks the built-in act.

### B) Template-style inheritance when TOML values are merged

When Ghostwriter copies keys from TOML into its internal map (`ActProcessor#setActData`), it has special behavior for **string** values:

- If a key is new, it is stored as-is.
- If the key already exists and both old and new values are strings, the **existing value becomes a wrapper**, and the new value is inserted into it using `String.format`.

Conceptually:

```
properties[key] = String.format(existingValue, newValue)
```

This only works if the existing value contains a placeholder like `%s`. If it does not, formatting will fail.

This mechanism is what allows patterns like:

- **Built-in act provides a wrapper**, custom act injects more details.
- **Parent act provides a wrapper**, child act injects specifics.

### C) Inheriting from current configuration defaults

When Ghostwriter applies act data to the running processor (`ActProcessor#applyActData`), it also checks the current configuration (`Configurator`). If a configuration value already exists for the same key, Ghostwriter formats the act’s value using the existing configuration value.

Conceptually:

```
effectiveValue = String.format(actValue, existingConfigValue)
```

This lets an act build on top of defaults defined elsewhere (for example, in a config file or system properties), as long as the act value has a `%s` placeholder.

### Practical inheritance example

A “base wrapper” act and a child act that injects content:

**base.toml**

```
instructions = "You are a helpful assistant.\n\n%s"
inputs = "# Task\n\n%s\n"
```

**doc.toml**

```
basedOn = "base"

instructions = "Focus only on documentation tasks."
inputs = "Update documentation for: %s"
```

Result (conceptually):

- `doc` starts with everything from `base`.
- If `base` is written as a wrapper (contains `%s`), `doc` can be inserted into it.
- Any key the child provides can also fully override the parent, depending on how the parent is written.

## How to use acts (step-by-step)

1. **Pick an act**
   - See built-in acts in `src/main/resources/acts`.

2. **Run Ghostwriter with the act name**

   ```
   --act <name> [your request text]
   ```

3. **Ghostwriter applies the act**
   - Loads the act TOML.
   - Inserts your request into `inputs`.
   - Uses `instructions` + the generated prompt to process matching files.

### Example commands

Ask for act help:

```
--act help What does basedOn do and how do overrides work?
```

Run a general request against the project:

```
--act task Please review this module for readability issues.
```

## Built-in acts

Built-in acts are bundled with Ghostwriter in `src/main/resources/acts`.

### `help`

**Purpose:** Explains Ghostwriter’s Act feature and helps you discover and inspect acts.

**When to use it:** Use this act when you want to list available acts, view an act’s definition, understand how acts work, or troubleshoot act inheritance/overrides.

### `task`

**Purpose:** A simple, general-purpose act for running an arbitrary request in the current project context.

**When to use it:** Use this act for one-off tasks (explain, refactor, document, review, implement) where you want minimal workflow and you will provide the task details yourself.

### `commit`

**Purpose:** Helps you create commits by checking VCS status, grouping changes, generating commit messages, and running the required git/svn commands.

**When to use it:** Use this act after making changes and you want Ghostwriter to organize them into logical commits and commit them.

### `release-notes`

**Purpose:** Generates release notes from git history and appends them to `src/changes/changes.xml` (Maven changes format).

**When to use it:** Use this act when preparing a release and you want a structured summary of changes between a snapshot update and the release.

### `unit-tests`

**Purpose:** Improves unit test coverage by building the project, generating a JaCoCo report, and creating/updating unit tests.

**When to use it:** Use this act when you want to increase test coverage and you want Ghostwriter to generate or enhance tests under `src/test/java`.

### `sonar-fix`

**Purpose:** Fixes issues reported by SonarQube by retrieving a report (via a configured SonarQube URL/API) and applying minimal code corrections. Includes strict rules for when and how `@SuppressWarnings` may be used.

**When to use it:** Use this act when you have SonarQube issues to address and want an automated workflow to remediate them safely.

### `grype-fix`

**Purpose:** Fixes dependency vulnerabilities found by Grype by generating an SBOM (Syft), running Grype, updating dependencies, rebuilding, and documenting changes.

**When to use it:** Use this act when you need to remediate dependency vulnerabilities (CVEs/GHSAs) in your build files and you can run Syft/Grype in your environment.
