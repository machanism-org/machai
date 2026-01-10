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
The `OpenAIProvider` integrates seamlessly with the OpenAI API and provides a robust implementation of the `GenAIProvider` interface. Key features:
- Prompt engineering and response handling with OpenAI Chat models
- File upload and referencing for workflow scenarios
- Advanced LLM requests (text generation, summarization, Q&A)
- Vector embedding creation for semantic search and similarity
- Abstraction over API calls with both synchronous and asynchronous support
- Custom tool integration for complex prompt flows

To use OpenAI functionalities, set the `OPENAI_API_KEY` environment variable.

**Usage:**
```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

*This implementation is NOT thread-safe.*

### NoneProvider
The `NoneProvider` is a stub implementation of `GenAIProvider` for scenarios where external AI services are not needed. Features:
- Logs all input requests to files (usually in the `inputsLog` folder)
- Does not communicate with external AI/LLM services
- Operations are either no-ops or throw exceptions if AI is strictly needed
- Useful for compliance, disabled environments, or fallback/testing
- Maintains code-path compatibility when generative features are off

No actual AI tasks are executed by this provider. Interface remains compatible and consistent.

### WebProvider
The `WebProvider` enables GenAI interaction through web-based user interfaces using an automation web driver. It is designed for platforms where API access is unavailable and supports:
- Automated interaction with platforms such as [AI DIAL](https://solutionshub.epam.com/solution/ai-dial) and [EPAM AI/Run CodeMie](https://www.youtube.com/@EPAMAIRunCodeMie) using [Anteater](https://ganteater.com) recipes
- Execution of workspace tasks and prompt-driven flows
- Workspace setup and environment initialization with flexible configuration

**Limitations:**
- May require additional plugins or platform-specific resource setup (e.g., clipboard access)
- Workspace configuration/data must be available and compatible

**Usage:**
```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```

*This implementation is NOT thread-safe.*

**Supported operations:**
- `perform()`: Executes AE workspace task with current input prompts
- `setWorkingDir(File workingDir)`: Initializes workspace and environment
- `model(String configName)`: Loads AE workspace configuration
