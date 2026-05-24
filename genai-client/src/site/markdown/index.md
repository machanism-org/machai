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

GenAI Client is a Java library that provides a vendor-neutral integration layer
between Machai modules and Generative AI providers. It defines the root API and
orchestration layer for working with large language model providers, including
provider resolution, request preparation, tool exposure, file-aware
interactions, and usage accounting. The library exposes a stable entry point for
application code while delegating implementation details to dedicated child
packages for concrete providers and supporting infrastructure.

The library standardizes how applications:

- Resolve a provider implementation from a configured model identifier such as
  `OpenAI:gpt-4o-mini` or `CodeMie:claude-3-5-sonnet`.
- Initialize provider clients from runtime configuration sources backed by the
  `Configurator` abstraction.
- Build prompts and system instructions, attach optional files, embeddings, and
  callable tools, and execute requests against the selected model.
- Discover and register host-defined function tools through Java's
  `ServiceLoader` mechanism, exposing controlled local capabilities (such as
  filesystem access, HTTP operations, or command execution) to the model in a
  structured manner.
- Enable optional built-in tools such as web search and Model Context Protocol
  (MCP) servers without provider-specific glue code.
- Collect provider-reported token usage metrics for monitoring, logging, and
  cost tracking.

The package is organized around several core areas:

- **Provider contracts** in `org.machanism.machai.ai.provider`, centered on the
  `Genai` interface and the `AbstractAIProvider` base class for text generation,
  embeddings, structured interactions, file handling, and usage reporting.
- **Provider resolution and lifecycle management** in
  `org.machanism.machai.ai.manager`, which maps model identifiers to
  implementations, initializes providers from configuration, and aggregates
  usage statistics.
- **Function tool integration** in `org.machanism.machai.ai.tools`, where
  application functions are discovered, described, and exposed to compatible
  providers for controlled tool calling.
- **Concrete provider implementations** in child packages below
  `org.machanism.machai.ai.provider`, including OpenAI-compatible integrations,
  Anthropic Claude, and EPAM CodeMie support.

# Supported AI providers

The following provider implementations are bundled with the library. Provider
identifiers passed to `GenaiProviderManager.getProvider(...)` use the form
`Provider:Model`. When the provider part is a simple name, the implementation
class is resolved by convention as
`org.machanism.machai.ai.provider.<provider-lowercase>.<Provider>Provider`. When
the provider part already contains a package separator, it is treated as a
fully qualified class name.

## OpenAI

Implementation: `org.machanism.machai.ai.provider.openai.OpenAIProvider`

Adapts the framework-level `Genai` contract to the OpenAI Responses and
Embeddings APIs through the OpenAI Java SDK. Supports text generation,
function tool-calling, file-assisted prompting, embedding generation, and
optional built-in tools such as web search and MCP servers.

| Parameter         | Description                                              | Default |
|-------------------|----------------------------------------------------------|---------|
| `OPENAI_BASE_URL` | Base URL for the OpenAI-compatible API endpoint.         | (none, required) |
| `OPENAI_API_KEY`  | API key used to authorize requests to the OpenAI API.    | (none, required) |
| `GENAI_TIMEOUT`   | Request timeout in seconds. `0` uses the SDK default.    | `0`     |

## Anthropic Claude

Implementation: `org.machanism.machai.ai.provider.claude.ClaudeProvider`

Executes requests against the Anthropic Claude API using the Anthropic Java
SDK. Supports prompt caching threshold tuning and shares the same provider
abstraction used by OpenAI integrations.

| Parameter            | Description                                                      | Default |
|----------------------|------------------------------------------------------------------|---------|
| `ANTHROPIC_BASE_URL` | Base URL for the Anthropic API endpoint.                         | SDK default |
| `ANTHROPIC_API_KEY`  | API key used to authorize requests to the Anthropic API.         | (none, required) |
| `cacheThreshold`     | Minimum content length (characters) eligible for prompt caching. | provider default |
| `GENAI_TIMEOUT`      | Request timeout in seconds. `0` uses the SDK default.            | `0`     |

## EPAM CodeMie

Implementation: `org.machanism.machai.ai.provider.codemie.CodeMieProvider`

Bridges CodeMie-specific authentication and endpoint configuration with the
shared provider abstraction. Resolves the appropriate OAuth 2.0 grant flow
(password or client credentials) from supplied credentials, obtains a bearer
token from the configured OpenID Connect token endpoint, and configures
delegated provider implementations for supported model families, including
OpenAI-compatible GPT models and Anthropic-compatible Claude models exposed
through the CodeMie platform.

| Parameter         | Description                                                                  | Default |
|-------------------|------------------------------------------------------------------------------|---------|
| `GENAI_USERNAME`  | Username (or client id) used for the configured OAuth 2.0 grant.             | (none, required) |
| `GENAI_PASSWORD`  | Password (or client secret) used for the configured OAuth 2.0 grant.         | (none, required) |
| `AUTH_URL`        | OpenID Connect token endpoint URL.                                           | `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token` |
| `GENAI_TIMEOUT`   | Request timeout in seconds. `0` uses the SDK default.                        | `0`     |

## Common configuration parameters

The following parameters are interpreted by `AbstractAIProvider` and apply to
all bundled provider implementations.

| Parameter                  | Description                                                              | Default |
|----------------------------|--------------------------------------------------------------------------|---------|
| `MAX_OUTPUT_TOKENS`        | Maximum number of tokens the model may generate per response.            | `18000` |
| `MAX_TOOL_CALLS`           | Maximum number of tool-call iterations permitted per response.           | `0` (unlimited) |
| `GENAI_TIMEOUT`            | Request timeout in seconds applied during client creation.               | `0` (SDK default) |
| `WebSearchTool.type`       | Provider-specific web-search tool type. Enables web search when set.     | (disabled) |
| `WebSearchTool.city`       | Optional user city used to localize web-search results.                  | (none) |
| `WebSearchTool.country`    | Optional user country used to localize web-search results.               | (none) |
| `WebSearchTool.region`     | Optional user region used to localize web-search results.                | (none) |
| `MCP.url`                  | Endpoint URL of the first MCP server.                                    | (none) |
| `MCP.name`                 | Provider-visible label of the first MCP server.                          | (none) |
| `MCP.authorization`        | Optional authorization token/value for the first MCP server.             | (none) |
| `MCP.description`          | Optional human-readable description of the first MCP server.             | (none) |
| `MCP_<n>.url`              | Endpoint URL of additional MCP servers (`MCP_1.*`, `MCP_2.*`, ...).      | (none) |
| `MCP_<n>.name`             | Provider-visible label of the additional MCP server.                     | (none) |
| `MCP_<n>.authorization`    | Optional authorization for the additional MCP server.                    | (none) |
| `MCP_<n>.description`      | Optional description for the additional MCP server.                      | (none) |
| `logInputs`                | Optional file path to which request inputs are written for logging.      | (disabled) |

# Resources

- [Machai platform documentation](https://machai.machanism.org/)
- [GenAI Client Javadoc](https://machai.machanism.org/genai-client/apidocs/)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
- [Machai parent project](https://machai.machanism.org/)
- [Configurator (Macha Core Commons)](https://macha.machanism.org/core/core-commons/configurator/apidocs/)
