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

GW Maven Plugin ("Ghostwriter Maven Plugin") is a documentation automation plugin for Maven-based Java projects. It scans documentation sources (typically under `src\site`) and runs them through the MachAI Generative Workflow (GW) pipeline to generate or update project documentation from embedded guidance tags and project context.

Key features:

- Maven goals to run the MachAI GW documentation pipeline from your build
- Scans documentation sources (commonly `src\site`) with an optional scan root override
- Supports additional instructions and default guidance to control output style/content
- Supports exclude patterns for skipping directories/files during scanning
- Can load GenAI credentials from Maven `settings.xml` via a `<server>` id
- Aggregator execution for reactor builds, with optional multi-threading
- Optional logging of the input file set passed to the workflow

## Installation Instructions

### Prerequisites

- Git
- Java 11+ (project compiles with `maven.compiler.release=11`)
- Maven 3.x

### Checkout

```bat
git clone https://github.com/machanism-org/machai.git
cd machai
```

### Build

```bat
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

Aggregator (reactor) goal:

```bat
mvn org.machanism.machai:gw-maven-plugin:REPLACE_WITH_LATEST_VERSION:gw -Dgw.genai=OpenAI:gpt-5
```

Standard (single-module) goal:

```bat
mvn gw:std -Dgw.genai=OpenAI:gpt-5
```

If credentials are stored in Maven `settings.xml`:

```bat
mvn org.machanism.machai:gw-maven-plugin:REPLACE_WITH_LATEST_VERSION:gw -Dgw.genai=OpenAI:gpt-5 -Dgw.genai.serverId=genai
```

### Configuration example

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>REPLACE_WITH_LATEST_VERSION</version>
  <configuration>
    <scanDir>${basedir}</scanDir>
    <excludes>
      <exclude>**\\.machai\\**</exclude>
    </excludes>
    <genai>OpenAI:gpt-5</genai>
    <serverId>genai</serverId>
    <threads>true</threads>
    <logInputs>false</logInputs>
  </configuration>
</plugin>
```

Command-line override example:

```bat
mvn org.machanism.machai:gw-maven-plugin:REPLACE_WITH_LATEST_VERSION:gw -Dgw.genai=OpenAI:gpt-5 -Dgw.genai.serverId=genai -Dgw.logInputs=true
```

### Typical workflow

1. Add and/or update documentation sources under `src\site` (for example, `src\site\markdown`).
2. Add embedded guidance tags in your documentation sources.
3. (Optional) Provide shared instructions and/or default guidance.
4. Configure credentials in Maven `settings.xml` and reference them via `gw.genai.serverId` if needed.
5. Run `mvn org.machanism.machai:gw-maven-plugin:gw` (reactor) or `mvn gw:std` (single module).
6. Review generated/updated documentation artifacts and commit changes.
