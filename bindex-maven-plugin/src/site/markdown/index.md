---
canonical: https://machai.machanism.org/bindex-maven-plugin/index.html
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

# Bindex Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

## Introduction

Bindex Maven Plugin enables automated generation, updating, and registration of **Bindex metadata** for Maven projects within the Machanism/Machai ecosystem.

It provides Maven goals (Mojos) that scan your project directory, build a Bindex index (optionally incrementally), and—when desired—publish that metadata to an external registry endpoint. The plugin is intended to support library discovery, integration, and assembly workflows by producing structured metadata that can be searched and consumed by other Machai tooling.

## Overview

The plugin integrates Bindex operations directly into the Maven lifecycle so you can:

- **Create** a fresh Bindex index and related artifacts for a project.
- **Update** an existing Bindex index (refresh/incremental mode).
- **Register** (publish) Bindex metadata to a registry service/database.
- **Clean** temporary artifacts produced by the tooling (for example, an inputs log file under `.machai`).

Projects with `pom` packaging (typical parent/aggregator modules) are automatically skipped for create/update/register.

## Key Features

- Maven goals for **create**, **update**, **register**, and **clean**
- **Model/provider selection** via a required `bindex.model` property (e.g., `OpenAI:gpt-5`)
- Optional **credential resolution from `~/.m2/settings.xml`** via `gw.genai.serverId`
- Registration support via `bindex.register.url` (defaults to the Bindex repository DB URL)
- Lifecycle integration (`install` phase for create/update/register; `clean` phase for clean)

## Getting Started

### Prerequisites

- Apache Maven
- Java Development Kit (JDK)
- Access to a supported GenAI provider/model (configured via `bindex.model`)
- (Optional) Maven `settings.xml` server credentials if your provider requires authentication
- (Optional) A reachable Bindex registry endpoint for `register`

### Java Version

- **Build/compile level (from `pom.xml`):** `maven.compiler.release=8` (Java 8 bytecode)
- **Practical runtime requirement:** depends on your Maven runtime and the Machai/Bindex dependencies in your environment. Many modern Maven setups run on Java 11+; if you use `register`, you may also need additional JVM module exports (see below).

### Basic Usage

Run a goal using the fully-qualified plugin coordinates:

```bash
mvn org.machanism.machai:bindex-maven-plugin:create -Dbindex.model=OpenAI:gpt-5
```

### Typical Workflow

1. **Choose a GenAI provider/model** and set it via `-Dbindex.model=...`.
2. **Create** the initial index:
   - `mvn ...:create`
3. **Iterate** on your project and periodically **update**:
   - `mvn ...:update`
4. If you have a registry service available, **register** the metadata:
   - `mvn ...:register -Dbindex.register.url=...`
5. If you enabled input logging and want to remove temporary artifacts, run **clean**:
   - `mvn ...:clean`

## Configuration

Common configuration parameters:

| Parameter | Description | Default |
|---|---|---|
| `bindex.model` | AI provider/model identifier used by Bindex (required). Example: `OpenAI:gpt-5`. | *(none; required)* |
| `gw.genai.serverId` | Optional `settings.xml` `<server>` id used to read GenAI credentials and populate `GENAI_USERNAME` / `GENAI_PASSWORD` for the workflow. | *(unset)* |
| `bindex.register.url` | Registry database endpoint URL to publish metadata to when running `register`. | `BindexRepository.DB_URL` |
| `update` | When running `register`, whether to update/refresh index content before publishing. | `true` |

To run the `register` goal, use the required JVM export setting:

```bash
set MAVEN_OPTS=--add-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming
mvn bindex:register -Dbindex.model=OpenAI:gpt-5 -Dbindex.register.url=http://localhost:8080
```

## Resources

- Documentation: https://machai.machanism.org/bindex-maven-plugin/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin
- Related component (Bindex Core): https://machai.machanism.org/bindex-core/
