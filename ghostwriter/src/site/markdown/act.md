---
<!-- @guidance: 
Create the Act page as a Project Information page for the project:
- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Create a separate section describing the action's interactive/non-interactive mode.
  - An action can be used as a non-interactive command to perform a predefined task without any additional data.
  - An action can be used interactively (as a chat). This is necessary when the user does not have full information about the desired action before initiating it.
  - Describe how it is activated and used.
— Create a special section describing how to use the `prompt` property in the toml file to set a default value for the user's prompt. This will be used if the user doesn't provide a prompt.
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

An act is a reusable preset that tells Ghostwriter what to do. You run an act by name, and Ghostwriter loads the act from a TOML file, then uses the act’s settings (instructions, prompt template, and runtime options) to process the files you selected.

Acts exist to make common workflows repeatable. Instead of rewriting a long prompt every time (for example “generate unit tests”, “fix Sonar issues”, or “write release notes”), you choose an act that already contains the right guidance.

Ghostwriter includes built-in acts packaged with the application (`src/main/resources/acts`, available at runtime as classpath resources under `/acts/<name>.toml`). You can also provide your own act files using the `--acts <path>` option (a directory path or an `http(s)://...` URL).

## What an act TOML file contains

An act is a TOML file (`*.toml`). Common fields:

- `description` (optional): short explanation of what the act is for
- `instructions`: the rules and role the AI should follow
- `inputs`: a prompt template; Ghostwriter inserts your request text into this template
- `prompt` (optional): a default user request text (used when you don’t provide your own)
- `basedOn` (optional): inherit from another act
- `gw.*`: Ghostwriter runtime options (for example `gw.scanDir`, `gw.threads`, `gw.nonRecursive`, `gw.interactive`)
- `ft.*`: functional tool options (for example `ft.command.denylist`)

Any other dotted keys are forwarded into Ghostwriter’s configuration.

## How Ghostwriter loads and runs an act

Act mode is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

When you run an act, Ghostwriter:

1. Reads the act argument as: `<name> [request text...]`.
   - If the act argument is missing or blank, it uses the `help` act.
2. Loads the act TOML from:
   - an optional user-defined acts location (`--acts <path>`), either a directory or URL
   - the built-in classpath resources (`/acts/<name>.toml`)
3. If the act contains `basedOn`, loads the parent act first (recursively).
4. Merges all loaded act data into one final set of properties.
5. Builds the final prompt by inserting your request text into `inputs`.
6. Applies the final properties to Ghostwriter’s runtime and configuration.
7. Scans and processes matching files using the composed `instructions` and final prompt.

## Interactive vs. non-interactive mode

Acts can run in either interactive or non-interactive mode.

- Non-interactive: the act runs like a predefined command. It can complete the task without asking you any questions.
- Interactive: the act can ask questions or request missing details while it runs (for example, asking what output format you want before making changes).

How it is activated:

- In the act TOML, set `gw.interactive = true` to enable interactive mode.
- Set `gw.interactive = false` to disable it.

How to choose:

- Pick an interactive act when you don’t have all details yet and want Ghostwriter to guide you.
- Pick a non-interactive act when you want a fully automated run with no back-and-forth.

## Using the `prompt` property (default user request)

Normally, you run an act like this:

```text
--act <name> [your request text]
```

If you do not provide any request text after the act name, Ghostwriter still needs something to place into the `inputs` template.

Ghostwriter resolves the final request text in this order:

1. If you provided request text on the command line, it uses that.
2. Otherwise, it uses the processor’s current default prompt.
3. When the prompt is inserted into the act’s `inputs`, Ghostwriter also considers the act’s `prompt` property as the last-resort fallback.

Practically, this means you can put a “default request” into the act file:

```toml
prompt = "Please describe what you want me to do and what files I should focus on."
inputs = "# Task\n\n%s\n"
```

Now running `--act <name>` without extra text will still produce a useful prompt.

## Inheritance and “inherited values” in acts

Acts support inheritance so you can reuse shared settings and override only what you need. Ghostwriter applies inheritance in three main places.

### 1) Act-to-act inheritance with `basedOn`

If an act includes:

```toml
basedOn = "task"
```

Ghostwriter loads the parent act first, then merges the child act on top. Child values override (or extend) parent values. After resolving inheritance, `basedOn` is removed from the final property set.

Use this when you want a base act that provides shared defaults, and a child act that only changes a few keys.

