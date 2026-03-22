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

Bindex Core (`org.machanism.machai:bindex-core`) provides the core library used to **generate**, **register**, **retrieve**, and **assemble** MachAI **Bindex** documents.

A **Bindex** is a JSON document (typically `bindex.json`) that describes a software project or reusable library in a form that is both **machine-readable** and **LLM-friendly**. It captures key metadata such as identity (`id`, `name`, `version`), semantic classification facets (domains, layers, languages, integrations), and dependencies on other Bindexes to support transitive expansion.

By standardizing project/library metadata and supporting semantic discovery over a registry, Bindex Core enables automated workflows for:

- generating and keeping `bindex.json` up to date for a project
- registering Bindexes into a MongoDB-backed registry
- selecting relevant libraries for a free-text query via classification + vector search
- assembling selected Bindexes into prompt-ready context for downstream LLM-assisted tasks

## Overview

Bindex Core is designed around an end-to-end workflow that starts from a local project folder and ends with reusable, prompt-ready context:

1. **Create or update** a project’s `bindex.json` using `BindexCreator` with an appropriate layout-specific builder (for example Maven, Python, or JavaScript).
2. **Register** the resulting Bindex into a **MongoDB** registry via `BindexRegister`/`BindexRepository` (optionally including embeddings for semantic retrieval).
3. **Pick** relevant Bindexes for a free-text query using `Picker` (classification + semantic search) and expand results using declared dependencies.
4. **Assemble** selected Bindexes into structured prompt inputs using `ApplicationAssembly`.

The primary API entry points are in the `org.machanism.machai.bindex` package, with builders under `org.machanism.machai.bindex.builder`.

## Key Features

- Generate or update a project’s `bindex.json` using AI-assisted builders.
- Choose a builder based on detected project layout (Maven, Python, JavaScript).
- Register Bindex documents into a MongoDB-backed registry.
- Retrieve Bindexes using query classification plus semantic/vector search.
- Expand results using transitive dependency references between Bindexes.
- Assemble selected Bindexes into structured, prompt-ready inputs for downstream LLM workflows.

## Getting Started

### Prerequisites

- **Java** (see version notes below)
- **Maven** (to build and run tests)
- **MongoDB** (only required for registry operations such as register/search)
- A configured **GenAI provider** compatible with MachAI’s `GenAIProvider` integration (required for classification/embedding and AI-assisted generation)

Environment/configuration you may need (depending on what you use):

- `BINDEX_REPO_URL`: MongoDB connection URI for repository-backed operations
- `BINDEX_REG_PASSWORD`: password used for registry authentication when required

### Java Version

- **Build configuration (from `pom.xml`)**: `maven.compiler.release = 8` (Java 8 bytecode)
- **Practical runtime requirements**: Java 8+ is expected for compilation and baseline usage. Some integrations (for example MongoDB driver usage or GenAI provider implementations available on your classpath) may impose stricter runtime requirements; validate against your dependency set and target deployment.

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
2. Use `BindexCreator` to create/update `bindex.json`.
3. Use `BindexRegister`/`BindexRepository` to register the Bindex in MongoDB.
4. Use `Picker` to classify a query and retrieve matching Bindexes (with dependency expansion).
5. Use `ApplicationAssembly` to turn the selected Bindexes into structured context for an LLM workflow.

## Configuration

Common configuration parameters:

| Parameter | Description | Default |
|---|---|---|
| `bindex.model` | Model/provider identifier used by `BindexCreator` and its builders. | `CodeMie:gpt-5-2-2025-12-11` |
| `pick.model` | Model/provider identifier used by `Picker` for classification prompts. | `CodeMie:gpt-5-2-2025-12-11` |
| `assembly.model` | Model/provider identifier used by `ApplicationAssembly`. | `CodeMie:gpt-5-2-2025-12-11` |
| `BINDEX_REPO_URL` | MongoDB connection URI for repository-backed operations. | (unset; falls back to a default cluster URI) |
| `BINDEX_REG_PASSWORD` | MongoDB registry password used to authenticate registration/search (when required). | (unset; depends on registry configuration) |

## Resources

- Documentation site: https://machai.machanism.org/bindex-core/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-core
