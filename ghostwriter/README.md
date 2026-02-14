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

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded `@guidance` directives and AI-powered synthesis.

## Introduction

Ghostwriter is designed to help teams keep documentation accurate and up-to-date by:

- Generating or updating content from real project sources (code, docs, site pages, and other relevant artifacts)
- Applying embedded, file-local directives (`@guidance`) to control what gets generated
- Producing consistent results using a configurable GenAI provider/model

## Usage

### Getting Started

#### Prerequisites

- Java 11 or later
- Network access to your selected GenAI provider (as configured in your environment/properties)

#### Installation

Download the Ghostwriter CLI bundle:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

#### Basic Usage

```cmd
java -jar gw.jar src\main\java
```

#### Typical Workflow

1. Add embedded `@guidance` blocks to files you want Ghostwriter to generate or refine.
2. Run Ghostwriter against the directory (or pattern) that contains those files.
3. Review the generated output, commit changes, and re-run as needed to keep docs current.
