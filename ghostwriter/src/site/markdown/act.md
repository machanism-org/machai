---
canonical: https://machai.machanism.org/ghostwriter/act.html
---

# Act

Ghostwriter “Acts” are reusable prompt templates stored as `.toml` files. An act lets you pick a ready-made workflow (for example: generating unit tests, fixing Sonar issues, or writing release notes) without having to re-type a long prompt or remember configuration options.

An act can:

- Set the AI’s instructions (how the assistant should behave).
- Wrap your request in an input template (how your text is structured before being sent to the AI).
- Adjust Ghostwriter runtime options (scan scope, exclusions, threading, recursion).
- Provide additional configuration properties that Ghostwriter passes through to its configurator.

## Where acts come from

Ghostwriter can load acts from two places:

1. **Built-in acts** shipped with the application (classpath resources under `/acts`).
2. **Custom acts** in a user directory configured via `gw.acts`.

If the same act name exists in both places, Ghostwriter loads both and combines them so the **custom act can override or extend** the built-in act.

## Act TOML format (what’s inside a `.toml` file)

An act file is named:

- `<act-name>.toml`

Common keys you will see:

- `instructions` (string): the “system” instructions for the AI.
- `inputs` (string): a prompt template. Ghostwriter uses `String.format(...)` to insert your provided prompt text into this template. In practice, your `inputs` string should usually contain **exactly one** `%s` placeholder.
- `basedOn` (string): inherit from another act by name.
- `prologue` (array of strings): acts to run before the main act.
- `epilogue` (array of strings): acts to run after the main act.
- `gw.threads` (string number): sets concurrency (mapped to `setDegreeOfConcurrency(...)`).
- `gw.excludes` (string): comma-separated scan exclusions.
- `gw.nonRecursive` (string boolean): disable module recursion.
- `gw.scanDir` (string): scan directory or glob pattern (many acts set this).
- Any other key/value pairs: forwarded into Ghostwriter’s `Configurator`.

## How acts are loaded and combined (inheritance and overrides)

All act behavior is implemented by `ActProcessor` (`src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`). The most important thing to understand is that acts can be combined in multiple layers.

### Layer 1: Parent inheritance via `basedOn`

If an act defines `basedOn`, Ghostwriter loads the parent act **first**, then applies the child act.

Example:

```toml
basedOn = "task"

instructions = "..."
```

Meaning: start with everything from `task`, then apply/override with this act.

### Layer 2: Built-in + custom act “wrapping”

For a given act name, Ghostwriter tries to load:

- Custom file: `<gw.acts>/<name>.toml` (if `gw.acts` is configured)
- Built-in resource: `/acts/<name>.toml`

If both exist, both are read and merged into one effective act.

Important detail: the current implementation reads **custom first**, then built-in. When both define the same key, how they combine depends on the key type and the “inherit formatting” behavior described next.

### Layer 3: Key-level inheritance/merge for strings (formatting)

When merging TOML into the act properties map, `ActProcessor.setActData(...)` applies special behavior for string values:

- If a key does **not** exist yet: the new string value is stored.
- If a key **already exists** and both are strings:
  - the **old** value is treated as a `String.format(...)` template
  - the **new** value is inserted into the old value

This means that to “inherit” and extend an existing string value, the earlier value should contain a `%s` placeholder.

Conceptual example (parent then child):

- Parent:
  ```toml
  instructions = "Base rules:\n%s"
  ```
- Child:
  ```toml
  instructions = "Child-specific rules."
  ```

Effective `instructions` becomes:

`Base rules:
Child-specific rules.`

Notes:

- This behavior applies to any string key (not just `instructions` and `inputs`).
- If the “inherited” value does not include `%s`, then `String.format(...)` will not work correctly. For inherited strings, always include `%s` in the base value.

### Layer 4: Applying act values onto existing runtime configuration

After an act is loaded, `ActProcessor.applyActData(...)` applies values to the running Ghostwriter configuration.

For string values, there is another formatting step:

- If the runtime configurator already has a value for the same key, Ghostwriter runs:
  - `value = String.format(actValue, existingConfigValue)`

Conceptual example:

- Existing configuration:
  ```properties
  instructions=Always be concise.
  ```
- Act TOML:
  ```toml
  instructions = "Project rules first. Then: %s"
  ```

Effective instructions become:

