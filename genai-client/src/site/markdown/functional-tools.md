---
<!-- @guidance: 
Create or update the `Function Tolls` page:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
- Analyze classes in the folder: `src/main/java/org/machanism/machai/ai/tools`.
- Analize methods: addMcpServer() and addWebSearch() in the class `/src/main/java/org/machanism/machai/ai/provider/openai/OpenAIProvider.java` as an example to use and describe configuration properties for use it.
- Describe the feature.
- Write a general description how to create a custom functional tool.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/genai-client/functional-tools.html
---

# Functional Tools

Functional tools let the host application expose controlled capabilities to a `Genai` provider as callable tools and reusable prompts. In this project, the feature covers three main integration styles:

- Java-backed host tools registered through the functional-tools SPI,
- OpenAI-native web search configured directly on `OpenAIProvider`,
- external MCP servers attached as OpenAI MCP tools.

Together, these mechanisms make tool support modular, discoverable, and provider-friendly. Tool declarations live in focused Java classes, while the provider handles discovery, schema generation, invocation, and provider-specific transport details.

## Feature overview

Functional tools provide a structured way to:

- expose controlled application capabilities to the model,
- group related tools into reusable installer classes,
- discover tool installers automatically through Java `ServiceLoader`,
- execute Java methods annotated with `@Tool` and parameter metadata from `@Param`,
- expose reusable prompts through `@Prompt`,
- inject runtime context such as `Configurator` and the project directory,
- enable OpenAI web search from configuration,
- connect one or more external MCP servers,
- and combine annotation-based registration with direct programmatic tool registration.

This separation improves maintainability and reuse. Tool logic stays in business-focused classes, while registration and execution are centralized in provider code.

## Package: `org.machanism.machai.ai.tools`

The package `org.machanism.machai.ai.tools` contains the host-side SPI, annotations, descriptors, and runtime contracts used to contribute functional tools.

### `FunctionTools`

`FunctionTools` is the marker SPI for contributing host-managed tools and prompts to a `Genai` provider.

#### Purpose

Implement this interface when you want to contribute a reusable bundle of related methods. Public methods annotated with `@Tool` are registered as callable tools, and public methods annotated with `@Prompt` are registered as reusable prompts.

#### How it behaves

The interface itself has no methods. Instead, providers inspect implementing classes reflectively. The shared registration logic in `AbstractAIProvider` scans public methods on the implementation instance and registers matching annotations.

A `FunctionTools` implementation is usually discovered from the classpath through Java `ServiceLoader`, then applied by `FunctionToolsLoader`.

#### Good use cases

Use `FunctionTools` to package a coherent capability set, such as:

- file operations,
- HTTP access,
- command execution,
- source-control automation,
- or project-specific integrations.

### `FunctionToolsLoader`

`FunctionToolsLoader` is the bootstrap component that discovers `FunctionTools` implementations and applies compatible ones to a provider.

#### Purpose

It scans the classpath with Java `ServiceLoader`, keeps discovered implementations, and registers each compatible implementation against the target `Genai` instance.

#### Main behavior

- The constructor loads available `FunctionTools` implementations from the classpath using `ServiceLoader`.
- Discovered implementations are kept in an internal list in discovery order.
- `applyTools(Genai provider, Class<?> appClass)` iterates over the discovered implementations.
- Compatibility is checked through `@SupportedFor`.
- Each compatible instance is registered by calling both `provider.addTools(functionTool)` and `provider.addPrompts(functionTool)`.

#### Compatibility rules

If a tool bundle class has `@SupportedFor`, the loader checks each declared class with `isAssignableFrom(appClass)`. If the annotation is absent, the bundle is treated as compatible with all application classes.

#### Good use cases

Use `FunctionToolsLoader` during provider initialization when all tool bundles available on the classpath should be activated automatically.

### `ToolFunction`

`ToolFunction` is the functional callback contract used by the provider when invoking a host-managed tool or prompt.

#### Purpose

It represents the executable handler behind a registered tool or prompt.

#### Method

```java
Object apply(JsonNode params, File projectDir, Configurator config) throws IOException
```

#### Parameters

