# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule (three hyphens) separator between sections. 
- Scan org/machanism/machai/ai source folder and describe all supported GenAIProvider implementation in separate section.
-->

GenAI Client is a Java abstraction layer over multiple Generative AI backends exposed via a single provider interface. It is designed for integrating LLM workflows (prompt/response), tool/function calling, file-based context, and embeddings into Java applications, build tooling, and server-side automation.

The primary integration point is the `GenAIProvider` interface, with `GenAIProviderManager` used to select and instantiate a concrete provider by name.

## Core Concepts

- `GenAIProvider` defines the common contract for prompting, adding file context, configuring a model, running a request, and optionally producing embeddings.
- `GenAIProviderManager` resolves a provider by name (for example `OpenAI:...` or `Web:...`) and returns a ready-to-configure instance.
- Tool/function calling is supported by registering tools on the provider and letting the model request their execution; tool output is fed back into the model.

## Supported GenAI Providers
<!-- 
@guidance: 
IMPORTANT: Update this section! Use from javadoc of classes from source folder which extend the GenAI interface 
and generate the content for this section following net format:
### [PROVIDER_NAME]
... FULL DESCRIPTION ...
-->

### OpenAI
The `OpenAIProvider` integrates with the OpenAI API as a concrete implementation of `GenAIProvider`.

It supports:

- Sending prompts and receiving responses from OpenAI chat models
- Managing files for use in OpenAI workflows
- Performing advanced LLM requests such as text generation, summarization, and question answering
- Creating and using vector embeddings for tasks like semantic search and similarity analysis

By abstracting direct API interaction, `OpenAIProvider` enables efficient use of OpenAI models within applications and can be extended or configured for different model parameters and use cases.

Environment variables read automatically by the client:

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

### None
The `NoneProvider` disables generative AI integrations by providing a stub implementation of `GenAIProvider`.

It is intended for environments where GenAI must be disabled, simulated, or tested without network access. It can also be used as a safe fallback when no provider is configured.

Typical use cases:

- Disabling generative AI features for security or compliance
- Implementing fallback logic when no provider is configured
- Logging requests for manual review or later processing
- Running tests in environments without access to external services

Behavior notes:

- Prompts (and optional instructions) can be logged locally when `inputsLog` is configured
- No external calls are made to any AI service or LLM
- Operations that require a real GenAI service (for example embeddings) throw exceptions
- Prompts and instructions are cleared after `perform()`

Usage example:

```java
GenAIProvider provider = new NoneProvider();
provider.prompt("Describe the weather.");
provider.perform();
```

### Web
The `WebProvider` is a gateway for interacting with web-based GenAI user interfaces via a web driver when direct API access is not feasible.

It automates communication with supported services such as AI DIAL and EPAM AI/Run CodeMie, using recipes from Anteater to send prompts and retrieve results.

Limitations:

- Configuration and usage may require additional plugins or handling of resources such as the clipboard, especially for platforms like CodeMie
- Refer to the target platform instructions prior to use

Usage example:

```java
GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
```

Thread safety: this implementation is not thread-safe.

Key methods:

- `perform()` executes the AE workspace task using input prompts
- `setWorkingDir(File workingDir)` initializes the workspace, loads configuration, and runs setup nodes
- `model(String configName)` sets the AE workspace configuration name
