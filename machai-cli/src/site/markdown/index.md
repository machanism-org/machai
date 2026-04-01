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

Machai CLI is a Spring Boot + Spring Shell command-line application for running Machai workflows from an interactive console.

It helps you:

- **Generate and manage metadata** for projects (bindex) to improve library discoverability and automation.
- **Scan and process a project tree** using Ghostwriter guidance, with configurable model, instructions, concurrency, and exclusions.
- **Run reusable “Acts”** (prompt templates) interactively against a scanned project.
- **Clean temporary artifacts** created by Machai workflows.

## Overview

Machai CLI starts a Spring Shell interactive console via `org.machanism.machai.cli.MachaiCLI` and exposes command groups implemented in `src/main/java/org/machanism/machai/cli`:

- **Configuration (`set`)** (`ConfigCommand`) — reads/writes persistent defaults in `machai.properties`.
- **Ghostwriter processing (`gw`)** (`GWCommand`) — scans directories/files and runs Ghostwriter’s guidance pipeline via `GuidanceProcessor`, supporting:
  - project directory selection (defaulting to current working directory)
  - scan directories (`--scanDir`) and directory exclusions (`--excludes`)
  - GenAI provider/model selection (`--model`)
  - instructions (inline text, URL, or file path; interactive prompt if empty)
  - concurrency (`--threads`)
  - optional logging of LLM request inputs (`--logInputs`)
- **Act mode (`act`)** (`ActCommand`) — scans the configured project and runs a predefined Act via `ActProcessor` (interactive prompt support).
- **Cleanup (`clean`)** (`CleanCommand`) — removes `.machai` temporary folders from a directory tree.

Configuration is loaded from `machai.properties` in the working directory when available.

## Getting Started

### Prerequisites

- **Java 17**
- Network access to your selected **GenAI provider** (provider/model dependent)

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

Machai CLI delegates authentication/provider configuration to the underlying GenAI provider implementation. Common environment variables (provider/model dependent) include:

| Variable | Description | When needed | Example |
|---|---|---|---|
| `OPENAI_API_KEY` | API key for OpenAI models | Using OpenAI | `sk-...` |
| `AZURE_OPENAI_API_KEY` | API key for Azure OpenAI | Using Azure OpenAI | `...` |
| `AZURE_OPENAI_ENDPOINT` | Azure OpenAI endpoint URL | Using Azure OpenAI | `https://<resource>.openai.azure.com/` |
| `ANTHROPIC_API_KEY` | API key for Anthropic models | Using Anthropic | `...` |

If your selected provider requires different variables, set those required by that provider.

## Configuration

Machai CLI persists user defaults in `machai.properties` (in the working directory). You can set values using the `set` command.

### Common configuration parameters

| Property key | Description | Default |
|---|---|---|
| `projectDir` | Default project directory used by commands that operate on a folder tree | Current working directory |
| `gw.model` | Default GenAI provider/model used by `gw` and `act` | (none) |
| `gw.instructions` | Default Ghostwriter system instructions source (text/URL/file path) | (none) |
| `logInputs` | Whether to log LLM request inputs to dedicated files | `false` |

### Typical workflow

1. Set defaults:

   ```text
   set --key projectDir --value ./my-project
   set --key gw.model --value OpenAI:gpt-5.1
   ```

2. Run Ghostwriter guidance over a directory:

   ```text
   gw --scanDir ./my-project --excludes target,.git --threads 4 --logInputs true
   ```

3. Run an Act over the scanned project:

   ```text
   act commit
   act sonar-fix --model OpenAI:gpt-5.1
   ```

4. Clean temporary artifacts (optional):

   ```text
   clean --dir ./my-project
   ```

## Usage

Start the shell:

```bash
java -jar machai.jar
```

Then use the following commands.

### `set` (configuration)

- Set a value:

  ```text
  set --key gw.model --value OpenAI:gpt-5.1
  ```

- Get a value:

  ```text
  set --key gw.model
  ```

### `gw` (Ghostwriter guidance scanning)

```text
gw --scanDir ./my-project --excludes target,.git --threads 4 --instructions "You are a strict code reviewer" --logInputs true
```

### `act` (Ghostwriter Act mode)

```text
act commit
act commit "and push"
```

### `clean` (remove temporary folders)

```text
clean --dir ./my-project
```

## Resources

- GitHub (parent project): https://github.com/machanism-org/machai
- Maven Central artifact: https://central.sonatype.com/artifact/org.machanism.machai/machai-cli
- Download JAR: https://sourceforge.net/projects/machanism/files/machai/machai.jar/download
