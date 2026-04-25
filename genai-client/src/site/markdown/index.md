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

GenAI Client is a Java library for integrating Java applications with generative AI providers through a consistent, provider-agnostic API. It centralizes prompt handling, model execution, embedding generation, tool registration, and usage tracking so applications can add AI capabilities without binding their core logic to a single vendor.

The library is designed to support practical AI workflows in MachAI modules and other JVM-based projects, including summarization, automation, semantic search, structured prompt execution, and provider-backed tool calling. By hiding provider-specific details behind the `Genai` contract, it makes AI integrations easier to build, test, evolve, and switch over time.

## Overview

The project revolves around the `Genai` interface and the `GenaiProviderManager` factory:

- `Genai` defines the common operations for prompts, instructions, execution, embeddings, tool registration, working-directory awareness, and input logging.
- `GenaiProviderManager` resolves providers from identifiers such as `OpenAI:gpt-4.1` and initializes them from a `Configurator`.
- Provider implementations encapsulate backend-specific authentication and API behavior while preserving a unified application-facing programming model.
- Usage metrics are exposed through `Usage` and can be aggregated centrally for reporting.

This architecture lets consumers keep application code stable while changing models, switching providers, or disabling AI entirely with the `None` provider for non-AI environments and testing scenarios.

## Key Features

- Unified `Genai` abstraction for prompts, instructions, execution, embeddings, and tools.
- Provider resolution by name or fully qualified class through `GenaiProviderManager`.
- OpenAI provider with prompt execution, file inputs, tool calling, embeddings, and usage capture.
- CodeMie integration that acquires OAuth tokens and delegates to OpenAI-, Gemini-, or Claude-style backends based on model naming.
- No-op `None` provider for environments where external AI access must be disabled.
- Optional input logging for prompts and instructions to support traceability and debugging.
- Optional working-directory propagation to tool handlers.
- Tool registration API for structured function invocation during model execution.

## Getting Started

### Prerequisites

- Java 8 or later.
- Maven 3.x.
- Access credentials for the provider you intend to use.
- Network access to the selected GenAI endpoint when using a remote provider.

### Environment Variables

| Variable | Required | Used by | Description |
|---|---:|---|---|
| `OPENAI_API_KEY` | Conditional | OpenAI / OpenAI-compatible | API key or bearer token used to authenticate requests. |
| `OPENAI_BASE_URL` | No | OpenAI / OpenAI-compatible | Optional base URL override for compatible endpoints. |
| `GENAI_USERNAME` | Conditional | CodeMie | User e-mail for password grant or client id for client-credentials flow. |
| `GENAI_PASSWORD` | Conditional | CodeMie | Password for password grant or client secret for client-credentials flow. |
| `AUTH_URL` | No | CodeMie | Optional override for the OAuth token endpoint. |
| `GENAI_TIMEOUT` | No | OpenAI / OpenAI-compatible | Request timeout in seconds. Defaults to `600` when configured through `OpenAIProvider`. |
| `MAX_OUTPUT_TOKENS` | No | OpenAI / OpenAI-compatible | Maximum number of output tokens for a response. Default is `18000`. |
| `MAX_TOOL_CALLS` | No | OpenAI / OpenAI-compatible | Maximum number of tool calls allowed in a response. Provider applies this only when greater than `0`. |
| `embedding.model` | No | OpenAI / OpenAI-compatible | Embedding model identifier used by `embedding(...)`. |

### Basic Usage

```java
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;

Configurator conf = new Configurator();
Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4.1", conf);
provider.instructions("Answer concisely.");
provider.prompt("Summarize this project in one paragraph.");
String result = provider.perform();
```

### Typical Workflow

1. Create or load a `Configurator` containing provider settings.
2. Resolve a provider with `GenaiProviderManager.getProvider("Provider:model", conf)`.
3. Optionally set instructions, input logging, a working directory, or tool functions.
4. Add one or more prompts.
5. Execute the request with `perform()`.
6. Read the response and inspect `usage()` when token metrics are needed.
7. Call `clear()` before reusing the same provider instance for a new conversation.

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
| `logInputs` | Enables writing request inputs through provider-specific logging support. | Not set |
| `genai.serverId` | Configurable server identifier exposed by the provider contract. | Not set |

