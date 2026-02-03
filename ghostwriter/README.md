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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

## Project Title and Overview

Ghostwriter is an AI-assisted documentation engine that scans a project workspace, extracts embedded `@guidance` instructions from files, and assembles consistent, review-ready documentation.

## Introduction

Ghostwriter is an AI-assisted documentation engine that scans a project workspace, extracts embedded `@guidance` instructions from files, and assembles consistent, review-ready documentation.
It is designed to be used locally or in CI to keep project documentation aligned with evolving code and requirements.

## Usage

### Prerequisites

- Java 11+
- Network access to your chosen GenAI provider (if required by your configuration)
- (Optional) A `gw.properties` file to provide defaults

### Installation

- Download the Ghostwriter CLI distribution:

  [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```bash
java -jar gw.jar <scanDir | glob_path_pattern>
```

Examples:

```bash
# scan a directory
java -jar gw.jar C:\projects\project

# specify root explicitly
java -r C:\projects\project -jar gw.jar src/project

# scan with a glob pattern
java -r C:\projects\project -jar gw.jar "**/*.java"
```

### Typical Workflow

1. Choose a `root` directory (or let it default to the current user directory).
2. Run Ghostwriter against one or more scan targets (directories or glob patterns).
3. Provide additional instructions (optional) via `--instructions` (URL/file) or via stdin.
4. Provide a default guidance (optional) via `--guidance` (file) or via stdin.
5. Review the produced/updated documentation and logs.

## Resources

- Platform: https://machanism.org/guided-file-processing/index.html
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download Ghostwriter CLI: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
