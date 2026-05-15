---
<!-- @guidance: 
Create the Act page as a Project Information page for the project:
- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Create a separate section describing the action's interactive/non-interactive mode.
  - An action can be used as a non-interactive command to perform a predefined task without any additional data.
  - An action can be used interactively (as a chat). This is necessary when the user does not have full information about the desired action before initiating it.
— Create a special section describing how to use the `prompt` property in the toml file to set a default value for the user's prompt. This will be used if the user doesn't provide a prompt.
— Create a dedicated section describing how to use the `episode` feature.
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
- The act name or interactive command: `exit` is reserved for terminating a process.
- Describe the usage of placeholder variables, which should be used in the ${...} format. 
  Placeholders of this type are intended for dynamic substitution by functional tools at runtime. 
  The LLM must not alter, resolve, or modify these placeholders in any way. 
  They are designed to enable retrieval of parameters from the configurator, 
  such as environment variables, system properties, action properties, and similar sources.
- An absolute path to a TOML file can be used as the file name; in this case, hierarchy using  classpath resources on the is not supported.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/ghostwriter/act.html
---

# Act

An Act is a reusable task definition for Ghostwriter. It lets you save instructions, prompt templates, and runtime settings in a TOML file, then start that task by name later.

This feature is implemented mainly by `ActProcessor`. That class reads act files, merges inherited values, applies prompt defaults, supports multi-step episode flows, and then runs Ghostwriter with the resulting configuration.

## What an Act is for

Acts are useful when you want to:

- reuse the same task many times,
- standardize common workflows,
- give Ghostwriter predefined instructions,
- switch between simple one-step tasks and guided multi-step tasks,
- customize behavior without rewriting the full request each time.

In simple terms, an Act works like a named recipe for Ghostwriter.

## Main Act properties

An Act file can define properties such as:

- `description`: a short explanation of the act.
- `instructions`: the main behavior and rules for the AI.
- `inputs`: the prompt template or prompt list used for execution.
- `prompt`: a default prompt value used when the user does not provide one.
- `basedOn`: the parent act used for inheritance.
- `gw.*` settings such as scan directory, recursion, thread count, model, or interactive mode.
- other custom properties that are passed into the configurator.

A very small Act can look like this:

```toml
description = '''
Short explanation of the act.
'''

instructions = '''
You are an assistant that performs a specific task.
'''

inputs = '''
# Task

%s
'''
```

Here, `%s` is the place where Ghostwriter inserts the active prompt text.

## How Act loading works

When an Act starts, `ActProcessor` usually follows this flow:

1. Read the requested act value.
2. Split the act name from any extra prompt text after the first whitespace.
3. Detect optional episode selection after `#`.
4. Load the act from a direct TOML file, a configured act location, and built-in resources.
5. Resolve parent inheritance with `basedOn`.
6. Merge parent and child values.
7. Apply prompt values and runtime settings.
8. Execute the resulting prompts against the matching files.

## Where Acts are loaded from

Ghostwriter can load Acts from:

- built-in resources in `src/main/resources/acts`,
- a configured custom act directory,
- a configured remote HTTP or HTTPS act location,
- a direct TOML file path or URL.

Important details from `ActProcessor`:

- Built-in and custom acts with the same name can both contribute to the final result.
- A custom act can extend or override a built-in act.
- If the requested name ends with `.toml`, it is treated as a direct TOML file reference.
- An absolute path to a TOML file can be used directly as the act name.
- When a direct TOML file is used, normal classpath hierarchy lookup is not used for that file name.

## Interactive and non-interactive mode

Acts support both interactive and non-interactive use.

### Non-interactive mode

In non-interactive mode, an Act behaves like a predefined command. You start it, and Ghostwriter performs the task with the information already available in the act and current configuration.

This is useful when:

- the task is already clear,
- the act contains enough instructions by itself,
- you want repeatable automation,
- you do not need a chat-style back-and-forth session.

Examples of acts mainly designed for predefined processing are `code-doc`, `grype-fix`, `sonar-fix`, and `unit-tests`.

### Interactive mode

In interactive mode, an Act is used more like a chat. This is helpful when the user does not yet know the complete request before starting.

Interactive mode is enabled with:

```toml
gw.interactive = true
```

Built-in examples include:

- `help`, which is designed for asking questions about acts,
- `task`, which is a general interactive task template,
- `commit`, which is also marked interactive because it guides a workflow that can require user-driven context.

### Reserved name

The act name or interactive command `exit` is reserved for terminating a process and should not be used as a custom act name.

## Using the `prompt` property

An Act can define a default prompt with the `prompt` property.

Example:

```toml
prompt = "Perform the default review."
```

This value is used when the user starts the act without supplying extra prompt text.

### How it works in `ActProcessor`

In `setAct(String act)`, Ghostwriter separates the act name from the optional user text.

