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

Bindex Core provides the core services for Bindex metadata management in the MachAI ecosystem. It combines structured metadata storage, semantic recommendation, schema-based classification, and AI function-tool integration so projects and assistants can discover reusable libraries, inspect their metadata, and register new library descriptors from local workspaces.

The module is designed to make library reuse more reliable and automation-friendly. Instead of relying only on informal descriptions, it works with schema-compliant metadata documents, persists them in a shared repository, enriches them with embedding vectors, and exposes lookup and recommendation capabilities through callable tools. This helps teams standardize library discovery, improve recommendation quality, and support higher-level implementation workflows driven by Ghostwriter and related tooling.

## Overview

Bindex Core supports a metadata-driven workflow for discovering, registering, and reusing software libraries:

1. **Repository access** manages MongoDB connectivity and stores serialized metadata records together with searchable fields.
2. **Semantic picking** converts natural-language requests into structured classifications, generates embeddings, and performs vector search with language and architectural-layer filtering.
3. **Tool integration** exposes retrieval, schema inspection, recommendation, and registration operations as callable AI tools.
4. **Workflow templates** provide reusable prompts for library selection, metadata generation, and assembly-oriented implementation flows.

### Architecture

![C4 Diagram](./images/c4-diagram.png)

The architecture is organized around a small set of collaborating responsibilities.

- A repository layer handles database connections and direct metadata persistence and retrieval.
- A semantic selection layer interprets user requests, generates embeddings from classification data, and queries the shared repository for relevant libraries.
- A tool layer bridges these capabilities into AI-assisted workflows by registering callable operations for lookup, schema access, recommendation, and local metadata registration.
- Supporting resources define the metadata schema and workflow templates that guide discovery and implementation tasks.

Together, these parts allow a user request to move from natural-language intent to structured library recommendations backed by stored metadata.

## Key Features

- Persist Bindex metadata as serialized JSON documents in MongoDB.
- Resolve repository access through configurable connection settings, including registration-capable access.
- Generate embeddings from classification metadata for semantic library search.
- Classify natural-language requests into schema-aligned metadata criteria.
- Filter recommendations by programming language and architectural layer.
- Expose metadata lookup, schema retrieval, recommendation, and registration as AI function tools.
- Register local `bindex.json` files from the working directory into the shared repository.
- Provide reusable workflow acts for discovery, metadata authoring, and project assembly.

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

After adding the dependency, Bindex Core can be used in Ghostwriter-driven workflows to:

- retrieve a registered metadata document by Bindex identifier,
- load the bundled Bindex schema,
- request semantic library recommendations from a natural-language prompt,
- register a local `bindex.json` file into the shared repository.

## Acts

### `assembly`

Use this act when the goal is to implement a user request by assembling a solution with recommended libraries. It is intended for end-to-end development workflows where the assistant should discover libraries, inspect their metadata, update the project, build it, and iterate until the requested behavior is implemented.

### `bindex`

Use this act when creating or updating a `bindex.json` file for a project or library. It is best suited for metadata-authoring workflows where the assistant analyzes source code and documentation, generates schema-compliant metadata, includes practical examples, and can register the file when it exists in the project root.

### `pick`

Use this act when the task is to recommend suitable libraries for a requirement without implementing the solution itself. It is ideal for discovery workflows where the assistant should request recommendations, review the returned metadata summaries, and present relevant options to the user.

## Configuration

| Parameter | Description | Default value |
|---|---|---|
| `BINDEX_REPO_URL` | MongoDB connection URI for the shared Bindex repository. | `mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0` |
| `BINDEX_REG_PASSWORD` | Password that enables registration-capable repository access. | unset |
| `pick.model` | GenAI model used for classification and recommendation workflows. | `CodeMie:gpt-5.4-2026-03-05` |
| `pick.score` | Minimum similarity threshold for semantic recommendation results. | `0.86` |
| `gw.model` | GenAI model used by Bindex-related workflow tools. | `CodeMie:gpt-5.4-2026-03-05` |
| `embedding.model` | Embedding model used for semantic indexing and search. | `text-embedding-005` |
| `gw.interactive` | Enables interactive execution for applicable acts. | `true` |
| `gw.nonRecursive` | Limits processing to the current project scope for applicable acts. | `true` |
| `gw.scanDir` | Scan scope used by the metadata-generation workflow. | `glob:.` |

## Resources

- [Project site](https://machai.machanism.org/bindex-core/index.html)
- [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html)
- [GW Maven Plugin](https://machai.machanism.org/gw-maven-plugin/index.html)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)
- [Issue tracker](https://github.com/machanism-org/machai/issues)
