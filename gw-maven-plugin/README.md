<!--
@guidance:
**ADD FOLLOWING SECTIONS TO THIS README FILE:**
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Use `src/site/markdown/index.md` as the primary source of information for generating the project description. Summarize and adapt its content as needed for clarity and conciseness.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])` after the title as a new paragraph. [groupId] and [artifactId] need to use from pom.xml.
2. **Installation Instructions:**  
   - Describe how to checkout the repository and build the project using Maven.
   - Include prerequisites such as Java version and build tools.
3. **Usage:**  
   - Explain how to run or use the project and its modules.
   - Provide examples of usage with configuration.
4. **Other Rules**
   - Do not use the horizontal rule separator between sections.	
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

## Project Title and Overview

GW Maven Plugin (Ghostwriter Maven Plugin) is a Maven plugin that automates keeping repository content—especially documentation—accurate and consistent by running a Ghostwriter workflow during your Maven build.

It scans a configurable directory (commonly `src\\site`) for project files (Markdown, source, and other artifacts) that contain embedded `@guidance` blocks and other instructions. Those inputs are provided to a configured GenAI provider/model, which synthesizes updates and writes the resulting changes back to your working tree.

The plugin provides two goals:

- `gw:std` — standard, per-module execution.
- `gw:gw` — aggregator/reactor execution across all modules.

## Installation Instructions

### Prerequisites

- Git
- Java 11+ (this module compiles with `maven.compiler.release=11`)
- Apache Maven 3.x

### Checkout

```cmd
git clone https://github.com/machanism-org/machai.git
cd machai
```

### Build

```cmd
mvn -U clean install
```

## Usage

### Add the plugin to your project

Add the plugin to your project `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>REPLACE_WITH_LATEST_VERSION</version>
</plugin>
```

### Run the plugin

Standard (single-module) goal:

```cmd
mvn gw:std -Dgw.genai=openai:gpt-4.1-mini
```

Aggregator (reactor) goal:

```cmd
mvn gw:gw -Dgw.genai=openai:gpt-4.1-mini
```

If credentials are stored in Maven `settings.xml`:

```cmd
mvn gw:gw -Dgw.genai=openai:gpt-4.1-mini -Dgw.genai.serverId=genai
```

### Configuration example

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>REPLACE_WITH_LATEST_VERSION</version>
  <configuration>
    <scanDir>${project.basedir}\\src\\site</scanDir>
    <excludes>
      <exclude>**\\.machai\\**</exclude>
    </excludes>
    <genai>openai:gpt-4.1-mini</genai>
    <serverId>genai</serverId>
    <threads>true</threads>
    <logInputs>false</logInputs>
  </configuration>
</plugin>
```

Command-line override example:

```cmd
mvn gw:gw -Dgw.genai=openai:gpt-4.1-mini -Dgw.genai.serverId=genai -Dgw.logInputs=true
```

### Typical workflow

1. Add or update project files (documentation under `src\\site\\markdown`, source code, etc.).
2. Add or refine embedded `@guidance` blocks in the files you want Ghostwriter to maintain.
3. Configure the plugin (in `pom.xml`) and/or pass system properties (for example `-Dgw.genai=...`).
4. (Optional) Configure GenAI credentials in Maven `settings.xml` and reference them via `gw.genai.serverId`.
5. Run `mvn gw:std` (single module) or `mvn gw:gw` (reactor/aggregator).
6. Review generated changes and commit them.
