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

Bindex Core (`org.machanism.machai:bindex-core`) is a Java library that creates, stores, discovers, and assembles **Bindex** documents for projects and reusable libraries in the MachAI ecosystem.

A **Bindex** is a JSON metadata document (typically `bindex.json`) designed to be both **machine-readable** and **LLM-friendly**. It provides stable identity and discovery information—such as identifiers, names, versions, classification facets, and dependencies—so that projects and libraries can be:

- **Generated** from a local project folder using layout-aware builders
- **Registered** into a MongoDB-backed registry
- **Searched and retrieved** via classification and semantic/vector search
- **Assembled** into curated, prompt-ready context for downstream LLM-assisted workflows

This enables consistent metadata across many repositories, faster discovery of relevant building blocks, and automated packaging of selected context for application assembly and analysis.

## Overview

Bindex Core is organized around an end-to-end workflow:

1. **Create/Update** a Bindex for a local project directory using a layout-aware builder.
2. **Register** the resulting Bindex into a MongoDB-backed registry.
3. **Pick** relevant Bindexes for a natural-language query using classification plus semantic/vector search, with dependency expansion.
4. **Assemble** selected Bindexes into structured, prompt-ready context inputs for LLM-driven workflows.

### Architecture (C4 overview)

![C4 Diagram](./images/c4-diagram.png)

At a high level, the library combines: a creation pipeline that inspects a project layout and generates a Bindex document; a repository layer that persists and queries Bindexes in MongoDB; a picker that selects relevant candidates by combining query classification with semantic retrieval and dependency expansion; and an assembly stage that turns the selected Bindexes into structured context suitable for LLM-assisted application workflows.

## Key Features

- Create or update `bindex.json` for a project using AI-assisted, layout-aware builders.
- Automatically select a builder based on detected project layout (for example Maven, Python, or JavaScript).
- Register Bindex documents into a MongoDB-backed registry.
- Retrieve Bindexes using query classification plus semantic/vector search.
- Expand results using declared dependencies between Bindexes.
- Assemble selected Bindexes into structured, prompt-ready inputs for downstream workflows.

## Getting Started

### Prerequisites

- **Java** (see version notes below)
- **Maven** (to build and run)
- **MongoDB** (required for registry operations such as register/search)
- A configured **GenAI provider** compatible with MachAI (required for AI-assisted generation, classification, and embeddings)

### Java Version

- **Build configuration (from `pom.xml`)**: `maven.compiler.release = 8` (Java 8 bytecode)
- **Practical runtime requirements**: Java 8+ is expected for core functionality. Some optional integrations on the classpath (for example the MongoDB driver or a specific GenAI provider implementation) may impose stricter requirements; validate against your dependency set and runtime environment.

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

1. Detect or construct a `ProjectLayout` for the target project.
2. Run `BindexCreator` to create/update `bindex.json`.
3. Run `BindexRegister` to persist the Bindex into MongoDB.
4. Use `Picker` to classify a query and retrieve matching Bindexes (with dependency expansion).
5. Use the assembly step to convert the selected Bindexes into structured context for an LLM workflow.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `bindex.model` | GenAI provider/model identifier used for AI-assisted Bindex generation (creator and layout builders). | `CodeMie:gpt-5-2-2025-12-11` |
| `pick.model` | GenAI provider/model identifier used for query classification during picking. | `CodeMie:gpt-5-2-2025-12-11` |
| `assembly.model` | GenAI provider/model identifier used to assemble prompt-ready context. | `CodeMie:gpt-5-2-2025-12-11` |
| `BINDEX_REPO_URL` | MongoDB connection URI for repository-backed operations. | (unset; depends on environment) |
| `BINDEX_REG_PASSWORD` | Registry password used to authenticate when required. | (unset; depends on registry configuration) |

## Resources

- Documentation site: https://machai.machanism.org/bindex-core/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-core
