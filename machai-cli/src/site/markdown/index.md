<!-- @guidance:
Page Structure: 
# Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
# Introduction
   - Full description of purpose and benefits.
# Overview
   - Analyze `src/main/java/org/machanism/machai/cli` classes and summarize functional to provide this the application.
# Getting Started
   - Prerequisites: List of required software and services.
   - Environment Variables: Table describing necessary environment variables.
   - Add the Machai CLI application jar download link: [![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/machai.jar/download) to the installation section.
# Configuration
   - Table of common configuration parameters, their descriptions, and default values.
   - Typical Workflow: Step-by-step outline of how to use the project artifacts.
# Usage
   - Detail explain how to use it.
   - Example: Command-line example showing how to configure and run the plugin with custom parameters.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Machai CLI

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-cli.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai-cli)

## Introduction

Machai CLI is a command-line tool for generating, registering, and managing library metadata within the Machanism ecosystem. It leverages GenAI to automate project assembly and enable semantic search for efficient library discovery and integration.

Beyond metadata workflows, Machai CLI also provides a “Ghostwriter” mode that can scan a directory tree and apply GenAI guidance (and reusable “Acts”) to help you refactor, review, or otherwise transform files at scale.

## Overview

Machai CLI is implemented as a Spring Boot application that starts a Spring Shell interactive console. The CLI exposes several command groups:

- **Ghostwriter processing (`gw`)**: scans directories/files and runs a guidance pipeline, with support for custom instructions, default guidance, excludes, concurrency, and request input logging.
- **Act mode (`act`)**: executes predefined reusable “acts” (prompt templates) interactively after scanning the configured project directory.
- **Bindex workflows (`bindex`, `register`)**: generates bindex metadata for a project and optionally registers it in an external registry service.
- **Semantic pick + assembly (`pick`, `assembly`)**: performs semantic search for libraries (bindex entries) and assembles an output project from selected results.
- **Prompt helper (`prompt`)**: sends a one-off prompt to the configured GenAI provider.
- **Cleanup (`clean`)**: removes temporary `.machai` folders from a directory tree.
- **Persistent configuration (`set`)**: stores and retrieves defaults in `machai.properties`.

At startup, the application attempts to load additional JVM system properties from `machai.properties` (or a file provided via `-Dconfig=/path/to/file`).

## Getting Started

### Prerequisites

- **Java 17** (runtime required)
- Network access to your chosen **GenAI provider** (depending on the configured model)
- (Optional) Access to a **bindex registry service** if you plan to register metadata (`registerUrl`)

### Installation

- Download the Machai CLI application JAR:

  [![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/machai.jar/download)

- Run the CLI (Spring Shell interactive mode):

  ```bash
  java -jar machai.jar
  ```

- (Optional) Use an explicit configuration file at startup:

  ```bash
  java -Dconfig=./machai.properties -jar machai.jar
  ```

### Environment Variables

Machai CLI delegates authentication and provider configuration to the underlying GenAI provider implementation. The following environment variables are commonly required (depending on the provider/model you choose):

| Variable | Description | Required | Example |
|---|---|---:|---|
| `OPENAI_API_KEY` | API key for OpenAI models | Provider-specific | `sk-...` |
| `AZURE_OPENAI_API_KEY` | API key for Azure OpenAI | Provider-specific | `...` |
| `AZURE_OPENAI_ENDPOINT` | Azure OpenAI endpoint URL | Provider-specific | `https://<resource>.openai.azure.com/` |
| `ANTHROPIC_API_KEY` | API key for Anthropic models | Provider-specific | `...` |

If your chosen provider uses different variables, set those required by that provider.

## Configuration

Machai CLI persists user defaults in `machai.properties` (in the working directory). You can set values using the `set` command.

### Common configuration parameters

| Property key | Description | Default |
|---|---|---|
| `projectDir` | Default project directory used by commands that operate on a folder tree | Current working directory |
| `gw.model` | Default GenAI provider/model for Ghostwriter workflows | (none) |
| `gw.instructions` | Default system instructions (text/URL/file path) for `gw` | (none) |
| `gw.guidance` | Default guidance (text/URL/file path) for `gw` | (none) |
| `gw.logInputs` | Whether to log LLM request inputs to dedicated log files | `false` |
| `score` | Default similarity threshold used for semantic search (`pick` / `assembly`) | `0.65` |

### Typical workflow

1. **Set defaults** (recommended):

   ```text
   set --key projectDir --value ./my-project
   set --key gw.model --value OpenAI:gpt-5.1
   set --key score --value 0.8
   ```

2. **Generate bindex metadata** for a project:

   ```text
   bindex --dir ./my-project --update false --model OpenAI:gpt-5.1
   ```

3. **Register bindex metadata** (optional):

   ```text
   register --dir ./my-project --registerUrl https://registry.example/api --update true
   ```

4. **Pick + assemble** an application from libraries:

   ```text
   pick --query "Create a web app" --score 0.8
   assembly --dir ./out
   ```

5. **Run Ghostwriter guidance** over a directory:

   ```text
   gw --scanDir ./my-project --excludes target,.git --threads 4 --logInputs true
   ```

## Usage

Start the shell:

```bash
java -jar machai.jar
```

Then use the following commands.

### `set` (configuration)

- Set a config value:

  ```text
  set --key gw.model --value OpenAI:gpt-5.1
  ```

- Get a config value:

  ```text
  set --key gw.model
  ```

### `gw` (Ghostwriter guidance pipeline)

Scan a directory tree and apply GenAI guidance:

```text
gw --scanDir ./my-project --threads 4 --excludes target,.git --model OpenAI:gpt-5.1 --guidance "Refactor for clarity" --logInputs true
```

### `act` (Ghostwriter Act mode)

Run a predefined act (prompt template), optionally with extra prompt text:

```text
act commit
act commit "and push"
```

### `bindex` (generate bindex metadata)

```text
bindex --dir ./my-project --update false --model OpenAI:gpt-5.1
```

### `register` (register bindex metadata)

```text
register --dir ./my-project --registerUrl https://registry.example/api --update true --model OpenAI:gpt-5.1
```

### `pick` and `assembly` (semantic search + project assembly)

```text
pick --query "Create a web app" --score 0.8 --model OpenAI:gpt-5.1
assembly --dir ./out
```

### `prompt` (one-off GenAI prompt)

```text
prompt --query "Explain what this project does" --model OpenAI:gpt-5.1 --dir ./my-project
```

### `clean` (remove temporary folders)

```text
clean --dir ./my-project
```

## Resources

- GitHub (parent project): https://github.com/machanism-org/machai
- Maven Central artifact: https://central.sonatype.com/artifact/org.machanism.machai/machai-cli
- Download JAR: https://sourceforge.net/projects/machanism/files/machai/machai.jar/download