### Example: configure and run with custom parameters

```java
Configurator conf = new Configurator();
conf.set("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY"));
conf.set("OPENAI_BASE_URL", System.getenv("OPENAI_BASE_URL"));
conf.set("GENAI_TIMEOUT", "120");
conf.set("MAX_OUTPUT_TOKENS", "4000");
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

`OpenAIProvider` is the primary production-ready provider in this module. It adapts the OpenAI Java SDK Responses API to the MachAI `Genai` contract and supports the core library workflow of prompt submission, response execution, tool calling, request logging, and embedding generation.

**Configuration**

- `chatModel` (required): model identifier such as `gpt-4.1` or `gpt-4o`.
- `OPENAI_API_KEY` (required): API key used to authenticate with the API.
- `OPENAI_BASE_URL` (optional): override for OpenAI-compatible endpoints.
- `GENAI_TIMEOUT` (optional): timeout in seconds. Defaults to `600`.
- `MAX_OUTPUT_TOKENS` (optional): maximum output tokens. Defaults to `18000`.
- `MAX_TOOL_CALLS` (optional): maximum number of tool calls. Applied when greater than `0`; initialization default is `0`.
- `embedding.model` (optional): embedding model used by `embedding(String, long)`.

**Capabilities**

- Prompt execution through `perform()`.
- Iterative function tool calling with tool-output feedback.
- Input logging to local files.
- Working-directory propagation to tool handlers.
- Embedding generation.
- Usage reporting through `Usage` and `GenaiProviderManager`.

**Thread safety:** not thread-safe.

### CodeMie

`CodeMieProvider` integrates with EPAM CodeMie by obtaining an OAuth 2.0 access token from a CodeMie OpenID Connect endpoint and then delegating execution to another provider implementation.

**Authentication modes**

- Password grant is used when `GENAI_USERNAME` contains `@`.
- Client credentials grant is used otherwise.

**Delegation behavior**

After authentication, the provider sets `OPENAI_BASE_URL` to `https://codemie.lab.epam.com/code-assistant-api/v1` and injects the retrieved access token as `OPENAI_API_KEY`.

Delegation is selected from the configured `chatModel`:

- `gpt-*` or blank model names delegate to `OpenAIProvider`.
- `gemini-*` model names delegate to `GeminiProvider`.
- `claude-*` model names delegate to `ClaudeProvider`.

**Configuration**

- `GENAI_USERNAME` (required): user e-mail or client id.
- `GENAI_PASSWORD` (required): password or client secret.
- `chatModel` (required): model identifier.
- `AUTH_URL` (optional): token endpoint override.

**Thread safety:** not thread-safe.

### Claude

`ClaudeProvider` is intended to adapt Anthropic models to the MachAI `Genai` API.

The current implementation is only a placeholder. Its documented purpose is to provide an Anthropic-backed provider using the Anthropic Java SDK, but `init(Configurator)` and most operational methods currently throw `UnsupportedOperationException`. The `embedding(String, long)` method currently returns an empty list rather than performing real embedding generation.

**Thread safety:** not thread-safe.

### Gemini

`GeminiProvider` is intended to provide MachAI integration for Google's Gemini models.

Its documented design covers prompts, tool definitions, files and attachments, and usage reporting mapped onto Gemini APIs. At present, however, it is a partial placeholder: `init(Configurator)`, `perform()`, and `embedding(...)` throw `NotImplementedException`, while several other methods contain TODO placeholders and no production logic yet.

**Thread safety:** not thread-safe.

### None

`NoneProvider` is a no-op implementation of `Genai` for environments where no external AI integration should run.

It stores prompts in memory, optionally writes instructions and prompt inputs to local files when `inputsLog(File)` is configured, performs no network activity, and always returns `null` from `perform()`. Unsupported capabilities such as embedding generation throw `UnsupportedOperationException`.

**Thread safety:** treat as not thread-safe.

## Resources

- Project site: https://machai.machanism.org/genai-client/
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/genai-client
- GitHub: https://github.com/machanism-org/machai
- SCM URL: https://github.com/machanism-org/machai.git