- If the user provides text after the act name, that text becomes the prompt.
- If the user does not provide text, Ghostwriter falls back to the processor's current default prompt.
- During prompt preparation, `applyPromptValues` and `applayPrompt` insert the prompt into the `inputs` template.
- If the prompt is missing, the act's `prompt` value is used as the default content for `%s`.

Example:

```toml
instructions = '''
You are an expert reviewer.
'''

inputs = '''
# Task

Review this request:
%s
'''

prompt = "Perform the standard review."
```

If the user starts the act without extra text, the final prompt becomes:

```text
# Task

Review this request:
Perform the standard review.
```

This is useful when an act should still do something meaningful even when no extra prompt is supplied.

## Using episodes

Acts can contain more than one prompt step by storing `inputs` as an array instead of a single string.

Example:

```toml
inputs = [
  "Step 1 prompt",
  "Step 2 prompt",
  "Step 3 prompt"
]
```

In this case, Ghostwriter treats the list as episodes.

### How episodes work

When multiple prompt values are present:

- the first prompt becomes the default starting point,
- each prompt is one episode,
- Ghostwriter can run them in normal order,
- a specific episode can be selected,
- an episode can be repeated,
- execution can jump to another episode.

`ActProcessor` manages this using internal state such as:

- `prompts`,
- `episodeIds`,
- `disableNormalOrder`.

It also reacts to `RepeatEpisodeException` and `MoveToEpisodeException`.

### Selecting episodes

Ghostwriter uses `#` after the act name to select episodes.

Example:

```text
my-act#1,3
```

This selects episodes 1 and 3.

If the selection ends with `!`, Ghostwriter does not continue with the normal remaining order.

Example:

```text
my-act#2!
```

This runs episode 2 and then stops.

Episode numbers must be valid. `ActProcessor.setEpisodeIds` checks that all selected numbers are between `1` and the total number of episodes.

### Moving or repeating during execution

During execution, the processor can:

- repeat the current episode,
- move to a specific episode by number,
- move to an episode by name,
- continue normally from the requested point.

Episode names are matched from the first Markdown heading line in the episode prompt.

## How inheritance works

Inheritance is controlled by the `basedOn` property.

Example:

```toml
basedOn = "task"
```

When an act uses `basedOn`, `ActProcessor.loadAct` loads the parent act first and then applies the child values afterward.

### Inheritance order

The effective processing order is:

1. load custom act data if available,
2. load built-in act data if available,
3. read `basedOn`,
4. recursively load the parent act,
5. keep parent values,
6. apply child values to override or extend the parent.

This means the child act inherits the parent settings but can still change them.

### Built-in and custom acts together

If both a built-in act and a custom act exist with the same name:

- the built-in act can provide the base behavior,
- the custom act can extend that behavior,
- the custom act can override parts of it.

This makes customization easier because you do not need to copy the entire built-in file.

### String inheritance using `%s`

Inheritance is not only simple replacement. If a value already exists and both old and new values are strings, `ActProcessor` uses `%s` as a merge point.

Example parent act:

```toml
instructions = '''
Base rules:
%s
'''
```

Example child act:

```toml
basedOn = "parent"

instructions = '''
Add Java-specific rules.
'''
```

Effective result:

```text
Base rules:
Add Java-specific rules.
```

This behavior comes from `setActDataEntry` and `putStringActData`.

### Array inheritance

If a property is a TOML array, `mergeTomlArrayValues` merges parent and child values by position.

That means:

- a child value can replace `%s` in the parent item at the same index,
- extra child items can be appended,
- a single existing string can also be merged into a list.

This is especially useful for episode-based acts.

### Runtime inherited values from the configurator

After act data is loaded, `applyActData` applies it to the processor. Before a string property is applied, `resolveInheritedValue` checks whether the configurator already contains a value for the same key.

If it does, and the act value contains `%s`, Ghostwriter replaces `%s` with the already configured value.

So the final value can be built from multiple layers, including:

1. existing configurator values,
2. built-in act values,
3. custom act values,
4. parent inheritance through `basedOn`,
5. final child overrides.

### Non-string values

While loading TOML data, `ActProcessor` also converts values as needed:

- booleans become strings,
- integers become strings,
- doubles become strings,
- arrays become lists,
- an `inputs` array becomes the episode prompt list.

## Placeholder variables

Acts can use placeholder variables in the `${...}` format.

These placeholders are intended for dynamic substitution by the application or functional tools at runtime. They can represent values such as:

- environment variables,
- system properties,
- action properties,
- configurator values,
- other runtime-provided parameters.

Important rules:

- Placeholders must remain exactly in the `${...}` format.
- They must not be manually resolved or changed in documentation or generated content.
- They are meant for runtime substitution only.
- The LLM must not alter them.

Example:

