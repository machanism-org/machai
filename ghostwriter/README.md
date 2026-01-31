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
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

![](src/site/resources/images/machai-ghostwriter-logo.png)

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Project Overview

Machai Ghostwriter is a CLI documentation engine (also available as a runnable JAR) that scans a project directory and processes documentation-related files according to embedded guidance and any additional instructions you provide.

## Introduction

Machai Ghostwriter is a CLI documentation engine that automates and standardizes project documentation and code annotation. Using guided file processing with embedded `@guidance` blocks, it helps teams keep documentation consistent, reviewable, and up to date across repositories. It’s designed to work well in scripts and CI so documentation changes can be generated and committed as part of your normal workflow.

## Usage

### Prerequisites

- Java 11+ runtime.
- Network access to your selected GenAI provider (if applicable).

### Installation

Download the CLI JAR:

[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/gw.jar/download)

### Basic Usage

```bash
java -jar gw.jar
```

Scan a specific root directory:

```bash
java -jar gw.jar --root /path/to/project
```

Scan one or more directories (positional arguments):

```bash
java -jar gw.jar --root /path/to/project docs src
```

### Typical Workflow

1. Build or download `gw.jar`.
2. Choose the project root (`--root`) and the directories to scan (positional args).
3. (Optional) Provide additional instructions via `--instructions` or `gw.properties`.
4. (Optional) Provide a default final guidance file via `--guidance`.
5. Run Ghostwriter.
6. Review and commit generated/updated documentation.

## Resources

- Platform: https://machanism.org/guided-file-processing/index.html
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download CLI JAR: https://sourceforge.net/projects/machanism/files/machai/gw.jar/download
