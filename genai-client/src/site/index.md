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

`OpenAIProvider` is a `GenAIProvider` implementation backed by the OpenAI API.

Capabilities:

- Sends prompts and receives responses from OpenAI chat/response models.
- Uploads local files and attaches them to requests; also supports attaching remote files by URL.
- Supports function/tool calling by registering tools and handling tool calls.
- Generates embeddings (using `text-embedding-ada-002`).
- Optional logging of request inputs to a file.

Environment variables / system properties:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

OpenAI-compatible endpoints (example: CodeMie API):

- `OPENAI_API_KEY` = `eyJhbGciOiJSUzI1NiIsInR5c....`
- `OPENAI_BASE_URL` = `https://codemie.lab.epam.com/code-assistant-api/v1`

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: this implementation is NOT thread-safe.

### CodeMie

`CodeMieProvider` is an `OpenAIProvider` specialization preconfigured for the CodeMie OpenAI-compatible endpoint.

It uses the Keycloak token endpoint to obtain an access token via the Resource Owner Password flow, then configures the OpenAI client via system properties:

- `OPENAI_API_KEY` is set to the retrieved access token
- `OPENAI_BASE_URL` is set to `https://codemie.lab.epam.com/code-assistant-api/v1`

Configuration (required Java system properties):

- `GENAI_USERNAME`
- `GENAI_PASSWORD`

Notes:

- Token retrieval uses `client_id=codemie-sdk` against the configured Keycloak token URL.
- Thread safety follows `OpenAIProvider` (not thread-safe).

### None

`NoneProvider` is a stub `GenAIProvider` intended to disable GenAI integrations while optionally logging request inputs locally.

Behavior:

- Accumulates prompts and can write them to the configured `inputsLog` file.
- Makes no external calls to any AI services.
- `embedding(...)` is not supported and throws an exception.
- Prompts and instructions are cleared after `perform()`.

Typical use cases:

- Disabling generative AI features for security or compliance.
- Fallback when no provider is configured.
- Logging prompts for manual review.
- Testing environments without external connectivity.

Usage example:

```java
GenAIProvider provider = new NoneProvider();
provider.prompt("Describe the weather.");
provider.perform();
```

### Web

`WebProvider` obtains model responses by automating a target GenAI service through its web UI using Anteater workspace recipes.

How it works:

- `model(String)` sets the Anteater configuration name that will be loaded.
- `setWorkingDir(File)` initializes the shared workspace, sets `PROJECT_DIR`, and loads/runs setup recipes.
- `perform()` runs the `Submit Prompt` recipe, passing prompts as a system variable `INPUTS`, and returns the captured `result` variable.

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