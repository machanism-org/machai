---
canonical: https://machai.machanism.org/ghostwriter/index.html
---
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

Ghostwriter is a guidance-driven documentation engine that scans project files, extracts embedded `@guidance` directives, and uses a configured GenAI provider to synthesize and apply updates. It is built for real-world repositories where documentation spans many formats (source code, Markdown, HTML, configuration, and site content), enabling teams to keep artifacts accurate and consistent with less manual effort.

Benefits:

- Keeps documentation and project artifacts aligned by generating updates directly from file-embedded guidance.
- Works across heterogeneous repositories (code, docs, site pages, configs) in a single run.
- Supports module-aware scanning, exclusions, and provider-agnostic GenAI execution.

## Overview

Ghostwriter runs as a CLI: you point it at a project root and a scan target (a directory, file, or a `glob:`/`regex:` path pattern). For each supported file type, Ghostwriter:

1. Selects a reviewer based on file extension.
2. Extracts embedded guidance directives from the file.
3. Builds a prompt that includes system instructions, OS-specific processing instructions, project structure metadata, and the extracted guidance.
4. Submits the prompt to the configured GenAI provider to produce and apply the result.

Because Ghostwriter is guidance-first, it can process not only source code, but also documentation, project site content, configuration files, and other relevant artifacts in a repository.

## Key Features

- Multi-format processing via pluggable reviewers (e.g., Java, Markdown, HTML, etc.).
- Guidance-driven generation based on embedded `@guidance` directives.
- Pattern-based scanning with `glob:` and `regex:` path matchers.
- Module-aware scanning for multi-module project layouts.
- Optional multi-threaded processing.
- Optional logging of composed LLM request inputs per processed file.
- Project-relative scan safety: absolute scan paths must be within the configured root directory.

## Getting Started

### Prerequisites

- Java 8+
- Network access to your configured GenAI provider (as applicable)
- A project directory containing files with embedded `@guidance` directives (or use `--guidance` as a fallback)

### Installation

Download the Ghostwriter CLI package:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```cmd
java -jar gw.jar src
```

### Typical Workflow

1. Add `@guidance` directives to the files you want Ghostwriter to process.
2. Run Ghostwriter against a folder, file, or pattern.
3. Review generated changes in your working tree.
4. Iterate by refining guidance (or by providing defaults via `--instructions` / `--guidance`) and re-running.

## Configuration

Ghostwriter CLI options (from `org.machanism.machai.gw.processor.Ghostwriter`):

| Option | Argument | Default | Description |
|---|---|---|---|
| `-h`, `--help` | none | n/a | Show help message and exit. |
| `-r`, `--root` | path | If not set: `root` from `gw.properties`; otherwise user directory | Root directory used as the base for scan path validation and project-relative resolution. |
| `-t`, `--threads` | `true`/`false` (optional) | `false` (from `gw.properties` key `threads`) | Enable multi-threaded processing. If present with no value, it enables threading (`true`). If a value is provided, that value is used. |
| `-a`, `--genai` | `provider:model` | `OpenAI:gpt-5-mini` (or `genai` from `gw.properties`) | GenAI provider and model identifier (for example, `OpenAI:gpt-5.1`). |
| `-i`, `--instructions` | text / URL / `file:` (optional) | `instructions` from `gw.properties` (if set) | System instructions appended to each prompt. If used without a value, Ghostwriter reads multi-line input from stdin until EOF. Input supports line-based inclusion: `http(s)://...` loads remote content, `file:...` loads file content, other lines are used as-is. Blank lines are preserved. |
| `-g`, `--guidance` | text / URL / `file:` (optional) | `guidance` from `gw.properties` (if set) | Default guidance used when embedded guidance is absent. When scanning a directory, it is also applied as a final step for the current directory. If used without a value, Ghostwriter reads multi-line input from stdin until EOF. Input supports `http(s)://...` and `file:...` line inclusions. Blank lines are preserved. |
| `-e`, `--excludes` | comma-separated list | `excludes` from `gw.properties` (if set) | Exclude paths or patterns from processing. Provide a comma-separated list.
| `-l`, `--logInputs` | none | `false` (from `gw.properties` key `logInputs`) | Log composed LLM request inputs to dedicated log files. |

### Command-line Examples

From the built-in help output:

```cmd
java -jar gw.jar C:\projects\project
java -jar gw.jar src\project
java -jar gw.jar "glob:**/*.java"
java -jar gw.jar "regex:^.*\\/[^\\/]+\\.java$"
```

A typical run with explicit configuration:

```cmd
java -jar gw.jar src -r C:\projects\project -a "OpenAI:gpt-5-mini" -t -e ".machai,target" -l
```

## Default Guidance

`defaultGuidance` is a fallback instruction set used when a matched file does not contain embedded `@guidance` directives. This is useful when you want to run Ghostwriter across a directory tree and ensure every matched file (or the directory itself) still receives actionable guidance.

How it is set and applied:

- Set via `--guidance` on the CLI (or via the `guidance` key in `gw.properties`).
- If a matched file has no embedded guidance, Ghostwriter applies `defaultGuidance` for that file.
- When scanning a directory path, Ghostwriter also applies `defaultGuidance` as a final step for the directory (as described by the CLI option help).
- The value can be provided as plain text, read from stdin, or composed from line-based input that includes `http(s)://...` and `file:...` references.

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- GitHub: https://github.com/machanism-org/machai
- Project site: https://machai.machanism.org/ghostwriter/index.html
