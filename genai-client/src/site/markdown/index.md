---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
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
canonical: https://machai.machanism.org/genai-client/index.html
---

# GenAI Client

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)

## Introduction

GenAI Client is a Java library for integrating JVM applications with generative AI providers through a shared, provider-agnostic API. It supplies the core abstractions used to build prompt-driven workflows, execute model requests, invoke tools during model runs, and generate embeddings for search and retrieval scenarios.

The library helps teams add AI functionality without coupling application logic to a single provider SDK. By standardizing configuration, execution, and usage reporting across providers, it makes it easier to switch models, test integrations, support disabled-AI environments, and evolve AI-enabled features across the MachAI ecosystem.

## Overview

This project provides the central provider contract and supporting infrastructure for GenAI integrations in MachAI.

- `Genai` defines a common interface for prompts, instructions, execution, embeddings, tool registration, working-directory propagation, and usage reporting.
- `GenaiProviderManager` resolves providers from identifiers such as `OpenAI:gpt-4.1`, initializes them from a `Configurator`, and aggregates token usage.
- `OpenAIProvider` delivers the most complete implementation, including prompt execution, tool calling, embeddings, and request logging.
- `CodeMieProvider` authenticates against CodeMie and delegates execution to a compatible downstream provider based on the configured model name.
- `NoneProvider` supports offline, disabled, and test scenarios without invoking any external AI service.

This abstraction lets applications keep a stable integration layer while changing models, providers, or runtime environments.

## Key Features

- Unified `Genai` API for prompts, instructions, execution, embeddings, and tool registration.
- Provider resolution through `GenaiProviderManager` using `Provider:model` selectors.
- OpenAI-backed implementation with support for response execution, function tool calling, input logging, and embeddings.
- CodeMie integration with OAuth-based token acquisition and provider delegation by model prefix.
- No-op provider for offline, disabled, and test environments.
- Token usage tracking via `Usage` and aggregated logging via `GenaiProviderManager`.
- Working-directory propagation for tool handlers that need file-system context.
- Extensible tool function integration through `ToolFunction` and provider-specific tool registration.

## Getting Started

### Prerequisites

- Java 8 or later.
- Maven 3.x.
- Access credentials for the provider you intend to use.
- Network access to the selected AI service when using a remote provider.

### Environment Variables

| Variable | Required | Used by | Description |
|---|---:|---|---|
| `OPENAI_API_KEY` | Conditional | OpenAI / OpenAI-compatible | API key or bearer token used to authenticate requests. |
| `OPENAI_BASE_URL` | No | OpenAI / OpenAI-compatible | Optional base URL override for compatible endpoints. |
| `GENAI_USERNAME` | Conditional | CodeMie | User e-mail for password grant or client id for client-credentials flow. |
| `GENAI_PASSWORD` | Conditional | CodeMie | Password for password grant or client secret for client-credentials flow. |
| `AUTH_URL` | No | CodeMie | Optional override for the OAuth token endpoint. |
| `GENAI_TIMEOUT` | No | OpenAI / OpenAI-compatible | Request timeout in seconds. Default is `600`. |
| `MAX_OUTPUT_TOKENS` | No | OpenAI / OpenAI-compatible | Maximum number of output tokens. Default is `18000`. |
| `MAX_TOOL_CALLS` | No | OpenAI / OpenAI-compatible | Maximum number of tool calls allowed in a response loop. Default is `0`. |
| `embedding.model` | No | OpenAI / OpenAI-compatible | Embedding model identifier used by `embedding(...)`. |

### Basic Usage

```java
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;

Configurator conf = new Configurator();
conf.set("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY"));

Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4.1", conf);
provider.instructions("Answer concisely.");
provider.prompt("Summarize this project in one paragraph.");
String result = provider.perform();
```

### Typical Workflow

1. Create or load a `Configurator` with provider-specific settings.
2. Resolve a provider with `GenaiProviderManager.getProvider("Provider:model", conf)`.
3. Optionally set instructions, input logging, a working directory, or tool functions.
4. Add one or more prompts.
5. Execute the request with `perform()`.
6. Read the response and inspect `usage()` when token metrics are needed.
7. Call `clear()` before reusing the same provider instance for a new interaction.

## Configuration

### Common configuration parameters

| Parameter | Description | Default |
|---|---|---|
| `chatModel` | Provider model identifier set by `GenaiProviderManager` from the `Provider:model` selector. | None |
| `OPENAI_API_KEY` | API key or access token for OpenAI-compatible backends. | None |
| `OPENAI_BASE_URL` | Base URL override for OpenAI-compatible endpoints. | SDK/provider default |
| `GENAI_USERNAME` | CodeMie username or client id. | None |
| `GENAI_PASSWORD` | CodeMie password or client secret. | None |
| `AUTH_URL` | CodeMie token endpoint override. | `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token` |
| `GENAI_TIMEOUT` | OpenAI request timeout in seconds. | `600` |
| `MAX_OUTPUT_TOKENS` | OpenAI maximum output tokens. | `18000` |
| `MAX_TOOL_CALLS` | OpenAI maximum tool calls. | `0` |
| `embedding.model` | OpenAI embedding model identifier. | None |
| `logInputs` | Enables writing provider inputs through provider-specific logging support. | Not set |
| `genai.serverId` | Configurable server identifier exposed by the provider contract. | Not set |

