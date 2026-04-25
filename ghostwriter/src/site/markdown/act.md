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
- The act name or interactive command: `exit` is reserved for terminating a process.
- An absolute path to a TOML file can be used as the file name; in this case, hierarchy using  classpath resources on the is not supported.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/ghostwriter/act.html
---

# Act

An Act is a reusable task preset for Ghostwriter.

Instead of rewriting the same long prompt each time, you can run an act by name. Ghostwriter loads the matching TOML file, applies its configuration, builds the final prompt, and then performs the requested work on the project.

Acts make common tasks easier, more consistent, and easier to repeat. For example, an act can help with:

- writing or improving code documentation
- generating unit tests
- creating release notes
- fixing issues reported by SonarQube or Grype
- learning which acts are available and how they work
- preparing and executing commits for local changes

Built-in acts are stored in `src/main/resources/acts`. You can also load acts from a custom directory or from an HTTP(S) location with the `--acts` option.

## What the Act feature does

The Act feature is implemented by `org.machanism.machai.gw.processor.ActProcessor`.

At a high level, it works like this:

1. Ghostwriter reads the act command.
2. It separates the act name from any user-supplied request text.
3. It loads the matching act TOML definition.
4. It resolves inheritance with `basedOn`.
5. It merges built-in and custom values when both exist.
6. It prepares the final prompt from the act `inputs` template.
7. It applies Ghostwriter runtime settings such as scan scope, recursion, threads, model selection, and interactive mode.
8. It runs the act against the matching project files.

Important `ActProcessor` methods include:

- `setAct(String)` - parses the act command, loads the act, applies the `prompt` fallback, fills `%s` in `inputs`, and activates the resulting configuration
- `loadAct(...)` - loads an act and recursively resolves `basedOn`
- `tryLoadActFromClasspath(...)` - loads built-in acts from `/acts/<name>.toml`
- `tryLoadActFromDirectory(...)` - loads custom acts from a file path, directory, or URL-based acts location
- `setActData(...)` - copies TOML values into the working property map and merges string templates
- `applyActData(...)` - applies the final act values to runtime configuration and processor settings

## How to run an act

Use this command format:

```text
--act <name> [your request text]
```

Examples:

```text
--act help
--act task Update the user guide for beginners
--act code-doc Add missing Javadoc to public services
```

How Ghostwriter interprets the value:

- if the act value is empty, Ghostwriter uses `help`
- the first word becomes the act name
- all remaining text becomes the user request
- if no request text is provided, Ghostwriter uses the current default prompt and may fall back to the act `prompt` value

The act name or interactive command `exit` is reserved for terminating a process, so it must not be used as an act name.

An absolute path to a TOML file can also be used as the act name. When you do that, Ghostwriter loads that file directly, and classpath act hierarchy lookup is not supported for that file name.

## Main properties in an act TOML file

Common properties include:

- `description` - short explanation of the act and when to use it
- `instructions` - the main rules and behavior for the AI assistant
- `inputs` - the user prompt template
- `prompt` - default prompt text used when the user does not provide request text
- `basedOn` - inherit settings from another act
- `gw.scanDir` - files or folders to scan
- `gw.threads` - concurrency level
- `gw.excludes` - excluded files or patterns
- `gw.nonRecursive` - disable recursive scanning
- `gw.interactive` - enable interactive mode
- `genai.model` - select a model for the act
- other properties such as `ft.command.denylist` - forwarded into Ghostwriter configuration

TOML tables are flattened into dotted properties. For example:

```toml
[gw]
scanDir = "glob:."
```

becomes the property `gw.scanDir`.

## Interactive and non-interactive mode

Acts can run in two main styles.

### Non-interactive mode

In non-interactive mode, an act behaves like a predefined command. It is best when the task is already clear and the act has enough information to continue without follow-up conversation.

Use this mode when:

- the task is already well defined
- the act contains all important instructions
- you want a more direct, automatic workflow

To activate it in a TOML file:

```toml
gw.interactive = false
```

If `gw.interactive` is not set, the act stays non-interactive unless an inherited or merged value changes that.

Examples of built-in non-interactive acts include:

- `code-doc`
- `commit`
- `grype-fix`
- `release-notes`
- `sonar-fix`
- `unit-tests`

### Interactive mode

In interactive mode, an act behaves more like a chat. This is useful when the user does not know all required details before starting, or when the task needs clarification during the session.

Use this mode when:

- the task is still being defined
- more context may be needed while working
- the workflow is exploratory rather than fully fixed in advance

To activate it in a TOML file:

```toml
gw.interactive = true
```

Built-in interactive examples include:

- `task`
- `help`

Typical usage:

```text
--act task
--act help
```

These can be used like interactive conversations rather than one-shot commands.

## Using the `prompt` property

The `prompt` property lets an act define default request text.

This is useful when an act should still work sensibly even if the user runs it without adding any extra text after the act name.

Example:

```toml
prompt = "Perform the default and special rules."
inputs = '''
# Task

%s
'''
```

During execution, Ghostwriter does the following:

1. It checks whether the user provided text after the act name.
2. If not, it uses the processor's current default prompt.
3. If that is empty or missing, it uses the act `prompt` value.
4. It inserts the chosen text into the `inputs` template where `%s` appears.

This means an act can still do useful work even when started like this:

```text
--act sonar-fix
```

The built-in `sonar-fix` act uses `prompt = "Perform the default and special rules."`.

## How inheritance and merged values work

Acts support several layers of inheritance and value reuse.

### 1. Inheriting from another act with `basedOn`

