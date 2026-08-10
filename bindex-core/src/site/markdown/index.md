---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/bindex.json)
2. Introduction
   - Provide a comprehensive description of the project, including its purpose and benefits.
   - Analyze java files in the `src/main/java` to inform the description.
3. Overview
   - Clearly explain the main functions and value proposition of the project.
   - Summarize how the project workflows and documentation.
   Describe the project with diagrams bellow:
     - Create a project structure overview based on the `.puml` files below.
     - Describe the project without including file names in the description.
     - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
4. Key Features
   - Present a bulleted list of the primary capabilities and unique features of the project.
5. How to use
   - Assembled MCP Bndex server [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server).
   - This library is included in [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download) as default. 
   - If you use `gw-maven-plugin`, you need to add this library by the dependency, e.g:
   ```xml
	<plugin>
		<groupId>org.machanism.machai</groupId>
		<artifactId>gw-maven-plugin</artifactId>
		<version>1.1.1</version>
		...
		<dependencies>
			<dependency>
				<groupId>org.machanism.machai</groupId>
				<artifactId>bindex-core</artifactId>
				<version>1.1.1</version>
			</dependency>
		</dependencies>
	</plugin>
  ```  
6. Built-In Acts
	- Analyze all act TOML files by glob pattern: `src/main/resources/acts/**/*.toml`.
	- For each act, create a section that includes:
		 - The act's name.
		 - A clear, concise description of the act's purpose and when it should be used.
	- Organize your output so that each act is easy to identify and understand.
	- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
7. Configuration
   - Include a table of common configuration parameters, with columns for parameter name, description, and default value.
   - Ensure descriptions are clear and concise.
8. Throubleshooting
   - Possible to add the following command-line argument to your Java startup command or environment variables: `--add-exports jdk.naming.dns/com.sun.jndi.dns=java.naming`.
9. Resources
   - Provide a list of relevant links, including:
     - Official platform or documentation site
     - GitHub repository
     - Maven Central page
     - Any other useful resources
# General Instructions     
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, tables, code blocks, and links.
- Ensure clarity, conciseness, and easy navigation throughout the page.
-->
canonical: https://machai.machanism.org/bindex-core/index.html
---

# Bindex Core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-core.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/bindex.json)

## Introduction

**Bindex Core** is the library-indexing and library-discovery component of the Machai platform. It provides a consistent way to describe software libraries as structured Bindex metadata, validate and register that metadata, and retrieve suitable libraries for a natural-language development request.

The library combines schema-based metadata, generated embeddings, semantic vector search, classification filters, and a MongoDB-backed repository. Its Java API supports both application code and AI tool integrations: an AI agent can recommend libraries, inspect a complete or GraphQL-filtered descriptor, register metadata from JSON/files/URLs, and obtain the Bindex schema or generation prompt. This reduces duplicate implementation work, improves dependency selection, and makes reusable capabilities discoverable across projects.

## Overview

A Bindex record captures a library's coordinates, version, purpose, classification, integrations, dependencies, examples, and configuration guidance. The workflow is:

1. A library's documentation and build metadata are assembled into a schema-compliant descriptor.
2. The descriptor's classification is converted into an embedding and stored with searchable metadata.
3. A user request is classified by a configured GenAI provider and converted into a query embedding.
4. Semantic search is narrowed by language and architectural layer, filtered by a score threshold, and reduced to the most useful version of each library.
5. The selected descriptors can then guide implementation, assembly, or further dependency resolution.

The architecture separates AI-facing tools from the domain workflow and persistence layer. Tool operations provide the external contract; the picker coordinates classification, embeddings, and recommendations; repository implementations manage storage and vector queries; and generated schema classes preserve a typed metadata model. The project structure is illustrated in the [C4 project structure diagram](./images/c4-diagram.png).

## Key Features

- Schema-compliant Bindex v2 metadata with practical installation and usage examples.
- Natural-language library recommendations powered by configurable GenAI and embedding providers.
- MongoDB persistence with exact vector search, classification filters, score thresholds, and version selection.
- Registration from a Bindex object, a project-relative JSON file, or a remote URL.
- Retrieval by coordinates or URL, with optional GraphQL-style field filtering to reduce response size.
- AI function tools for discovery, metadata access, registration, schema retrieval, and Bindex-generation prompts.
- Recursive dependency resolution and language-name normalization for reliable matching.
- Maven integration and an assembled artifact profile for distribution.

