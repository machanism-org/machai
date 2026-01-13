# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client provides a Java abstraction layer over multiple Generative AI backends through a single provider interface. It is intended for integrating LLM workflows (prompt/response), tool/function calling, file-based context, and embeddings into Java applications, build tooling, and server-side automation.

The main integration point is the `GenAIProvider` interface, with `GenAIProviderManager` used to select and instantiate a concrete provider by name.

## Supported GenAI Providers
<!-- 
@guidance: 
IMPORTANT: Update this section! Use from javadoc of classes from source folder which extend the GenAI interface 
and generate the content for this section following net format:
### [PROVIDER_NAME]
... FULL DESCRIPTION ...
-->

### OpenAIProvider
The `OpenAIProvider` class integrates with the OpenAI API as a concrete implementation of the `GenAIProvider` interface.

It enables a wide range of generative AI capabilities, including sending prompts and receiving responses from OpenAI chat models, managing files for use in OpenAI workflows, performing advanced LLM requests (text generation, summarization, and question answering), and creating/using vector embeddings for tasks such as semantic search and similarity analysis.

By abstracting direct API interaction, `OpenAIProvider` allows applications to leverage OpenAI models efficiently. It supports both synchronous and asynchronous operations and can be configured for different use cases and model parameters.

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: this implementation is NOT thread-safe.

### NoneProvider
The `NoneProvider` class is an implementation of the `GenAIProvider` interface used to disable generative AI integrations and log input requests locally when an external AI provider is not required or available.

Purpose: it provides a stub implementation that stores requests in input files (typically in an `inputsLog` folder). All GenAI operations are non-operative, or throw exceptions where necessary. No calls are made to any external AI services or large language models (LLMs).

Typical use cases include disabling generative AI features for security or compliance, implementing fallback logic when no provider is configured, logging requests for manual review or later processing, and running tests in environments without access to external services.

Example usage:

```java
GenAIProvider provider = new NoneProvider();
provider.prompt("Describe the weather.");
provider.perform();
```

Notes:

- Operations requiring GenAI services will throw exceptions when called (for example, embeddings).
- Prompts and instructions are cleared after performing.

### WebProvider
The `WebProvider` class serves as a gateway for interacting with web-based user interfaces via a web driver when direct access to a GenAI API is not feasible.

It automates communication with supported services such as AI DIAL and EPAM AI/Run CodeMie, utilizing Anteater recipes for sending and receiving information.

Limitations: configuration and usage may require additional plugins or handling of resources such as the clipboard, especially for platforms like CodeMie. Refer to the target platform instructions prior to use.

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```

Thread safety: this implementation is not thread-safe.

Parameters and methods:

- `perform()` executes the AE workspace task using input prompts.
- `setWorkingDir(File workingDir)` initializes the workspace with configuration and runs setup nodes.
- `model(String configName)` sets the AE workspace configuration name.