- `params`: parsed JSON arguments supplied by the model.
- `projectDir`: provider working directory, if configured.
- `config`: runtime configuration available to the provider.

#### Notes

- `SESSION_ID_PARAM_NAME` defines the constant `mcp_client_session_id`.
- The return value may be a string or another object. Non-string values are serialized by provider code before being sent back to the model.
- Provider implementations call tool handlers through safety wrappers so failures become model-visible error text.

### `@Tool`

`@Tool` is a method-level annotation that marks a public method on a `FunctionTools` implementation as a callable tool.

#### Attributes

- `name`: tool name visible to the provider and model. If omitted, the method name is used. Internally, the sentinel constant `Tool.NOT_DEFINED` is used to detect an unspecified value.
- `description`: human-readable explanation of what the tool does.

#### Example

```java
@Tool(name = "read_file", description = "Reads the content of a file.")
public String readFile(@Param(name = "path", description = "File path to read") String path) {
    // ...
}
```

If `name` is omitted, the Java method name becomes the tool name.

### `@Param`

`@Param` is a parameter-level annotation used to describe method parameters for tool and prompt schema generation.

#### Attributes

- `name`: parameter name exposed to the model. If omitted, the runtime uses the sentinel `Param.NOT_DEFINED` to indicate it was not explicitly set and falls back to the Java parameter name.
- `description`: human-readable description of the parameter.
- `defaultValue`: optional default value. The sentinel `Param.NOT_DEFINED` means no default was declared.

#### Constants

- `Param.NULL`: literal sentinel value `___NULL___`.
- `Param.NOT_DEFINED`: literal sentinel value `___NOT_DEFINED___`.

#### Runtime behavior

The provider uses `@Param` metadata to build parameter descriptors and JSON schema for the tool. Parameters without a declared default are treated as required.

At invocation time, the provider:

- reads the JSON argument by the declared parameter name,
- uses the default when the argument is missing,
- treats `Param.NULL` and `Param.NOT_DEFINED` as no effective default value,
- and converts the resulting string into the Java parameter type.

The special parameter name `project_dir` is reserved by the provider. When a parameter uses that name and a working directory is configured, the provider injects the current project directory path automatically. That parameter is also excluded from the published schema so the model does not need to supply it.

#### Type mapping

Java parameter types are mapped to JSON schema types through the provider type converter. Common mappings include:

- `String` and `File` to `string`,
- `int` and `Integer` to `integer`,
- `boolean` and `Boolean` to `boolean`.

#### Additional injections

Parameters without `@Param` can still be injected when supported by the provider runtime, most notably `Configurator` and `File`.

### `@Prompt`

`@Prompt` is a method-level annotation that marks a public method on a `FunctionTools` implementation as a reusable prompt.

#### Attributes

- `name`: prompt name passed to the provider. If omitted, the sentinel `Prompt.NOT_DEFINED` indicates that the method name should be used.
- `description`: human-readable description of the prompt.
- `role`: conversation role for the registered prompt. Defaults to `Role.ASSISTANT`.

#### Example

```java
@Prompt(name = "summarize_instructions", description = "Instruction prompt for summarization.", role = Role.ASSISTANT)
public String summarizeInstructions() {
    return "Summarize the provided content concisely.";
}
```

#### When to use it

Use `@Prompt` when a tool bundle should contribute reusable prompt text in addition to callable functions.

### `Role`

`Role` is the enum used by `@Prompt` to specify the conversation role associated with a prompt.

#### Values

- `ASSISTANT`: the prompt content is registered as assistant-role text.
- `USER`: the prompt content is registered as user-role text.

### `ParamDescriptor`

`ParamDescriptor` is a simple metadata holder for a single tool or prompt parameter.

#### Purpose

It carries the structured parameter information used by the provider to build a schema programmatically.

#### Constructor

```java
new ParamDescriptor(String name, String type, boolean required, String description)
```

#### Accessor methods

- `getName()`: returns the parameter name.
- `getType()`: returns the JSON schema type string.
- `getDescription()`: returns the parameter description.
- `isRequired()`: returns whether the parameter is required.

### `@SupportedFor`

