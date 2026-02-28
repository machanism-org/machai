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

GenAI Client is a Java library that provides a single, consistent API for integrating with multiple Generative AI providers. It offers a provider abstraction (`GenAIProvider`), centralized provider selection (`GenAIProviderManager`), and common building blocks such as prompts/instructions, optional tool (function) calling, optional file attachments, and (provider-dependent) embeddings.

Key benefits:

- **Provider portability:** switch or combine backends mostly through configuration rather than rewriting calling code.
- **Stable abstraction:** use the same `GenAIProvider` interface across OpenAI-compatible APIs, token-brokered gateways like CodeMie, or disable AI entirely via the `None` provider.
- **Extensibility:** register Java functions as tools so models can request structured actions (provider-dependent).

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

- Java 8+.
- Maven.
- Provider credentials/configuration (depending on the provider you use):
  - OpenAI-compatible providers: API key/token and (optionally) a base URL override.
  - CodeMie provider: credentials for token acquisition.

### Environment Variables

| Variable | Required | Used by | Description |
|---|---:|---|---|
| `OPENAI_API_KEY` | Conditional | OpenAI / OpenAI-compatible / CodeMie (internal delegation) | API key (or access token) used to authenticate requests. |
| `OPENAI_ORG_ID` | No | OpenAI | Optional organization identifier. |
| `OPENAI_PROJECT_ID` | No | OpenAI | Optional project identifier. |
| `OPENAI_BASE_URL` | No | OpenAI / OpenAI-compatible / CodeMie (internal delegation) | Override API base URL (useful for OpenAI-compatible gateways). |
| `GENAI_USERNAME` | Conditional | CodeMie | Username used to obtain an access token (or a client id if using client_credentials). |
| `GENAI_PASSWORD` | Conditional | CodeMie | Password used to obtain an access token (or a client secret if using client_credentials). |
| `AUTH_URL` | No | CodeMie | OAuth2 token endpoint override (defaults to CodeMie Keycloak token URL). |
| `GENAI_TIMEOUT` | No | OpenAI / OpenAI-compatible | Request timeout in seconds; when `0` or missing, SDK defaults are used. |
| `MAX_OUTPUT_TOKENS` | No | OpenAI / OpenAI-compatible | Max output tokens (defaults to `65536`). |
| `MAX_TOOL_CALLS` | No | OpenAI / OpenAI-compatible | Max tool calls per request (defaults to `100`). |

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
| `model(String)` | Sets the provider model name (OpenAI/CodeMie) or configuration name. | Provider-dependent |
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

`OpenAIProvider` is an OpenAI-backed implementation of MachAI's `GenAIProvider` abstraction.

This provider adapts the OpenAI Java SDK to MachAI's provider interface. It accumulates user inputs (text prompts and optional file references), optional system-level instructions, and an optional set of function tools. When `perform()` is invoked, the provider calls the OpenAI Responses API, processes the model output (including iterative function tool calls), and returns the final assistant text.

**Configuration variables consumed by `init(Configurator)`**

- `chatModel`: required model identifier passed to the OpenAI Responses API (for example, `gpt-4.1` or `gpt-4o`).
- `OPENAI_API_KEY`: required API key used to authenticate with the OpenAI API.
- `OPENAI_BASE_URL`: optional base URL for OpenAI-compatible endpoints. If unset, the SDK default base URL is used.
- `GENAI_TIMEOUT`: optional request timeout (in seconds). If missing or `0`, the SDK default timeouts are used.
- `MAX_OUTPUT_TOKENS`: optional maximum number of output tokens (defaults to `65536`).
- `MAX_TOOL_CALLS`: optional maximum number of tool calls allowed in a single response (defaults to `100`).

**Capabilities**

- Submit prompts and retrieve text responses.
- Upload local files or attach files by URL for use in a request.
- Register function tools and dispatch tool calls to application handlers.
- Create vector embeddings for input text.
- Report token usage to `GenAIProviderManager`.

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

**Thread safety:** not thread-safe.

### CodeMie

`CodeMieProvider` is a GenAI provider implementation for EPAM CodeMie.

This provider obtains an access token from a configurable OpenID Connect token endpoint and then initializes an OpenAI-compatible client (via `OpenAIProvider`) to call the CodeMie Code Assistant REST API.

**Authentication modes**

The authentication mode is selected based on the configured username:

- If the username contains `@`, the password grant is used (typical user e-mail login).
- Otherwise, the client credentials grant is used (service-to-service).

**Delegation**

After a token is retrieved, this provider configures the underlying OpenAI-compatible provider by setting:

- `OPENAI_BASE_URL` to the CodeMie API base URL
- `OPENAI_API_KEY` to the retrieved access token

and then delegates requests based on the configured model prefix:

- `OpenAIProvider` for `gpt-*` models
- `GeminiProvider` for `gemini-*` models
- `ClaudeProvider` for `claude-*` models

**Configuration keys consumed by `init(Configurator)`**

- `GENAI_USERNAME` (required): user e-mail (password grant) or client id (client_credentials).
- `GENAI_PASSWORD` (required): password (password grant) or client secret (client_credentials).
- `chatModel` (required): model identifier.
- `AUTH_URL` (optional): token endpoint override.

**Endpoints (defaults)**

- Token endpoint: `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token` (override with `AUTH_URL`)
- OpenAI-compatible base URL: `https://codemie.lab.epam.com/code-assistant-api/v1`

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("CodeMie:gpt-5.1");
```

**Thread safety:** not thread-safe.

### Claude

`ClaudeProvider` is an Anthropic-backed implementation of MachAI's `GenAIProvider` abstraction.

This provider adapts the Anthropic Java SDK to MachAI's provider interface.

The current implementation is not available yet: `init(Configurator)` throws `NotImplementedException` and other methods are currently placeholders.

**Configuration**

- Not available yet.

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Claude:claude-...");
```

**Thread safety:** not thread-safe.

### Gemini

`GeminiProvider` is a MachAI `GenAIProvider` implementation for Google's Gemini models.

This provider is responsible for adapting MachAI's provider-agnostic abstractions (prompts, tool definitions, files/attachments, and usage reporting) to Gemini's specific API.

The current implementation is a placeholder: most operations are not yet implemented and will be completed in a future iteration.

**Configuration**

- Not available yet.

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Gemini:gemini-...");
```

**Thread safety:** not thread-safe.

### None

`NoneProvider` is a no-op implementation of `GenAIProvider`.

This provider is intended for environments where no external LLM integration should be used. It accumulates prompt text in memory and can optionally write instructions and prompts to local files when `inputsLog(File)` has been configured.

**Key characteristics**

- No network calls are performed.
- `perform()` always returns `null`.
- Unsupported capabilities (for example, `embedding(String, long)`) throw `UnsupportedOperationException`.

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("None:");
provider.inputsLog(new java.io.File(".\\inputsLog\\inputs.txt"));
provider.instructions("You are a helpful assistant.");
provider.prompt("Describe the weather.");
provider.perform();
provider.close();
```

**Thread safety:** not specified; treat as not thread-safe unless documented.

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/genai-client
- Badge: https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg
- GitHub: https://github.com/machanism-org/machai
