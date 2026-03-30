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

Acts are reusable “recipes” for running Ghostwriter.

An act bundles:

- what the AI should do (instructions)
- how your request text is placed into a prompt template (inputs)
- optional Ghostwriter runtime settings (for example scan patterns or interactive mode)

This makes it easy to repeat common tasks (write docs, generate tests, produce release notes, etc.) without rewriting long prompts each time.

Ghostwriter includes built-in acts under `src/main/resources/acts` (loaded from the classpath as `/acts/<name>.toml`). You can also add your own act files using `--acts <path>`, where `<path>` can be a directory path or an `http(s)://...` URL.

## What an Act TOML file contains

Acts are TOML files (`*.toml`). Common keys include:

- `description` (optional): short summary of what the act does
- `instructions`: the “rules” for the AI run
- `inputs`: a prompt template; your request text is inserted into this template
- `basedOn` (optional): inherit from another act
- `gw.*`: Ghostwriter runtime options (for example scan patterns, interactive mode)
- `ft.*`: functional tool options (for example command denylist)
- other dotted keys: forwarded into Ghostwriter configuration

## How Ghostwriter runs an Act

Act mode is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

When you run an act, Ghostwriter:

1. Parses the act argument in the form: `<name> [request text...]`.
   - If the act argument is missing or blank, Ghostwriter uses `help`.
2. Loads the act TOML from (in this order):
   - an optional user-provided acts location (`--acts <path>`) as a directory or URL
   - the built-in classpath resources (`/acts/<name>.toml`)
3. Resolves inheritance (`basedOn`) so parent acts are loaded first.
4. Merges all act properties into a final property set.
5. Builds the final prompt by inserting your request text into the act’s `inputs`.
6. Applies the act settings to Ghostwriter’s runtime configuration.
7. Scans and processes the matching files using the composed instructions and prompt.

## Inheritance and “inherited values”

Acts support inheritance so you can reuse a base act and change only what you need.

There are three main “inheritance” layers:

1. Inheritance from another act via `basedOn`.
2. Overlay of a user act on top of a built-in act with the same name.
3. Inheritance from the existing runtime configuration when the act is applied.

### 1) Act-to-act inheritance (`basedOn`)

If an act contains:

```toml
basedOn = "task"
```

Ghostwriter loads the parent act first (recursively), then loads the child act and merges the child values on top of the parent. After this is done, the `basedOn` key is removed from the final merged properties.

What this means for you:

- Put shared defaults in the base act.
- Override only the keys you want in the child.

### 2) User acts can overlay built-in acts (same name)

Ghostwriter can load an act from two places:

- a custom acts location (`--acts <path>`)
- the built-in resources (`/acts/<name>.toml`)

If both exist, Ghostwriter loads both into the same property map (custom first, built-in second).

Important note: because of the load order, a built-in value may replace a custom value unless you use the string extension mechanism described below.

Practical guidance:

- Prefer creating a new act name and using `basedOn` to inherit from a built-in act.
- If you intentionally “shadow” a built-in act by using the same name, verify the final merged content.

### 3) Extending string values during merge using `%s`

When Ghostwriter merges act data (`ActProcessor.setActData(...)`), string values can be extended rather than simply replaced.

Rule:

- If a key already exists as a string, and a later-loaded act provides a new string for the same key, Ghostwriter replaces the first `%s` in the existing string with the new string.

This creates a “wrap/insert” mechanism.

Example:

Parent act:

```toml
inputs = "# Task\n\n%s\n"
```

Child act:

```toml
inputs = "Please focus only on documentation changes."
```

Final merged result:

```text
# Task

Please focus only on documentation changes.
```

### 4) Inheriting from the current runtime configuration using `%s`

After all act files are merged, Ghostwriter applies act properties to the running configuration (`ActProcessor.applyActData(...)`).

For string values:

- If Ghostwriter already has a value for the same key (from CLI flags or other config sources), and the act value contains `%s`, Ghostwriter replaces `%s` with that existing value.
- If the act value does not contain `%s`, the act value replaces the existing value.

This is commonly used to extend multi-line settings such as `ft.command.denylist`.

Example:

```toml
[ft.command]
denylist = '''
%s
# extra blocks
rm -rf
'''
```

At runtime, `%s` is replaced with the denylist that Ghostwriter already had.

### 5) Inserting your request text into `inputs`

When you run an act, your request text is inserted into the act’s `inputs`.

- Your request text is whatever comes after the act name.
- If you do not provide request text, Ghostwriter uses its current default prompt.
- Ghostwriter replaces `%s` in `inputs` with that request text.

## How Acts fit into Ghostwriter (key responsibilities)

`ActProcessor` is responsible for:

- parsing the `--act` argument into act name and request text (`setDefaultPrompt(String act)`)
- loading act TOML from the classpath and/or a user-defined location (`tryLoadActFromClasspath`, `tryLoadActFromDirectory`, `loadAct`)
- resolving inheritance via `basedOn` (`loadAct`)
- merging act properties, including `%s` string extension (`setActData`)
- applying act settings into Ghostwriter runtime/configuration (`applyActData`)

## How to use an Act (step-by-step)

1. Pick an act name (for example `task`).
2. Optionally point Ghostwriter to a directory or URL containing custom acts using `--acts <path>`.
3. Add your request text after the act name.
4. Run Ghostwriter.

Example:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -a "task Please rewrite these docs for end users"
```

If you omit the act name (or pass an empty act), Ghostwriter uses `help`.

## Built-in acts (`src/main/resources/acts`)

### assembly

Uses library recommendations (Bindex) to help implement a task.

Use it when you want Ghostwriter to select and apply third-party libraries instead of building everything from scratch (for example: “pick the best logging library”, “integrate an HTTP client”, etc.).

### code-doc

Adds or updates documentation comments in source code.

Use it when you want better in-code documentation (Javadoc/docstrings/XML comments) without changing program behavior.

### commit

Groups local changes and helps commit them using version control commands.

Use it when you want an automated workflow for staging and committing changes with sensible commit messages.

### grype-fix

Fixes dependency vulnerabilities reported by Grype by updating dependencies, rebuilding, and documenting the changes.

Use it when you have Grype results (or can run Syft + Grype) and want a streamlined dependency remediation workflow.

### help

Explains what acts are, lists available acts, and helps interpret act configuration and inheritance.

Use it when you are new to acts or want to understand how a particular act is configured.

### release-notes

Generates release notes from git commit history and writes them into `src/changes/changes.xml` using the Maven Changes schema.

Use it when preparing a release and you want consistent, structured release notes recorded in the standard Maven changes format.

### sonar-fix

Fixes issues reported by SonarQube (typically from a JSON report), with strict rules around if and how `@SuppressWarnings` may be used.

Use it when you have a SonarQube report/API available and want targeted code fixes aligned with SonarQube rules.

### task

A minimal, general-purpose template.

Use it for one-off requests where you want Ghostwriter to apply your instructions to the project.

### unit-tests

Generates and improves unit tests using the project’s build and coverage tooling (for example Maven + JaCoCo).

Use it when you want higher test coverage and new or improved tests added to the project.
