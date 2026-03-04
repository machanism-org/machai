---
canonical: https://machai.machanism.org/ghostwriter/act.html
---

<!--
@guidance:
Create the Act page as a Project Information page for the project.

- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class.
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

Act is a way to run Ghostwriter with a pre-made “recipe” (an **act**) instead of typing or configuring everything every time. An act is stored as a small **TOML** file (`.toml`) that defines:

- what instructions Ghostwriter should follow,
- what prompt text should be sent,
- and a few Ghostwriter options (like threading and scan exclusions).

This makes repeated tasks—like “write documentation for this folder”, “summarize changes”, or “review code for style issues”—easy and consistent.

## What an act file contains

Act files are TOML documents that can contain the following common keys:

- `instructions`
  - The system/provider instructions to use for the run.
- `inputs`
  - A prompt template. The user-provided prompt is inserted into this template using Java’s `String.format(...)`.
- `gw.threads`
  - Enables/disables module multi-threading.
- `gw.excludes`
  - A comma-separated list of scan exclusions.
- `gw.nonRecursive`
  - Disables module recursion.
- Any other key
  - Forwarded into Ghostwriter’s configuration (via the configurator), so acts can set normal configuration values too.

Acts can be loaded from:

- **Built-in** acts bundled with the application (classpath resources under `/acts/<name>.toml`), and/or
- **Custom** acts from a user directory configured as `gw.acts`.

## How Act chooses the prompt you run

When you run an act, you typically provide:

- the act name, and optionally
- additional prompt text.

The `--act` argument supports this shape:

- `--act <name> [prompt]`

If you include extra text after the act name, that text becomes the **prompt** inserted into the act’s template. If you do not include extra text, Ghostwriter uses its normal default prompt (`getDefaultPrompt()`).

Inside the processor:

1. It splits the first “word” as the act name.
2. Everything after the first whitespace is treated as the optional prompt text.
3. It loads the act’s TOML into a `Properties` map.
4. It builds the final prompt using `String.format(...)`.

## Inheritance: how values are inherited and overridden

Act supports two kinds of inheritance/overlay behavior:

1. **Act-to-act inheritance (`basedOn`)**: start from another act, then override.
2. **String-template inheritance for repeated keys**: combine parent and child values using `String.format(...)`.

These work together to let you build small specialized acts on top of general ones.

### 1) Act-to-act inheritance using `basedOn`

An act file may include:

- `basedOn = "<otherActName>"`

When present, Ghostwriter loads the base act first, then loads the current act.

Important details from `ActProcessor.loadAct(...)`:

- Ghostwriter tries to load the act from the **custom directory** first, and also tries the **bundled classpath** copy.
- It reads `basedOn` from the custom TOML if available; otherwise it reads it from the classpath TOML.
- If `basedOn` is set, it recursively loads the base act and merges settings into the same `Properties` object.
- After the inheritance chain is processed, `basedOn` is removed so it doesn’t get applied as a normal configuration key.

In plain terms: **the base act provides defaults**, and **the child act can override them**.

### 2) Inheriting/combining string values with `String.format`

When Ghostwriter loads TOML keys into the `Properties` map, it merges string values like this:

- If a key does not exist yet: it is set directly.
- If a key already exists (inherited from a base act loaded earlier):
  - the existing value is treated like a *format string*,
  - and the new value is inserted into it using `String.format(inheritValue, value)`.

This allows the parent act to define a “wrapper” and the child act to provide the inserted content.

Example:

**base.toml**

```toml
instructions = "You are a helpful assistant. Follow these rules: %s"
inputs = "Task: %s"
```

**child.toml**

```toml
basedOn = "base"
instructions = "Always respond in Markdown."
inputs = "Summarize the following: %s"
```

Resulting merged values:

- `instructions` becomes:
  - `"You are a helpful assistant. Follow these rules: Always respond in Markdown."`
- `inputs` becomes:
  - `"Task: Summarize the following: %s"`

Then, at run time, the user’s prompt text is inserted into the final `inputs` template.

### When inherited values are applied (and how overriding works)

- Inheritance is applied **during act loading**, before Ghostwriter starts processing files.
- The `basedOn` chain is loaded **first**, so base settings are in place before child settings are read.
- For string keys, if both base and child define the same key, the base string is used as the format/wrapper and the child value is inserted.
- For non-string values, the loader currently only copies keys whose TOML value is a string.

## What Act actually does during execution

After loading and merging the act values, `ActProcessor.applyActData(...)` applies each property:

- `instructions` → sets the provider/system instructions used for the run.
- `inputs` → formats the template with the current `prompt` value (if present) and sets the final default prompt.
- `gw.threads` → toggles module multi-threading.
- `gw.excludes` → sets scan exclusions (split on commas).
- `gw.nonRecursive` → disables recursion.
- Any other key → stored in the main configurator as a normal configuration value.

Finally, Ghostwriter processes files according to the project layout. In Act mode it can process:

- files under the project directory (excluding module directories and non-matching paths), and
- the project directory itself, if it matches the scan rules and there is a prompt to run.

## Key methods (mapped to what you care about)

- `setDefaultPrompt(String act)`
  - Entry point for Act mode: parses the `--act` value, loads the act, builds the final prompt, and applies act configuration.
- `loadAct(String name, Properties properties)`
  - Loads a TOML act (custom and/or bundled), handles `basedOn` inheritance, and accumulates merged values.
- `setActData(Properties properties, TomlParseResult toml)`
  - Copies TOML string keys into the properties map and performs the string “inherit/format” merge when a key already exists.
- `applyActData(Properties properties)`
  - Applies special Ghostwriter keys (`instructions`, `inputs`, `gw.*`) and forwards all other keys to the configurator.

## Practical, step-by-step usage

### 1) Create an act file

Create a file named `review.toml` in your acts directory (the directory configured as `gw.acts`):

```toml
instructions = "You are a careful reviewer. Output actionable feedback."
inputs = "Please review the following and suggest improvements:\n\n%s"

gw.excludes = "target,.git"
```

### 2) Run Ghostwriter with the act

Run with just the act name (uses Ghostwriter’s default prompt as the inserted text):

- `--act review`

Or include prompt text after the act name (this is what gets inserted into `inputs`):

- `--act review Review src/main/java for correctness and readability.`

### 3) Build a specialized act using `basedOn`

Create a second act that builds on the first:

```toml
basedOn = "review"
instructions = "Focus on security risks and input validation."
```

Now run:

- `--act securityReview Check the API endpoints for potential injection issues.`

Because `instructions` is inherited via formatting, you can create a base “wrapper” act that enforces global rules, and child acts that supply a focused add-on.

## How Act fits into Ghostwriter

Ghostwriter can operate in different processing modes. Act mode is the “preset-driven” mode:

- It packages instructions, prompt templates, and runtime configuration into reusable TOML files.
- It supports building a library of acts (built-in and user-defined).
- It supports inheritance so teams can standardize baseline instructions and customize for specific tasks.

The overall purpose is to make Ghostwriter runs **repeatable**, **shareable**, and **easy to use**—even if you don’t know the internal configuration details.
