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

Bindex Maven Plugin is a Maven plugin that generates, updates, registers, and cleans **Bindex metadata** for Maven projects in the Machai ecosystem.

The plugin wraps the Bindex core engine and integrates it into standard Maven workflows so that projects can:

- **Create** a Bindex index for a module (typically during `install`)
- **Update** (refresh) an existing index
- **Register** the project’s metadata to an external registry endpoint for discovery
- **Clean** temporary artifacts produced during indexing

To support semantic indexing, the plugin requires a GenAI provider/model identifier and can optionally load provider credentials from Maven `settings.xml`.

## Overview

The plugin is implemented as a set of Mojos (`create`, `update`, `register`, `clean`). For indexing-related goals, it:

- Skips aggregator/parent modules with `pom` packaging
- Builds a `MavenProjectLayout` using `${basedir}` and the Maven project model
- Invokes the Bindex core engine in either **create** or **update** mode to generate structured project metadata
- Optionally publishes metadata to a registry via the `register` goal

This standardizes how project/library metadata is produced across teams and improves discovery, integration, and assembly in the broader Machai toolchain.

## Key Features

- Goals for **create**, **update**, **register**, and **clean**
- Skips `pom`-packaged (aggregator) projects
- Requires an AI provider/model via `-Dgw.genai.model=...`
- Optional credential resolution from `~/.m2/settings.xml` via `-Dgw.genai.serverId=...`
- Registry publishing with configurable endpoint URL (`-Dbindex.register.url=...`)
- Optional input logging controlled by Bindex/Machai properties (passed through to the core engine)
- Logs GenAI provider usage after goal execution

## Getting Started

### Prerequisites

- Java (see version notes below)
- Apache Maven
- Access to a supported GenAI provider/model (as configured for Machai)
- (Optional) Maven `settings.xml` credentials if your provider requires authentication
- (Optional) Network access to a Bindex registry endpoint for registration

### Java Version

- **Configured compile level (POM):** Java **8** (`maven.compiler.release=8`)
- **Practical runtime requirement:** Java **8+** is typically sufficient for local indexing, but the effective runtime requirements can be higher depending on:
  - Maven version in use
  - TLS/runtime constraints for contacting the GenAI provider and/or registry
  - Whether you run under newer JDKs where additional module exports may be required (see `register` notes below)

### Basic Usage

Run a goal by fully qualifying the plugin:

```bash
mvn org.machanism.machai:bindex-maven-plugin:create -Dgw.genai.model=OpenAI:gpt-5
```

### Typical Workflow

1. Choose a GenAI provider/model and pass it on the command line:
   - `-Dgw.genai.model=<Provider>:<Model>`
2. (Optional) Configure credentials in `~/.m2/settings.xml`:
   - Add a `<server>` entry.
   - Run with `-Dgw.genai.serverId=<id>` so the plugin can load `GENAI_USERNAME` / `GENAI_PASSWORD`.
3. Generate metadata locally:
   - Run `bindex-maven-plugin:create`.
4. Refresh metadata over time:
   - Run `bindex-maven-plugin:update`.
5. Publish metadata to a registry:
   - Run `bindex-maven-plugin:register` and provide a URL if you are not using the default.
6. Clean temp files:
   - Run `bindex-maven-plugin:clean` to remove `.machai/bindex-inputs.txt`.

## Configuration

Common parameters:

| Parameter | Description | Default |
|---|---|---|
| `gw.genai.model` | GenAI provider/model identifier used by the plugin and passed through to Bindex (e.g. `OpenAI:gpt-5`). | *(required)* |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to load GenAI credentials (`GENAI_USERNAME` / `GENAI_PASSWORD`). | *(none)* |
| `bindex.register.url` | Registry endpoint URL used by the `register` goal to publish metadata. | `BindexRepository.DB_URL` |
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
