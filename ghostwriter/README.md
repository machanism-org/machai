<!--
@guidance:
**ADD FOLLOWING SECTIONS TO THIS README FILE:**
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\\site\\markdown\\index.md` content summary.
   - Add `![](src/site/resources/images/machai-ghostwriter-logo.png)` before the title.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
   - Add the Ghostwriter CLI application jar download link: [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download) to the installation section.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

![](src/site/resources/images/machai-ghostwriter-logo.png)

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

Ghostwriter is an advanced documentation engine and CLI that scans project files, extracts embedded `@guidance` directives, and uses a configured GenAI provider to synthesize and apply updates.

## Introduction

Ghostwriter is designed to work with all types of project files—including source code, documentation, project site content, configuration, and other relevant artifacts. It scans a project tree, extracts embedded `@guidance` directives, and uses a configured GenAI provider to synthesize and apply updates in a consistent, repeatable way.

Benefits:

- Keeps documentation and project artifacts aligned by generating updates directly from file-embedded guidance.
- Works across heterogeneous repositories (code, docs, site pages, configs) in a single run.
- Supports module-aware scanning, exclusions, and provider-agnostic GenAI execution.

## Usage

### Getting Started

#### Prerequisites

- Java (build/target is configured for Java 8 via `maven.compiler.release` in `pom.xml`; functional requirements may differ depending on provider/tooling)
- Network access to your configured GenAI provider (as applicable)
- A project directory containing files with embedded `@guidance` directives (or use `--guidance` as a fallback)

#### Download

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

#### Basic Usage

```cmd
java -jar gw.jar src
```

#### Typical Workflow

1. Add `@guidance` directives to the files you want Ghostwriter to process.
2. Run Ghostwriter against a folder, file, or pattern.
3. Review generated changes in your working tree.
4. Iterate by refining guidance (or providing `--instructions` / `--guidance` defaults) and re-running.

### Configuration

#### Command-Line Options

| Option | Description | Default |
|---|---|---|
| `-h`, `--help` | Show help message and exit. | N/A |
| `-r`, `--root <path>` | Root directory used as the base for relative paths (and to validate scan paths). | `root` from `gw.properties` (if present); otherwise the current working directory |
| `-t`, `--threads[=<true\|false>]` | Enable multi-threaded module processing. If provided without a value, it enables multi-threading. | `threads` from `gw.properties` (fallback `false`) |
| `-a`, `--genai <provider:model>` | GenAI provider and model (for example: `OpenAI:gpt-5.1`). | `genai` from `gw.properties` (fallback `OpenAI:gpt-5-mini`) |
| `-i`, `--instructions[=<text\|url\|file:path>]` | System instructions as plain text, URL, or file reference. If used without a value, you will be prompted to enter multi-line instructions via stdin (EOF to finish). | `instructions` from `gw.properties` (if present) |
| `-g`, `--guidance[=<text\|url\|file:path>]` | Default guidance applied when a file has no embedded `@guidance` directives. Accepts plain text, URL, or file reference. If used without a value, you will be prompted to enter multi-line guidance via stdin (EOF to finish). | `guidance` from `gw.properties` (if present) |
| `-e`, `--excludes <dir[,dir...]>` | Comma-separated list of directories or patterns to exclude. | `excludes` from `gw.properties` (if present) |
| `-l`, `--logInputs` | Log LLM request inputs to dedicated log files under the Machai temp directory. | `logInputs` from `gw.properties` (fallback `false`) |

#### Help Output and Pattern Examples

From `Ghostwriter.help()`:

- Usage: `java -jar gw.jar <scanDir> [options]`
- `<scanDir>` may be a raw directory name, a `glob:` pattern (example: `"glob:**/*.java"`), or a `regex:` pattern (example: `"regex:^.*\\/[^\\/]+\\.java$"`).

Example commands:

```cmd
java -jar gw.jar C:\projects\project
java -jar gw.jar src\project
java -jar gw.jar "glob:**/*.java"
java -jar gw.jar "regex:^.*\\/[^\\/]+\\.java$"
```

### Default Guidance

`defaultGuidance` is a fallback instruction set applied when a processed file does not contain embedded `@guidance` directives. It lets you apply consistent rules across a run (for example, formatting constraints, repository conventions, or update policies) without requiring every file to carry its own guidance block.

You can provide `defaultGuidance`:

- As plain text
- As a URL (`http://` or `https://`)
- As a file reference (`file:<path>`) resolved relative to the configured root directory when not absolute

When present, Ghostwriter will use embedded file guidance when available; otherwise it applies `defaultGuidance`.

### Resources

- Maven Central (parent): https://central.sonatype.com/artifact/org.machanism.machai/machai
- Project source: https://github.com/machanism-org/machai
- Project site: https://machai.machanism.org/ghostwriter/index.html
