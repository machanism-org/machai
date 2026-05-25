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

GenAI Client is a Java library designed for seamless integration with Generative AI providers across the Machai ecosystem. It provides a vendor-neutral API and orchestration layer for building AI-enabled features while isolating provider-specific SDK details behind a consistent application-facing contract.

The library standardizes how applications:

- Resolve a provider implementation from model identifiers such as `OpenAI:gpt-4o-mini`, `Claude:claude-3-5-sonnet`, or `CodeMie:gpt-4o-mini`.
- Initialize providers from runtime configuration using the `Configurator` abstraction.
- Build requests with system instructions, user prompts, optional files, and host-defined tools.
- Expose controlled local capabilities through Java `ServiceLoader`-discovered function tools.
- Use optional built-in features such as web search and Model Context Protocol (MCP) server integration.
- Generate embeddings for semantic and similarity-driven workflows when supported by the selected provider.
- Capture token usage information for monitoring, reporting, and cost analysis.

The source tree is organized around several core areas:

- **Root AI abstractions** in `org.machanism.machai.ai`, which define the overall package scope and the top-level integration model.
- **Provider management** in `org.machanism.machai.ai.manager`, which resolves provider implementations, initializes them from `Provider:Model` identifiers, and aggregates token-usage statistics.
- **Provider contracts and shared infrastructure** in `org.machanism.machai.ai.provider`, centered on `Genai`, `EmbeddingProvider`, `GenaiAdapter`, and `AbstractAIProvider`.
- **Tool integration** in `org.machanism.machai.ai.tools`, which loads host-defined function tools and applies them to compatible providers.
- **Concrete provider implementations** in child packages for OpenAI, Anthropic Claude, and EPAM CodeMie.

# Supported AI providers

Provider identifiers passed to `GenaiProviderManager.getProvider(...)` use the form `Provider:Model`. When the provider part is a simple name, the implementation class is resolved by convention as `org.machanism.machai.ai.provider.<provider-lowercase>.<Provider>Provider`. When the provider part already contains a package separator, it is treated as a fully qualified class name.

## OpenAI

Implementation: `org.machanism.machai.ai.provider.openai.OpenAIProvider`

The OpenAI provider adapts the Machai `Genai` contract to the OpenAI Java SDK. It supports text generation, iterative function tool-calling, file-aware prompting, embedding generation, optional web search, and MCP server registration.

| Parameter | Description | Default |
|---|---|---|
| `OPENAI_API_KEY` | API key used to authorize requests to the OpenAI-compatible API. | (none, required) |
| `OPENAI_BASE_URL` | Base URL for the OpenAI-compatible API endpoint. | SDK default |
| `GENAI_TIMEOUT` | Request timeout in seconds. `0` uses the SDK default timeout settings. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum number of output tokens the model may generate. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of iterative tool-call rounds permitted. `0` leaves the limit unset. | `0` |
| `WebSearchTool.type` | Enables the provider web-search tool and selects its type/version. | (disabled) |
| `WebSearchTool.city` | Optional city used to localize web-search results. | (none) |
| `WebSearchTool.country` | Optional country used to localize web-search results. | (none) |
| `WebSearchTool.region` | Optional region used to localize web-search results. | (none) |
| `MCP.url` / `MCP_<n>.url` | URL of an MCP server exposed to the model. | (none) |
| `MCP.name` / `MCP_<n>.name` | Provider-visible label of the MCP server. | (none) |
| `MCP.authorization` / `MCP_<n>.authorization` | Optional authorization value for the MCP server. | (none) |
| `MCP.description` / `MCP_<n>.description` | Optional human-readable description of the MCP server. | (none) |

## Anthropic Claude

Implementation: `org.machanism.machai.ai.provider.claude.ClaudeProvider`

The Claude provider adapts the Anthropic Java SDK to the shared Machai provider abstraction. It supports prompt execution, host-defined tools, optional web search, MCP server definitions, request logging, and usage tracking. It also supports prompt-cache threshold tuning for large tool results.

| Parameter | Description | Default |
|---|---|---|
| `ANTHROPIC_API_KEY` | API key used to authorize requests to the Anthropic API. | (none, required) |
| `ANTHROPIC_BASE_URL` | Base URL for the Anthropic-compatible endpoint. | SDK default |
| `GENAI_TIMEOUT` | Request timeout in seconds. `0` uses the SDK default timeout settings. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum number of output tokens the model may generate. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of tool calls permitted by the shared provider abstraction. | `0` |
| `cacheThreshold` | Minimum tool-result length before ephemeral cache control is applied. | `10240` |
| `WebSearchTool.type` | Claude web-search tool type. Supported values are `20260209`, `20250305`, or `default`. | (disabled) |
| `WebSearchTool.city` | Optional city used to localize web-search results. | (none) |
| `WebSearchTool.country` | Optional country used to localize web-search results. | (none) |
| `WebSearchTool.region` | Optional region used to localize web-search results. | (none) |
| `MCP.url` / `MCP_<n>.url` | URL of an MCP server exposed to Claude. | (none) |
| `MCP.name` / `MCP_<n>.name` | Provider-visible MCP server name. | (none) |
| `MCP.authorization` / `MCP_<n>.authorization` | Optional bearer token for the MCP server. | (none) |
| `MCP.description` / `MCP_<n>.description` | Optional MCP server description. | (none) |

## EPAM CodeMie

Implementation: `org.machanism.machai.ai.provider.codemie.CodeMieProvider`

The CodeMie provider authenticates against a CodeMie OpenID Connect token endpoint, obtains an OAuth 2.0 bearer token, and delegates requests to the appropriate downstream provider based on the configured model family. OpenAI-compatible and embedding-capable models are routed through `OpenAIProvider`, while Claude models are routed through `ClaudeProvider`.

| Parameter | Description | Default |
|---|---|---|
| `GENAI_USERNAME` | Username or client id used for CodeMie authentication. | (none, required) |
| `GENAI_PASSWORD` | Password or client secret used for CodeMie authentication. | (none, required) |
| `AUTH_URL` | OpenID Connect token endpoint used to obtain access tokens. | `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token` |
| `GENAI_TIMEOUT` | Request timeout in seconds propagated to the delegated provider client. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum number of output tokens enforced by the delegated provider. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of tool-call rounds enforced by the delegated provider. | `0` |
| `cacheThreshold` | Claude-only delegated cache threshold when a Claude model is selected. | `10240` |

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

- [Machai platform documentation](https://machai.machanism.org/)
- [GenAI Client Javadoc](https://machai.machanism.org/genai-client/apidocs/)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
