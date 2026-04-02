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

An **Act** is a reusable preset that tells Ghostwriter what to do.

When you run Ghostwriter with an act name, Ghostwriter loads an act definition file (`.toml`) and uses it to:

- provide the AI rules to follow (`instructions`)
- build the task text sent to the AI (the `inputs` template combined with your request)
- optionally set Ghostwriter runtime options (via `gw.*` properties, such as scan scope, concurrency, and interactive mode)

Acts make common workflows repeatable. Instead of rewriting a long request each time, you select an act that already contains the right structure and constraints.

Built-in acts ship with the application under `src/main/resources/acts` and are available at runtime on the classpath as `/acts/<name>.toml`. You can also provide your own acts with `--acts <path>` (directory) or `--acts <url>` (an `http(s)://...` base URL).

## Running an act

The command form is:

```text
--act <name> [your request text]
```

How Ghostwriter interprets it (see `org.machanism.machai.gw.processor.ActProcessor#setDefaultPrompt(String)`):

- If the whole `--act` argument is missing or blank, Ghostwriter uses the `help` act.
- The first word is the act name.
- Everything after the first whitespace is treated as your request text.

## What an act TOML file contains

Acts are TOML files (`*.toml`) and are read using dotted keys. For example, the TOML table:

```toml
[gw]
scanDir = "glob:."
```

is loaded as the key `gw.scanDir`.

Common act keys:

- `description` (optional): short, user-friendly summary
- `instructions`: rules the AI must follow
- `inputs`: the task template; Ghostwriter replaces `%s` with your request text
- `prompt` (optional): a default request text used when you run the act without providing request text
- `basedOn` (optional): inherit settings from another act
- `gw.*`: Ghostwriter options, such as:
  - `gw.scanDir`
  - `gw.threads`
  - `gw.excludes`
  - `gw.nonRecursive`
  - `gw.interactive`
- any other dotted keys are forwarded into Ghostwriter configuration (for example `ft.command.denylist`)

## Interactive vs. non-interactive mode

Acts can run in either **non-interactive** or **interactive** mode:

- **Non-interactive mode**: the act behaves like a predefined command. It can complete the task without asking follow-up questions.
- **Interactive mode**: the act behaves more like a chat. The AI can ask questions to collect missing details. This is useful when you do not yet know all details before starting.

How to activate it:

- In the act TOML file, set `gw.interactive = true` to enable interactive mode.
- Set `gw.interactive = false` (or omit it) for non-interactive mode.

Example:

```toml
gw.interactive = true
```

The built-in `task` act enables interactive mode.

## Using the `prompt` property (default request text)

You normally run an act like this:

```text
--act <name> [your request text]
```

If you do not provide request text after the act name, Ghostwriter uses the processor’s default prompt.

Some acts also define a `prompt` property to provide an act-specific default request text.

How it is applied (see `ActProcessor#setDefaultPrompt(String)`):

- Ghostwriter always formats the final `inputs` by replacing `%s`.
- When `%s` is replaced, Ghostwriter uses:
  - your request text, if you provided it, otherwise
  - the processor’s current default prompt
- If that value is still empty, Ghostwriter falls back to the act’s `prompt` value.

Example:

```toml
prompt = "Describe what you want done and which files to focus on."
inputs = '''
# Task

%s
'''
```

Now running `--act <name>` (with no request text) still produces a meaningful task.

## Inheritance and how “inherited values” work

Acts support inheritance and layering so you can reuse shared settings and override only what you need.

### 1) Inherit from another act with `basedOn`

If an act contains:

```toml
basedOn = "task"
```

Ghostwriter loads the parent act first (recursively), merges its properties, then merges the child act.

Rules:

- When both parent and child define the same key, the child wins.
- After inheritance is resolved, `basedOn` is removed from the final merged properties.

### 2) Layering: custom acts can extend built-in acts

Ghostwriter may load two definitions for the same act name:

- a user-defined act from `--acts <path-or-url>`
- the built-in act from `/acts/<name>.toml`

