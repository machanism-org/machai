<!-- @guidance: >>> ${guidances}/readme-content.md -->

# GenAI Client

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/genai-client/refs/heads/main/bindex.json)

GenAI Client is a Java library for integrating Machai applications with generative AI providers through a consistent provider abstraction. It supports prompt and instruction handling, provider resolution, optional embeddings, usage tracking, and registration of Java functions, prompts, and resources for AI-powered workflows.

## Project Structure

The library exposes a common lifecycle for submitting prompts, instructions, project context, and local capabilities to a selected AI provider. A provider manager resolves `Provider:Model` identifiers and initializes the appropriate implementation, while shared provider behavior manages configuration, local callback discovery and invocation, web-search support, MCP server setup, argument conversion, and error handling.

OpenAI-compatible and Anthropic implementations execute remote model requests and record token usage; the CodeMie implementation obtains OAuth 2.0 tokens and delegates to the compatible implementation selected by the model family. A local-tools implementation executes registered callbacks from YAML tool-call descriptions, and a no-op implementation provides a safe disabled mode. Service-loaded tool metadata describes Java tools, prompts, resources, parameters, and supported applications, allowing host applications to expose capabilities without coupling directly to a vendor SDK.

## Introduction

Applications interact with the common GenAI contract rather than provider-specific SDKs. Resolve a configured provider, attach prompts or system instructions, register tools and resources when needed, and execute requests through the same lifecycle. The library can also generate embeddings where supported and aggregate captured token usage by model for reporting and diagnostics.

Supported providers include:

- **OpenAI** for Responses API and embedding requests, iterative function tools, web search, and MCP tools.
- **Anthropic** for Claude messages, system instructions, function tools, web search, and MCP definitions.
- **CodeMie** for CodeMie Code Assistant endpoints, delegating supported OpenAI-compatible or Anthropic model families after OAuth 2.0 authentication.
- **Tools** for host-side execution of registered callbacks described by YAML.
- **None** for disabled processing and tests without external requests.

## Usage

Configure a provider using a `Provider:Model` identifier and the credentials required by that provider. For example, an OpenAI integration commonly uses `OpenAI:gpt-4o-mini` with `OPENAI_API_KEY`; an Anthropic integration uses `Anthropic:claude-3-5-sonnet` with `ANTHROPIC_API_KEY`. CodeMie uses identifiers such as `CodeMie:gpt-4o-mini` or `CodeMie:claude-3-5-sonnet` together with `GENAI_USERNAME` and `GENAI_PASSWORD`. Use `Tools:yaml` to invoke locally registered callbacks, or `None:disabled` when requests must be suppressed.

```java
Genai genai = GenaiProviderManager.getGenai("OpenAI:gpt-4o-mini");
// Add prompts, instructions, tools, or resources as required, then execute the request.
```

Common optional settings include `OPENAI_BASE_URL` or `ANTHROPIC_BASE_URL` for compatible endpoints, `GENAI_TIMEOUT` for request timeout control, `MAX_OUTPUT_TOKENS` for response limits, `MAX_TOOL_CALLS` for OpenAI tool-call loops, `WebSearchTool.*` for location-aware web search, and `MCP*` groups for MCP servers. CodeMie may use `AUTH_URL` to override its token endpoint. Refer to the project documentation for the complete configuration contract and provider-specific behavior.

## Resources

- [Machai GenAI Client site](https://machai.machanism.org/genai-client/index.html)
- [Machanism platform](https://machanism.org/)
- [Machai project documentation](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai.git)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
- [API documentation](https://machai.machanism.org/genai-client/apidocs/)