An act can inherit from another act like this:

```toml
basedOn = "task"
```

When Ghostwriter sees `basedOn`, it:

1. loads the parent act first
2. copies the parent properties into the working data
3. loads the child act
4. lets the child override or extend the inherited values

After loading is complete, `basedOn` itself is removed from the final property set.

If both parent and child define the same key, the child normally wins, unless string template merging using `%s` is involved.

### 2. Merging built-in and custom acts with the same name

Ghostwriter may load both:

- a custom act from `--acts`
- a built-in act from the classpath

If both exist with the same act name, both are loaded and merged.

For string values, `ActProcessor#setActData(...)` uses `%s` as a merge placeholder.

How string merging works:

- if the existing value already contains `%s`, the newly loaded string is inserted into that placeholder
- if the existing value does not contain `%s`, the new value replaces it

Example:

Built-in act:

```toml
inputs = '''
# Task

%s
'''
```

Custom act:

```toml
inputs = "Only update documentation. %s"
```

Merged result before final user text is inserted:

```text
# Task

Only update documentation. %s
```

Later, the remaining `%s` is replaced with the actual request text.

### 3. Reusing existing runtime configuration values

When `applyActData(...)` applies the act properties, Ghostwriter also checks whether the current runtime configuration already has a value for the same key.

If the act value contains `%s`, that placeholder is replaced with the existing configuration value.

This makes it possible to extend an existing setting instead of replacing it completely.

Example:

```toml
[ft.command]
denylist = '''
%s
rm -rf
'''
```

If Ghostwriter already has denylist entries, they are inserted where `%s` appears, and the new rule is added after them.

## Step-by-step example

A simple real-world workflow looks like this:

1. Choose an act.
   - Use `task` for a general task.
   - Use `code-doc` for documentation comments.
   - Use `unit-tests` for test creation.

2. Optionally provide a custom acts location.

```text
--acts path/to/acts
```

or

```text
--acts https://example.com/acts/
```

3. Run the act with or without extra request text.

```text
--act code-doc Add missing Javadoc in public API classes
```

4. Ghostwriter loads the act file and any inherited parent act.
5. Ghostwriter builds the final prompt from `inputs` and your request.
6. Ghostwriter applies settings such as scan scope and interactive mode.
7. Ghostwriter processes the matching files in the project.

## Built-in acts

The following built-in acts are defined in `src/main/resources/acts`.

### task

A general-purpose act for custom project work.

Use it when you want a flexible starting point for almost any task. It applies a simple `# Task` wrapper around the user request and enables interactive mode, so it works well when you want a conversation rather than a fixed one-step command.

Main points:

- purpose: generic project task execution
- mode: interactive
- notable setting: `gw.interactive = true`

### help

An act for learning about Ghostwriter acts.

Use it when you want to list acts, inspect act details, understand inheritance, or get guidance on act configuration. It is interactive, scans only the current directory, and disables recursive scanning.

Main points:

- purpose: explain and inspect acts
- mode: interactive
- notable settings: `gw.scanDir = "."`, `gw.nonRecursive = true`, `gw.interactive = true`

### code-doc

An act for adding or improving documentation comments.

Use it when code needs Javadoc, docstrings, or similar comments. It is focused on documentation quality and explicitly says that program logic should not be changed.

Main points:

- purpose: add or refresh code documentation
- mode: non-interactive
- notable behavior: updates comments only and preserves code logic

### commit

An act for analyzing local version-control changes and committing them.

Use it when you want Ghostwriter to inspect the current project changes, group them into logical commits, prepare commit messages in the project's historical style, and run version-control commands automatically.

Main points:

- purpose: group changes and create commits
- mode: non-interactive
- notable settings: scans `glob:.` and can extend `ft.command.denylist`

### grype-fix

An act for fixing dependency vulnerabilities reported by Grype.

Use it when you want Ghostwriter to analyze vulnerability results, update dependencies to secure versions, verify the build, and document the remediation work in the relevant build files.

Main points:

- purpose: remediate vulnerable dependencies
- mode: non-interactive
- notable settings: scans `glob:.`
- note: requires Syft and Grype to already be installed

### release-notes

An act for generating release notes from project commit history.

Use it when preparing a release and you want Ghostwriter to collect commit messages, group them by change type, and save the result in `src/changes/changes.xml` using Maven Changes XML format.

Main points:

- purpose: create release notes and append them to Maven changes XML
- mode: non-interactive
- notable settings: scans `glob:.`

### sonar-fix

An act for fixing issues reported by SonarQube.

Use it when you have SonarQube issue data and want focused code corrections that follow strict rules, especially for `@SuppressWarnings`. This act also defines a default `prompt` value so it can run even when no extra request text is supplied.

Main points:

- purpose: review and remediate SonarQube issues
- mode: non-interactive
- notable property: `prompt = "Perform the default and special rules."`
- note: usually needs a custom act to provide environment-specific SonarQube access details

### unit-tests

An act for creating or improving unit tests.

Use it when you want Ghostwriter to build the project, measure coverage, review existing tests, add new tests, and work toward high coverage for the selected source area.

Main points:

- purpose: increase automated test coverage
- mode: non-interactive
- notable setting: `gw.scanDir = "glob:**/test/java"`

## Summary

The Act feature gives Ghostwriter a practical way to package repeatable workflows into named commands.

It combines:

- reusable instructions
- reusable input templates
- optional default prompt values
- inheritance and extension rules
- Ghostwriter runtime settings

This makes common tasks easier to run, easier to understand, and easier to maintain for both experienced users and newcomers.
