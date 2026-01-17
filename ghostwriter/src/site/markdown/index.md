<!-- @guidance:
Page Structure: 
# Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
# Introduction
   - Full description of purpose and benefits.
# Overview
   - Explanation of the project function and value proposition.
# Key Features
   - Bulleted list highlighting the primary capabilities of the project.
# Getting Started
   - Prerequisites: List of required software and services.
   - Environment Variables: Table describing necessary environment variables.
   - Add the Machai CLI application jar download link: [Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/gw.jar/download) to the installation section.
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

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis.

It helps teams keep documentation accurate and consistent by applying structured rules (guidance) across Markdown and other documentation artifacts, while preserving `@guidance` markers in-place.

## Overview

Ghostwriter is a CLI tool that:

- traverses a project directory (or specific directories you pass on the command line)
- reads documentation and source files
- uses embedded guidance markers (for example `@guidance` comments) to determine mandatory rules
- leverages a configured GenAI provider/model to synthesize updates
- writes updates back to files while preserving guidance markers

## Key Features

- CLI-based documentation scanning and processing
- Project-wide traversal with optional directory targeting
- Pluggable GenAI provider/model selection
- Optional custom processing instructions (inline or from a file)
- Optional multi-threaded processing mode

## Getting Started

### Prerequisites

- Java 17+ (recommended)
- Maven 3.9+ (for building)
- Network access to your configured GenAI provider (if required by the selected model)

### Environment Variables

The CLI accepts most configuration via command-line options. Environment variables may be required depending on the GenAI provider you use.

| Variable | Required | Description |
|---|---:|---|
| `OPENAI_API_KEY` | No* | API key used by OpenAI-backed models. Required only if you select an OpenAI model/provider. |

\* Provider-specific; set the variables required by your chosen GenAI service.

### Installation

- Build from source (example):

```bash
mvn -DskipTests package
```

- Download the CLI jar:

[[Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")]((https://sourceforge.net/projects/machanism/files/gw.jar/download))

### Basic Usage

```bash
java -jar gw.jar -d /path/to/project
```

### Typical Workflow

1. Choose (or set) a GenAI provider/model.
2. Point Ghostwriter at a project root directory.
3. Optionally provide additional instructions (inline or from a file).
4. Run a scan against the root directory or specific subdirectories.
5. Review changes produced by the scan and commit updates.

## Configuration

Ghostwriter CLI options are defined in `org.machanism.machai.gw.Ghostwriter`.

### Command-line Options

| Option | Long option | Arg | Default | Description |
|---|---|---:|---|---|
| `-h` | `--help` | No | N/A | Displays help information for usage. |
| `-t` | `--threads` | No | `false` | Enable multi-threaded processing. |
| `-d` | `--dir` | Yes | `${user.dir}` | Path to the project root directory. |
| `-g` | `--genai` | Yes | Provider default (resolved by configuration) | GenAI provider and model identifier (for example `OpenAI:gpt-5.1`). |
| `-i` | `--instructions` | Yes | None | Additional file-processing instructions. Provide either the instruction text directly, or a path to a file containing the instructions. |

Notes:

- Positional arguments: any additional arguments after options are treated as scan directory paths. If none are provided, the scan target defaults to the resolved root directory.

### Example

Run against a project root, enable multi-threading, select a model, and provide extra instructions:

```bash
java -jar gw.jar \
  -d /path/to/project \
  -t \
  -g OpenAI:gpt-5.1 \
  -i /path/to/instructions.txt
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
