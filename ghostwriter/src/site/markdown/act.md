---
canonical: https://machai.machanism.org/ghostwriter/act.html
---

<!--
@guidance:
Create the Act page as a Project Information page for the project.

- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Clearly describe how inherited values are processed within the file:
  - Explain the mechanism by which values can be inherited from parent sections, templates, or defaults.
  - Specify how and when these inherited values are applied or overridden in the context of the Act TOML configuration.
  - Provide examples if relevant, to illustrate the inheritance process. 
- Summarize the purpose of the Act feature, its key methods, and how it fits into the overall project.
- Provide easy-to-follow, step-by-step instructions or a practical example showing how to use the Act feature in real scenarios.
- Ensure the content is accessible and helpful for all users, including those new to the codebase or without a technical background.
-->

# Act

Ghostwriter **Act** mode lets you run Ghostwriter using a reusable, named prompt template called an **act**.

An act is a small **TOML** file (`.toml`) that can:

- set the assistant **instructions** (the rules the AI should follow)
- define an **inputs** template that wraps the prompt you type
- optionally set Ghostwriter scan/runtime options (for example exclusions or multi-threading)
- optionally set additional configuration keys that other Ghostwriter features read

This makes common workflows repeatable (for example: creating unit tests, drafting a commit message, generating release notes, or running a consistent “task” template).

## Where act files come from

Ghostwriter can load acts from two places:

1. **Built-in acts** shipped with Ghostwriter
   - in this repository: `src/main/resources/acts/*.toml`
   - at runtime: packaged as classpath resources under `/acts/*.toml`
2. **User-defined acts directory** (optional)
   - configured via `gw.acts = <directory>`

For a given act name, Ghostwriter may load both the user-defined file and the built-in file, then merge them.

## Act TOML format (common keys)

Acts are parsed as TOML and read using **dotted keys** (for example `gw.threads` or `ft.command.denylist`).

Common keys you will see in act files:

- `instructions` — assistant instructions for the run
- `inputs` — a prompt wrapper; your text is inserted using Java `String.format(...)`
- `basedOn` — optional; inherits from another act by name (for example `basedOn = "task"`)

Ghostwriter-specific keys handled by `ActProcessor`:

- `gw.threads` — enable/disable module multi-threading (`true`/`false`)
- `gw.excludes` — comma-separated scan exclusions (for example `"target,.git"`)
- `gw.nonRecursive` — enable/disable module recursion (`true`/`false`)

Other keys:

- Any other dotted string key/value is forwarded into Ghostwriter’s underlying configuration (via the `Configurator`). This is how acts can provide settings for other parts of the system.

## How prompts are built (`inputs` and `%s`)

Most acts define an `inputs` string that contains a `%s` placeholder.

When you run an act, Ghostwriter inserts your prompt text into that placeholder using Java `String.format(...)`.

Example act snippet:

```toml
inputs = '''
# Task

%s
'''
```

If you run:

```text
ghostwriter --act task Fix the failing build
```

Ghostwriter builds a prompt like:

```text
# Task

Fix the failing build
```

If you provide no prompt text after the act name, Ghostwriter uses its normal default prompt.

## Inheritance and “inherited values”

`ActProcessor` supports inheritance in two different (but complementary) ways:

1. **Act-to-act inheritance** using `basedOn`
2. **String template inheritance** using `String.format(...)` when the same key is seen more than once

These mechanisms are what allow small acts to build on shared defaults.

### 1) Inheriting from another act with `basedOn`

You can declare a parent act:

```toml
basedOn = "task"
```

When `basedOn` is set, Ghostwriter:

1. loads the current act from the user directory (if configured) and from built-in resources (if present)
2. reads `basedOn` from the loaded TOML (preferring the user file if it exists)
3. loads the parent act next (recursively, following the chain)
4. removes the `basedOn` key from the final merged properties

Important detail: because the current act file(s) are parsed *before* loading the parent, values from the parent can become the **outer wrapper** during string-template merging (explained next).

