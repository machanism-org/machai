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

Ghostwriter **Act** mode lets you run Ghostwriter with a reusable, named prompt template called an **act**.
An act is defined in a small **TOML** file (`.toml`) and is used to:

- set the AI assistant **instructions** (the “rules” the assistant should follow)
- set an **inputs** template that wraps the prompt text you type
- optionally set Ghostwriter scanning/runtime options (for example module threading or exclusions)
- optionally set other configuration values that Ghostwriter reads at runtime

Acts are designed to make common workflows repeatable (for example: generating unit tests, preparing release notes, drafting a commit message, or applying a consistent review checklist).

## Where act files come from

Ghostwriter can load acts from two places:

1. **Built-in acts** bundled with the application
   - stored in this project at `src/main/resources/acts/*.toml`
   - packaged at runtime as classpath resources under `/acts/*.toml`
2. **User-defined acts directory** (optional)
   - configured by `gw.acts = <directory>`

When you run an act, Ghostwriter tries to load:

- `<directory>/<act>.toml` (if `gw.acts` is set)
- `/acts/<act>.toml` from the built-in resources

Both can be loaded for the same act name, and their values are merged.

## Act TOML format (what keys mean)

Act files are parsed as TOML, and Ghostwriter reads dotted keys from the file. Common keys include:

- `instructions` — the assistant instructions for this run
- `inputs` — a prompt template; your prompt is inserted using Java `String.format(...)`
- `basedOn` — optional; name of another act to inherit from (for example `basedOn = "task"`)

Special Ghostwriter options:

- `gw.threads` — `true`/`false` to enable module multi-threading
- `gw.excludes` — comma-separated scan exclusions (for example `"target,.git"`)
- `gw.nonRecursive` — `true`/`false` to disable module recursion

Other keys:

- Any other string key/value is written into Ghostwriter’s underlying configuration and can be used by other parts of the system.

## How prompts are built (`inputs` and `%s`)

Acts typically define an `inputs` string that contains a `%s` placeholder.
When you run an act, Ghostwriter inserts your prompt text into that placeholder using Java `String.format(...)`.

For example, an act might define:

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

Ghostwriter sends the AI a prompt like:

```text
# Task

Fix the failing build
```

If you don’t type any text after the act name, Ghostwriter uses its normal default prompt instead.

## Inheritance and “inherited values”

Act inheritance in Ghostwriter comes from two mechanisms in `ActProcessor`:

1. **Act-to-act inheritance via `basedOn`**
2. **String value inheritance via `String.format(...)`**, used both while loading and while applying configuration

These combine to let you create small, focused acts that build on a shared base act.

### 1) `basedOn`: inherit from another act

You can declare a parent act:

```toml
basedOn = "task"
```

When `basedOn` is present, Ghostwriter:

1. loads the parent act first (recursively, following its `basedOn` chain)
2. then loads the current act
3. produces one combined set of properties

After the parent chain is loaded, `basedOn` is removed from the final properties.

This logic is implemented in `ActProcessor.loadAct(...)`.

### 2) Inheriting/combining string values with templates (`String.format`)

Ghostwriter also supports a template-style inheritance for **string values**.
This happens in two places:

#### A. While loading act files (merging built-in/custom and `basedOn`)

When Ghostwriter loads a TOML file into an in-memory `Properties` object, it copies only **string** values.
If a key already exists, the *existing* value is treated as a format template and the *new* value is inserted into it:

```java
value = String.format(existingValue, value)
```

Practical example (base wraps child):

```toml
# base act
instructions = "Base rules...\n\n%s"
```

```toml
# child act
instructions = "Extra rules for this specific task."
```

Result:

```text
Base rules...

Extra rules for this specific task.
```

Important note: for this to work, the existing (parent/earlier) value must include a `%s` placeholder.
If it does not, there is nowhere to insert the child value.

#### B. While applying act values to the runtime configuration

After all act files are loaded, Ghostwriter applies each property.
If there is already a configured value for the same key, Ghostwriter can insert that existing value into the act value:

```java
value = String.format(actValue, existingConfiguratorValue)
```

This allows an act value to act as a wrapper around a project or user default.

### Override rules (what “wins”)

Putting it all together:

- `basedOn` loads the base act(s) first, then the child act.
- When the same string key appears multiple times, the earlier value can wrap the later value *if* it includes `%s`.
- When applying to the runtime configuration, an act value may wrap an existing configurator value (again using `%s`).
- Some keys are treated specially (`instructions`, `inputs`, and `gw.*`), everything else is forwarded to the configurator.

## How Act fits into Ghostwriter

Act mode is implemented by `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java`.
At a high level it:

1. parses the `--act` argument into an **act name** and optional **prompt text**
2. loads act configuration from a user directory and/or built-in resources
3. follows `basedOn` to load a parent chain
4. merges string values (including template-style inheritance)
5. builds the final prompt from `inputs` and your text
6. applies act settings to Ghostwriter (instructions, prompt, scanning options, and configuration keys)

Key methods:

- `setDefaultPrompt(String act)` — entry point for `--act`; parses the input, loads the act, formats the prompt, and applies settings
- `loadAct(String name, Properties properties, File actDir)` — loads the act and its `basedOn` chain
- `tryLoadActFromClasspath(...)` / `tryLoadActFromDirectory(...)` — loads `.toml` act files from built-in resources or an external directory
- `setActData(Properties, TomlParseResult)` — copies TOML dotted string values into `Properties`, merging via `String.format`
- `applyActData(Properties)` — applies act properties to runtime settings and to the configurator

## Step-by-step: use a built-in act

1. **Choose an act name**
   - Built-in acts are in `src/main/resources/acts` (for example `task`, `unit-tests`, `release-notes`).
2. **Run Ghostwriter with the act**

   ```text
   ghostwriter --act task Describe what you want Ghostwriter to do
   ```

3. **What Ghostwriter does**
   - loads the act TOML
   - inserts your prompt text into `inputs`
   - sets `instructions`
   - applies any `gw.*` scan options
   - runs Ghostwriter using those defaults

## Practical example: create your own act that extends `task`

1. Create an acts directory (for example `my-acts/`).
2. Create `my-acts/my-review.toml`:

```toml
basedOn = "task"

# Add extra review-specific rules.
instructions = '''
When reviewing changes:
- focus on correctness and readability
- propose minimal, safe improvements
'''

# Use a custom prompt wrapper.
inputs = '''
# Review Request

%s
'''

gw.excludes = "target,.git"
gw.threads = "true"
```

3. Point Ghostwriter to your acts directory:

```text
gw.acts = my-acts
```

4. Run the act:

```text
ghostwriter --act my-review Review the new Act documentation page
```

Ghostwriter loads `task` first, then `my-review`, merges settings, and runs using the resulting instructions and prompt template.
