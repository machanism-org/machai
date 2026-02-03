<!-- @guidance:
Page Structure: 
# Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge [![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
# Introduction
   - Full description of purpose and benefits.
# Overview
   - Explanation of the project function and value proposition.
# Key Features
   - Bulleted list highlighting the primary capabilities of the project.
# Getting Started
   - Prerequisites: List of required software and services.
   - Environment Variables: Table describing necessary environment variables.
   - Add the Ghostwriter CLI application jar download link: [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download) to the installation section.
   - Basic Usage: Example command to run the application.
   - Typical Workflow: Step-by-step outline of how to use the project artifacts.
# Configuration
   - Analyze /java/org/machanism/machai/gw/Ghostwriter.java java source file and generate cmd options description.
   - Table of cmd options, their descriptions, and default values.
   - Example: Command-line example showing how to configure and run the application with custom parameters.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Introduction

Ghostwriter is an AI-assisted documentation engine that scans a project workspace, extracts embedded `@guidance` instructions from files, and assembles consistent, review-ready documentation.
It can be run locally or in CI to keep documentation aligned with the codebase and project requirements.

## Overview

Ghostwriter is a CLI tool that:

- Scans one or more directories or glob patterns under a chosen root.
- Loads optional instruction sources (URL/file/stdin) to guide generation.
- Optionally applies a default guidance text as a final pass.
- Logs progress and results for traceability.

## Key Features

- CLI-driven scanning of directories and glob patterns.
- Embedded `@guidance` discovery and application during documentation processing.
- Optional external instructions via URL(s), file path(s), or stdin.
- Optional default guidance applied as a final step.
- Configurable GenAI provider/model selection.
- Optional multi-threaded processing.
- Directory exclusion support.
- Optional logging of LLM request inputs.

## Getting Started

### Prerequisites

- Java 11+
- Network access to your chosen GenAI provider (if required by your configuration)
- (Optional) A `gw.properties` file to provide defaults (see option defaults below)

### Environment Variables

Ghostwriter itself is configured primarily via CLI options and `gw.properties`. Your GenAI provider may require additional environment variables (for example, API keys). Configure those according to the provider you select.

| Variable | Required | Description |
|---|---:|---|
| *(provider-specific)* | Varies | Credentials and settings required by the selected GenAI provider/model. |

### Installation

- Download the Ghostwriter CLI distribution:

  [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```bash
java -jar gw.jar <scanDir | glob_path_pattern>
```

Examples (Windows):

```bash
# scan a directory
java -jar gw.jar C:\projects\project

# specify root explicitly
java -jar gw.jar -r C:\projects\project src\project

# scan with a glob pattern
java -jar gw.jar -r C:\projects\project "**/*.java"
```

### Typical Workflow

1. Choose a root directory (or let it default to the current user directory).
2. Run Ghostwriter against one or more scan targets (directories or glob patterns).
3. Provide additional instructions (optional) via `--instructions` (URL/file) or via stdin.
4. Provide default guidance (optional) via `--guidance` (file) or via stdin.
5. Review the produced/updated documentation and logs.

## Configuration

Ghostwriter supports the following command-line options (see `org.machanism.machai.gw.Ghostwriter`):

| Option | Long | Arg | Default | Description |
|---|---|---:|---|---|
| `-h` | `--help` | No | — | Show help and exit. |
| `-l` | `--logInputs` | No | `false` | Log LLM request inputs to dedicated log files. |
| `-r` | `--root` | Yes | From `gw.properties` (`root`); otherwise the current user directory | Root directory used to validate scan targets and compute related paths. |
| `-t` | `--threads` | Yes (optional) | `true` | Enable/disable multi-threaded processing. If provided without a value, defaults to `true`. |
| `-a` | `--genai` | Yes | From `gw.properties` (`genai`); otherwise `OpenAI:gpt-5-mini` | GenAI provider and model selector (format: `Provider:Model`). |
| `-i` | `--instructions` | Yes (optional) | From `gw.properties` (`instructions`) | Additional instructions source(s). Provide a comma-separated list of URLs/file paths, or pass the option without a value to enter instruction text via stdin. Relative file paths are resolved from the executable directory. |
| `-g` | `--guidance` | Yes (optional) | — | Default guidance applied as a final step for the current directory. Provide a file path, or pass the option without a value to enter guidance text via stdin. Relative file paths are resolved from the executable directory. |
| `-e` | `--excludes` | Yes | From `gw.properties` (`excludes`) | Directories to exclude from processing. Provide a comma-separated list. |

Command-line example with custom parameters:

```bash
java -jar gw.jar C:\projects\project \
  -r C:\projects\project \
  -a OpenAI:gpt-5-mini \
  -t true \
  -i https://example.com/instructions.md,local-instructions.md \
  -g default-guidance.md \
  -e target,.git,node_modules \
  -l
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- CLI download: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
