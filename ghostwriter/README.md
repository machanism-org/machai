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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

> A guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.

## Introduction

Machai Ghostwriter is an AI-assisted documentation and review engine that scans your entire project—source code, tests, documentation, and other relevant assets—extracts embedded `@guidance:` directives, and turns them into actionable prompts for a configured GenAI provider.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): instead of treating files as isolated inputs, Ghostwriter treats a repository as a structured system, where each file can carry local guidance and the tool orchestrates processing across the project consistently.

## Usage

### Getting Started

#### Prerequisites

- Java **8** (as configured by `maven.compiler.release` in `pom.xml`).
- A configured GenAI provider setting (`gw.genai`) or CLI override (`-a/--genai`).
- (Optional) A `gw.properties` file to persist configuration.

#### Download

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

#### Basic Usage

```bash
java -jar gw.jar <scanDir> -a OpenAI:gpt-5.1
```

#### Typical Workflow

1. Add `@guidance:` blocks to the files you want Ghostwriter to improve or document.
2. Create `gw.properties` (optional) and configure:
   - `gw.rootDir` (project root)
   - `gw.genai` (provider:model)
   - `gw.instructions` (optional)
   - `gw.excludes` (optional)
   - `gw.guidance` (optional default guidance)
3. Run Ghostwriter against a directory or pattern (e.g., `src`, `glob:**/*.md`, `regex:...`).
4. Review the resulting changes and iterate.

#### Java Version

Ghostwriter requires **Java 8**. In addition, you must configure an accessible GenAI provider/model (e.g., via `gw.genai` or `-a/--genai`), otherwise the CLI will fail fast.

## Resources

- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
