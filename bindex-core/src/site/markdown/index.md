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
5. Getting Started
   - **Prerequisites:**  
     - List all required software, services, and environment settings needed to use the project.
   - **Java Version:**  
     Note that the required Java version is defined in `pom.xml`, but actual functional requirements may differ. Clearly state both.
   - **Basic Usage.**  
   - **Typical Workflow.**  
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

Bindex Core (`org.machanism.machai:bindex-core`) is the core Java library for **Bindex** metadata management in the MachAI ecosystem.

A *Bindex* is a JSON document (typically `bindex.json`) that captures stable library identity and discovery metadata (for example: id, name, version, human-readable description, classification facets, and dependencies). Bindex Core provides the building blocks to:

- Persist and retrieve Bindex documents in a MongoDB-backed registry
- Compute and store embedding vectors for semantic retrieval
- Classify a free-text query into structured classification objects using a GenAI provider
- Execute MongoDB vector search with facet filters and score thresholds
- Return the most relevant libraries and expand results via transitive dependency discovery

## Overview

Bindex Core supports an end-to-end discovery workflow:

1. **Register**: serialize a Bindex document and store it together with classification facets and a vector embedding used for semantic retrieval.
2. **Classify**: convert a natural-language query into one or more structured classifications via an LLM prompt constrained by the Bindex JSON schema.
3. **Search**: run MongoDB vector search over stored embeddings, apply facet filters (languages, layers, etc.), and enforce a minimum similarity score.
4. **Expand**: include transitive dependencies declared by selected results.

### Architecture (C4 overview)

![C4 Diagram](./images/c4-diagram.png)

At a high level, the library combines: (1) a MongoDB repository layer for storing serialized Bindex documents; (2) a picking/search layer that produces embeddings, performs schema-guided LLM classification, runs MongoDB vector search, and expands dependencies; and (3) an integration layer that exposes these capabilities as function tools to GenAI-driven workflows.

## Key Features

- MongoDB-backed persistence and lookup for Bindex documents.
- Schema-guided query classification using a GenAI provider.
- Embedding generation for classification data and semantic retrieval.
- MongoDB vector search pipeline with facet filtering and configurable score threshold.
- Dependency expansion for returned results (transitive discovery).
- GenAI function tools to fetch a Bindex, fetch the Bindex schema, pick libraries, and register a local Bindex file.

## Getting Started

### Prerequisites

- **Java** (see version notes below)
- **Maven** (to build the project)
- **MongoDB Atlas / MongoDB** with:
  - A collection containing Bindex records
  - A vector search index configured for the stored embedding field
- A configured **GenAI provider** supported by MachAI (required for schema classification prompts and embeddings)

### Java Version

- **Build configuration (from `pom.xml`)**: `maven.compiler.release = 8` (Java 8 bytecode)
- **Practical runtime requirements**: Java 8+ for core features. MongoDB driver / GenAI provider implementations on the classpath may require newer Java versions depending on how they are deployed.

### Basic Usage

Pick relevant Bindexes for a free-text query:

```java
Configurator config = ...;

Picker picker = new Picker("openai", null, config);
List<Bindex> selected = picker.pick("Find libraries for server-side logging");
```

Register a Bindex in MongoDB:

```java
Configurator config = ...;

Picker picker = new Picker("openai", null, config);
String recordId = picker.create(bindex);
```

### Typical Workflow

1. Ensure MongoDB is configured with a vector search index for stored Bindex embeddings.
2. Register Bindex documents into the registry.
3. Run `Picker.pick(query)` to classify the query and perform semantic retrieval.
4. Use returned Bindexes and their transitive dependencies in downstream automation (for example, assembling LLM context).

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `pick.model` | GenAI provider/model identifier used for query classification and embeddings in picking flows. | `CodeMie:gpt-5-2-2025-12-11` |
| `pick.score` | Minimum vector similarity score required for a result to be included. | `0.85` |
| `BINDEX_REPO_URL` | MongoDB connection URI used by the repository/picker. When unset, a built-in default is used. | (unset) |
| `BINDEX_REG_PASSWORD` | Password used to authenticate to MongoDB when required (paired with a configured username). | (unset) |
| `gw.model` | GenAI provider/model identifier used by the registration function tool when registering a local Bindex file. | (unset) |

## Resources

- Documentation site: https://machai.machanism.org/bindex-core/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-core
