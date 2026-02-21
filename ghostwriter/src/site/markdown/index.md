---
canonical: https://machai.machanism.org/ghostwriter/index.html
---
<!-- @guidance:
**IMPORTANT:**  
The Ghostwriter Maven plugin is designed to work with **all types of project files**—including source code, documentation, project site content, and any other relevant files.  
**When generating content or documentation, always consider the full range of file types present in the project.**

## Page Structure

### Header
- **Project Title:** Extract from `pom.xml`.
- **Maven Central Badge:**  
  `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`

### Introduction
- Provide a comprehensive description of the project's purpose and its main benefits.

### Overview
- Explain the core functionality and value proposition of the project.

### Key Features
- Present a bulleted list of the primary capabilities and features.

### Getting Started
- **Prerequisites:** List all required software and services.
- **Download:**  
  Add the Ghostwriter CLI application jar download link:  
  `[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)`
- **Basic Usage:** Provide an example command to run the application.
- **Typical Workflow:** Outline the step-by-step process for using the project artifacts.
- **Java Version:**  
  Note that the required Java version is defined in `pom.xml`, but actual functional requirements may differ. Clearly state both.

### Configuration
- **Command-Line Options:**  
  Analyze the `/java/org/machananism/machai/gw/processor/Ghostwriter.java` source file to extract and describe all available command-line options.
- **Options Table:**  
  Present a table listing each option, its description, and default value.
- **Example:**  
  Provide a command-line example showing how to configure and run the application with custom parameters.  
  Include information from the `Ghostwriter.help()` method.

### Default Guidance
- Use the documentation for the `FileProcessor.setDefaultGuidance(String defaultGuidance)` method to generate a section detailing the purpose and usage of `defaultGuidance`.

### Resources
- List relevant links, including platform, GitHub, and Maven Central.

**General Instructions:**
- Ensure clarity and completeness in each section.
- Use information from project files and source code as specified.
- Structure the documentation for easy navigation and practical use.
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

> No mainstream tool offers the full range of features provided by Machai Ghostwriter out of the box.
>
>  ― <cite>&copy; OpenAI</cite>

## Introduction

Ghostwriter is an advanced documentation engine for projects that automatically scans, analyzes, and assembles documentation from the files you already have—source code, documentation, project site content, and other project artifacts—by using embedded `@guidance:` directives and AI-powered synthesis.

Its goal is to turn your repository into the single source of truth: you add or update small, local guidance next to the content (e.g., in a Markdown page, Java source file, or HTML template), and Ghostwriter can generate or refine the surrounding documentation consistently.

Ghostwriter runs as a CLI that traverses a project directory tree, detects supported file types, extracts embedded `@guidance:` instructions (via file-type reviewers), and sends the resulting prompt to a configured GenAI provider.

At a high level, the pipeline is:

1. Determine the project root and scan target (directory or `glob:` / `regex:` pattern).
2. Traverse the project (module-aware for Maven multi-module layouts).
3. For each supported file, extract `@guidance:` directives.
4. Compose a prompt including:
   - processing instructions (including OS-specific constraints),
   - the current project structure,
   - extracted per-file guidance (or fallback default guidance),
   - optional global instructions.
5. Execute the prompt via the configured provider/model.

## Machai Ghostwriter vs. Other Tools

