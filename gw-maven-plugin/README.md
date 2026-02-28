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

GW Maven Plugin is the primary Maven adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It integrates Ghostwriter’s guided file processing into Maven builds so you can generate and maintain project documentation (and other guided updates) as part of a consistent, repeatable workflow.

The plugin scans your project for files containing embedded `@guidance:` blocks and then delegates transformation/synthesis work to the Machai Ghostwriter engine, based on the [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) model.

It provides multiple Maven goals:

- **`gw:gw`**: an **aggregator** goal that can run **without a `pom.xml`** (`requiresProject=false`) and processes modules in reverse order (sub-modules first).
- **`gw:reactor`**: a **reactor-aware** goal that processes modules according to Maven reactor dependency ordering, with an option to defer the execution-root project until other reactor projects complete.
- **`gw:act`**: an **interactive** goal that runs predefined action bundles backed by resource bundles.

## Installation Instructions

### Prerequisites

- Git
- Java installed
  - **Declared build target:** Java **8** (from `pom.xml`: `maven.compiler.release=8`).
  - **Practical runtime requirement:** depends on the Ghostwriter runtime and the GenAI provider/client libraries you use; some provider stacks may require a newer Java version.
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

Guided file processing for the current directory (module order: sub-modules first):

```cmd
mvn gw:gw
```

Reactor-ordered processing in a multi-module build:

```cmd
mvn gw:reactor
```

Interactive actions:

```cmd
mvn gw:act
```

Enable multi-threaded module processing (for `gw:gw`):

```cmd
mvn gw:gw -Dgw.threads=true
```

### GenAI provider and credentials examples

Specify a GenAI provider/model:

```cmd
mvn gw:gw -Dgw.genai=openai:gpt-4.1-mini
```

If credentials are stored in Maven `settings.xml` under a `<server>` entry:

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
