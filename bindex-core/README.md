<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-mcp-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
   - Add the Ghostwriter CLI application jar download link: [![Download Bindex Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex-core/releases/) to the installation section.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# Bindex Core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-mcp-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)

Bindex Core is the library-indexing and library-discovery component of the Machai platform. It provides a consistent way to describe software libraries as structured Bindex metadata, validate and register that metadata, and retrieve suitable libraries for a natural-language development request.

## Introduction

Bindex Core combines schema-based metadata, generated embeddings, semantic vector search, classification filters, and a MongoDB-backed repository. Its Java API supports application code and AI tool integrations: an AI agent can recommend libraries, inspect a complete or GraphQL-filtered descriptor, register metadata from JSON, files, or URLs, and obtain the Bindex schema or generation prompt. This reduces duplicate implementation work, improves dependency selection, and makes reusable capabilities discoverable across projects.

## Overview

A Bindex record captures a library's coordinates, version, purpose, classification, integrations, dependencies, examples, and configuration guidance. The workflow is:

1. Assemble a schema-compliant descriptor from a library's documentation and build metadata.
2. Convert the descriptor's classification into an embedding and store it with searchable metadata.
3. Classify a user's request with a configured GenAI provider and convert it into a query embedding.
4. Narrow semantic search by language and architectural layer, apply a score threshold, and select the most useful version of each library.
5. Use the selected descriptors to guide implementation, assembly, or further dependency resolution.

The architecture separates AI-facing tools from the domain workflow and persistence layer. Tool operations provide the external contract; the picker coordinates classification, embeddings, and recommendations; repository implementations manage storage and vector queries; and generated schema classes preserve a typed metadata model. The project structure is illustrated in the [C4 project structure diagram](src/site/images/c4-diagram.png).

## Key Features

- Schema-compliant Bindex v2 metadata with practical installation and usage examples.
- Natural-language library recommendations powered by configurable GenAI and embedding providers.
- MongoDB persistence with exact vector search, classification filters, score thresholds, and version selection.
- Registration from a Bindex object, a project-relative JSON file, or a remote URL.
- Retrieval by coordinates or URL, with optional GraphQL-style field filtering to reduce response size.
- AI function tools for discovery, metadata access, registration, schema retrieval, and Bindex-generation prompts.
- Recursive dependency resolution and language-name normalization for reliable matching.
- Maven integration and an assembled artifact profile for distribution.

## Usage

### Installation

Bindex Core is assembled for use with the [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server). It is included by default in the [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download).

[![Download Bindex Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex-core/releases/)

If you use `gw-maven-plugin`, add Bindex Core as a plugin dependency:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>RELEASE</version>
  <!-- other plugin configuration -->
  <dependencies>
    <dependency>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-core</artifactId>
      <version>RELEASE</version>
    </dependency>
  </dependencies>
</plugin>
```

For direct Maven use, declare the dependency in the consuming project:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>bindex-core</artifactId>
  <version>RELEASE</version>
</dependency>
```

The AI-facing operations are exposed as `get_bindex`, `pick_libraries`, `register_bindex`, and `register_bindex_json`. Configure the GenAI and embedding providers through the host application's `Configurator`; repository connections can be customized with the parameters listed below.

## Built-In Acts

The following acts support repeatable Bindex and implementation workflows.

### `assembly`

Uses Bindex library recommendations to help an AI software engineer implement a user task. Use it when a task requires selecting existing libraries, creating or updating a project, building it, and documenting the result.

### `bindex`

Coordinates Bindex generation for a non-parent Maven project. Use it to produce schema-compliant metadata from documentation and effective build information, validate the descriptor, and register it.

### `bindex/java/extract-javadoc`

Extracts a complete, standalone Markdown report from generated Java Javadoc HTML. Use it when API documentation must be supplied as authoritative input for Bindex generation.

### `bindex/java/mvn-project`

Builds Javadoc for a Maven project and uses the reports, site Markdown, effective POM, and generation rules to create and validate `bindex.json`. Use it as the Maven-specific implementation stage of the Bindex workflow.

### `bindex/register`

Determines whether the current project is a supported non-parent Maven project and delegates Bindex generation to the Maven workflow. Use it as the entry point for registering metadata.

### `pick`

Selects libraries relevant to a user's query through Bindex recommendations. Use it when planning a new implementation or extending an existing project and suitable reusable libraries need to be identified before coding.

## Configuration

| Parameter | Description | Default value |
|---|---|---|
| `gw.model` | GenAI model used by the picker when `pick.model` is not set. | Host/application-defined. |
| `pick.model` | Model override used specifically for classifying library-selection requests. | Falls back to `gw.model`. |
| `embedding.model` | Embedding provider model used to encode classifications for semantic search. | Host/application-defined. |
| `picker.classificationInstruction` | Custom instruction template for producing classification JSON. | Built-in classification instruction. |
| `BINDEX_REPO_URL` | MongoDB connection URI for the Bindex repository. | `mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0`. |
| `BINDEX_USER` | MongoDB username when authentication is required. | Not set. |
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
