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

An Act is a saved set of instructions that tells Ghostwriter what to ask the AI and how to run the scan.

Instead of writing a long prompt every time, you select an act name and (optionally) add a short request. Ghostwriter loads the act definition from a TOML file, combines it with your request, applies any act settings, and then processes the files in your project.

Built-in acts ship with Ghostwriter under `src/main/resources/acts` (loaded from classpath as `/acts/<name>.toml`). You can also provide your own act files with `--acts <path>`, where `<path>` can be a directory or an `http(s)://...` URL.

## What an Act TOML file contains

Acts are TOML files (`*.toml`). Common keys include:

- `description` (optional): a human-friendly summary shown in help or listings
- `instructions`: the AI “rules” for the run (sent as system instructions)
- `inputs`: a template used to build the final prompt (usually contains `%s`)
- `basedOn` (optional): inherit from another act
- `gw.*`: Ghostwriter runtime options (scan and execution settings)
- `ft.*`: functional tool settings (for example `ft.command.denylist`)
- Other dotted keys: forwarded into Ghostwriter configuration

## How Ghostwriter runs an Act

Act mode is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

When you run an act, Ghostwriter:

1. Reads the act argument in the form: `<name> [request text...]`.
   - If the act argument is missing or blank, it uses `help`.
2. Loads the act TOML from:
   - built-in classpath resources (`/acts/<name>.toml`)
   - and, if configured, a user-provided acts location (`--acts <path>`) as either a directory or an `http(s)://...` URL
3. Resolves inheritance (`basedOn`) and merges parent + child act data.
4. Builds the final prompt by inserting your request text into the act’s `inputs`.
5. Applies act settings to Ghostwriter (instructions, prompt, scan settings, and other configuration).
6. Scans and processes the matching files using the composed instructions and prompt.

## Inheritance and “inherited values”

Acts support several inheritance/override behaviors. This allows you to reuse a base act and override only the parts you need.

### 1) Act-to-act inheritance (`basedOn`)

If an act contains:

```toml
basedOn = "task"
```

Ghostwriter loads the parent act first (recursively, if needed), then merges the child act on top. After the merge is complete, `basedOn` is removed from the final merged properties.

### 2) Combining a custom act and a built-in act

Ghostwriter can look for the same act name in two places:

- built-in resources (`/acts/<name>.toml`)
- a custom acts location (`--acts <path>`)

If both exist, both are loaded and merged into the same property map.

Practical guidance:

- For most “customization”, prefer creating a new act and setting `basedOn` to a built-in act.
- If you define an act with the same name as a built-in act, test the final merged result carefully.

### 3) Extending strings during act merge using `%s`

When Ghostwriter merges act data (`ActProcessor.setActData(...)`), string values can be *extended* instead of replaced.

If the same key already exists as a string (for example from a parent act), and a later-loaded act provides a string for the same key, Ghostwriter will:

- take the existing string value
- replace `%s` inside the existing string with the new string

This provides a “wrap/extend” mechanism for fields like `inputs`.

Example:

Parent act:

```toml
inputs = "# Task\n\n%s\n"
```

Child act:

```toml
inputs = "Please focus only on documentation changes."
```

Effective merged result:

```text
# Task

Please focus only on documentation changes.
```

### 4) Extending values from the current runtime configuration using `%s`

After all act files are loaded and merged, Ghostwriter applies the act properties to the running configuration (`ActProcessor.applyActData(...)`).

For string properties, Ghostwriter can also inherit from whatever value is already present in the configuration (for example values coming from CLI flags or other config sources):

- If there is an existing config value for the same key, Ghostwriter replaces `%s` inside the act’s value with that existing value.
- If there is an existing config value and the act value does not contain `%s`, the act value replaces the existing value.

This is commonly used to extend multi-line settings such as `ft.command.denylist`.

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

After the act is loaded and merged, Ghostwriter inserts your request text into the act’s `inputs` template.

- The request text is whatever comes after the act name: `--act <name> [request text...]`.
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
3. Write a short request text after the act name.
4. Run Ghostwriter.

Example:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -a "task Please rewrite these docs for end users"
```

If you omit the act name (or pass an empty act), Ghostwriter uses `help`.

## Built-in Acts (`src/main/resources/acts`)

### assembly

Implements user tasks by using recommended libraries described as Bindex JSON.

Use it when you want Ghostwriter to pick libraries for you (via Bindex), retrieve detailed library metadata, and build your solution using those libraries rather than starting from scratch.

### code-doc

Adds or updates documentation comments in source code (for example Javadoc, docstrings, or XML comments).

Use it when you want better in-code documentation without changing program behavior.

### commit

Automates committing changes by checking version control status, grouping changes, generating commit messages, and running the commit commands.

Use it when you want guided, automated commits that follow the project’s historical commit style.

### grype-fix

Fixes dependency vulnerabilities reported by Grype by updating dependencies, rebuilding, and documenting the changes.

Use it when you can run (or provide) Syft/Grype results and want a streamlined vulnerability remediation workflow.

### help

Explains Acts, lists available acts, and helps you understand act settings and inheritance.

Use it when you are new to Acts or want to inspect how a particular act is configured.

### release-notes

Generates release notes from git commit history and writes them into `src/changes/changes.xml` using the Maven Changes schema.

Use it when preparing a release and you want consistent, structured release notes recorded in the standard Maven changes format.

### sonar-fix

Fixes issues reported by SonarQube (typically from a JSON report), with strict rules around when and how `@SuppressWarnings` may be used.

Use it when you can provide SonarQube access/report data and want targeted, minimal code fixes that match SonarQube guidance.

### task

A minimal, general-purpose template.

Use it for one-off requests where you want Ghostwriter to apply your instructions to the project.

### unit-tests

Generates and improves unit tests using the project’s build and coverage tooling (for example Maven + JaCoCo).

Use it when you want higher test coverage and new or improved tests added to the project.
