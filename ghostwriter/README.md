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

Machai Ghostwriter is a guided, AI-assisted processing engine that runs across an entire repository—source code, tests, documentation, and other project assets—to generate and maintain project-wide documentation and code improvements.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): instead of treating files as isolated inputs, Ghostwriter treats a repository as a structured system where each file can carry its own embedded guidance and the tool orchestrates consistent processing across the project.

Main benefits:

- **Guidance-first prompting**: instructions live next to the content they govern via embedded `@guidance:` blocks.
- **Repository-scale consistency**: deterministic scanning, per-type reviewers, and injected project context make runs repeatable.
- **Automation-ready**: designed for non-interactive execution and integration into scripts and CI/CD pipelines.

## Usage

### Getting Started

#### Prerequisites

- Java **8** (per `maven.compiler.release` in `pom.xml`).
- A configured GenAI provider/model (set `gw.model` in `gw.properties` or pass `-m/--model`).
- (Optional) `gw.properties` to persist configuration.

#### Installation

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

#### Basic Usage

```bash
java -jar gw.jar src -m OpenAI:gpt-5.1
```

#### Typical Workflow

1. Add `@guidance:` blocks to the files you want Ghostwriter to improve or document (code, docs, configs, site pages, etc.).
2. Create `gw.properties` (optional) and configure values such as:
   - `project.dir` (project root)
   - `gw.model` (provider:model)
   - `instructions` (optional system instructions)
   - `gw.excludes` (optional excludes)
   - `gw.guidance` (optional default guidance)
3. Run Ghostwriter against a directory or pattern (e.g., `src`, `glob:**/*.md`, `regex:...`).
4. Review the resulting changes and iterate.

#### Java Version

Ghostwriter requires **Java 8**. In addition, you must configure an accessible GenAI provider/model (for example via `gw.model` or `-m/--model`), otherwise the CLI fails fast.

## Resources

- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
