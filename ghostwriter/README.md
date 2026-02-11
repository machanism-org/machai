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

Ghostwriter is a documentation engine that scans a project, applies mandatory `@guidance` constraints embedded in source and documentation files, and uses GenAI to generate or update documentation consistently.

## Introduction

Ghostwriter scans a project, interprets mandatory `@guidance` blocks embedded in source and documentation files, and uses GenAI to generate or update documentation artifacts in a repeatable way.

It is designed to keep documentation consistent and current by treating `@guidance` blocks as non-negotiable constraints during content generation.

## Usage

### Run the CLI

From the built artifact (example):

```cmd
java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar C:\projects\my-project
```

### Common options

- Set the root directory:

```cmd
java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar -r C:\projects\my-project
```

- Target files with glob patterns:

```cmd
java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar "glob:**\*.md"
```
