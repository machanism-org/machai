# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client is a Java library that provides a small, provider-agnostic API for running generative AI tasks through a single `GenAIProvider` interface.

It is designed to keep your application code stable while you swap or combine different backends (API-based providers and UI/web-automation providers).

## Key features

- Provider selection via a single identifier (`Provider:Model`), resolved by `GenAIProviderManager`.
- Prompt composition from plain text or (provider-dependent) from files.
- Optional attachment of local or remote files (provider-dependent).
- Embeddings support (provider-dependent).
- Tool/function calling support for extending provider behavior (provider-dependent).
- Working directory support and optional request input logging.

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

`OpenAIProvider` integrates with the OpenAI API as a concrete implementation of `GenAIProvider`.

It supports:

- Sending prompts and receiving responses from OpenAI chat models.
- Managing files for use in OpenAI workflows.
- Performing LLM requests (text generation, summarization, question answering).
- Creating and using vector embeddings for tasks like semantic search and similarity analysis.

Environment variables (read automatically; you must set at least `OPENAI_API_KEY`):

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

Using the CodeMie OpenAI-compatible endpoint:

- `OPENAI_API_KEY` = `eyJhbGciOiJSUzI1NiIsInR5c....`
- `OPENAI_BASE_URL` = `https://codemie.lab.epam.com/code-assistant-api/v1`

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: this implementation is NOT thread-safe.

### CodeMie

`CodeMieProvider` extends `OpenAIProvider` and targets the CodeMie OpenAI-compatible endpoint.

It retrieves an access token from a Keycloak token endpoint using the Resource Owner Password (password) grant and configures the OpenAI client via Java system properties:

- sets `OPENAI_API_KEY` to the retrieved access token
- sets `OPENAI_BASE_URL` to `https://codemie.lab.epam.com/code-assistant-api/v1`

Configuration (required Java system properties):

- `GENAI_USERNAME`
- `GENAI_PASSWORD`

Notes:

- Token retrieval uses `client_id=codemie-sdk`.
- Thread safety follows `OpenAIProvider`.

### None

`NoneProvider` implements `GenAIProvider` to disable generative AI integrations and optionally log inputs locally.

Purpose:

- Provides a stub implementation that stores requests in an `inputsLog` file.
- No calls are made to any external AI services or large language models (LLMs).

Typical use cases:

- Disabling generative AI features for security or compliance.
- Implementing fallback logic when no provider is configured.
- Logging requests for manual review or later processing.
- Testing environments not connected to external services.

Notes:

- Operations requiring GenAI services may be non-operative or throw exceptions.
- `embedding(...)` is not supported and throws an exception.
- Prompts and instructions are cleared after `perform()`.

Example usage:

```java
GenAIProvider provider = new NoneProvider();
provider.prompt("Describe the weather.");
provider.perform();
```

### Web

`WebProvider` obtains model responses by automating a target GenAI service through its web user interface.

Automation is executed via Anteater workspace recipes. The provider loads a workspace configuration (via `model(String)`), initializes the workspace with a project directory (via `setWorkingDir(File)`), and submits the current prompt list by running the `Submit Prompt` recipe (via `perform()`).

Thread safety and lifecycle:

- This provider is not thread-safe.
- Workspace state is stored in static fields; the working directory cannot be changed once initialized in the current JVM instance.
- `close()` closes the underlying workspace.

Example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
provider.model("config.yaml");
provider.setWorkingDir(new File("/path/to/project"));
String response = provider.perform();
provider.close();
```

<!-- @guidance:
- Follow the rules described in @guidance tags and do not change or delete `@guidance` related tags in processing.
- Update the file follow to described @guidance in markdown html comments marked as 
  `@guidance` in the format `<!-- @guidance: ...RULES... -->`. 
-->