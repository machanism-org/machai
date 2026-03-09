---
canonical: https://machai.machanism.org/ghostwriter/act.html
---

# Act

Ghostwriter “Acts” are reusable prompt templates stored as `.toml` files. An act can:

- Set the AI’s *instructions* (how the assistant should behave).
- Define an *inputs* template (how your prompt is wrapped/structured).
- Set Ghostwriter runtime options (for example scan scope, exclusions, threading).
- Pass any other values through as configuration properties.

Acts are meant to make common workflows repeatable (e.g., “generate unit tests”, “write release notes”, “fix Sonar issues”) without having to re-type long prompts or remember configuration knobs.

## Where acts come from

Ghostwriter can load acts from two places:

1. **Built-in acts** bundled with the application (classpath resources under `/acts`).
2. **Custom acts** from a user directory configured with `gw.acts`.

If an act exists in both places with the same name, **both are loaded** and the **custom act wraps/overrides** the built-in one.

## Act TOML format

Acts are TOML files named `<act-name>.toml`.

Common keys:

- `instructions` (string): what the AI should do.
- `inputs` (string): a template used to build the final prompt. It uses `String.format(...)` with **one** placeholder (`%s`) where your provided prompt text is inserted.
- `basedOn` (string): inherit from another act by name.
- `gw.threads` (string boolean): enable module multi-threading.
- `gw.excludes` (string): comma-separated scan exclusions.
- `gw.nonRecursive` (string boolean): disable module recursion.
- Any other key/value pairs: forwarded into Ghostwriter’s configurator.

## How inheritance and overrides work

Inheritance is handled by `ActProcessor.loadAct(...)`.

### 1) Parent inheritance via `basedOn`

If an act defines `basedOn`, Ghostwriter loads the parent act **first** (recursively). After the parent is loaded, the child is loaded and can override or extend values.

Example:

```toml
basedOn = "task"

instructions = "..."
```

This means: start with everything from `task`, then apply this act’s values.

### 2) Built-in + custom act wrapping

For a given name, Ghostwriter tries to load:

- Custom file: `<gw.acts>/<name>.toml` (if `gw.acts` is set)
- Built-in resource: `/acts/<name>.toml`

If both exist, Ghostwriter reads both and merges their keys into one effective act configuration. The practical result is that a custom act can adjust a built-in act without copying it entirely.

### 3) Key-level “inherit/merge” behavior (string formatting)

When Ghostwriter merges act values, **string values can be inherited** using `String.format(...)`.

There are two places where this happens:

1. **Act-to-act merge (parent/custom/built-in layering)**
   - When a key already exists and a new value is loaded for the same key, the previous value becomes the format string.
   - The new value is inserted into the previous value.

   Conceptual example:

   - Parent has:
     ```toml
     instructions = "Base instructions:\n%s"
     ```
   - Child has:
     ```toml
     instructions = "Child details."
     ```

   Effective value becomes:

   `"Base instructions:\nChild details."`

2. **Act vs. existing runtime configuration (`Configurator`)**
   - When applying act properties, if a configuration value already exists for the same key, Ghostwriter formats the act value using the existing configuration value.

   Conceptual example:

   - Your existing configuration has:
     ```properties
     instructions=Always be concise.
     ```
   - Act has:
     ```toml
     instructions = "Project rules first. Then: %s"
     ```

   Effective instructions become:

   `"Project rules first. Then: Always be concise."`

### 4) How `inputs` and the final prompt are built

When you run an act, you typically provide:

- The **act name**
- Optionally, a **prompt** after the name

Ghostwriter then:

1. Takes the act’s `inputs` (or the configured `prompt` if set) as the prompt template.
2. Inserts your provided prompt text into that template using `String.format(template, promptText)`.
3. Applies act properties:
   - `instructions` becomes the active instructions.
   - `inputs` can further wrap the current default prompt (again via formatting).

Because `inputs` uses formatting, it should generally include exactly one `%s` placeholder.

## How acts fit into Ghostwriter

Acts are an execution mode implemented by `ActProcessor`.

At a high level, `ActProcessor`:

- Loads the act definition (with inheritance and overrides).
- Builds the final prompt by formatting the act’s `inputs` with your prompt.
- Applies act configuration to Ghostwriter (instructions, scan behavior, and other properties).
- Runs the resulting instructions + prompt against the matched project files.

Key methods:

- `setDefaultPrompt(String act)`: parses `<name> [prompt]`, loads the act, and constructs the final prompt.
- `loadAct(String name, Properties props, File actDir)`: loads TOML from custom dir and/or classpath, and resolves `basedOn` recursively.
- `setActData(Properties props, TomlParseResult toml)`: merges dotted TOML string entries into a `Properties` map with inheritance via `String.format`.
- `applyActData(Properties props)`: applies act keys to Ghostwriter runtime (special handling for `instructions`, `inputs`, and `gw.*` options).

## Using acts (practical steps)

1. **Find an act name**
   - Use a built-in act listed below, or create your own `<name>.toml`.

2. **(Optional) Create/override acts in a custom directory**
   - Create a folder for your acts.
   - Set `gw.acts` to that folder.
   - Add `<name>.toml` to override/extend a built-in act.

3. **Run Ghostwriter with an act**
   - Provide the act name and (optionally) a prompt.

Example (conceptual):

- Act file `unit-tests.toml` defines how to generate tests.
- You run Ghostwriter with:

  `--act unit-tests "Generate tests for ActProcessor"`

Ghostwriter will load the act, wrap your prompt with the act’s `inputs`, apply the act’s `instructions`, and then process the project files that match the scan rules.

## Built-in acts

Below are the acts shipped in `src/main/resources/acts`.

### `commit`

Helps you create well-structured commit messages and provides the corresponding git commands to stage and commit grouped changes. Use it when you want to turn a set of local modifications into clean, logical commits.

### `grype-fix`

Guides you through fixing vulnerabilities reported by Grype by updating dependencies, rebuilding the project, and documenting each change (including CVE/GHSA references). Use it when dependency scanning reports known vulnerabilities.

### `help`

An explainer/troubleshooting act focused specifically on Ghostwriter acts: what they are, how act TOML files are structured, how inheritance works, and how to list or inspect acts. Use it when you want information about acts or need help creating/debugging them.

### `release-notes`

Generates markdown release notes from git commit history for a target release version and saves them under the documentation tree. Use it when preparing a release and you need a human-readable summary of changes.

### `sonar-fix`

Helps fix issues reported by SonarQube by applying minimal, standards-compliant code changes, rebuilding after each fix, and producing an issue-by-issue report. Use it when a SonarQube scan flags code quality or security problems.

### `task`

A general-purpose “project assistant” act that focuses on understanding the project context and completing a user-specified task safely and clearly. Use it as the default act when you want Ghostwriter to perform a specific development task in this repository.

### `unit-tests`

Guides the generation of high-quality unit tests (framework-appropriate, isolated, repeatable, covering typical and edge cases) without modifying production code unless explicitly requested. Use it when you need new or improved test coverage.

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
