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

It helps you keep documentation consistent and up to date across large codebases by turning structured, in-repo guidance into repeatable, automatable documentation updates.

## Overview

Ghostwriter is a CLI that processes one or more scan targets (directory paths or path patterns) under a root directory. It applies optional instructions and optional default guidance, then produces updates while logging its work.

It is designed for both local use and CI pipelines where you want deterministic, scriptable documentation review and regeneration.

## Key Features

- Scans project directories and supports path patterns (e.g., `glob:` and `regex:` targets).
- Uses embedded guidance tags to drive consistent documentation output.
- Supports configurable GenAI provider/model selection.
- Accepts external instructions via URL(s) or file path(s), or via interactive stdin.
- Optional default guidance that can be applied as a final step per directory.
- Excludes directories from processing.
- Optional multi-threaded processing.
- Optional logging of LLM request inputs to dedicated log files for audit/debug.

## Getting Started

### Prerequisites

- Java 11 or later
- Network access to your configured GenAI provider
- (Optional) A `gw.properties` configuration file placed next to the executable (or set `-Dgw.config=...`)

### Installation

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```bash
java -jar gw.jar <path | path_pattern>
```

Examples (Windows):

```bat
java -jar gw.jar C:\projects\project
java -jar gw.jar -r C:\projects\project src\project
java -jar gw.jar -r C:\projects\project "glob:**/*.java"
java -jar gw.jar -r C:\projects\project "regex:^.*\/[^\/]+\.java$"
```

### Typical Workflow

1. Download and unzip the Ghostwriter distribution.
2. (Optional) Create `gw.properties` in the same directory as the executable to set defaults (e.g., `root`, `genai`, `instructions`, `excludes`).
3. Run Ghostwriter against a directory (or pattern) under the chosen root.
4. Provide additional instructions via `--instructions` (URL/file) or interactively via stdin.
5. Review logs and generated changes; repeat in CI as needed.

## Configuration

Ghostwriter is configured via command-line options and (optionally) a properties file loaded from the execution directory (defaults to `gw.properties`, override with `-Dgw.config=...`).

### Command-line Options

| Option | Args | Default | Description |
|---|---:|---|---|
| `-h`, `--help` | no | — | Show help and exit. |
| `-r`, `--root <path>` | yes | From `gw.properties` key `root`; otherwise: current user directory (`user.dir`). | Root directory used to compute and validate scan targets. Scan targets must be within this root. |
| `-t`, `--threads[=true|false]` | optional | `true` | Enable/disable multi-threaded processing. If provided without a value, it is treated as enabled. Use `--threads=false` to disable. |
| `-a`, `--genai <provider:model>` | yes | From `gw.properties` key `genai`; otherwise: `OpenAI:gpt-5-mini`. | Set the GenAI provider and model (e.g., `OpenAI:gpt-5.1`). |
| `-i`, `--instructions[=<url|file>[,<url|file>...]]` | optional | From `gw.properties` key `instructions` (comma-separated); otherwise: none. | Additional instruction locations (URL or file path). Multiple values are comma-separated. If used without a value, Ghostwriter reads instruction text from stdin (EOF-terminated). Relative file paths are resolved against the execution directory. |
| `-g`, `--guidance[=<file>]` | optional | From `gw.properties` key `guidance`; otherwise: none. | Default guidance applied as a final step per directory. If provided with a value, it is treated as a guidance file path (relative paths resolved against the execution directory). If used without a value, Ghostwriter reads guidance text from stdin (EOF-terminated). |
| `-e`, `--excludes <dir[,dir...]>` | yes | From `gw.properties` key `excludes` (comma-separated); otherwise: none. | Comma-separated list of directories to exclude from processing. |
| `-l`, `--logInputs` | no | `false` | Log LLM request inputs to dedicated log files. |

### Example

```bash
java -jar gw.jar "glob:**/*.md" \
  --root "/path/to/repo" \
  --genai "OpenAI:gpt-5.1" \
  --instructions "https://example.com/team-guidelines.md,docs/extra-instructions.md" \
  --guidance docs/default-guidance.md \
  --excludes "target,node_modules" \
  --threads=false \
  --logInputs
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
