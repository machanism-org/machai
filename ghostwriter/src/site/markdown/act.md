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

An Act is a reusable set of instructions that tells Ghostwriter what to do and how to talk to the AI.

Instead of rewriting a long prompt each time, you run Ghostwriter with an act name and (optionally) a short request. Ghostwriter loads the act file (a small TOML document), combines it with your request, applies any act settings (scan options and other configuration), and then processes your project files.

Built-in acts are included with Ghostwriter under `src/main/resources/acts`. You can also provide your own act files using `--acts <path>`.

## What an Act TOML file contains

Acts are TOML files (`*.toml`). Most acts contain:

- `description` (optional): a short human-friendly summary
- `instructions`: the AI behavior rules (the “system instructions”)
- `inputs`: a prompt template used to build the final request sent to the AI
- `basedOn` (optional): inherit from another act
- `gw.*` options: Ghostwriter runtime settings (for example scan directory, non-recursive mode, concurrency)
- `ft.*` options: functional tool settings (for example a command denylist)
- Any other dotted keys: forwarded into Ghostwriter configuration

## How Ghostwriter runs an Act

Act mode is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

When you run an act, Ghostwriter:

1. Picks the act name from `--act`. If not provided or blank, it uses `help`.
2. Loads the act TOML:
   - from built-in resources on the classpath (`/acts/<name>.toml`)
   - and optionally from a user-supplied acts location (`--acts <path>`), which can be a directory or an `http(s)://...` URL
3. Resolves inheritance (`basedOn`) and merges parent and child act data.
4. Builds the final prompt by inserting your request text into the act’s `inputs` template.
5. Applies act settings to Ghostwriter (instructions, prompt, scan settings, and other configuration values).
6. Scans and processes files using the composed instructions and prompt.

## Inheritance and “inherited values”

Acts support multiple kinds of inheritance and override behavior. This is how you reuse a base act and change only what you need.

### 1) Act-to-act inheritance (`basedOn`)

If an act contains:

```toml
basedOn = "task"
```

Ghostwriter loads the parent act first (recursively, if needed), then loads the child act and merges the values.

Important details:

- Inheritance can be chained: an act can be based on an act that is itself based on another act.
- After loading the parent, Ghostwriter removes the `basedOn` property from the merged result.

### 2) Custom acts override built-in acts (same name)

Ghostwriter can load the same act name from:

- built-in resources (`src/main/resources/acts` -> classpath `/acts/...`)
- a custom acts location (`--acts <path>`)

If both exist for the same act name, Ghostwriter loads both. The custom act is applied in addition to the built-in act, so custom values can override or extend the built-in definition. This is the recommended way to customize behavior without editing the shipped act files.

### 3) Extending strings during act merge using `%s`

When Ghostwriter merges act data, it uses a special rule for string values (`ActProcessor.setActData(...)`):

- If a key already exists as a string (from a parent act or from the built-in act)
- and the child provides a string value for the same key
- the child’s value is inserted into the existing value by replacing the first `%s` in the existing value

This provides “extend” behavior instead of always replacing the parent value.

Example:

Parent act:

```toml
inputs = "# Task\n\n%s\n"
```

Child act:

```toml
inputs = "Please focus only on documentation changes."
```

Merged result:

```text
# Task

Please focus only on documentation changes.
```

### 4) Inheriting from existing runtime configuration using `%s`

After all act files are loaded and merged, Ghostwriter applies the values (`ActProcessor.applyActData(...)`). For each string property:

- If Ghostwriter already has a value for the same key in its current configuration (for example from CLI flags or other config sources), and the act value contains `%s`, Ghostwriter replaces `%s` with that existing value.
- If there is an existing value but the act value does not contain `%s`, the act value replaces it.

This is commonly used to extend multi-line configuration strings (for example to append extra entries to a functional tool denylist).

Example:

```toml
[ft.command]
denylist = '''
%s
# extra project-specific blocks
rm -rf
'''
```

At runtime, `%s` is replaced with whatever denylist Ghostwriter already had.

### 5) Inserting your request text into `inputs`

Finally, Ghostwriter inserts your request text into the act’s `inputs`:

- Request text is the part after the act name in `--act <name> [request text...]`.
- If you do not provide request text, Ghostwriter uses its current default prompt.
- Ghostwriter replaces `%s` in `inputs` with your request text.

## How Acts fit into Ghostwriter (key responsibilities)

`ActProcessor` is responsible for:

- selecting act name and request text (`setDefaultPrompt(String act)`)
- loading act TOML from classpath and/or user-defined location (`tryLoadActFromClasspath`, `tryLoadActFromDirectory`)
- resolving inheritance via `basedOn` (`loadAct`)
- merging act properties (including `%s` string extension) (`setActData`)
- applying act settings into Ghostwriter runtime/configuration (`applyActData`)

## How to use an Act

1. Choose an act name (for example `task`).
2. (Optional) Provide a custom acts directory or URL using `--acts <path>`.
3. Add your short request after the act name.
4. Run Ghostwriter.

Example:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -a "task Please rewrite these docs for end users"
```

If you omit the act name, Ghostwriter uses `help`.

## Built-in Acts (`src/main/resources/acts`)

### assembly

Uses recommended libraries (described via Bindex JSON) to implement a task.

Use it when you want Ghostwriter to choose and apply libraries instead of writing everything from scratch, and you have (or want to generate) a list of library recommendations.

### code-doc

Adds or updates documentation comments in source code (for example Javadoc or docstrings).

Use it when you want to improve code documentation without changing program behavior.

### commit

Analyzes repository changes and helps group and commit them.

Use it when you want Ghostwriter to generate commit messages and run the version control commands needed to commit changes in logical groups.

### grype-fix

Fixes dependency vulnerabilities reported by Grype.

Use it when you want Ghostwriter to generate an SBOM (Syft), scan it (Grype), update vulnerable dependencies, build the project, and document what changed.

### help

Explains acts and helps you discover and inspect available acts.

Use it when you are new to acts, want to understand inheritance/overrides, or want details about a specific act.

### release-notes

Generates release notes from git history and writes them into `src/changes/changes.xml` using the Maven Changes schema.

Use it when preparing a release and you want consistent, structured release notes.

### sonar-fix

Fixes issues reported by SonarQube.

Use it when you can provide access to a SonarQube report (typically JSON) and want Ghostwriter to apply minimal, targeted fixes, using `@SuppressWarnings` only under strict rules.

### task

A minimal, general-purpose template.

Use it for one-off requests where you simply want Ghostwriter to run your instruction against the project.

### unit-tests

Generates and improves unit tests and aims for high coverage.

Use it when you want Ghostwriter to run the build, inspect coverage (JaCoCo), and add or improve tests until coverage is high (targeting at least 90%).
