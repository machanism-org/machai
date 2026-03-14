---
canonical: https://machai.machanism.org/ghostwriter/act.html
---

# Act

Ghostwriter **Acts** are reusable “recipes” that tell Ghostwriter *how to run* and *how to structure what it sends to the AI*. An act is stored as a **TOML** file (`.toml`) and can:

- Provide the AI’s **instructions** (how it should behave).
- Provide an **input template** (how your request text is wrapped before sending).
- Adjust Ghostwriter options such as scan scope, exclusions, and concurrency.
- Pass extra configuration values through to Ghostwriter’s underlying configuration system.

Acts are intended to make common workflows repeatable (for example: writing unit tests, generating release notes, or fixing dependency vulnerabilities) without needing to retype long prompts or remember options.

## Where acts come from

Ghostwriter loads acts from two sources:

1. **Built-in acts** shipped with Ghostwriter (classpath resources under `src/main/resources/acts`).
2. **Custom acts** stored in a user directory configured by the property `gw.acts`.

If an act exists in both places with the same name, Ghostwriter reads both and **merges** them into a single effective act.

## Act file name and common keys

An act file is named:

- `<act-name>.toml`

Common keys inside an act file:

- `description` (string): a human-friendly summary of what the act does.
- `instructions` (string): the AI “system” instructions for this act.
- `inputs` (string): a prompt template used to wrap your request text.
- `basedOn` (string): inherit from another act by name.
- `prologue` (array): list of other acts to run before the main act.
- `epilogue` (array): list of other acts to run after the main act.
- `gw.*` (table or dotted keys): Ghostwriter runtime options such as scan scope and recursion.
- Any other keys: forwarded into Ghostwriter’s configuration.

### About the `inputs` template

Ghostwriter uses Java `String.format(...)` to insert your request into `inputs`. In practice, this means your `inputs` string should usually include **exactly one** `%s`, which represents “the text I typed after `--act <name>`”.

Example:

```toml
inputs = '''
# Task

Please do the following:

%s
'''
```

## How inheritance and overrides work

Acts can be combined in layers. The effective act is the result of several merge steps implemented in `ActProcessor` (`src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`).

### Layer 1: Inherit from a parent act using `basedOn`

If an act contains `basedOn`, Ghostwriter loads the parent act **first**, then merges the child act on top.

Example:

```toml
basedOn = "task"

instructions = "My additional rules."
```

Meaning: start with `task`, then apply this act’s settings.

### Layer 2: Built-in act + custom act with the same name

When an act name exists in both locations:

- Custom: `<gw.acts>/<name>.toml`
- Built-in: `/acts/<name>.toml`

Ghostwriter reads **both** and merges them.

Important detail: the current implementation reads **custom first** and then **built-in** (see `loadAct(...)`). This order matters because of how string values can “wrap” each other (next section).

### Layer 3: How string values are inherited (string “wrapping”)

When Ghostwriter merges act TOML into the in-memory act map (`setActData(...)`):

- If a key does **not** exist yet, the value is stored.
- If the key already exists and **both values are strings**, Ghostwriter treats the *existing* string as a `String.format(...)` template and inserts the *new* string into it:

```java
value = String.format((String) inheritValue, value);
```

This is how “inherited” strings are extended.

Practical inheritance example (parent then child):

- Parent act:
  ```toml
  instructions = "Base rules:\n%s"
  ```
- Child act:
  ```toml
  instructions = "Child-specific rules."
  ```

Effective result:

```
Base rules:
Child-specific rules.
```

Important notes:

- This formatting rule applies to *any* string key (not only `instructions` and `inputs`).
- To make a string safely extendable, the “base” value should include a `%s` placeholder.

### Layer 4: Applying act values to the current runtime configuration

After an act is loaded, Ghostwriter applies it onto the current runtime settings (`applyActData(...)`). During this step, string values can again be formatted:

- If the runtime configuration already has a value for the same key, Ghostwriter runs:

```java
value = String.format(actValue, existingConfigValue);
```

This lets an act wrap an existing configuration value.

Example:

- Existing configuration:
  ```properties
  instructions=Always be concise.
  ```
- Act:
  ```toml
  instructions = "Project rules first. Then: %s"
  ```

