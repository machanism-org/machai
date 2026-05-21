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

GenAI Client is a Java library for provider-agnostic integration with generative AI services in the MachAI ecosystem. It defines a common API for prompt execution, instructions, tool calling, embeddings, file-aware requests, provider initialization, and usage tracking so application code can work with multiple AI backends through a single abstraction.

The library centers on the `org.machanism.machai.ai` API and its subpackages:

- `org.machanism.machai.ai` provides the root abstractions for vendor-neutral AI interactions.
- `org.machanism.machai.ai.manager` resolves configured model identifiers into concrete provider implementations and aggregates token usage statistics.
- `org.machanism.machai.ai.provider` defines the shared provider contract, reusable base implementations, and adapter support.
- `org.machanism.machai.ai.provider.openai` integrates with the OpenAI Responses API, including embeddings, function tools, optional built-in web search, and MCP server tools.
- `org.machanism.machai.ai.provider.claude` integrates with Anthropic Claude models, including prompts, usage tracking, local function tools, and optional web search support.
- `org.machanism.machai.ai.provider.codemie` integrates with EPAM CodeMie by obtaining OAuth access tokens and delegating requests to OpenAI-compatible or Anthropic-compatible provider implementations based on the selected model.
- `org.machanism.machai.ai.tools` supports discovery and registration of host-side tool functions that can be exposed to compatible providers.

## Supported AI providers

### OpenAI

The OpenAI provider uses the OpenAI Java SDK and the Responses API to support:

- chat-style prompt execution
- system instructions
- embedding generation
- local function tool registration
- optional built-in web search
- optional MCP server registration
- usage tracking for input, cached input, and output tokens

Typical selection format through the provider manager:

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
| `MCP.url` / `MCP_1.url`... | Registers one or more MCP server tools. | Disabled |
| `MCP.label` / `MCP_1.label`... | Optional MCP server label. | None |
| `MCP.description` / `MCP_1.description`... | Optional MCP server description. | None |
| `MCP.authorization` / `MCP_1.authorization`... | Optional MCP server authorization value. | None |

### Claude

The Claude provider uses the Anthropic Java SDK to support:

- prompt execution against Claude models
- system instructions
- local function tool registration
- optional Anthropic web search tool variants
- usage tracking

Typical selection format through the provider manager:

- `Claude:claude-3-5-sonnet`
- `Claude:claude-3-opus-20240229`

Primary Claude-specific configuration:

| Parameter | Description | Default |
| --- | --- | --- |
| `ANTHROPIC_API_KEY` | API key used to authenticate Anthropic requests. | None |
| `ANTHROPIC_BASE_URL` | Optional base URL for Anthropic-compatible endpoints. | SDK default |
| `WebSearchTool.type` | Anthropic web search tool type. Supported values include `WebSearchTool20260209` and `WebSearchTool20250305`. | Disabled |
| `WebSearchTool.city` | Optional city for web search location. | None |
| `WebSearchTool.country` | Optional country for web search location. | None |
| `WebSearchTool.region` | Optional region for web search location. | None |

### CodeMie

The CodeMie provider authenticates against the EPAM CodeMie identity endpoint, retrieves an OAuth 2.0 access token, and delegates execution to a compatible downstream provider based on the configured `chatModel`.

Supported delegated model families in the current implementation:

- `gpt-*` and blank model values delegate to the OpenAI-compatible provider against the CodeMie base URL
- `gemini-*` values are handled through the OpenAI-compatible path in the current implementation
- `claude-*` values delegate to the Claude-compatible provider

Typical selection format through the provider manager:

- `CodeMie:gpt-4o-mini`
- `CodeMie:claude-3-5-sonnet`

Primary CodeMie-specific configuration:

| Parameter | Description | Default |
| --- | --- | --- |
| `GENAI_USERNAME` | User e-mail for password grant or client identifier for client-credentials grant. | None |
| `GENAI_PASSWORD` | Password or client secret used to obtain the OAuth token. | None |
| `AUTH_URL` | Optional override for the default CodeMie token endpoint. | `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token` |

## Common configuration parameters

These parameters are shared across the provider abstraction or base provider initialization logic.

| Parameter | Description | Default |
| --- | --- | --- |
| `chatModel` | Model name passed to the selected provider after provider resolution from `Provider:Model`. | None |
| `MAX_OUTPUT_TOKENS` | Maximum number of output tokens requested from the model. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of tool calls allowed in a response loop. `0` means unset. | `0` |
| `GENAI_TIMEOUT` | Request timeout in seconds for provider SDK clients. `0` means SDK defaults. | `0` |
| `logInputs` | Indicates whether provider inputs should be logged by the application workflow. | Provider/application-controlled |
| `genai.serverId` | Target GenAI server identifier used by the application configuration model. | None |

## Resources

- [MachAI platform](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai.git)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
