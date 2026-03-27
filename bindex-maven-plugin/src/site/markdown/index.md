---
<!-- @guidance:
Page Structure: 
# Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([!\[Maven Central\](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
2. Introduction
   - Provide a comprehensive description of the GW Maven plugin, including its purpose and benefits.
   - Analyze java files in the `src/main/java` to inform the description.
3. Overview
   - Clearly explain the main functions and value proposition of the GW Maven plugin.
   - Summarize how the plugin enhances project workflows and documentation.
4. Key Features
   - Present a bulleted list of the primary capabilities and unique features of the plugin.
5. Getting Started
   - **Prerequisites:**  
     - List all required software, services, and environment settings needed to use the plugin.
   - **Java Version:**  
     Note that the required Java version is defined in `pom.xml`, but actual functional requirements may differ. Clearly state both.
   - **Basic Usage:**  
     - Provide an example command for running the plugin.
   - **Typical Workflow:**  
     - Outline the step-by-step process for using the plugin and its artifacts.
6. Configuration
   - Include a table of common configuration parameters, with columns for parameter name, description, and default value.
   - Ensure descriptions are clear and concise.
   - Use `MAVEN_OPTS="\-\-add\-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming"` to run `mvn bindex:register`.
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
canonical: https://machai.machanism.org/bindex-maven-plugin/index.html
---

# Bindex Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

## Introduction

Bindex Maven Plugin is a Maven plugin that integrates the Machai/Bindex indexing workflow into your build.

It scans the current project directory using a configured GenAI provider/model and generates (or updates) **Bindex metadata**. This structured index is used within the Machanism ecosystem for library discovery, semantic search, integration, and assembly workflows.

The plugin also supports publishing the generated metadata to an external registry endpoint, allowing other tools and services to discover your project by its capabilities rather than only by coordinates.

## Overview

The plugin provides four goals:

- `create`: generate a new Bindex index for the current module
- `update`: refresh an existing Bindex index (incremental/update mode)
- `register`: optionally update and then publish Bindex metadata to a registry URL
- `clean`: remove temporary artifacts produced during indexing (currently, the `.machai/bindex-inputs.txt` inputs log)

Modules with `pom` packaging (typical aggregator/parent modules) are automatically skipped by `create`, `update`, and `register`.

## Key Features

- Maven goals for **create**, **update**, **register**, and **clean**
- Uses your module’s Maven base directory and POM model to build a `MavenProjectLayout`, then runs Bindex against it
- Required model selection via `-Dbindex.model=Provider:Model` (for example, `OpenAI:gpt-5`)
- Optional credential resolution from `~/.m2/settings.xml` via `-Dgenai.serverId=...` (maps to `GENAI_USERNAME` / `GENAI_PASSWORD`)
- Registry publishing via `-Dbindex.register.url=...` (defaults to `BindexRepository.DB_URL`)
- Lifecycle integration:
  - `create`, `update`, `register` default to the Maven `install` phase
  - `clean` defaults to the Maven `clean` phase
- Automatically skips `pom`-packaged aggregator projects

## Getting Started

### Prerequisites

- Apache Maven
- Java Development Kit (JDK)
- A supported GenAI provider/model configured via `bindex.model`
- (Optional) Maven `settings.xml` credentials when using `genai.serverId`
- (Optional) Network access to the registry endpoint used by `register`

### Java Version

- **Build/compile level (from `pom.xml`):** `maven.compiler.release=8` (Java 8 bytecode)
- **Practical runtime requirement:** Maven itself runs on a Java runtime (often Java 11+). In addition, the `register` goal may require extra JVM options on newer JDKs (see below).

### Basic Usage

```bash
mvn org.machanism.machai:bindex-maven-plugin:create -Dbindex.model=OpenAI:gpt-5
```

### Typical Workflow

1. Select the GenAI model: `-Dbindex.model=Provider:Model`.
2. Create the initial index: `mvn ...:create`.
3. Update the index after code changes: `mvn ...:update`.
4. Register metadata (optional): `mvn ...:register -Dbindex.register.url=...`.
5. Clean temporary artifacts (optional): `mvn ...:clean`.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `bindex.model` | GenAI provider/model identifier used by Bindex (required). Example: `OpenAI:gpt-5`. | *(none; required)* |
| `genai.serverId` | Maven `settings.xml` `<server>` id used to load GenAI credentials and any additional provider configuration. Populates `GENAI_USERNAME` / `GENAI_PASSWORD` when present. | *(unset)* |
| `bindex.register.url` | Registry database endpoint URL used by `register` to publish metadata. | `BindexRepository.DB_URL` |
| `update` | When running `register`, whether to update/refresh index content before publishing. | `true` |

To run `register`, use:

```bash
set MAVEN_OPTS=--add-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming
mvn bindex:register -Dbindex.model=OpenAI:gpt-5 -Dbindex.register.url=http://localhost:8080
```

## Resources

- Documentation: https://machai.machanism.org/bindex-maven-plugin/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin
- Related component (Bindex Core): https://machai.machanism.org/bindex-core/
