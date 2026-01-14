# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client is a Java library that provides a small abstraction layer for working with generative AI (LLM) services through a common `GenAIProvider` interface.

It helps you:

- Select a provider and model using a single identifier (for example, `OpenAI:gpt-5.1`).
- Submit prompts as plain text or from files.
- Attach local or remote files (provider-dependent).
- Generate embeddings (provider-dependent).
- Extend requests with callable “tools” (provider-dependent).
- Configure a working directory and optionally log request inputs.

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
The `OpenAIProvider` class integrates seamlessly with the OpenAI API, serving as a concrete implementation of the `GenAIProvider` interface.

This provider enables a wide range of generative AI capabilities, including:

- Sending prompts and receiving responses from OpenAI Chat models.
- Managing files for use in various OpenAI workflows.
- Performing advanced large language model (LLM) requests, such as text generation, summarization, and question answering.
- Creating and utilizing vector embeddings for tasks like semantic search and similarity analysis.

By abstracting the complexities of direct API interaction, `OpenAIProvider` allows developers to leverage OpenAI’s powerful models efficiently within their applications. It supports both synchronous and asynchronous operations, and can be easily extended or configured to accommodate different use cases and model parameters.

Environment variables:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

Using the CodeMie API:

- `OPENAI_API_KEY`=eyJhbGciOiJSUzI1NiIsInR5c....
- `OPENAI_BASE_URL`=`https://codemie.lab.epam.com/code-assistant-api/v1`

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: this implementation is not thread-safe.

### None
The `NoneProvider` class is an implementation of the `GenAIProvider` interface used to disable generative AI integrations and log input requests locally when an external AI provider is not required or available.

Purpose:

- Provides a stub implementation that stores requests in input files (in the `inputsLog` folder).
- All GenAI operations are non-operative, or throw exceptions where necessary.
- No calls are made to any external AI services or large language models (LLMs).

Typical use cases:

- Disabling generative AI features for security or compliance
- Implementing fallback logic when no provider is configured
- Logging requests for manual review or later processing
- Testing environments not connected to external services

Example usage:

```java
GenAIProvider provider = new NoneProvider();
provider.prompt("Describe the weather.");
provider.perform();
```

Notes:

- Operations requiring GenAI services will throw exceptions when called.
- All prompts and instructions are cleared after performing.

### Web
The `WebProvider` class serves as a gateway for interacting with web-based user interfaces via a web driver when direct access to the GenAI API is not feasible.

It automates communication with supported services such as AI DIAL and EPAM AI/Run CodeMie, utilizing recipes from Anteater for sending and receiving information.

Limitations:

- Configuration and usage may require additional plugins or handling of resources such as the clipboard, especially for platforms like CodeMie.
- Refer to the target platform instructions prior to use.

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```

Thread safety: this implementation is not thread-safe.

Parameters and methods:

- `perform()` — executes the AE workspace task using input prompts.
- `setWorkingDir(File workingDir)` — initializes workspace with configuration and runs setup nodes.
- `model(String configName)` — sets the AE workspace configuration name.

<!-- @guidance:
- Follow the rules described in @guidance tags and do not change or delete `@guidance` related tags in processing.
- Update the file follow to described @guidance in markdown html comments marked as 
  `@guidance` in the format `<!-- @guidance: ...RULES... -->`. 
-->