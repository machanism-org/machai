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

An **Act** is a reusable AI workflow for MachAI Ghostwriter. It is defined in a TOML file and describes what the AI assistant should do, which instructions it should follow, what prompt it should receive, which project files it should inspect, and whether the workflow should run once or continue as a conversation.

In everyday language, an Act is a saved task. Instead of writing a long prompt every time, you choose an Act such as `task`, `code-doc`, `unit-tests`, or `sonar-fix`, optionally add your request, and let Ghostwriter run the configured workflow in the current project.

The Act feature is implemented mainly by `ActProcessor`, using the shared AI processing behavior from `AIFileProcessor`. Together they load the Act, apply configuration, prepare prompts, add project context, make function tools available, send requests to the configured AI provider, and collect the result.

## Main functionality

Acts can:

- provide reusable instructions for common development work;
- define a single prompt or a multi-step list of prompts;
- process the current project, a folder, a file, or files selected by `glob:` or `regex:` patterns;
- use project information such as source folders, test folders, documentation folders, modules, and the processed file path;
- expose selected configuration values to prompt templates;
- use function tools for file access, command execution, REST calls, project context variables, and Act inspection;
- run once as a command or run interactively as a chat;
- inherit from another Act with `basedOn`;
- use default values from the `[default]` TOML section;
- select individual episodes from a multi-step workflow.

## Basic TOML structure

A simple Act can look like this:

```toml
description = '''
Explains what this Act does.
'''

instructions = '''
You are a helpful assistant working inside this project.
'''

inputs = '''
# Task

${public.prompt}
'''

[gw]
interactive = true
path = "glob:."
```

Common properties are:

- `description`: a readable explanation of the Act.
- `instructions`: system-level instructions for the AI assistant.
- `inputs`: the prompt template sent to the AI.
- `basedOn`: another Act to inherit from.
- `gw.model`: the AI provider/model to use.
- `gw.path`: the file or folder selection, for example `glob:.`.
- `gw.threads`: the number of concurrent processing threads.
- `gw.excludes`: comma-separated paths or patterns to exclude.
- `gw.nonRecursive`: whether folder scanning should avoid child folders.
- `gw.interactive`: whether the Act should behave like a chat.
- `[default]`: fallback values used when no explicit value is already set.

## Running an Act

The common command form is:

```text
--act <act-name> [optional request text]
```

Examples:

```text
--act help
--act task Explain this project structure
--act code-doc Add missing documentation comments
--act unit-tests Improve tests for parser classes
```

If text appears after the Act name, Ghostwriter stores it as `public.prompt`. If no text is provided, Ghostwriter uses the processor's current default prompt or a default prompt value configured by the Act.

A command that begins with `>` is treated as shorthand for the built-in `task` Act:

```text
--act > summarize the project
```

This is processed like a general `task` request.

## Interactive and non-interactive mode

Acts can run in two modes.

### Non-interactive mode

In non-interactive mode, the Act runs once. Ghostwriter prepares the configured prompt, sends it to the AI provider, receives the response, and finishes.

Use this mode when the task is already clear and does not need follow-up questions. Examples include:

```text
--act code-doc Add Javadoc to this package
--act grype-fix Fix dependency vulnerabilities reported by Grype
```

### Interactive mode

In interactive mode, the Act works like a chat. After the AI responds, the user can send another message, ask for changes, provide missing details, or end the workflow.

This is useful when the request is incomplete at the beginning. For example:

```text
--act help
```

can start a conversation about available Acts and how to use them.

Special interactive commands are:

- `.` ends interactive processing successfully. This is the value of `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND`.
- `>` accepts the current AI response and continues without sending a new user prompt. This is the value of `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND`.

For example, enter:

```text
.
```

to finish the current interactive Act, or enter:

```text
>
```

to continue processing without adding another prompt.

Built-in Acts such as `help`, `task`, and `commit` enable interactive mode.

## Default user prompt with the `prompt` property

Ghostwriter stores the user's request in the public property `public.prompt`. Most Acts place this value inside the prompt template:

```toml
inputs = '''
# Task

${public.prompt}
'''
```

If the user provides request text after the Act name, that text becomes `public.prompt`.

An Act can also define a fallback prompt. Put the fallback value in the `[default]` section:

```toml
[default]
public.prompt = "Describe the current project and suggest useful next steps."
```

Then this command:

