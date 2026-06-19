---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([!\[Maven Central\](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
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
6. Acts
	- Analyze all act TOML files located in the `src/main/resources/acts` folder.
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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-core.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)

## Introduction

Bindex Core provides the core services for Bindex metadata management in the Machai ecosystem. It helps AI-assisted development workflows discover, register, retrieve, and reuse structured library metadata so that project assembly can be driven by accurate, schema-based information rather than ad hoc search results.

The library combines a MongoDB-backed repository, semantic library selection, Bindex JSON serialization, and AI-callable function tools. It stores Bindex documents, enriches them with classification embeddings, searches for libraries that match natural-language requirements, and exposes the results to Ghostwriter and Maven-plugin workflows. This makes library discovery more repeatable, enables assistants to inspect detailed usage metadata, and supports faster integration of existing components into generated or updated projects.

## Overview

Bindex Core supports a metadata-centered workflow for library discovery and registration:

1. **Repository access** resolves MongoDB connection settings, opens repository collection access, retrieves registered Bindex documents by logical identifier, and supports deletion or replacement of existing records.
2. **Registration** serializes Bindex JSON, extracts classification fields, generates embedding vectors, and stores searchable metadata in the shared repository.
3. **Semantic recommendation** converts a user requirement into classification JSON, generates query embeddings, applies language and layer filters, and returns the best matching registered libraries.
4. **AI tool integration** exposes lookup, recommendation, file-based registration, and JSON-based registration operations as GenAI function tools.
5. **Reusable acts** document common assistant workflows for implementing tasks with recommended libraries, generating Bindex descriptors, and selecting suitable dependencies.

### Architecture

![C4 Diagram](./images/c4-diagram.png)

The component model is intentionally compact. A tool-facing layer receives requests from AI-assisted workflows and delegates repository lookup, library recommendation, and registration tasks. A recommendation layer handles classification prompts, embedding generation, semantic search, score filtering, dependency-aware lookup, and repository writes. A repository layer centralizes database connectivity and basic document retrieval. External systems provide working-directory files, GenAI classification and embedding capabilities, and persistent storage for registered metadata.

Together, these parts move a request from natural-language intent to actionable, metadata-backed library choices that can be inspected and used during implementation.

## Key Features

- Stores Bindex metadata as serialized JSON documents in MongoDB.
- Resolves repository credentials from configuration and environment variables.
- Registers or replaces library metadata records by logical Bindex identifier.
- Generates classification embeddings for vector-based semantic search.
- Converts natural-language prompts into schema-aligned classification requests.
- Filters recommendations by normalized programming language and architectural layer.
- Applies configurable recommendation score thresholds.
- Selects preferred library versions when multiple records match the same artifact name.
- Exposes `get_bindex`, `pick_libraries`, `register_bindex`, and `register_bindex_json` as AI function tools.
- Registers local Bindex files from the working directory or directly from JSON objects.
- Provides reusable acts for project assembly, Bindex descriptor generation, and library selection.

## How to use

This library is included in [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download) as default.

If you use `gw-maven-plugin`, you need to add this library by the dependency, e.g:

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

Once available to Ghostwriter or the Maven plugin, Bindex Core can be used to:

- retrieve a registered Bindex document by id,
- recommend libraries from a natural-language project requirement,
- register a local `bindex.json` file from the project working directory,
- register a Bindex JSON object directly through the function tool API.

For registration into the shared repository, configure registration credentials with `BINDEX_REG_PASSWORD`. Without registration credentials, repository access is intended for public read-oriented operations.

## Acts

### `assembly`

Use this act for end-to-end implementation tasks that should be built around recommended libraries. It instructs the assistant to call the library picker with the user's request, inspect each matching Bindex descriptor through lookup tools, use those libraries instead of writing everything from scratch, create or update project files, build the project, fix errors, and document the result. It is appropriate when the user asks the assistant to implement a feature, create an application, or update an existing project.

### `bindex`

Use this act to generate or refresh a schema-compliant `bindex.json` descriptor for a reusable library project. It guides the assistant to build Javadoc, read generated API documentation instead of raw source files, use the official Bindex schema, produce valid JSON with classification data and practical usage examples, and optionally register the metadata file. It is appropriate when preparing a project so it can be discovered and reused through Bindex.

### `pick`

Use this act when the task is library discovery rather than full implementation. It directs the assistant to call the recommendation tool, analyze returned library descriptions, retrieve detailed Bindex metadata when needed, and present relevant options for the user's requirement. It is appropriate for comparing candidates, selecting dependencies, or planning an implementation that will use existing libraries.

## Configuration

| Parameter | Description | Default value |
|---|---|---|
| `BINDEX_REPO_URL` | MongoDB connection URI for the Bindex repository. | `mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0` |
| `BINDEX_REG_PASSWORD` | Registration password. When set, repository access uses the registration account; otherwise public credentials are used. | unset |
| `gw.model` | GenAI model used by Ghostwriter-facing Bindex function tools and acts. | `CodeMie:gpt-5.4-2026-03-05` |
| `pick.model` | GenAI model used for prompt classification during recommendation. Falls back to `gw.model` when used from function tools. | `CodeMie:gpt-5.4-2026-03-05` |
| `embedding.model` | Embedding model used to create classification and query vectors. | `CodeMie:text-embedding-005` |
| `pick.score` | Minimum vector-search score accepted for recommendations. | `0.86` in acts, `0.85` in code |
| `picker.classificationInstruction` | Optional custom prompt template used to convert a user request into classification JSON. | built-in classification instruction |
| `gw.interactive` | Enables interactive mode for applicable acts. | `true` |
| `gw.nonRecursive` | Limits applicable act execution to the current project scope. | `true` |
| `gw.path` | Processing scope used by the Bindex metadata generation workflow. | `glob:.` |
| `maven.compiler.release` | Java release used to compile the module. | `8` |

## Troubleshooting

If DNS-related MongoDB connectivity fails on newer Java runtimes or restricted module configurations, add the following command-line argument to your Java startup command or environment variables:

```text
--add-exports jdk.naming.dns/com.sun.jndi.dns=java.naming
```

Additional checks:

- Verify that `BINDEX_REPO_URL` points to a reachable MongoDB deployment.
- Set `BINDEX_REG_PASSWORD` before attempting registration operations.
- Confirm that `embedding.model`, `pick.model`, or `gw.model` are configured for an available GenAI provider.
- Lower or tune `pick.score` if valid libraries are not returned for broad or exploratory prompts.

## Resources

- [Project site](https://machai.machanism.org/bindex-core/index.html)
- [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html)
- [GW Maven Plugin](https://machai.machanism.org/gw-maven-plugin/index.html)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)
- [Issue tracker](https://github.com/machanism-org/machai/issues)
