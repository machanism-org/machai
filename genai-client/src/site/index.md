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
provider.model("gpt-5.1");
provider.instructions("You are a helpful assistant.");
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

`OpenAIProvider` integrates with the OpenAI API as a concrete implementation of the `GenAIProvider` interface.

Capabilities include:

- Sending prompts and receiving responses from OpenAI chat models.
- Managing files for use in OpenAI workflows.
- Performing LLM requests such as text generation, summarization, and question answering.
- Creating and using vector embeddings for semantic search and similarity analysis.
- Tool/function calling via registered tools.

Environment variables (read automatically by the OpenAI client; you must set at least `OPENAI_API_KEY`):

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

Using the CodeMie API via an OpenAI-compatible endpoint:

- Set `OPENAI_API_KEY` to an access token.
- Set `OPENAI_BASE_URL` to `https://codemie.lab.epam.com/code-assistant-api/v1`.

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
provider.model("gpt-5.1");
```

Thread safety: this implementation is NOT thread-safe.

### CodeMie

`CodeMieProvider` is an `OpenAIProvider` specialization that targets the CodeMie OpenAI-compatible endpoint.

Before creating the underlying OpenAI client, it authenticates against a Keycloak token endpoint using the Resource Owner Password flow (`grant_type=password`, `client_id=codemie-sdk`). It then configures the OpenAI client via Java system properties:

- `OPENAI_API_KEY` is set to the retrieved access token.
- `OPENAI_BASE_URL` is set to `https://codemie.lab.epam.com/code-assistant-api/v1`.

Required Java system properties:

- `GENAI_USERNAME`
- `GENAI_PASSWORD`

### None

`NoneProvider` is an implementation of `GenAIProvider` used to disable generative AI integrations and optionally log input requests locally.

Purpose:

- Provides a stub implementation that stores requests in input files (in the `inputsLog` folder).
- No calls are made to any external AI services or LLMs.

Typical use cases:

- Disabling generative AI features for security or compliance.
- Implementing fallback logic when no provider is configured.
- Logging requests for manual review or later processing.
- Testing environments not connected to external services.

Notes:

- Operations requiring GenAI services throw exceptions when called (for example, embeddings).
- Prompts and instructions are cleared after performing.

### Web

`WebProvider` is a `GenAIProvider` implementation that obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via Anteater workspace recipes. The provider loads a workspace configuration (via `model(String)`), initializes the workspace with a project directory (via `setWorkingDir(File)`), and submits the current prompt list by running the `Submit Prompt` recipe (via `perform()`). The recipe is expected to place the final response text into a variable named `result`.

Thread safety and lifecycle:

- This provider is not thread-safe.
- Workspace state is stored in static fields; the working directory cannot be changed once initialized in the current JVM instance.
- `close()` closes the underlying workspace.

<!-- @guidance:
- Follow the rules described in @guidance tags and do not change or delete `@guidance` related tags in processing.
- Update the file follow to described @guidance in markdown html comments marked as 
  `@guidance` in the format `<!-- @guidance: ...RULES... -->`. 
-->