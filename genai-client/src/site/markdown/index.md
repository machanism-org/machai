# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client provides an abstraction and integration layer for Large Language Model (LLM) and AI inference services in Java. Its modular architecture supports multiple GenAI providers out of the box and can be easily extended for further providers. The project is designed for usage with prompt engineering, code generation, and classic generative AI workloads in Java applications, tools, or server environments.

## Supported GenAIProvider Implementations

### AEProvider
- **Package**: `org.machanism.machai.ai.ae`
- **Class**: `AeProvider`
- **Description**: Provides integration with AE Workspace for running prompt-to-code tasks. Extends `NoneProvider` to utilize prompt-driven workflows within the AE environment, manages AE workspace, runs setup nodes, and can execute project recipes based on user prompts.
- **Usage Example**:
  ```java
  AeProvider provider = new AeProvider();
  provider.model("CodeMie");
  provider.setWorkingDir(new File("/path/to/project"));
  String result = provider.perform();
  ```

### OpenAIProvider
- **Package**: `org.machanism.machai.ai.openAI`
- **Class**: `OpenAIProvider`
- **Description**: Provides integration with the OpenAI API (such as GPT-4, GPT-3.5) and supports prompts, chat, tool-calling, file uploads, embeddings, and instructions. Not thread-safe. Working directory and model name can be set. Tools can be registered to handle function calls.
- **Usage Example**:
  ```java
  OpenAIProvider provider = new OpenAIProvider();
  provider.model("gpt-4");
  provider.prompt("Hello, how are you?");
  String response = provider.perform();
  ```

### NoneProvider
- **Package**: `org.machanism.machai.ai.none`
- **Class**: `NoneProvider`
- **Description**: Fallback or stub implementation for environments where AI features are disabled. All operations are no-ops or throw exceptions. Useful for testing default/fallback scenarios in code.
- **Usage Example**:
  ```java
  GenAIProvider provider = new NoneProvider();
  provider.prompt("A prompt");
  provider.perform(); // returns null, does not call any LLM
  ```


