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

`OpenAIProvider` integrates seamlessly with the OpenAI API, serving as a concrete implementation of the `GenAIProvider` interface.

This provider enables a wide range of generative AI capabilities, including:

- Sending prompts and receiving responses from OpenAI Chat models.
- Managing files for use in various OpenAI workflows.
- Performing advanced large language model (LLM) requests (for example, text generation, summarization, and question answering).
- Creating and utilizing vector embeddings for tasks like semantic search and similarity analysis.

By abstracting the complexities of direct API interaction, `OpenAIProvider` allows developers to leverage OpenAI’s powerful models efficiently within their applications. It supports synchronous operations, and can be configured for different use cases and model parameters.

Environment variables:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

Using the CodeMie API:

- `OPENAI_API_KEY` = `eyJhbGciOiJSUzI1NiIsInR5c....`
- `OPENAI_BASE_URL` = `https://codemie.lab.epam.com/code-assistant-api/v1`

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: this implementation is NOT thread-safe.

### None

`NoneProvider` is an implementation of the `GenAIProvider` interface used to disable generative AI integrations and optionally log input requests locally when an external AI provider is not required or available.

Purpose:

- Provides a stub implementation that stores requests in input files (via `inputsLog`).
- Performs no external calls to any AI services or LLMs.
- Useful for scenarios where GenAI features must be disabled, simulated, or used for fallback testing.

Typical use cases:

- Disabling GenAI features for security or compliance.
- Implementing fallback logic when no provider is configured.
- Logging requests for manual review or later processing.
- Running tests in environments without external connectivity.

Notes:

- Operations requiring GenAI services (for example, embeddings) throw exceptions.
- Prompts and instructions are cleared after `perform()`.

Usage example:

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

Usage example:

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