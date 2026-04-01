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

An act is a reusable preset that tells Ghostwriter what to do.

When you run Ghostwriter with an act name, Ghostwriter loads a TOML file (the act definition) and uses it to:

- decide what instructions the AI should follow
- build the final prompt sent to the AI
- apply optional runtime settings (for example scan location, threading, and interactive mode)

Acts exist to make common workflows repeatable. Instead of rewriting a long prompt every time (for example “generate unit tests”, “fix Sonar issues”, or “write release notes”), you choose an act that already contains the right guidance.

Ghostwriter ships with built-in acts in `src/main/resources/acts` (available at runtime from the classpath under `/acts/<name>.toml`). You can also provide your own acts using `--acts <path>` (a directory path) or `--acts <url>` (an `http(s)://...` location).

## What an act TOML file contains

An act is a TOML file (`*.toml`). Common fields:

- `description` (optional): a short, user-friendly description of what the act does
- `instructions`: the “rules” and role the AI should follow
- `inputs`: a template used to build the final prompt; Ghostwriter replaces `%s` with your request text
- `prompt` (optional): a default request text used when you do not provide any request text
- `basedOn` (optional): inherit settings from another act
- `gw.*`: Ghostwriter runtime options (for example `gw.scanDir`, `gw.threads`, `gw.nonRecursive`, `gw.interactive`)
- `ft.*`: functional tool options (for example `ft.command.denylist`)

All other dotted keys are forwarded into Ghostwriter’s configuration.

## How Ghostwriter loads and runs an act

Act mode is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

When you run an act, Ghostwriter:

1. Parses the `--act` argument as: `<name> [request text...]`.
   - If the argument is missing or blank, Ghostwriter uses the `help` act.
2. Loads the act definition from:
   - an optional user-defined location (`--acts <path-or-url>`)
   - the built-in classpath resources (`/acts/<name>.toml`)
3. Resolves inheritance:
   - If the act declares `basedOn`, Ghostwriter loads the parent act first (recursively), then applies the child act.
4. Merges all loaded data into one final set of properties.
5. Builds the final prompt by inserting your request text into `inputs`.
6. Applies the final properties to Ghostwriter runtime/configuration.
7. Scans and processes the matching files using the composed `instructions` and final prompt.

## Interactive vs. non-interactive mode

Acts can run in either interactive or non-interactive mode.

- Non-interactive: the act runs like a predefined command. It can complete the task without asking you any questions.
- Interactive: the act can ask questions or request missing details while it runs. This is useful when you do not yet know all details of what you want (for example, you want Ghostwriter to ask which output format you prefer).

How it is activated:

- In the act TOML file, set `gw.interactive = true` to enable interactive mode.
- Set `gw.interactive = false` (or omit it) to run non-interactively.

Practical guidance:

- Use interactive mode when you want a “chat-like” workflow and guidance.
- Use non-interactive mode when you want a fully automated run.

## Using the `prompt` property (default user request)

You usually run an act like this:

```text
--act <name> [your request text]
```

If you do not provide any request text after the act name, Ghostwriter needs a fallback.

How the request text is chosen:

1. If you provided request text on the command line, Ghostwriter uses it.
2. Otherwise, Ghostwriter uses the processor’s current default prompt.
3. When Ghostwriter formats the act’s `inputs`, it uses the act’s own `prompt` as the default request text if there is still no request text.

Example:

```toml
prompt = "Please describe what you want me to do and what files I should focus on."
inputs = "# Task\n\n%s\n"
```

Now running `--act <name>` without extra text still produces a useful final prompt.

## Inheritance and “inherited values” in acts

Acts support inheritance so you can reuse shared settings and override only what you need.

Ghostwriter supports inheritance in two ways:

1. Act-to-act inheritance using `basedOn`.
2. String “inherit/extend” using `%s` when multiple definitions or layers are applied.

### 1) Act-to-act inheritance with `basedOn`

If an act contains:

```toml
basedOn = "task"
```

Ghostwriter loads the parent act first and merges it into the properties map, then merges the child act.

- If both parent and child define the same key, the child takes precedence.
- After inheritance is resolved, `basedOn` is removed from the final property set.

Use `basedOn` to create a base act with shared defaults, then create small child acts that only override a few keys.

