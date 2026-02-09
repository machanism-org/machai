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

### OpenAI

The `OpenAIProvider` integrates with the OpenAI API and implements the `GenAIProvider` interface.

This provider supports:

- Sending prompts and receiving responses from OpenAI Chat models.
- Managing files for OpenAI workflows.
- Performing common LLM tasks (text generation, summarization, question answering).
- Creating vector embeddings for semantic search and similarity use cases.

By abstracting direct API interaction, `OpenAIProvider` provides a straightforward Java API for prompts, files, tools, and embeddings.

Environment variables

The client reads the following environment variables; you must set at least `OPENAI_API_KEY`:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

Using the CodeMie API

To use the CodeMie OpenAI-compatible endpoint with this provider, set:

- `OPENAI_API_KEY` = access token
- `OPENAI_BASE_URL` = `https://codemie.lab.epam.com/code-assistant-api/v1`

Usage example

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety

This implementation is NOT thread-safe.

### CodeMie

`CodeMieProvider` is an `OpenAIProvider` specialization that targets the CodeMie OpenAI-compatible endpoint.

On initialization, it authenticates against a token endpoint using the Resource Owner Password flow (`grant_type=password`, `client_id=codemie-sdk`). It then configures the underlying OpenAI client with the retrieved access token and uses the CodeMie base URL.

Required configuration

- `GENAI_USERNAME`
- `GENAI_PASSWORD`

Built-in endpoints

- Token URL (default): `https://auth.codemie.lab.epam.com/realms/codemie-prod/protocol/openid-connect/token`
- Base URL: `https://codemie.lab.epam.com/code-assistant-api/v1`

### None

No-op implementation of `GenAIProvider`.

This provider is intended for environments where no external LLM integration should be used. It accumulates prompt text in memory and can optionally write instructions and prompts to local files when `inputsLog(File)` has been configured.

Key characteristics

- No network calls are performed.
- `perform()` always returns `null`.
- Unsupported capabilities (for example, `embedding(String)`) throw an exception.

Example

```java
GenAIProvider provider = new NoneProvider();
provider.inputsLog(new File("./inputsLog/inputs.txt"));
provider.instructions("You are a helpful assistant.");
provider.prompt("Describe the weather.");
provider.perform();
```

### Web

`WebProvider` is a `GenAIProvider` implementation that obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via Anteater workspace recipes. The provider loads a workspace configuration (see `model(String)`), initializes the workspace with a project directory (see `setWorkingDir(File)`), and submits the current prompt list by running the `"Submit Prompt"` recipe (see `perform()`).

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