<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\\site\\markdown\\index.md` content summary.
   - Add `![](src/site/resources/images/machai-ghostwriter-logo.png)` before the title.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
   - Add the Ghostwriter CLI application jar download link: [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download) to the installation section.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

![](src/site/resources/images/machai-ghostwriter-logo.png)

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

> Ghostwriter is a repository-wide AI automation and documentation engine for applying embedded guidance across source code, documentation, project site content, configuration, diagrams, and other project artifacts.

## Introduction

Ghostwriter is an advanced repository-wide AI automation and documentation engine in the Machai ecosystem. It scans project content, detects embedded `@guidance` instructions, and applies GenAI-assisted processing to source code, documentation, project site content, configuration, diagrams, and other relevant artifacts. The project is designed to reduce documentation drift, automate repetitive maintenance, and keep human intent close to the files it governs.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html). Rather than relying only on one-off conversational prompts, Ghostwriter turns persistent repository guidance into repeatable, reviewable project automation that can run locally or in CI/CD pipelines.

## Usage

### Getting Started

#### Prerequisites

- Java 8 or later, based on `maven.compiler.release` set to `8` in `pom.xml`.
- Access to a supported GenAI provider and any required credentials or network connectivity.
- A project or working directory containing files to scan and update.
- Optional `gw.properties` configuration in the Ghostwriter home directory, or a custom configuration file selected with the `gw.config` system property.
- Optional acts directory when using Act mode with predefined act definitions.
- Version control is strongly recommended so generated changes can be reviewed before commit.

#### Installation

Download the Ghostwriter CLI application pack:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

The delivery pack provides `gw.jar` and runtime dependencies needed to execute Ghostwriter from the command line.

#### Basic Usage

```bash
java -jar gw.jar <path> [options]
```

Examples:

```bash
java -jar gw.jar src
java -jar gw.jar "glob:**/*.java"
java -jar gw.jar "regex:^.*/[^/]+\\.java$"
```

#### Typical Workflow

1. Add or update `@guidance` directives in the files Ghostwriter should maintain.
2. Configure provider/model, scan defaults, excludes, instructions, and optional acts in `gw.properties` or through command-line options.
3. Run Ghostwriter against a directory or pattern target.
4. Review generated changes in version control.
5. Re-run locally or in CI/CD to keep governed project artifacts current.

#### Java Version

Ghostwriter requires **Java 8+**. Functional use also requires a valid GenAI provider/model configuration and any credentials or connectivity required by that provider.

## Configuration

Ghostwriter loads settings from `gw.properties` in the resolved home directory unless overridden with the `gw.config` system property. The home directory is resolved from `gw.home` when defined; otherwise it defaults to the current user directory. The project root is taken from `-d` / `--project.dir`, then from configuration, and otherwise falls back to the current user directory.

### Command-Line Options

| Option | Description | Default value |
|---|---|---|
| `-h`, `--help` | Show the help message and exit. | None |
| `-d`, `--project.dir <path>` | Specify the root directory for file processing. | `project.dir` from configuration, otherwise the current user directory |
| `-t`, `--threads <n>` | Set the degree of concurrency for processing. | `gw.threads` from configuration |
| `-m`, `--model <provider:model>` | Set the GenAI provider and model, for example `OpenAI:gpt-5.1`. | `gw.model` from configuration |
| `-i`, `--instructions [value]` | Specify system instructions as plain text, by URL, or by file path. Lines beginning with `http://` or `https://` are loaded from the URL, lines beginning with `file:` are loaded from the file path, blank lines are preserved, and other lines are used as-is. If used without a value, Ghostwriter prompts for standard input. | `instructions` from configuration |
| `-e`, `--excludes <csv>` | Specify a comma-separated list of directories or patterns to exclude from processing. | `gw.excludes` from configuration |
| `-l`, `--logInputs` | Log LLM request inputs to dedicated log files. | Disabled unless enabled by the option or configuration |
| `-as`, `--acts <path>` | Specify the directory containing predefined act prompt files. | `gw.acts` from configuration |
| `-a`, `--act [value]` | Run Ghostwriter in Act mode for executing predefined or ad-hoc prompts. If used without a value, Ghostwriter prompts for act text interactively. | No act unless supplied on the command line or through `gw.act` when applicable |

The positional `<path>` argument defines the scan target. It may be a relative path with respect to the current project directory, an absolute path located within the root project directory, a raw directory name, a glob pattern such as `glob:**/*.java`, or a regex pattern such as `regex:^.*/[^/]+\\.java$`. If no scan target is supplied, Ghostwriter falls back to `gw.path` from configuration and then to `.`.

### Example

```bash
java -Dgw.config=gw.properties -jar gw.jar src \
  -d . \
  -m OpenAI:gpt-5.1 \
  -t 4 \
  -e ".git,target" \
  -i "file:./instructions.txt" \
  -l
```

Act mode example:

```bash
java -jar gw.jar src -a "Summarize the repository" -as ./acts
```

## Resources

- Official platform: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
