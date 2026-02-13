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

GenAI Client is a Java library for integrating with Generative AI providers. It provides foundational prompt management, optional tool/function calling, optional file context, and provider-dependent embedding support.

The primary benefit is provider portability: you can swap or combine backends by changing configuration rather than application code.

## Overview

GenAI Client is centered around the `GenAIProvider` interface:

- Provider implementations are resolved by name using `GenAIProviderManager`.
- You select a provider and its model/configuration.
- You execute requests via `perform()`.

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
| `OPENAI_API_KEY` | Conditional | OpenAI / OpenAI-compatible | API key (or access token) used to authenticate requests. |
| `OPENAI_ORG_ID` | No | OpenAI | Optional organization identifier. |
| `OPENAI_PROJECT_ID` | No | OpenAI | Optional project identifier. |
| `OPENAI_BASE_URL` | No | OpenAI / OpenAI-compatible | Override API base URL (useful for OpenAI-compatible gateways). |
| `GENAI_USERNAME` | Conditional | CodeMie | Username used to obtain an access token (or a client id if using client_credentials). |
| `GENAI_PASSWORD` | Conditional | CodeMie | Password used to obtain an access token (or a client secret if using client_credentials). |
| `AUTH_URL` | No | CodeMie | OAuth2 token endpoint override (defaults to CodeMie Keycloak token URL). |

> Note: The Web provider uses Anteater recipes and is typically configured via `model(...)` and JVM system properties (for example, `-Drecipes=...`) rather than environment variables.

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
| `model(String)` | Sets the provider model name (OpenAI/CodeMie) or configuration name (Web). | Provider-dependent |
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

The `OpenAIProvider` integrates with the OpenAI API as a concrete `GenAIProvider` implementation.

It enables:

- Sending prompts and receiving responses from OpenAI chat models.
- Managing files for use in OpenAI workflows.
- Performing common LLM tasks such as text generation, summarization, and question answering.
- Creating vector embeddings for use cases like semantic search and similarity analysis.

**Environment variables** (must set at least `OPENAI_API_KEY`)

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

**Thread safety:** not thread-safe.

### CodeMie

The `CodeMieProvider` extends `OpenAIProvider` and authenticates against CodeMie, then calls CodeMie’s OpenAI-compatible API endpoint.

How it works:

- Obtains an access token from CodeMie Keycloak.
- Uses the access token as the API key when creating the OpenAI client.
- Uses the CodeMie OpenAI-compatible base URL: `https://codemie.lab.epam.com/code-assistant-api/v1`.

**Authentication / configuration**

- `GENAI_USERNAME` (required)
- `GENAI_PASSWORD` (required)
- `AUTH_URL` (optional; defaults to CodeMie Keycloak token URL)

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("CodeMie:gpt-5.1");
```

**Thread safety:** not thread-safe.

### None

The `NoneProvider` is a no-op implementation of `GenAIProvider`.

This provider is intended for environments where no external LLM integration should be used. It accumulates prompt text in memory and can optionally write instructions and prompts to local files when `inputsLog(File)` has been configured.

**Key characteristics**

- No network calls are performed.
- `perform()` always returns `null`.
- Unsupported capabilities (for example, `embedding(String)`) throw an exception.

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("None:");
provider.inputsLog(new java.io.File(".\\inputsLog\\inputs.txt"));
provider.instructions("You are a helpful assistant.");
provider.prompt("Describe the weather.");
provider.perform();
provider.close();
```

**Thread safety:** thread-safe.

### Web

The `WebProvider` is a `GenAIProvider` implementation that obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via [Anteater](https://ganteater.com) workspace recipes. The provider loads a workspace configuration (see `model(String)`), initializes the workspace with a project directory (see `setWorkingDir(File)`), and submits the current prompt list by running the `"Submit Prompt"` recipe (see `perform()`).

**Thread safety and lifecycle**

- Not thread-safe.
- Workspace state is stored in static fields; the working directory cannot be changed once initialized in the current JVM instance.
- `close()` closes the underlying workspace.

**Configuration**

- Call `model(String)` to set the Anteater configuration name before initializing the workspace.
- Call `setWorkingDir(File)` once per JVM instance to initialize the shared workspace.
- The workspace start directory defaults to the provided working directory.
- If a directory (or file) exists under `workingDir` at the path specified by system property `recipes` (default: `genai-client/src/main/resources`), it is used instead.

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
provider.model("config.yaml");
provider.setWorkingDir(new java.io.File("C:\\path\\to\\project"));
String response = provider.perform();
provider.close();
```

**Thread safety:** not thread-safe.

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/genai-client
- Badge: https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg
- GitHub: https://github.com/machanism-org/machai
