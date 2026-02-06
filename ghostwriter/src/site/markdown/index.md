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
   - Analyze /java/org/machanism/machai/gw/Ghostwriter.java java source file and generate cmd options description.
   - Table of cmd options, their descriptions, and default values.
   - Example: Command-line example showing how to configure and run the application with custom parameters.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Introduction

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis.

It helps teams keep documentation accurate and consistent by combining:

- project structure and source scanning
- embedded, file-local guidance directives
- configurable AI prompts (instructions and default guidance)
- reproducible CLI-driven generation suitable for local use and CI

## Overview

Ghostwriter is designed to process one or more directories (or patterns) under a configured root directory, apply guidance/instructions, and generate or update documentation artifacts. It is packaged as a runnable CLI JAR (`gw.jar`) and can be configured via:

- command-line options
- a properties file (`gw.properties`) located next to the executable JAR by default
- an alternative config file path via `-Dgw.config=<path>`

## Key Features

- Scans directories or path patterns to process documentation sources
- Supports default configuration via `gw.properties` (or `-Dgw.config` override)
- Selects a GenAI provider/model for synthesis (e.g., `OpenAI:gpt-5.1`)
- Accepts additional instructions and default guidance from text, URLs, or files
- Excludes directories from processing
- Optional multi-threaded processing
- Optional logging of LLM request inputs into dedicated log files

## Getting Started

### Prerequisites

- Java 11 or later
- Network access for the selected GenAI provider (as configured in your environment)
- (Optional) A `gw.properties` configuration file

### Installation

Download the Ghostwriter CLI distribution:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

Run Ghostwriter against a project directory:

```bash
java -jar gw.jar C:\projects\my-project
```

### Typical Workflow

1. (Optional) Create `gw.properties` next to `gw.jar` (or set `-Dgw.config=<path>`).
2. Decide a root directory (`--root`) if you want all scan targets resolved relative to a specific root.
3. Provide one or more scan targets as arguments:
   - a directory path
   - a pattern such as `glob:**/*.java` or `regex:^.*\/[^\/]+\.java$`
4. (Optional) Add instructions (`--instructions`) and/or default guidance (`--guidance`).
5. Run the CLI and review generated/updated documentation outputs.

## Configuration

Ghostwriter can be configured with a combination of:

- **System property**: `-Dgw.config=<path>` to point to a properties file
- **Properties** (typically in `gw.properties`): `root`, `genai`, `instructions`, `guidance`, `excludes`
- **CLI options** (below) which override properties where applicable

### Command-line Options

| Option | Argument | Default | Description |
|---|---:|---|---|
| `-h`, `--help` | No | — | Show this help message and exit. |
| `-r`, `--root` | Yes | If not set: uses the current user directory as root. | Specify the path to the root directory for file processing. |
| `-t`, `--threads` | Optional | `true` | Enable multi-threaded processing to improve performance. |
| `-a`, `--genai` | Yes | `OpenAI:gpt-5-mini` | Set the GenAI provider and model (for example: `OpenAI:gpt-5.1`). |
| `-i`, `--instructions` | Optional | From `gw.properties` key `instructions` (if present) | Specify additional instructions as plain text, by URL, or by file path. Each line is processed: blank lines preserved; `http(s)://...` loaded from URL; `file:...` loaded from file path; other lines used as-is. To provide multiple locations, separate by comma (`,`). If used without a value, you will be prompted to enter text via stdin (EOF to finish). |
| `-g`, `--guidance` | Optional | From `gw.properties` key `guidance` (if present) | Specify the default guidance as plain text, by URL, or by file path to apply as a final step for the current directory. Input handling matches `--instructions`. If used without a value, you will be prompted to enter text via stdin (EOF to finish). |
| `-e`, `--excludes` | Yes | From `gw.properties` key `excludes` (if present) | Specify a list of directories to exclude from processing. Provide multiple directories separated by commas or by repeating the option. |
| `-l`, `--logInputs` | No | `false` | Log LLM request inputs to dedicated log files. |

### Example

```bash
java -Dgw.config=gw.properties -jar gw.jar C:\projects\my-project ^
  --root C:\projects\my-project ^
  --genai OpenAI:gpt-5.1 ^
  --threads true ^
  --excludes target,.git,node_modules ^
  --instructions file:docs/instructions.txt ^
  --guidance https://example.com/ghostwriter-guidance.txt ^
  --logInputs
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download (CLI distribution): https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
