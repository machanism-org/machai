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

It is a CLI that processes one or more scan targets (directory paths or path patterns) under a root directory. It applies optional instructions and optional default guidance, then produces documentation updates while logging its work. It is designed for both local use and CI pipelines where you want deterministic, scriptable documentation review and regeneration.

## Introduction

Ghostwriter helps you keep documentation consistent and up to date across large codebases by turning structured, in-repo guidance into repeatable, automatable documentation updates.

## Usage

### Basic Usage

```bash
java -jar gw.jar <path | path_pattern>
```

Examples (Windows):

```bat
java -jar gw.jar C:\projects\project
java -jar gw.jar -r C:\projects\project src\project
java -jar gw.jar -r C:\projects\project "glob:**/*.java"
java -jar gw.jar -r C:\projects\project "regex:^.*\/[^\/]+\.java$"
```

### Typical Workflow

1. Download and unzip the Ghostwriter distribution.
2. (Optional) Create `gw.properties` in the same directory as the executable to set defaults (e.g., `root`, `genai`, `instructions`, `excludes`).
3. Run Ghostwriter against a directory (or pattern) under the chosen root.
4. Provide additional instructions via `--instructions` (URL/file) or interactively via stdin.
5. Review logs and generated changes; repeat in CI as needed.
