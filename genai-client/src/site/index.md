# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client is a Java library that provides a small, provider-agnostic API for running generative AI tasks through a single `GenAIProvider` interface.

It helps keep application code stable while you swap or combine different backends (API-based providers and UI/web-automation providers) through a unified programming model.

## Key features

- Provider selection by a single identifier (`Provider:Model`), resolved by `GenAIProviderManager`.
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

- Not implemented yet: `init(...)` throws `NotImplementedError`, and other methods are currently no-op or placeholders.

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

- `OPENAI_BASE_URL` to the CodeMie API base URL.
- `OPENAI_API_KEY` to the retrieved access token.

It then delegates requests to:

- `OpenAIProvider` for `gpt-*` models
- `ClaudeProvider` for `claude-*` models

Configuration

Required configuration keys:

- `GENAI_USERNAME` – user e-mail or client id.
- `GENAI_PASSWORD` – password or client secret.
- `chatModel` – model identifier (for example `gpt-4o-mini` or `claude-3-5-sonnet`).

Optional configuration keys:

- `AUTH_URL` – token endpoint override.

Built-in endpoints

- Token URL (default): `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token`
- Base URL: `https://codemie.lab.epam.com/code-assistant-api/v1`

### None

No-op implementation of `GenAIProvider`.

This provider is intended for environments where no external LLM integration should be used. It accumulates prompt text in memory and can optionally write instructions and prompts to local files when `inputsLog(File)` has been configured.

Key characteristics

- No network calls are performed.
- `perform()` always returns `null`.
- Unsupported capabilities (for example, `embedding(String)`) throw `UnsupportedOperationException`.

Example

```java
GenAIProvider provider = new NoneProvider();
provider.inputsLog(new File("./inputsLog/inputs.txt"));
provider.instructions("You are a helpful assistant.");
provider.prompt("Describe the weather.");
provider.perform();
```

### OpenAI

OpenAI-backed implementation of MachAI's `GenAIProvider` abstraction.

This provider adapts the OpenAI Java SDK to MachAI's provider interface. It accumulates user inputs (text prompts and optional file references), optional system-level instructions, and an optional set of function tools. When `perform()` is invoked, the provider calls the OpenAI Responses API, processes the model output (including iterative function tool calls), and returns the final assistant text.

Capabilities

- Submit prompts and retrieve text responses.
- Upload local files or attach files by URL for use in a request.
- Register function tools and dispatch tool calls to application handlers.
- Create vector embeddings for input text.
- Report token usage to `GenAIProviderManager`.

Configuration

- `OPENAI_API_KEY` (required)
- `OPENAI_BASE_URL` (optional)
- `chatModel` (optional; required before `perform()` if not set via configuration)

Usage

```java
Configurator conf = ...;
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1", conf);

provider.instructions("You are a concise assistant.");
provider.prompt("Summarize this text...");
String answer = provider.perform();

provider.clear();
provider.close();
```

Thread-safety

Instances are not thread-safe. Use one provider instance per request or synchronize externally.

### Web

`GenAIProvider` implementation that obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via Anteater workspace recipes. The provider loads a workspace configuration (see `model(String)`), initializes the workspace with a project directory (see `setWorkingDir(File)`), and submits the current prompt list by running the `Submit Prompt` recipe (see `perform()`).

Thread safety and lifecycle

- This provider is not thread-safe.
- Workspace state is stored in static fields; the working directory cannot be changed once initialized in the current JVM instance.
- `close()` closes the underlying workspace.

Example

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
provider.model("config.yaml");
provider.setWorkingDir(new File("C:\\path\\to\\project"));
String response = provider.perform();
```

<!-- @guidance:
- Follow the rules described in @guidance tags and do not change or delete `@guidance` related tags in processing.
- Update the file follow to described @guidance in markdown html comments marked as 
  `@guidance` in the format `<!-- @guidance: ...RULES... -->`. 
-->