`@SupportedFor` restricts a `FunctionTools` implementation to one or more application classes.

#### Purpose

Use it when a tool bundle only makes sense for specific processors, workflows, or application types.

#### Behavior

`FunctionToolsLoader` reads this annotation and uses `isAssignableFrom` to decide whether the current `appClass` is compatible.

#### Example

```java
@SupportedFor({ ActProcessor.class })
public class ActSpecFunctionTools implements FunctionTools {
    // ...
}
```

If the annotation is absent, the tool bundle is treated as compatible with all application classes.

## How functional tools work

A typical lifecycle looks like this:

1. Create one or more classes that implement `FunctionTools`.
2. Annotate public tool methods with `@Tool` and their exposed parameters with `@Param`.
3. Optionally annotate reusable prompt methods with `@Prompt`.
4. Register those classes with Java `ServiceLoader`.
5. Create and initialize the AI provider.
6. Call `FunctionToolsLoader.applyTools(provider, appClass)`.
7. The loader applies each compatible implementation.
8. The provider scans annotated methods and registers tools and prompts.
9. When the model invokes a tool, the provider resolves the matching method by tool name.
10. The method is invoked with model arguments and any injected runtime values.
11. The return value is sent back through the provider so the response can continue.

This design keeps tool registration modular, discoverable, and easy to package.

## How annotation-based registration works internally

The shared provider logic in `AbstractAIProvider` performs most of the annotation-driven registration work.

### Tool registration flow

When `provider.addTools(functionTools)` is called:

- the provider scans public methods of the implementation class,
- it selects methods annotated with `@Tool`,
- it resolves the exposed tool name from `@Tool.name` or falls back to the Java method name,
- it collects `@Param` metadata from method parameters,
- it converts parameter Java types through the internal type converter,
- it marks parameters as required when `defaultValue` is not declared,
- and it wraps reflective method invocation into a `ToolFunction` callback.

### Prompt registration flow

When `provider.addPrompts(functionTools)` is called:

- the provider scans public methods annotated with `@Prompt`,
- it resolves the prompt name from `@Prompt.name` or the method name,
- it captures the prompt role from `@Prompt.role`,
- it builds parameter descriptors in the same way as for tools,
- and it wraps the method in a `ToolFunction` callback for prompt execution.

### Invocation and parameter resolution

At invocation time, the provider:

- reads JSON arguments into a Jackson `JsonNode`,
- resolves explicit tool parameters from JSON by annotated name,
- applies `@Param.defaultValue` when no argument was supplied,
- supports placeholder substitution between earlier resolved argument values and later default values,
- injects `Configurator` for unannotated parameters of that type,
- injects `File` for unannotated file-context parameters,
- auto-fills the reserved `project_dir` parameter from the configured provider project directory,
- and applies placeholder substitution to string return values before returning them.

This means a custom tool method can combine model-supplied arguments with application runtime context without manual parsing boilerplate.

## OpenAI-specific functional tools

`OpenAIProvider` adds two provider-native tool types in addition to host-managed Java tools:

- built-in OpenAI web search,
- MCP server tools.

These are configured during provider initialization and stored in the provider tool map beside standard function tools.

## Web Search

The `addWebSearch(String type, String city, String country, String region)` method on `OpenAIProvider` registers the built-in OpenAI web search tool.

### How `addWebSearch(...)` behaves

- It creates a `UserLocation` builder and always sets the location type to `approximate`.
- If `type` equals the provider alias `default`, the method translates it to `web_search_preview`.
- It optionally fills `city`, `country`, and `region` when values are present.
- It builds an OpenAI `WebSearchTool` instance.
- The resulting tool is wrapped as a provider `Tool` and stored in the provider tool map.

### Configuration

Web search is enabled when `WebSearchTool.type` is present in configuration. The base registration flow lives in `AbstractAIProvider.addWebSearch()`, which reads the configured values and calls the OpenAI-specific `addWebSearch(...)` implementation.

#### Property reference

- `WebSearchTool.type`: required to enable web search. Defines the OpenAI web-search tool type. Use `default` to let the provider translate it to `web_search_preview`, or provide an explicit type supported by the OpenAI SDK.
- `WebSearchTool.city`: optional city used for approximate user location.
- `WebSearchTool.country`: optional country used for approximate user location.
- `WebSearchTool.region`: optional region or state used for approximate user location.

