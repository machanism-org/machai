<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/genai-client/bindex.json)
# Overview
   - Full description the project based on package-info.java files in source folder..
   - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
# Supported AI providers
   - Describe all supported AP providers with configurations.
   - Table of common configuration parameters, their descriptions, and default values.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# GenAI Client

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/genai-client/bindex.json)

## Overview

GenAI Client is a Java library designed for integrating Machai applications with generative AI providers through a consistent provider abstraction. It provides provider resolution, prompt and instruction handling, runtime configuration, optional embedding generation, usage tracking, and Java function-tool, prompt, and resource registration for AI-powered workflows.

The library is organized around the common `Genai` lifecycle contract and shared provider infrastructure. Applications can resolve a configured provider from identifiers such as `OpenAI:gpt-4o-mini`, attach prompts, system instructions, tools, resources, web-search support, or MCP servers, and execute requests without depending directly on vendor-specific SDK details. Token usage is captured in immutable usage records and can be aggregated per model for reporting and diagnostics.

GenAI Client also includes a lightweight tool metadata layer. Java methods annotated as tools, prompts, or resources can be discovered through `ServiceLoader`, described with parameter metadata, filtered for supported application classes, and registered with providers as AI-callable functions or resource callbacks. This enables advanced use cases such as semantic search, automated content generation, intelligent project assembly, structured tool execution, local tool orchestration, and provider-independent prompt workflows.

![GenAI Client structure](./images/c4-diagram.png)

The main package areas are:

- `org.machanism.machai.ai.manager` resolves configured provider identifiers, initializes provider instances, and records token usage by model.
- `org.machanism.machai.ai.provider` defines the common provider contracts and reusable base behavior for conversational, embedding, tool-enabled, web-search-enabled, and MCP-enabled AI integrations.
- `org.machanism.machai.ai.provider.impl` contains concrete provider implementations for OpenAI-compatible APIs, Anthropic Claude, CodeMie, and direct host-side tool execution.
- `org.machanism.machai.ai.tools` defines tool, prompt, resource, parameter, role, and supported-application metadata, plus service loading and callback contracts used to expose Java methods as AI-accessible capabilities.

## Supported AI providers

### OpenAI

The OpenAI provider adapts the Machai `Genai` API to the OpenAI Java SDK Responses API and Embeddings API. It supports conversational text generation, iterative function-tool calling, optional OpenAI web search, MCP server tools, embeddings, request input logging, and OpenAI usage conversion.

Typical configuration includes an OpenAI API key, a chat or embedding model name, and optional values such as a custom OpenAI-compatible base URL, timeout, maximum output tokens, and tool-call limits. It can also be used with OpenAI-compatible endpoints by overriding the base URL.

### Anthropic

The Anthropic provider adapts the Machai `Genai` API to Anthropic Claude models through the Anthropic Java SDK Beta Messages API. It supports prompt execution, system instructions, custom function tools, automatic tool-use loops, optional web search, MCP server forwarding, prompt-cache control on the final registered tool, and token-usage capture.

Typical configuration includes an Anthropic API key or authorization token, a Claude model identifier, and optional values such as a custom base URL, timeout, output-token limits, web-search settings, and MCP server definitions. When local function tools are registered, the provider applies Anthropic ephemeral prompt-cache control to the final registered tool.

### CodeMie

The CodeMie provider integrates with EPAM CodeMie Code Assistant endpoints. It authenticates with a CodeMie OpenID Connect token endpoint, obtains OAuth 2.0 bearer tokens, configures the delegated AI provider with the CodeMie API base URL, and routes supported model families to the appropriate implementation.

Models beginning with `gpt-`, `gemini-`, `text-embedding-`, `codemie-text-embedding-`, or `amazon.titan-embed-text-` are delegated to the OpenAI provider configured for CodeMie endpoints. Models beginning with `claude-` are delegated to the Anthropic provider. The provider supports password-grant and client-credentials authentication flows based on `GENAI_USERNAME` and `GENAI_PASSWORD`, with an optional `AUTH_URL` token-endpoint override.

### Tools provider

The Tools provider exposes registered application functions for structured invocation. It collects prompts, registers `ToolFunction` callbacks, and executes those callbacks from YAML-based tool-call descriptions containing a tool name and parameter payload. This provider is useful for internal orchestration when host-defined tools need to be invoked through the same lifecycle as other AI providers without sending the request to an external model service.

### None provider

The None provider is a disabled, no-op implementation for configurations that must not make AI requests. It accepts the standard provider lifecycle calls, discards submitted state, and returns `null` from execution. Initialize it with the model name `log` to emit its implemented lifecycle activity at INFO level; other model names remain silent. It is useful as a safe default and in tests.

## Common configuration parameters

| Parameter | Description | Default value |
| --- | --- | --- |
| Provider/model identifier | Model identifier used by the selected provider. `GenaiProviderManager` commonly resolves identifiers in the `Provider:Model` form, such as `OpenAI:gpt-4o-mini`; the provider itself receives the model portion. | Required |
| `OPENAI_API_KEY` | API key for OpenAI or OpenAI-compatible endpoints. CodeMie sets this to the retrieved OAuth 2.0 bearer token for delegated OpenAI-compatible requests. | Required for OpenAI-compatible providers |
| `OPENAI_BASE_URL` | Optional base URL override for OpenAI-compatible APIs. | OpenAI SDK default |
| `ANTHROPIC_API_KEY` | API key or authorization token for Anthropic Claude requests. | Required for Anthropic |
| `ANTHROPIC_BASE_URL` | Optional base URL override for Anthropic-compatible APIs. | Anthropic SDK default |
| `GENAI_TIMEOUT` | Request timeout in seconds. A value of `0` or an absent value leaves SDK defaults in effect. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum number of tokens the model may generate. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of tool calls the OpenAI Responses API may issue in a response loop. A value of `0` leaves the limit unset. | `0` |
| `WebSearchTool.type` | Enables provider-specific web search when present. The value `default` maps to the provider default web-search tool type where supported. | Not set |
| `WebSearchTool.city` | Optional city hint for web-search user location. | Not set |
| `WebSearchTool.country` | Optional country hint for web-search user location. | Not set |
| `WebSearchTool.region` | Optional region hint for web-search user location. | Not set |
| `MCP.url` | URL for the first MCP server tool. Additional servers can be configured with numbered groups such as `MCP_1.url`, `MCP_2.url`, and so on. | Not set |
| `MCP.name` | Provider-visible MCP server name. Additional servers can use `MCP_1.name`, `MCP_2.name`, and so on. A name is required for a configured group to be registered. | Not set |
| `MCP.authorization` | Optional authorization value for the MCP server. Additional servers can use numbered variants. | Not set |
| `MCP.description` | Optional MCP server description. Additional servers can use numbered variants. | Not set |
| `GENAI_USERNAME` | Generic username used by provider authentication flows. CodeMie can use it for password-grant authentication. | Provider-specific |
| `GENAI_PASSWORD` | Generic password or secret used by provider authentication flows. CodeMie can use it as a password or client secret. | Provider-specific |
| `AUTH_URL` | Optional CodeMie OpenID Connect token endpoint override. | CodeMie default endpoint |

## Resources

- [Machai GenAI Client site](https://machai.machanism.org/genai-client/index.html)
- [Machanism platform](https://machanism.org/)
- [Machai project documentation](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai.git)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
- [API documentation](https://machai.machanism.org/genai-client/apidocs/)