```text
--act task
```

can still produce a useful request because `${public.prompt}` receives the configured default value when the user does not provide one.

## Placeholder variables

Act files and prompts can use placeholder variables in the `${...}` format. These placeholders are resolved dynamically at runtime from configuration, system properties, environment values, Act properties, public values, or tool-specific settings.

Examples:

```text
${public.prompt}
${public.projectName}
${sonar.host}
${sonar.token}
${sonar.qualities}
${sonar.severity}
```

Important rules:

- Keep placeholders exactly in the `${...}` format.
- Do not rewrite, resolve, or modify placeholders in Act files or documentation examples.
- Placeholders are intended for runtime substitution by Ghostwriter, the configurator, or function tools.
- Only properties beginning with `public.` are automatically exposed for prompt template substitution by `AIFileProcessor`.

## Including external prompt content

`AIFileProcessor` supports the include marker:

```text
>>>
```

A prompt or instruction line beginning with this marker can include external content from:

- `http://...`
- `https://...`
- `file://...`

Example:

```text
>>> file://docs/shared-instructions.md
```

Included content is read as UTF-8. Included content may also contain more include markers.

## Episodes

An Act can define one prompt or several prompts. When `inputs` is a TOML array, each item is an episode. Episodes are useful for multi-step workflows, such as first analyzing the project, then updating files, then validating the result.

Example:

```toml
inputs = [
  '''
  # Analyze
  Review the project and identify risky files.
  ''',
  '''
  # Update
  Update the selected files and validate the changes.
  '''
]
```

By default, Ghostwriter runs episodes in order. You can select episodes by adding `#` after the Act name:

```text
--act review#1,2 Check error handling
```

Use `!` to stop after the selected episodes and skip the remaining normal order:

```text
--act review#1,2!
```

Episode syntax summary:

- `#` separates the Act name from the episode selection.
- `,` separates multiple episode numbers.
- `!` means stop after the selected episodes.

During episode execution, Ghostwriter also sends Act metadata to the AI so it can understand the Act name, episode number, and current workflow state.

## Prompt input parameters

Each prompt or episode may start with YAML front matter delimited by `---`. These input parameters are read before the prompt is sent to the AI, then removed from the prompt body.

Supported parameters include:

- `gw.model`: overrides the AI provider/model for the current prompt.
- `enabledTools`: restricts available function tools for the current prompt. It may be a single value or a YAML list.

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

This lets one episode use a different model or a smaller tool set without changing the whole Act.

## Process information sent to the AI

Every request contains:

- `PROCESSED_FILE_REL_PATH`: the processed file path relative to the project directory.
- `PROCESS_MODE`: either `INTERACTIVE` or `NOT-INTERACTIVE`.

This helps the AI understand what it is processing and whether a follow-up conversation is available.

## Inheritance and default values

Acts can inherit from another Act with `basedOn`:

```toml
basedOn = "task"
```

The processing order is:

1. Ghostwriter loads the parent Act first.
2. Ghostwriter loads the child Act next.
3. Child values override or extend parent values.
4. Default values are applied after the Act data is loaded.

The inherited-value marker is:

```text
${super.value}
```

Use it when a child value should include a parent or previously configured value.

Example:

```toml
basedOn = "task"

instructions = '''
${super.value}

Also follow this extra project rule.
'''
```

In this example, the child Act keeps the inherited instructions and appends an additional rule.

Array values, such as an `inputs` list, can be merged by position. If the parent has multiple episodes and the child also defines an array, matching positions can use `${super.value}` to include inherited episode text.

The `[default]` section provides fallback values. A value from `[default]` is applied only when the corresponding normal property is not already set. For example:

```toml
[default]
gw.path = "glob:."
gw.model = "OpenAI:gpt-4.1"
public.prompt = "Summarize the current project."
```

If `gw.path` is not set elsewhere, it becomes `glob:.`. If `gw.path` is already set by the Act, command configuration, or configurator, the explicit value takes precedence.

## Loading Acts

Ghostwriter can load Acts from:

- built-in classpath resources under `/acts/`;
- a configured user Acts directory;
- a configured HTTP or HTTPS Acts location;
- a direct TOML file reference.

If an Act exists both as a built-in resource and in the user-defined location, the user-defined Act can override or extend the built-in behavior.

An absolute path to a TOML file can be used as the Act file name. In that case, classpath hierarchy lookup is not supported for that file reference.

