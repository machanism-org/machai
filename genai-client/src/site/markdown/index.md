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

GenAI Client is a Java library designed for seamless integration with Generative AI providers. It offers foundational prompt management, optional file context, tool/function calling, and embedding capabilities (provider-dependent), enabling AI-powered features such as semantic search, automated content generation, and intelligent project assembly without coupling your application to a single vendor.

## Overview

GenAI Client exposes a small, provider-agnostic API centered on the `GenAIProvider` interface. Provider implementations are resolved by name through `GenAIProviderManager`, configured with a model and optional working directory, and executed via `perform()`.

This design provides:

- A consistent API across different GenAI backends.
- Centralized provider selection and configuration.
- Optional tool calling where models can request execution of registered Java functions and use their outputs in the response.

## Key Features

- Provider abstraction via `GenAIProvider` with resolution through `GenAIProviderManager`.
- Prompt and instruction management for request construction.
- Tool (function) calling with Java handlers (provider-dependent).
- Optional file context support (provider-dependent).
- Text embeddings support (provider-dependent).
- Optional request input logging for audit/debugging.
- Optional working-directory awareness for tools and automation-based providers.

## Getting Started

### Prerequisites

- Java (version compatible with your build; this project is built with Maven).
- Maven.
- Provider credentials/configuration:
  - For OpenAI-compatible providers: an API key/token and (optionally) a base URL override.
  - For CodeMie provider: credentials for token acquisition.
  - For Web provider: Anteater workspace recipes/configuration and a supported web UI environment.

### Environment Variables

| Variable | Required | Used by | Description |
|---|---:|---|---|
| `OPENAI_API_KEY` | Yes | OpenAI | API key (or access token) used to authenticate requests. |
| `OPENAI_ORG_ID` | No | OpenAI | Optional organization identifier. |
| `OPENAI_PROJECT_ID` | No | OpenAI | Optional project identifier. |
| `OPENAI_BASE_URL` | No | OpenAI | Override API base URL (useful for OpenAI-compatible gateways). |
| `GENAI_USERNAME` | Conditional | CodeMie | Username used to obtain an access token (when using `CodeMieProvider`). |
| `GENAI_PASSWORD` | Conditional | CodeMie | Password used to obtain an access token (when using `CodeMieProvider`). |

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

### OpenAI

`OpenAIProvider` integrates with the OpenAI API as a concrete implementation of `GenAIProvider`.

Capabilities include:

- Sending prompts and receiving responses using OpenAI chat/response models.
- Managing files for use in OpenAI workflows.
- Performing LLM tasks such as text generation, summarization, and question answering.
- Creating and using vector embeddings for semantic search and similarity analysis.
- Optional request input logging via `inputsLog(File)`.

Configuration is read from environment variables and/or Java system properties:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

Using the CodeMie OpenAI-compatible endpoint via this provider:

- Set `OPENAI_API_KEY` to an access token.
- Set `OPENAI_BASE_URL` to `https://codemie.lab.epam.com/code-assistant-api/v1`.

Thread safety: this implementation is NOT thread-safe.

### CodeMie

`CodeMieProvider` extends `OpenAIProvider` and configures it to authenticate against CodeMie and call it via an OpenAI-compatible API.

How it works:

- Obtains an access token from a Keycloak token endpoint using the Resource Owner Password flow (`grant_type=password`, `client_id=codemie-sdk`).
- Sets Java system properties used by `OpenAIProvider`:
  - `OPENAI_API_KEY` is set to the retrieved token.
  - `OPENAI_BASE_URL` is set to the CodeMie API base URL.

Configuration:

- `GENAI_USERNAME` (required; Java system property)
- `GENAI_PASSWORD` (required; Java system property)

Thread safety follows `OpenAIProvider`.

### None

`NoneProvider` is a `GenAIProvider` implementation used to disable generative AI integrations and optionally log input requests locally when an external provider is not required or available.

Purpose and typical use cases:

- Disabling GenAI features for security/compliance.
- Providing fallback behavior when no provider is configured.
- Logging requests for manual review or later processing.
- Supporting test environments without external connectivity.

Behavior notes:

- No calls are made to external AI services.
- When `inputsLog(...)` is configured, `perform()` writes accumulated prompts to the configured file and, if instructions were set, writes them to `instructions.txt` in the same directory.
- Operations that require a real provider (for example, embeddings generation) throw an exception.
- Prompts and instructions are cleared after `perform()`.

### Web

`WebProvider` obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via [Anteater](https://ganteater.com) workspace recipes. The provider loads a workspace configuration (via `model(String)`), initializes the workspace with a project directory (via `setWorkingDir(File)`), and submits the current prompt list by running the `Submit Prompt` recipe (via `perform()`).

Thread safety and lifecycle:

- This provider is not thread-safe.
- Workspace state is stored in static fields; the working directory and configuration cannot be changed once initialized in the current JVM instance.
- `close()` closes the underlying workspace.

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/genai-client
- Badge: https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg
- GitHub: https://github.com/machanism-org/machai
