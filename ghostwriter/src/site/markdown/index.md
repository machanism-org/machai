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
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Introduction

Ghostwriter is a documentation engine and CLI that scans your project, extracts embedded `@guidance` directives, and uses a configured GenAI provider to generate or refine documentation.

It supports **all types of project files**—including source code, documentation, project site content, and other relevant artifacts—so teams can keep docs aligned with the evolving codebase.

## Overview

Ghostwriter provides a command-line workflow to:

- Scan one or more directories or patterns for supported project files
- Extract embedded `@guidance` directives (optionally combined with system instructions)
- Invoke a configured GenAI provider/model to synthesize changes
- Write improved content back to the project files

Common use cases include project site/README generation, API documentation enrichment, and continuous documentation maintenance.

## Key Features

- Scans directories or patterns (raw paths, glob patterns, or regex patterns)
- Extracts and applies embedded `@guidance` directives during processing
- Supports system-level instructions and directory-level default guidance
- Configurable GenAI provider and model via properties and/or CLI
- Optional multi-threaded processing
- Optional logging of LLM request inputs for auditing/debugging

## Getting Started

### Prerequisites

- Java 11 or later
- Network access and credentials for your selected GenAI provider (as configured in your environment and/or `gw.properties`)

### Installation

Download the Ghostwriter CLI bundle:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```cmd
java -jar gw.jar src\main\java
```

### Typical Workflow

1. Add embedded `@guidance` blocks to the files you want Ghostwriter to generate or refine.
2. Run Ghostwriter against the directory (or pattern) containing those files.
3. Review the output, commit changes, and re-run as needed.

## Configuration

Ghostwriter can be configured through a properties file (default: `gw.properties`, or via `-Dgw.config=<path>`) and/or CLI options.

### Command-line options

| Option | Long option | Argument | Default | Description |
|---|---|---|---|---|
| `-h` | `--help` | No | n/a | Show help and exit. |
| `-l` | `--logInputs` | No | `false` | Log LLM request inputs to dedicated log files. |
| `-t` | `--threads` | Yes (optional) | `true` | Enable multi-threaded processing (default: `true`). If used without a value, defaults to `true`. You may pass an explicit boolean value (e.g., `-t false`). |
| `-r` | `--root` | Yes | `user.dir` | Root directory used as the project boundary and base for scanning when not configured via properties. |
| `-a` | `--genai` | Yes | `OpenAI:gpt-5-mini` | GenAI provider and model (e.g., `OpenAI:gpt-5.1`). |
| `-i` | `--instructions` | Yes (optional) | none | System instructions. Each line is processed: blank lines are preserved; `http(s)://...` lines are loaded and inlined; `file:...` lines are loaded and inlined; other lines are used as-is. If used without a value, Ghostwriter reads instructions from stdin until EOF. |
| `-g` | `--guidance` | Yes (optional) | none | Default directory-level guidance applied as a final step for the current directory. Same loading rules as `--instructions`. If used without a value, Ghostwriter reads guidance from stdin until EOF. |
| `-e` | `--excludes` | Yes | none | Comma-separated list of directories to exclude from processing (e.g., `target,.git`). |

### Example

From the CLI help: `<scanDir>` can be a raw path, a glob pattern, or a regex pattern.

```cmd
java -jar gw.jar "glob:**\*.java" -r . -t true -a "OpenAI:gpt-5.1" -e "target,.git" -l
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
