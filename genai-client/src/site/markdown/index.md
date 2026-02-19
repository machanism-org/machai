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

GenAI Client is a Java library for integrating applications with Generative AI providers via a single, consistent API. It provides foundational capabilities such as prompt/instruction management, optional tool (function) calling, optional file attachments, and (provider-dependent) embeddings.

The main benefits are:

- **Provider portability:** switch or combine backends mostly through configuration rather than rewriting calling code.
- **A stable abstraction:** use the same `GenAIProvider` interface across OpenAI-compatible APIs, token-brokered gateways (like CodeMie), UI-automation providers, or no-op operation.
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

- Java 11+.
- Maven.
- Provider credentials/configuration (depending on the provider you use):
  - OpenAI-compatible providers: API key/token and (optionally) a base URL override.
  - CodeMie provider: credentials for token acquisition.
  - Web provider: Anteater workspace recipes/configuration and a supported web UI environment.

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
| `ANTHROPIC_API_KEY` | Conditional | Claude | API key used to authenticate requests. |
| `ANTHROPIC_BASE_URL` | No | Claude | Override API base URL (if using a compatible gateway/proxy). |

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
| `model(String)` | Sets the provider model name (OpenAI/CodeMie/Claude) or configuration name (Web). | Provider-dependent |
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

`OpenAIProvider` is an OpenAI-backed implementation of the `GenAIProvider` abstraction.

This provider adapts the OpenAI Java SDK to MachAI's provider interface. It accumulates user inputs (text prompts and optional file references), optional system-level instructions, and an optional set of function tools. When `perform()` is invoked, the provider calls the OpenAI Responses API, processes the model output (including iterative function tool calls), and returns the final assistant text.

**Capabilities**

- Submit prompts and retrieve text responses.
- Upload local files or attach files by URL for use in a request.
- Register function tools and dispatch tool calls to application handlers.
- Create vector embeddings for input text.
- Report token usage to `GenAIProviderManager`.

**Configuration**

- `OPENAI_API_KEY` (required)
- `OPENAI_BASE_URL` (optional)
- `chatModel` (optional; required before `perform()` if not set via configuration)

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

**Thread safety:** not thread-safe.

### CodeMie

`CodeMieProvider` is a GenAI provider implementation for EPAM CodeMie.

This provider obtains an access token from a configurable OpenID Connect token endpoint and then initializes an OpenAI-compatible client (via `OpenAIProvider`) to call the CodeMie Code Assistant REST API.

After a token is retrieved, this provider configures the underlying OpenAI-compatible provider by setting:

- `OPENAI_BASE_URL` to the CodeMie API base URL
- `OPENAI_API_KEY` to the retrieved access token

and then delegates requests to either `OpenAIProvider` (for `gpt-*` models) or `ClaudeProvider` (for `claude-*` models).

**Authentication modes**

The authentication mode is selected based on the configured username:

- If the username contains `@`, the password grant is used (typical user e-mail login).
- Otherwise, the client credentials grant is used (service-to-service).

**Authentication / configuration**

- `GENAI_USERNAME` (required)
- `GENAI_PASSWORD` (required)
- `chatModel` (required)
- `AUTH_URL` (optional; token endpoint override)

**Endpoints (defaults)**

- Token endpoint: `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token` (override with `AUTH_URL`)
- OpenAI-compatible base URL: `https://codemie.lab.epam.com/code-assistant-api/v1`

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("CodeMie:gpt-5.1");
```

**Thread safety:** not thread-safe.

### Claude

`ClaudeProvider` is intended to be an Anthropic-backed implementation of the `GenAIProvider` abstraction.

It is currently a stub: most methods are not implemented and `init(...)` throws `NotImplementedError`.

**Configuration**

- Not available yet.

**Usage example**

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Claude:claude-...");
```

**Thread safety:** not thread-safe.

### None

`NoneProvider` is a no-op implementation of `GenAIProvider` intended for environments where no external LLM integration should be used.

It accumulates prompt text in memory and can optionally write instructions and prompts to local files when `inputsLog(File)` has been configured.

**Key characteristics**

- No network calls are performed.
- `perform()` always returns `null`.
- Unsupported capabilities (for example, `embedding(String)`) throw `UnsupportedOperationException`.

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

`WebProvider` is a `GenAIProvider` implementation that obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via Anteater workspace recipes. The provider loads a workspace configuration (see `model(String)`), initializes the workspace with a project directory (see `setWorkingDir(File)`), and submits the current prompt list by running the `"Submit Prompt"` recipe (see `perform()`).

**Thread safety and lifecycle**

- Not thread-safe.
- Workspace state is stored in static fields; the working directory cannot be changed once initialized in the current JVM instance.
- `close()` closes the underlying workspace.

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
