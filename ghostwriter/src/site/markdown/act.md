---
<!-- @guidance: 
Create the Act page as a Project Information page for the project:
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
canonical: https://machai.machanism.org/ghostwriter/act.html
---

# Act

An **Act** is a reusable “recipe” that tells Ghostwriter what to do and how to talk to the AI.

Instead of writing a long prompt every time, you choose an act by name and (optionally) add a short request. Ghostwriter loads the act’s TOML file, combines it with your request, and applies the result while scanning and processing your project files.

Acts are small **TOML** files. Built-in acts ship with Ghostwriter in `src/main/resources/acts`. You can also provide your own acts using `--acts <path>`.

## What an Act can contain

An act file can provide:

- **AI behavior instructions** via `instructions`
- A **prompt template** via `inputs` (your request is inserted into it)
- Optional **Ghostwriter runtime options** under `[gw]` (for example `scanDir`, `excludes`, `threads`, `nonRecursive`)
- Optional **functional tool configuration** under `[ft.*]` (for example `[ft.command] denylist`)
- Any other dotted keys (for example `gw.scanDir = "glob:."`), which are forwarded into Ghostwriter configuration

## How Acts run (what Ghostwriter does)

Act mode is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

When you run an act, Ghostwriter:

1. **Chooses the act name** (from `--act`). If you don’t provide one, it uses `help`.
2. **Loads the act TOML** from:
   - built-in resources (classpath `/acts/<name>.toml`)
   - and/or a custom acts location (`--acts <path>`, which can be a local directory or an `http(s)://...` URL)
3. **Resolves inheritance** (`basedOn`) and merges parent → child.
4. **Builds the final prompt** by inserting your request text into the act’s `inputs`.
5. **Applies act settings** (AI instructions, prompt, scan options, and other configuration values).
6. **Scans and processes files** using the composed instructions and prompt.

## Common Act TOML keys

You will commonly see:

- `description` (optional): short summary of the act
- `instructions`: the high-level “how to behave” instructions sent to the AI
- `inputs`: a template that receives your request text
- `basedOn` (optional): inherit from another act
- `[gw] ...`: Ghostwriter scan/run options
- `[ft.command] ...`: functional-tool related configuration (for example a denylist)
- Other dotted keys: also applied to Ghostwriter configuration

## Inheritance and “inherited values”

Acts support several kinds of inheritance/override behavior. This lets you reuse a base act and change only what you need.

### 1) Inheriting from another act (`basedOn`)

If an act contains:

```toml
basedOn = "task"
```

Ghostwriter loads the parent act first, then loads the child act and merges the properties.

Key points:

- Inheritance is **recursive** (a parent can itself have `basedOn`).
- After loading the parent, Ghostwriter removes `basedOn` from the merged result.

### 2) Custom acts override built-in acts (same act name)

Ghostwriter can load an act definition from both:

- built-in resources (`src/main/resources/acts` → classpath `/acts/...`)
- a custom acts location (`--acts <path>`)

If both exist for the same act name, Ghostwriter loads both so your **custom act overlays and overrides** the built-in act.

This is the recommended way to customize behavior without editing the shipped act files.

### 3) Template-style inheritance for strings using `%s` (act-to-act merge)

When merging act properties, Ghostwriter uses a special rule for **string values** (`ActProcessor.setActData(...)`):

- If a key already exists as a string (inherited from a parent act, or from the built-in act)
- and the child also provides a string for the same key
- the child string is inserted into the parent string by replacing the parent’s first `%s`

This allows “extend” behavior instead of “replace” behavior.

Example:

Parent act:

```toml
inputs = "# Task\n\n%s\n"
```

Child act:

```toml
inputs = "Please focus only on documentation changes."
```

Merged `inputs` becomes:

```text
# Task

Please focus only on documentation changes.
```

### 4) Inheriting from existing Ghostwriter configuration at runtime (`%s`)

After the act data is loaded and merged, Ghostwriter applies it (`ActProcessor.applyActData(...)`).

For each string property:

- Ghostwriter checks whether there is already an existing value for the same key in the current configuration (from CLI flags, environment, or other config sources).
- If there is an existing value and the act value contains `%s`, Ghostwriter replaces `%s` with that existing value.
- If there is an existing value and the act value does not contain `%s`, the act value replaces it.

This is commonly used to *extend* multi-line configuration strings, such as a functional-tool denylist.

Example (append to an existing denylist):

```toml
[ft.command]
denylist = '''
%s
# extra project-specific blocks
rm -rf
'''
```

At runtime, `%s` is replaced with the denylist Ghostwriter already had.

### 5) Inserting your request text into `inputs`

Finally, Ghostwriter inserts your request text into `inputs`:

- Your request text comes from `--act <name> [request text...]`.
- If you do not provide request text, Ghostwriter uses the processor’s current default prompt.
- Ghostwriter replaces the first `%s` in `inputs` with your request.

## How to run an act

You run an act using `-a/--act`.

### Step-by-step

1. Pick an act name (for example `task`).
2. (Optional) Provide a custom acts directory or URL with `--acts <path>`.
3. Add a short request after the act name.
4. Run Ghostwriter.

Example:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -a "task Please rewrite these docs for end users"
```

If you omit the act name, Ghostwriter uses `help`.

## Where Acts fit in Ghostwriter (key responsibilities)

`ActProcessor` is responsible for Act mode. Its key responsibilities are:

- **Select act + request text** (`setDefaultPrompt(String act)`)
- **Load act TOML** from classpath and/or an external acts location (`tryLoadActFromClasspath`, `tryLoadActFromDirectory`)
- **Resolve inheritance** via `basedOn` (`loadAct`)
- **Merge act properties** including `%s`-based template extension (`setActData`)
- **Apply act settings** to Ghostwriter runtime/configuration (`applyActData`)

## Built-in Acts (`src/main/resources/acts`)

### assembly

Uses a list of recommended libraries (in **Bindex JSON** format) to help build or assemble an application.

Use it when you have library recommendations available and want Ghostwriter to fetch detailed library metadata (via `get_bindex` and `get_bindex_schema`) and implement a solution using those libraries.

### commit

Helps you document and commit changes in your repository.

Use it when you want Ghostwriter to review what changed, group changes into logical commits (feature/fix/docs/chore, etc.), generate commit messages that match the project’s style, and provide (and potentially run) the commands to commit each group.

### grype-fix

Helps you fix dependency vulnerabilities reported by Grype.

Use it when you have (or can generate) a Grype report and want Ghostwriter to update vulnerable dependencies (for Maven, typically `pom.xml`), build the project to verify, and document what changed and why.

Note: Syft and Grype must be installed.

### help

Explains how acts work and helps you discover what acts are available.

Use it when you want to list available acts, inspect an act’s contents (instructions/inputs/options), and understand inheritance and override behavior.

### release-notes

Generates release notes from git history and writes them into `src/changes/changes.xml` using the Maven Changes schema.

Use it when preparing a release and you want consistent, structured release notes.

### sonar-fix

Helps you fix issues reported by SonarQube.

Use it when you have access to a SonarQube JSON report and want Ghostwriter to apply minimal, targeted fixes, rebuilding as needed, and only using `@SuppressWarnings` under strict documented rules.

### task

A minimal, general-purpose template.

Use it for one-off requests where you simply want Ghostwriter to run your instruction against the project.

### unit-tests

Guides Ghostwriter to generate and improve unit tests.

Use it when you want Ghostwriter to run the build, inspect JaCoCo coverage, and add or improve tests until coverage is high (targeting at least 90%).
