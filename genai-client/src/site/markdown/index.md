# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client is a Java abstraction layer over multiple Generative AI backends exposed via a single provider interface. It is designed for integrating prompt/response flows, tool (function) calling, file-based context, and embeddings into Java applications and automation.

The primary integration point is the `GenAIProvider` interface, with `GenAIProviderManager` used to resolve and instantiate a concrete provider by name.

## Core concepts

- `GenAIProvider` defines the contract for prompting, adding file context, configuring a model, running a request, and optionally producing embeddings.
- `GenAIProviderManager` resolves a provider by name (for example `OpenAI:...` or `Web:...`) and returns a ready-to-configure instance.
- Tool/function calling is supported by registering tools on the provider; when the model requests a tool invocation, the tool output is fed back into the model.

## Supported GenAI Providers
<!-- 
@guidance: 
IMPORTANT: Update this section! Use from javadoc of classes from source folder which extend the GenAI interface 
and generate the content for this section following net format:
### [PROVIDER_NAME]
... FULL DESCRIPTION ...
-->

### None
The `NoneProvider` class is an implementation of the `GenAIProvider` interface used to disable generative AI integrations and log input requests locally when an external AI provider is not required or available.

Purpose:

- Provides a stub implementation that stores requests in input files (in the `inputsLog` folder).
- All GenAI operations are non-operative, or throw exceptions where necessary, making this useful for scenarios where generative AI features must be disabled, simulated, or for fallback testing.
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
provider.perform(); // No AI service is called; input may be logged locally
```

Notes:

- Operations requiring GenAI services will throw exceptions when called.
- All prompts and instructions are cleared after performing.

### OpenAI
The `OpenAIProvider` integrates with the OpenAI API as a concrete implementation of the `GenAIProvider` interface.

This provider enables a wide range of generative AI capabilities, including:

- Sending prompts and receiving responses from OpenAI Chat models
- Managing files for use in various OpenAI workflows
- Performing advanced large language model (LLM) requests such as text generation, summarization, and question answering
- Creating and using vector embeddings for tasks like semantic search and similarity analysis

It supports both synchronous and asynchronous operations, and can be extended or configured to accommodate different use cases and model parameters.

Environment variables:

- `OPENAI_API_KEY` (required)
- `OPENAI_ORG_ID` (optional)
- `OPENAI_PROJECT_ID` (optional)
- `OPENAI_BASE_URL` (optional)

Using the CodeMie API (via an OpenAI-compatible endpoint):

- `OPENAI_API_KEY` = `eyJhbGciOiJSUzI1NiIsInR5c....`
- `OPENAI_BASE_URL` = `https://codemie.lab.epam.com/code-assistant-api/v1`

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: this implementation is not thread-safe.

### Web
The `WebProvider` class serves as a gateway for interacting with web-based user interfaces via a web driver when direct access to the GenAI API is not feasible. It automates communication with supported services such as AI DIAL and EPAM AI/Run CodeMie, utilizing recipes from Anteater for sending and receiving information.

Limitations:

- Configuration and usage may require additional plugins or handling of resources such as the clipboard, especially for platforms like CodeMie.
- Refer to target platform instructions prior to use.

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```

Thread safety: this implementation is not thread-safe.

Parameters and methods:

- `perform()` executes the AE workspace task using input prompts.
- `setWorkingDir(File workingDir)` initializes the workspace, loads configuration, and runs setup nodes.
- `model(String configName)` sets the AE workspace configuration name.
