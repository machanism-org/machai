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

Ghostwriter Maven Plugin (GW Maven Plugin) is a documentation automation tool for Maven-based Java projects. It scans a repository for embedded `@guidance:` directives and uses them to assemble and refresh Maven Site Markdown pages, helping teams keep documentation accurate and consistent as the code evolves.

Key features:

- Generates and refreshes Maven Site pages from embedded `@guidance:` comments
- Scans project sources and resources to discover `@guidance:` directives
- Integrates with standard Maven Site layout and workflows
- Can be run from the command line or bound into the Maven lifecycle

## Installation Instructions

### Prerequisites

- Git
- Java 11+
- Maven 3+

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

Run the plugin goal:

```bat
mvn org.machanism.machai:gw-maven-plugin:REPLACE_WITH_LATEST_VERSION:gw
```

### Configuration example

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>REPLACE_WITH_LATEST_VERSION</version>
  <configuration>
    <genai>CodeMie:gpt-5-2-2025-12-11</genai>
    <serverId>CodeMie</serverId>
  </configuration>
</plugin>
```

Command-line override example:

```bat
mvn org.machanism.machai:gw-maven-plugin:REPLACE_WITH_LATEST_VERSION:gw -Dgw.genai=CodeMie:gpt-5-2-2025-12-11 -Dgw.serverId=CodeMie
```

### Typical workflow

1. Add `@guidance:` comments close to the code or artifacts they describe.
2. Run the plugin goal to (re)generate or update the Maven Site Markdown pages.
3. Run `mvn site` to render the site and review the generated documentation.
4. Iterate: update code and `@guidance:` comments as requirements evolve, then re-run the goals.
