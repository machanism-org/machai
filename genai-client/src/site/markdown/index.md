# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client provides an abstraction and integration layer for Large Language Model (LLM) and AI inference services in Java. Its modular architecture supports multiple GenAI providers out of the box and can be easily extended for further providers. The project is designed for usage with prompt engineering, code generation, and classic generative AI workloads in Java applications, tools, or server environments.

## Supported GenAI Provider
<!-- 
@guidance: 
IMPORTANT: Update this section! Use from javadoc of classes from source folder which extend the GenAI interface 
and generate the content for this section following net format:
### [PROVIDER_NAME]
... FULL DESCRIPTION ...
-->

### WebProvider
This provider offers an alternative solution when direct access to the GenAI API is not possible. It functions as a gateway for interacting with web-based user interfaces through a web driver, enabling seamless integration with services like [AI DIAL](https://solutionshub.epam.com/solution/ai-dial) and [EPAM AI/Run CodeMie](https://www.youtube.com/@EPAMAIRunCodeMie). Communication with web pages is automated using [Anteater](https://ganteater.com) recipes, which facilitate the sending and receiving of information.

Please note that this provider may have certain limitations. Depending on the specific recipes executed, there may be special requirements, such as handling streaming security concerns or managing shared resources like the clipboard. Additionally, you might need to install extra plugins when working with platforms such as CodeMie. Be sure to review the instructions for your target system and complete all necessary setup steps before using this provider.

To configure the provider, use the model method to specify the Anteater configuration name (e.g., CodeMie or AIDial). You can also refer to the ae.xml file to view the list of supported configurations.

This class extends `NoneProvider` to utilize prompt-driven workflows within the AE environment. It manages the AE workspace, setup nodes, and can execute project recipes based on user prompts.

**Usage Example:**
```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```
Thread safety: This implementation is NOT thread-safe.

### OpenAIProvider
The `OpenAIProvider` class integrates seamlessly with the OpenAI API, serving as a concrete implementation of the `GenAIProvider` interface.

This provider enables a wide range of generative AI capabilities, including:
- Sending prompts and receiving responses from OpenAI Chat models.
- Managing files for use in various OpenAI workflows.
- Performing advanced large language model (LLM) requests, such as text generation, summarization, and question answering.
- Creating and utilizing vector embeddings for tasks like semantic search and similarity analysis.

By abstracting the complexities of direct API interaction, `OpenAIProvider` allows developers to leverage OpenAI’s powerful models efficiently within their applications. It supports both synchronous and asynchronous operations, and can be easily extended or configured to accommodate different use cases and model parameters.

This class provides capabilities to send prompts, manage files, perform LLM requests, and create embeddings using OpenAI Chat models.

**Usage Example:**
```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```
Thread safety: This implementation is NOT thread-safe.

### NoneProvider
The `NoneProvider` is a stub implementation of the `GenAIProvider` interface, designed to act as a placeholder when no AI service integration is required or available.

This class does not interact with any external AI service or large language model (LLM). All operations performed by `NoneProvider` are either no-operations (no-ops) or will throw exceptions where appropriate, ensuring that any attempt to use AI functionality is safely handled without side effects.

Typical use cases for `NoneProvider` include:
- Environments where AI services are disabled (e.g., due to security or compliance requirements).
- Testing scenarios where AI interactions should be mocked or bypassed.
- Default fallback when no other provider is configured.

By using `NoneProvider`, you can maintain consistent application behavior and interface compatibility, even when generative AI features are not in use.

**Usage Example:**
```java
GenAIProvider provider = GenAIProviderManager.getProvider(null);
```

This class does not interact with any AI service or LLM. All operations are either no-ops or throw exceptions where appropriate. Intended for environments where AI services are disabled, testing, or default fallback scenarios.
