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
   - Add the Machai CLI application jar download link: [![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/gw.jar/download) to the installation section.
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

Machai Ghostwriter is a CLI documentation engine that automates and standardizes project documentation and code annotation. Using guided file processing with embedded `@guidance` blocks, it helps teams keep documentation consistent, reviewable, and up to date across multi-module repositories. It’s designed to work well in scripts and CI so documentation changes can be generated and committed as part of your normal workflow.

<iframe class="youtube" title="Ghostwriter | Machai" src="https://www.youtube.com/embed/Z3jFvJLKS2I"></iframe>

## Overview

Ghostwriter scans a project directory and processes documentation-related files according to embedded guidance and any additional instructions you provide.

You can optionally specify:

- A GenAI provider/model (for example, `OpenAI:gpt-5.1`).
- Additional processing instructions (inline or loaded from a file).
- One or more directories to include in the scan.

Learn more about guided file processing: https://machanism.org/guided-file-processing/index.html

## Key Features

- Scans directories and updates documentation artifacts according to embedded guidance.
- Supports pluggable GenAI provider/model selection.
- Accepts additional instruction input (inline text or via file path).
- Optional multi-threaded processing.
- Runs as a single runnable JAR for scripts and CI.

## Getting Started

### Prerequisites

- Java 11+ runtime.
- Network access to your selected GenAI provider (if applicable).

### Environment Variables

Ghostwriter itself does not require environment variables, but your chosen GenAI provider may.

| Name | Description | Example |
|------|-------------|---------|
| `OPENAI_API_KEY` | API key for OpenAI provider (if using OpenAI). | `sk-...` |
| `OPENAI_BASE_URL` | Custom base URL for OpenAI-compatible services. | `https://api.openai.com` |

### Installation

Download the CLI JAR:

[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/gw.jar/download)

### Basic Usage

```bash
java -jar gw.jar
```

Scan a specific project root directory:

```bash
java -jar gw.jar --dir /path/to/project
```

Scan one or more specific directories (positional arguments):

```bash
java -jar gw.jar . docs src
```

### Typical Workflow

1. Build or download `gw.jar`.
2. Choose the project root (`--dir`) and the directories to scan (positional args).
3. (Optional) Provide additional instructions (inline or via `--instructions /path/to/file`).
4. Run Ghostwriter.
5. Review and commit generated/updated documentation.

## Configuration

Ghostwriter supports the following command-line options (from `org.machanism.machai.gw.Ghostwriter`).

### Command-line Options

| Option | Long Option | Argument | Description | Default |
|--------|------------|:--------:|-------------|---------|
| `-h` | `--help` | No | Displays help information for usage. | Off |
| `-t` | `--threads` | No | Enable multi-threaded processing. | Off |
| `-d` | `--dir` | Yes | The path to the project directory (project root). | Value from `gw.properties` key `dir`; otherwise the current working directory |
| `-g` | `--genai` | Yes | Specifies the GenAI service provider and model (for example, `OpenAI:gpt-5.1`). | Value from `gw.properties` key `genai`; otherwise `OpenAI:gpt-5-mini` |
| `-i` | `--instructions` | Yes | Additional file processing instructions. Provide either the instruction text directly or the path to a file containing the instructions; if the provided value is an existing file, its contents are used. | None |

**Positional arguments**: Zero or more directories to scan. If none are provided, Ghostwriter scans the resolved root directory.

### Example

Scan the current project using multi-threaded processing, specify a GenAI provider/model, and provide instructions from a file:

```bash
java -jar gw.jar \
  --threads \
  --genai OpenAI:gpt-5.1 \
  --instructions ./ghostwriter-instructions.txt \
  .
```

## Resources

- Platform: https://machanism.org/guided-file-processing/index.html
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download CLI JAR: https://sourceforge.net/projects/machanism/files/gw.jar/download
