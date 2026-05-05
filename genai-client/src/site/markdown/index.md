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

GenAI Client is a Java library designed for seamless integration with generative AI providers. It provides foundational prompt management, model execution, tool/function calling, embedding support, and usage reporting so JVM applications can add AI-powered features through a consistent provider-agnostic API.

The library reduces direct coupling to individual provider SDKs. Applications can build prompt-driven workflows, semantic search, retrieval, automated content generation, and intelligent project assembly while keeping provider selection, authentication, request limits, and runtime behavior configurable across the MachAI and Machanism ecosystem.

## Overview

This project provides the central provider contract and supporting infrastructure for GenAI integrations in MachAI.

- `Genai` defines the common lifecycle and operations for initialization, instructions, prompts, file references, request execution, embeddings, tool registration, working-directory propagation, cleanup, and usage reporting.
- `GenaiProviderManager` resolves providers from identifiers such as `OpenAI:gpt-4.1`, initializes them from a `Configurator`, and aggregates token usage.
- `OpenAIProvider` is the production-ready OpenAI-compatible implementation, including prompt execution through the OpenAI Responses API, iterative tool calling, embeddings, request logging, and usage mapping.
- `CodeMieProvider` authenticates against EPAM CodeMie and delegates execution to an OpenAI-compatible downstream provider configured for the CodeMie Code Assistant API.
- `NoneProvider` supports disabled, offline, and test scenarios without invoking external AI services.

This abstraction lets applications keep a stable integration layer while changing models, providers, or runtime environments.

## Key Features

- Unified `Genai` API for prompts, instructions, execution, embeddings, and tool registration.
- Provider resolution through `GenaiProviderManager` using `Provider:model` selectors or fully qualified provider class names.
- OpenAI-compatible implementation with support for response execution, function tool calling, input logging, timeout configuration, output-token limits, and embeddings.
- CodeMie integration with OAuth-based token acquisition and OpenAI-compatible backend configuration.
- No-op provider for offline, disabled, and test environments.
- Token usage tracking via `Usage` and aggregated logging via `GenaiProviderManager`.
- Working-directory propagation for tool handlers that need file-system context.
- Extensible tool function integration through `ToolFunction`, `FunctionTools`, and Java `ServiceLoader` discovery.

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

`OpenAIProvider` is the primary production-ready provider in this module. It adapts the OpenAI Java SDK Responses API to the MachAI `Genai` contract and supports prompt submission, optional system instructions, file references, locally registered tools, embedding generation, timeout handling, input logging, response parsing, function-tool execution, and OpenAI token accounting mapped to MachAI usage metrics.

Configuration values are read from the `Configurator` passed to `init(...)`.

- `chatModel` (required): model identifier sent to the OpenAI Responses API, such as `gpt-4.1` or `gpt-4o-mini`.
- `OPENAI_API_KEY` (required): API key used to authenticate with the OpenAI API or compatible backend.
- `OPENAI_BASE_URL` (optional): base URL override for OpenAI-compatible endpoints.
- `GENAI_TIMEOUT` (optional): request timeout in seconds. Defaults to `600`.
- `MAX_OUTPUT_TOKENS` (optional): maximum number of output tokens. Defaults to `18000`.
- `MAX_TOOL_CALLS` (optional): maximum number of tool calls in a single response loop. A value of `0` leaves the limit unset.
- `embedding.model` (optional): embedding model identifier used by `embedding(String, long)`.

Capabilities include:

- Prompt execution through `perform()`.
- Iterative function tool calling with tool-output feedback until a final model message is returned.
- Input logging to local files.
- Working-directory propagation to tool handlers.
- Embedding generation.
- Usage reporting through `Usage` and `GenaiProviderManager`.

Thread safety: not thread-safe.

### CodeMie

`CodeMieProvider` integrates with EPAM CodeMie. It authenticates against a CodeMie OpenID Connect token endpoint to obtain an OAuth 2.0 access token, configures the CodeMie Code Assistant API as an OpenAI-compatible backend, and delegates execution to an internal provider instance.

Authentication modes:

- Password grant is used when `GENAI_USERNAME` contains `@`, which is the typical e-mail login mode.
- Client credentials grant is used otherwise for service-to-service authentication.

After retrieving a token, the provider sets:

- `OPENAI_BASE_URL` to `https://codemie.lab.epam.com/code-assistant-api/v1`
- `OPENAI_API_KEY` to the retrieved access token

Configuration:

- `GENAI_USERNAME` (required): user e-mail or client id.
- `GENAI_PASSWORD` (required): password or client secret.
- `chatModel` (required): model identifier passed to the delegated provider, for example `gpt-4o-mini`.
- `AUTH_URL` (optional): token endpoint override.

The current source delegates to `OpenAIProvider`, which enables CodeMie access through the same OpenAI-compatible execution, tool-calling, logging, embedding, and usage-reporting behavior described for the OpenAI provider.

Thread safety: not thread-safe.

### None

`NoneProvider` is a no-operation `Genai` implementation for disabled, offline, test, or prompt-capture scenarios.

It fulfills the provider contract without invoking any local or remote AI model. Instructions and prompts are accumulated in memory, and when `inputsLog(File)` is configured they can be written to local log files for inspection. Calling `perform()` performs no network activity and returns `null`, while unsupported capabilities such as `embedding(String, long)` throw `UnsupportedOperationException`.

Typical use cases include:

- Running application flows when AI execution is intentionally disabled.
- Testing provider integration paths without contacting an external service.
- Capturing generated prompts and instructions for review.

Thread safety: not documented; treat as not thread-safe.

## Resources

- Project site: https://machai.machanism.org/genai-client/
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/genai-client
- GitHub: https://github.com/machanism-org/machai
- SCM URL: https://github.com/machanism-org/machai.git
