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

Machai CLI is a Spring Boot + Spring Shell command-line application for generating, registering, and managing library metadata (Bindex) within the Machanism ecosystem. It also supports GenAI-assisted semantic search (library picking), project assembly from selected libraries, and Ghostwriter-based document/file processing.

Key benefits:

- **Automated Bindex generation** to keep metadata descriptors consistent and up to date.
- **Registry integration** to centralize and query metadata across projects.
- **Semantic library picking** using natural-language prompts and similarity scoring.
- **Guided project assembly** to bootstrap a project skeleton from chosen libraries.
- **Ghostwriter pipeline** for scanning and processing documents/files with GenAI guidance.

## Overview

Machai CLI boots a Spring Shell environment (REPL-style) and exposes several command groups implemented under `src/main/java/org/machanism/machai/cli`.

- **Application bootstrap**
  - `MachaiCLI` loads system properties from `machai.properties` (or from the file specified by `-Dconfig=...`) and starts Spring Boot / Spring Shell.

- **Persisted defaults (configuration)**
  - `ConfigCommand` reads/writes common defaults into `machai.properties` via `PropertiesConfigurator` (e.g., default `genai`, `dir`, and `score`).

- **Bindex metadata generation and registration**
  - `BindexCommand`
    - `bindex`: scans a project directory and generates Bindex metadata.
    - `register`: scans a directory and registers Bindex metadata into a registry service (via `--registerUrl`).

- **Semantic search (picking) and project assembly**
  - `AssembyCommand`
    - `pick`: selects libraries matching a prompt (or prompt file) using similarity scoring.
    - `assembly`: creates a project skeleton from picked libraries (optionally reuses the previous pick if `--query` is omitted).
    - `prompt`: sends an ad-hoc prompt to the configured GenAI provider (useful for guidance).

- **Ghostwriter document/file processing**
  - `GWCommand`
    - `gw`: scans one or more directories and processes files using GenAI guidance and optional instructions.

- **Cleanup utility**
  - `CleanCommand`
    - `clean`: removes all `.machai` temporary/template folders under a target directory.

## Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.9+** (for building from source)
- Credentials for a supported **GenAI provider** (depends on your selected provider/model)
- Optional: access to a **Bindex registry service** (for `register`, and for remote-backed `pick/assembly` setups)

### Environment Variables

| Name | Required | Description | Example |
|---|---:|---|---|
| `OPENAI_API_KEY` | If using OpenAI | API key for OpenAI-based providers | `sk-...` |
| `GW_HOME` | Optional | Ghostwriter home directory for runtime/logs; if absent, the CLI uses the directory where the JAR is located | `C:\\tools\\machai` |

> You may need additional environment variables depending on the GenAI provider you configure.

### Installation

- Download the runnable JAR:

  [![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/machai.jar/download)

- Run:

  ```bat
  java -jar machai.jar
  ```

- Optional: load system properties from a specific file:

  ```bat
  java -Dconfig=machai.properties -jar machai.jar
  ```

## Configuration

Machai CLI stores common defaults in `machai.properties`. You can manage these values interactively using `config` commands.

### Common configuration parameters

| Key | Description | Default |
|---|---|---|
| `genai` | Default GenAI provider/model identifier when `--genai` is omitted | `CodeMie:gpt-5-2-2025-12-11` |
| `dir` | Default working/project directory when `--dir` is omitted | Current user directory |
| `score` | Default minimum similarity threshold used by semantic picking | `0.90` |
| `registerUrl` | Default registry URL used by `register` and (optionally) by `pick/assembly` | (not set) |
| `GW_ROOTDIR` | Root directory used by Ghostwriter when scanning (property key used by `Ghostwriter.GW_ROOTDIR_PROP_NAME`) | Current user directory |
| `GW_INSTRUCTIONS` | Default Ghostwriter instructions (property key used by `Ghostwriter.GW_INSTRUCTIONS_PROP_NAME`) | (not set) |
| `GW_GUIDANCE` | Default Ghostwriter guidance (property key used by `Ghostwriter.GW_GUIDANCE_PROP_NAME`) | (not set) |

> Exact Ghostwriter property keys are defined by constants in `org.machanism.machai.gw.processor.Ghostwriter`.

### Typical workflow

1. **Set defaults** (optional):

   ```text
   config genai CodeMie:gpt-5-2-2025-12-11
   config dir C:\\work\\my-project
   config score 0.90
   ```

2. **Generate Bindex metadata**:

   ```text
   bindex --dir C:\\work\\my-project
   ```

3. **Register Bindex metadata** (optional):

   ```text
   register --dir C:\\work\\my-project --registerUrl https://your-registry.example/api
   ```

4. **Pick libraries** (semantic search):

   ```text
   pick --query "Create a Spring Boot web app with REST and PostgreSQL" --score 0.90
   ```

5. **Assemble** a project skeleton:

   ```text
   assembly --dir C:\\work\\out
   ```

## Usage

Machai CLI is typically used in an interactive Spring Shell session. The commands below are the primary user-facing operations.

### Configure defaults

- Set default GenAI provider/model:

  ```text
  config genai CodeMie:gpt-5-2-2025-12-11
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

### Generate and register Bindex

- Generate Bindex files:

  ```text
  bindex --dir C:\\work\\my-project --genai CodeMie:gpt-5-2-2025-12-11 --update false
  ```

- Register Bindex files:

  ```text
  register --dir C:\\work\\my-project --registerUrl https://your-registry.example/api --update true --genai CodeMie:gpt-5-2-2025-12-11
  ```

### Pick libraries and assemble a project

- Pick libraries from a prompt:

  ```text
  pick --query "Create a web app" --score 0.90 --genai CodeMie:gpt-5-2-2025-12-11
  ```

- Assemble a project in a directory:

  ```text
  assembly --dir C:\\work\\out --genai CodeMie:gpt-5-2-2025-12-11
  ```

- Use a prompt file instead of inline text:

  ```text
  pick --query C:\\work\\prompts\\app.txt
  assembly --query C:\\work\\prompts\\app.txt --dir C:\\work\\out
  ```

### Process documents/files (Ghostwriter)

- Scan and process one or more directories:

  ```text
  gw --genai CodeMie:gpt-5-2-2025-12-11 --threads false --scanDirs C:\\work\\my-project\\docs
  ```

- Provide inline instructions/guidance (empty value triggers interactive input):

  ```text
  gw --instructions "" --guidance "" --scanDirs C:\\work\\my-project
  ```

### Clean temporary folders

- Remove all `.machai` folders under a directory:

  ```text
  clean --dir C:\\work\\my-project
  ```

### Example: configure and run with custom parameters

```bat
java -Dconfig=machai.properties -jar machai.jar
```

Then, in the shell:

```text
config genai CodeMie:gpt-5-2-2025-12-11
config dir C:\\work\\demo
config score 0.85
bindex
pick --query "Generate a Spring Boot REST service"
assembly --dir C:\\work\\demo\\generated
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/machai-cli
- Machanism organization: https://github.com/machanism-org
