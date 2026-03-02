---
canonical: https://machai.machanism.org/ghostwriter/act.html
---

<!--
@guidance:
Create the Act page as a Project Information page for the project.

- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Summarize the purpose of the Act feature, its key methods, and how it fits into the overall project.
- Provide easy-to-follow, step-by-step instructions or a practical example showing how to use the Act feature in real scenarios.
- Ensure the content is accessible and helpful for all users, including those new to the codebase or without a technical background.
-->

# Act

## What “Act” means in Ghostwriter

In Ghostwriter, an **Act** is a reusable preset that tells Ghostwriter *how to run* on a set of files.

An Act typically bundles:

- **Instructions**: the high-level rules you want the AI to follow.
- **Inputs / prompt template**: a prompt (often with a placeholder) that becomes the request sent to the AI.
- **Run options**: small configuration toggles like exclusions or whether to use multiple threads.

This lets you run the same type of work (for example: “write documentation”, “refactor”, “generate summaries”) consistently across different files or projects by selecting an Act by name.

## Where Acts come from

Ghostwriter looks for Act definitions in two places, in this order:

1. **Built-in Acts packaged with the application** (classpath resources):
   - `acts/<name>.toml`
2. **Optional custom Act directory** (configured via `gw.acts`):
   - `<gw.acts>/<name>.toml`

If the Act is not found in either location, Ghostwriter stops with an error.

## What an Act file contains (TOML)

An Act is defined in a `.toml` file. The `ActProcessor` reads the TOML file and applies its keys.

Common keys used by Ghostwriter:

- `instructions` — the instructions text applied to the AI run.
- `inputs` — the prompt template. It can contain a placeholder for your extra text (see below).
- `gw.threads` — enables/disables multi-threading for processing.
- `gw.excludes` — a comma-separated list of exclude patterns.
- `gw.nonRecursive` — whether file scanning should avoid recursion.

Any other string key/value pairs in the Act TOML are forwarded into Ghostwriter’s configuration as-is.

### Prompt placeholder

When you run an Act, you can provide extra text after the Act name. Ghostwriter inserts that extra text into the Act’s `inputs` value using `String.format(...)`.

That means `inputs` is usually written like:

```toml
inputs = "Write documentation for: %s"
```

…and your provided extra text becomes the `%s`.

## How Act processing works (based on `ActProcessor`)

`ActProcessor` is responsible for loading an Act and applying it before Ghostwriter processes files.

Key behavior:

- **`setDefaultPrompt(String act)`**
  - Parses `act` as: `<name> [prompt...]`.
  - Loads `acts/<name>.toml` from built-in resources, then also tries `<gw.acts>/<name>.toml` if configured.
  - Applies the TOML settings (instructions, inputs, and other configuration keys).
  - If no Act file is found, throws an error.

- **`setActData(String prompt, TomlParseResult toml)`**
  - Reads all TOML properties.
  - Recognizes a few special keys (like `instructions`, `inputs`, `gw.excludes`, etc.).
  - Everything else is written into the general configuration.

- **`processParentFiles(ProjectLayout projectLayout)`**
  - Scans the current project directory for files.
  - Filters out module directories and any files that do not match Ghostwriter’s selection rules.
  - Processes each matching file.
  - If the *project directory itself* matches and there is a prompt, it will also run processing at the directory level.

In other words, an Act is the “recipe” that tells Ghostwriter what to do, and `ActProcessor` applies that recipe before the normal file-processing pipeline runs.

## Practical example: create and run a custom Act

This example shows the typical workflow for making an Act that writes or updates documentation.

### Step 1: Create an Act directory

Choose a folder to store your custom Acts, for example:

- `./acts-custom/`

Configure Ghostwriter to use it by setting `gw.acts` to that folder (how you set configuration depends on how you run Ghostwriter in your environment).

### Step 2: Create the Act TOML file

Create:

- `./acts-custom/docs.toml`

Example contents:

```toml
# High-level rules for the AI
instructions = "You are a documentation assistant. Keep the writing clear and user-friendly."

# Prompt template; your extra text will be inserted into %s
inputs = "Update documentation for the following area: %s"

# Optional run settings
# Use multiple threads (if supported by your run)
gw.threads = "true"

# Exclude noisy or generated paths (comma-separated)
gw.excludes = "target,.git,node_modules"

# Whether to avoid scanning subfolders
# gw.nonRecursive = "false"
```

### Step 3: Run the Act

Run Ghostwriter with the Act name, and optionally add a short prompt after it.

Conceptually:

- Act name: `docs`
- Extra prompt text: `the Act feature page`

Ghostwriter will:

1. Load `docs.toml`.
2. Apply `instructions`.
3. Build the final prompt by inserting your extra text into `inputs`.
4. Scan and process matching files under the project directory (respecting excludes and other settings).

## Tips for writing reliable Acts

- Keep `instructions` short and specific: “what good looks like” is more helpful than lots of detail.
- Make `inputs` a template with exactly one `%s` unless you intentionally want multiple placeholders.
- Put exclude patterns in `gw.excludes` to avoid processing build output, dependencies, or generated files.
- Prefer custom Acts in `gw.acts` when you want team-specific or project-specific behavior.