#### Example

```properties
WebSearchTool.type=web_search_preview
WebSearchTool.city=Prague
WebSearchTool.country=CZ
WebSearchTool.region=Prague
```

Or use the provider alias:

```properties
WebSearchTool.type=default
```

#### When to use it

Use web search when the model should access current public information from the web instead of relying only on its internal training knowledge.

## MCP Servers

The `addMcpServer(String name, String url, String authorization, String description)` method on `OpenAIProvider` registers an MCP server as an OpenAI MCP tool.

### How MCP server loading works

The base implementation in `AbstractAIProvider.addMcpServers()` looks for configuration groups in this order:

- `MCP.*` for the first server,
- `MCP_1.*` for the second server,
- `MCP_2.*` and higher for additional servers.

For each group:

- `.url` provides the MCP endpoint,
- `.name` provides the visible server label,
- `.authorization` is optional,
- `.description` is optional.

A server is registered when the group has a non-null `.name` value.

### How `addMcpServer(...)` behaves

- It creates an OpenAI MCP tool builder.
- `name` is mapped to `serverLabel`.
- `url` is mapped to `serverUrl`.
- `description` is mapped to `serverDescription` when present.
- `authorization` is attached when present.
- The resulting MCP tool is stored in the provider tool map.

### Configuration properties for the first MCP server

- `MCP.url`
- `MCP.name`
- `MCP.description`
- `MCP.authorization`

### Configuration properties for additional MCP servers

The provider also supports numbered groups such as:

- `MCP_1.url`
- `MCP_1.name`
- `MCP_1.description`
- `MCP_1.authorization`
- `MCP_2.url`
- `MCP_2.name`
- `MCP_2.description`
- `MCP_2.authorization`

Each numbered group with a non-null `.name` can register another MCP server.

### Property reference

- `MCP.url`: URL of the first MCP server endpoint.
- `MCP.name`: label of the first MCP server. This is the value passed into `addMcpServer(...)` and mapped to the OpenAI MCP `serverLabel`.
- `MCP.description`: optional description for the first MCP server.
- `MCP.authorization`: optional authorization value attached to the MCP server definition.
- `MCP_1.url`, `MCP_2.url`, and higher: endpoint URLs for additional MCP servers.
- `MCP_1.name`, `MCP_2.name`, and higher: labels for additional MCP servers.
- `MCP_1.description`, `MCP_2.description`, and higher: optional descriptions for additional MCP servers.
- `MCP_1.authorization`, `MCP_2.authorization`, and higher: optional authorization values for additional MCP servers.

### Example for one MCP server

```properties
MCP.url=https://example.org/mcp
MCP.name=Project MCP
MCP.description=MCP server for project-specific tools
MCP.authorization=Bearer your-token
```

### Example for multiple MCP servers

```properties
MCP.url=https://example.org/mcp
MCP.name=Primary MCP
MCP.description=Primary project tools
MCP.authorization=Bearer primary-token

MCP_1.url=https://example.org/mcp-admin
MCP_1.name=Admin MCP
MCP_1.description=Administrative MCP tools
MCP_1.authorization=Bearer admin-token
```

### When to use it

Use MCP integration when the provider should expose tools from external Model Context Protocol servers instead of implementing those tools directly in the local Java process.

## Host-managed Java tools

Host-managed Java-backed tools can be added either through the annotation-based SPI or programmatically.

### Annotation-based registration

The preferred approach is to implement `FunctionTools`, annotate methods with `@Tool`, and let the provider register them.

```java
provider.addTools(new MyFunctionTools());
provider.addPrompts(new MyFunctionTools());
```

In practice, `FunctionToolsLoader` usually handles this automatically for discovered implementations.

The provider scans public methods on the instance, finds those annotated with `@Tool`, generates parameter descriptors from `@Param` annotations, and registers each one. Methods annotated with `@Prompt` are registered as prompts in the same setup flow.

### Programmatic registration

For situations where annotation-based registration is not suitable, the provider exposes an explicit API:

