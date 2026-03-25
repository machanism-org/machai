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
  - The act's name.
  - A clear, concise description of the act's purpose and when it should be used.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->

# Acts

An **Act** is a reusable set of instructions ("prompt template") that Ghostwriter can apply to files in your project.

Instead of repeating the same guidance each time, you run Ghostwriter with an act name (for example `help` or `unit-tests`). Ghostwriter loads that act's configuration from a TOML file, combines it with your request text, and then runs the AI using the act's rules.

Acts are stored as TOML files and can come from:

- **Built-in acts**: shipped with Ghostwriter at `src/main/resources/acts` (available on the classpath under `/acts`).
- **User-defined acts**: stored in a folder or URL you configure (via `gw.acts` or programmatically with `ActProcessor#setActsLocation`).

## What an act TOML file contains

Each act is a TOML file (usually named `<act-name>.toml`). Common keys you will see:

- `description` (optional): a short summary shown to users.
- `instructions`: the "rules" for the AI (how to behave, what to focus on, what to avoid).
- `inputs`: the request template. Ghostwriter inserts your request into this template.
- `basedOn` (optional): inherit/extend settings from another act.
- `gw.*` (optional): Ghostwriter runtime settings such as scan directory, recursion, exclusions, and thread count.
- Any other keys: forwarded into Ghostwriter's configuration (`Configurator`).

## How Ghostwriter loads and runs an act

This is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

### 1) Choosing an act (`--act`)

The `--act` value supports:

```
--act &lt;name&gt; [your request text]
```

Examples:

- `--act help`
- `--act task Please refactor this code`

If the act name is missing or blank, Ghostwriter defaults to `help`.

### 2) Loading the act TOML

Ghostwriter tries to load act definitions from two places:

1. a **user-defined location** (if `gw.acts` is set), and
2. the **built-in classpath** resource `/acts/&lt;name&gt;.toml`.

Either one can exist, and both can exist at the same time.

If neither is found, Ghostwriter errors with:

- `Act: `&lt;name&gt;` not found.`

### 3) Turning your request into the prompt

After the act is loaded, Ghostwriter creates the final prompt by inserting your request text into the act's `inputs` template.

In code, the processor does this by replacing the `%s` placeholder:

- `inputs = String.replace(inputs, "%s", requestText)`

If you do not provide request text after the act name, Ghostwriter uses the processor's existing default prompt.

### 4) Applying act settings

After loading, the act's data is applied:

- `instructions` becomes the AI "system" instructions.
- `inputs` becomes the default prompt used for scanning.
- `gw.threads`, `gw.excludes`, `gw.nonRecursive` control Ghostwriter runtime behavior.
- other keys are forwarded into the `Configurator`.

## Inheritance and "inherited values" (how overrides work)

Ghostwriter supports inheritance in three related ways. These are important when you want to create custom acts that build on existing ones.

### A) Act-to-act inheritance with `basedOn`

If an act contains `basedOn = "parentAct"`, Ghostwriter:

1. loads the named act,
2. checks whether it has a `basedOn` parent (recursively),
3. loads all parents first,
4. then applies the child act.

This is a true "inherit then override" workflow.

If both built-in and custom versions exist, Ghostwriter decides the `basedOn` value like this:

- if the **custom** act has `basedOn`, it is used;
- otherwise, if the **built-in** act has `basedOn`, it is used.

### B) String "wrapper template" inheritance when TOML values are merged

When TOML is merged into the in-memory act map (`ActProcessor#setActData`), Ghostwriter has special behavior for **string values**:

- If a key does not exist yet, it is stored as-is.
- If a key already exists and both values are strings, the existing value is treated like a *wrapper template*, and the new value is inserted into it.

In code, this is implemented as:

- `merged = Strings.replace(existing, "%s", newValue)`

This lets you build layered instructions, for example:

- a "base" act provides: `instructions = "General rules...\n\n%s"`
- a "child" act provides: `instructions = "Extra rules for docs"`
- result: the child rules are injected into the `%s` placeholder.

Important rule: for this to work, the *parent/existing* string must contain a `%s` placeholder. If it does not, the child value cannot be injected (and the parent effectively behaves like a hard override).

### C) Inheriting from existing configuration defaults (`Configurator`)

When applying act data (`ActProcessor#applyActData`), Ghostwriter also checks the current configuration for each string key:

- if configuration already has a value for that key, Ghostwriter replaces `%s` in the act value with the existing configuration value.

In code:

- `effective = Strings.replace(actValue, "%s", existingConfigValue)`

This allows acts to "build on top of" values set elsewhere (system properties, config files, previous settings), as long as the act value contains `%s`.

### Practical inheritance example

A base act and a derived act:

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

Conceptually:

- `doc` starts with everything from `base`.
- because `base.instructions` contains `%s`, `doc.instructions` is injected into it.
- because `base.inputs` contains `%s`, `doc.inputs` is injected into it.

## How to use acts (step-by-step)

1. **Pick an act**
   - For built-in acts, see `src/main/resources/acts`.

2. **Run Ghostwriter with the act name**

   ```
   --act &lt;name&gt; [your request text]
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

**Purpose:** Explains Ghostwriter's Act feature and helps you discover and inspect acts.

**When to use it:** Use this act when you want to list available acts, view an act's definition, understand how acts work (including inheritance), or troubleshoot act configuration.

### `task`

**Purpose:** A simple, general-purpose act for running an arbitrary request in the current project context.

**When to use it:** Use this act for one-off tasks (explain, refactor, document, review, implement) where you want minimal workflow and you will provide the task details yourself.

### `commit`

**Purpose:** Automates committing changes by checking VCS status, grouping changes, generating commit messages, and running the required VCS commands.

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
