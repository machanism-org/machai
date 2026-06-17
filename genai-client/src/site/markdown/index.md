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

# Overview

GenAI Client is a Java library designed for seamless integration with generative AI providers in the MachAI ecosystem. It offers a vendor-neutral API for resolving providers from `Provider:Model` identifiers, initializing them from runtime configuration, preparing prompts and instructions, exposing host-defined tools, attaching file context, generating embeddings, and collecting usage metrics for monitoring and cost analysis.

The library is organized into several complementary areas:

- **Root AI API** in `org.machanism.machai.ai`, which defines the top-level integration model for provider-agnostic generative AI features.
- **Provider management** in `org.machanism.machai.ai.manager`, which resolves provider implementations, initializes them from configuration, and aggregates token-usage statistics.
- **Provider abstractions and shared infrastructure** in `org.machanism.machai.ai.provider`, centered on `Genai`, `EmbeddingProvider`, `GenaiAdapter`, and `AbstractAIProvider`.
- **Tool integration SPI** in `org.machanism.machai.ai.tools`, which discovers Java `ServiceLoader`-provided tool contributors and exposes function tools to compatible providers.
- **Provider-side tool contracts** in `org.machanism.machai.ai.provider.tools`, which define the supporting contracts used by providers that integrate tool-capable workflows.
- **Concrete provider implementations** in `org.machanism.machai.ai.provider.openai`, `org.machanism.machai.ai.provider.anthropic`, and `org.machanism.machai.ai.provider.codemie`.

Across these areas, the library standardizes how applications:

- Resolve providers such as `OpenAI:gpt-4o-mini`, `Anthropic:claude-3-5-sonnet`, or `CodeMie:gpt-4o-mini`.
- Apply configuration-driven initialization, including credentials, base URLs, request timeouts, token limits, and optional provider extensions.
- Build requests with system instructions, user prompts, optional file context, and registered function tools.
- Invoke local capabilities through structured tool-calling backed by Java methods.
- Enable optional provider-native features such as web search and Model Context Protocol (MCP) server integration.
- Generate embeddings for semantic search and similarity-driven workflows when supported by the selected provider.
- Capture token usage information for observability, reporting, and cost tracking.

# Supported AI providers

Provider identifiers passed to `GenaiProviderManager.getProvider(...)` use the form `Provider:Model`. When the provider segment is a simple name, the implementation class is resolved by convention as `org.machanism.machai.ai.provider.<provider-lowercase>.<Provider>Provider`. When the provider segment already contains a package separator, it is treated as a fully qualified class name.

## OpenAI

Implementation: `org.machanism.machai.ai.provider.openai.OpenAIProvider`

The OpenAI provider adapts the MachAI `Genai` abstraction to the OpenAI Java SDK. It supports conversational text generation, iterative function-tool execution, file-aware prompting, embedding generation, optional web search, and MCP server registration.

| Parameter | Description | Default |
|---|---|---|
| `OPENAI_API_KEY` | API key used to authorize requests to the OpenAI-compatible API. | (none, required) |
| `OPENAI_BASE_URL` | Base URL for the OpenAI-compatible API endpoint. | SDK default |
| `GENAI_TIMEOUT` | Request timeout in seconds. `0` uses the SDK default timeout settings. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum number of output tokens the model may generate. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of iterative tool-call rounds permitted. `0` leaves the limit unset. | `0` |
| `WebSearchTool.type` | Enables the provider web-search tool and selects its type or version. | (disabled) |
| `WebSearchTool.city` | Optional city used to localize web-search results. | (none) |
| `WebSearchTool.country` | Optional country used to localize web-search results. | (none) |
| `WebSearchTool.region` | Optional region used to localize web-search results. | (none) |
| `MCP.url` / `MCP_<n>.url` | URL of an MCP server exposed to the model. | (none) |
| `MCP.name` / `MCP_<n>.name` | Provider-visible name of the MCP server. | (none) |
| `MCP.authorization` / `MCP_<n>.authorization` | Optional authorization value for the MCP server. | (none) |
| `MCP.description` / `MCP_<n>.description` | Optional human-readable description of the MCP server. | (none) |

## Anthropic Claude

Implementation: `org.machanism.machai.ai.provider.anthropic.AnthropicProvider`

The Anthropic provider adapts the Anthropic Java SDK to the shared MachAI provider abstraction. It supports prompt execution, host-defined tools, optional web search, MCP server definitions, request logging, and usage tracking. It also supports prompt-cache threshold tuning for large tool results.

