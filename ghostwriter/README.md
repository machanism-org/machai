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

Ghostwriter is a documentation engine and CLI that scans your project, extracts embedded `@guidance` directives, and uses a configured GenAI provider to generate or refine documentation.

## Introduction

Ghostwriter is a documentation engine and CLI that scans your project, extracts embedded `@guidance` directives, and uses a configured GenAI provider to generate or refine documentation.

It supports **all types of project files**—including source code, documentation, project site content, and other relevant artifacts—so teams can keep docs aligned with the evolving codebase.

## Usage

### Getting Started

#### Prerequisites

- Java 11 or later
- Network access and credentials for your selected GenAI provider (as configured in your environment and/or `gw.properties`)

#### Installation

Download the Ghostwriter CLI bundle:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

#### Basic Usage

```cmd
java -jar gw.jar src\main\java
```

#### Typical Workflow

1. Add embedded `@guidance` blocks to the files you want Ghostwriter to generate or refine.
2. Run Ghostwriter against the directory (or pattern) containing those files.
3. Review the output, commit changes, and re-run as needed.
