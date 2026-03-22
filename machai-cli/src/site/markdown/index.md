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

Machai CLI is a Spring Shell-based command-line tool for generating, registering, and managing library metadata within the Machanism ecosystem.

It leverages GenAI to:

- generate library metadata (“bindex”) from source code and project descriptors
- register metadata in a remote registry for sharing and discovery
- perform semantic search (“pick”) across registered libraries
- assemble an application/project using selected libraries
- run the Ghostwriter guidance pipeline over files and folders
- execute predefined Ghostwriter “acts” (reusable interactive prompt workflows)

## Overview

Machai CLI exposes Machai capabilities as interactive commands:

- **`gw`**: scans folders/files and applies Ghostwriter guidance (refactoring/review/documentation-style automation) via the `GuidanceProcessor`.
- **`act`**: runs Ghostwriter in “Act mode” using `ActProcessor` (predefined actions/prompts).
- **`bindex`**: generates bindex metadata for a project directory using `BindexCreator`.
- **`register`**: registers generated bindex metadata into a remote registry using `BindexRegister`.
- **`pick`**: performs semantic search for libraries matching a prompt using `Picker` and prints ranked results.
- **`assembly`**: assembles an output project using `ApplicationAssembly` from the picked libraries.
- **`prompt`**: sends a one-off prompt to the configured GenAI provider.
- **`clean`**: removes Machai temporary folders (`.machai`) from a directory tree.
- **`set`**: sets/gets persistent configuration values stored in `machai.properties`.

## Getting Started

### Prerequisites

- **Java 17+**
- **Internet access** (required for GenAI providers and optionally for registry access)
- **GenAI provider account/credentials** (for example OpenAI, depending on your configured provider)

### Installation

- Download the CLI jar:

  [![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/machai.jar/download)

- Run the CLI:

  ```bash
  java -jar machai.jar
  ```

### Environment Variables

Machai CLI itself does not require specific environment variables, but GenAI providers typically do. Configure the variables required by your selected provider.

| Variable | Description | Required | Example |
|---|---|---:|---|
| `OPENAI_API_KEY` | API key for OpenAI-backed models (if using OpenAI provider) | Provider-dependent | `sk-...` |
| `ANTHROPIC_API_KEY` | API key for Anthropic-backed models (if using Anthropic provider) | Provider-dependent | `...` |
| `AZURE_OPENAI_API_KEY` | API key for Azure OpenAI (if using Azure provider) | Provider-dependent | `...` |
| `AZURE_OPENAI_ENDPOINT` | Endpoint URL for Azure OpenAI resource | Provider-dependent | `https://<resource>.openai.azure.com/` |

## Configuration

Configuration defaults are stored in `machai.properties` (in the working directory) and are used by all commands.

You can also load system properties at startup from `machai.properties` or specify an alternative file via:

```bash
java -Dconfig=path\\to\\machai.properties -jar machai.jar
```

### Common configuration parameters

| Parameter | Description | Default |
|---|---|---|
| `dir` | Default working directory used by some commands | Current user directory |
| `projectDir` | Default root directory for scanning/processing (`-d/--projectDir`) | Current user directory |
| `genai` | Default GenAI provider/model identifier (for example `OpenAI:gpt-5.1`) | Command-dependent |
| `gw.model` | Default model for Ghostwriter/GW flows | _not set_ |
| `gw.instructions` | Default Ghostwriter system instructions (text/URL/path) | _not set_ |
| `gw.guidance` | Default Ghostwriter guidance (text/URL/path) | _not set_ |
| `gw.logInputs` | Log LLM request inputs to files | `false` |
| `gw.scanDir` | Default scan directory for Act mode (if not provided) | falls back to project/user dir |
| `score` | Similarity threshold used by semantic search (`pick`) | `0.75` (module default) |

> Note: exact defaults may vary by module/version; the CLI resolves missing values from `machai.properties` when available.

### Typical workflow

1. Configure defaults (model, directory, score):

   ```bash
   set --key genai --value OpenAI:gpt-5.1
   set --key projectDir --value .
   set --key score --value 0.8
   ```

2. Generate metadata for a project:

   ```bash
   bindex --projectDir .\\my-lib --model OpenAI:gpt-5.1
   ```

3. Register the metadata to a registry service:

   ```bash
   register --dir .\\my-lib --registerUrl https://registry.example/api --update true
   ```

4. Search for libraries semantically:

   ```bash
   pick --query "Create a web app" --score 0.8
   ```

5. Assemble an output project from selected libraries:

   ```bash
   assembly --dir .\\out
   ```

## Usage

Start the interactive shell:

```bash
java -jar machai.jar
```

### Command reference (high level)

- `set --key <k> [--value <v>]`: set or get a configuration value in `machai.properties`.
- `gw [options]`: scan and process files using Ghostwriter guidance.
- `act <actName> [extra prompt text...]`: run an Act mode workflow.
- `bindex [options]`: generate bindex files for a project.
- `register [options]`: register bindex data to a registry.
- `pick [options]`: semantic search for libraries matching a prompt.
- `assembly [options]`: assemble a project from the picked libraries.
- `prompt [options]`: run a one-off prompt against the configured provider.
- `clean [options]`: remove `.machai` temporary directories.

### Example

Run Ghostwriter over a project with custom parameters:

```bash
java -jar machai.jar \
  --spring.shell.interactive.enabled=true

# In the Machai shell:
set --key gw.model --value OpenAI:gpt-5.1
set --key gw.guidance --value "Refactor for readability; keep behavior unchanged."
gw --scanDir .\\my-project --excludes target,.git --threads 4 --logInputs true
```

## Resources

- GitHub (monorepo): https://github.com/machanism-org/machai
- Maven Central artifact: https://central.sonatype.com/artifact/org.machanism.machai/machai-cli
- Machanism organization: https://github.com/machanism-org
