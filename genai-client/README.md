<!-- @guidance: >>> ${guidances}/readme-content.md -->

# GenAI Client

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/genai-client/refs/heads/main/bindex.json)

GenAI Client is a Java library for integrating Machai applications with generative AI providers through a consistent provider abstraction. It provides provider resolution, prompt and instruction handling, runtime configuration, optional embedding generation, token-usage tracking, and registration of Java tools, prompts, and resources for AI-powered workflows.

## Project Structure

A host application requests a configured `Provider:Model` implementation from `GenaiProviderManager`, then uses the common `Genai` contract to submit prompts, register local capabilities, execute requests, and, where supported, generate embeddings. The manager initializes the selected provider; shared provider infrastructure handles configuration, local callback discovery and invocation, web-search and MCP setup, argument conversion, and error handling.

Concrete provider components integrate OpenAI-compatible Responses and Embeddings APIs, Anthropic Messages APIs, and CodeMie delegation. The CodeMie component obtains an OAuth 2.0 access token and routes supported model families to the appropriate compatible provider. Local YAML-described callbacks and a safe no-op provider support host-side execution and disabled environments without an external model request.

A metadata and service-loading layer discovers compatible Java tools, prompts, and resources, builds parameter schemas, and registers callbacks with providers. Usage components capture and aggregate token consumption by model. Optional MCP servers are registered or forwarded to the OpenAI and Anthropic integrations.

At the component level, a host application supplies configuration, resolves an initialized provider, and interacts through the common generation and embedding contracts. Shared provider behavior coordinates local capability discovery, callback argument conversion, web-search and MCP configuration, and error handling. OpenAI and Anthropic implementations call their respective external APIs, while CodeMie obtains an OAuth 2.0 token and configures a delegated compatible client. The project-structure illustration is omitted because no generated diagram image is available.

## Introduction

Applications follow the same lifecycle regardless of provider: resolve a configured provider, attach prompts or system instructions, register tools and resources as needed, then execute the request. Java methods exposed through the function-tool metadata can be discovered with `ServiceLoader` and made available as AI-callable functions or resource callbacks.

This supports semantic search, automated content generation, intelligent project assembly, structured tool execution, local tool orchestration, and provider-independent prompt workflows. Token usage is captured in immutable records and can be aggregated by model for reporting and diagnostics.

### Supported providers

#### OpenAI

The OpenAI provider uses the OpenAI Java SDK Responses and Embeddings APIs. It supports conversational generation, iterative local function-tool calls, optional web search, MCP tools, embeddings, input logging, and token-usage capture. Configure it with an identifier such as `OpenAI:gpt-4o-mini` or `OpenAI:text-embedding-3-small`, `OPENAI_API_KEY`, and optionally `OPENAI_BASE_URL`, `GENAI_TIMEOUT`, `MAX_OUTPUT_TOKENS`, `MAX_TOOL_CALLS`, `WebSearchTool.*`, and `MCP*` settings.

#### Anthropic

The Anthropic provider uses Anthropic Claude Messages APIs. It supports system instructions, local function-tool loops, web search, MCP definitions, and token-usage capture. Configure it with an identifier such as `Anthropic:claude-3-5-sonnet`, `ANTHROPIC_API_KEY`, and optionally `ANTHROPIC_BASE_URL`, `GENAI_TIMEOUT`, `MAX_OUTPUT_TOKENS`, `WebSearchTool.*`, and `MCP*` settings. `MAX_TOOL_CALLS` applies to OpenAI Responses API loops and does not limit Anthropic tool-use loops.

#### CodeMie

The CodeMie provider authenticates with a CodeMie OpenID Connect token endpoint, obtains an OAuth 2.0 bearer token, supplies the compatible provider base URL and token, and delegates supported model families. Models beginning with `gpt-`, `gemini-`, `text-embedding-`, `codemie-text-embedding-`, or `amazon.titan-embed-text-` use the OpenAI-compatible implementation; models beginning with `claude-` use the Anthropic implementation.

Use an identifier such as `CodeMie:gpt-4o-mini` or `CodeMie:claude-3-5-sonnet` with `GENAI_USERNAME` and `GENAI_PASSWORD`. A username containing `@` selects password-grant authentication; another username selects client-credentials authentication. `AUTH_URL` optionally overrides the token endpoint.

#### Tools

The Tools provider executes a registered host-side `ToolFunction` callback from a YAML tool-call description containing a tool name and parameter payload. Use `Tools:yaml`, register local callbacks before `perform()`, and supply the final prompt as the YAML descriptor. It requires no external credentials or endpoint configuration.

#### None

The None provider is a disabled no-op implementation for safe defaults and tests. It accepts the standard lifecycle calls, discards submitted state, and returns `null` from execution. Use `None:log` to emit lifecycle activity at INFO level, or a model value such as `None:disabled` for silent operation.

## Usage

Configure a provider with a `Provider:Model` identifier and the credentials appropriate to that provider. For example, configure OpenAI with `OpenAI:gpt-4o-mini` and `OPENAI_API_KEY`.

```java
Genai genai = GenaiProviderManager.getGenai("OpenAI:gpt-4o-mini");
// Add prompts, instructions, tools, or resources as required, then execute the request.
```

Use the same lifecycle for other providers. Register compatible Java tools, prompts, and resources when the request requires local capabilities; providers expose them to the model as supported functions or resource callbacks.

### Common configuration

| Parameter | Description | Default |
| --- | --- | --- |
| Provider/model identifier | Provider and model in `Provider:Model` form, for example `OpenAI:gpt-4o-mini`. | Required |
| `OPENAI_API_KEY` | API key for OpenAI or OpenAI-compatible requests; CodeMie supplies a bearer token for delegated requests. | Required for OpenAI-compatible providers |
| `OPENAI_BASE_URL` | Base-URL override for an OpenAI-compatible endpoint. | OpenAI SDK default |
| `ANTHROPIC_API_KEY` | API key or authorization token for Anthropic requests. | Required for Anthropic |
| `ANTHROPIC_BASE_URL` | Base-URL override for an Anthropic-compatible endpoint. | Anthropic SDK default |
| `GENAI_TIMEOUT` | Request timeout in seconds; `0` or no value retains SDK behavior. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum generated tokens. | `18000` |
| `MAX_TOOL_CALLS` | Maximum OpenAI Responses API tool calls in a response loop; it does not limit Anthropic tool-use loops. | `0` (unset) |
| `WebSearchTool.type` | Enables provider-specific web search when present; `default` uses the provider's default supported type. | Not set |
| `WebSearchTool.city`, `WebSearchTool.country`, `WebSearchTool.region` | Optional user-location hints for web search. | Not set |
| `MCP.url` | URL for the first MCP server; numbered groups, such as `MCP_1.url`, configure additional servers. | Not set |
| `MCP.name` | Provider-visible MCP server name; a configured group requires a name to be registered. | Not set |
| `MCP.authorization` | Optional authorization value for an MCP server. | Not set |
| `MCP.description` | Optional MCP server description. | Not set |
| `GENAI_USERNAME` / `GENAI_PASSWORD` | CodeMie credentials for password-grant or client-credentials authentication. | Provider-specific |
| `AUTH_URL` | CodeMie token-endpoint override. | CodeMie default endpoint |

## Resources

- [Machai GenAI Client site](https://machai.machanism.org/genai-client/index.html)
- [Machanism platform](https://machanism.org/)
- [Machai project documentation](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai.git)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
- [API documentation](https://machai.machanism.org/genai-client/apidocs/)