```toml
some.property = "${MY_ENV_VAR}"
```

The value `${MY_ENV_VAR}` must stay unchanged until runtime processing.

## Step-by-step examples

### Run a built-in act

1. Choose an act name, for example `code-doc`.
2. Optionally add your own extra request text.
3. Start Ghostwriter with that act.
4. Ghostwriter loads the TOML definition.
5. It prepares the final prompt from `instructions` and `inputs`.
6. Matching files are processed.

Conceptual pattern:

```text
--act <name> [your request text]
```

Example:

```text
--act code-doc Add missing documentation to public APIs.
```

### Run an interactive act

Example:

```text
--act help How does act inheritance work?
```

Because `help` is interactive, it is suitable for question-and-answer usage.

### Run selected episodes

Example:

```text
--act my-act#1,2
```

This runs only the selected episode numbers.

### Run an act from a direct TOML file

Example:

```text
--act C:/path/to/custom-act.toml
```

In this case, Ghostwriter loads the TOML file directly. Normal classpath act hierarchy lookup is not used for that file name.

## Built-in acts in `src/main/resources/acts`

### `code-doc`

Purpose: adds or updates documentation comments in source code files.

When to use it: use this act when you want Ghostwriter to improve Javadoc, docstrings, XML comments, or similar developer-facing documentation without changing code behavior.

Main characteristics:

- focuses on documentation comments only,
- tells the AI not to change logic or code structure,
- requires output to contain only the updated file content,
- requires LF line endings,
- explicitly ignores `.machai`.

### `commit`

Purpose: helps review modified files, group changes into logical commit sets, generate commit messages, and support commit execution through functional tools.

When to use it: use this act when you want help turning working-copy changes into structured version control commits.

Main characteristics:

- checks version control status in the current folder,
- groups changes by logical type such as feature, fix, refactor, docs, or chore,
- asks for commit messages based on project history style,
- is interactive,
- defines `ft.command.denylist` as a mergeable property using `%s`.

### `grype-fix`

Purpose: fixes dependency vulnerabilities identified by Grype results.

When to use it: use this act when you want to update vulnerable dependencies in a Maven project and verify the build after remediation.

Main characteristics:

- explains how to generate an SBOM with Syft,
- explains how to obtain Grype results,
- updates only dependencies that have available fixes,
- validates the build after changes,
- is intended for Maven projects and mentions multi-module handling.

### `help`

Purpose: explains the Act feature and helps users inspect available act definitions.

When to use it: use this act when you want to ask questions about acts, list acts, or understand a specific act file.

Main characteristics:

- designed for interactive use,
- references online guidance resources,
- explains how act commands are constructed,
- sets `gw.scanDir = "."`,
- sets `gw.nonRecursive = true`,
- sets `gw.interactive = true`.

### `sonar-fix`

Purpose: reviews SonarQube issues and helps apply focused fixes in Java projects.

When to use it: use this act when you want to remediate code quality, reliability, or security issues reported by SonarQube.

Main characteristics:

- includes strong Java and security-focused remediation rules,
- emphasizes that fixes must compile and pass tests,
- requires high unit test coverage for changes,
- uses runtime placeholders such as `${sonar.host}`, `${sonar.token}`, `${sonar.qualities}`, and `${sonar.severity}`,
- saves changed files to the `UPDATED_FILES_REPORT` project context variable.

### `task`

Purpose: provides a minimal general-purpose act template.

When to use it: use this act when you want a simple interactive act that mainly follows the user's own request.

Main characteristics:

- uses a very small `# Task` input template,
- is interactive,
- acts as a good starting point for custom acts,
- keeps the behavior general instead of task-specific.

### `unit-tests`

Purpose: generates or improves unit tests to raise coverage.

When to use it: use this act when you want Ghostwriter to analyze coverage, improve existing tests, and create new tests where coverage is weak.

Main characteristics:

- tells the workflow to build the project first,
- uses JaCoCo coverage reporting,
- reviews and updates existing tests,
- allows refactoring when strongly required for testability,
- sets `gw.scanDir = "glob:**/test/java"`.

## How Acts fit into Ghostwriter

Acts provide the reusable automation layer of Ghostwriter.

Instead of typing the full task every time, you can save it as a TOML definition and run it again later. `ActProcessor` then loads the file, merges inherited values, prepares the prompt, manages episodes if needed, and applies the resulting settings to file processing.

## Summary

The Act feature makes Ghostwriter easier to use for both new and experienced users.

It provides:

- reusable TOML-based task definitions,
- optional inheritance with `basedOn`,
- default prompt support with `prompt`,
- interactive and non-interactive execution,
- multi-step episode workflows,
- easy customization through built-in, custom, and direct TOML act sources.

Together, these features let users create repeatable workflows that are easier to understand, easier to maintain, and faster to run.