`Project rules first. Then: Always be concise.`

### How `inputs` becomes the final prompt

Acts can wrap your text in two steps:

1. **Your command line prompt**
   - You invoke an act as: `--act <name> [prompt]`.
   - If you don’t provide a prompt, Ghostwriter uses the current default prompt.

2. **The act’s `inputs` template**
   - When applying the act, `applyActData(...)` formats `inputs` using the current default prompt:
     - `finalPrompt = String.format(inputsTemplate, defaultPrompt)`

Because of this, `inputs` should normally contain a single `%s` where your request should be inserted.

## How acts fit into Ghostwriter (key methods)

Acts are an execution mode implemented by `ActProcessor`. At a high level it:

- Loads the act definition (including built-in/custom layering and optional `basedOn` inheritance).
- Builds the final prompt from your text and the act’s `inputs`.
- Applies act properties (instructions, scan behavior, and other configuration) to the Ghostwriter runtime.
- Runs the resulting instructions + prompt against the scanned project files.

Key methods:

- `setDefaultPrompt(String act)`: parses `<name> [prompt]`, loads the act, and constructs the final prompt.
- `loadAct(String name, Map<String,Object> props, File actDir)`: loads TOML (custom and/or classpath), resolves `basedOn` recursively.
- `setActData(Map<String,Object> props, TomlParseResult toml)`: merges dotted TOML keys into a map, with string inheritance via `String.format`.
- `applyActData(Map<String,Object> props)`: applies act keys to runtime (special handling for `instructions`, `inputs`, and `gw.*` options).

## Using acts (step-by-step)

### Option A: Use a built-in act

1. Pick an act name (see “Built-in acts” below).
2. Run Ghostwriter with that act name, plus your request.

Example (conceptual):

- You run:

  `--act unit-tests Generate tests for ActProcessor`

- Ghostwriter will:
  - load the `unit-tests` act
  - format the `inputs` template by inserting your text
  - apply `instructions` from the act
  - scan the project (based on `gw.*` options)
  - generate/update unit tests under `src/test/java`

### Option B: Create or override an act

1. Create a folder for custom acts.
2. Configure `gw.acts` to point to that folder.
3. Add a file named `<name>.toml`.
4. (Optional) Use `basedOn = "<parent>"` to start from another act.

Tip: If you want to extend a parent string value (like `instructions`), make sure the parent value contains a `%s` placeholder.

## Built-in acts

These acts are shipped in `src/main/resources/acts`.

### `commit`

Helps you turn local working-tree changes into clean, logically grouped commits.

Use it when you want the assistant to:

- Check version control status in the current folder.
- Group modified files by purpose (feature/fix/docs/refactor/chore).
- Generate commit messages that match the project’s historical style.
- Provide and (per the act’s instructions) execute the corresponding git commands.

### `grype-fix`

Helps you fix vulnerabilities reported by Grype (dependency security scanning).

Use it when you have Grype results (or can generate them) and you want the assistant to:

- Identify vulnerable dependencies.
- Update Maven dependencies to fixed versions.
- Build the project to verify.
- Document each change with CVE/GHSA references.

### `help`

Explains and troubleshoots Ghostwriter acts.

Use it when you want to:

- List available acts (built-in and custom).
- Inspect a specific act (purpose, key properties, inheritance).
- Understand act inheritance/overrides or fix act configuration issues.

### `release-notes`

Generates release notes based on git commit history and stores them in the project change log.

Use it when preparing a release and you want the assistant to:

- Collect commits for a target release version.
- Group them into readable sections.
- Write the result into `src/changes/changes.xml` as a new `<release>` entry.

### `sonar-fix`

Helps you fix issues from a SonarQube scan.

Use it when you want the assistant to:

- Fetch a SonarQube report (via the SonarQube Web API).
- Apply minimal code changes to address each issue.
- Rebuild after fixes.
- Produce a per-issue explanation/report.

### `task`

A general-purpose “project assistant” act.

Use it for most day-to-day work when you simply want Ghostwriter to complete a task in this repository with awareness of the project structure and conventions.

### `unit-tests`

Guides the creation of high-quality unit tests.

Use it when you want the assistant to:

- Understand the target code.
- Create isolated, repeatable tests with good coverage.
- Place tests under the correct test source directories.
- Avoid modifying production code unless explicitly requested.

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
