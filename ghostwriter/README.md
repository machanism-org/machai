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

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis.

## Introduction

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis.

It helps teams keep documentation accurate and consistent by combining:

- project structure and source scanning
- embedded, file-local guidance directives
- configurable AI prompts (instructions and default guidance)
- reproducible CLI-driven generation suitable for local use and CI

## Usage

### Basic Usage

Run Ghostwriter against a project directory:

```bash
java -jar gw.jar C:\projects\my-project
```

### Typical Workflow

1. (Optional) Create `gw.properties` next to `gw.jar` (or set `-Dgw.config=<path>`).
2. Decide a root directory (`--root`) if you want all scan targets resolved relative to a specific root.
3. Provide one or more scan targets as arguments:
   - a directory path
   - a pattern such as `glob:**/*.java` or `regex:^.*\/[^\/]+\.java$`
4. (Optional) Add instructions (`--instructions`) and/or default guidance (`--guidance`).
5. Run the CLI and review generated/updated documentation outputs.
