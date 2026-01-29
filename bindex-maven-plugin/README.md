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

The **Bindex Maven Plugin** generates and maintains a `bindex.json` descriptor for a Maven module and can optionally register or publish that metadata. This keeps structured library metadata in sync with the build so it can be used for artifact discovery, GenAI-powered semantic search, and downstream assembly workflows in the Machanism ecosystem.

## Installation Instructions

### Prerequisites

- Git
- Maven 3.6.0 or later
- Java 11 or later

### Checkout and build

```bash
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -pl bindex-maven-plugin -am clean install
```

## Usage

### Run goals directly

Generate (or update) `bindex.json`:

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex
```

Register/publish the descriptor (optional):

```bash
mvn org.machanism.machai:bindex-maven-plugin:register
```

### Configure the plugin

Add the plugin to your project `pom.xml` and (optionally) bind goals to lifecycle phases.

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

Common parameters:

| Parameter | Description | Default |
|---|---|---|
| `update` | Whether to update an existing `bindex.json` if present. | `true` |

Example (disable updates):

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex -Dupdate=false
```

### Registration credentials

Registration/publishing may require credentials depending on your registry configuration.

| Environment variable | Description |
|---|---|
| `BINDEX_REG_PASSWORD` | Password/token used when writing to the registration database/registry (only required if registration is enabled). |
