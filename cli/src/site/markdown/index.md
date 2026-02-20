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

Machai CLI is a Spring Shell-based command-line application for generating, registering, and managing library metadata (bindex) within the Machanism ecosystem. It integrates with GenAI providers to automate two common workflows:

- **Metadata automation**: generate and register bindex descriptors for projects to enable semantic search and discovery.
- **AI-assisted assembly**: pick relevant libraries for an app idea and assemble a project skeleton guided by the selected libraries.

It also includes utilities for **cleaning Machai temporary directories** and for **processing documents/files** via the Ghostwriter pipeline.

## Overview

The CLI boots a Spring Boot + Spring Shell REPL (or executes single commands) and exposes the following command groups:

- **Application entry point**
  - `org.machanism.machai.cli.MachaiCLI`: starts the CLI and optionally loads system properties from `machai.properties` (or from the file specified by `-Dconfig=...`).

- **Configuration**
  - `ConfigCommand`: manages persisted defaults in `machai.properties` used by other commands (default GenAI model, default working directory, default similarity score).

- **Bindex (metadata) tooling**
  - `BindexCommand`:
    - `bindex`: generate bindex files for a project directory.
    - `register`: register bindex files into an external registry database.

- **Semantic search + assembly**
  - `AssembyCommand`:
    - `pick`: performs semantic picking of libraries matching a prompt.
    - `assembly`: assembles a project skeleton based on picked libraries.
    - `prompt`: sends an ad-hoc prompt to the configured GenAI provider.

- **Ghostwriter file processing**
  - `GWCommand`:
    - `gw`: scans and processes directories/files using GenAI guidance and optional instructions.

- **Cleanup utility**
  - `CleanCommand`:
    - `clean`: removes `.machai` temporary/template folders under a target directory.

## Getting Started

### Prerequisites

- **Java 17+** (project is built with `maven.compiler.release=17`)
- **Maven 3.9+** (recommended for building from source)
- Access to a supported **GenAI provider** (for example OpenAI) and the required credentials
- Optional: access to a **Bindex registry** endpoint (when using `register` and when `pick/assembly` are configured to query a remote store)

### Environment Variables

| Name | Required | Description | Example |
|---|---:|---|---|
| `OPENAI_API_KEY` | For OpenAI | API key used by the OpenAI provider | `sk-...` |
| `GW_HOME` | Optional | Home directory for Ghostwriter runtime/logs; if absent, the CLI uses the directory where the JAR is located | `C:\\tools\\machai` |

> Additional provider-specific variables may be required depending on the configured GenAI provider.

### Installation

- Download the runnable JAR:

  [![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/machai.jar/download)

- Run (example):

  ```bat
  java -jar machai.jar
  ```

- Optional: load system properties from a specific file:

  ```bat
  java -Dconfig=machai.properties -jar machai.jar
  ```

## Configuration

Machai CLI persists common defaults in `machai.properties` (when present). You can set these values interactively using `config` commands.

### Common configuration parameters

| Key | Description | Default |
|---|---|---|
| `genai` | Default GenAI provider/model identifier | `OpenAI:gpt-5-mini` |
| `dir` | Default working/project directory used when `--dir` is omitted | Current user directory |
| `score` | Default minimum similarity threshold for semantic library picking | `0.90` |

### Typical workflow

1. **Set defaults** (optional):

   ```text
   config genai OpenAI:gpt-5-mini
   config dir C:\\work\\my-project
   config score 0.90
   ```

2. **Generate bindex metadata** for a project:

   ```text
   bindex --dir C:\\work\\my-project
   ```

3. **Register bindex** (optional, if you use a registry database):

   ```text
   register --dir C:\\work\\my-project --registerUrl https://your-registry.example/api
   ```

4. **Pick libraries** for an application idea (semantic search):

   ```text
   pick --query "Create a Spring Boot web app with REST and PostgreSQL" --score 0.90
   ```

5. **Assemble** a project skeleton:

   ```text
   assembly --dir C:\\work\\out
   ```

## Usage

Machai CLI can be used interactively (REPL) or by invoking a single command (depending on how you package/launch Spring Shell in your environment). The commands below are the primary user-facing operations.

### Configure defaults

- Set default GenAI provider/model:

  ```text
  config genai OpenAI:gpt-5-mini
  ```

- Set default working directory:

  ```text
  config dir C:\\work\\my-project
  ```

- Set default semantic-search score:

  ```text
  config score 0.90
  ```

- Display current configuration:

  ```text
  config conf
  ```

### Generate and register bindex

- Generate bindex files:

  ```text
  bindex --dir C:\\work\\my-project --genai OpenAI:gpt-5-mini --update false
  ```

- Register bindex files:

  ```text
  register --dir C:\\work\\my-project --registerUrl https://your-registry.example/api --update true
  ```

### Pick libraries and assemble a project

- Pick libraries from a prompt:

  ```text
  pick --query "Create a web app" --score 0.90 --genai OpenAI:gpt-5-mini
  ```

- Assemble a project in a directory:

  ```text
  assembly --dir C:\\work\\out --genai OpenAI:gpt-5-mini
  ```

- Use a prompt file instead of inline text:

  ```text
  pick --query C:\\work\\prompts\\app.txt
  assembly --query C:\\work\\prompts\\app.txt --dir C:\\work\\out
  ```

### Process documents/files (Ghostwriter)

- Process a directory with optional guidance and instructions:

  ```text
  gw --root C:\\work\\my-project --scanDirs C:\\work\\my-project\\docs --genai OpenAI:gpt-5-mini --threads false
  ```

### Clean temporary folders

- Remove all `.machai` folders under a directory:

  ```text
  clean --dir C:\\work\\my-project
  ```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/machai-cli
- Machanism organization: https://github.com/machanism-org
