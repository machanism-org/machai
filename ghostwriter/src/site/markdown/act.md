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

In Ghostwriter, an **Act** is a reusable preset that tells Ghostwriter **what instructions to use** and **what prompt template to run** when processing your project’s files.

Instead of rewriting the same instructions every time (“write docs”, “summarize code”, “refactor”, etc.), you pick an Act by name and Ghostwriter loads its settings from a small configuration file.

## Where Acts are loaded from

`ActProcessor` looks for an Act definition in this order:

1. **Built-in Acts** bundled with the application (classpath resources):
   - `/acts/<name>.toml`
2. **Optional custom Acts folder** configured by `gw.acts`:
   - `<gw.acts>/<name>.toml`

If no matching file is found, Ghostwriter stops with an error.

## What an Act file contains

An Act is a TOML file (`.toml`). `ActProcessor` reads every key/value pair in the file.

### Special keys Ghostwriter recognizes

These keys have built-in meaning:

- `instructions` — the “system instructions” sent to the AI provider.
- `inputs` — a **prompt template**. Ghostwriter uses `String.format(...)` to insert your extra prompt text.
- `gw.threads` — turns module multi-threading on/off.
- `gw.excludes` — comma-separated exclude patterns used during scanning.
- `gw.nonRecursive` — turns off recursion when scanning modules.

### Other keys

Any other string key/value pairs are forwarded into Ghostwriter’s configuration.

## How Act processing works (key methods)

These are the key pieces of behavior from `ActProcessor`:

- **`setDefaultPrompt(String act)`**
  - Expects: `<name> [optional prompt...]`.
  - Splits the Act name from the optional extra prompt text.
  - Loads `<name>.toml` from built-in `/acts/` resources first, then tries the configured `gw.acts` directory.
  - Applies the Act settings (instructions, prompt template, and other config values).
  - Throws an error if the Act can’t be found.

- **`setActData(String prompt, TomlParseResult toml)`**
  - Walks all TOML properties.
  - Applies the special keys listed above.
  - For `inputs`, builds the final prompt with:
    - `String.format(inputsTemplate, prompt)`
  - Any other TOML keys are stored in the general configuration.

- **`processParentFiles(ProjectLayout projectLayout)`**
  - Scans the project directory for files.
  - Skips module directories and anything that doesn’t match Ghostwriter’s file selection rules.
  - Processes each matching file.
  - If the project directory itself matches and there is a prompt, it may also run once at the directory level.

In short: an Act is the “recipe”, and `ActProcessor` loads that recipe before Ghostwriter runs its normal file-processing pipeline.

## Practical example: create and run a custom Act

This example shows a typical workflow for creating a custom Act to update documentation.

### Step 1: Create a folder for custom Acts

Pick a directory for your team/project, for example:

- `./acts-custom/`

Configure Ghostwriter to use it by setting:

- `gw.acts=./acts-custom`

(How you set configuration depends on how you run Ghostwriter in your environment.)

### Step 2: Create the Act TOML file

Create this file:

- `./acts-custom/docs.toml`

Example contents:

```toml
# Instructions for the AI
instructions = "You are a documentation assistant. Write clearly for beginners."

# Prompt template: Ghostwriter inserts your extra text where %s appears
inputs = "Update documentation for: %s"

# Optional run settings
# Note: values are strings in the TOML file; ActProcessor parses some of them.
gw.threads = "true"
gw.excludes = "target,.git,node_modules"
# gw.nonRecursive = "false"
```

### Step 3: Run the Act

Run Ghostwriter with the Act name, plus an optional short prompt.

Conceptually:

- Act name: `docs`
- Extra prompt text: `the Act feature page`

Ghostwriter will:

1. Load `docs.toml`.
2. Apply `instructions`.
3. Build the final prompt by inserting your extra text into `inputs`.
4. Scan and process files under the project directory (respecting excludes and other settings).

## Tips for writing Acts that work well

- Keep `instructions` short and specific so results are consistent.
- Make sure your `inputs` template contains the placeholder(s) you intend (commonly a single `%s`).
- Use `gw.excludes` to avoid generated folders such as `target/` and dependency folders.

## Notes from the current implementation

- If `inputs` does not include a valid `String.format(...)` placeholder, prompt formatting can fail when the Act is applied.
- `gw.nonRecursive` is currently parsed using `Boolean.getBoolean(...)` (which reads a JVM system property), not `Boolean.parseBoolean(...)`. If you set `gw.nonRecursive = "true"` in an Act file, it may not behave as expected unless there is a matching system property.
