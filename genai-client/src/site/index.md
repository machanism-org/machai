# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client provides an advanced abstraction and integration layer for Large Language Model (LLM) and AI inference services in Java. Its modular architecture supports multiple GenAI providers and can be easily extended for future integrations. The project is designed for developers and teams working on AI-driven prompt engineering, code generation, and classic generative AI workloads in enterprise Java applications, tooling platforms, and server environments.

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

Features include:
- Prompt engineering and response handling with OpenAI Chat models
- File upload and reference for workflow scenarios
- Advanced LLM requests (text generation, summarization, Q&A)
- Vector embedding creation for semantic search and similarity analysis
- Abstraction over API calls with both synchronous and asynchronous support
- Custom tool integration for complex prompt flows

To use OpenAI functionalities, set the `OPENAI_API_KEY` environment variable.

**Sample Usage:**
```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

*This implementation is NOT thread-safe.*

### NoneProvider
The `NoneProvider` class is an implementation of `GenAIProvider`, intended for use as a request logger when integration with AI services is not required or available. This class does not interact with any external AI services or large language models (LLMs). Requests are stored in input files located in the `inputsLog` folder, which can be processed later. Operations strictly requiring GenAI will throw exceptions or do nothing if not critical. Useful when AI services are disabled, for testing scenarios, or as a default fallback when no other provider is configured.

**Key Features:**
- Logs prompts and instructions to files
- No operations for actual AI tasks or model selection
- Fallback for disabled or compliance-restricted environments
- Maintains interface compatibility even when generative AI is off

### WebProvider
The `WebProvider` class serves as a gateway for interacting with web-based user interfaces via a web driver when direct access to the GenAI API is not feasible. It automates communication with supported services such as [AI DIAL](https://solutionshub.epam.com/solution/ai-dial) and [EPAM AI/Run CodeMie](https://www.youtube.com/@EPAMAIRunCodeMie), utilizing [Anteater](https://ganteater.com) recipes for sending and receiving information.

**Limitations:**
Configuration and usage may require additional plugins/resource setup, such as clipboard access for CodeMie. Workspace configuration/data must be compatible.

**Sample Usage:**
```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```

*This implementation is NOT thread-safe.*

**Supported operations:**
- `perform()`: Executes AE workspace task with current input prompts
- `setWorkingDir(File workingDir)`: Initializes workspace and environment
- `model(String configName)`: Loads AE workspace configuration

<!-- @guidance:
- Follow the rules described in @guidance tags and do not change or delete `@guidance` related tags in processing.
- Update the file follow to described @guidance in markdown html comments marked as 
  `@guidance` in the format `<!-- @guidance: ...RULES... -->`. 
-->
