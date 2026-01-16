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

`OpenAIProvider` is a concrete `GenAIProvider` implementation that integrates with the OpenAI API.

Capabilities:

- Sends prompts and receives responses from OpenAI chat models.
- Uploads files and references them in requests.
- Supports tool/function calling.
- Supports embeddings generation.

Environment variables:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

CodeMie-compatible OpenAI API example:

- `OPENAI_API_KEY` = `eyJhbGciOiJSUzI1NiIsInR5c....`
- `OPENAI_BASE_URL` = `https://codemie.lab.epam.com/code-assistant-api/v1`

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: this implementation is not thread-safe.

### None

`NoneProvider` is a `GenAIProvider` implementation used to disable GenAI integration while optionally logging requests locally.

Purpose:

- Acts as a stub provider when an external AI backend is not required or not available.
- Stores requests in input files when `inputsLog` is configured (commonly under an `inputsLog` folder).
- Does not call any external services.

Typical use cases:

- Disabling GenAI features for security or compliance.
- Fallback behavior when no provider is configured.
- Logging requests for later manual review.
- Running tests in environments without external connectivity.

Notes:

- Operations that require GenAI services (for example, embeddings) may throw exceptions.
- Prompts and instructions are cleared after `perform()`.

Usage example:

```java
GenAIProvider provider = new NoneProvider();
provider.prompt("Describe the weather.");
provider.perform();
```

Thread safety: no explicit guarantees.

### Web

`WebProvider` is a UI automation-based provider that uses a web driver workflow (via Anteater AE recipes) to interact with web-based GenAI user interfaces when direct API access is not feasible.

Supported targets mentioned by the provider documentation:

- AI DIAL
- EPAM AI/Run CodeMie

Limitations:

- May require additional plugins and OS resources (for example, clipboard integration), especially for CodeMie.
- Requires an initialized workspace via `setWorkingDir(File)` and configuration selection via `model(String)`.

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```

Thread safety: this implementation is not thread-safe.

<!-- @guidance:
- Follow the rules described in @guidance tags and do not change or delete `@guidance` related tags in processing.
- Update the file follow to described @guidance in markdown html comments marked as 
  `@guidance` in the format `<!-- @guidance: ...RULES... -->`. 
-->