Based on the current landscape of AI code assistant tools, **the tool most similar to Machai Ghostwriter is [Amazon CodeWhisperer](https://aws.amazon.com/codewhisperer/)**—but even it is not a perfect match. Here’s why:

**Key Similarities:**

- **Project-wide and batch processing:**  
  Both Machai Ghostwriter and CodeWhisperer can be integrated into CI/CD pipelines and can process entire projects, not just provide inline suggestions.
- **Automation focus:**  
  Both are designed to automate code review, documentation, and code analysis tasks, rather than just assist interactively in an IDE.
- **Integration with build tools:**  
  Machai Ghostwriter is a Maven plugin/CLI, while CodeWhisperer can be integrated into build and deployment pipelines via AWS tooling.

**Key Differences:**

- **Custom Guidance and Documentation:**  
  Machai Ghostwriter is unique in its use of `@guidance` tags, customizable documentation generation, and enforcement of project standards.
- **Open extensibility:**  
  Machai Ghostwriter is open for extension via Maven configuration, custom guidance, and GenAI provider integration, while CodeWhisperer is more tightly coupled to AWS and less customizable.
- **IDE vs. Pipeline:**  
  Most other tools (GitHub Copilot, Cursor, Tabnine, etc.) are primarily IDE assistants, focusing on inline code suggestions, not project-wide automation.

### Other comparisons

- **Tabnine:**  
  Offers some project-wide code analysis and batch suggestions, but is still primarily an IDE assistant.
- **Claude Code (Anthropic) and OpenAI Function Calling:**  
  Can be used for project-wide automation if integrated via API, but require custom scripting and do not offer out-of-the-box project scanning or documentation enforcement like Machai Ghostwriter.
- **Cursor:**  
  Has some project-wide features, but is fundamentally an AI-powered code editor.

**Summary Table:**

| Tool                   | Project-wide Automation | Custom Guidance | CI/CD Integration | Documentation Generation |
|------------------------|------------------------|-----------------|-------------------|-------------------------|
| **Machai Ghostwriter** | Yes                    | Yes             | Yes               | Yes                     |
| **Amazon CodeWhisperer** | Yes                  | Limited         | Yes (AWS)         | Limited                 |
| **Tabnine**            | Partial                | No              | Partial           | No                      |
| **GitHub Copilot**     | No                     | No              | No                | Partial                 |
| **Claude Code / OpenAI API** | Possible (API)   | Yes (API)       | Yes (API)         | Yes (API)               |
| **Cursor**             | Partial                | Yes             | No                | Partial                 |


**Amazon CodeWhisperer** is the closest in terms of automation and project-wide processing, but **Machai Ghostwriter remains unique** in its deep integration with Maven, customizable guidance, and documentation-first approach.  

## Key Features

- Scans and processes many project file types (not just source code), including project site content.
- Extracts embedded `@guidance:` directives using pluggable per-type reviewers.
- Supports directory scanning as well as `glob:` and `regex:` path matchers.
- Module-aware scanning for Maven multi-module projects (child modules first).
- Optional multi-threaded module processing (when the provider is thread-safe).
- Optional logging of composed LLM inputs to per-file log files.
- Supports reusable global instruction blocks and default guidance loaded from plain text, URLs, or local files.

## Getting Started

### Prerequisites

- Java: required to run the CLI.
  - **Build target (from `pom.xml`):** Java **8** (`maven.compiler.release=8`).
  - **Runtime requirement:** depends on the chosen GenAI provider client and your environment; if you encounter runtime issues on Java 8, use a newer JRE (e.g., 11+) while still building at the project’s configured release level.
- Network access to your configured GenAI provider endpoint (as applicable).
- Provider credentials/configuration (commonly via `GW_HOME\gw.properties`, environment variables, or provider-specific configuration expected by the Machai GenAI client).

### Download

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```text
java -jar gw.jar <scanDir> [options]
```

Example (scan a folder):

```text
java -jar gw.jar src\main\java
```

### Typical Workflow

1. Add `@guidance:` directives to the files you want Ghostwriter to act on (e.g., Markdown pages under `src\site`, Java sources, HTML, etc.).
2. Choose a scan target:
   - a directory path (relative to the current project), or
   - a `glob:` matcher (for example: `glob:**/*.java`), or
   - a `regex:` matcher.
3. Configure your GenAI provider/model (via `-a/--genai` or configuration).
4. Optionally set global instructions (`-i/--instructions`) and/or fallback guidance (`-g/--guidance`).
5. Run Ghostwriter and review the generated outputs/changes (depending on provider tools and your workflow).

## Configuration

### Command-Line Options

The CLI options are defined in `org.machanism.machai.gw.processor.Ghostwriter`.

- `-h`, `--help` — Show help and exit.
- `-t`, `--threads[=<true|false>]` — Enable multi-threaded processing. If specified without a value, it enables multi-threading.
- `-a`, `--genai <provider:model>` — Set the GenAI provider and model (example: `OpenAI:gpt-5.1`).
- `-i`, `--instructions[=<text|url|file:...>]` — Provide global system instructions. When used without a value, you are prompted to enter multi-line text via stdin. Lines can reference `http(s)://...` or `file:...` to include external/local content.
- `-g`, `--guidance[=<text|url|file:...>]` — Provide default guidance (fallback). When used without a value, you are prompted to enter multi-line text via stdin. Lines can reference `http(s)://...` or `file:...`.
- `-e`, `--excludes <csv>` — Comma-separated list of directories/paths/patterns to exclude from processing.
- `-l`, `--logInputs` — Log composed LLM request inputs to dedicated log files.

### Options Table

| Option | Argument | Description | Default |
|---|---:|---|---|
| `-h`, `--help` | none | Show help message and exit. | n/a |
| `-t`, `--threads` | `true`/`false` (optional) | Enable multi-threaded module processing; if used without a value, it enables it. | From config key `threads` (default `false`); when enabled, provider must be thread-safe. |
| `-a`, `--genai` | `provider:model` | GenAI provider/model identifier. | From config key `genai`; otherwise `OpenAI:gpt-5-mini`. |
| `-i`, `--instructions` | text/url/file (optional) | Global instructions appended to every prompt; supports `http(s)://...` and `file:...` includes; prompts via stdin if no value. | From config key `instructions`; otherwise none. |
| `-g`, `--guidance` | text/url/file (optional) | Fallback guidance used when files have no embedded `@guidance:`; supports `http(s)://...` and `file:...` includes; prompts via stdin if no value. | From config key `guidance`; otherwise none. |
| `-e`, `--excludes` | csv | Comma-separated exclude list (can also be configured via config key `excludes`). | From config key `excludes`; otherwise none. |
| `-l`, `--logInputs` | none | Log composed LLM inputs to per-file log files under a temp folder. | From config key `logInputs` (default `false`). |

### Example

The built-in help text documents supported scan targets:

- raw directory names,
- `glob:` patterns (e.g., `glob:**/*.java`),
- `regex:` patterns.

Example (Windows) scanning Java sources with a glob, enabling threads, setting provider/model, adding instructions and default guidance:

```text
java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l
```

## Default Guidance

`defaultGuidance` is a fallback instruction block used when a file does not contain embedded `@guidance:` directives.

You set it via:

- CLI: `-g/--guidance` (plain text, `http(s)://...`, or `file:...`; supports stdin when provided without a value), or
- API: `FileProcessor#setDefaultGuidance(String)`.

Behavior and supported formats (as documented on `setDefaultGuidance`):

- Plain text is used as provided.
- Each line is processed:
  - blank lines are preserved,
  - lines starting with `http://` or `https://` are loaded and included,
  - lines starting with `file:` are read from the specified file path and included,
  - other lines are included as-is.

When a supported file has no embedded guidance, Ghostwriter applies the default guidance to ensure the file can still be processed with meaningful instructions.

## Resources

- Project site: https://machai.machanism.org/ghostwriter/
- GitHub (SCM): https://github.com/machanism-org/machai
- Maven Central (artifact): https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- GitHub Copilot: https://github.com/features/copilot
- Anthropic Claude: https://www.anthropic.com/claude
- Cursor: https://www.cursor.so/
