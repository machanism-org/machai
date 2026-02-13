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
   - Add the Ghostwriter CLI application jar download link: [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download) to the installation section.
   - Basic Usage: Example command to run the application.
   - Typical Workflow: Step-by-step outline of how to use the project artifacts.
# Configuration
   - Analyze /java/org/machananism/machai/gw/Ghostwriter.java java source file and generate cmd options description.
   - Table of cmd options, their descriptions, and default values.
   - Example: Command-line example showing how to configure and run the application with custom parameters.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Introduction

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis. It helps teams keep documentation accurate and consistent by generating updates directly from the source tree and the rules embedded in documentation files.

## Overview

Ghostwriter runs as a CLI that traverses one or more directories (or file patterns), extracts `@guidance` blocks embedded in documents, and uses a configured GenAI provider/model to synthesize or review content. It supports applying default, directory-level guidance as a final step, while allowing file-specific guidance to steer output.

## Key Features

- Scans directories or patterns (including `glob:` / `regex:` style inputs) and processes supported document types
- Uses embedded guidance tags to drive consistent, repeatable documentation output
- Optional default guidance and system instructions, supplied inline, via URL, or from local files
- Multi-threaded processing for faster runs on large trees
- Optional logging of LLM request inputs for auditability and debugging

## Getting Started

### Prerequisites

- Java 11+ (JRE or JDK)
- Network access to your configured GenAI provider (if applicable)

### Installation

- Download the Ghostwriter CLI bundle:

  [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```bat
java -jar gw.jar C:\projects\my-project
```

### Typical Workflow

1. Add `@guidance` blocks to documentation files where you want consistent, rule-driven output.
2. Configure your GenAI provider/model (via `gw.properties` or CLI).
3. Run Ghostwriter against a project directory (or a pattern) to generate/review documentation updates.
4. Review changes and commit the updated documentation.

## Configuration

Ghostwriter can be configured using `gw.properties` (or `-Dgw.config=<path>`), and/or by CLI options.

### Command-Line Options

| Option | Long option | Argument | Default | Description |
|---|---|---:|---|---|
| `-h` | `--help` | No | — | Show help message and exit. |
| `-l` | `--logInputs` | No | `false` | Log LLM request inputs to dedicated log files. |
| `-t` | `--threads` | Yes (optional) | `true` | Enable multi-threaded processing to improve performance. Use `-t false` to disable. |
| `-r` | `--root` | Yes | User directory (if not configured) | Root directory used as the base for scanning. If not provided, the user directory is used unless configured via properties. |
| `-a` | `--genai` | Yes | `OpenAI:gpt-5-mini` | GenAI provider and model, e.g. `OpenAI:gpt-5.1`. |
| `-i` | `--instructions` | Yes (optional) | From properties (or none) | System instructions as plain text, by URL (`http(s)://...`), or by file path (`file:...`). If used without a value, reads from stdin until EOF. |
| `-g` | `--guidance` | Yes (optional) | From properties (or none) | Default guidance applied as a final step. Accepts plain text, URL, or `file:` input. If used without a value, reads from stdin until EOF. |
| `-e` | `--excludes` | Yes | From properties (or none) | Comma-separated list of directories to exclude from processing. |

### Example

```bat
java -Dgw.config=gw.properties -jar gw.jar C:\projects\my-project ^
  -a OpenAI:gpt-5.1 ^
  -t true ^
  -e target,.git,node_modules ^
  -g file:C:\projects\my-project\docs\default-guidance.txt
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Downloads: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
