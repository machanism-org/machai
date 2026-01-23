# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client is a Java library that provides a small, provider-agnostic API for running generative AI tasks through a single `GenAIProvider` interface.

It is designed to keep your application code stable while you swap or combine different backends (API-based providers and UI/web-automation providers).

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
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
provider.prompt("Summarize the following text...");
String answer = provider.perform();
provider.close();
```

If the provider part is omitted or blank, the manager falls back to the `None` provider.

## Supported GenAI Providers
<!-- 
@guidance: 
IMPORTANT: Update this section! Use from javadoc of classes from source folder which extend the GenAI interface 
and generate the content for this section following net format:
### [PROVIDER_NAME]
... FULL DESCRIPTION ...
-->

### OpenAI

`OpenAIProvider` integrates with the OpenAI API and serves as a concrete implementation of `GenAIProvider`.

Capabilities include:

- Sending prompts and receiving responses from OpenAI chat models.
- Managing files for use in OpenAI workflows.
- Common LLM tasks such as text generation, summarization, and question answering.
- Creating and using vector embeddings for semantic search and similarity analysis.
- Tool/function calling, where registered tools are executed locally and the outputs are fed back into the model.

Configuration is read from environment variables and/or Java system properties:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

Using the CodeMie OpenAI-compatible endpoint via this provider:

- Set `OPENAI_API_KEY` to an access token.
- Set `OPENAI_BASE_URL` to `https://codemie.lab.epam.com/code-assistant-api/v1`.

Example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: this implementation is NOT thread-safe.

### CodeMie

`CodeMieProvider` is an `OpenAIProvider` specialization that targets the CodeMie OpenAI-compatible endpoint.

Before creating the underlying OpenAI client it authenticates against a Keycloak token endpoint using the Resource Owner Password flow (`grant_type=password`, `client_id=codemie-sdk`). It then configures the OpenAI client via Java system properties:

- `OPENAI_API_KEY` is set to the retrieved access token
- `OPENAI_BASE_URL` is set to `https://codemie.lab.epam.com/code-assistant-api/v1`

Required Java system properties:

- `GENAI_USERNAME`
- `GENAI_PASSWORD`

Thread safety follows `OpenAIProvider`.

### None

`NoneProvider` is an implementation of `GenAIProvider` used to disable generative AI integrations and optionally log input requests locally when an external AI provider is not required or available.

Purpose and typical use cases:

- Disabling GenAI features for security/compliance.
- Providing fallback behavior when no provider is configured.
- Logging requests for manual review or later processing.
- Supporting test environments without external connectivity.

Behavior notes:

- No calls are made to external AI services.
- When `inputsLog(...)` is configured, `perform()` writes accumulated prompts to the configured file and, if instructions were set, writes them to `instructions.txt` in the same directory.
- Operations that require a real provider (for example, embeddings generation) throw an exception.
- Prompts and instructions are cleared after `perform()`.

### Web

`WebProvider` obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via Anteater workspace recipes:

- `model(String)` sets the Anteater configuration name to load (must be set before `setWorkingDir(...)`).
- `setWorkingDir(File)` initializes the workspace for a given project directory. It is intended to be called once per JVM instance; changing the directory later is rejected.
- `perform()` runs the `"Submit Prompt"` recipe, passing prompts via the `INPUTS` variable and returning the recipe-produced `result`.

Thread safety and lifecycle:

- This provider is not thread-safe.
- Workspace state is stored in static fields; the working directory and configuration cannot be changed once initialized in the current JVM instance.
- `close()` closes the underlying workspace.

<!-- @guidance:
- Follow the rules described in @guidance tags and do not change or delete `@guidance` related tags in processing.
- Update the file follow to described @guidance in markdown html comments marked as 
  `@guidance` in the format `<!-- @guidance: ...RULES... -->`. 
-->