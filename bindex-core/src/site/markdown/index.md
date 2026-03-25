---
canonical: https://machai.machanism.org/bindex-core/index.html
---

<!-- @guidance:
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

# Bindex Core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-core.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)

## Introduction

Bindex Core (`org.machanism.machai:bindex-core`) is the foundational Java library for generating, persisting, registering, and retrieving **MachAI Bindex** documents.

A **Bindex** is a JSON document (typically named `bindex.json`) that describes a software project or reusable library in a way that is both **machine-readable** and **LLM-friendly**. It focuses on stable identity and discovery metadata (such as `id`, `name`, `version`, classification facets, and dependencies), enabling Bindexes to be:

- **Generated** from a local project folder using layout-aware builders
- **Registered** into a MongoDB-backed registry (optionally with embeddings for semantic retrieval)
- **Retrieved** later by query classification and vector search
- **Assembled** into curated, prompt-ready context for downstream LLM-assisted workflows

In practice, Bindex Core helps teams keep consistent metadata across many codebases, search for relevant building blocks via a registry, and automatically package selected information into a context bundle suitable for application assembly and analysis.

## Overview

Bindex Core supports an end-to-end workflow:

1. **Create/Update** a Bindex from a local project directory using a layout-aware builder.
2. **Register** the resulting Bindex into a MongoDB-backed registry.
3. **Pick** relevant Bindexes for a free-text query using classification plus semantic/vector search (with dependency expansion).
4. **Assemble** selected Bindexes into structured, prompt-ready context for LLM-driven workflows.

### Architecture (C4 overview)

![C4 Diagram](./images/c4-diagram.png)

At a high level, the library is organized around four collaborating components: a creation pipeline that scans a project layout and produces a Bindex document, a repository layer that persists and queries Bindexes in MongoDB, a picker that combines query classification with semantic search to select relevant candidates (and expands them via declared dependencies), and an assembly step that turns the selected Bindexes into structured context inputs for LLM-driven workflows.

## Key Features

- Generate or update `bindex.json` for a project using layout-aware builders.
- Select a builder based on detected project layout (for example Maven, Python, or JavaScript).
- Register Bindex documents into a MongoDB-backed registry.
- Retrieve Bindexes using query classification plus semantic/vector search.
- Expand results using declared dependencies between Bindexes.
- Assemble selected Bindexes into structured, prompt-ready inputs for downstream workflows.

## Getting Started

### Prerequisites

- **Java** (see version notes below)
- **Maven** (to build and run)
- **MongoDB** (required for registry operations such as register/search)
- A configured **GenAI provider** compatible with the MachAI `GenAIProvider` integration (required for classification/embedding and AI-assisted generation)

### Java Version

- **Build configuration (from `pom.xml`)**: `maven.compiler.release = 8` (Java 8 bytecode)
- **Practical runtime requirements**: Java 8+ is expected for core usage. Some integrations on the classpath (for example the MongoDB driver or a specific GenAI provider implementation) may impose stricter requirements; validate against your dependency set and runtime environment.

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

Assemble prompt-ready inputs:

```java
Configurator config = ...;
File projectDir = ...;
List<Bindex> bindexes = ...;

new ApplicationAssembly("openai", config, projectDir)
    .assembly("Create a minimal sample that uses the selected libraries", bindexes);
```

### Typical Workflow

1. Detect/construct a `ProjectLayout` for the target project.
2. Run `BindexCreator` to create/update `bindex.json`.
3. Run `BindexRegister` (and the underlying repository) to persist the Bindex into MongoDB.
4. Use `Picker` to classify a query and retrieve matching Bindexes (with dependency expansion).
5. Use `ApplicationAssembly` to turn the selected Bindexes into structured context for an LLM workflow.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `bindex.model` | Model/provider identifier used for Bindex generation and layout builders. | `CodeMie:gpt-5-2-2025-12-11` |
| `pick.model` | Model/provider identifier used for query classification during picking. | `CodeMie:gpt-5-2-2025-12-11` |
| `assembly.model` | Model/provider identifier used to assemble prompt-ready context. | `CodeMie:gpt-5-2-2025-12-11` |
| `BINDEX_REPO_URL` | MongoDB connection URI for repository-backed operations. | (unset; falls back to an internal default) |
| `BINDEX_REG_PASSWORD` | Registry password used to authenticate when required. | (unset; depends on registry configuration) |

## Resources

- Documentation site: https://machai.machanism.org/bindex-core/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-core
