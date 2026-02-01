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
   - Add the Machai CLI application jar download link: [![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/machai.jar/download) to the installation section.
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

Machai CLI is a command-line tool for generating, registering, and managing library metadata within the Machanism ecosystem. It leverages GenAI to automate project assembly, run guided file-processing workflows, and enable semantic search for efficient library discovery and integration.

## Overview

Machai CLI is a Spring Boot + Spring Shell application that helps you:

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

### Installation

[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai/machai.jar/download)

Run the packaged CLI:

```bash
java -jar machai.jar
```

Then enter `help` to see the available commands.

### Environment Variables

Set the following environment variables according to your chosen GenAI provider:

| Variable Name | Description |
|---|---|
| `OPENAI_API_KEY` | API key for authenticating requests to the configured GenAI provider (e.g., OpenAI, Azure OpenAI, etc.). |
| `BINDEX_REG_PASSWORD` | Password required for database write access, specifically for metadata registration operations. |
| `GENAI_USERNAME` | Username for authenticating with CodeMie (when using the CodeMie integration). |
| `GENAI_PASSWORD` | Password for authenticating with CodeMie (when using the CodeMie integration). |

**Notes**

- Only set the variables relevant to your selected provider.
- Keep credentials secure and never commit them to version control.

### Basic Usage

```bash
java -jar machai.jar
```

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
   gw --root <working-directory>
   ```

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `--dir` | Working directory used by commands that operate on a project/workspace (e.g., `bindex`, `register`, `assembly`). | Current directory |
| `--registerUrl` | Base URL of the metadata database used for `register`, `pick`, and related operations. | (none) |
| `--update` | When `true`, updates existing `bindex.json` instead of generating from scratch. | `false` |
| `--query` | Natural-language query string or a prompt file path used by `pick`. | (none) |
| `--root` | Root folder for Ghostwriter (`gw`) workflows. | Current directory |

Example (generate metadata and register it):

```bash
bindex --dir ./my-project --update true
register --dir ./my-project --registerUrl https://metadata.example.com
```

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/machai-cli
- Source repository: https://github.com/machanism-org/machai
- Project site: https://machai.machanism.org

<!-- @guidance: DO NOT REMOVE OR MODIFY GUIDANCE TAG CONTENT. KEEP IT AS IS. -->
