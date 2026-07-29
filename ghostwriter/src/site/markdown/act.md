---
<!-- @guidance: 
Update this page: "The Act" as a Project Information page for the project:
- Analyze `src/main/java/org/machanism/machai/gw/processor/package-info.java` 
- Analyze `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` and `src/main/java/org/machanism/machai/gw/processor/AIFileProcessor.java` 
  classes and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Create a separate section describing the action's interactive/non-interactive mode.
  - An action can be used as a non-interactive command to perform a predefined task without any additional data.
  - An action can be used interactively (as a chat). This is necessary when the user does not have full information about the desired action before initiating it.
  - Describe how to use value of `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` and `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` to continue or terminate the act processing.
  - Describe how to use value of ``.
— Create a special section describing how to use the `prompt` property in the toml file to set a default value for the user's prompt. This will be used if the user doesn't provide a prompt.
— Create a dedicated section describing how to use the `episode` feature and `input parameters` definition described in `AIFileProcessor` class.
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
title: The Act
---

# The Act

An **Act** is a ready-to-run AI workflow for MachAI Ghostwriter. It is written as a TOML file and tells Ghostwriter what the AI should do, which instructions to use, which project files to inspect, and whether the workflow should run once or continue as a conversation.

In simple terms, an Act is like a reusable task template. Instead of writing the same long prompt every time, you can choose an Act such as `unit-tests`, `code-doc`, or `sonar-fix`, add an optional request, and let Ghostwriter run the configured workflow in the current project.

Acts are processed by `ActProcessor`, which builds on `AIFileProcessor`. The processor loads the Act definition, applies configuration values, prepares prompts, sends project context to the AI provider, and collects results.

## What an Act can do

An Act can:

- define system instructions for the AI assistant;
- define one prompt or several ordered prompts;
- use project layout information, such as source folders, test folders, documentation folders, modules, and the current processed path;
- use function tools, for example file operations, command execution, REST calls, project context variables, and Act inspection tools;
- run in non-interactive mode as a predefined command;
- run in interactive mode as a chat-like workflow;
- inherit values from another Act with `basedOn`;
- override or extend inherited values;
- use default configuration values from a `[default]` section;
- select specific prompt episodes with the `#` syntax;
- load built-in Acts, user-defined Acts, local TOML files, or remote HTTP/HTTPS TOML files.

## Basic TOML structure

A typical Act file contains these values:

```toml
description = '''
A short explanation of what this Act does.
'''

instructions = '''
System-level instructions for the AI assistant.
'''

inputs = '''
# Task

${public.prompt}
'''

[gw]
interactive = true
path = "glob:."
```

Common properties include:

- `description`: explains the purpose of the Act.
- `instructions`: gives the AI its role, rules, and behavior for this Act.
- `inputs`: defines the user-facing task prompt sent to the AI.
- `basedOn`: names another Act to inherit from.
- `gw.model`: selects the AI model/provider.
- `gw.path`: selects files or folders to process.
- `gw.threads`: controls concurrency.
- `gw.excludes`: excludes paths from processing.
- `gw.nonRecursive`: limits folder scanning.
- `gw.interactive`: enables or disables interactive mode.
- `[default]`: provides fallback values when no explicit value is supplied.

## Running an Act

The common command shape is:

```text
--act <act-name> [optional user prompt]
```

Examples:

```text
--act help
--act task Explain this project structure
--act code-doc Add missing Javadoc to service classes
--act unit-tests Improve tests for the parser package
```

If text is provided after the Act name, Ghostwriter uses it as the user prompt. If no text is provided, Ghostwriter uses the configured default prompt, including any default prompt set by the Act.

## Interactive and non-interactive mode

Acts can run in two main modes.

### Non-interactive mode

In non-interactive mode, the Act runs as a command. Ghostwriter sends the configured instructions and prompt to the AI provider, receives the answer, and finishes the task.

Use this mode when the task is already clear. For example:

```text
--act code-doc Add Javadoc to the files in this package
```

This is useful for repeatable automation, such as documentation updates, test generation, dependency review, or vulnerability remediation.

### Interactive mode

In interactive mode, the Act behaves more like a chat. After the AI responds, the user can continue the conversation, provide missing information, ask for corrections, or end the workflow.

Interactive mode is useful when you do not know all details before starting. For example, you may start with:

```text
--act help
```

Then ask follow-up questions about available Acts or configuration.

The special interactive commands are:

- `.` terminates interactive processing successfully. This is the value of `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND`.
- `>` accepts the current provider response and continues processing without sending a new user prompt. This is the value of `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND`.

For example:

```text
.
```

ends the current interactive Act processing.

```text
>
```

continues the workflow without adding another prompt.

Some built-in Acts enable interactive mode by setting `gw.interactive = true`, such as `help`, `task`, and `commit`.

## Default user prompt with the `prompt` property

