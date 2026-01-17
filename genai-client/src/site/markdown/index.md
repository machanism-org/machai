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

# GenAI Client

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)

## Introduction

GenAI Client is a Java library designed for seamless integration with Generative AI providers. It provides foundational prompt management, optional tool (function) calling, and embedding capabilities so you can add AI-powered features (such as semantic search, automated content generation, and intelligent project assembly) without coupling your application to a single vendor.

## Overview

The main integration point is the `GenAIProvider` interface. A concrete provider (for example, OpenAI or a web-automation provider) is resolved by name using `GenAIProviderManager`, configured with a model and optional working directory, then executed via `perform()`.

This design enables:

- A consistent API across different GenAI backends.
- Centralized configuration and provider selection.
- Tool calling where the model can request execution of registered Java functions and use their outputs in the response.

## Key Features

- Provider abstraction via `GenAIProvider` with resolution through `GenAIProviderManager`.
- Prompt and instruction management for request construction.
- Tool (function) calling with Java handlers.
- Optional file context support (provider-dependent).
- Text embeddings support (provider-dependent).
- Optional request input logging for audit/debugging.

## Getting Started

### Prerequisites

- Java (a version compatible with your build; this project is built with Maven).
- Maven.
- Provider credentials/configuration:
  - For OpenAI: an OpenAI-compatible API endpoint and API key.
  - For Web provider: Anteater workspace recipes/configuration and a supported web UI environment.

### Environment Variables

| Variable | Required | Used by | Description |
|---|---:|---|---|
| `OPENAI_API_KEY` | Yes | OpenAI | API key used to authenticate requests. |
| `OPENAI_ORG_ID` | No | OpenAI | Optional organization identifier. |
| `OPENAI_PROJECT_ID` | No | OpenAI | Optional project identifier. |
| `OPENAI_BASE_URL` | No | OpenAI | Override API base URL (useful for OpenAI-compatible gateways). |

### Basic Usage

```java
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;

GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
provider.prompt("Summarize this project in one paragraph.");
String answer = provider.perform();
provider.close();
```

### Typical Workflow

1. Resolve a provider using `GenAIProviderManager.getProvider("ProviderName:modelOrConfig")`.
2. (Optional) Configure a working directory with `setWorkingDir(...)` (used by tools and some providers).
3. (Optional) Set provider-specific model/config with `model(...)` if needed.
4. Add instructions with `instructions(...)` and prompts with `prompt(...)` / `promptFile(...)`.
5. (Optional) Register tools using `addTool(...)`.
6. Execute the request with `perform()`.
7. Close resources with `close()`.

## Configuration

### Common configuration parameters

| Parameter | Description | Default |
|---|---|---|
| Provider spec (`ProviderName:modelOrConfig`) | String passed to `GenAIProviderManager` to select provider and model/config. | None |
| `model(String)` | Sets the provider model name (OpenAI) or configuration name (Web). | Provider-dependent |
| `setWorkingDir(File)` | Sets a working directory used by tools and/or provider workflows. | Not set |
| `inputsLog(File)` | Enables logging of prompt inputs to a file for auditing/debugging. | Disabled |
| `instructions(String)` | Sets system-level instructions for the request/session. | Not set |

### Example: configure and run with a custom model

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
provider.instructions("Answer concisely.");
provider.prompt("Explain how tool calling works in this library.");
String result = provider.perform();
provider.close();
```

## Supported GenAI Providers
<!-- 
@guidance: 
IMPORTANT: Update this section! Use from javadoc of classes from source folder which extend the GenAI interface 
and generate the content for this section following net format:
### [PROVIDER_NAME]
... FULL DESCRIPTION ...
-->

### None

The `NoneProvider` class is an implementation of the `GenAIProvider` interface used to disable generative AI integrations and log input requests locally when an external AI provider is not required or available.

Purpose:

- Provides a stub implementation that stores requests in input files (in the `inputsLog` folder).
- All GenAI operations are non-operative, or throw exceptions where necessary, making this useful for scenarios where generative AI features must be disabled, simulated, or for fallback testing.
- No calls are made to any external AI services or large language models (LLMs).

Typical use cases:

- Disabling generative AI features for security or compliance.
- Implementing fallback logic when no provider is configured.
- Logging requests for manual review or later processing.
- Testing environments not connected to external services.

Example usage:

```java
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.provider.none.NoneProvider;

GenAIProvider provider = new NoneProvider();
provider.prompt("Describe the weather.");
provider.perform(); // No AI service is called; input may be logged locally.
provider.close();
```

Notes:

- Operations requiring GenAI services may throw exceptions when called.
- Prompts and instructions may be cleared after performing (provider-dependent behavior).

### OpenAI

The `OpenAIProvider` class integrates with the OpenAI API as a concrete implementation of the `GenAIProvider` interface.

Capabilities typically include:

- Sending prompts and receiving responses from OpenAI chat models.
- Tool calling (when configured by the client and supported by the selected model).
- Creating and using vector embeddings for tasks like semantic search and similarity analysis.

Environment variables:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

Using an OpenAI-compatible gateway (example):

- `OPENAI_BASE_URL` = `https://your-gateway.example.com/v1`

Usage example:

```java
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;

GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: this implementation is not thread-safe.

### Web

The `WebProvider` class is a `GenAIProvider` implementation that obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via Anteater workspace recipes. The provider loads a workspace configuration (via `model(String)`), initializes the workspace with a project directory (via `setWorkingDir(File)`), and submits the current prompt list by running the "Submit Prompt" recipe (via `perform()`).

Thread safety and lifecycle:

- This provider is not thread-safe.
- Workspace state may be stored in static fields; the working directory may not be changeable once initialized in the current JVM instance.
- `close()` closes the underlying workspace.

Usage example:

```java
import java.io.File;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;

GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
provider.model("config.yaml");
provider.setWorkingDir(new File("/path/to/project"));
String response = provider.perform();
provider.close();
```

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/genai-client
- API badge: https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg
