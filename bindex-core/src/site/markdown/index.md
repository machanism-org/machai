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
5. Acts
	- Analyze all act TOML files located in the `src/main/resources/acts` folder.
	- For each act, create a section that includes:
		 - The act's name.
		 - A clear, concise description of the act's purpose and when it should be used.
	- Organize your output so that each act is easy to identify and understand.
	- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
6. Configuration
   - Include a table of common configuration parameters, with columns for parameter name, description, and default value.
   - Ensure descriptions are clear and concise.
7. Resources
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

Bindex Core (`org.machanism.machai:bindex-core`) is a Java library that provides the core building blocks for working with **Bindex** metadata in the MachAI ecosystem.

A *Bindex* is a structured JSON document describing a software library/project with stable identity and discovery metadata (such as id, name, version, description, classification facets like domains/layers/languages/integrations, plus dependency identifiers). Bindex Core focuses on enabling automated, machine-friendly library discovery and integration by:

- Persisting Bindex documents in a MongoDB-backed registry.
- Enriching stored records with vector embeddings for semantic retrieval.
- Using a GenAI provider to classify free-text queries into structured classification objects that can drive filtered vector search.
- Exposing these capabilities as function tools so LLM-driven workflows can retrieve a Bindex, retrieve the schema, recommend candidate libraries, or register a local Bindex file.

## Overview

Bindex Core enables an end-to-end “register and pick” workflow:

1. **Register**: serialize and persist a Bindex into MongoDB, storing the raw JSON plus indexed projection fields (id/name/version and facet arrays) together with a classification embedding vector.
2. **Classify**: convert a natural-language query into one or more structured classification objects using a schema-guided prompt derived from the Bindex JSON schema.
3. **Search**: run MongoDB vector search against stored embeddings, applying facet filters (for example by language and layer) and a configurable minimum similarity score.
4. **Consume**: return matching Bindex documents (and expand results with transitive dependencies) for downstream automation via direct API calls or GenAI function tools.

### Architecture (C4 overview)

![C4 Diagram](./images/c4-diagram.png)

At a high level, the library centers around a semantic picker that orchestrates:

- A GenAI provider for schema-guided classification and embedding generation.
- A MongoDB-backed registry for persistence and vector search.
- A tool-integration layer that exposes Bindex operations as callable tools for LLM-assisted workflows.

## Key Features

- MongoDB-backed persistence and lookup for Bindex documents.
- Registration flow that stores both Bindex JSON and searchable projection fields.
- Schema-guided query classification using a GenAI provider.
- Embedding generation for classification data and semantic retrieval.
- MongoDB vector search with facet filtering (languages/layers/integrations) and configurable score threshold.
- GenAI function tools for fetching a Bindex, fetching the schema, recommending libraries for a prompt, and registering a local Bindex file.

## Acts

### assembly

Implements an application task by leveraging Bindex-based library recommendations. Use it when you want the assistant to recommend suitable libraries, retrieve full Bindex metadata for selected candidates, and assemble a working solution in a project (including creating files and building/fixing errors).

### bindex

Generates or updates a `bindex.json` file for the current project based on source and documentation content, ensuring it conforms to the Bindex schema. Use it when you need to produce high-quality Bindex metadata for a project and optionally register it if the file exists.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `pick.model` | GenAI provider/model identifier used by picking flows (classification and embeddings). | `CodeMie:gpt-5-2-2025-12-11` |
| `pick.score` | Minimum vector similarity score required for a result to be included. | `0.85` |
| `BINDEX_REPO_URL` | MongoDB connection URI used to access the Bindex registry. | `mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0` |
| `BINDEX_REG_PASSWORD` | Password used for MongoDB authentication when registering/updating records (enables privileged user). | (unset) |
| `gw.model` | GenAI provider/model identifier used by the `register_bindex` tool when registering a local Bindex file. | (unset) |
| `embedding.model` | Embedding model identifier used by acts that generate embeddings. | `text-embedding-005` |

## Resources

- Documentation site: https://machai.machanism.org/bindex-core/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-core
