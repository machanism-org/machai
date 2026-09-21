<!-- @guidance: >>> ${guidances}/readme-content.md -->
# Bindex Core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-core.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/bindex-core/refs/heads/main/bindex.json)

## Cloning and Getting Started

To clone and set up this project locally, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/machanism-org/bindex-core.git
   cd bindex-core
   ```
2. **Build the project using Maven:**
   ```bash
   mvn clean install
   ```

Bindex Core is a Java 17 library for describing reusable software components as Bindex v2 metadata and making that metadata discoverable to developers, AI agents, and MCP integrations. It validates and registers Bindex JSON descriptors, retrieves complete or field-selected metadata, and recommends libraries from natural-language requirements.

## Project Structure

Bindex Core separates AI-facing operations from workflow and persistence layers. Its tools accept developer and build-process requests, read descriptors from a working directory when needed, filter retrieved metadata through GraphQL-style field selections, and delegate registration and recommendations to the picker. The picker classifies requests, creates embeddings through a configured GenAI provider, and coordinates registration and discovery. A repository abstraction isolates persistence concerns, while the MongoDB implementation stores Bindex records and performs filtered vector searches. Generated schema classes preserve a typed metadata model. This structure keeps the external tool contract, domain workflow, and storage integration independently maintainable.

## Introduction

Bindex Core provides a consistent way to describe software libraries as structured Bindex metadata, validate and register that metadata, and retrieve suitable libraries for a natural-language development request. The library combines schema-based metadata, generated embeddings, semantic vector search, classification filters, and a MongoDB-backed repository.

Its Java API supports both application code and AI tool integrations: an AI agent can recommend libraries, inspect a complete or GraphQL-filtered descriptor, register metadata from JSON, files, or URLs, and obtain the Bindex schema or generation prompt. This reduces duplicate implementation work, improves dependency selection, and makes reusable capabilities discoverable across projects.

## Overview

A Bindex record captures a library's coordinates, version, purpose, classification, integrations, dependencies, examples, and configuration guidance. The discovery workflow is:

1. Assemble project documentation and build metadata into a schema-compliant descriptor.
2. Convert its classification into an embedding and store it with searchable metadata.
3. Classify a development request and convert it into a query embedding.
4. Search semantically, narrow results by language and architectural layer, apply relevance thresholds, and select the most useful library versions.
5. Use the selected descriptors to guide implementation, assembly, or dependency resolution.

## Key Features

- Schema-compliant Bindex v2 metadata with installation and usage guidance.
- Natural-language library recommendations powered by configurable GenAI and embedding providers.
- MongoDB persistence with vector search, classification filters, score thresholds, and version selection.
- Registration from a Bindex object, a project-relative JSON file, or a remote URL.
- Retrieval by coordinates or URL, with optional GraphQL-style field filtering.
- AI function tools for discovery, metadata access, registration, schema retrieval, and Bindex-generation prompts.
- Recursive dependency resolution and language-name normalization for reliable matching.
- Maven integration and an assembled distribution profile for deployment with host applications.

## How to use

Bindex Core is assembled for use with the [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server) and is included by default in the [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download).

When using `gw-maven-plugin`, add Bindex Core as a plugin dependency:

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

For direct Maven use, declare the library in the consuming project:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>bindex-core</artifactId>
  <version>RELEASE</version>
</dependency>
```

The AI-facing operations are exposed as `get-bindex`, `pick-libraries`, `register-bindex`, and `register-bindex-json`. Configure the GenAI provider and embedding provider through the host application's `Configurator`; repository connections can be customized with the parameters listed below. Register Bindex descriptors, then use library selection to find reusable components for new requirements.

## Built-In Acts

The following acts are defined under `src/main/resources/acts/**/*.toml` and support repeatable Bindex and implementation workflows.

### `assembly`

Uses Bindex library recommendations to help an AI software engineer implement a user task. Use it when a task requires selecting existing libraries, creating or updating a project, building it, and documenting the result.

### `bindex`

Coordinates Bindex generation for a non-parent Maven project. Use it to select the Maven project workflow, produce schema-compliant metadata from documentation and effective build information, validate the resulting descriptor, and register it.

### `bindex/java/extract-javadoc`

Extracts a complete, standalone Markdown report from generated Java Javadoc HTML. Use it when API documentation must be supplied as authoritative input for Bindex generation.

### `bindex/java/mvn-project`

Builds Javadoc for a Maven project and uses the reports, site Markdown, effective POM, and generation rules to create and validate `bindex.json`. Use it as the Maven-specific implementation stage of the Bindex workflow.

### `bindex/register`

Determines whether the current project is a supported non-parent Maven project and runs the registration stage of the Maven workflow. Use it to register an existing `bindex.json`; it stops for parent projects or unsupported project layouts.

### `bindex/validation`

Validates a generated Bindex JSON descriptor for a non-parent Maven project by loading it through the Bindex tooling. Use it after generating or editing metadata to find validation errors and correct the descriptor before registration.

### `pick`

Selects libraries relevant to a user's query through Bindex recommendations. Use it when planning a new implementation or extending an existing project and suitable reusable libraries need to be identified before coding.

## Configuration

| Parameter | Description | Default value |
|---|---|---|
| `gw.model` | GenAI model used by the picker when `pick.model` is not set. | Host/application-defined. |
| `gw.mini.model` | Compact GenAI model used by Bindex generation and registration acts. | Host/application-defined. |
| `pick.model` | Model override used for classifying library-selection requests. | Falls back to `gw.model`. |
| `embedding.model` | Embedding provider model used to encode classifications for semantic search. | Host/application-defined. |
| `pick.score` | Similarity threshold used when selecting recommendations. | Act-defined; commonly `0.86`. |
| `picker.classificationInstruction` | Custom instruction template for producing classification JSON. | Built-in classification instruction. |
| `gw.path` | File glob used by an act to select project files. | `glob:.` for Bindex generation acts. |
| `BINDEX_REPO_URL` | MongoDB connection URI for the Bindex repository. | Application-defined. |
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
- [Bindex Maven Plugin](bindex-maven-plugin/)
- [Bindex MCP Server](bindex-mcp-server/)
- [Bindex Core on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)
- [Bindex metadata schema](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json)
- [Ghostwriter CLI download](https://machai.machanism.org/ghostwriter/index.html#Download)
