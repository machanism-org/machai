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

![](images/machai-ghostwriter-logo.png)

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Introduction

Ghostwriter is an AI-assisted documentation engine for Maven/Java projects. It scans project sources and documentation, interprets embedded guidance tags, and generates or updates documentation artifacts in a consistent, repeatable way.

It is designed for use in local workflows and CI pipelines to keep documentation accurate as the code evolves.

## Overview

Ghostwriter runs as a CLI application. You point it at a project directory (or let it default to the current working directory) and optionally provide:

- A GenAI provider/model selection.
- Additional processing instructions (inline or from a file).
- A list of directories to scan.

It then traverses the target directories and processes documentation-related files according to the embedded guidance and the provided instructions.

## Key Features

- Scans project directories and processes documentation artifacts.
- Supports pluggable GenAI provider/model selection.
- Accepts additional instruction input (inline text or via file path).
- Optional multi-threaded processing.
- Works well in scripts and CI as a single runnable JAR.

## Getting Started

### Prerequisites

- Java 9+ runtime.
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

To scan specific directories (relative to the working directory), pass them as trailing arguments:

```bash
java -jar gw.jar /path/to/project
```

### Typical Workflow

1. Build or download `gw.jar`.
2. Choose a working directory (the project root) and optionally the directories to scan.
3. (Optional) Provide additional instructions (inline or as a file path).
4. Run Ghostwriter.
5. Review and commit generated/updated documentation.

## Configuration

Ghostwriter supports the following command-line options (from `org.machanism.machai.gw.Ghostwriter`).

### Command-line Options

| Option | Long | Argument | Description | Default |
|---|---|---:|---|---|
| `-h` | `--help` | No | Displays help information for usage. | Off |
| `-t` | `--threads` | No | Enable multi-threaded processing. | Off |
| `-d` | `--dir` | Yes | Path to the project directory (project root). | Current working directory (resolved via `Config.getWorkingDir(...)`) |
| `-g` | `--genai` | Yes | GenAI service provider and model (e.g. `OpenAI:gpt-5.1`). | Resolved by `Config.getChatModel(...)` |
| `-i` | `--instructions` | Yes | Additional file-processing instructions (either the instruction text directly, or a path to a file containing instructions). | None |

**Positional arguments**: one or more directories to scan. If none are provided, Ghostwriter scans the resolved root directory.

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

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download CLI JAR: https://sourceforge.net/projects/machanism/files/gw.jar/download
