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

It helps keep documentation consistent and up to date by treating `@guidance` blocks embedded in source and documentation files as mandatory constraints, then applying GenAI to produce repeatable, reviewable updates.

## Overview

Ghostwriter runs over one or more directories or path patterns, discovers files containing `@guidance` instructions, and generates or updates documentation artifacts based on that guidance.

Configuration can come from `gw.properties` located next to the executable directory (or specified via `-Dgw.config=<file>`) and can be overridden via command-line options.

## Key Features

- Scans directories or supports `glob:` / `regex:` path patterns to target files.
- Converts embedded `@guidance` instructions into mandatory constraints for generated/updated artifacts.
- Accepts additional runtime instructions and default guidance via CLI (plain text, URL, file, or stdin).
- Optional multi-threaded processing for improved throughput on larger repositories.
- Optional logging of LLM request inputs to dedicated log files for traceability.

## Getting Started

### Prerequisites

- Java 11 or newer
- Network access to the configured GenAI provider

### Installation

- Download the Ghostwriter CLI package:
  [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```cmd
java -jar gw.jar C:\projects\my-project
```

### Typical Workflow

1. Add `@guidance` blocks (typically at the top of docs/source files) describing required structure and constraints.
2. Run Ghostwriter against a project directory or a `glob:` / `regex:` path pattern.
3. Review the updated/generated artifacts and commit the results.
4. Re-run Ghostwriter periodically (or in CI) to keep documentation aligned with the guidance.

## Configuration

Ghostwriter can be configured via:

- A properties file `gw.properties` located next to the executable (or specified via `-Dgw.config=<file>`), and/or
- Command-line options (which override corresponding configuration where applicable).

### Command-line Options

| Option | Long option | Argument | Default | Description |
|---|---|---|---|---|
| `-h` | `--help` | none | n/a | Show help and exit. |
| `-l` | `--logInputs` | none | `false` | Log LLM request inputs to dedicated log files. |
| `-r` | `--root` | required | (not set) | Root directory used as the base for scanning; if not set, defaults to the current user directory. |
| `-t` | `--threads` | optional | `true` | Enable multi-threaded processing. If present without a value, it prompts for stdin (see notes below). |
| `-a` | `--genai` | required | `OpenAI:gpt-5-mini` | GenAI provider and model identifier (example: `OpenAI:gpt-5.1`). |
| `-i` | `--instructions` | optional | (not set) | Additional system instructions (plain text, URL, or file path). If used without a value, reads from stdin until EOF. |
| `-g` | `--guidance` | optional | (not set) | Default guidance (plain text, URL, or file path) applied as a final step for the current directory. If used without a value, reads from stdin until EOF. |
| `-e` | `--excludes` | required | (not set) | Comma-separated list of directories to exclude (example: `target,.git,node_modules`). |

Notes:

- Configuration file: Ghostwriter attempts to load configuration from `gw.properties` located next to the executable. You can override the file location with `-Dgw.config=<file>`.
- Scan targets: positional arguments after options are treated as scan targets (directories or `glob:` / `regex:` patterns). If no targets are provided, Ghostwriter scans the current user directory.

### Example

```cmd
java -Dgw.config=gw.properties -jar gw.jar -r C:\projects\my-project -a OpenAI:gpt-5.1 -t true -e target,.git,node_modules -l "glob:**\*.md"
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download (CLI package): https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
