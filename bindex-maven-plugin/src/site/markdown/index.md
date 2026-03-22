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

Bindex Maven Plugin is a Maven plugin that generates, updates, and registers **Bindex metadata** for Maven projects within the Machai ecosystem.

It provides build goals that:

- **Create** a Bindex index for a project (typically during `install`)
- **Update** (refresh) an existing index
- **Register** the project’s metadata to an external registry endpoint for discovery
- **Clean** temporary artifacts produced during indexing

The plugin integrates with Machai’s GenAI capabilities by requiring an AI provider/model identifier and (optionally) loading provider credentials from Maven `settings.xml`.

## Overview

At a high level, the plugin scans the current Maven project (skipping aggregator/parent modules with `pom` packaging), builds a `MavenProjectLayout` from the project directory and POM model, and then invokes the Bindex core engine to:

- Produce structured metadata for the module
- Maintain that metadata over time via an update mode
- Publish the metadata to a registry for organization-wide discovery and assembly

This helps teams standardize how library/project metadata is produced and makes it easier to discover, integrate, and assemble components (including via semantic search) in the broader Machai toolchain.

## Key Features

- Maven goals for **create**, **update**, **register**, and **clean** workflows
- Skips modules with `pom` packaging (parent/aggregator projects)
- Configurable GenAI provider/model via `-Dgw.genai.model=...`
- Optional credential resolution from `~/.m2/settings.xml` via `-Dgw.genai.serverId=...`
- Registry publishing support with configurable URL (`-Dbindex.register.url=...`)
- Logs GenAI provider usage at the end of each goal execution

## Getting Started

### Prerequisites

- Apache Maven
- Access to a supported GenAI provider/model (as configured for Machai)
- (Optional) Maven `settings.xml` server credentials if your provider requires authentication
- (Optional) Access to a Bindex registry endpoint for registration

### Java Version

- **Configured compile level (POM):** Java **8** (`maven.compiler.release=8`)
- **Practical runtime requirement:** Java **8+** is expected for this module, but the broader Machai/GenAI stack may impose additional requirements depending on the configured provider, TLS/runtime environment, and registry connectivity.

### Basic Usage

Run a goal by fully qualifying the plugin:

```bash
mvn org.machanism.machai:bindex-maven-plugin:create -Dgw.genai.model=OpenAI:gpt-5
```

### Typical Workflow

1. Configure the GenAI provider/model:
   - Provide the model identifier via `-Dgw.genai.model=...`.
2. (Optional) Configure credentials in `~/.m2/settings.xml`:
   - Add a `<server>` entry.
   - Run with `-Dgw.genai.serverId=<id>` to load `GENAI_USERNAME` / `GENAI_PASSWORD`.
3. Generate metadata locally:
   - Run `bindex-maven-plugin:create` to create a new index.
4. Refresh metadata over time:
   - Run `bindex-maven-plugin:update` to refresh an existing index.
5. Publish metadata to a registry:
   - Run `bindex-maven-plugin:register` with a registry URL.
6. Clean temp files:
   - Run `bindex-maven-plugin:clean` to remove the `.machai/bindex-inputs.txt` log file.

## Configuration

Common parameters:

| Parameter | Description | Default |
|---|---|---|
| `gw.genai.model` | GenAI provider/model identifier used by the plugin (passed through to Bindex), e.g. `OpenAI:gpt-5`. | *(required)* |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to load GenAI credentials (`GENAI_USERNAME` / `GENAI_PASSWORD`). | *(none)* |
| `bindex.register.url` | Registry endpoint URL used by the `register` goal to publish metadata. | `Picker.DB_URL` |
| `update` | Whether `register` should update metadata as part of registration. | `true` |

When running `register`, you may need to add an export for JDK DNS internals depending on your environment:

```bash
set MAVEN_OPTS="--add-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming"
mvn bindex:register -Dgw.genai.model=OpenAI:gpt-5
```

## Resources

- Documentation: https://machai.machanism.org/bindex-maven-plugin/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin
- Related module (Bindex core): https://machai.machanism.org/bindex-core/