### 2) Extending values with `%s` (wrapping behavior)

Ghostwriter uses `%s` as a placeholder to support “inherit/extend” behavior for string values.

This can happen in three places.

#### 2.1) Extending a built-in act with a custom act of the same name

Ghostwriter can load up to two definitions for the same act name:

- a custom act from `--acts <path-or-url>`
- a built-in act from `/acts/<name>.toml`

Both are merged. For a string key that already exists, `ActProcessor.setActData(...)` replaces the first `%s` in the existing (already loaded) value with the new value.

Example (custom act extends built-in `inputs`):

Built-in `inputs`:

```toml
inputs = "# Task\n\n%s\n"
```

Custom override `inputs`:

```toml
inputs = "Only make documentation changes."
```

Resulting `inputs`:

```text
# Task

Only make documentation changes.
```

If there is no `%s` in the existing value, the new value replaces it.

#### 2.2) Extending values already set by your current configuration (for example CLI options)

When Ghostwriter applies the final act properties (`ActProcessor.applyActData(...)`), it checks whether a value already exists in the current configuration.

- If a configuration value already exists and the act value contains `%s`, Ghostwriter replaces `%s` with the existing configuration value.
- If there is no `%s`, the act value replaces the existing configuration value.

Example (extend an existing command denylist):

```toml
[ft.command]
denylist = '''
%s
# additional blocks
rm -rf
'''
```

#### 2.3) Inserting your request text into `inputs`

Whatever text you place after the act name becomes the request text. Ghostwriter inserts that into the act’s `inputs` template by replacing `%s`.

Example:

```toml
inputs = "# Task\n\n%s\n"
```

Running:

```text
--act task Please rewrite these docs for end users.
```

produces a final prompt where `%s` is replaced by `Please rewrite these docs for end users.`.

## How acts fit into Ghostwriter (key responsibilities)

`ActProcessor` is responsible for:

- parsing the `--act` argument into act name and request text (`setDefaultPrompt(String act)`)
- loading act TOML from the classpath and/or a user-defined location (`tryLoadActFromClasspath`, `tryLoadActFromDirectory`, `loadAct`)
- resolving inheritance via `basedOn` (`loadAct`)
- merging act properties, including `%s`-based extension when definitions are layered (`setActData`)
- applying act settings to Ghostwriter runtime/configuration (`applyActData`)

## How to use an act (step-by-step)

1. Choose an act name (see the list of built-in acts below).
2. Decide whether you want to provide a custom acts location:
   - directory: `--acts path/to/acts`
   - URL: `--acts https://example.com/acts/`
3. Decide whether you want to add request text.
4. Run Ghostwriter with `--act`.

Example:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -a "task Please rewrite these docs for end users"
```

If you omit the act name (or pass a blank act argument), Ghostwriter uses the `help` act.

## Built-in acts (`src/main/resources/acts`)

### task

Minimal, general-purpose act template.

Use it for one-off requests where you want Ghostwriter to follow the act instructions and apply your request to the project.

This act enables interactive mode (`gw.interactive = true`).

### code-doc

Adds or updates documentation comments inside code (for example Javadoc or docstrings) without changing program behavior.

Use it when you want clearer in-code documentation and improved maintainability.

### commit

Helps analyze local version-control changes and commit them.

Use it when you want Ghostwriter to group changes into logical commits, generate commit messages, and (when permitted by the environment) run the version-control commands.

### grype-fix

Fixes dependency vulnerabilities reported by Grype.

Use it when you can run Syft + Grype and want Ghostwriter to update vulnerable dependencies, verify the build, and document each change.

### help

Provides help for the Act feature.

Use it when you want to list/understand acts, learn how act TOML files work, or troubleshoot act configuration and inheritance.

### release-notes

Generates release notes from git history and writes them to `src/changes/changes.xml` in Maven Changes format.

Use it when preparing a release and you want structured release notes recorded in the standard Maven changes file.

### sonar-fix

Fixes issues reported by SonarQube.

Use it when you have access to a SonarQube report (for example via an API or JSON output) and want targeted fixes aligned with Sonar rules and strict suppression rules.

### unit-tests

Generates and improves unit tests to increase coverage.

Use it when you want Ghostwriter to run the build, measure coverage (JaCoCo), and add/update tests until the coverage target is reached.
