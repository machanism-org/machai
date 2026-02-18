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

Ghostwriter is a guidance-driven documentation engine and CLI that scans project files, extracts embedded `@guidance` directives, and uses a configured GenAI provider to synthesize and apply updates.

## Introduction

Ghostwriter is a guidance-driven documentation engine that scans project files, extracts embedded `@guidance` directives, and uses a configured GenAI provider to synthesize and apply updates. It is designed for real-world repositories where documentation spans many formats (source code, Markdown, HTML, configuration, and site content), enabling teams to keep artifacts accurate and consistent with less manual effort.

Benefits:

- Keeps documentation and project artifacts aligned by generating updates directly from file-embedded guidance.
- Works across heterogeneous repositories (code, docs, site pages, configs) in a single run.
- Supports project/module-aware scanning, exclusions, and provider-agnostic GenAI execution.

## Usage

### Getting Started

#### Prerequisites

- Java 11+
- Network access to your configured GenAI provider (as applicable)
- A project directory containing files with embedded `@guidance` directives (or use `--guidance` as a fallback)

#### Installation

Download the Ghostwriter CLI package:

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
