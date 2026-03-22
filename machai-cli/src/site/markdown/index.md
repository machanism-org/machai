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

At its core, Machai CLI wraps the Machai “bindex” and “ghostwriter” capabilities behind an interactive Spring Shell interface, allowing you to:

- Generate metadata (“bindex”) for local projects
- Register that metadata in a remote registry
- Perform semantic search across registered libraries and assemble new projects from the results
- Run Ghostwriter guidance pipelines to refactor/transform code and docs
- Run predefined “Act” prompts for common tasks

## Overview

The CLI is implemented as a Spring Boot + Spring Shell application (`org.machanism.machai.cli.MachaiCLI`) and provides a set of shell commands:

- **`gw`** (`GWCommand`): scans files/directories and runs the Ghostwriter guidance pipeline (instructions + guidance) with configurable concurrency and excludes.
- **`act`** (`ActCommand`): runs Ghostwriter in “Act mode” to execute predefined actions/prompts after scanning the configured directory.
- **`bindex`** (`BindexCommand`): generates bindex metadata for a project directory.
- **`register`** (`BindexCommand`): registers generated bindex metadata to an external registry service.
- **`pick`** (`AssembyCommand`): semantic search for libraries matching a natural-language query.
- **`assembly`** (`AssembyCommand`): assembles a new project from previously picked libraries (or runs a fresh pick if a query is provided).
- **`prompt`** (`AssembyCommand`): sends a one-off prompt to the configured GenAI provider and prints the response.
- **`clean`** (`CleanCommand`): deletes all `.machai` temporary folders under a directory.
- **`config set`** (`ConfigCommand`): persists CLI defaults into `machai.properties` (and reads current values).

The application also supports loading system properties from `machai.properties` at startup (or from a custom file via `-Dconfig=...`).

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+ (for building from source)
- Network access to your chosen GenAI provider (for example, OpenAI)
- (Optional) A Machai registry service endpoint if you want to use `register`/`pick` against a remote database

### Environment Variables

Machai CLI relies on provider-specific environment variables for GenAI access. Common examples include:

| Variable | Required | Description |
|---|---:|---|
| `OPENAI_API_KEY` | Often | API key for OpenAI-backed models (if using an OpenAI provider). |
| `OPENAI_BASE_URL` | No | Overrides the default OpenAI API base URL (useful for proxies/compatible endpoints). |

> The exact variables depend on the GenAI provider you configure (see `--model` usage and your provider documentation).

### Installation

#### Download

[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/machai.jar/download)

#### Build from source

```bash
mvn -Ppack -DskipTests install
```

This produces an executable jar named `machai.jar`.

#### Run

```bash
java -jar machai.jar
```

To load configuration from a custom file:

```bash
java -Dconfig=./machai.properties -jar machai.jar
```

## Configuration

Machai CLI reads and persists defaults in `machai.properties` (in the working directory), and also reads some keys for Ghostwriter and bindex workflows.

### Common configuration parameters

| Key | Description | Default |
|---|---|---|
| `gw.model` | Default GenAI provider/model for Ghostwriter-based commands. | Provider default (varies) |
| `gw.projectDir` | Default project root directory used for scanning/processing. | Current working directory |
| `gw.guidance` | Default guidance text (or URL/file reference) for `gw`. | (none) |
| `gw.instructions` | Default system instructions (or URL/file reference) for `gw`. | (none) |
| `gw.logInputs` | Whether to log LLM request inputs. | `false` |
| `score` | Similarity threshold used by semantic search (`pick`/`assembly`). | `0.5` |
| `dir` | Default working directory used by some commands (for example `prompt`/`clean`). | Current working directory |

> Notes:
> - Keys prefixed with `gw.` are used by Ghostwriter processors.
> - The `config set` command can be used to persist any key/value into `machai.properties`.

### Typical workflow

1. Set defaults (optional):

   ```bash
   config set --key gw.model --value OpenAI:gpt-5.1
   config set --key gw.projectDir --value .
   config set --key score --value 0.8
   ```

2. Generate bindex metadata for a project:

   ```bash
   bindex --dir .\my-project
   ```

3. Register the project metadata to a registry:

   ```bash
   register --dir .\my-project --registerUrl https://registry.example/api
   ```

4. Find libraries for a new app idea and assemble a project:

   ```bash
   pick --query "Create a web app" --score 0.8
   assembly --dir .\out
   ```

5. Apply Ghostwriter transformations to a codebase:

   ```bash
   gw --scanDir .\my-project --excludes target,.git --threads 4
   ```

6. Clean `.machai` temp folders:

   ```bash
   clean --dir .\my-project
   ```

## Usage

Start the CLI:

```bash
java -jar machai.jar
```

You will be dropped into an interactive shell. Use `help` to list available commands.

### Command examples

Configure defaults:

```bash
config set --key gw.model --value OpenAI:gpt-5.1
config set --key gw.projectDir --value .
config set --key gw.logInputs --value true
```

Run Ghostwriter guidance processing:

```bash
gw --scanDir .\my-project --excludes target,.git --threads 4 --guidance "Refactor for clarity"
```

Run an Act prompt:

```bash
act commit
act commit "and push"
```

Generate and register bindex metadata:

```bash
bindex --dir .\my-project --update false
register --dir .\my-project --registerUrl https://registry.example/api --update true
```

Semantic search and assemble:

```bash
pick --query "Create a web app" --score 0.8
assembly --dir .\out
```

One-off GenAI prompt:

```bash
prompt --query "Summarize this project" --model OpenAI:gpt-5.1 --dir .
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central (artifact): https://central.sonatype.com/artifact/org.machanism.machai/machai-cli
- Project downloads: https://sourceforge.net/projects/machanism/files/machai/machai.jar/download