## How to use

Bindex Core is assembled for use with the [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server). It is included by default in the [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download).

If you use `gw-maven-plugin`, add Bindex Core as a plugin dependency:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>1.1.1</version>
  <!-- other plugin configuration -->
  <dependencies>
    <dependency>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-core</artifactId>
      <version>1.1.1</version>
    </dependency>
  </dependencies>
</plugin>
```

For direct Maven use, declare the dependency in the project that consumes the library:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>bindex-core</artifactId>
  <version>1.1.1</version>
</dependency>
```

The AI-facing operations are exposed as `get_bindex`, `pick_libraries`, `register_bindex`, and `register_bindex_json`. Configure the GenAI provider and embedding provider through the host application's `Configurator`; repository connections can be customized with the parameters listed below.

## Built-In Acts

The following acts are defined under `src/main/resources/acts/**/*.toml` and support repeatable Bindex and implementation workflows.

### `assembly`

Uses Bindex library recommendations to help an AI software engineer implement a user task. Use it when a task requires selecting existing libraries, creating or updating a project, building it, and documenting the result.

### `bindex`

Coordinates Bindex generation for a non-parent Maven project. Use it to select the Maven project workflow, produce schema-compliant metadata from documentation and effective build information, validate the resulting descriptor, and register it.

### `bindex/java/extract-javadoc`

Extracts a complete, standalone Markdown report from generated Java Javadoc HTML. Use it when API documentation must be supplied as authoritative input for Bindex generation, including class metadata, inheritance, constructors, methods, signatures, and member descriptions.

### `bindex/java/mvn-project`

Builds Javadoc for a Maven project and uses the reports, site Markdown, effective POM, and generation rules to create and validate `bindex.json`. Use it as the Maven-specific implementation stage of the Bindex workflow.

### `pick`

Selects libraries relevant to a user's query through Bindex recommendations. Use it when planning a new implementation or extending an existing project and suitable reusable libraries need to be identified before coding.

## Configuration

| Parameter | Description | Default value |
|---|---|---|
| `gw.model` | GenAI model used for classification and general AI operations. | Host/application-defined; acts commonly use `CodeMie:gpt-5.4-2026-03-05`. |
| `pick.model` | Optional model override used specifically by the picker. | Falls back to `gw.model`. |
| `embedding.model` | Embedding provider model used to encode classifications for semantic search. | Host/application-defined; acts commonly use `CodeMie:text-embedding-005`. |
| `picker.classificationInstruction` | Custom instruction template for producing classification JSON; it receives the schema and user query as format arguments. | Built-in classification instruction. |
| `BINDEX_REPO_URL` | MongoDB connection URI for the Bindex repository. | The configured default cluster URI. |
| `BINDEX_USER` | MongoDB username when authentication is required. | Not set; public defaults are used with the default URI. |
| `BINDEX_PASSWORD` | MongoDB password used to authenticate to the repository. | Not set. |
| `vectorSearchLimits` / `search_limits` | Maximum number of vector-search candidates or recommendations. | `25` for the AI tool. |
| `score` | Minimum semantic similarity score for returned recommendations. | `0.85` for the AI tool. |

## Troubleshooting

If Java cannot access the JDK DNS implementation while starting the application, add the following JVM argument to the Java startup command or configure it through the environment used to launch Java:

```text
--add-exports jdk.naming.dns/com.sun.jndi.dns=java.naming
```

Also verify that the configured MongoDB URI and credentials are reachable, that the embedding model produces vectors compatible with the repository's vector index, and that the Bindex descriptor validates against the [Bindex v2 schema](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json).

## Resources

- [Machai official platform site](https://machai.machanism.org/)
- [Bindex Core documentation](https://machai.machanism.org/bindex-core/index.html)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server)
- [Bindex Core on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)
- [Bindex metadata schema](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json)
- [Ghostwriter CLI download](https://machai.machanism.org/ghostwriter/index.html#Download)
