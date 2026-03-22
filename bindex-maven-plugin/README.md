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

# Bindex Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

Bindex Maven Plugin enables automated generation and registration of **Bindex metadata** for Maven projects. This metadata supports **library discovery, integration, and assembly** by providing structured information that can be indexed and searched (including GenAI-powered semantic search) within the Machanism ecosystem.

## Installation Instructions

### Prerequisites

- Java: built with `maven.compiler.release=8` (Java 8 bytecode)
- Apache Maven
- Network access to a Bindex registry endpoint (only for the `register` goal)
- A configured AI provider/model compatible with Machanism (when semantic processing is enabled by your configuration)

### Checkout and Build

```bash
git clone https://github.com/machanism-org/machai.git
cd machai/bindex-maven-plugin
mvn -DskipTests package
```

## Usage

This project is a Maven plugin (`packaging=maven-plugin`). It provides goals to create/update/register Bindex metadata and to clean previously generated artifacts.

### Goals

- `create`: create a new Bindex index for the current project
- `update`: refresh an existing index as the project evolves
- `register`: publish the project’s metadata to a remote registry endpoint
- `clean`: remove previously generated artifacts

### Basic usage (fully-qualified)

```bash
mvn org.machanism.machai:bindex-maven-plugin:register
```

If you have configured plugin groups in your Maven settings, you may also be able to run:

```bash
mvn bindex:register
```

### Configuration examples

Set a custom registry URL when registering:

```bash
mvn org.machanism.machai:bindex-maven-plugin:register -Dbindex.register.url=http://localhost:8080
```

To run `mvn bindex:register`, use:

```bash
set "MAVEN_OPTS=--add-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming"
mvn bindex:register
```

## Resources

- Documentation: https://machai.machanism.org/bindex-maven-plugin/index.html
- Source (monorepo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin
