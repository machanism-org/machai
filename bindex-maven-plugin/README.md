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

The **Bindex Maven Plugin** enables automated generation and registration of `bindex` metadata for Maven projects. It generates `bindex.json` for a Maven module (and can optionally update an existing file) to keep structured metadata aligned with project changes, supporting discovery, automation, and indexing workflows in the Machanism ecosystem.

## Installation Instructions

### Prerequisites

- Git
- Java 11+
- Maven 3.6+

### Checkout and build

```bash
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -pl bindex-maven-plugin -am clean install
```

## Usage

### Run the plugin

Generate (or update) `bindex.json` for the current module:

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex
```

### Typical workflow

1. Add/configure the plugin in your project `pom.xml` (optionally bind it to a lifecycle phase).
2. Run `mvn org.machanism.machai:bindex-maven-plugin:bindex` to generate or update `bindex.json`.
3. Commit `bindex.json` if your repository policy expects generated metadata to be versioned.
4. Use the generated descriptor with any downstream tooling that consumes `bindex.json`.

### Configure the plugin

Add the plugin to your project `pom.xml` and (optionally) bind the goal to a lifecycle phase.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-maven-plugin</artifactId>
      <version>${machai.version}</version>
      <executions>
        <execution>
          <id>generate-bindex</id>
          <phase>package</phase>
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

### Configuration

| Parameter | Description | Default |
|---|---|---|
| `update` | Whether to update an existing `bindex.json` if present. | `false` |

Example (system property):

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex -Dupdate=false
```
