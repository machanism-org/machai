---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
# Overview
   - Full description the project based on package-info.java files in source folder..
# Supported AI providers
   - Describe all supported AP providers with configurations.
   - Table of common configuration parameters, their descriptions, and default values.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->
canonical: https://machai.machanism.org/genai-client/index.html
---

# GenAI Client

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)

## Overview

GenAI Client is a Java library for integrating Machai applications with generative AI platforms through a consistent provider abstraction. It provides prompt and instruction handling, provider resolution, runtime configuration, embedding support, usage tracking, and Java function-tool registration for AI-powered workflows across the Machanism ecosystem.

The library is organized around a common `Genai` contract and shared provider infrastructure. Applications can resolve a configured model provider, add prompts, instructions, files, tools, web-search support, or MCP servers, and then execute requests without depending directly on vendor-specific SDK details. Usage information is captured as token counts and can be aggregated per model for reporting and diagnostics.

GenAI Client also includes a lightweight tool metadata layer. Java methods annotated as tools or prompts can be discovered through `ServiceLoader`, described with parameter metadata, and registered with providers as AI-callable functions. This enables advanced use cases such as semantic search, automated content generation, intelligent project assembly, structured tool execution, and provider-independent prompt orchestration.

## Supported AI providers

### OpenAI

The OpenAI provider adapts the Machai `Genai` API to the OpenAI Java SDK Responses API. It supports conversational text generation, file-based inputs, iterative function-tool calling, optional OpenAI web search, MCP server tools, embeddings, request input logging, and OpenAI usage conversion.

Typical configuration includes an OpenAI API key, a chat or embedding model name, and optional values such as a custom OpenAI-compatible base URL, timeout, maximum output tokens, and tool-call limits.

### Anthropic

The Anthropic provider adapts the Machai `Genai` API to Anthropic Claude models through the Anthropic Java SDK. It supports prompt execution, system instructions, custom function tools, automatic tool-use loops, optional web search, MCP server forwarding, and token-usage capture.

Typical configuration includes an Anthropic API key, a Claude model identifier, and optional values such as a custom base URL, timeout, output-token limits, tool-call limits, and prompt-cache thresholds for large tool results.

### CodeMie

The CodeMie provider integrates with EPAM CodeMie Code Assistant endpoints. It authenticates with a CodeMie OpenID Connect token endpoint, obtains OAuth 2.0 bearer tokens, configures the delegated AI provider with the CodeMie API base URL, and routes supported model families to the appropriate implementation.

OpenAI-compatible, Gemini-compatible, and embedding model identifiers are delegated to the OpenAI provider configured for CodeMie endpoints. Claude-compatible model identifiers are delegated to the Anthropic provider. The provider supports password-grant and client-credentials authentication flows based on the supplied username or client identifier.

### Tools provider

The Tools provider exposes registered application functions for structured invocation. It collects prompts, registers `ToolFunction` callbacks, and executes those callbacks from YAML-based tool-call descriptions containing a tool name and parameter payload. This provider is useful for internal orchestration when host-defined tools need to be invoked through the same lifecycle as other AI providers.

## Common configuration parameters

| Parameter | Description | Default value |
| --- | --- | --- |
| `chatModel` | Model identifier used by the selected provider. Provider resolution commonly uses identifiers in the `Provider:Model` form, such as `OpenAI:gpt-4o-mini`. | Required |
| `OPENAI_API_KEY` | API key for OpenAI or OpenAI-compatible endpoints. CodeMie sets this to the retrieved OAuth 2.0 bearer token for delegated OpenAI-compatible requests. | Required for OpenAI-compatible providers |
| `OPENAI_BASE_URL` | Optional base URL override for OpenAI-compatible APIs. | OpenAI SDK default |
| `ANTHROPIC_API_KEY` | API key or authorization token for Anthropic Claude requests. | Required for Anthropic |
| `ANTHROPIC_BASE_URL` | Optional base URL override for Anthropic-compatible APIs. | Anthropic SDK default |
| `GENAI_TIMEOUT` | Request timeout in seconds. A value of `0` or an absent value leaves SDK defaults in effect. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum number of tokens the model may generate. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of tool calls the model may issue in a response loop. A value of `0` leaves the provider limit unset. | `0` |
| `logInputs` | Enables logging of provider request inputs when an input log file is configured by the caller. | Not enabled |
| `WebSearchTool.type` | Enables provider-specific web search when present. The value `default` maps to the provider default web-search tool type where supported. | Not set |
| `WebSearchTool.city` | Optional city hint for web-search user location. | Not set |
| `WebSearchTool.country` | Optional country hint for web-search user location. | Not set |
| `WebSearchTool.region` | Optional region hint for web-search user location. | Not set |
| `MCP.url` | URL for the first MCP server tool. Additional servers can be configured with numbered groups such as `MCP_1.url`, `MCP_2.url`, and so on. | Not set |
| `MCP.name` | Provider-visible MCP server name. Additional servers can use `MCP_1.name`, `MCP_2.name`, and so on. | Not set |
| `MCP.authorization` | Optional authorization value for the MCP server. Additional servers can use numbered variants. | Not set |
| `MCP.description` | Optional MCP server description. Additional servers can use numbered variants. | Not set |
| `cacheThreshold` | Anthropic-specific threshold for applying ephemeral prompt caching to large tool results. | Provider default |
| `GENAI_USERNAME` | Generic username used by provider authentication flows. CodeMie can use it for password-grant authentication. | Provider-specific |
| `GENAI_PASSWORD` | Generic password or secret used by provider authentication flows. CodeMie can use it as a password or client secret. | Provider-specific |
| `genai.serverId` | Optional GenAI server identifier used by shared provider configuration. | Not set |

## Resources

- [Machai GenAI Client site](https://machai.machanism.org/genai-client/index.html)
- [Machanism platform](https://machanism.org/)
- [Machai project documentation](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai.git)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
- [API documentation](https://machai.machanism.org/genai-client/apidocs/)
