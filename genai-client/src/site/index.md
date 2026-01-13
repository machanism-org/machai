# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client provides an abstraction and integration layer for Large Language Model (LLM) and AI inference services in Java. Its modular architecture supports multiple `GenAIProvider` implementations and is designed to be extended with additional providers as needed.

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

This provider enables a wide range of generative AI capabilities, including:

- Sending prompts and receiving responses from OpenAI Chat models.
- Managing files for use in various OpenAI workflows.
- Performing advanced large language model (LLM) requests, such as text generation, summarization, and question answering.
- Creating and utilizing vector embeddings for tasks like semantic search and similarity analysis.

By abstracting the complexities of direct API interaction, `OpenAIProvider` allows developers to leverage OpenAI models efficiently within their applications. It supports synchronous operations and can be configured to accommodate different use cases and model parameters.

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

Thread safety: This implementation is NOT thread-safe.

### NoneProvider
The `NoneProvider` class is an implementation of the `GenAIProvider` interface used to disable generative AI integrations and log input requests locally when an external AI provider is not required or available.

Purpose:

Provides a stub implementation that stores requests in input files (in the `inputsLog` folder). All GenAI operations are non-operative, or throw exceptions where necessary, making this useful for scenarios where generative AI features must be disabled, simulated, or for fallback testing. No calls are made to any external AI services or large language models (LLMs).

Typical use cases:

- Disabling generative AI features for security or compliance.
- Implementing fallback logic when no provider is configured.
- Logging requests for manual review or later processing.
- Testing environments not connected to external services.

Notes:

- Operations requiring GenAI services will throw exceptions when called.
- All prompts and instructions are cleared after performing.

### WebProvider
The `WebProvider` class serves as a gateway for interacting with web-based user interfaces via a web driver when direct access to the GenAI API is not feasible.

It automates communication with supported services such as AI DIAL and EPAM AI/Run CodeMie, utilizing recipes from Anteater for sending and receiving information.

Limitations:

Configuration and usage of this class may require additional plugins or handling of resources such as the clipboard, especially for platforms like CodeMie. Refer to the target platform instructions prior to use.

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```

Thread safety: This implementation is not thread-safe.

Parameters and methods:

- `perform()` - Executes the AE workspace task using input prompts.
- `setWorkingDir(File workingDir)` - Initializes workspace with configuration and runs setup nodes.
- `model(String configName)` - Sets the AE workspace configuration name.

<!-- @guidance:
- Follow the rules described in @guidance tags and do not change or delete `@guidance` related tags in processing.
- Update the file follow to described @guidance in markdown html comments marked as 
  `@guidance` in the format `<!-- @guidance: ...RULES... -->`. 
-->
