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

GenAI Client is a Java library designed for provider-agnostic generative AI integration in the MachAI ecosystem. It provides a unified API for prompt execution, system instructions, tool calling, embeddings, provider initialization, usage tracking, and configuration-driven provider resolution so application code can work with multiple AI backends through a consistent abstraction.

The library is organized around the `org.machanism.machai.ai` API and its supporting packages:

- `org.machanism.machai.ai` defines the root abstractions for vendor-neutral generative AI interactions.
- `org.machanism.machai.ai.manager` resolves configured `Provider:Model` identifiers into concrete providers and aggregates token usage statistics.
- `org.machanism.machai.ai.provider` defines the shared provider contract, reusable base implementations, and adapter support used across concrete integrations.
- `org.machanism.machai.ai.provider.openai` integrates with the OpenAI Responses API, including prompt execution, embeddings, local function tools, optional built-in web search, optional MCP server tools, and usage tracking.
- `org.machanism.machai.ai.provider.claude` integrates with Anthropic Claude models, including prompt execution, system instructions, local function tools, optional web search, optional MCP server registration, and usage tracking.
- `org.machanism.machai.ai.provider.codemie` integrates with EPAM CodeMie by obtaining OAuth 2.0 access tokens and delegating requests to OpenAI-compatible or Anthropic-compatible provider implementations according to the configured model family.
- `org.machanism.machai.ai.tools` provides the service-provider infrastructure for discovering and registering host-side tool functions that can be exposed to compatible AI providers.

## Supported AI providers

### OpenAI

The OpenAI provider uses the OpenAI Java SDK and the Responses API to support:

- prompt execution with optional system instructions
- embedding generation
- local function tool registration with JSON-schema parameters
- optional built-in web search
- optional MCP server registration
- usage tracking for input, cached input, and output tokens

Typical provider manager identifiers include:

- `OpenAI:gpt-4o-mini`
- `OpenAI:gpt-4.1`

Primary OpenAI-specific configuration:

| Parameter | Description | Default |
| --- | --- | --- |
| `OPENAI_API_KEY` | API key or compatible bearer token used to authenticate requests. | None |
| `OPENAI_BASE_URL` | Optional base URL for OpenAI-compatible endpoints. | SDK default |
| `embedding.model` | Model used for embedding requests. | None |
| `WebSearchTool.type` | Enables the built-in OpenAI web search tool when set. | Disabled |
| `WebSearchTool.city` | Optional city for approximate web search user location. | None |
| `WebSearchTool.country` | Optional country for approximate web search user location. | None |
| `WebSearchTool.region` | Optional region for approximate web search user location. | None |
| `MCP.name` / `MCP_1.name`... | Registers one or more MCP server tools. | Disabled |
| `MCP.url` / `MCP_1.url`... | Optional MCP server name. | None |
| `MCP.description` / `MCP_1.description`... | Optional MCP server description. | None |
| `MCP.authorization` / `MCP_1.authorization`... | Optional MCP server authorization value. | None |

### Claude

The Claude provider uses the Anthropic Java SDK to support:

- prompt execution against Claude models
- system instructions
- local function tool registration
- optional Anthropic web search tool variants
- optional MCP server registration
- usage tracking for prompt, cache, and output tokens

Typical provider manager identifiers include:

- `Claude:claude-3-5-sonnet`
- `Claude:claude-3-opus-20240229`

Primary Claude-specific configuration:

| Parameter | Description | Default |
| --- | --- | --- |
| `ANTHROPIC_API_KEY` | API key used to authenticate Anthropic requests. | None |
| `ANTHROPIC_BASE_URL` | Optional base URL for Anthropic-compatible endpoints. | SDK default |
| `WebSearchTool.type` | Anthropic web search tool type. Supported values are `20260209` and `20250305`. | Disabled |
| `WebSearchTool.city` | Optional city for web search location. | None |
| `WebSearchTool.country` | Optional country for web search location. | None |
| `WebSearchTool.region` | Optional region for web search location. | None |
| `MCP.name` / `MCP_1.name`... | Registers one or more MCP server tools. | Disabled |
| `MCP.url` / `MCP_1.url`... | Optional MCP server name. | None |
| `MCP.authorization` / `MCP_1.authorization`... | Optional MCP server authorization token. | None |

### CodeMie

The CodeMie provider authenticates against the EPAM CodeMie identity endpoint, retrieves an OAuth 2.0 access token, and delegates execution to a compatible downstream provider based on the configured `chatModel`.

Supported delegated model families in the current implementation:

- `gpt-*` and blank model values delegate to the OpenAI-compatible provider against the CodeMie API base URL
- `gemini-*` values are currently handled through the OpenAI-compatible path
- `claude-*` values delegate to the Claude-compatible provider

Typical provider manager identifiers include:

- `CodeMie:gpt-4o-mini`
- `CodeMie:claude-3-5-sonnet`

Primary CodeMie-specific configuration:

| Parameter | Description | Default |
| --- | --- | --- |
| `GENAI_USERNAME` | User e-mail for password grant or client identifier for client-credentials grant. | None |
| `GENAI_PASSWORD` | Password or client secret used to obtain the OAuth token. | None |
| `AUTH_URL` | Optional override for the CodeMie token endpoint. | `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token` |

## Common configuration parameters

These parameters are shared across provider initialization or provider selection.

| Parameter | Description | Default |
| --- | --- | --- |
| `chatModel` | Model name passed to the selected provider after provider resolution from `Provider:Model`. | None |
| `MAX_OUTPUT_TOKENS` | Maximum number of output tokens requested from the model. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of tool calls allowed in a response loop. `0` means unset. | `0` |
| `GENAI_TIMEOUT` | Request timeout in seconds for provider SDK clients. `0` means SDK defaults. | `0` |
| `logInputs` | Indicates whether provider inputs should be logged by the application workflow. | Provider or application controlled |
| `genai.serverId` | Target GenAI server identifier used by the application configuration model. | None |

## Resources

- [MachAI platform](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai.git)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
