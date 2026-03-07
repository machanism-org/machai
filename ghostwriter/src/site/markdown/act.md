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

Ghostwriter “Act” mode lets you run Ghostwriter using a reusable, named prompt template called an **act**.
An act is stored in a small **TOML** file (`.toml`) and can:

- set the AI provider instructions (the “system” style rules)
- provide an input template that wraps your own prompt text
- optionally set some Ghostwriter options (threads, excludes, recursion)
- pass additional configuration keys through to Ghostwriter’s configurator

Acts are intended to make common workflows repeatable (for example: writing release notes, generating unit tests, preparing a commit message, etc.).

## Where act files come from

Ghostwriter can load acts from two places:

1. **Built-in acts** bundled with the application, located on the classpath under:
   - `src/main/resources/acts/*.toml` (packaged at runtime as `/acts/*.toml`)
2. **Your own acts directory** (optional), configured by:
   - `gw.acts = <directory>`

If an act exists in both places, the processor loads both; the result is effectively “merged” (see inheritance and overrides below).

## Act TOML format

Act files are parsed as TOML. Ghostwriter looks for these keys:

- `instructions` — the provider/assistant instructions used for the run
- `inputs` — a template used to build the final prompt
- `basedOn` — optional: name of another act to inherit from (for example `basedOn = "task"`)
- `gw.threads` — `true`/`false` to enable module multi-threading
- `gw.excludes` — comma-separated scan exclusions
- `gw.nonRecursive` — `true`/`false` to disable module recursion
- any other keys — forwarded into the underlying configuration (via the project configurator)

Most bundled acts follow a common pattern:

- `instructions` contains the long, “how to behave” rules.
- `inputs` contains the “Task” template and usually includes a `%s` placeholder.

## How prompts are built (the `%s` placeholder)

When you run an act, you normally provide:

- an **act name**, and
- optional **prompt text** after the first whitespace.

Ghostwriter then produces a final prompt by inserting your text into a template.
Internally this uses Java’s `String.format(...)`.

Example idea:

- you run: `--act task Fix the failing build`
- act file has:
  - `inputs = '''\n# Task\n\n%s\n'''`
- the final prompt becomes:

```text
# Task

Fix the failing build
```

If you do not provide any prompt text after the act name, Ghostwriter falls back to its normal default prompt.

## Inheritance and “inherited values” processing

Acts support inheritance in two related ways:

1. **Act-to-act inheritance via `basedOn`**
2. **Value inheritance/combination via string templating (`String.format`)**

These two mechanisms are what make it possible to have a “base” act (for example, `task`) and then create specialized acts that extend it.

### 1) `basedOn`: inherit from another act

An act can declare:

```toml
basedOn = "task"
```

When this is present, Ghostwriter:

1. loads the `task` act first
2. then loads the current act
3. the child act’s keys override the parent act’s keys when they are the same key

This is implemented in `ActProcessor.loadAct(...)` as a recursive load.

Important details:

- `basedOn` is read from the loaded TOML (custom directory first; built-in second).
- Once the base act has been loaded, `basedOn` is removed from the final property set.

### 2) Inheriting/combining values using templates (string formatting)

Ghostwriter also supports “inheritance” where one value is used as a template that wraps another value.
This happens in two phases.

#### Phase A: combining act values across loads

While loading an act into an in-memory `Properties` map, Ghostwriter uses this rule for **string** values:

- if a key already exists, the older value is treated as a **format template**
- the newly loaded value becomes the **argument**

In code (simplified):

```java
if (existingValue != null) {
  value = String.format(existingValue, value);
}
```

What this means in practice:

- A base act can define something like:
  - `instructions = "<base instructions>\n\n%s"`
- A child act can define:
  - `instructions = "<extra rules>"`
- The resulting instructions become:
  - `<base instructions> <extra rules>`

For this to work, the “parent” value must contain a `%s` placeholder.
If it does not, the child value is effectively ignored when combining (because formatting has nowhere to insert it).

#### Phase B: combining act values with existing runtime configuration

After loading all act properties, Ghostwriter applies them.
During this step, for **string** values it may combine the act value with an already-configured value from the configurator:

- if there is an existing configurator value for the same key, it is inserted into the act value using `String.format(actValue, existingValue)`

This is mainly useful when an act’s value is designed to wrap or incorporate a configurable default.

### Override rules (when values win)

Putting it together, the effective behavior is:

- `basedOn` loads the base act first
- then the child act is loaded
- for each key:
  - if the key is new, it is added
  - if the key already exists and is a string, the old value can wrap the new one (if it uses `%s`)
- finally, when applying:
  - special keys are handled directly (`instructions`, `inputs`, and `gw.*` keys)
  - other keys are written into the configurator

## How Act fits into Ghostwriter

Act mode is implemented by `ActProcessor`.
At a high level it:

1. parses the `--act` argument into an act name and optional prompt
2. loads and merges act data (including `basedOn` inheritance)
3. builds the final prompt (using `inputs` and your provided text)
4. applies act settings (instructions, scanning options, and other configuration)
5. runs Ghostwriter processing using those defaults

Key methods (from `ActProcessor`):

- `setDefaultPrompt(String act)` — entry point for “use this act”; parses the act argument, loads the act, formats the prompt, and applies configuration
- `loadAct(String name, Properties properties)` — loads an act and its `basedOn` chain
- `tryLoadActFromClasspath(...)` / `tryLoadActFromDirectory(...)` — load `.toml` act definitions from built-in resources or a configured external directory
- `setActData(Properties, TomlParseResult)` — merges TOML string values into the in-memory properties (including template-style inheritance)
- `applyActData(Properties)` — applies properties to Ghostwriter runtime settings and configurator

## Practical step-by-step: run a built-in act

### 1) Pick an act

Built-in examples are in `src/main/resources/acts` (for example: `task`, `unit-tests`, `release-notes`, `sonar-fix`).

### 2) Run Ghostwriter with the act name and your prompt text

Conceptually (exact CLI flags may vary by your environment):

```text
ghostwriter --act task "Describe what you want Ghostwriter to do"
```

### 3) What happens

- Ghostwriter loads `task.toml`
- it takes your provided text and inserts it into `inputs`
- it sets `instructions`
- it runs the processor over the project using the act’s scanning configuration

## Practical example: make your own act that extends another

Create a new file like `my-acts/my-review.toml`:

```toml
basedOn = "task"

# Base act provides general behavior; we add extra, specific behavior.
# This works best if the base instructions include a "%s" placeholder.
instructions = '''
When reviewing changes:
- focus on correctness and readability
- suggest minimal, safe improvements
'''

# Wrap the user prompt in a standard structure.
inputs = '''
# Review Request

%s
'''

[gw]
excludes = "target,.git"
threads = "true"
```

Then point Ghostwriter to your acts directory (for example via configuration):

```text
gw.acts = my-acts
```

And run:

```text
ghostwriter --act my-review "Review the new Act documentation page"
```

If `basedOn` is used, Ghostwriter loads `task` first and then layers `my-review` on top, applying the inheritance rules described above.
