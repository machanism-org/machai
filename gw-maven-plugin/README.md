<!--
@guidance:
**ADD FOLLOWING SECTIONS TO THIS README FILE:**
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Use `src/site/markdown/index.md` as the primary source of information for generating the project description. Summarize and adapt its content as needed for clarity and conciseness.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])` after the title as a new paragraph. [groupId] and [artifactId] need to use from pom.xml.
2. **Installation Instructions:**  
   - The project uses SVN repository.	
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

GW Maven Plugin (Ghostwriter Maven Plugin) is a documentation automation plugin for Maven-based Java projects. It scans your repository for embedded `@guidance:` directives and uses them to generate and update Maven Site documentation, keeping docs consistent, current, and aligned with the in-repo source of truth.

## Installation Instructions

### Prerequisites

- Java 9+
- Maven 3+
- SVN client (`svn`) for repository checkout

### Checkout (SVN)

```sh
svn checkout <svn-repo-url> gw-maven-plugin
cd gw-maven-plugin
```

### Build

```sh
mvn clean install
```

## Usage

### Add the plugin to your project

Add the plugin to your project `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>${project.version}</version>
</plugin>
```

### Run the plugin

```sh
mvn gw:gw
```

### Configuration example

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>${project.version}</version>
  <configuration>
    <genai>CodeMie:gpt-5-2-2025-12-11</genai>
    <serverId>CodeMie</serverId>
  </configuration>
</plugin>
```

Command-line override example:

```sh
mvn gw:gw -Dgw.genai=CodeMie:gpt-5-2-2025-12-11
```

### Typical workflow

1. Add `@guidance:` comments close to the code or artifacts they describe.
2. Run `mvn gw:gw` to (re)generate/update the Maven Site Markdown pages.
3. Run `mvn site` to render the site and review the generated documentation.
4. Iterate: update code and `@guidance:` comments as requirements evolve, then re-run the goals.