### Example: configure and run with custom parameters

```java
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;

Configurator conf = new Configurator();
conf.set("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY"));
conf.set("OPENAI_BASE_URL", System.getenv("OPENAI_BASE_URL"));
conf.set("GENAI_TIMEOUT", "120");
conf.set("MAX_OUTPUT_TOKENS", "4000");
conf.set("MAX_TOOL_CALLS", "4");
conf.set("embedding.model", "text-embedding-3-large");

Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4.1", conf);
provider.instructions("Use a professional tone.");
provider.prompt("Explain how to add a custom tool.");
String answer = provider.perform();
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

`OpenAIProvider` is the primary production-ready provider in this module. It adapts the OpenAI Java SDK Responses API to the MachAI `Genai` contract and supports prompting, file inputs, tool/function calling, and embedding generation.

Configuration values are read from the `Configurator` passed to `init(...)`.

- `chatModel` (required): model identifier sent to the OpenAI Responses API, such as `gpt-4.1` or `gpt-4o`.
- `OPENAI_API_KEY` (required): API key used to authenticate with the OpenAI API.
- `OPENAI_BASE_URL` (optional): base URL override for OpenAI-compatible endpoints.
- `GENAI_TIMEOUT` (optional): request timeout in seconds. Defaults to `600`.
- `MAX_OUTPUT_TOKENS` (optional): maximum number of output tokens. Defaults to `18000`.
- `MAX_TOOL_CALLS` (optional): maximum number of tool calls in a single response loop. A value of `0` leaves the limit unset.
- `embedding.model` (optional): embedding model identifier used by `embedding(String, long)`.

Capabilities include:

- Prompt execution through `perform()`.
- Iterative function tool calling with tool-output feedback.
- Input logging to local files.
- Working-directory propagation to tool handlers.
- Embedding generation.
- Usage reporting through `Usage` and `GenaiProviderManager`.

Thread safety: not thread-safe.

### CodeMie

`CodeMieProvider` integrates with EPAM CodeMie. It authenticates against a CodeMie OpenID Connect token endpoint to obtain an OAuth 2.0 access token, configures the CodeMie Code Assistant API as an OpenAI-compatible backend, and delegates execution to another provider implementation.

Authentication modes:

- Password grant is used when `GENAI_USERNAME` contains `@`.
- Client credentials grant is used otherwise.

After retrieving a token, the provider sets:

- `OPENAI_BASE_URL` to `https://codemie.lab.epam.com/code-assistant-api/v1`
- `OPENAI_API_KEY` to the retrieved access token

Delegation is selected from the configured `chatModel`:

- `gpt-*` or blank model names delegate to `OpenAIProvider`.
- `gemini-*` model names delegate to `GeminiProvider`.
- `claude-*` model names delegate to `ClaudeProvider`.

Configuration:

- `GENAI_USERNAME` (required): user e-mail or client id.
- `GENAI_PASSWORD` (required): password or client secret.
- `chatModel` (required): model identifier.
- `AUTH_URL` (optional): token endpoint override.

Thread safety: not thread-safe.

### Claude

`ClaudeProvider` is an Anthropic-backed implementation of the MachAI `Genai` abstraction.

Its documented purpose is to adapt the Anthropic Java SDK to the MachAI provider interface. The current implementation is not finished: `init(Configurator)` and the core prompt, execution, tool, logging, working-directory, and usage methods all throw `UnsupportedOperationException`. The `embedding(String, long)` method currently returns an empty list.

Thread safety: not documented; treat as not thread-safe.

### Gemini

`GeminiProvider` is intended to integrate Google's Gemini models with the MachAI `Genai` API.

Its documented design covers MachAI abstractions for prompts, tool definitions, files or attachments, and usage reporting mapped onto Gemini APIs. The current implementation is explicitly marked as a placeholder. `init(Configurator)`, `perform()`, and `embedding(...)` throw `NotImplementedException`, while other operations remain TODO stubs.

Thread safety: not documented; treat as not thread-safe.

### None

`NoneProvider` is a no-op implementation of `Genai` intended for environments where no external LLM integration should be used.

It accumulates prompt text in memory, can optionally write instructions and prompts to local files when `inputsLog(File)` is configured, performs no network calls, and always returns `null` from `perform()`. Unsupported capabilities such as `embedding(String, long)` throw `UnsupportedOperationException`.

Thread safety: not documented; treat as not thread-safe.

## Resources

- Project site: https://machai.machanism.org/genai-client/
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/genai-client
- GitHub: https://github.com/machanism-org/machai
- SCM URL: https://github.com/machanism-org/machai.git
