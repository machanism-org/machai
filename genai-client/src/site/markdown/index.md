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
The `OpenAIProvider` integrates with the OpenAI API and provides a comprehensive implementation of the `GenAIProvider` interface. It enables:

- Text generation, summarization, question answering, and other LLM tasks via OpenAI Chat models
- File management to support workflow scenarios
- Creation and utilization of vector embeddings for semantic search and similarity functions
- Abstraction over direct API calls, supporting synchronous and asynchronous interaction
- Addition of custom tools with complex parameterization in prompt flows

To use OpenAI functionalities, ensure the `OPENAI_API_KEY` environment variable is set. 

Usage:
```java
GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
```

This implementation is NOT thread-safe.

---

### NoneProvider
The `NoneProvider` is an implementation of `GenAIProvider` that acts as a request logger and stub when integration with external AI services is not desired or available.

- Stores requests as input logs in files (typically in the `inputsLog` folder)
- Does not interact with any external LLM or AI service
- All operations are either no-ops or throw exceptions if AI functionality is strictly required
- Ideal for testing scenarios, compliance environments, or as a default fallback

No actual AI operations are performed. Maintains interface compatibility and consistent application code paths where GenAI features are disabled.

---

### WebProvider
The `WebProvider` facilitates GenAI interactions through web-based user interfaces via a web driver when API-level access isn’t possible. It automates:

- Communication with platforms like [AI DIAL](https://solutionshub.epam.com/solution/ai-dial) and [EPAM AI/Run CodeMie](https://www.youtube.com/@EPAMAIRunCodeMie) using recipes from [Anteater](https://ganteater.com)
- Execution of workspace tasks using input prompts and recipe runners
- Workspace setup, configuration loading, and environment initialization

**Limitations:**
- May require additional plugins, resource access (e.g., clipboard), or platform-specific setup
- Configuration/data must match environment requirements

Usage:
```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```

This implementation is NOT thread-safe.

Supported operations:
- `perform()`: Run AE workspace task with current input prompts
- `setWorkingDir(File workingDir)`: Initialize workspace and configuration
- `model(String configName)`: Specify AE workspace configuration
