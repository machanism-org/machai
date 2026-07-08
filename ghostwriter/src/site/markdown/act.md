---
<!-- @guidance: 
Create the Act page as a Project Information page for the project:
- Analyze the `src/main/java/org/machanism/machai/gw/processor/ActProcessor.java` class and `src/main/resources/acts` files as toml act file examples.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Create a separate section describing the action's interactive/non-interactive mode.
  - An action can be used as a non-interactive command to perform a predefined task without any additional data.
  - An action can be used interactively (as a chat). This is necessary when the user does not have full information about the desired action before initiating it.
  - Describe how to use value of `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` and `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` to continue or terminate the act processing.
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

An **Act** is a predefined task template for Machai Ghostwriter. It lets you describe a repeatable workflow — such as
generating documentation, committing code changes, or fixing security vulnerabilities — and store it as a
[TOML](https://toml.io/) configuration file. When you run an act, Ghostwriter loads the template, merges it with your
input, and sends the composed request to the configured AI provider.

Acts make it straightforward to share, reuse, and customize AI-assisted workflows without writing code. They are the
primary way to extend or automate Ghostwriter for any project.

---

## How Acts Work

1. **Select an act** by name (e.g. `commit`, `code-doc`) or provide a full path to a TOML file.
2. **Optionally supply a prompt** — a short piece of text that describes what you want the AI to do. If you omit the
   prompt, the default prompt defined in the act file is used.
3. Ghostwriter loads the TOML file, resolves any inherited configuration, substitutes placeholder variables, and sends
   the assembled request to the AI provider.
4. The AI processes the request and returns its response. In interactive mode you can continue the conversation; in
   non-interactive mode the result is written out immediately.

### Act Syntax

```
--act <name> [your prompt text]
```

- `<name>` — the name of the built-in act **or** a full path to a `.toml` file.
- `[your prompt text]` — optional text appended to the act's `inputs` template.

**Examples**

```
# Run the built-in commit act with no extra prompt
--act commit

# Run the code-doc act and focus on a specific package
--act code-doc Update Javadoc for the service layer

# Run a custom act from an absolute file path
--act /home/user/my-acts/review.toml
```

> **Tip:** An absolute path to a TOML file can be used as the act name. In this case the classpath resource hierarchy
> is not used, and the file is loaded directly from the file system.

---

## Act TOML File Format

An act is stored as a `.toml` file and may contain the following keys.

| Key | Type | Description |
|-----|------|-------------|
| `instructions` | string | System-level instructions sent to the AI (sets persona, tone, and scope). |
| `inputs` | string or array | The prompt template(s). Use `${public.prompt}` to embed the user-supplied text. |
| `description` | string | Human-readable description of the act's purpose. |
| `basedOn` | string | Inherit from another act by name (see [Inheritance](#inheritance)). |
| `gw.path` | string | File or directory pattern to scan (e.g., `glob:.`). |
| `gw.threads` | integer | Number of worker threads for parallel processing. |
| `gw.excludes` | string | Comma-separated paths or patterns to exclude from scanning. |
| `gw.nonRecursive` | boolean | When `true`, disables recursive directory traversal. |
| `gw.interactive` | boolean | When `true`, enables interactive (chat) mode. |
| `gw.model` | string | Overrides the AI model/provider for this act. |

Any other key-value pair defined in the TOML file is forwarded to the underlying configuration and is available for
placeholder substitution.

### Minimal Example

```toml
description = "A simple example act."

instructions = '''
You are a helpful assistant.
'''

inputs = '''
# Task
${public.prompt}
'''
```

---

## Placeholder Variables

Placeholder variables use the `${...}` format and are resolved at runtime by the Ghostwriter engine. They allow act
templates to reference dynamic values such as environment variables, system properties, configuration entries, and
act-level properties without hard-coding them.

**Important:** Placeholders must never be altered, resolved, or removed from the source TOML. They are substituted
automatically by the runtime engine. Modifying them will break the act.

Common placeholders used in act files:

| Placeholder | Resolved Value |
|-------------|----------------|
| `${public.prompt}` | The user-supplied prompt text (or the act's default prompt). |
| `${sonar.host}` | The SonarQube server URL (read from configuration). |
| `${sonar.token}` | The SonarQube authentication token (read from configuration). |
| `${sonar.qualities}` | The SonarQube quality filter setting (read from configuration). |
| `${sonar.severity}` | The SonarQube severity filter setting (read from configuration). |

You can also define custom properties at the top level of an act TOML (or in a `[default]` section) and reference them
via `${<key>}` in `inputs` or `instructions`.

---

## Default Prompt

The `inputs` key in an act TOML file serves as a **prompt template**. Embed `${public.prompt}` where you want the
user-supplied text to appear:

```toml
inputs = '''
# Task
${public.prompt}
'''
```

If the user runs the act without providing any prompt text, the `inputs` template is used as-is, with
`${public.prompt}` substituting to an empty string (or a configured default). This means the act still executes
meaningfully even when no extra instruction is given.

You can also configure a hardcoded default directly in the `inputs` field if you want a fully self-contained act that
never requires a user prompt.

---

## Interactive and Non-Interactive Mode

### Non-Interactive Mode (default)

By default acts run in **non-interactive** mode. Ghostwriter sends the composed prompt to the AI, collects the
response, and finishes. This is ideal for automated pipelines, CI/CD integrations, or any scenario where the full
task can be described upfront.

```toml
# Non-interactive act (default behaviour — no gw.interactive key needed)
inputs = '''
# Task
${public.prompt}
'''
```

### Interactive Mode (chat)

Set `gw.interactive = true` to enable a **chat-like conversation**. After the AI returns its first response,
Ghostwriter waits for your next input. You can ask follow-up questions, provide clarification, or guide the AI
through a multi-step task — all within a single session.

```toml
[gw]
interactive = true
```

Interactive mode is useful when you do not have all the information needed before starting the act, or when the
result of one step should influence the next.

### Controlling the Session

While in interactive mode two special commands control the flow:

| Command | Constant | Effect |
|---------|----------|--------|
| `.` | `AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND` | Terminates the interactive session immediately and exits processing. |
| `>` | `AIFileProcessor.CONTINUE_SPECIAL_PROMPT_COMMAND` | Skips the remaining interactive prompts and continues with normal (sequential) execution. |

Type `.` (a single period) at the prompt to stop the session, or `>` (a greater-than sign) to skip ahead and let
Ghostwriter continue with the next step.

---

## Using Episodes

An act's `inputs` key can be a **single string** for a simple one-shot task, or a **TOML array** of strings to define
multiple sequential steps called **episodes**. Each episode is an independent prompt that Ghostwriter sends to the AI
in order.

```toml
inputs = [
  '''
  # Step 1 – Analyse
  Analyse the project structure and list all public APIs.
  ${public.prompt}
  ''',
  '''
  # Step 2 – Document
  Write Javadoc for each public API identified in the previous step.
  ''',
  '''
  # Step 3 – Review
  Review the documentation for completeness and accuracy.
  ''',
]
```

### How Episodes Are Executed

- Episodes are numbered **starting from 1** in the order they appear in the `inputs` array.
- By default all episodes run in sequence (regular order).
- You can **select specific episodes** to run using the `#` notation appended to the act name:

  ```
  --act my-act#2        Run only episode 2
  --act my-act#1,3      Run episodes 1 and 3
  --act my-act#2!       Run only episode 2 and skip the normal sequential order
  ```

- The `!` suffix after the episode selection stops Ghostwriter from continuing with the remaining episodes after the
  selected ones have finished.
- Episodes can also be addressed by their **heading text** (the first `# Heading` line in the episode string).

### Episode Context

Before each episode Ghostwriter provides the AI with act metadata — including the act name, the full list of episodes
and their names, and the ID of the current episode — so the AI always knows where it is in the workflow.

---

## Inheritance

Acts can inherit configuration from other acts using the `basedOn` property. This allows you to build specialised
acts on top of a common base without duplicating configuration.

```toml
basedOn = "base-act"

instructions = '''
# Extended instructions that build on the base act.
$$super.value$$
Additional instructions specific to this act.
'''
```

### How Inheritance Works

1. When an act declares `basedOn = "<parent-act>"`, the **parent act is loaded first**.
2. The parent's properties are placed into the merged property map.
3. The child act's properties are then applied on top. For **string values**, the child may embed the parent's value
   using the special placeholder `$$super.value$$`. At merge time this placeholder is replaced with whatever the
   parent had defined.
4. For **array values** (episode lists), each slot is merged independently: if a child slot contains
   `$$super.value$$`, it is replaced by the corresponding parent slot.
5. The `basedOn` key itself is removed after the parent is loaded so it does not leak into configuration.

### Default Section Inheritance

A special `[default]` section inside a TOML file allows you to declare **fallback values** for any property. If the
property already has a value in the active configuration (e.g., from the command line or environment), the configured
value is kept. Otherwise the `[default]` value is applied.

```toml
[default]
gw.model = "openai:gpt-4o"
gw.threads = "2"
```

This is useful for providing sensible defaults that can still be overridden by the user's environment.

### Combining Custom and Built-in Acts

If an act with the same name exists both as a **user-defined file** (in the configured `gw.acts` directory) and as a
**built-in classpath resource**, both are loaded. The user-defined act wraps the built-in act: its properties take
precedence, and `$$super.value$$` can be used to embed values from the built-in counterpart.

### Inheritance Example

**base.toml** (built-in)

```toml
instructions = '''
You are a helpful assistant.
'''

inputs = '''
# Task
$$super.value$$
Base context is applied here.
'''
```

**my-act.toml** (user-defined, in `gw.acts` directory)

```toml
basedOn = "base"

instructions = '''
$$super.value$$
Additionally, focus on security best practices.
'''

inputs = '''
# Task
Perform a security review.
${public.prompt}
'''
```

When `my-act` is loaded, its `instructions` will read:
> *You are a helpful assistant.*
> *Additionally, focus on security best practices.*

---

## Step-by-Step: Running Your First Act

1. **Install and configure Ghostwriter** according to the project setup guide.
2. **Choose an act** from the built-in list below, or create your own TOML file.
3. **Run the act** from your project root directory:

   ```
   gw --act code-doc
   ```

4. Ghostwriter will scan the relevant files, compose the prompt, and call the AI. The result is displayed in your
   terminal (and optionally written to the target file).

5. To pass additional context to the act, append it after the act name:

   ```
   gw --act code-doc Document only the public API surface
   ```

6. For interactive acts (e.g. `commit` or `task`), the session keeps running until you type `.` to exit or `>` to
   skip to the next step.

---

## Built-in Acts

The following acts are bundled with Ghostwriter and are available out of the box.

---

### `code-doc`

**Purpose:** Automates the process of adding or updating documentation comments in source code files.

**When to use:** Run this act when you want to generate or improve documentation comments (such as Javadoc for Java,
docstrings for Python, or XML comments for C#) across your codebase. The act analyses each source file, generates
clear and concise descriptions for classes, methods, and functions, and inserts or updates the comments in the correct
format for the target programming language.

**Key behaviour:**
- Reviews all code files and identifies elements that lack documentation.
- Generates documentation that includes purpose, parameter descriptions, return values, and exception notes.
- Updates existing comments for improved clarity and completeness.
- Outputs only the updated file content — no extra explanations are added.
- Does not modify code logic; only documentation comments are changed.

**Example usage:**

```
gw --act code-doc
gw --act code-doc Focus on the payment module only
```

---

### `commit`

**Purpose:** Automates documentation and committing of code changes in a version-controlled project.

**When to use:** Run this act after making changes to your project files to automatically group the modifications,
generate meaningful commit messages (following the project's historical style), and execute the appropriate
`git` or `svn` commit commands.

**Key behaviour:**
- Checks the current directory status using `git status` (or the equivalent VCS command).
- If no changes exist, the task ends immediately.
- Groups changes by type (feature, fix, refactor, docs, chore, etc.) and generates a commit message for each group.
- Executes the commit commands automatically via functional tools.
- Runs in interactive mode by default, allowing you to review or adjust before committing.
- Output is plain text; no markdown formatting is used.

**Example usage:**

```
gw --act commit
gw --act commit Only commit documentation changes
```

---

### `grype-fix`

**Purpose:** Automatically identifies and fixes dependency vulnerabilities detected by a
[Grype](https://github.com/anchore/grype) scan.

**When to use:** Run this act after running a Grype vulnerability scan on your project. It reads the scan results,
updates affected dependencies to secure versions in the build configuration (e.g., `pom.xml` for Maven), verifies
that the project still builds successfully, and documents each change with a reference to the CVE or GHSA identifier.

**Prerequisites:** Syft and Grype must be installed and available on the system `PATH`.

**Key behaviour:**
- Generates an SBOM using Syft and runs Grype to obtain vulnerability data.
- For each vulnerability where a fix is available, updates the dependency version in the build file.
- Builds the project after updates to verify there are no regressions.
- Adds inline comments to the build file referencing the vulnerability identifier.
- Summarises any vulnerabilities that could not be fixed automatically.

**Example usage:**

```
gw --act grype-fix
gw --act grype-fix Focus on HIGH and CRITICAL severity only
```

---

### `help`

**Purpose:** Provides expert assistance and help information for the Ghostwriter Act feature itself.

**When to use:** Run this act when you need to understand how acts work, explore available acts, view act definitions,
or learn how inheritance and configuration options are applied. It is the best starting point for users who are new to
Ghostwriter.

**Key behaviour:**
- Retrieves online documentation and act reference material via functional tools.
- Explains act structure, properties, and inheritance relationships.
- Can describe a specific act by name — just pass the act name as your prompt.
- Runs in interactive mode, allowing follow-up questions.
- Output is plain text; no markdown formatting is used.

**Example usage:**

```
gw --act help
gw --act help How do I use the basedOn property?
gw --act help commit
```

---

### `sonar-fix`

**Purpose:** Reviews and fixes all open issues detected by SonarQube for the project.

**When to use:** Run this act when you have a running SonarQube instance and want to automatically address quality,
security, and reliability issues reported for your project. It connects to the SonarQube API, retrieves open issues,
applies code fixes, generates or updates unit tests, and saves a list of modified files.

**Key behaviour:**
- Queries the SonarQube API using the configured `${sonar.host}`, `${sonar.token}`, `${sonar.qualities}`, and
  `${sonar.severity}` placeholder values.
- Applies targeted fixes for common Java security rules (e.g., SQL injection, XSS, weak cryptography) and code
  quality issues.
- Adds unit tests to cover all fixed code paths, aiming for high code coverage.
- Validates the project build and full test suite after each fix.
- Saves the list of updated files to the `UPDATED_FILES_REPORT` project context variable.
- Does not modify SonarQube configuration, plugin settings, or quality gates.

**Example usage:**

```
gw --act sonar-fix
gw --act sonar-fix Fix only BLOCKER and CRITICAL security issues
```

---

### `task`

**Purpose:** A minimal, generic act for executing custom user prompts within the project context.

**When to use:** Use this act when you have a specific, ad-hoc task or question that does not fit any of the
specialised acts. It provides a clean, context-aware environment where the AI can answer questions, write code, or
perform any project-related activity based entirely on your prompt.

**Key behaviour:**
- Provides context-aware AI assistance with full awareness of the project structure, files, and configuration.
- Runs in interactive mode by default — you can have a back-and-forth conversation.
- Does not declare any prologue or epilogue actions; the act is exactly what you type.
- Confirms actions with side effects (such as file modifications) before proceeding.

**Example usage:**

```
gw --act task
gw --act task Explain the purpose of the ActProcessor class
gw --act task Refactor the login service to use dependency injection
```

---

### `unit-tests`

**Purpose:** Automatically generates high-quality unit tests to improve code coverage.

**When to use:** Run this act when you want to boost test coverage for your project. It builds the project,
runs the JaCoCo coverage tool to identify under-covered areas, and generates or updates unit tests targeting the
gaps.

**Key behaviour:**
- Builds the project with `mvn clean install` and generates a JaCoCo coverage report.
- Analyses the report (`target/site/jacoco/jacoco.xml`) to find uncovered or under-covered classes and methods.
- Creates new unit tests (or updates existing ones) in the appropriate test source directory.
- Targets at least 90% overall test coverage for the scanned source folder.
- Follows the Arrange-Act-Assert (AAA) pattern and uses descriptive test method names.
- Avoids removing existing passing tests; only fixes or supplements them.
- Scans files matching `glob:**/test/java` by default.

**Example usage:**

```
gw --act unit-tests
gw --act unit-tests Focus on the repository layer
```

---

## Creating a Custom Act

You can create your own acts by writing a TOML file and either placing it in the directory configured by `gw.acts` or
providing its absolute path as the act name.

**Minimal custom act (`my-review.toml`):**

```toml
description = "Performs a code review and suggests improvements."

instructions = '''
You are a senior software engineer performing a thorough code review.
Focus on correctness, readability, and performance.
'''

inputs = '''
# Code Review Task
Please review the following code and provide actionable feedback.
${public.prompt}
'''

[gw]
path = "glob:src/main/java/**"
interactive = true
```

**Run it:**

```
gw --act /absolute/path/to/my-review.toml
```

Or, if `gw.acts` is configured to point to the directory containing `my-review.toml`:

```
gw --act my-review
```

---

## Summary

| Feature | Description |
|---------|-------------|
| Act | A reusable TOML template that drives an AI-assisted workflow. |
| `inputs` | Prompt template(s); embed `${public.prompt}` for user-supplied text. |
| `instructions` | System instructions that set the AI's persona and constraints. |
| `basedOn` | Inherit and extend another act's configuration. |
| `$$super.value$$` | Placeholder to embed the parent act's value during inheritance merge. |
| Episodes | Multiple sequential prompts defined as a TOML array in `inputs`. |
| Interactive mode | Chat-like session; type `.` to exit, `>` to continue. |
| Placeholder variables | `${...}` expressions resolved at runtime from configuration and environment. |
| Default prompt | `${public.prompt}` substitution — uses user text or falls back to act default. |
| Custom acts | Any `.toml` file on the file system; load by absolute path or `gw.acts` directory. |