An Act may define a default value for the user's prompt by setting a prompt-related property in the TOML configuration. The processor stores the user prompt as `public.prompt`.

The common pattern is to use `${public.prompt}` inside `inputs`:

```toml
inputs = '''
# Task

${public.prompt}
'''
```

If the user supplies text after the Act name, that text becomes `public.prompt`. If the user does not supply text, the processor can use a default prompt value from the Act configuration, commonly through the default section:

```toml
[default]
public.prompt = "Describe the current project and suggest next steps."
```

Then this command:

```text
--act task
```

can still produce a meaningful prompt because `${public.prompt}` receives the configured fallback value.

## Placeholder variables

Acts and prompts can use placeholder variables in the `${...}` format. These placeholders are resolved dynamically at runtime by the configurator or function tools.

Examples include:

```text
${public.prompt}
${public.projectName}
${sonar.host}
${sonar.token}
${sonar.qualities}
${sonar.severity}
```

Important rules:

- Placeholders must keep the exact `${...}` format.
- They are intended for runtime substitution.
- They may refer to environment values, system properties, Act properties, public configuration values, or tool-specific settings.
- Do not rewrite, pre-resolve, or alter these placeholders in Act files or documentation examples.

The `public.` prefix is special. Values whose names start with `public.` are exposed to prompt templates and can be safely substituted into prompts.

## Include marker for external prompt content

`AIFileProcessor` supports the include marker:

```text
>>>
```

A prompt or instruction line that starts with this marker can include external content from:

- `http://...`
- `https://...`
- `file://...`

Example:

```text
>>> file://docs/shared-instructions.md
```

The included file is read as UTF-8 and can itself contain more include markers.

## Episodes

An Act can contain one prompt or a list of prompts. When `inputs` is a TOML array, each item is treated as an episode. Episodes allow one Act to run as a multi-step workflow.

Example:

```toml
inputs = [
  '''
  # Episode 1
  Analyze the project and identify risky files.
  ''',
  '''
  # Episode 2
  Update the selected files and run validation.
  '''
]
```

Ghostwriter normally runs episodes in order. You can select episodes by adding `#` after the Act name:

```text
--act refactor#1,2 Run only the first two selected episodes
```

Add `!` to stop after the selected episodes and skip the normal remaining order:

```text
--act refactor#1,2!
```

The `#` character separates the Act name from the episode selection. The `,` character separates episode numbers. The `!` character means "stop after the requested episodes".

During episode execution, the processor also sends Act metadata to the AI. This helps the AI understand the Act name, episode number, and current workflow state.

## Prompt input parameters

Each prompt or episode may start with YAML front matter delimited by `---`. These parameters are read before the prompt is sent to the AI.

Supported input parameters include:

- `gw.model`: overrides the model/provider for this specific request.
- `enabledTools`: limits which function tools are available for this specific prompt. It may be a single value or a YAML list.

Example:

```text
---
gw.model: ${public.ai.model}
enabledTools:
  - get_bindex
  - pick_libraries
---
Analyze the library choices for this project.
```

After the front matter is read, it is removed from the prompt body. The remaining prompt text is then sent to the provider.

## Process information sent to the AI

Every request receives a JSON block named `PROCESS_INFO`. It contains:

- `PROCESSED_FILE_REL_PATH`: the processed file path relative to the project directory.
- `PROCESS_MODE`: either `INTERACTIVE` or `NOT-INTERACTIVE`.

This helps the AI understand what it is working on and whether the current workflow supports follow-up interaction.

## Inheritance and default values

Acts can inherit from another Act using `basedOn`:

```toml
basedOn = "task"
```

When an Act inherits from another Act:

1. The parent Act is loaded first.
2. The child Act is loaded next.
3. Child values override parent values.
4. Some values can extend inherited values instead of replacing them.

The special inherited-value marker is:

```text
$$super.value$$
```

Use it when a child value should include the parent value.

Example:

```toml
basedOn = "task"

instructions = '''
$$super.value$$

Also follow this additional project-specific rule.
'''
```

In this example, the parent `instructions` are kept and the child appends more instructions.

Arrays can also be merged by position. If a parent Act defines multiple input episodes and a child Act defines an array too, the processor can combine matching positions and use `$$super.value$$` to place inherited content inside the child value.

The `[default]` section supplies fallback values. A default value is applied only when the corresponding non-default property is not already set. For example:

```toml
[default]
gw.path = "glob:."
gw.model = "openai:gpt-4.1"
```

If `gw.path` is not otherwise defined, the default value is applied. If a value already exists from the configurator, explicit Act properties, or command configuration, that value can take precedence.

## Loading Acts

Ghostwriter can load Acts from several locations:

- built-in classpath resources under `/acts/`;
- a user-defined Acts directory configured by `gw.actsLocation`;
- an HTTP or HTTPS Acts location;
- a direct TOML file reference.

