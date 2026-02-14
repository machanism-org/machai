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

Ghostwriter is an advanced documentation engine that scans, analyzes, and assembles project documentation using embedded `@guidance` directives and AI-powered synthesis.

It helps teams keep documentation accurate and up-to-date by:

- Generating or updating content from real project sources (code, docs, site pages, and other relevant artifacts)
- Applying embedded, file-local directives (`@guidance`) to control what gets generated
- Producing consistent results using a configurable GenAI provider/model

## Overview

Ghostwriter provides a CLI that scans one or more directories (or patterns) for supported files, extracts embedded guidance directives, and then invokes a configured GenAI provider to produce the requested content.

Common uses include:

- Project site and README generation
- API and developer documentation enrichment
- Reviewing and improving existing Markdown/HTML/text documentation
- Keeping documentation aligned with the current codebase

## Key Features

- Scans directories and patterns (raw paths, glob patterns, or regex patterns) for supported project files
- Extracts embedded `@guidance` directives and applies them during processing
- Supports system-level instructions and directory-level default guidance
- Configurable GenAI provider and model via properties and/or CLI
- Optional multi-threaded processing
- Optional logging of LLM request inputs for auditing/debugging

## Getting Started

### Prerequisites

- Java 11 or later
- Network access to your selected GenAI provider (as configured in your environment/properties)

### Installation

Download the Ghostwriter CLI bundle:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```cmd
java -jar gw.jar src\main\java
```

### Typical Workflow

1. Add embedded `@guidance` blocks to files you want Ghostwriter to generate or refine.
2. Run Ghostwriter against the directory (or pattern) that contains those files.
3. Review the generated output, commit changes, and re-run as needed to keep docs current.

## Configuration

Ghostwriter can be configured through a properties file (default: `gw.properties`, or via `-Dgw.config=<path>`) and/or CLI options.

### Command-line options

| Option | Long option | Arg | Default | Description |
|---|---|---:|---|---|
| `-h` | `--help` | No | n/a | Show help and exit. |
| `-l` | `--logInputs` | No | `false` | Log LLM request inputs to dedicated log files. |
| `-t` | `--threads` | Yes (optional) | `true` | Enable multi-threaded processing to improve performance. Accepts an optional boolean value (e.g., `-t false`). |
| `-r` | `--root` | Yes | `user.dir` (when not configured) | Root directory used as the project boundary and base for scanning. |
| `-a` | `--genai` | Yes | `OpenAI:gpt-5-mini` | GenAI provider and model (for example: `OpenAI:gpt-5.1`). |
| `-i` | `--instructions` | Yes (optional) | none | System instructions text. Each line is processed: `http(s)://...` lines are loaded and inlined, `file:...` lines are loaded and inlined, other lines are used as-is. If used without a value, Ghostwriter reads instructions from stdin until EOF. |
| `-g` | `--guidance` | Yes (optional) | none | Default directory-level guidance applied as a final step for the current directory. Same loading rules as `--instructions`. If used without a value, Ghostwriter reads guidance from stdin until EOF. |
| `-e` | `--excludes` | Yes | none | Comma-separated list of directories to exclude from processing. |

### Example

The CLI help notes that `<scanDir>` can be a raw path, a glob pattern, or a regex pattern.

```cmd
java -jar gw.jar "glob:**\*.java" -r . -t true -a "OpenAI:gpt-5.1" -e "target,.git" -l
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