Effective instructions become:

```
Project rules first. Then: Always be concise.
```

## How an act turns your request into the final prompt

When you run an act, you typically invoke it like:

```
--act <name> [your request text]
```

Ghostwriter builds the final prompt in two steps:

1. **Choose the request text**
   - If you provide text after the act name, Ghostwriter uses it.
   - If you provide nothing, Ghostwriter uses the processor’s current default prompt.

2. **Wrap it with `inputs`**
   - Ghostwriter formats the act’s `inputs` using your request text:
     - `finalPrompt = String.format(inputsTemplate, requestText)`

That `finalPrompt`, together with the act’s `instructions`, is what Ghostwriter sends to the AI when processing scanned files.

## How acts fit into Ghostwriter (purpose and key methods)

Acts are an execution mode implemented by `ActProcessor`. Its responsibilities are:

- Load the act definition from built-in and/or custom sources.
- Resolve inheritance via `basedOn`.
- Merge act properties using the string “wrapping” behavior.
- Apply act properties to Ghostwriter runtime settings (instructions, scan options, and extra config).
- Process matching files using the final instructions + prompt.

Key methods (in `ActProcessor`):

- `setDefaultPrompt(String act)`: parses `<name> [prompt]`, loads the act, sets the user prompt, then applies the act.
- `loadAct(String name, Map<String,Object> props, String actsLocation)`: loads custom + built-in TOML and resolves `basedOn`.
- `tryLoadActFromDirectory(...)` / `tryLoadActFromClasspath(...)`: load TOML from the configured custom location or from `/acts`.
- `setActData(...)`: merges dotted TOML entries into a map (including string inheritance).
- `applyActData(...)`: applies act keys to runtime configuration and options.

## Using acts (step-by-step)

### 1) Use a built-in act

1. Pick an act from the list below.
2. Run Ghostwriter with `--act <name>`.
3. Optionally add your request text after the act name.

Example:

```
--act unit-tests Create unit tests for ActProcessor string inheritance behavior.
```

### 2) Create or override an act

1. Create a folder for your custom acts.
2. Set `gw.acts` to that folder (path or URL).
3. Create `<name>.toml` inside it.
4. (Optional) Use `basedOn = "<parent>"` to inherit from an existing act.

Tip: if you want to extend a parent string value (especially `instructions` or `inputs`), make sure the parent value includes a `%s` placeholder.

## Built-in acts

Built-in acts live in `src/main/resources/acts`.

### `commit`

Helps you turn your local working-tree changes into clean, logically grouped commits.

Use it when you want Ghostwriter to check version control status, group changed files by purpose (feature/fix/docs/refactor/chore), generate good commit messages that match the project’s historical style, and run the corresponding version control commands.

### `grype-fix`

Helps you identify and fix dependency vulnerabilities reported by **Grype**.

Use it when you want Ghostwriter to run (or use existing) Syft/Grype results, update vulnerable dependencies to fixed versions, verify the project builds, and document each change for traceability.

### `help`

Explains and troubleshoots Ghostwriter acts.

Use it when you want to list available acts (built-in and custom) or inspect a specific act’s configuration, inheritance, and key properties.

### `release-notes`

Generates release notes from git commit history and writes them into `src/changes/changes.xml`.

Use it when preparing a release and you want Ghostwriter to collect commits for a release version, group changes by type, and append a new `<release>` entry that follows the Maven changes format.

### `sonar-fix`

Helps you fix issues reported by **SonarQube**.

Use it when you have access to a SonarQube report (via its Web API) and want Ghostwriter to apply minimal code changes to resolve issues, rebuild and re-check, and only use `@SuppressWarnings` (never `//NOSONAR`) when a real code fix is not practical.

### `task`

A general-purpose act for doing a task in this repository.

Use it for day-to-day requests where you want Ghostwriter to act like a project-aware assistant, using the repository’s structure and conventions.

### `unit-tests`

Guides Ghostwriter to create high-quality unit tests.

Use it when you want Ghostwriter to analyze code behavior and generate isolated, repeatable tests with good coverage, placing them under the project’s test sources and avoiding production changes unless you explicitly request them.

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