Both definitions are merged. For string properties, Ghostwriter supports a simple “template extension” mechanism using `%s` (see `ActProcessor#setActData(...)`):

- If the existing value already present in the merged properties contains `%s`, the newly loaded string is inserted where `%s` appears.
- If the existing value does not contain `%s`, the newly loaded value replaces it.

Example (extend a base `inputs` template):

Base:

```toml
inputs = '''
# Task

%s
'''
```

Override that inserts text into the placeholder:

```toml
inputs = "Only update documentation. %s"
```

Resulting merged `inputs`:

```text
# Task

Only update documentation. %s
```

Later, when the act runs, Ghostwriter replaces `%s` with your request text.

### 3) Inheriting from current runtime/CLI configuration

When applying act properties, Ghostwriter also supports inheriting from values already present in the running configuration (for example values set by CLI or system properties). This happens when the act value contains `%s` (see `ActProcessor#applyActData(...)`):

- If a configuration key already has a value and the act’s value contains `%s`, Ghostwriter replaces `%s` with the existing configuration value.
- If there is no `%s`, the act value is applied as-is.

Example (extend an existing functional-tool denylist):

```toml
[ft.command]
denylist = '''
%s
# additional blocks
rm -rf
'''
```

## How acts fit into Ghostwriter

Act mode is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

Key responsibilities:

- parsing `--act` into act name and request text (`setDefaultPrompt(String act)`)
- loading act TOML from the classpath and/or a user-defined location (`tryLoadActFromClasspath`, `tryLoadActFromDirectory`, `loadAct`)
- resolving inheritance via `basedOn` (`loadAct`)
- merging act properties, including `%s`-based string extension when multiple layers are applied (`setActData`, `applyActData`)
- applying act settings to Ghostwriter runtime/configuration (such as `instructions`, `inputs`, `gw.threads`, `gw.excludes`, `gw.nonRecursive`, `gw.interactive`)

## Step-by-step example

1. Choose an act (see “Built-in acts” below).
2. Optionally point Ghostwriter at your custom acts:

   - directory: `--acts path/to/acts`
   - URL base: `--acts https://example.com/acts/`

3. Run the act:

```text
--act task Rewrite the README for new users
```

What happens:

- Ghostwriter loads `task.toml`.
- It builds a final prompt by inserting your request text into the act’s `inputs` where `%s` appears.
- It applies act options (for example `gw.interactive` and scanning options).
- It scans the selected files and runs the act’s `instructions` + final prompt against each match.

## Built-in acts (`src/main/resources/acts`)

### task

A minimal, general-purpose act template.

Use it for one-off requests where you want Ghostwriter to apply your request to the project without extra pre-steps. This act runs in interactive mode (`gw.interactive = true`).

### help

Help and guidance for Ghostwriter’s Act feature.

Use it to list acts, inspect an act’s definition, and understand how act configuration and inheritance work. It scans only the current directory (`gw.scanDir="."`) and disables recursion (`gw.nonRecursive="true"`).

### code-doc

Adds or updates documentation comments inside code (for example Javadoc or docstrings) without changing program behavior.

Use it when you want clearer in-code documentation and improved maintainability.

### commit

Assists with committing local version-control changes.

Use it when you want Ghostwriter to check the working tree, group changes into logical commits, and execute the needed git or svn commands.

### grype-fix

Fixes dependency vulnerabilities reported by Grype.

Use it when you can run Syft + Grype and want Ghostwriter to update vulnerable dependencies, verify the build, and document the changes.

### release-notes

Generates release notes from git history and writes them to `src/changes/changes.xml` in Maven Changes format.

Use it when preparing a release and you want structured release notes recorded in the standard Maven changes file.

### sonar-fix

Fixes issues reported by SonarQube.

Use it when you have a SonarQube report available (for example via the SonarQube Web API) and want targeted fixes aligned with Sonar rules and strict suppression rules.

### unit-tests

Generates and improves unit tests to increase coverage.

Use it when you want Ghostwriter to build the project, measure coverage (JaCoCo), and add/update tests until the coverage target is reached.
