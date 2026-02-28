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

Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine. It scans a repository (source code, documentation, project-site content under `src/site`, build metadata like `pom.xml`, and other artifacts), extracts embedded `@guidance:` directives, and uses a configured GenAI provider to apply consistent improvements across many files in a repeatable, CI-friendly way.

## Introduction

Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans your repository (source code, documentation, project-site content under `src/site`, build metadata like `pom.xml`, and other artifacts), extracts embedded `@guidance:` directives, and uses a configured GenAI provider to apply consistent improvements across many files in a repeatable way. This makes it practical to keep documentation, conventions, and refactors aligned across an entire project—especially when changes must be deterministic, reviewable, and CI-friendly.

Ghostwriter is built on **[Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)**: guidance lives next to the content it controls, and the processor composes those local directives—plus any configured defaults—into a structured prompt per file. The result is automation that remains explicit and version-controlled inside the repository.

## Usage

### Getting Started

#### Prerequisites

- **Java**
  - **Build target:** Java **8** (from `pom.xml`: `maven.compiler.release=8`).
  - **Runtime:** you can generally run on Java 8+; some GenAI provider/client libraries may require a newer JVM at runtime.
- **GenAI provider access and credentials** as required by your provider (for example via `GW_HOME\\gw.properties`, environment variables, or provider-specific configuration).
- **Network access** to the provider endpoint (if applicable).

#### Download

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

#### Basic Usage

```cmd
java -jar gw.jar <scanTarget> [options]
```

Example (scan a folder on Windows):

```cmd
java -jar gw.jar src\\main\\java
```

#### Typical Workflow

1. Add `@guidance:` directives to the files you want Ghostwriter to update (Markdown under `src\\site`, Java sources, templates, etc.).
2. Choose a scan target:
   - directory path (relative to the project), or
   - `glob:` matcher (example: `glob:**/*.java`), or
   - `regex:` matcher.
3. Configure your GenAI provider/model and credentials.
4. Optionally add global instructions and/or default guidance.
5. Run Ghostwriter, then review and commit the results.

### Configuration

#### Command-Line Options

| Option | Description | Default |
|---|---|---|
| `-h`, `--help` | Show help message and exit. | n/a |
| `-r`, `--root <path>` | Root directory used as the base for relative scan targets and `file:` includes. | From config key `gw.rootDir`; otherwise current working directory. |
| `-t`, `--threads[=<true\\|false>]` | Enable multi-threaded module processing; if provided without a value, it enables multi-threading. | From config key `gw.threads` (default `false`) |
| `-a`, `--genai <provider:model>` | GenAI provider/model identifier (example: `OpenAI:gpt-5.1`). | From config key `gw.genai`; otherwise must be provided |
| `-i`, `--instructions[=<text\\|url\\|file:...>]` | Global system instructions appended to every prompt; supports `http(s)://...` and `file:...`; prompts via stdin if no value. | From config key `gw.instructions`; otherwise none |
| `-g`, `--guidance[=<text\\|url\\|file:...>]` | Fallback guidance used when files have no embedded `@guidance:`; supports `http(s)://...` and `file:...`; prompts via stdin if no value. | From config key `gw.guidance`; otherwise none |
| `-e`, `--excludes <csv>` | Comma-separated list of directories/paths/patterns to exclude from processing. | From config key `gw.excludes`; otherwise none |
| `-l`, `--logInputs` | Log composed LLM inputs to per-file log files under a temp folder. | From config key `gw.logInputs` (default `false`) |
| `--act[=<text>]` | Run Ghostwriter in Act mode (execute predefined prompts); prompts via stdin if no value. | Disabled |

#### Example

```cmd
java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l
```

### Default Guidance

`defaultGuidance` is a fallback instruction block used when a file does not contain embedded `@guidance:` directives.

It can be set via:

- CLI: `-g` / `--guidance` (plain text, `http(s)://...`, or `file:...`; supports stdin when provided without a value), or
- API: `FileProcessor#setDefaultGuidance(String)`.

The value is parsed line-by-line:

- blank lines are preserved,
- lines beginning with `http://` or `https://` are fetched and included,
- lines beginning with `file:` are read from the referenced file and included,
- other lines are included as-is.

### Resources

- Official platform: https://machai.machanism.org/ghostwriter/
- GitHub (SCM): https://github.com/machanism-org/machai
- Maven Central (parent): https://central.sonatype.com/artifact/org.machanism.machai/machai
- Project site: https://machai.machanism.org/ghostwriter/index.html
