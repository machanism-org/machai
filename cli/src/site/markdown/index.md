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
   - Basic Usage: Example command to run the plugin.
   - Typical Workflow: Step-by-step outline of how to use the project artifacts.
# Configuration
   - Table of common configuration parameters, their descriptions, and default values.
   - Example: Command-line example showing how to configure and run the plugin with custom parameters.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Machai CLI

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-cli.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai-cli)

## Introduction

`machai-cli` is a command-line tool for generating, registering, and managing library metadata within the Machanism ecosystem. It leverages GenAI to automate project assembly and enable semantic search for efficient library discovery and integration.

## Overview

`machai-cli` is a Spring Boot + Spring Shell application that helps you:

- Generate and update `bindex.json` metadata for projects.
- Register metadata into a metadata database for later discovery.
- Search for libraries ("bricks") using a natural-language prompt.
- Assemble new projects from previously picked libraries.
- Run Ghostwriter (GenAI) file-processing workflows from the terminal.

## Key Features

- Generate or update `bindex.json` metadata for a project.
- Register generated metadata into a metadata database.
- Pick relevant libraries using a natural-language prompt (or a prompt file).
- Assemble a new project from previously picked libraries.
- Process project files/documents using Ghostwriter (GenAI).
- Clean `.machai` temporary folders from a workspace.
- Configure default CLI settings (working directory, GenAI model, similarity score).

## Getting Started

### Prerequisites

- Java 17+
- A GenAI provider API key (for example, OpenAI)
- Network access to the metadata database endpoint used by `pick` / `assembly` / `register`

### Environment Variables

| Variable Name | Description |
|---|---|
| `OPENAI_API_KEY` | API key used by the configured GenAI provider for AI-powered operations. |
| `BINDEX_REG_PASSWORD` | Password for database write access (required for metadata registration). |

### Basic Usage

Run the packaged CLI:

```bash
java -jar machai.jar
```

Then enter `help` to see the available commands.

### Typical Workflow

1. Generate (or update) `bindex.json` for a project:

   ```bash
   bindex --dir <project-path> --update true
   ```

2. Register the generated metadata in the metadata database:

   ```bash
   register --dir <project-path> --registerUrl <url>
   ```

3. Pick libraries by semantic search:

   ```bash
   pick --query "<prompt or file path>" --registerUrl <url>
   ```

4. Assemble a new project from the picked libraries:

   ```bash
   assembly --dir <output-folder>
   ```

5. (Optional) Run Ghostwriter processing:

   ```bash
   gw --dir <working-directory>
   ```

## Configuration

### Common Parameters

| Parameter | Description | Default |
|---|---|---|
| `--dir` | Working directory used by commands that operate on a project/workspace. | Current directory / configured default |
| `--registerUrl` | Metadata database endpoint for `pick` / `assembly` / `register`. | (none) |
| `--genai` | GenAI provider/model (format: `provider:model`). | Configured default |
| `--score` | Minimum similarity score for matches (used by commands that support it). | Configured default |
| `--update` | Whether to update existing metadata/registration. | Command-specific |

### Example: Configure and run with custom parameters

```bash
pick --query "Find libraries for a Spring Boot REST API" --registerUrl https://example.com/register --score 0.75 --genai openai:gpt-4o-mini
```

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/machai-cli
- Source repository: https://github.com/machanism-org/machai
- Project site: https://machai.machanism.org

<!-- @guidance: DO NOT REMOVE OR MODIFY GUIDANCE TAG CONTENT. KEEP IT AS IS. -->
