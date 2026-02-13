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

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis. It helps teams keep documentation accurate and consistent by generating updates directly from the source tree and the rules embedded in documentation files.

## Introduction

Ghostwriter runs as a CLI that traverses one or more directories (or file patterns), extracts `@guidance` blocks embedded in documents, and uses a configured GenAI provider/model to synthesize or review content. It supports applying default, directory-level guidance as a final step, while allowing file-specific guidance to steer output.

## Usage

### Basic usage

```bat
java -jar gw.jar C:\projects\my-project
```

### Typical workflow

1. Add `@guidance` blocks to documentation files where you want consistent, rule-driven output.
2. Configure your GenAI provider/model (via `gw.properties` or CLI).
3. Run Ghostwriter against a project directory (or a pattern) to generate/review documentation updates.
4. Review changes and commit the updated documentation.