If an Act exists both as a built-in resource and in the user-defined location, the user-defined Act can override or extend the built-in behavior.

An absolute path to a TOML file can be used as the Act file name. In that case, classpath hierarchy lookup is not used for that file reference.

## Key classes and methods

The Act feature is mainly implemented by these classes:

- `ActProcessor`: loads Act TOML files, applies inheritance, applies defaults, selects episodes, configures processing, and collects results.
- `AIFileProcessor`: prepares prompts, handles interactive mode, resolves include markers, extracts YAML input parameters, substitutes public placeholders, and sends requests to the AI provider.
- `Episodes`: manages ordered episode execution, selected episodes, repeated episodes, and jumps to another episode.
- `GuidanceProcessor`: processes files with `@guidance:` comments. It is separate from Acts, but uses the same AI processing infrastructure.

Important `ActProcessor` methods include:

- `setAct(...)`: selects and loads an Act by name, parses the optional user prompt, applies defaults, and configures episodes.
- `loadAct(...)`: loads a TOML Act and recursively loads its parent if `basedOn` is present.
- `tryLoadActFromClasspath(...)`: loads a built-in Act.
- `tryLoadActFromDirectory(...)`: loads a user-defined, local, absolute, or remote Act.
- `applyActData(...)`: applies loaded TOML values to the processor configuration.
- `getResults()`: returns collected Act outputs.

Important `AIFileProcessor` behavior includes:

- prompt input parameter extraction from YAML front matter;
- prompt placeholder substitution for `public.` values;
- include processing with `>>>`;
- interactive commands `.` and `>`;
- process metadata generation with `PROCESS_INFO`.

## Practical example: create and run a custom Act

1. Create a TOML file in your Acts location, for example `review-docs.toml`.

2. Add a description, instructions, and inputs:

```toml
description = '''
Reviews project documentation and suggests improvements.
'''

instructions = '''
You are a helpful technical writer. Review documentation for clarity, accuracy, and completeness.
'''

inputs = '''
# Task

Review the documentation in this project and suggest improvements.

${public.prompt}
'''

[default]
gw.path = "glob:src/site/**"
public.prompt = "Focus on pages that are unclear for new users."
```

3. Run the Act without extra text:

```text
--act review-docs
```

4. Or run it with a more specific request:

```text
--act review-docs Check whether the Act documentation explains episodes clearly
```

5. If interactive mode is enabled, provide follow-up instructions after the first response. Type `.` to finish or `>` to continue without adding another prompt.

## Built-in Acts

The following Acts are provided in `src/main/resources/acts`.

### `code-doc`

Adds or updates documentation comments in source code. Use this Act when classes, methods, functions, or similar code elements need clearer documentation, such as Javadoc for Java or docstrings for Python.

It focuses on documentation only. It should not change code logic or structure.

### `commit`

Analyzes current version-control changes, groups related modifications, creates commit messages, and can run the appropriate Git or SVN commands through function tools.

Use this Act when you want help preparing and committing local project changes. It runs interactively and is designed to avoid asking for extra confirmation once the task is clear.

### `grype-fix`

Reviews vulnerability results from Grype, identifies affected dependencies, updates them to fixed versions when possible, builds the project, and documents dependency changes.

Use this Act when Syft and Grype are installed and you want to remediate dependency vulnerabilities in a project, especially a Maven project.

### `help`

Provides help for the Ghostwriter Act feature. It can explain available Acts, summarize Act definitions, describe configuration, and guide users through inheritance, inputs, and usage.

Use this Act when you are learning how Acts work or when you need details about a specific Act.

### `sonar-fix`

Fetches and fixes SonarQube issues for the current project component. It focuses on quality, security, maintainability, unit test coverage, and build validation.

Use this Act when a project has SonarQube findings that should be corrected in code. It uses SonarQube-related placeholders such as `${sonar.host}`, `${sonar.token}`, `${sonar.qualities}`, and `${sonar.severity}` at runtime.

### `task`

A minimal general-purpose Act for custom user prompts. It provides a basic project-aware assistant role and sends the user's request through `${public.prompt}`.

Use this Act when no specialized Act fits the task or when you want to ask a one-off project-related question.

### `unit-tests`

Generates or improves unit tests. It builds the project, uses coverage reports such as JaCoCo, identifies under-tested areas, creates or updates tests, and aims for meaningful high coverage.

Use this Act when you want to improve test coverage for source code while following the project's existing test framework and structure.

## Summary

The Act feature turns common AI-assisted development tasks into reusable workflows. It combines TOML configuration, project context, prompts, inheritance, episodes, placeholders, and function tools into one practical mechanism.

For new users, the easiest starting points are:

```text
--act help
--act task Describe this project
--act code-doc Add missing documentation
--act unit-tests Improve unit test coverage
```

Use specialized Acts when the goal is clear, and use interactive Acts when you expect to refine the request through conversation.
