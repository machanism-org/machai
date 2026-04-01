<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
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

Machai CLI is a command-line tool for generating, registering, and managing library metadata within the Machanism ecosystem. It leverages GenAI to automate project assembly and to enable semantic search, improving the speed and accuracy of library discovery and integration.

## Overview

Machai CLI is a Spring Boot + Spring Shell application that provides commands to run Machai workflows from a terminal:

- **Application entry point** (`MachaiCLI`): boots Spring and starts the Spring Shell runtime.
- **Ghostwriter processing** (`GWCommand`): scans directories/files and processes documents using GenAI guidance; supports excludes, per-run model selection, custom system instructions, concurrency, and optional logging of LLM inputs.
- **Act mode** (`ActCommand`): runs a predefined “act” prompt interactively and applies it to the scanned project context.
- **Configuration management** (`ConfigCommand`): reads/writes persistent settings in `machai.properties` (for example, default model, scan directory, and project directory).
- **Cleanup utility** (`CleanCommand`): removes Machai temporary folders (`.machai`) from a project tree.

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+ (for building from source)
- Network access to your selected GenAI provider (for example OpenAI, etc.)

### Installation

- Download the runnable jar:

[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/machai.jar/download)

- Run:

```bash
java -jar machai.jar
```

### Environment Variables

The CLI delegates GenAI access to the configured provider. Set the provider-specific credentials in your environment.

| Variable | Required | Description | Example |
|---|---:|---|---|
| `OPENAI_API_KEY` | Provider-specific | API key for OpenAI (when using an OpenAI model). | `sk-...` |
| `AZURE_OPENAI_ENDPOINT` | Provider-specific | Azure OpenAI endpoint (when using Azure OpenAI). | `https://...openai.azure.com/` |
| `AZURE_OPENAI_API_KEY` | Provider-specific | Azure OpenAI API key (when using Azure OpenAI). | `...` |
| `ANTHROPIC_API_KEY` | Provider-specific | API key for Anthropic (when using Anthropic models). | `sk-ant-...` |

## Configuration

Machai CLI uses `machai.properties` for persistent defaults. You can set values using the `config` command.

### Common Parameters

| Key | Description | Default |
|---|---|---|
| `project.dir` | Root directory used by commands when a directory is not provided explicitly. | Current working directory |
| `gw.model` | Default GenAI provider/model identifier (for example `OpenAI:gpt-5.1`). | *(unset)* |
| `gw.scanDir` | Default scan directory path. | `project.dir` |
| `gw.instructions` | System instructions (text, URL, or file path) used by Ghostwriter. | *(unset)* |
| `gw.excludes` | Comma-separated list of directories to exclude from scanning. | *(unset)* |
| `genai.logInputs` | Whether to log LLM request inputs to dedicated log files. | `false` |

### Typical Workflow

1. (Optional) Set the default project directory:
   ```bash
   config set --key project.dir --value .\my-project
   ```
2. Configure the default GenAI model:
   ```bash
   config set --key gw.model --value OpenAI:gpt-5.1
   ```
3. (Optional) Configure a default scan directory:
   ```bash
   config set --key gw.scanDir --value src\main\java
   ```
4. Run Ghostwriter processing:
   ```bash
   gw --gw.threads 4 --gw.scanDir src\main\java
   ```
5. Clean temporary `.machai` artifacts when needed:
   ```bash
   clean --project.dir .\my-project
   ```

## Usage

Machai CLI starts an interactive Spring Shell session. Type `help` to list available commands and options.

### Commands

- `gw`: Scan and process directories or files using GenAI guidance.
- `act`: Run Act mode interactively for a predefined act/prompt.
- `config set`: Set or get persistent configuration values in `machai.properties`.
- `clean`: Remove `.machai` temporary folders from a directory tree.

### Example: Run Ghostwriter with custom parameters

```bash
# Set defaults (optional)
config set --key project.dir --value .\my-project
config set --key gw.model --value OpenAI:gpt-5.1

# Run Ghostwriter with explicit overrides
gw --gw.threads 4 \
   --gw.model OpenAI:gpt-5.1 \
   --gw.instructions "Follow repository guidance comments strictly." \
   --gw.excludes target,.git,.machai \
   --project.dir .\my-project \
   --gw.scanDir src\main\java src\site
```

### Example: Run Act mode

```bash
# Execute an act (prompt template) and then answer interactive questions
act commit "and push"
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/machai-cli
- Machanism (organization): https://github.com/machanism-org
