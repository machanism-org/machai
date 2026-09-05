<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId])` after the title as a new paragraph.
   - Add [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/[artifactId]/bindex.json)
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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)

[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/genai-client/bindex.json)

## Overview

GenAI Client is a Java library for integrating Machai applications with generative AI providers through a consistent, provider-neutral API. It resolves configured providers, manages prompts and system instructions, supports embeddings and token-usage tracking, and registers Java tools, prompts, and resources for AI-powered workflows.

It provides integrations for OpenAI-compatible APIs, Anthropic Claude, and EPAM CodeMie, alongside local YAML-driven tool execution and a no-op provider. Applications can use the common `Genai` lifecycle to add prompts, tools, web search, and MCP servers without directly depending on vendor-specific SDKs.

## Usage

Add the dependency to your Maven project. Use the current released version from [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/genai-client).

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>genai-client</artifactId>
  <version>1.4.0</version>
</dependency>
```

Configure the selected provider through your application `Configurator`, then resolve it by its `Provider:Model` identifier and use the shared lifecycle:

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

For embeddings, resolve an embedding-capable model such as `OpenAI:text-embedding-3-small`. To expose host functionality to a model, implement `FunctionTools`, annotate public methods with `@Tool` and `@Param`, and register discovered tools with `FunctionToolsLoader` before calling `perform()`.

## Supported providers

- **OpenAI** — OpenAI-compatible Responses and Embeddings APIs, function tools, web search, and MCP server tools. Configure `OPENAI_API_KEY`; optionally set `OPENAI_BASE_URL` for a compatible endpoint.
- **Anthropic** — Claude Messages API with local function tools, web search, MCP forwarding, and usage capture. Configure `ANTHROPIC_API_KEY` and optionally `ANTHROPIC_BASE_URL`.
- **CodeMie** — EPAM CodeMie endpoint integration that delegates supported GPT, Gemini, embedding, and Claude models after OAuth authentication. Configure `GENAI_USERNAME` and `GENAI_PASSWORD`.
- **Tools** — Local YAML-based invocation of registered Java callbacks with `Tools:yaml`; no external credentials are required.
- **None** — Disabled no-op provider for safe defaults and tests; use `None:log` for lifecycle diagnostics.

## Resources

- [Project documentation](https://machai.machanism.org/genai-client/)
- [API documentation](https://machai.machanism.org/genai-client/apidocs/)
- [Machanism platform](https://machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai)
