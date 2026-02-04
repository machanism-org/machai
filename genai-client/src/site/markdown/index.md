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

GenAI Client is a Java library designed for seamless integration with Generative AI providers. It offers foundational prompt management and (provider-dependent) embeddings, tools/function calling, and file context to enable AI-powered features across applications.

The primary benefit is a stable, small API (`GenAIProvider`) that lets you swap or combine different backends (API-based providers and UI/web-automation providers) without hard-coupling your code to a single vendor.

## Overview

GenAI Client exposes an API centered on the `GenAIProvider` interface. Provider implementations are resolved by name through `GenAIProviderManager`, configured with a model (or provider-specific configuration), and executed via `perform()`.

This design provides:

- A consistent API across different GenAI backends.
- Centralized provider selection and configuration.
- Optional tool calling where models can request execution of registered Java functions and use their outputs in the response (provider-dependent).

## Key Features

- Provider abstraction via `GenAIProvider` with resolution through `GenAIProviderManager`.
- Prompt composition from plain text and (optionally) files (`promptFile(...)`).
- Optional attachment of local or remote files (`addFile(File|URL)`) (provider-dependent).
- Tool (function) calling via registered tools (`addTool(...)`) (provider-dependent).
- Text embeddings via `embedding(...)` (provider-dependent).
- Optional request input logging for audit/debugging (`inputsLog(...)`).
- Optional working-directory awareness for tools and automation-based providers (`setWorkingDir(...)`).

## Getting Started

### Prerequisites

- Java 11+.
- Maven.
- Provider credentials/configuration (depending on the provider you use):
  - OpenAI-compatible providers: API key/token and (optionally) a base URL override.
  - CodeMie provider: credentials for token acquisition.
  - Web provider: Anteater workspace recipes/configuration and a supported web UI environment.

### Environment Variables

| Variable | Required | Used by | Description |
|---|---:|---|---|
| `OPENAI_API_KEY` | Yes | OpenAI / CodeMie (via OpenAI gateway) | API key (or access token) used to authenticate requests. |
| `OPENAI_ORG_ID` | No | OpenAI | Optional organization identifier. |
| `OPENAI_PROJECT_ID` | No | OpenAI | Optional project identifier. |
| `OPENAI_BASE_URL` | No | OpenAI / CodeMie (via OpenAI gateway) | Override API base URL (useful for OpenAI-compatible gateways). |
| `GENAI_USERNAME` | Conditional | CodeMie | Username used to obtain an access token. |
| `GENAI_PASSWORD` | Conditional | CodeMie | Password used to obtain an access token. |
| `recipes` | No | Web | Java system property to override the recipes/config location (relative to `workingDir`); defaults to `genai-client/src/main/resources`. |

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

The `OpenAIProvider` class integrates seamlessly with the OpenAI API, serving as a concrete implementation of the `GenAIProvider` interface.

This provider enables a wide range of generative AI capabilities, including:

- Sending prompts and receiving responses from OpenAI Chat models.
- Managing files for use in various OpenAI workflows.
- Performing advanced large language model (LLM) requests, such as text generation, summarization, and question answering.
- Creating and utilizing vector embeddings for tasks like semantic search and similarity analysis.

By abstracting the complexities of direct API interaction, `OpenAIProvider` allows developers to leverage OpenAI’s powerful models efficiently within their applications. It supports both synchronous and asynchronous operations, and can be easily extended or configured to accommodate different use cases and model parameters.

**Environment Variables**

The client automatically reads the following environment variables. You must set at least `OPENAI_API_KEY`:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

**Using the CodeMie API**

To use the CodeMie API, set the following environment variables:

- `OPENAI_API_KEY` = access token
- `OPENAI_BASE_URL` = `https://codemie.lab.epam.com/code-assistant-api/v1`

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

**Thread safety:** NOT thread-safe.

### CodeMie

The `CodeMieProvider` extends `OpenAIProvider` and authenticates against CodeMie before sending requests to CodeMie through its OpenAI-compatible API endpoint.

How it works:

- Obtains an access token from the CodeMie Keycloak token endpoint using the Resource Owner Password flow (`grant_type=password`, `client_id=codemie-sdk`).
- Uses that token as the API key when creating the OpenAI client.
- Uses the CodeMie OpenAI-compatible base URL: `https://codemie.lab.epam.com/code-assistant-api/v1`.

**Authentication / configuration**

- `GENAI_USERNAME` and `GENAI_PASSWORD` are required.

**Thread safety:** NOT thread-safe (inherits behavior from `OpenAIProvider`).

### None

No-op implementation of `GenAIProvider`.

This provider is intended for environments where no external LLM integration should be used. It accumulates prompt text in memory and can optionally write instructions and prompts to local files when `inputsLog(File)` has been configured.

**Key characteristics**

- No network calls are performed.
- `perform()` always returns `null`.
- Unsupported capabilities (for example, `embedding(String)`) throw an exception.

### Web

`GenAIProvider` implementation that obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via [Anteater](https://ganteater.com) workspace recipes. The provider loads a workspace configuration (see `model(String)`), initializes the workspace with a project directory (see `setWorkingDir(File)`), and submits the current prompt list by running the `"Submit Prompt"` recipe (see `perform()`).

**Thread safety and lifecycle**

- This provider is not thread-safe.
- Workspace state is stored in static fields; the working directory cannot be changed once initialized in the current JVM instance.
- `close()` closes the underlying workspace.

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
provider.model("config.yaml");
provider.setWorkingDir(new File("/path/to/project"));
String response = provider.perform();
provider.close();
```

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/genai-client
- Badge: https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg
- GitHub: https://github.com/machanism-org/machai
