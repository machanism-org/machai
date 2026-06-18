---
<!-- @guidance:
Create a web page content that guides users on how to start the MCP (Model Context Protocol) server to enable Bindex functionality.  
The page should include:
- An introduction explaining what the MCP server is, how it relates to Bindex, and its role in governance and observability for AI tools.
- Step-by-step instructions for starting the MCP server, including prerequisites (such as Java version, required files, and configuration).
- Example command-line usage for launching the server.
- Description of the Bindex functionality provided by the MCP server (such as metadata retrieval, library picking, and record registration).
- Information about integrating public Bindex tools with the MCP server, referencing the setup and configuration methods described at [Machai MCP Server documentation](https://machai.machanism.org/machai-mcp-server/index.html).  
  - Provide a link to a description of the available tools [Functional Tools](https://machai.machanism.org/bindex-core/functional-tools.html).
  - Download links for command line run:
  	- [machai-mcp-server.jar](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/).
  	- [bindex-core.jar](https://sourceforge.net/projects/machanism/files/machai/bindex-core/releases/).
  - Specify that functional tools are published using the Machai MCP server by adding a bindex-core jar file. e.g.: 
  ```
  	java -DGENAI_PASSWORD=<password> -DGENAI_USERNAME=<user_name> \
  	     -cp machai-mcp-server.jar;bindex-core.jar \
  	     -Dembedding.model=CodeMie:text-embedding-005 \
  	     -Dgw.model=CodeMie:gpt-5.4-2026-03-05 \
  	     org.machanism.machai.mcp.server.McpServer \
  	     -p 45000
  ```
  - List examples of tools that can be integrated, such as Claude Desktop, Cursor, Windsurf, VS Code (GitHub Copilot), JetBrains IDEs, Zed, and Warp.
- Instructions for Maven project integration using the [MCP Server Maven Plugin](https://machai.machanism.org/mcp-server-maven-plugin/index.html):
  - Describe how to add the plugin to a Maven project. e.g. (withot execution configuration):
  ```
	<plugin>
		<groupId>org.machanism.machai</groupId>
		<artifactId>mcp-server-maven-plugin</artifactId>
		<configuration>
			<port>45000</port>
			<params>
				<gw.model>CodeMie:gpt-5.4-2026-03-05</gw.model>
				<embedding.model>CodeMie:text-embedding-005</embedding.model>
				<GENAI_PASSWORD>[password]</GENAI_PASSWORD>
				<GENAI_USERNAME>[user_name]</GENAI_USERNAME>
			</params>
		</configuration>
		<dependencies>
			<dependency>
				<groupId>org.machanism.machai</groupId>
				<artifactId>bindex-core</artifactId>
				<version>[VERSION]</version>
			</dependency>
		</dependencies>
	</plugin>  
  ```
  - Provide example configuration snippets for the plugin.
  - Explain how the plugin can be used to automate MCP server startup and Bindex tool invocation as part of the Maven build lifecycle.
  - Highlight benefits such as CI/CD integration, repeatable automation, and project-wide governance.
- Tips for troubleshooting common issues.
- Links to relevant documentation, downloads, and support resources.
- Clear, user-friendly formatting with headings, code blocks, and actionable guidance.

The content should be suitable for both new and experienced users, helping them quickly understand and activate Bindex features via the MCP server, 
leverage public Bindex tools, and integrate MCP server functionality into Maven-based projects for enhanced automation and governance.
-->
canonical: https://machai.machanism.org/bindex-core/bindex-mcp-server.html
---

# Bindex MCP Server

The Bindex MCP server lets MCP-compatible AI tools access Bindex functionality through the Model Context Protocol (MCP). In this setup, the Machai MCP server hosts and publishes Bindex functional tools, while Bindex provides the domain-specific capabilities behind those tools.

This approach gives teams a practical way to expose Bindex operations to AI assistants and development environments with stronger governance and observability. Instead of embedding Bindex behavior separately in each client, you can run a central MCP server that controls configuration, publishing, access patterns, and runtime visibility.

## What the MCP server does for Bindex

When Bindex is published through the Machai MCP server, MCP clients can discover and invoke Bindex-related tools for tasks such as:

- Retrieving metadata about libraries and other indexed assets.
- Picking relevant libraries for a task, dependency, or implementation context.
- Registering records so they can be searched, reused, and governed later.
- Supporting governance and observability for AI-driven tool usage across teams and environments.

To review the public tools that can be exposed, see [Functional Tools](https://machai.machanism.org/bindex-core/functional-tools.html).

## Prerequisites

Before starting the MCP server, make sure you have the following in place:

- A supported Java runtime installed and available on your `PATH`.
- Access to the required JAR files.
- Model configuration values for the AI services you want the server to use.
- Network connectivity to any configured model providers.
- An available TCP port, such as `45000`.

### Required files

For command-line startup, you typically need these files in the same working directory:

- `machai-mcp-server.jar`
- `bindex-core.jar`

Download them here:

- [machai-mcp-server.jar](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/)
- [bindex-core.jar](https://sourceforge.net/projects/machanism/files/machai/bindex-core/releases/)

## Start the MCP server

### Step 1: Download the required JAR files

Download `machai-mcp-server.jar` and `bindex-core.jar` into a working directory.

### Step 2: Verify Java is installed

Check that Java is available:

```bash
java -version
```

If Java is not found, install a supported Java runtime and make sure `java` is available from your shell.

### Step 3: Prepare configuration values

Gather the configuration values needed for your environment, for example:

- `GENAI_USERNAME`
- `GENAI_PASSWORD`
- `embedding.model`
- `gw.model`
- The server port

### Step 4: Launch the server

Functional tools are published using the Machai MCP server by adding `bindex-core.jar` to the classpath.

Example:

```bash
java -DGENAI_PASSWORD=<password> -DGENAI_USERNAME=<user_name> \
     -cp machai-mcp-server.jar;bindex-core.jar \
     -Dembedding.model=CodeMie:text-embedding-005 \
     -Dgw.model=CodeMie:gpt-5.4-2026-03-05 \
     org.machanism.machai.mcp.server.McpServer \
     -p 45000
```

On Windows, `;` is the normal classpath separator. On Unix-like systems, replace `;` with `:` if needed.

### Step 5: Connect an MCP-compatible client

After the server is running, configure your MCP-compatible client to connect to it. For setup patterns, transport details, and client configuration guidance, see the [Machai MCP Server documentation](https://machai.machanism.org/machai-mcp-server/index.html).

Examples of tools that can be integrated include:

- Claude Desktop
- Cursor
- Windsurf
- VS Code with GitHub Copilot
- JetBrains IDEs
- Zed
- Warp

## Example command-line usage

### Minimal startup example

```bash
java -cp machai-mcp-server.jar;bindex-core.jar \
     org.machanism.machai.mcp.server.McpServer \
     -p 45000
```

### Startup example with model configuration

```bash
java -DGENAI_PASSWORD=<password> \
     -DGENAI_USERNAME=<user_name> \
     -Dembedding.model=CodeMie:text-embedding-005 \
     -Dgw.model=CodeMie:gpt-5.4-2026-03-05 \
     -cp machai-mcp-server.jar;bindex-core.jar \
     org.machanism.machai.mcp.server.McpServer \
     -p 45000
```

## Bindex functionality available through the MCP server

By publishing `bindex-core.jar` through the Machai MCP server, you make Bindex functional tools available to MCP clients. Depending on your configuration and exposed tool set, users can work with capabilities such as:

- Metadata retrieval for libraries and indexed records.
- Library picking to help select relevant libraries for a given problem or codebase.
- Record registration so useful assets and findings can be stored for later discovery.
- Shared, governed access to Bindex capabilities across multiple MCP-compatible clients.

This gives organizations a central way to expose Bindex behavior while improving auditability, consistency, and operational visibility.

## Integrating public Bindex tools with the MCP server

Public Bindex tools are published by the Machai MCP server when `bindex-core.jar` is included on the classpath. This allows MCP clients to discover and invoke Bindex tools from a shared runtime.

For client setup and configuration methods, refer to the [Machai MCP Server documentation](https://machai.machanism.org/machai-mcp-server/index.html).

For a description of the available Bindex tools, see [Functional Tools](https://machai.machanism.org/bindex-core/functional-tools.html).

A typical command-line launch looks like this:

```bash
java -DGENAI_PASSWORD=<password> -DGENAI_USERNAME=<user_name> \
     -cp machai-mcp-server.jar;bindex-core.jar \
     -Dembedding.model=CodeMie:text-embedding-005 \
     -Dgw.model=CodeMie:gpt-5.4-2026-03-05 \
     org.machanism.machai.mcp.server.McpServer \
     -p 45000
```

This pattern is especially useful when you want one MCP server to publish a shared set of public Bindex tools for multiple users or client applications.

## Maven project integration

If you want Maven-based automation, use the [MCP Server Maven Plugin](https://machai.machanism.org/mcp-server-maven-plugin/index.html). It can start the MCP server as part of your build process and include Bindex tools through a plugin dependency.

### Add the plugin to a Maven project

The following example shows how to add the plugin without execution configuration:

```xml
<plugin>
	<groupId>org.machanism.machai</groupId>
	<artifactId>mcp-server-maven-plugin</artifactId>
	<configuration>
		<port>45000</port>
		<params>
			<gw.model>CodeMie:gpt-5.4-2026-03-05</gw.model>
			<embedding.model>CodeMie:text-embedding-005</embedding.model>
			<GENAI_PASSWORD>[password]</GENAI_PASSWORD>
			<GENAI_USERNAME>[user_name]</GENAI_USERNAME>
		</params>
	</configuration>
	<dependencies>
		<dependency>
			<groupId>org.machanism.machai</groupId>
			<artifactId>bindex-core</artifactId>
			<version>[VERSION]</version>
		</dependency>
	</dependencies>
</plugin>
```

### Example plugin configuration with execution

You can also bind the plugin to the Maven build lifecycle so the MCP server starts automatically during local builds or CI/CD runs.

```xml
<plugin>
    <groupId>org.machanism.machai</groupId>
    <artifactId>mcp-server-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>start-mcp-server</id>
            <goals>
                <goal>start</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <port>45000</port>
        <params>
            <gw.model>CodeMie:gpt-5.4-2026-03-05</gw.model>
            <embedding.model>CodeMie:text-embedding-005</embedding.model>
            <GENAI_PASSWORD>[password]</GENAI_PASSWORD>
            <GENAI_USERNAME>[user_name]</GENAI_USERNAME>
        </params>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.machanism.machai</groupId>
            <artifactId>bindex-core</artifactId>
            <version>[VERSION]</version>
        </dependency>
    </dependencies>
</plugin>
```

### How the Maven plugin helps

The plugin can be used to automate MCP server startup and Bindex tool publication as part of the Maven build lifecycle. This is useful for:

- Repeatable local development workflows.
- CI/CD integration where the same MCP setup must be started consistently.
- Project-wide governance through standardized server configuration.
- Reduced manual setup when multiple developers or pipelines need the same Bindex-enabled MCP runtime.

## Troubleshooting tips

If something does not work as expected, check the following:

- **Java not available**: run `java -version` and verify your Java installation and `PATH`.
- **Port conflicts**: change the configured port or stop the process already using it.
- **Authentication errors**: confirm that `GENAI_USERNAME` and `GENAI_PASSWORD` are set correctly.
- **Classpath problems**: verify that both `machai-mcp-server.jar` and `bindex-core.jar` exist and are referenced correctly.
- **Wrong classpath separator**: use `;` on Windows and `:` on Unix-like systems.
- **Client connection issues**: ensure the MCP client is configured to connect to the running server using the correct transport and endpoint settings.
- **Missing Bindex tools**: confirm that `bindex-core.jar` is included so the server can publish the Bindex functional tools.

## Documentation, downloads, and support resources

- [Machai MCP Server documentation](https://machai.machanism.org/machai-mcp-server/index.html)
- [MCP Server Maven Plugin](https://machai.machanism.org/mcp-server-maven-plugin/index.html)
- [Functional Tools](https://machai.machanism.org/bindex-core/functional-tools.html)
- [machai-mcp-server.jar downloads](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/)
- [bindex-core.jar downloads](https://sourceforge.net/projects/machanism/files/machai/bindex-core/releases/)

## Quick start summary

1. Download `machai-mcp-server.jar` and `bindex-core.jar`.
2. Verify Java is installed.
3. Start the Machai MCP server with `bindex-core.jar` on the classpath.
4. Configure your MCP-compatible client.
5. Test Bindex features such as metadata retrieval, library picking, and record registration.
6. Add the Maven plugin if you want repeatable project-level automation and governance.
