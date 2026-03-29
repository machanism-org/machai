<!--
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.

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

Bindex Maven Plugin is a Maven plugin for generating, updating, and optionally publishing **Bindex metadata** for Maven modules.

It integrates the Machai/Bindex indexing workflow into your build by scanning the current project directory (using a configured GenAI provider/model) and producing a structured index that can be used for capability-based discovery, semantic search, integration, and assembly workflows across the Machanism ecosystem.

When desired, the plugin can also publish the generated metadata to an external registry endpoint so other tools and services can discover the project by its capabilities rather than only by Maven coordinates.

## Installation Instructions

### Prerequisites

- Apache Maven
- Java Development Kit (JDK)
  - Build/compile level (from `pom.xml`): `maven.compiler.release=8` (Java 8 bytecode)
  - Practical runtime requirement: Maven itself runs on a Java runtime (commonly Java 11+)

### Checkout and Build

```bash
git clone https://github.com/machanism-org/machai.git
cd machai/bindex-maven-plugin
mvn -DskipTests package
```

## Usage

This project is a Maven plugin (`packaging=maven-plugin`) with goals to create/update/register Bindex metadata and to clean temporary artifacts.

### Goals

- `create`: generate a new Bindex index for the current module
- `update`: refresh an existing Bindex index (update mode)
- `register`: optionally update and then publish Bindex metadata to a registry URL
- `clean`: remove temporary artifacts produced during indexing

### Basic usage (fully-qualified)

```bash
mvn org.machanism.machai:bindex-maven-plugin:create -Dbindex.model=OpenAI:gpt-5
```

### Register with a custom registry URL

```bash
mvn org.machanism.machai:bindex-maven-plugin:register -Dbindex.model=OpenAI:gpt-5 -Dbindex.register.url=http://localhost:8080
```

### Running via plugin group (`bindex:...`)

If you have configured plugin groups in your Maven settings, you may be able to run:

```bash
mvn bindex:register -Dbindex.model=OpenAI:gpt-5
```

Some environments require an additional JVM export to run `register`:

```bash
set MAVEN_OPTS=--add-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming
mvn bindex:register -Dbindex.model=OpenAI:gpt-5 -Dbindex.register.url=http://localhost:8080
```

## Resources

- Documentation: https://machai.machanism.org/bindex-maven-plugin/index.html
- Source (monorepo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin
- Related component (Bindex Core): https://machai.machanism.org/bindex-core/