### 2) Inheriting/combining string values with templates (`String.format`)

Ghostwriter merges act files by copying dotted string values into a `Properties` map.

If a key already exists and a new string value for the same key is loaded later, Ghostwriter uses:

```java
value = String.format(existingValue, value);
```

That means the **earlier** value is treated like a template (wrapper), and the **later** value is inserted into it.

To make this work, the earlier value must include a `%s` placeholder.

#### Example: a parent wraps a child

Parent act (`task.toml`):

```toml
instructions = "Base rules...\n\n%s"
```

Child act (`my-review.toml`):

```toml
basedOn = "task"
instructions = "Extra rules for this specific run."
```

Resulting `instructions`:

```text
Base rules...

Extra rules for this specific run.
```

If the existing (earlier) string does **not** contain `%s`, then there is nowhere to insert the new value.

### Inheriting from existing runtime configuration (defaults)

After all act files are loaded, Ghostwriter applies the merged properties.

If the runtime configuration already has a value for a key, Ghostwriter can insert that existing value into the act value using:

```java
value = String.format(actValue, existingConfiguratorValue);
```

This lets an act value wrap a project/user default (again, only if the act value contains `%s`).

### Override summary (what “wins”)

- `basedOn` controls which parent act is loaded.
- When the same string key is loaded multiple times, earlier values can wrap later values (if they include `%s`).
- When applying to runtime configuration, act values may wrap existing configurator values (if they include `%s`).
- Some keys are handled specially by `ActProcessor` (`instructions`, `inputs`, and `gw.*`), while everything else is forwarded to the `Configurator`.

## How Act fits into Ghostwriter

Act mode is implemented by `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`.

At a high level it:

1. parses the `--act` argument into an **act name** and optional **prompt text**
2. loads act configuration from a user directory and/or built-in resources
3. follows `basedOn` to load a parent chain
4. merges string values using `String.format(...)`
5. builds the final prompt from `inputs` and your text
6. applies act settings (instructions, prompt wrapper, scan options, and other config keys)

Key methods:

- `setDefaultPrompt(String act)` — entry point for `--act`; parses the input, loads the act, builds the prompt, and applies act settings
- `loadAct(String name, Properties properties, File actDir)` — loads an act and its `basedOn` parent chain
- `tryLoadActFromClasspath(...)` / `tryLoadActFromDirectory(...)` — load `.toml` act files from built-in resources or an external directory
- `setActData(Properties, TomlParseResult)` — copy dotted string keys into `Properties`, merging duplicates via `String.format`
- `applyActData(Properties)` — apply act properties to runtime settings and to the underlying `Configurator`

## Step-by-step: run a built-in act

1. **Pick an act**
   - Built-in acts are in `src/main/resources/acts` (for example `task`, `unit-tests`, `commit`, `release-notes`).
2. **Run Ghostwriter with the act**

   ```text
   ghostwriter --act task Describe what you want Ghostwriter to do
   ```

3. **What happens next**
   - Ghostwriter loads and merges the act TOML.
   - Your text is inserted into the act’s `inputs` template.
   - `instructions` are applied as the assistant rules.
   - Any `gw.*` options (like exclusions or multi-threading) are applied.

## Practical example: create your own act

Goal: create a custom act that extends the built-in `task` act and adds review-focused guidance.

1. Create a folder for your acts (for example `my-acts/`).
2. Create `my-acts/my-review.toml`:

```toml
basedOn = "task"

instructions = '''
When reviewing changes:
- focus on correctness and readability
- propose minimal, safe improvements
'''

inputs = '''
# Review Request

%s
'''

gw.excludes = "target,.git"
gw.threads = "true"
```

3. Point Ghostwriter to your acts folder:

```text
gw.acts = my-acts
```

4. Run it:

```text
ghostwriter --act my-review Review the new Act documentation page
```

Ghostwriter loads `my-review`, follows `basedOn = "task"`, merges the string values (using `%s` templates where present), then runs with the combined instructions and prompt wrapper.
