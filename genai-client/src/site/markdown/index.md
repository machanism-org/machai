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

GenAI Client is a Java library for integrating Generative AI (GenAI) capabilities into applications and build workflows without binding your code to a single vendor. It provides a provider abstraction, prompt/instruction management, optional tool (function) calling, and (provider-dependent) file context and embeddings.

## Overview

The main integration point is the `GenAIProvider` interface. A concrete provider is resolved by name using `GenAIProviderManager`, configured with a model and optional working directory, then executed via `perform()`.

This design enables:

- A consistent API across different GenAI backends.
- Centralized configuration and provider selection.
- Tool calling where the model can request execution of registered Java functions and use their outputs in the response.

## Key Features

- Provider abstraction via `GenAIProvider` with resolution through `GenAIProviderManager`.
- Prompt and instruction management for request construction.
- Tool (function) calling with Java handlers (provider-dependent).
- Optional file context support (provider-dependent).
- Text embeddings support (provider-dependent).
- Optional request input logging for audit/debugging.

## Getting Started

### Prerequisites

- Java (version compatible with your build; this project is built with Maven).
- Maven.
- Provider credentials/configuration:
  - For OpenAIProvider: an OpenAI-compatible API endpoint and API key.
  - For CodeMieProvider: CodeMie credentials via system properties.
  - For WebProvider: Anteater workspace recipes/configuration and a supported web UI environment.

### Environment Variables

| Variable | Required | Used by | Description |
|---|---:|---|---|
| `OPENAI_API_KEY` | Yes | OpenAIProvider | API key used to authenticate requests. |
| `OPENAI_ORG_ID` | No | OpenAIProvider | Optional organization identifier. |
| `OPENAI_PROJECT_ID` | No | OpenAIProvider | Optional project identifier. |
| `OPENAI_BASE_URL` | No | OpenAIProvider | Override API base URL (useful for OpenAI-compatible gateways). |

### System Properties

| Property | Required | Used by | Description |
|---|---:|---|---|
| `GENAI_USERNAME` | Conditional | CodeMieProvider | Username used to obtain an access token. |
| `GENAI_PASSWORD` | Conditional | CodeMieProvider | Password used to obtain an access token. |
| `OPENAI_API_KEY` | No | OpenAIProvider / CodeMieProvider | Can be provided as a system property instead of an environment variable. |
| `OPENAI_BASE_URL` | No | OpenAIProvider / CodeMieProvider | Can be provided as a system property instead of an environment variable. |
| `recipes` | No | WebProvider | Path (relative to `workingDir`) to Anteater recipes/config (default: `genai-client/src/main/resources`). |

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
| `model(String)` | Sets the provider model name (OpenAIProvider) or configuration name (WebProvider). | Provider-dependent |
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

The `NoneProvider` is an implementation of `GenAIProvider` intended to disable Generative AI integrations while optionally logging prompts locally.

Purpose and behavior:

- No calls are made to any external AI services.
- Prompts and instructions can be written to disk when `inputsLog(File)` is configured.
- Methods that inherently require a GenAI backend (for example, `embedding(String)`) throw an exception.
- After `perform()`, accumulated prompts are cleared.

Example:

```java
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.provider.none.NoneProvider;

GenAIProvider provider = new NoneProvider();
provider.prompt("Describe the weather.");
provider.perform();
provider.close();
```

### OpenAI

The `OpenAIProvider` is a concrete `GenAIProvider` backed by the OpenAI API (or an OpenAI-compatible gateway). It supports chat prompting, file inputs, tool/function calling, and embeddings.

Capabilities:

- Send prompts via `prompt(...)` / `promptFile(...)` and execute with `perform()`.
- File context via `addFile(File)` (uploads the file) and `addFile(URL)` (references a URL).
- Tool/function calling via `addTool(...)` (provider executes registered Java handlers when the model requests a tool).
- Text embeddings via `embedding(String)`.
- Optional input logging via `inputsLog(File)`.

Configuration:

- Environment variables (preferred): `OPENAI_API_KEY` (required), `OPENAI_ORG_ID` (optional), `OPENAI_PROJECT_ID` (optional), `OPENAI_BASE_URL` (optional).
- System properties: `OPENAI_API_KEY`, `OPENAI_BASE_URL`.

Thread safety:

- This provider is not thread-safe.

### CodeMie

The `CodeMieProvider` extends `OpenAIProvider` to authenticate against the CodeMie service and then communicate with it via an OpenAI-compatible API.

How it works:

- Requests an access token from the CodeMie Keycloak token endpoint using the Resource Owner Password Credentials flow.
- Sets `OPENAI_API_KEY` to the retrieved token and `OPENAI_BASE_URL` to the CodeMie API base URL, then delegates all behavior to `OpenAIProvider`.

Configuration:

- System properties: `GENAI_USERNAME` (required), `GENAI_PASSWORD` (required).

Thread safety:

- Same as `OpenAIProvider` (not thread-safe).

### Web

The `WebProvider` is a `GenAIProvider` implementation that obtains model responses by automating a target GenAI service through its web user interface using Anteater workspace recipes.

How it works:

- Set the Anteater configuration name via `model(String)`.
- Initialize the workspace with `setWorkingDir(File)` (intended to be called once per JVM; the working directory cannot be changed afterward).
- `perform()` runs the `"Submit Prompt"` recipe, passing prompts in the `INPUTS` system variable; the recipe is expected to store the response in variable `result`.

Thread safety and lifecycle:

- Not thread-safe.
- Workspace state is stored in static fields.
- `close()` closes the underlying workspace.

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/genai-client
- Badge: https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg
- GitHub: https://github.com/machanism-org/machai