| Parameter | Description | Default |
|---|---|---|
| `ANTHROPIC_API_KEY` | API key or auth token used to authorize requests to the Anthropic API. | (none, required) |
| `ANTHROPIC_BASE_URL` | Base URL for the Anthropic-compatible endpoint. | SDK default |
| `GENAI_TIMEOUT` | Request timeout in seconds. `0` uses the SDK default timeout settings. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum number of output tokens the model may generate. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of tool calls permitted by the shared provider abstraction. | `0` |
| `cacheThreshold` | Minimum tool-result length before ephemeral cache control is applied. | (none) |
| `WebSearchTool.type` | Claude web-search tool type. Supported values are `20260209`, `20250305`, or `default`. | (disabled) |
| `WebSearchTool.city` | Optional city used to localize web-search results. | (none) |
| `WebSearchTool.country` | Optional country used to localize web-search results. | (none) |
| `WebSearchTool.region` | Optional region used to localize web-search results. | (none) |
| `MCP.url` / `MCP_<n>.url` | URL of an MCP server exposed to Claude. | (none) |
| `MCP.name` / `MCP_<n>.name` | Provider-visible MCP server name. | (none) |
| `MCP.authorization` / `MCP_<n>.authorization` | Optional bearer token or authorization value for the MCP server. | (none) |
| `MCP.description` / `MCP_<n>.description` | Optional MCP server description. | (none) |

## EPAM CodeMie

Implementation: `org.machanism.machai.ai.provider.codemie.CodeMieProvider`

The CodeMie provider authenticates against a CodeMie OpenID Connect token endpoint, obtains an OAuth 2.0 bearer token, and delegates requests to the appropriate downstream provider based on the configured model family. OpenAI-compatible and embedding-capable models are routed through `OpenAIProvider`, while Claude models are routed through `AnthropicProvider`.

| Parameter | Description | Default |
|---|---|---|
| `GENAI_USERNAME` | Username or client id used for CodeMie authentication. | (none, required) |
| `GENAI_PASSWORD` | Password or client secret used for CodeMie authentication. | (none, required) |
| `AUTH_URL` | OpenID Connect token endpoint used to obtain access tokens. | `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token` |
| `GENAI_TIMEOUT` | Request timeout in seconds propagated to the delegated provider client. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum number of output tokens enforced by the delegated provider. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of tool-call rounds enforced by the delegated provider. | `0` |
| `cacheThreshold` | Claude-only delegated cache threshold when a Claude model is selected. | (none) |

Supported model families include:

- `gpt-*`
- `gemini-*`
- `text-embedding-*`
- `codemie-text-embedding-*`
- `amazon.titan-embed-text-*`
- `claude-*`

## Common configuration parameters

The following parameters are shared across the provider abstraction or are commonly used by multiple bundled providers.

| Parameter | Description | Default |
|---|---|---|
| `MAX_OUTPUT_TOKENS` | Maximum number of output tokens generated by a provider response. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of iterative tool calls allowed during a single response flow. | `0` |
| `GENAI_TIMEOUT` | Timeout in seconds used when constructing provider SDK clients. | `0` |
| `logInputs` | Optional file path used to write request inputs for debugging and audit purposes. | (disabled) |
| `WebSearchTool.type` | Enables provider-specific web search when supported by the selected provider. | (disabled) |
| `WebSearchTool.city` | Optional city used to localize provider web-search requests. | (none) |
| `WebSearchTool.country` | Optional country used to localize provider web-search requests. | (none) |
| `WebSearchTool.region` | Optional region used to localize provider web-search requests. | (none) |
| `MCP.url` | URL of the first configured MCP server. | (none) |
| `MCP.name` | Provider-visible name of the first configured MCP server. | (none) |
| `MCP.authorization` | Optional authorization value for the first configured MCP server. | (none) |
| `MCP.description` | Optional description of the first configured MCP server. | (none) |
| `MCP_<n>.url` | URL of an additional numbered MCP server. | (none) |
| `MCP_<n>.name` | Provider-visible name of an additional numbered MCP server. | (none) |
| `MCP_<n>.authorization` | Optional authorization value for an additional numbered MCP server. | (none) |
| `MCP_<n>.description` | Optional description of an additional numbered MCP server. | (none) |

# Resources

- [MachAI platform documentation](https://machai.machanism.org/)
- [GenAI Client Javadoc](https://machai.machanism.org/genai-client/apidocs/)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
