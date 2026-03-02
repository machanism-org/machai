---
canonical: https://machai.machanism.org/ghostwriter/act.html
---

## ActProcessor

`ActProcessor` is an `AIFileProcessor` implementation that runs Ghostwriter in **Act mode**. Instead of reading per-file `@guidance` directives, it loads a predefined “act” definition from a TOML file and applies it as the run configuration, including:

- the **system instructions** sent to the GenAI provider,
- the **default prompt** (optionally parameterized by user input),
- scan/execution behavior such as **threading**, **excludes**, and **non-recursive** traversal,
- and any additional configuration properties (via the shared `Configurator`).

Act definitions are searched in two places:

1. Built-in classpath resources: `acts/<name>.toml`
2. A custom directory configured by `gw.acts` (or set programmatically via `setActDir(...)`).

If the TOML cannot be found in either location, Act mode fails fast with an error.

## How it fits in Ghostwriter

Ghostwriter’s CLI (`org.machanism.machai.gw.processor.Ghostwriter`) chooses the processor implementation:

- Normal mode: `GuidanceProcessor` (processes embedded `@guidance` directives)
- Act mode (`--act`): `ActProcessor` (loads an act TOML and applies it as the run configuration)

Once configured, `ActProcessor` uses the inherited `AIFileProcessor#process(...)` workflow to:

- create and configure a `GenAIProvider` (`GenAIProviderManager.getProvider(...)`),
- apply function tools (`FunctionToolsLoader`),
- prepend the “Project Layout Overview” block to the prompt,
- and execute the provider.

## Key methods

### `setDefaultPrompt(String act)` (override)

Parses the CLI `--act` argument (format: `--act <name> [prompt]`), loads `<name>.toml`, and applies the act configuration.

Behavior summary:

- Splits the first token as the act **name**.
- Uses the remainder as the **prompt argument**.
  - If no remainder is provided, it falls back to the current processor default prompt (if any).
- Looks up the act definition:
  - first from `acts/<name>.toml` on the classpath,
  - then from `<actDir>/<name>.toml` if `actDir` is configured.
- Calls `setActData(promptArg, toml)` to apply TOML fields.
- Throws `IllegalArgumentException` if the act cannot be found.

### `setActData(String promptArg, TomlParseResult toml)`

Reads TOML properties using `toml.dottedEntrySet()` and applies recognized keys:

- `instructions`: forwarded to `AIFileProcessor#setInstructions(...)`
- `inputs`: used as the template for the default prompt
  - `String.format(value, promptArg)` is used to interpolate the user prompt argument
- `gw.threads`: forwarded to `AbstractFileProcessor#setModuleMultiThread(boolean)`
- `gw.excludes`: forwarded to `AbstractFileProcessor#setExcludes(String[])` (comma-separated)
- `gw.nonRecursive`: forwarded to `AbstractFileProcessor#setNonRecursive(boolean)`
- any other key: stored into the shared `Configurator` via `getConfigurator().set(key, value)`

### `processParentFiles(ProjectLayout projectLayout)` (override)

Controls what gets processed in Act mode:

1. Collects children under the project directory (recursive list via `findFiles(projectDir)`).
2. Removes:
   - module directories (`isModuleDir(projectLayout, child)`), and
   - any file/dir that does not match the scan include rules (`!match(child, projectDir)`).
3. Processes each remaining child via `processFile(...)`.
4. Finally, if the project directory itself matches and a default prompt is set, it runs a final `process(...)` on the project directory.

### `processFile(ProjectLayout projectLayout, File file)`

Thin wrapper that executes the provider using the currently configured instructions and default prompt:

- `process(projectLayout, file, getInstructions(), getDefaultPrompt())`

## Act TOML format (expected keys)

An act is a TOML file named `<name>.toml`. Typical keys:

```toml
# acts/<name>.toml
instructions = """
You are ...
"""

# Must contain a single `%s` placeholder if you want to inject the user-provided prompt argument.
inputs = """
Write release notes for: %s
"""

# Optional processor controls
"gw.threads" = "true"
"gw.excludes" = "target,node_modules"
"gw.nonRecursive" = "true"

# Any other custom settings consumed elsewhere can be set as strings as well.
"some.custom.key" = "value"
```

Notes:

- `ActProcessor` only applies TOML entries whose values are strings.
- `inputs` is formatted using `String.format(...)`. If it contains `%s`, it will be replaced with the prompt argument.

## Example: using Act mode from the CLI

### 1) Provide an act on the classpath

Create (or package) a resource:

- `src/main/resources/acts/review.toml`

```toml
instructions = """
You are a reviewer. Produce a short report.
"""

inputs = """
Review the following topic: %s
Provide risks and improvements.
"""
```

### 2) Run Ghostwriter with `--act`

```bash
java -jar gw.jar . --act "review Dependency upgrades"
```

What happens:

1. `Ghostwriter` constructs `ActProcessor`.
2. `ActProcessor.setDefaultPrompt("review Dependency upgrades")` loads `acts/review.toml`.
3. The user argument (`"Dependency upgrades"`) is inserted into `inputs` via `String.format(...)`.
4. `ActProcessor` scans and processes matching files/directories under the project root, and finally (if configured) processes the project directory itself with the act prompt.

### 3) Use a custom acts directory

If you don’t want to repackage resources, put `review.toml` in a directory and point Ghostwriter to it:

- set `gw.acts=<path-to-acts-dir>` in `gw.properties`, or
- call `ActProcessor#setActDir(...)` programmatically (the CLI also supports `--acts <dir>`).

<!--
@guidance:
Carefully analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class.  
- Create a clear and concise description of the main feature(s) and functionality provided by this class.
- Summarize its purpose, key methods, and how it integrates within the project.
- Provide step-by-step instructions or an example demonstrating how to use this class in practice.
- Ensure your description is understandable for developers who may be new to the codebase.
-->