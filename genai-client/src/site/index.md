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

Anthropic-backed implementation of MachAI's `GenAIProvider` abstraction.

This provider adapts the Anthropic Java SDK to MachAI's provider interface.

Status

The current implementation is a placeholder: `init(Configurator)` throws `NotImplementedException`, and most operations are stubs.

Thread-safety

Instances are not thread-safe.

### CodeMie

GenAI provider implementation for EPAM CodeMie.

This provider obtains an access token from a configurable OpenID Connect token endpoint and then initializes an OpenAI-compatible client (via `OpenAIProvider`) to call the CodeMie Code Assistant REST API.

Authentication modes

The authentication mode is selected based on the configured username:

- If the username contains `"@"`, the password grant is used (typical user e-mail login).
- Otherwise, the client credentials grant is used (service-to-service).

Delegation

After a token is retrieved, this provider configures the underlying OpenAI-compatible provider by setting:

- `OPENAI_BASE_URL` to `https://codemie.lab.epam.com/code-assistant-api/v1`
- `OPENAI_API_KEY` to the retrieved access token

It then delegates requests based on the configured model prefix:

- `gpt-*` (or blank model) → `OpenAIProvider`
- `gemini-*` → `GeminiProvider`
- `claude-*` → `ClaudeProvider`

Configuration

Required configuration keys:

- `GENAI_USERNAME` – user e-mail (password grant) or client id (client credentials grant).
- `GENAI_PASSWORD` – password (password grant) or client secret (client credentials grant).
- `chatModel` – model identifier (for example `gpt-4o-mini` or `claude-3-5-sonnet`).

Optional configuration keys:

- `AUTH_URL` – token endpoint override.

Built-in endpoints

- Token URL (default): `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token`
- Base URL: `https://codemie.lab.epam.com/code-assistant-api/v1`

### Gemini

MachAI `GenAIProvider` implementation for Google's Gemini models.

This provider adapts MachAI's provider-agnostic abstractions (prompts, tool definitions, files/attachments, and usage reporting) to Gemini's API.

Status

The current implementation is a placeholder. Most operations are not yet implemented and will be completed in a future iteration. `init(Configurator)`, `perform()`, and `embedding(...)` currently throw `NotImplementedException`.

Thread-safety

Instances are not thread-safe.

### None

No-op implementation of `GenAIProvider`.

This provider is intended for environments where no external LLM integration should be used. It accumulates prompt text in memory and can optionally write instructions and prompts to local files when `inputsLog(File)` has been configured.

Key characteristics

- No network calls are performed.
- `perform()` always returns `null`.
- Unsupported capabilities (for example, `embedding(String,long)`) throw `UnsupportedOperationException`.

Example

```java
GenAIProvider provider = new NoneProvider();
provider.inputsLog(new File("./inputsLog/inputs.txt"));
provider.instructions("You are a helpful assistant.");
provider.prompt("Describe the weather.");
provider.perform();
```

### OpenAI

OpenAI-backed `GenAIProvider` implementation.

This provider integrates the OpenAI Java SDK with MachAI by assembling and executing requests via the OpenAI Responses API. It accumulates user inputs (text prompts and optional file references), optional system-level instructions, and an optional set of function tools. When `perform()` is invoked, the provider calls the OpenAI Responses API, processes the model output (including iterative function tool calls), and returns the final assistant text.

Configuration

Configuration variables consumed by `init(Configurator)`:

- `chatModel`: required model identifier passed to the OpenAI Responses API (for example, `gpt-4.1` or `gpt-4o`).
- `OPENAI_API_KEY`: required API key used to authenticate with the OpenAI API.
- `OPENAI_BASE_URL`: optional base URL for OpenAI-compatible endpoints. If unset, the SDK default base URL is used.
- `GENAI_TIMEOUT`: optional request timeout (in seconds). If missing or `0`, the SDK default timeouts are used.
- `MAX_OUTPUT_TOKENS`: optional maximum number of output tokens. Defaults to `OpenAIProvider.MAX_OUTPUT_TOKENS`.
- `MAX_TOOL_CALLS`: optional maximum number of tool calls allowed in a single response. Defaults to `OpenAIProvider.MAX_TOOL_CALLS`.

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