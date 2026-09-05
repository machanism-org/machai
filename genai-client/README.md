<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId])` and 
     [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/[artifactId]/bindex.json) in one line after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
- If used resources by uri: `src/site/resources/`, need to use project site location: `https://machai.machanism.org/[artifactId]/`.
-->

# GenAI Client

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/genai-client/bindex.json)

## Overview

GenAI Client is a Java library for integrating Machai applications with generative AI providers through a consistent provider abstraction. It manages provider resolution, prompts and system instructions, runtime configuration, optional embeddings, token-usage tracking, and registration of Java tools, prompts, and resources for AI-powered workflows.

Applications use the shared `Genai` lifecycle to resolve a configured provider, add prompts, instructions, tools, resources, web search, or MCP servers, and execute requests without directly depending on vendor SDKs. Immutable usage records can be aggregated by model for reporting and diagnostics.

The library includes OpenAI-compatible, Anthropic Claude, and EPAM CodeMie integrations, as well as local YAML-based tool execution and a disabled no-op provider. Its tool metadata layer discovers annotated Java methods through `ServiceLoader` and registers them as AI-callable functions or resource callbacks.

## Usage

Add GenAI Client to your Maven project, using the current released version from [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/genai-client):

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>genai-client</artifactId>
  <version>1.4.0</version>
</dependency>
```

Configure a provider through the application `Configurator`, then resolve its `Provider:Model` identifier and use the common lifecycle:

```properties
OPENAI_API_KEY=your-secret
GENAI_TIMEOUT=60
```

```java
Genai provider = GenaiProviderManager.getProvider(
    "OpenAI:gpt-4o-mini", getApplicationConfigurator());
provider.instructions("You are a concise assistant.");
provider.prompt("Summarize the project architecture.");
String response = provider.perform();
provider.clear();
```

For embeddings, resolve an embedding-capable model such as `OpenAI:text-embedding-3-small`. To expose host functionality, implement `FunctionTools`, annotate public methods with `@Tool` and `@Param`, and register discovered tools with `FunctionToolsLoader` before calling `perform()`.

## Supported providers

- **OpenAI** — Supports OpenAI-compatible Responses and Embeddings APIs, iterative function tools, web search, and MCP server tools. Configure `OPENAI_API_KEY`; set `OPENAI_BASE_URL` when using a compatible endpoint.
- **Anthropic** — Supports Claude Messages API requests, local function tools, web search, MCP forwarding, prompt caching for registered tools, and usage capture. Configure `ANTHROPIC_API_KEY`; `ANTHROPIC_BASE_URL` is optional.
- **CodeMie** — Authenticates with EPAM CodeMie and delegates supported GPT, Gemini, embedding, and Claude models to the appropriate provider. Configure `GENAI_USERNAME` and `GENAI_PASSWORD`; `AUTH_URL` can override the token endpoint.
- **Tools** — Invokes registered Java callbacks from YAML tool-call descriptions with `Tools:yaml`; no external credentials are required.
- **None** — A no-op provider for safe defaults and tests. Use `None:log` to emit lifecycle diagnostics.

## Resources

- [Project documentation](https://machai.machanism.org/genai-client/)
- [API documentation](https://machai.machanism.org/genai-client/apidocs/)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
- [Machanism platform](https://machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai)
