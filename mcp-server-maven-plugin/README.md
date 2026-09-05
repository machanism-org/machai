<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Use `src/site/markdown/index.md` as the primary source of information for generating the project description. Summarize and adapt its content as needed for clarity and conciseness.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])` after the title as a new paragraph. [groupId] and [artifactId] need to use from pom.xml.
   - Add a clickable link to the project site: [<project name>](https://machai.machanism.org/[artifactId]/index.html).
2. **Installation Instructions:**  
   - Describe how to checkout the repository and build the project using Maven.
   - Include prerequisites such as Java version and build tools.
3. **Usage:**  
   - Explain how to run or use the project and its modules.
   - Provide examples of usage with configuration.
4. **Other Rules:**
   - Do not use the horizontal rule separator between sections.	

**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# MCP Server Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/mcp-server-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)

[MCP Server Maven Plugin](https://machai.machanism.org/mcp-server-maven-plugin/index.html) integrates a Machai Model Context Protocol (MCP) server into a Maven build. Its aggregator goals start stateless or streamable HTTP servers using the current Maven project's metadata and base directory, configured properties, port, and tools. This provides MCP clients with build-managed access to configured Machai tools without a separate launcher, making it suitable for local development, integration tests, demonstrations, and reactor-build automation.

## Installation

### Prerequisites

- Java 17 or a compatible newer JDK.
- Apache Maven.
- Git, to check out the source repository.
- An MCP configuration properties file and any credentials required by the selected Machai tools.

### Build from source

Check out the Machai repository, enter this module, and run the Maven build:

```shell
git clone https://github.com/machanism-org/machai.git
cd machai/mcp-server-maven-plugin
mvn clean verify
```

To use a released plugin without building it, invoke it by its Maven coordinates as shown below, replacing `<version>` with the desired published version.

## Usage

The plugin exposes two aggregator goals. `stateless` starts an HTTP stateless MCP server, while `streamable` starts an HTTP streamable MCP server. Run either goal from the Maven project or reactor root so that the server receives that project's name, version, and base directory. Both goals require `mcp.port`; provide `mcp.config` with a readable Machai MCP configuration file.

### Run a stateless server

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:<version>:stateless \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

### Run a streamable server

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:<version>:streamable \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

The configuration file is loaded by Machai's `PropertiesConfigurator` and defines the server and tool settings. Additional `params` supplied in the plugin configuration are copied to JVM system properties only when those properties have not already been set, allowing environment-specific values to take precedence.

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>mcp-server-maven-plugin</artifactId>
  <version>&lt;version&gt;</version>
  <configuration>
    <port>8080</port>
    <configFile>/path/to/mcp.properties</configFile>
    <params>
      <example.setting>value</example.setting>
    </params>
  </configuration>
</plugin>
```

After the server starts, connect an MCP-compatible HTTP client to the configured endpoint and use the tools enabled by the configuration. When work is complete, the `stop-mcp-server` function tool can request an orderly shutdown; its optional `exit-code` parameter defaults to `0`.

## Configuration reference

| Parameter | Maven property | Purpose |
| --- | --- | --- |
| `port` | `mcp.port` | Required HTTP listening port. |
| `configFile` | `mcp.config` | MCP configuration file to load. |
| `params` | — | Additional system properties, applied only when not already defined. |
| `basedir` | — | Maven base directory passed to the server as the project directory. |

## Resources

- [Project site](https://machai.machanism.org/mcp-server-maven-plugin/index.html)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
