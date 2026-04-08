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
8. Resources
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

Bindex Core provides the core runtime needed to manage Bindex metadata within the MachAI ecosystem. It focuses on describing software libraries and projects as structured metadata, storing those records in MongoDB, enriching them with embeddings for semantic retrieval, and exposing the result through tools that AI-assisted workflows can call directly.

The project combines repository services, semantic recommendation logic, and GenAI tool registration into a single library. It supports creating and updating metadata records, retrieving them by identifier, loading the JSON schema that defines the metadata format, and recommending suitable libraries from a natural-language prompt. This makes it useful for automated project assembly, dependency discovery, metadata cataloging, and schema-driven documentation workflows.

## Overview

Bindex Core supports a metadata lifecycle built around registration, retrieval, and semantic library selection:

1. **Metadata registration**: a structured metadata document is serialized, normalized, embedded, and stored with searchable classification fields.
2. **Schema-guided interpretation**: a natural-language requirement is converted into structured classification data aligned with the Bindex schema.
3. **Semantic recommendation**: vector search and metadata filters are used to identify relevant libraries by language, architectural layer, and similarity score.
4. **AI tool integration**: metadata access, schema lookup, recommendation, and file-based registration are exposed as tools for automated implementation workflows.

### Architecture

![C4 Diagram](./images/c4-diagram.png)

The architecture centers on a metadata workflow that connects persistence, semantic retrieval, and AI tool integration.

- A repository component manages structured metadata documents in MongoDB and provides direct lookup and deletion operations.
- A semantic selection component generates classification prompts and embeddings, stores indexed metadata, and performs vector-based recommendation queries.
- A tool integration component exposes metadata retrieval, schema access, recommendation, and working-directory registration for AI-assisted automation.
- External services provide language-model prompting, embedding generation, vector-capable database storage, schema resources, and access to local metadata files.

Together, these parts allow the project to move from structured library descriptions to searchable semantic recommendations that can be used in automation and documentation workflows.

## Key Features

- Persist Bindex metadata documents in MongoDB.
- Store serialized metadata together with normalized fields for filtering and lookup.
- Generate and store classification embeddings for semantic vector search.
- Convert natural-language requests into structured classification queries based on the Bindex schema.
- Recommend libraries using similarity thresholds plus language and layer filters.
- Retrieve registered metadata directly by Bindex identifier.
- Expose metadata lookup, schema access, recommendation, and registration as GenAI function tools.
- Register a metadata file from the current working directory for reuse in automated workflows.

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

After adding the dependency, the library can participate in Ghostwriter-driven workflows to retrieve registered metadata, inspect the Bindex schema, recommend matching libraries from a requirement prompt, and register a root-level Bindex file from the working directory.

## Acts

### `assembly`

Use this act when you want an assistant to implement a software task by selecting and applying suitable libraries from the Bindex registry. It is intended for project assembly workflows where the assistant should request recommendations, inspect detailed metadata, create or update project files, add dependencies, build the project, and iterate until the implementation works.

### `bindex`

Use this act when you want to create or update a `bindex.json` file for a library or project. It is intended for metadata authoring workflows where the assistant should produce schema-compliant library metadata with realistic examples, and register the root-level metadata file when it exists.

## Configuration

| Parameter | Description | Default value |
|---|---|---|
| `pick.model` | GenAI model used for semantic classification and library recommendation. | `CodeMie:gpt-5-2-2025-12-11` |
| `pick.score` | Minimum similarity score required for recommendation results. | `0.85` |
| `BINDEX_REPO_URL` | MongoDB connection URI for the Bindex repository. | `mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0` |
| `BINDEX_REG_PASSWORD` | Password used to enable privileged registration access. | unset |
| `gw.model` | GenAI model used by the registration tool when reading a local Bindex file. | unset |
| `embedding.model` | Embedding model used by Ghostwriter and related semantic workflows. | `text-embedding-005` |
| `gw.interactive` | Enables interactive behavior for the assembly act. | `true` |
| `gw.scanDir` | Scan scope used by the bindex act when working with project files. | `glob:.` |

## Resources

- [Project site](https://machai.machanism.org/bindex-core/index.html)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)
- [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html)
- [GW Maven Plugin](https://machai.machanism.org/gw-maven-plugin/index.html)
- [GitHub issues](https://github.com/machanism-org/machai/issues)
