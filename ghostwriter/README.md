<!--
@guidance:
**ADD FOLLOWING SECTIONS TO THIS README FILE:**
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\site\markdown\index.md` content summary.
   - Add `![](src/site/resources/images/machai-ghostwriter-logo.png)` before the title.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

![](src/site/resources/images/machai-ghostwriter-logo.png)

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

## Project Overview

Ghostwriter is an AI-powered CLI application that scans a project directory and processes documentation-related files according to embedded guidance and any additional instructions you provide.

You can optionally specify:

- A GenAI provider/model (for example, `OpenAI:gpt-5.1`).
- Additional processing instructions (inline or loaded from a file).
- One or more directories to include in the scan.

## Introduction

Machai Ghostwriter is an AI-powered tool that automates and standardizes project documentation and code annotation. Using guided file processing and `@guidance` annotations, it enables developers to maintain consistent, clear, and up-to-date documentation across multi-module projects in languages such as Java, TypeScript, and Python. Ghostwriter simplifies the embedding, extraction, and management of project guidance, ensuring your codebase and documentation remain synchronized.

Learn more about guided file processing: https://machanism.org/guided-file-processing/index.html

## Usage

### Prerequisites

- Java 9+ runtime.
- Network access to your selected GenAI provider (if applicable).

### Installation

Download the CLI JAR:

[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/gw.jar/download)

### Basic Usage

```bash
java -jar gw.jar
```

To scan a specific project root directory:

```bash
java -jar gw.jar --dir /path/to/project
```

To scan one or more specific directories (positional arguments):

```bash
java -jar gw.jar . docs src
```

### Typical Workflow

1. Build or download `gw.jar`.
2. Choose the project root (`--dir`) and the directories to scan (positional args).
3. (Optional) Provide additional instructions (inline or via `--instructions /path/to/file`).
4. Run Ghostwriter.
5. Review and commit generated/updated documentation.

## Resources

- Platform: https://machanism.org/guided-file-processing/index.html
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download CLI JAR: https://sourceforge.net/projects/machanism/files/gw.jar/download
