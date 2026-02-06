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

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded `@guidance` tags and AI-powered synthesis.

## Introduction

Ghostwriter helps keep documentation consistent and up to date by treating `@guidance` blocks embedded in source and documentation files as mandatory constraints, then applying GenAI to produce repeatable, reviewable updates.

## Usage

### Basic Usage

```bash
java -jar gw.jar C:\projects\my-project
```

### Typical Workflow

1. Add `@guidance` blocks (typically at the top of docs/source files) describing required structure and constraints.
2. Run Ghostwriter against a project directory or a `glob:` / `regex:` path pattern.
3. Review the updated/generated artifacts and commit the results.
4. Re-run Ghostwriter periodically (or in CI) to keep documentation aligned with the guidance.