## How Acts fit into the project

The Act feature belongs to the AI-backed processing layer in `org.machanism.machai.gw.processor`. This package connects project layout information, prompt configuration, function-tool registration, file scanning, interactive processing, guidance processing, and episode orchestration.

Key classes are:

- `AIFileProcessor`: prepares provider requests, handles placeholders, includes, prompt front matter, process metadata, function tools, and interactive input.
- `ActProcessor`: loads TOML Acts, applies inheritance and defaults, selects episodes, applies Act configuration, processes files or folders, and stores results.
- `Episodes`: manages ordered episodes, selected episodes, repeated episodes, and movement to another episode.
- `GuidanceProcessor`: processes files containing `@guidance:` comments. It is separate from Acts but uses the same AI processing infrastructure.

Important `ActProcessor` responsibilities include:

- selecting the Act from the command text;
- loading built-in, custom, local, absolute, or remote TOML definitions;
- applying `basedOn` inheritance;
- applying `[default]` fallback values;
- storing the user prompt as `public.prompt`;
- applying `gw.*` settings such as model, path, concurrency, exclusions, and interactive mode;
- running selected or regular episodes;
- collecting Act results.

## Practical example: create and run a custom Act

1. Create a TOML file in your configured Acts location, for example `review-docs.toml`.

2. Add a description, instructions, prompt, and defaults:

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

3. Run the Act without extra request text:

```text
--act review-docs
```

4. Or run it with a specific request:

```text
--act review-docs Check whether the Act documentation explains episodes clearly
```

5. If `gw.interactive = true`, continue the conversation after the first response. Enter `.` to finish or `>` to continue without a new message.

## Built-in Acts

The following Acts are provided in `src/main/resources/acts`.

### `code-doc`

Adds or updates documentation comments in source code. Use this Act when classes, methods, functions, or similar code elements need clearer documentation, such as Javadoc for Java, docstrings for Python, or XML comments for C#.

This Act focuses only on documentation. It should not change code logic or structure.

### `commit`

Helps document and commit local version-control changes. It checks the current folder status, analyzes modified files, groups related changes, generates commit messages that match the project's history, and can execute Git or SVN commands through function tools.

Use this Act when you want help preparing clean commits for the current project. It runs interactively and is intended to avoid unnecessary confirmation once the task is clear.

### `grype-fix`

Identifies and fixes dependency vulnerabilities using Grype scan results. It expects Syft and Grype to be installed, generates or uses an SBOM, finds vulnerable dependencies with available fixes, updates build files where possible, builds the project, and documents changes.

Use this Act when you need to remediate dependency vulnerabilities, especially in Maven or multi-module Maven projects.

### `help`

Provides user-friendly help for the Ghostwriter Act feature. It can explain what Acts are, show how to run them, summarize Act definitions, describe inheritance and overrides, and help users understand available tools and configuration.

Use this Act when you are learning Ghostwriter, exploring available Acts, or troubleshooting Act usage.

### `sonar-fix`

Fetches and fixes SonarQube issues for the current project component. It focuses on quality, security, maintainability, unit test coverage, successful builds, and avoiding changes to SonarQube configuration or quality gates.

Use this Act when a project has SonarQube findings that should be corrected in code. It uses runtime placeholders such as `${sonar.host}`, `${sonar.token}`, `${sonar.qualities}`, and `${sonar.severity}`.

### `task`

Provides a minimal general-purpose project-aware assistant. It sends the user's request through `${public.prompt}` without adding a specialized workflow.

Use this Act for one-off project questions or tasks when no specialized Act is a better fit.

### `unit-tests`

Generates or improves unit tests. It builds the project, uses Maven and JaCoCo coverage reports, identifies uncovered or under-covered code, creates or updates tests, and aims for meaningful high coverage.

Use this Act when you want to improve test coverage while following the project's existing test framework, package structure, and test style.

## Summary

Acts turn common AI-assisted development work into reusable workflows. They combine TOML configuration, instructions, prompts, project context, placeholders, inheritance, defaults, episodes, interactive mode, and function tools.

Good starting commands are:

```text
--act help
--act task Describe this project
--act code-doc Add missing documentation
--act unit-tests Improve unit test coverage
```

Use a specialized Act when the goal is clear. Use an interactive Act when you expect to refine the request through conversation.
