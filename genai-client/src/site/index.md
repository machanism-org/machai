# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client is a Java library that provides a provider-agnostic API for running generative AI tasks through a single `GenAIProvider` interface.

It helps keep application code stable while you swap or combine different backends (API-based providers and UI/web-automation providers) through a unified programming model.

## Key features

- Provider selection using a single identifier in the form `Provider:Model`, resolved by `GenAIProviderManager`.
- Prompt composition from plain text or from files (`promptFile(...)`).
- Optional attachment of local or remote files (`addFile(File|URL)`) (provider-dependent).
- Embeddings support (`embedding(...)`) (provider-dependent).
- Tool/function calling support (`addTool(...)`) (provider-dependent).
- Working directory support (`setWorkingDir(...)`) and optional request input logging (`inputsLog(...)`).

## Getting started

The typical entry point is `GenAIProviderManager`, which resolves a provider implementation by a `Provider:Model` string.

```java
Configurator conf = ...;
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1", conf);
provider.model("gpt-5.1");
provider.instructions("You are a helpful assistant.");
provider.prompt("Summarize the following text...");
String answer = provider.perform();
provider.close();
```

If the provider part is omitted or blank, the manager falls back to the `None` provider:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("gpt-5.1", conf); // uses None provider
```

## Supported GenAI Providers
<!-- 
@guidance: 
IMPORTANT: Update this section! Use from javadoc of classes from source folder which extend the GenAI interface 
and generate the content for this section following net format:
### [PROVIDER_NAME]
... FULL DESCRIPTION ...
-->

### Claude

Anthropic-backed implementation of MachAI's `Genai` abstraction.

This provider is not implemented yet. All core operations (`init(...)`, `prompt(...)`, `addFile(...)`, `perform()`, `clear()`, `addTool(...)`, `instructions(...)`, `inputsLog(...)`, `setWorkingDir(...)`, and `usage()`) currently throw `UnsupportedOperationException` with the message `"ClaudeProvider is not implemented yet."`.

The `embedding(String,long)` method currently returns an empty list.

### CodeMie

`Genai` implementation that integrates with EPAM CodeMie.

This provider authenticates against a CodeMie OpenID Connect (OIDC) token endpoint to obtain an OAuth 2.0 access token and then configures an OpenAI-compatible backend (CodeMie Code Assistant REST API).

Authentication modes

- Password grant is used when `GENAI_USERNAME` contains `"@"` (typical e-mail login).
- Client credentials is used otherwise (service-to-service).

Provider delegation

After retrieving a token, this provider sets the following configuration keys before delegating to a downstream provider:

- `OPENAI_BASE_URL` to `https://codemie.lab.epam.com/code-assistant-api/v1`
- `OPENAI_API_KEY` to the retrieved access token

Delegation is selected based on the configured `chatModel` prefix:

- `gpt-*` (or blank/unspecified) models delegate to OpenAI.
- `gemini-*` models delegate to Gemini.
- `claude-*` models delegate to Claude.

Configuration

Required configuration keys:

- `GENAI_USERNAME` – user e-mail or client id.
- `GENAI_PASSWORD` – password or client secret.
- `chatModel` – model identifier (for example `gpt-4o-mini`, `gemini-1.5-pro`, `claude-3-5-sonnet`).

Optional configuration keys:

- `AUTH_URL` – token endpoint override.

### Gemini

MachAI `Genai` implementation for Google's Gemini models.

This provider adapts MachAI's provider-agnostic abstractions (prompts, tool definitions, files/attachments, and usage reporting) to Gemini's API.

Status

The current implementation is a placeholder. Most operations are not yet implemented and will be completed in a future iteration. In particular, `init(Configurator)`, `perform()`, and `embedding(...)` throw `NotImplementedException`.

### None

No-op implementation of `Genai`.

This provider is intended for environments where no external LLM integration should be used. It accumulates prompt text in memory and can optionally write instructions and prompts to local files when `inputsLog(File)` has been configured.

Key characteristics

- No network calls are performed.
- `perform()` always returns `null`.
- Unsupported capabilities (for example, `embedding(String,long)`) throw `UnsupportedOperationException`.
- Tool registration is ignored (no-op).
- File attachments are ignored (no-op).

### OpenAI

OpenAI-backed `Genai` implementation.

This provider adapts the MachAI provider abstraction to the OpenAI Java SDK Responses API. It supports prompting, file inputs, tool/function calling, embedding generation, and usage reporting.

Configuration

Configuration values are read from the `Configurator` passed to `init(Configurator)`. The following keys are used:

- `chatModel` (required): model identifier passed to the OpenAI Responses API (for example, `gpt-4.1` or `gpt-4o`).
- `OPENAI_API_KEY` (required): API key used to authenticate with the OpenAI API.
- `OPENAI_BASE_URL` (optional): base URL for OpenAI-compatible endpoints. If unset, the SDK default base URL is used.
- `GENAI_TIMEOUT` (optional): request timeout in seconds. If missing, `0`, or negative, the SDK default timeouts are used. Defaults to `600` seconds.
- `MAX_OUTPUT_TOKENS` (optional): maximum number of output tokens. Defaults to `300000`.
- `MAX_TOOL_CALLS` (optional): maximum number of tool calls allowed in a single response. Defaults to `200`.

Capabilities

- Submit prompts and retrieve text responses.
- Upload local files or attach files by URL for use in a request.
- Register function tools and dispatch tool calls to application handlers.
- Create vector embeddings for input text.
- Report token usage to `GenAIProviderManager`.

Thread-safety

Instances are not thread-safe.

<!-- @guidance:
- Follow the rules described in @guidance tags and do not change or delete `@guidance` related tags in processing.
- Update the file follow to described @guidance in markdown html comments marked as 
  `@guidance` in the format `<!-- @guidance: ...RULES... -->`. 
-->