### 2) Merging values from multiple act sources (custom + built-in)

For the same act name, Ghostwriter can load up to two definitions:

- a custom act from `--acts <path>`
- a built-in act from `/acts/<name>.toml`

These are merged into one property map.

For string properties there is a special “inherit/extend” behavior (`ActProcessor.setActData(...)`):

- If the key already exists and both the existing value and the new value are strings, Ghostwriter replaces the first `%s` in the existing (already-loaded) string with the new string.
- Otherwise, the new value replaces the old value.

This lets you “wrap” or extend a base template instead of replacing it entirely.

Example (extend `inputs`):

Built-in act:

```toml
inputs = "# Task\n\n%s\n"
```

Custom act override:

```toml
inputs = "Only make documentation changes."
```

Final `inputs` becomes:

```text
# Task

Only make documentation changes.
```

### 3) Inheriting from existing runtime configuration when the act is applied

When Ghostwriter applies the final act properties (`ActProcessor.applyActData(...)`), it can also inherit from any existing configuration value (for example, values already set by command-line options).

For string values:

- If a value for the same key already exists in the running configuration and the act value contains `%s`, Ghostwriter replaces `%s` with the existing value.
- If the act value does not contain `%s`, the act value replaces the existing value.

This pattern is useful for settings like multi-line denylists, where an act can add new entries while keeping what was already configured.

Example (extend `ft.command.denylist`):

```toml
[ft.command]
denylist = '''
%s
# additional blocks
rm -rf
'''
```

### 4) Inserting your request text into `inputs`

When you run an act, whatever text you place after the act name becomes the request text. Ghostwriter inserts it into the act’s `inputs` template by replacing `%s`.

## How acts fit into Ghostwriter (key responsibilities)

`ActProcessor` is responsible for:

- parsing the act argument into act name and request text (`setDefaultPrompt(String act)`)
- loading act TOML from the classpath and/or a user-defined location (`tryLoadActFromClasspath`, `tryLoadActFromDirectory`, `loadAct`)
- resolving inheritance via `basedOn` (`loadAct`)
- merging act properties, including `%s`-based string extension (`setActData`)
- applying act settings into Ghostwriter runtime/configuration (`applyActData`)

## How to use an act (step-by-step)

1. Pick an act name.
2. Optionally provide a custom acts directory or URL with `--acts <path>`.
3. Optionally add request text after the act name.
4. Run Ghostwriter with `--act`.

Example:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -a "task Please rewrite these docs for end users"
```

If you omit the act name (or pass an empty act), Ghostwriter uses the `help` act.

## Built-in acts (`src/main/resources/acts`)

### assembly

Purpose: Implements your task using recommended third-party libraries.

Use it when you want Ghostwriter to pick suitable libraries (via Bindex metadata) and build your solution using those libraries instead of writing everything from scratch. This act is interactive (`gw.interactive = true`) and is designed to ask for missing information when needed.

### code-doc

Purpose: Adds or updates documentation comments inside code (for example Javadoc or docstrings).

Use it when you want clearer in-code documentation without changing program behavior.

### commit

Purpose: Helps analyze local changes and perform version control commits.

Use it when you want Ghostwriter to group changes into logical commits, generate commit messages, and run the required VCS commands.

### grype-fix

Purpose: Fixes dependency vulnerabilities reported by Grype.

Use it when you can run Syft + Grype and want Ghostwriter to update vulnerable dependencies, verify the build, and document each change.

### help

Purpose: Explains acts and provides help for listing, understanding, and troubleshooting act configuration.

Use it when you are new to acts, or when you want to understand how a specific act behaves (including inheritance and key properties).

### release-notes

Purpose: Generates release notes from git history and writes them to `src/changes/changes.xml` in Maven Changes format.

Use it when preparing a release and you want structured release notes recorded in the standard Maven changes file.

### sonar-fix

Purpose: Fixes issues reported by SonarQube (from a JSON report or API response), with strict rules for when and how `@SuppressWarnings` may be used.

Use it when you have SonarQube access and want targeted fixes aligned with Sonar rules and auditing requirements.

### task

Purpose: A minimal, general-purpose act template.

Use it for one-off requests where you want Ghostwriter to apply your instructions to the project. This act is interactive (`gw.interactive = true`).

### unit-tests

Purpose: Generates and improves unit tests to increase coverage.

Use it when you want Ghostwriter to build the project, measure coverage (JaCoCo), and add/update tests until the coverage target is reached.
