---
<!-- @guidance: 
Create or update the Act page as a Project Information page for the project:
- Analyze `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` and `src/main/java/org/machanism/machai/gw/processor/AIFileProcessor.java` 
  classes and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Create a separate section describing the action's interactive/non-interactive mode.
  - An action can be used as a non-interactive command to perform a predefined task without any additional data.
  - An action can be used interactively (as a chat). This is necessary when the user does not have full information about the desired action before initiating it.
  - Describe how to use value of `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` and `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` to continue or terminate the act processing.
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

An Act is a reusable task definition for Machai Ghostwriter. It tells Ghostwriter what the AI assistant should do, what instructions it should follow, which files or folders it should process, and which runtime options should be used.

Acts are written as TOML files. A TOML file is a simple configuration file format made of key-value pairs and sections. In this project, built-in acts are stored in `src/main/resources/acts` and can be used as examples for creating custom acts.

The main goal of the Act feature is to make common AI-assisted project tasks easy to repeat. Instead of writing a long prompt every time, a user can choose an act such as `unit-tests`, `commit`, or `sonar-fix`, optionally add a short request, and let Ghostwriter apply the predefined instructions.

## What an Act can do

An Act can:

- Run a predefined task, such as generating unit tests, fixing security findings, or preparing commits.
- Apply a standard set of AI instructions to a project or selected files.
- Use a default prompt when the user does not provide one.
- Process one prompt or multiple prompts in sequence using episodes.
- Enable interactive chat-style processing when the task needs user feedback.
- Configure Ghostwriter options such as path scanning, recursion, thread count, model, exclusions, and interactive mode.
- Enable AI tools through prompt input parameters.
- Inherit settings from another act through `basedOn`.
- Load act definitions from built-in resources, a configured custom act location, a remote URL, or a direct TOML file path.

## Basic TOML structure

A typical act can contain these properties:

```toml
description = '''
A short explanation of what this act does.
'''

instructions = '''
System-level instructions that define the assistant role and behavior.
'''

inputs = '''
# Task

${public.prompt}
'''

[gw]
path = "glob:."
interactive = true

[default]
gw.model = "some-default-model"
```

Common properties include:

- `description`: Human-readable explanation of the act.
- `instructions`: The main behavior rules sent to the AI provider.
- `inputs`: The prompt template sent to the AI provider.
- `basedOn`: The parent act to inherit from.
- `gw.path`: The project path or glob pattern to process.
- `gw.interactive`: Enables or disables interactive mode.
- `gw.nonRecursive`: Limits scanning to the selected folder when set to `true`.
- `gw.excludes`: Comma-separated paths or patterns to exclude.
- `gw.threads`: Degree of concurrency for processing.
- `gw.model`: AI model or provider identifier.
- `default.*`: Default values that are applied only when a direct value is not already configured.

## How Ghostwriter loads an Act

The `ActProcessor` is responsible for loading and applying acts.

When an act name is requested, Ghostwriter:

1. Splits the act command into the act name and optional user prompt.
2. Uses `help` if the act name is blank.
3. Loads a matching TOML file.
4. Applies inheritance through `basedOn`, if present.
5. Applies default values from the `[default]` section.
6. Applies the user prompt or the configured default prompt.
7. Applies Ghostwriter settings such as path, model, exclusions, recursion, and interactive mode.
8. Processes the project, selected files, or episode sequence.

Built-in acts are searched under the classpath location `/acts/` and use the `.toml` extension. Custom acts can be loaded from a configured act location. The configured location can be a local folder or an HTTP/HTTPS URL.

A direct TOML file path can also be used as the act name if it ends with `.toml`. When an absolute path to a TOML file is used, classpath hierarchy lookup is not supported for that file reference.

## Interactive and non-interactive mode

Acts can run in two main modes.

### Non-interactive mode

In non-interactive mode, the act runs like a command. The user starts the task, Ghostwriter performs the predefined work, and the process completes without asking for more input.

Use non-interactive mode when:

- The task is clear before starting.
- No additional user decisions are expected.
- The act should be suitable for automation or scripting.

Example use cases:

- Generate documentation for selected source files.
- Run a vulnerability remediation workflow.
- Analyze a project and produce a one-time result.

### Interactive mode

In interactive mode, the act behaves more like a chat. The AI response can be followed by additional user input. This is useful when the user does not know all details before starting the action, or when the AI may need clarification.

Interactive mode is enabled with:

```toml
[gw]
interactive = true
```

or with the dotted form:

```toml
gw.interactive = true
```

During interactive processing, special prompt commands are supported:

- `.` is the value of `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND`. Enter it to terminate processing immediately.
- `>` is the value of `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND`. Enter it to accept the current response and continue processing without sending another follow-up prompt.

Any other input is treated as a follow-up prompt and is sent to the AI provider.

For example:

```text
Please review the current implementation.
```

continues the conversation, while:

```text
>
```

continues the act flow, and:

