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

An Act is a saved “recipe” that tells Ghostwriter what to ask the AI and how to run the scan.

Instead of writing a long prompt every time, you choose an act name and (optionally) provide a short request. Ghostwriter loads the act definition from a small TOML file, combines it with your request, applies any act settings (for example scan directory or interactive mode), and then processes your project files.

Built-in acts ship with Ghostwriter under `src/main/resources/acts` (loaded from classpath `/acts/<name>.toml`). You can also provide your own act files with `--acts <path>`.

## What an Act TOML file contains

Acts are TOML files (`*.toml`). Common keys include:

- `description` (optional): human-friendly summary of what the act is for
- `instructions`: the AI “behavior rules” (sent as system instructions)
- `inputs`: a template used to build the final prompt (typically contains `%s`)
- `basedOn` (optional): inherit from another act
- `gw.*`: Ghostwriter runtime options (scan and execution settings)
- `ft.*`: functional tool settings (for example `ft.command.denylist`)
- Any other dotted keys: forwarded into Ghostwriter configuration

## How Ghostwriter runs an Act

Act mode is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

When you run an act, Ghostwriter:

1. Reads `--act <name> [request text...]`. If the act argument is missing or blank, it uses `help`.
2. Loads the act TOML from:
   - built-in classpath resources (`/acts/<name>.toml`)
   - and, if configured, from a user-provided acts location (`--acts <path>`), which can be a directory or an `http(s)://...` URL
3. Resolves inheritance (`basedOn`) and merges parent + child act data.
4. Builds the final prompt by inserting your request text into the act’s `inputs` template.
5. Applies act settings to Ghostwriter (instructions, prompt, scan settings, and other configuration).
6. Scans and processes the matched files using the composed instructions and prompt.

## Inheritance and “inherited values”

Acts support a few kinds of inheritance/override behavior. This lets you reuse a base act and override only what you need.

### 1) Act-to-act inheritance (`basedOn`)

If an act contains:

```toml
basedOn = "task"
```

Ghostwriter loads the parent act first (recursively, if needed), then merges the child act on top.

Notes based on `ActProcessor.loadAct(...)`:

- Inheritance can be chained.
- After the merge is complete, `basedOn` is removed from the final merged properties.

### 2) Custom acts can extend/override built-in acts (same name)

Ghostwriter can load an act of the same name from two places:

- built-in resources (classpath `/acts/...`)
- a custom acts location (`--acts <path>`)

If both exist, Ghostwriter loads both into the same properties map. Because the custom act is loaded first and the built-in act is loaded afterwards, the effective result depends on the merge rules below.

Practical guidance:

- If you want to safely customize a built-in act, use `basedOn` (create a new act that is based on the built-in one).
- If you do override an act by using the same name, verify the final merged keys (especially `instructions` and `inputs`) behave as intended.

### 3) String extension during act merge using `%s`

When Ghostwriter merges act data (`ActProcessor.setActData(...)`), string values do not always replace each other.

If the same key already exists as a string (for example from a parent act), and a later-loaded act provides a string for the same key, Ghostwriter will:

- take the existing string value
- replace the first `%s` in that existing string with the new string

This is a template-style “extend” mechanism.

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

### 4) Inheriting from the current runtime configuration using `%s`

After all act files are loaded and merged, Ghostwriter applies the act properties to the running configuration (`ActProcessor.applyActData(...)`).

For string properties, Ghostwriter also supports inheriting from whatever value is already present in the configurator (for example values coming from CLI flags or other config sources):

- If there is an existing config value for the same key, Ghostwriter replaces `%s` inside the act’s value with that existing value.
- If there is an existing config value and the act value does not contain `%s`, the act value replaces the existing value.

This is useful for extending multi-line strings such as a tool denylist.

Example:

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

After the act is loaded and merged, Ghostwriter inserts your request text into the act’s `inputs`.

- The request text is the part after the act name: `--act <name> [request text...]`.
- If you do not provide request text, Ghostwriter uses its current default prompt.
- Ghostwriter replaces `%s` in `inputs` with that request text.

## How Acts fit into Ghostwriter (key responsibilities)

`ActProcessor` is responsible for:

- selecting the act name and extracting request text (`setDefaultPrompt(String act)`)
- loading act TOML from classpath and/or user-defined location (`tryLoadActFromClasspath`, `tryLoadActFromDirectory`, `loadAct`)
- resolving inheritance via `basedOn` (`loadAct`)
- merging act properties (including `%s` string extension) (`setActData`)
- applying act settings into Ghostwriter runtime/configuration (`applyActData`)

## How to use an Act (step-by-step)

1. Choose an act name (for example `task`).
2. (Optional) Point Ghostwriter to a directory (or URL) containing custom acts with `--acts <path>`.
3. Write your short request text after the act name.
4. Run Ghostwriter.

Example:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -a "task Please rewrite these docs for end users"
```

If you omit the act name (or pass an empty act), Ghostwriter uses `help`.

## Built-in Acts (`src/main/resources/acts`)

### assembly

Implements a task using recommended libraries described as Bindex JSON.

Use it when you want Ghostwriter to select and apply existing libraries (via Bindex metadata) instead of writing everything from scratch.

### code-doc

Adds or updates documentation comments in source code (for example Javadoc or docstrings).

Use it when you want to improve code documentation without changing program behavior.

### commit

Helps you commit repository changes by checking status, grouping changes, generating commit messages, and running the version control commands.

Use it when you want guided, automated commits with messages that match the project’s commit style.

### grype-fix

Fixes dependency vulnerabilities reported by Grype by updating dependencies, rebuilding, and documenting the changes.

Use it when you have Grype results (or can run Grype) and want to streamline vulnerability remediation.

### help

Explains acts and helps you discover, inspect, and understand available acts (including inheritance and configuration).

Use it when you are new to acts, or when you want details about how an act is configured.

### release-notes

Generates release notes from git commit history and writes them into `src/changes/changes.xml` using the Maven Changes schema.

Use it when preparing a release and you want consistent, structured release notes.

### sonar-fix

Fixes issues reported by SonarQube (typically from a JSON report), applying minimal changes and only using `@SuppressWarnings` under strict rules.

Use it when you can provide SonarQube report access and want targeted code quality and security fixes.

### task

A minimal, general-purpose template.

Use it for one-off requests where you want Ghostwriter to apply your instructions to the project.

### unit-tests

Generates and improves unit tests using the project’s build and coverage tooling (for example Maven + JaCoCo).

Use it when you want higher test coverage and new or improved tests added to the project.
