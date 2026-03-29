---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.

Page Structure: 
# Header
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

Bindex Core (`org.machanism.machai:bindex-core`) is a Java library for **Bindex** metadata management in the MachAI ecosystem.

A **Bindex** is a JSON document (typically `bindex.json`) that captures stable project/library identity and discovery metadata (id, name, version, classification facets, and dependencies). Bindex Core provides the building blocks to:

- Generate Bindex documents from a local project folder using layout-aware builders
- Register and query Bindex documents in a MongoDB-backed registry
- Select relevant libraries for a natural-language query using LLM classification and semantic/vector search
- Assemble selected results into structured, prompt-ready context for LLM-assisted workflows

## Overview

Bindex Core supports an end-to-end workflow:

1. **Create/Update**: inspect a project layout and generate/update `bindex.json`.
2. **Register**: persist the Bindex into a MongoDB registry, including classification facets and an embedding vector for semantic retrieval.
3. **Pick**: classify a user query into one or more classification objects and run vector search with facet filters, then expand results via transitive dependencies.
4. **Assemble**: package selected Bindexes into structured context suitable for downstream LLM-assisted application assembly.

### Architecture (C4 overview)

![C4 Diagram](./images/c4-diagram.png)

At a high level, the library combines: a creation pipeline that selects a layout-aware builder and produces `bindex.json`; a repository-backed registry layer for persistence and search; a picker that mixes LLM classification with MongoDB vector search and dependency expansion; and an assembly stage that turns chosen Bindexes into prompt-ready context inputs.

## Key Features

- Generate or update `bindex.json` from a local project directory using layout-aware builders.
- Auto-select a builder based on detected project layout (for example Maven, Python, or JavaScript).
- Register Bindex documents into a MongoDB-backed registry.
- Retrieve relevant Bindexes using LLM-driven classification plus MongoDB vector search.
- Expand results using declared Bindex dependencies (transitive dependency discovery).
- Assemble selected Bindexes into structured, prompt-ready inputs for downstream workflows.

## Getting Started

### Prerequisites

- **Java** (see version notes below)
- **Maven** (build tool)
- **MongoDB** (required for registry operations such as register/search)
- A configured **GenAI provider** supported by MachAI (required for AI-assisted generation, classification, and embeddings)

### Java Version

- **Build configuration (from `pom.xml`)**: `maven.compiler.release = 8` (Java 8 bytecode)
- **Practical runtime requirements**: Java 8+ for core features. Some optional dependencies on the classpath (for example the MongoDB driver or a specific GenAI provider implementation) may impose stricter runtime requirements.

### Basic Usage

Create or update `bindex.json` for a project:

```java
Configurator config = ...;
ProjectLayout layout = ...;

new BindexCreator("openai", config)
    .update(true)
    .processFolder(layout);
```

Register a local `bindex.json` in the registry:

```java
Configurator config = ...;
ProjectLayout layout = ...;

new BindexRegister("openai", null, config)
    .update(true)
    .processFolder(layout);
```

Pick relevant Bindexes for a free-text query:

```java
Configurator config = ...;
Picker picker = new Picker("openai", null, config);

List<Bindex> selected = picker.pick("Find libraries for server-side logging");
```

### Typical Workflow

1. Detect or construct a `ProjectLayout` for the target project.
2. Run `BindexCreator` to create/update `bindex.json`.
3. Run `BindexRegister` to register the Bindex into MongoDB.
4. Use `Picker` to classify a query and retrieve matching Bindexes (with dependency expansion).
5. Assemble the selected Bindexes into structured context for an LLM workflow.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `bindex.model` | GenAI provider/model identifier used for AI-assisted Bindex generation. | `CodeMie:gpt-5-2-2025-12-11` |
| `pick.model` | GenAI provider/model identifier used for query classification during picking. | `CodeMie:gpt-5-2-2025-12-11` |
| `assembly.model` | GenAI provider/model identifier used to assemble prompt-ready context. | `CodeMie:gpt-5-2-2025-12-11` |
| `BINDEX_REPO_URL` | MongoDB connection URI for repository-backed operations. | (unset; environment-specific) |
| `BINDEX_REG_PASSWORD` | Registry password used to authenticate when required. | (unset; environment-specific) |

## Resources

- Documentation site: https://machai.machanism.org/bindex-core/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-core
