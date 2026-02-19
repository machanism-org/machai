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

GW Maven Plugin integrates MachAI Ghostwriter guided file processing into Maven builds, enabling documentation and other project artifacts to be generated and kept up to date as part of normal development and CI workflows.

It works by configuring a Ghostwriter `FileProcessor`, detecting the active project layout (including Maven reactor metadata when available), then scanning a configurable root directory for files containing embedded `@guidance:` instructions. Those instructions drive repeatable, consistent synthesis and updates across the repository—including source code, documentation, Maven Site content, and other relevant files.

The plugin provides two primary goals:

- **`gw:gw`**: an **aggregator** goal that can run **without a `pom.xml`** (`requiresProject=false`) and can process modules in reverse order (sub-modules first), similar to the Ghostwriter CLI.
- **`gw:reactor`**: a **reactor-aware** goal that follows standard Maven reactor dependency ordering, with an option to defer the execution-root project until the rest of the reactor has completed.

## Installation Instructions

### Prerequisites

- Git
- Java 8+
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

Basic guided processing:

```cmd
mvn gw:gw
```

Reactor/module processing:

```cmd
mvn gw:reactor
```

With a GenAI provider/model:

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
