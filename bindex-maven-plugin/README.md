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

The **Bindex Maven Plugin** automates the generation and (optionally) registration of **bindex metadata** for Maven projects. This machine-readable metadata enables downstream tools to index and query project outputs to improve library discovery, dependency analysis, and automated integration/assembly workflows—especially when paired with Machanism tooling and GenAI-powered semantic search.

## Installation Instructions

### Prerequisites

- Git
- Java 8+ (JDK)
- Maven 3.x

### Checkout and build

```cmd
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -pl bindex-maven-plugin -am clean install
```

## Usage

### Run the plugin

If the plugin is configured in your project `pom.xml`, run a normal Maven lifecycle (for example, `verify`) and the plugin will execute where you bound it:

```cmd
mvn verify
```

You can also invoke the goal directly:

```cmd
mvn org.machanism.machai:bindex-maven-plugin:bindex
```

### Configure the plugin

Add the plugin to your project `pom.xml` and (optionally) bind the goal to a lifecycle phase:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-maven-plugin</artifactId>
      <version><!-- use the latest release from Maven Central --></version>
      <executions>
        <execution>
          <id>generate-bindex</id>
          <phase>generate-resources</phase>
          <goals>
            <goal>bindex</goal>
          </goals>
          <configuration>
            <update>true</update>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Configuration examples

Example (system property):

```cmd
mvn org.machanism.machai:bindex-maven-plugin:bindex -Dupdate=true
```

Common configuration parameters (names may vary by plugin version):

| Parameter | Description | Default |
|---|---|---|
| `outputDirectory` | Where generated bindex metadata is written. | Plugin-defined |
| `skip` | Skips bindex generation when `true`. | `false` |
| `register` | Registers/publishes generated metadata when `true`. | `false` |
| `update` | Update an existing `bindex.json` if present. | `false` |
