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

An **Act** is a reusable template that tells Ghostwriter what to do and how to talk to the AI.

Instead of writing a long prompt every time, you select an act by name and (optionally) add a short request. Ghostwriter combines your request with the act’s template and runs it against your project files.

Acts are stored as small **TOML** files.

## What an Act can contain

An act file can:

- Provide **AI behavior instructions** via `instructions`
- Provide a **prompt template** via `inputs` (your request is inserted into it)
- Optionally set **Ghostwriter runtime options** under `[gw]` (for example `scanDir`, `excludes`, `threads`, `nonRecursive`)
- Provide **other configuration values** (any other keys are applied to Ghostwriter configuration)

Built-in acts ship with Ghostwriter under `src/main/resources/acts`. You can also provide your own acts using `--acts <path>`.

## How Acts run (what Ghostwriter does)

Act mode is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

At a high level, when you run an act, Ghostwriter:

1. **Chooses the act name** (from `--act`). If you don’t provide one, it uses `help`.
2. **Loads the act TOML** from:
   - built-in resources (classpath `/acts/<name>.toml`)
   - and/or your custom acts location (`--acts <path>`, which can be a local directory or an `http(s)://...` URL)
3. **Resolves inheritance** (`basedOn`) and merges parent → child.
4. **Builds the final prompt** by inserting your request text into the act’s `inputs` template.
5. **Applies act settings** to the current run (AI instructions, default prompt, scan options, and other configuration values).
6. **Scans and processes files** using the composed instructions and prompt.

## Act TOML keys you will commonly see

- `description` (optional): a short summary of the act
- `instructions`: the “system-style” guidance sent to the AI
- `inputs`: a template for your request text
- `basedOn` (optional): inherit from another act
- `[gw] ...`: Ghostwriter scan/run options
- `[ft.command] ...`: functional-tool related configuration (for example a denylist)
- Other dotted keys (for example `gw.scanDir = "glob:."`): also applied to Ghostwriter configuration

## Inheritance and “inherited values”

Acts support several kinds of inheritance/overrides. This is important because it lets you reuse a base act and change only the parts you need.

### 1) Inheriting from another act (`basedOn`)

If an act contains:

```toml
basedOn = "task"
```

Ghostwriter loads the parent act first, then loads the child act and merges the properties.

Key points:

- Inheritance is **recursive**: the parent can itself have `basedOn`.
- After loading the parent, Ghostwriter removes `basedOn` from the merged result.

### 2) Custom acts override built-in acts (same act name)

Ghostwriter can load an act from both:

- built-in resources (`src/main/resources/acts` → classpath `/acts/...`)
- a custom acts location (`--acts <path>`)

If both exist for the same act name, Ghostwriter loads both so your **custom act wraps and overrides** the built-in one.

This is the recommended way to customize behavior without editing the shipped act files.

### 3) Template-style inheritance for strings using `%s` (act-to-act merge)

When merging act properties, Ghostwriter uses a special rule for **string values** (`ActProcessor.setActData(...)`):

- If a key already exists as a string (inherited from a parent act or from the built-in act)
- and the child also provides a string for the same key
- the child string is inserted into the parent string by replacing the parent’s first `%s`

This lets a child act extend a parent template instead of replacing it.

Example:

Parent act:

```toml
inputs = "# Task\n\n%s\n"
```

Child act:

```toml
inputs = "Please focus only on documentation changes."
```

Merged `inputs`:

```text
# Task

Please focus only on documentation changes.
```

### 4) Inheriting from Ghostwriter’s existing configuration (runtime “inherit value”)

After the act data is loaded and merged, Ghostwriter applies it (`ActProcessor.applyActData(...)`).

For each string property:

- Ghostwriter first checks whether there is already an existing value for the same key in the current configuration (from CLI flags, environment, or other config sources).
- If there is an existing value and the act value contains `%s`, Ghostwriter replaces `%s` with that existing value.
- If there is an existing value and the act value does not contain `%s`, the act value simply replaces it.

This is commonly used to extend multi-line configuration strings such as a functional-tool denylist.

Example idea (append extra denylist lines while keeping the existing denylist):

```toml
[ft.command]
denylist = '''
%s
# extra project-specific blocks
rm -rf
'''
```

At runtime, `%s` is replaced with the existing denylist value.

### 5) Inserting your request text into `inputs`

Finally, Ghostwriter inserts your request text into `inputs`:

- Your request text comes from `--act <name> [request text...]`.
- If you do not provide request text, Ghostwriter uses the processor’s current default prompt.
- Ghostwriter replaces the first `%s` in `inputs` with your request.

## How to run an act

You run an act using `-a/--act`.

### Step-by-step

1. Pick an act name (for example `task`).
2. (Optional) Provide a custom acts directory with `--acts <path>`.
3. Add a short request after the act name.
4. Run Ghostwriter.

Example:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -a "task Please rewrite these docs for end users"
```

If you omit the act name, Ghostwriter uses `help`.

## Where Acts fit in Ghostwriter (key responsibilities)

`ActProcessor` is responsible for Act mode. The most important responsibilities are:

- **Select act + request text** (`setDefaultPrompt(String act)`)
- **Load act TOML** from classpath and/or an external acts location (`tryLoadActFromClasspath`, `tryLoadActFromDirectory`)
- **Resolve inheritance** via `basedOn` (`loadAct`)
- **Merge properties** including `%s`-based template extension (`setActData`)
- **Apply act settings** to Ghostwriter runtime/configuration (`applyActData`)

## Built-in Acts (`src/main/resources/acts`)

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