```text
.
```

exits the process.

## Default user prompt with the `prompt` property

An act normally receives the user request from the command line after the act name. If the user does not provide a request, Ghostwriter can use a default prompt.

The public prompt value used by act templates is stored as `public.prompt`. In TOML, place a default value under the `[default]` section using `public.prompt`:

```toml
[default]
public.prompt = "Review the project and summarize the most important findings."
```

Then reference the prompt in `inputs`:

```toml
inputs = '''
# Task

${public.prompt}
'''
```

If the user starts the act without extra prompt text, the default value is used. If the user provides prompt text, that user text is used instead.

This is helpful for acts that should have useful behavior even when launched with only the act name.

## Episodes

An act can define one prompt or multiple prompt episodes. Episodes let an act perform a larger task in ordered steps.

A single prompt can be written as a string:

```toml
inputs = '''
Analyze the project and summarize it.
'''
```

Multiple episodes can be written as an array:

```toml
inputs = [
  '''Analyze the current project structure.''',
  '''Suggest improvements.''',
  '''Create a final summary.'''
]
```

Ghostwriter processes episodes in regular order unless a specific episode selection is requested.

Episode selection uses `#` after the act name:

```text
unit-tests#1,2
```

This requests episodes 1 and 2. A comma separates episode numbers.

Adding `!` disables normal continuation after the requested episodes finish:

```text
unit-tests#1,2!
```

This means: run episodes 1 and 2, then stop instead of continuing with the regular order.

The episode mechanism is useful when a complex task should be broken into steps such as analysis, modification, testing, and reporting.

## Prompt input parameters

`AIFileProcessor` supports YAML front matter at the beginning of a prompt. These input parameters are removed from the prompt text and used to control the AI provider for that prompt.

Supported parameters include:

- `gw.model`: Overrides the model or provider for the current prompt.
- `enabledTools`: Enables one tool or a list of tools for the current prompt.

Example:

```text
---
gw.model: ${public.ai.model}
enabledTools:
  - project-context
  - file-system
---
Analyze this project and recommend improvements.
```

String values in the YAML block are resolved through the active configurator. List values are preserved, and their string entries can also be resolved.

This feature allows a single episode to select a different model or enable specific tools only for that prompt.

## Including external prompt content

Prompt and instruction text can include external content by starting a line with `>>>`.

Examples:

```text
>>> https://example.com/instructions.md
>>> file://docs/project-guidelines.md
```

Supported references include HTTP URLs, HTTPS URLs, and `file://` paths. File paths can be absolute or relative to the project root. Included content is read as UTF-8 and parsed recursively, so included files can include more files.

## Placeholder variables

Act files commonly use placeholder variables in the `${...}` format. These placeholders are designed for dynamic substitution at runtime.

Examples include:

```text
${public.prompt}
${public.ai.model}
${sonar.host}
${sonar.token}
${sonar.qualities}
${sonar.severity}
```

Placeholders retrieve values from configuration sources such as environment variables, system properties, action properties, or other configurator layers.

Important rule: placeholders in the `${...}` format must remain exactly as written in act files and documentation examples. They should not be altered, resolved, renamed, or rewritten by the LLM. Runtime tools and configurators are responsible for substituting them.

## Inheritance and defaults

Acts can inherit from another act by using `basedOn`:

```toml
basedOn = "task"
```

When inheritance is used, Ghostwriter loads the parent act first, then loads the child act. The resulting configuration is a merged set of properties.

### Parent and child values

If a child defines the same property as the parent, the child value normally overrides or merges with the parent value.

For string values, the special marker `$$super.value$$` can be used to insert the inherited value into the child value.

Example:

```toml
# Parent
instructions = '''
Always follow project conventions.
'''
```

```toml
# Child
basedOn = "parent"
instructions = '''
$$super.value$$
Also focus on security concerns.
'''
```

The final instructions include the parent instructions followed by the child addition.

### Array values

If `inputs` or another TOML array is inherited, values are merged by position. Each child array entry can replace or extend the matching parent entry. The `$$super.value$$` marker can keep the inherited text and add new text around it.

### Default section values

Values under `[default]` are fallback values. They are applied only when the same direct property is not already present.

Example:

```toml
[default]
gw.path = "glob:."
gw.model = "default-model"
```

If the act or configurator already provides `gw.path`, the default does not override it. For `gw.model`, the current model is preserved when one has already been selected.

### Configurator inheritance

When a property value contains `$$super.value$$`, Ghostwriter can also replace it with an existing value from the active configurator. This allows custom acts to extend configured values without losing them.

For example, a custom denylist can extend an inherited denylist:

```toml
[ft.command]
denylist = '''
$$super.value$$
additional-dangerous-command
'''
```

## How Acts fit into the project

The Act feature connects user-friendly task names to the lower-level AI processing engine.

Key classes and responsibilities:

