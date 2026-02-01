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

Machai Ghostwriter is a CLI documentation engine that automates and standardizes project documentation and code annotation. Using guided file processing with embedded `@guidance` blocks, it helps teams keep documentation consistent, reviewable, and up to date across repositories. It’s designed to work well in scripts and CI so documentation changes can be generated and committed as part of your normal workflow.

<iframe class="youtube" title="Ghostwriter | Machai" src="https://www.youtube.com/embed/Z3jFvJLKS2I"></iframe>

## Overview

Ghostwriter scans a project directory and processes documentation-related files according to embedded guidance and any additional instructions you provide.

You can optionally specify:

- A GenAI provider/model (for example, `OpenAI:gpt-5.1`).
- Additional processing instructions (loaded from `--instructions` or `gw.properties`).
- A root directory that bounds scanning (`--root`).
- One or more directories to include in the scan (positional arguments).

Learn more about guided file processing: https://machanism.org/guided-file-processing/index.html

## Key Features

- Scans directories and updates documentation artifacts according to embedded guidance.
- Supports pluggable GenAI provider/model selection.
- Accepts additional instruction input (loaded from `--instructions` and/or `gw.properties`).
- Optional multi-threaded processing.
- Optional final default guidance step via `--guidance`.
- Supports excluding directories via `--excludes`.
- Runs as a single runnable JAR for scripts and CI.

## Getting Started

### Prerequisites

- Java 11+ runtime.
- Network access to your selected GenAI provider (if applicable).

### Environment Variables

Ghostwriter itself does not require environment variables, but your chosen GenAI provider may.

#### For OpenAI-Compatible Services

| Variable Name     | Description |
|------------------|-------------|
| `OPENAI_API_KEY` | API key for authenticating requests to the configured GenAI provider (e.g., OpenAI, Azure OpenAI). |

#### For CodeMie Integration

| Variable Name    | Description |
|-----------------|-------------|
| `GENAI_USERNAME` | Username for authenticating with CodeMie. |
| `GENAI_PASSWORD` | Password for authenticating with CodeMie. |

**Note:**
- Only set the variables relevant to your selected provider.
- Ensure all credentials are kept secure and never committed to version control.
- For additional configuration details, refer to the provider’s documentation.

### Installation

Download the CLI JAR:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```bash
java -jar gw.jar
```

Scan a specific root directory:

```bash
java -jar gw.jar --root /path/to/project
```

Scan one or more directories (positional arguments):

```bash
java -jar gw.jar --root /path/to/project docs src
```

### Typical Workflow

1. Build or download `gw.jar`.
2. Choose the project root (`--root`) and the directories to scan (positional args).
3. (Optional) Provide additional instructions via `--instructions` or `gw.properties`.
4. (Optional) Exclude directories via `--excludes`.
5. (Optional) Provide a default final guidance file via `--guidance`.
6. Run Ghostwriter.
7. Review and commit generated/updated documentation.

## Configuration

Ghostwriter supports the following command-line options (from `org.machanism.machai.gw.Ghostwriter`).

### Command-line Options

| Short | Long | Arg | Description | Default |
|------:|------|:---:|-------------|---------|
| `-h` | `--help` | No | Show help message and exit. | Off |
| `-t` | `--threads` | Optional (`true`/`false`) | Enable multi-threaded processing. If present without a value, defaults to `true`. | `true` |
| `-r` | `--root` | Yes (path) | Root directory that bounds scanning. All scanned directories must be located within this root. | From `gw.properties` key `root`; otherwise current user directory |
| `-a` | `--genai` | Yes (`provider:model`) | GenAI provider and model (e.g., `OpenAI:gpt-5.1`). | From `gw.properties` key `genai`; otherwise `OpenAI:gpt-5-mini` |
| `-i` | `--instructions` | Yes (URL/path[,URL/path...]) | Additional instruction locations (URL or file path). Multiple locations may be comma-separated. | From `gw.properties` key `instructions` (comma-separated); otherwise none |
| `-g` | `--guidance` | Optional (path) | Default guidance file applied as a final step. If present without a value, uses `@guidance.txt` resolved relative to the executable directory (the directory containing `gw.jar`). | Off (not applied) |
| `-e` | `--excludes` | Yes (dir[,dir...]) | Directories to exclude from processing. Multiple values may be provided by repeating the option; values may also be comma-separated. | From `gw.properties` key `excludes` (comma-separated); otherwise none |

**Positional arguments**: Zero or more directories to scan.

- If no directories are provided:
  - When `--root` is not set (and no `root` in `gw.properties`), the root defaults to the current user directory and that directory is scanned.
  - When `--root` is set (or configured), the directory scanned defaults to the current user directory.

### Example

Scan a project with multi-threaded processing, specify a GenAI provider/model, load extra instructions from a file, exclude build output, and apply a default guidance file:

```bash
java -jar gw.jar \
  --threads \
  --root . \
  --genai OpenAI:gpt-5.1 \
  --instructions ghostwriter-instructions.txt \
  --excludes target,.git \
  --guidance @guidance.txt \
  src docs
```

## Resources

- Platform: https://machanism.org/guided-file-processing/index.html
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download Ghostwriter CLI: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
