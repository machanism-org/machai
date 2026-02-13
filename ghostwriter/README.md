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

Ghostwriter is an AI-assisted documentation engine that scans a project, applies mandatory `@guidance` constraints embedded in source and documentation files, and generates or updates documentation to keep it consistent with the current project state.

## Introduction

Ghostwriter works with many file types across a project, including source code, documentation, project site content, and other relevant artifacts. It treats inline `@guidance` blocks as mandatory constraints and can generate or update documentation in repeatable runs to keep project documentation aligned with the current project state.

Key features include:

- Scans many file types, including source code, Markdown, and other project artifacts
- Treats inline `@guidance` blocks as mandatory constraints during generation
- Generates or updates documentation in repeatable runs
- Provides language-aware reviewers for multiple formats (for example: Java, Markdown, HTML, Python, TypeScript)

## Usage

### Run the CLI

After building, run the packaged JAR against a local project directory:

```cmd
java -jar ghostwriter\target\ghostwriter-0.0.10-SNAPSHOT.jar C:\projects\my-project
```

### Examples

Set an explicit root directory:

```cmd
java -jar ghostwriter\target\ghostwriter-0.0.10-SNAPSHOT.jar -r C:\projects\my-project
```

Target files with a glob pattern:

```cmd
java -jar ghostwriter\target\ghostwriter-0.0.10-SNAPSHOT.jar "glob:**\*.md"
```
