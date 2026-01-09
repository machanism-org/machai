# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client provides an abstraction and integration layer for Large Language Model (LLM) and AI inference services in Java. Its modular architecture supports multiple GenAI providers out of the box and can be easily extended for further providers. The project is designed for usage with prompt engineering, code generation, and classic generative AI workloads in Java applications, tools, or server environments.

## Supported GenAI Providers
<!-- 
@guidance: 
IMPORTANT: Update this section! Use from javadoc of classes from source folder which extend the GenAI interface 
and generate the content for this section following net format:
### [PROVIDER_NAME]
... FULL DESCRIPTION ...
-->

### OpenAIProvider
The `OpenAIProvider` class integrates seamlessly with the OpenAI API, serving as a concrete implementation of the `GenAIProvider` interface.

This provider enables a wide range of generative AI capabilities, including:
- Sending prompts and receiving responses from OpenAI Chat models
- Managing files for use in various OpenAI workflows
- Performing advanced large language model (LLM) requests, such as text generation, summarization, and question answering
- Creating and utilizing vector embeddings for tasks like semantic search and similarity analysis

By abstracting the complexities of direct API interaction, `OpenAIProvider` allows developers to leverage OpenAI’s powerful models efficiently within their applications. It supports both synchronous and asynchronous operations, and can be easily extended or configured to accommodate different use cases and model parameters.

Usage example:
```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```
Thread safety: This implementation is NOT thread-safe.

### NoneProvider
The `NoneProvider` class is an implementation of the `GenAIProvider` interface, intended for use as a request logger when integration with AI services is not required or available.

This class does not interact with any external AI services or large language models (LLMs). `NoneProvider` stores requests in input files located in the `inputsLog` folder, which can be viewed or processed later in another process. Operations that necessarily require access to GenAI services will throw an exception or do nothing if it is not a critical action.

Typical use cases for `NoneProvider` include:
- Environments where AI services are disabled (e.g., due to security or compliance requirements)
- Testing scenarios where interaction with AI must be simulated or skipped
- Default fallback when no other provider is configured

By using `NoneProvider`, you can maintain consistent application behavior and interface compatibility even when generative AI features are not used. All operations are either non-operations or throw exceptions when appropriate. Intended for environments where AI services are disabled, for testing, or as a default backup scenario.

### WebProvider
The `WebProvider` class serves as a gateway for interacting with web-based user interfaces via a web driver when direct access to the GenAI API is not feasible.

It automates communication with supported services such as [AI DIAL](https://solutionshub.epam.com/solution/ai-dial) and [EPAM AI/Run CodeMie](https://www.youtube.com/@EPAMAIRunCodeMie), utilizing recipes from [Anteater](https://ganteater.com) for sending and receiving information.

Limitations: Configuration and usage of this class may require additional plugins or handling of resources such as the clipboard, especially for platforms like CodeMie. Please refer to target platform instructions prior to use.

Usage Example:
```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```
This implementation is NOT thread-safe.

Parameters and Methods:
- `perform()`: Executes the AE workspace task using input prompts.
- `setWorkingDir(File workingDir)`: Initializes workspace with configuration and runs setup nodes.
- `model(String configName)`: Sets the AE workspace configuration name.