- `ActProcessor`: Loads TOML act files, handles act inheritance, applies defaults, selects episodes, and runs act processing.
- `AIFileProcessor`: Sends instructions and prompts to the AI provider, resolves prompt includes, extracts YAML input parameters, handles interactive mode, and builds process metadata.
- `Episodes`: Manages ordered and selected episode execution.
- `ProjectLayout`: Provides project structure, relative paths, modules, sources, tests, and documentation folders.
- Function tools: Allow the AI provider to perform controlled actions such as reading files, running allowed commands, accessing project context, or ending a task.

Each AI call receives process information as JSON. It includes:

- `PROCESSED_FILE_REL_PATH`: The project-relative path currently being processed.
- `PROCESS_MODE`: Either `INTERACTIVE` or `NOT-INTERACTIVE`.

This helps the AI understand where it is operating and whether follow-up conversation is expected.

## Practical example: creating a simple custom act

1. Create a TOML file in your configured acts directory, for example `review.toml`.

2. Add a description:

```toml
description = '''
Reviews the project and gives practical improvement suggestions.
'''
```

3. Add instructions:

```toml
instructions = '''
You are a helpful software reviewer. Explain findings clearly and focus on practical improvements.
'''
```

4. Add an input template:

```toml
inputs = '''
# Task

Review this project and provide useful suggestions.

${public.prompt}
'''
```

5. Add Ghostwriter options:

```toml
[gw]
path = "glob:."
interactive = true
```

6. Optionally add a default prompt:

```toml
[default]
public.prompt = "Focus on maintainability, test coverage, and security."
```

7. Run the act by name. If additional prompt text is supplied, it is used as `${public.prompt}`. If no prompt is supplied, the default prompt is used.

## Built-in Acts

The built-in act files are located in `src/main/resources/acts`.

### `code-doc`

Purpose: Adds or updates documentation comments in source code files.

Use this act when code needs clearer Javadoc, docstrings, XML comments, or equivalent language-specific documentation. It is designed to preserve code logic and only improve documentation comments.

Typical use cases:

- Add missing method or class documentation.
- Improve unclear existing comments.
- Standardize documentation style across code files.

### `commit`

Purpose: Helps analyze current project changes and commit them using meaningful commit messages.

Use this act when you want Ghostwriter to inspect version control changes, group related modifications, generate commit messages, and execute suitable git or svn commands. This act is interactive and is intended for development workflow automation.

Typical use cases:

- Prepare commits after completing a feature or fix.
- Group unrelated changes into separate commits.
- Match commit message style to project history.

### `grype-fix`

Purpose: Helps fix dependency vulnerabilities reported by Grype.

Use this act when Syft and Grype are installed and you want Ghostwriter to generate an SBOM, review Grype scan results, update vulnerable dependencies to fixed versions, verify the build, and document the remediation.

Typical use cases:

- Remediate dependency CVEs in Maven projects.
- Update vulnerable packages while keeping the project build stable.
- Produce a clear summary of fixed and unresolved vulnerabilities.

### `help`

Purpose: Provides help and explanations for the Act feature itself.

Use this act when you want to list available acts, understand a specific act, inspect act configuration, or learn how inheritance and overrides work. It is interactive and intended to answer user questions in plain text.

Typical use cases:

- Learn how to run an act.
- Understand the properties in an act TOML file.
- Ask for a summary of a built-in or custom act.

### `sonar-fix`

Purpose: Reviews and fixes SonarQube-detected issues in the project.

Use this act when SonarQube issues need to be fetched, analyzed, fixed, tested, and reported. It focuses on Java security and quality rules, avoids changing SonarQube configuration, and stores the list of changed files in the `UPDATED_FILES_REPORT` project context variable.

Typical use cases:

- Fix SonarQube bugs, vulnerabilities, and code smells.
- Add or update tests for fixed code.
- Ensure changes compile and do not introduce new issues.

### `task`

Purpose: Provides a minimal generic act for custom user requests.

Use this act when no specialized act matches the request. It passes the user's prompt into a simple project-aware task template and enables interactive mode.

Typical use cases:

- Ask a general project question.
- Request a small custom change.
- Run an ad hoc AI-assisted development task.

### `unit-tests`

Purpose: Generates or improves unit tests and increases test coverage.

Use this act when the project needs better automated tests. It builds the project, uses JaCoCo coverage information, identifies under-tested areas, and creates or updates meaningful unit tests in the correct test source folders.

Typical use cases:

- Improve coverage for new or existing code.
- Add tests for public and package-private methods.
- Cover edge cases, error handling, and static methods where appropriate.

## Choosing the right built-in Act

- Need documentation comments? Use `code-doc`.
- Need to commit current changes? Use `commit`.
- Need to fix Grype vulnerability findings? Use `grype-fix`.
- Need help with acts? Use `help`.
- Need to fix SonarQube findings? Use `sonar-fix`.
- Need a general-purpose assistant task? Use `task`.
- Need more or better tests? Use `unit-tests`.