```java
addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc)
```

In `OpenAIProvider`, `addTool(...)` converts `ParamDescriptor` entries into an object-style JSON schema and creates an OpenAI `FunctionTool`.

The generated parameter schema includes:

- a `properties` object built from parameter descriptors,
- a top-level `type` value of `object`,
- and a `required` array for parameters whose `isRequired()` returns `true`.

Parameters whose name equals `project_dir` are excluded from the schema and are injected by the provider at runtime instead.

The tool is created with `strict(false)` and stored together with its `ToolFunction` callback.

### Runtime invocation flow in `OpenAIProvider`

When the model calls a host-managed function tool in `OpenAIProvider`:

1. The provider receives the tool call from the OpenAI response.
2. The JSON arguments are parsed into a Jackson `JsonNode`.
3. The provider searches registered tools by normalized function name.
4. The matching `ToolFunction` is invoked with parsed parameters, the current `projectDir`, and the provider `Configurator`.
5. The returned value is serialized if necessary and attached as function output.
6. The provider sends a follow-up request so the model can continue using the tool result.

If JSON argument parsing fails, the provider throws an `IllegalArgumentException`.

## How to create a custom functional tool

To create a custom functional tool, implement `FunctionTools`, annotate your methods, register the implementation through Java `ServiceLoader`, and apply it during provider setup.

### Step 1: Create a tool bundle

```java
package com.example.tools;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Tool;

public class ExampleFunctionTools implements FunctionTools {

    @Tool(name = "example_tool", description = "Processes an input value and returns a simple response.")
    public String exampleTool(
            @Param(name = "input", description = "Text value to process") String input,
            Configurator config) {
        String prefix = config != null ? config.get("example.prefix", "") : "";
        return prefix + (input != null ? input : "");
    }
}
```

### Step 2: Register the implementation with `ServiceLoader`

Create this file:

`src/main/resources/META-INF/services/org.machanism.machai.ai.tools.FunctionTools`

Add the fully qualified class name:

```text
com.example.tools.ExampleFunctionTools
```

If the file contains multiple class names, all of them can be discovered and applied.

### Step 3: Apply tools during provider setup

```java
Genai provider = ...;
Class<?> appClass = MyProcessor.class;

FunctionToolsLoader loader = new FunctionToolsLoader();
loader.applyTools(provider, appClass);
```

### Step 4: Optionally add prompts

If your bundle should contribute reusable prompts, add public methods annotated with `@Prompt`.

```java
@Prompt(name = "example_prompt", description = "Reusable helper prompt", role = Role.ASSISTANT)
public String examplePrompt() {
    return "Follow the project conventions and keep the answer concise.";
}
```

### Step 5: Design the tool carefully

When creating a custom tool, follow these recommendations:

- use a short, stable tool name,
- write a description that clearly explains the tool purpose,
- annotate parameters with accurate descriptions,
- use `defaultValue` on `@Param` for optional parameters,
- use the reserved `project_dir` parameter name when the tool needs the provider working directory path,
- use an unannotated `Configurator` parameter when the tool needs runtime configuration,
- use an unannotated `File` parameter when the tool needs direct access to the current project directory object,
- return simple structured output when possible,
- register the implementation through `META-INF/services` so it can be discovered automatically,
- and apply security restrictions before exposing file, network, or command capabilities.

### Step 6: Restrict the tool when necessary

Use `@SupportedFor` when a tool bundle should be active only for specific application classes.

```java
@SupportedFor({ ActProcessor.class })
public class ActSpecFunctionTools implements FunctionTools {
    // ...
}
```

If `@SupportedFor` is omitted, the tool bundle is treated as globally compatible.

## Choosing the right functional tool approach

Use this quick guide:

- Choose `FunctionTools` + `@Tool` when you want local Java methods exposed as provider tools.
- Choose `@Prompt` when you want reusable prompt fragments registered beside tools.
- Choose OpenAI web search when the model needs current public web information.
- Choose MCP servers when the capabilities already exist in an external MCP-compatible service.
- Choose programmatic `addTool(...)` when tool registration metadata is easier to build in code than through annotations.
