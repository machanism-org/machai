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

![](images/machai-ghostwriter-logo.png)

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Introduction

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis. It reduces manual documentation effort, enforces consistent documentation standards, and helps teams keep project docs up to date by extracting, reviewing, and composing documentation from source files.

## Overview

Ghostwriter performs a source-aware scan of a project, applies language- and format-specific reviewers (Java, Markdown, Python, TypeScript, plain text, HTML, etc.), and uses a configured GenAI provider to synthesize improved documentation and recommendations. It is intended for use in local development, CI pipelines, and automated site generation flows to produce higher-quality, consistent documentation artifacts.

## Key Features

- Automatic recursive scanning of project directories and source files.
- Pluggable reviewers for multiple languages and formats (Java, Markdown, Python, TypeScript, HTML, text).
- Integration with GenAI providers and selectable models (via command-line or environment configuration).
- Optional multi-threaded processing for faster analysis on large codebases.
- Simple CLI for quick integration into scripts and CI.
- Extensible design allowing additional reviewers and custom prompts.

## Getting Started

### Prerequisites

- Java 11 or later installed (`java` available on your `PATH`).
- (Optional) Maven 3.x if you need to build the project from source.
- Credentials for your chosen GenAI provider if you plan to use AI model integration.

### Environment Variables

| Name | Description | Example |
|------|-------------|---------|
| `OPENAI_API_KEY` | API key for the OpenAI provider (when using OpenAI). | `sk-...` |
| `OPENAI_BASE_URL` | Custom base URL for OpenAI-compatible services. | `https://api.openai.com` |

### Basic Usage

Assuming you have an assembled artifact named `gw.jar` (assembly output), a typical run looks like:

```bash
java -jar gw.jar -g OpenAI:gpt-5.1 -d /path/to/project -t
```

This runs Ghostwriter against the specified project directory using the OpenAI `gpt-5.1` model with multi-threading enabled.

### Typical Workflow

1. Set your GenAI provider credentials in the environment (for example, `OPENAI_API_KEY`).
2. Build or obtain the `gw.jar` artifact (or run the main class via your build tool).
3. Run the scanner against your project directory with the desired model and options.
4. Inspect logs and generated output; iterate by adjusting prompts, reviewers, or configuration.
5. Integrate Ghostwriter runs into CI pipelines to keep documentation current.

## Configuration

Common command-line options and defaults:

| Option | Description | Default |
|--------|-------------|---------|
| `-d, --dir <path>` | Path to the project directory to scan. | Current working directory |
| `-g, --genai <provider:model>` | GenAI provider and model identifier (for example, `OpenAI:gpt-5.1`). | Not set (use provider defaults or environment) |
| `-t, --threads` | Enable multi-threaded processing. | Disabled |
| `-h, --help` | Show help/usage. | N/A |

Example: run with a specific model and project directory

```bash
java -jar gw.jar -g OpenAI:gpt-5.1 -d /home/dev/my-project
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Project site / docs: https://machai.machanism.org
