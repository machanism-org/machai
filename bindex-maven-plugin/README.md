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

The **Bindex Maven Plugin** generates and (optionally) registers **bindex metadata** for Maven projects. During a Maven build, it inspects the current Maven project model (coordinates, packaging, dependencies, and related descriptors) and emits structured, machine-readable metadata that can be indexed locally and, when enabled, published to a registry for cross-project discovery.

Downstream tooling (including components in the Machanism ecosystem) can use this metadata to improve library discoverability, enrich dependency analysis, and enable GenAI-assisted semantic search and automated assembly workflows.

## Installation Instructions

### Prerequisites

- Git
- Java 8+ (JDK)
- Maven 3.x

### Checkout and build

This module lives in the `machai` monorepo.

```cmd
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -pl bindex-maven-plugin -am clean install
```

## Usage

### Add the plugin to your project

Configure the plugin in your project `pom.xml` and bind a goal to a lifecycle phase:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-maven-plugin</artifactId>
      <version><!-- use the latest release from Maven Central --></version>
      <executions>
        <execution>
          <id>bindex-create</id>
          <phase>verify</phase>
          <goals>
            <goal>create</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Run the plugin

Run a normal Maven lifecycle (for example, `verify`) and the plugin will execute where you bound it:

```cmd
mvn verify
```

You can also invoke a goal directly:

```cmd
mvn org.machanism.machai:bindex-maven-plugin:create -Dbindex.genai=OpenAI:gpt-5
```

### Typical workflow

1. Configure the plugin in your `pom.xml` (or invoke a goal directly).
2. Run your standard Maven lifecycle (`mvn verify`, `mvn package`, etc.).
3. Use `create` (first run) or `update` (subsequent runs) to generate/refresh the project’s bindex metadata.
4. (Optional) Use `register` to publish the metadata to a registry endpoint.
5. Use downstream tooling to search, analyze, or assemble artifacts based on the indexed metadata.

### Configuration examples

Example using command-line properties:

```cmd
mvn org.machanism.machai:bindex-maven-plugin:register -Dbindex.genai=OpenAI:gpt-5 -Dbindex.register.url=http://localhost:8080
```

Common configuration parameters:

| Parameter | Description | Default |
|---|---|---|
| `bindex.genai` | AI provider/model identifier used for indexing (for example `OpenAI:gpt-5`). | (required) |
| `bindex.register.url` | Registry endpoint URL used by the `register` goal. | (none) |
| `update` | When `true`, `register` performs an update while registering. | `true` |
