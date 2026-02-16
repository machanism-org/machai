<!-- @guidance:
**VERY IMPORTANT NOTE:**  
Ghostwriter maven plugin works with **all types of project files—including source code, documentation, project site content, and other relevant files**.
Ensure that your content generation and documentation efforts consider the full range of file types present in the project.
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
   - Analyze /java/org/machananism/machai/gw/processor/Ghostwriter.java java source file and generate cmd options description.
   - Table of cmd options, their descriptions, and default values.
   - Example: Command-line example showing how to configure and run the application with custom parameters included information from Ghostwriter.help() method.
# Default Guidance
   - Use the information in the documentation for the FileProcessor.setDefaultGuidance(String defaultGuidance) method to generate a section detailing the purpose and use of `defaultGuidance`.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Introduction

Ghostwriter is an AI-assisted documentation engine and CLI that scans your project, extracts embedded `@guidance` directives, and uses a configured GenAI provider/model to generate or refine content.

It works across **all types of project files**—including source code, documentation, project site content, and other relevant artifacts—helping teams keep documentation aligned with the evolving codebase.

## Overview

Ghostwriter provides a command-line workflow to:

- Scan a directory or a path pattern (raw path, `glob:` pattern, or `regex:` pattern)
- Extract embedded `@guidance` blocks from supported file types
- Optionally apply system-wide `--instructions`
- Invoke the configured GenAI provider/model to synthesize improvements
- Write updated content back to project files

Common use cases include project site/README generation, API documentation enrichment, and ongoing documentation maintenance.

## Key Features

- Scans raw directories, glob patterns, or regex patterns
- Extracts embedded `@guidance` directives during processing
- Supports system instructions (`--instructions`) and directory-level default guidance (`--guidance`)
- Configurable GenAI provider and model (example: `OpenAI:gpt-5.1`)
- Optional multi-threaded module processing
- Optional logging of composed LLM request inputs (for auditing/debugging)
- Exclude directories via comma-separated `--excludes`

## Getting Started

### Prerequisites

- Java 11 or later
- Network access and credentials for your selected GenAI provider (configured via `gw.properties` and/or environment)

### Installation

Download the Ghostwriter CLI bundle:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```cmd
java -jar gw.jar src\main\java
```

### Typical Workflow

1. Add embedded `@guidance` blocks to the files you want Ghostwriter to generate or refine.
2. Run Ghostwriter against a directory (or pattern) containing those files.
3. Review the changes, commit, and re-run as needed.

## Configuration

Ghostwriter can be configured through a properties file (default: `gw.properties`, or via `-Dgw.config=<path>`) and/or CLI options.

### Command-line options

Defaults shown below are based on the current implementation (including fallback behavior when a value is not provided).

| Option | Long option | Arg | Default | Description |
|---|---|---:|---|---|
| `-h` | `--help` | No | n/a | Show help and exit. |
| `-l` | `--logInputs` | No | `false` | Log composed LLM request inputs to per-file logs. |
| `-t` | `--threads` | Yes (optional) | `true` | Enable multi-threaded module processing. If the option is present with no value, it defaults to `true`. You may pass an explicit boolean (e.g., `-t false`). |
| `-r` | `--root` | Yes | `user.dir` (when not configured) | Root directory used as the project boundary and base for scanning. If not provided via config or option, defaults to the current working directory. |
| `-a` | `--genai` | Yes | `OpenAI:gpt-5-mini` | GenAI provider and model (example: `OpenAI:gpt-5.1`). |
| `-i` | `--instructions` | Yes (optional) | none | System instructions added to every prompt. Each line is processed: blank lines preserved; `http(s)://...` lines are fetched and inlined; `file:...` lines are read and inlined; other lines used as-is. If used without a value, Ghostwriter reads from stdin until EOF. |
| `-g` | `--guidance` | Yes (optional) | none | Default directory-level guidance applied as a final step for the current directory and as a fallback for files without embedded `@guidance`. Same loading rules as `--instructions`. If used without a value, Ghostwriter reads from stdin until EOF. |
| `-e` | `--excludes` | Yes | none | Comma-separated list of directories/patterns to exclude (example: `target,.git`). |

### Example

From the CLI help: `<scanDir>` can be a raw path, a glob pattern, or a regex pattern.

```cmd
java -jar gw.jar "glob:**\*.java" -r . -t true -a "OpenAI:gpt-5.1" -e "target,.git" -l
```

## Default Guidance

`defaultGuidance` is a fallback instruction set used when a file does not contain an embedded `@guidance` directive.

- **Purpose:** ensure files without per-file guidance can still be processed consistently.
- **How it’s set:** via CLI `--guidance` (or the `guidance` property), as plain text or by referencing external content:
  - `http://` or `https://` lines are fetched and inlined
  - `file:` lines are read from disk and inlined
  - blank lines are preserved; other lines are used as-is
- **How it’s applied:**
  - If a supported file contains `@guidance`, that guidance is used.
  - Otherwise, if `defaultGuidance` is set, it is used to build the prompt for the file.
  - Additionally, when `--guidance` is set, Ghostwriter applies it as a final step to the directory being processed.

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download bundle: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
