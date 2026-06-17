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

Functional tools let the host application expose controlled local and remote capabilities to a `Genai` provider as callable tools. In this project, they cover three integration styles:

- Java-backed host tools registered through the provider API,
- OpenAI-native web search configured directly on `OpenAIProvider`,
- external MCP servers attached as OpenAI MCP tools.

This feature enables modular tool registration. Tool discovery and registration are handled by the SPI in org.machanism.machai.ai.tools, and tool support is implemented in providers.

## Feature overview

Functional tools provide a structured way to:

- expose controlled application capabilities to the model,
- group related tools into reusable installer classes,
- discover tool installers automatically through Java `ServiceLoader`,
- execute Java methods annotated with `@Tool` and `@Param`,
- expose prompt methods annotated with `@Prompt`,
- enable web search from configuration,
- and connect one or more external MCP servers.

This separation makes the system easier to maintain. Tool logic stays in focused Java classes, while provider-specific registration details stay inside the provider implementation.

## Package: `org.machanism.machai.ai.tools`

The package `org.machanism.machai.ai.tools` contains the host-side SPI and runtime contracts used to contribute functional tools.

### `FunctionTools`

`FunctionTools` is the service-provider interface for contributing host-managed tools to a `Genai` provider.

#### Purpose

Implement this interface when you want to contribute a reusable bundle of related tools. Each public method annotated with `@Tool` or `@Prompt` is automatically registered when the instance is passed to `provider.addTools(instance)`. A single implementation can register one tool or many tools.

#### How it behaves

A `FunctionTools` implementation is typically discovered from the classpath through Java `ServiceLoader`. The framework scans public methods on the implementation class for `@Tool` and `@Prompt` annotations, converts each annotated method into a tool or prompt registration, and adds it to the provider.

#### Good use cases

Use `FunctionTools` when you want to package a coherent capability set, for example:

- file operations,
- HTTP access,
- command execution,
- source-control automation,
- or business-specific integrations.

### `FunctionToolsLoader`

`FunctionToolsLoader` is the bootstrap component that discovers all available `FunctionTools` implementations and applies them to a provider.

#### Purpose

It scans the classpath with Java `ServiceLoader`, collects tool installer instances, and then applies compatible ones to the target `Genai` instance.

#### Main behavior

- The constructor loads available `FunctionTools` implementations from the classpath using `ServiceLoader` and retains the discovered instances.
- `applyTools(Genai provider, Class<?> appClass)` iterates over the discovered installer instances.
- The `@SupportedFor` annotation is checked; only installers compatible with `appClass` are applied.
- Each compatible instance is registered by calling `provider.addTools(instance)`.

#### Good use cases

Use `FunctionToolsLoader` during provider initialization when all available tool bundles on the classpath should be activated automatically.

### `@Tool`

`@Tool` is a method-level annotation that marks a public method on a `FunctionTools` implementation as a callable tool.

#### Attributes

- `name`: the tool name passed to the provider and visible to the model. Defaults to the method name when not specified (the sentinel value `Tool.NULL_VALUE` is used internally to detect a missing name).
- `description`: human-readable description of what the tool does.

#### Example

```java
@Tool(name = "read_file", description = "Reads the content of a file.")
public String readFile(@Param(name = "path", description = "File path to read") String path) {
    // ...
}
```

When `name` is omitted, the Java method name is used as the tool name:

```java
@Tool(description = "Returns the current server time.")
public String currentTime() {
    // tool name will be "currentTime"
}
```

### `@Param`

`@Param` is a parameter-level annotation that describes a method parameter for tool schema generation.

#### Attributes

- `name`: the parameter name as seen by the model.
- `description`: human-readable description of the parameter.
- `defaultValue`: optional default value. If omitted, the parameter is treated as required. The sentinel value `Param.NULL_VALUE` (`"___NULL_SENTINEL___"`) is used internally to distinguish a missing default from an explicit empty string.

The special parameter name `project_dir` is reserved. When a `@Param`-annotated parameter is named `project_dir` and the provider has a working directory configured, the provider injects the working directory at runtime and excludes it from the model-visible tool schema. If no working directory is configured, the parameter is included in the schema and the model supplies the value.

#### Type mapping

Java types are mapped to JSON schema types automatically:

- `String` and `File` map to `string`,
- `int` and `Integer` map to `integer`,
- `boolean` and `Boolean` map to `boolean`.

#### Unannotated parameters

Method parameters without `@Param` can still receive injected values. If the parameter type is `Configurator`, the runtime configurator is injected. If the parameter type is `File`, the project directory is injected.

### `@Prompt`

`@Prompt` is a method-level annotation that marks a public method on a `FunctionTools` implementation as a provider-registered prompt.

#### Attributes

- `name`: the prompt name passed to the provider.
- `description`: human-readable description of the prompt's purpose.
- `role`: the conversation role associated with this prompt. Defaults to `Role.ASSISTANT`.

#### Example

```java
@Prompt(name = "summarize_instructions", description = "System-level instruction for document summarization.", role = Role.ASSISTANT)
public String summarizeInstructions() {
    return "You are a helpful assistant that summarizes documents concisely.";
}
```

#### When to use it

Use `@Prompt` when a `FunctionTools` implementation should also contribute reusable system or user prompt text that the provider can register and invoke by name, in addition to or instead of callable tools.

### `Role`

`Role` is an enum used by `@Prompt` to specify the conversation role of a registered prompt method.

#### Values

- `ASSISTANT`: the prompt content is attributed to the assistant role.
- `USER`: the prompt content is attributed to the user role.

### `ParamDescriptor`

`ParamDescriptor` carries structured metadata for a single tool parameter: name, type, required flag, and description. It is produced by the framework when processing `@Tool` annotations and passed to `addTools(...)` to build the JSON schema.

#### Constructor

```java
new ParamDescriptor(String name, String type, boolean required, String description)
```

#### Accessor methods

- `getName()`: returns the parameter name.
- `getType()`: returns the JSON schema type string (e.g., `"string"`, `"integer"`, `"boolean"`).
- `isRequired()`: returns `true` when the parameter has no default value.
- `getDescription()`: returns the parameter description.

### `SupportedFor`

See the `@SupportedFor` annotation section below.

## How functional tools work

A typical lifecycle looks like this:

1. Create one or more classes that implement `FunctionTools`.
2. Annotate public methods with `@Tool` and their parameters with `@Param`.
3. Optionally annotate prompt methods with `@Prompt`.
4. Register those classes with Java `ServiceLoader`.
5. Create and initialize the AI provider.
6. Call `FunctionToolsLoader.applyTools(provider, appClass)`.
7. The loader applies each compatible installer instance discovered at construction time.
8. `provider.addTools(instance)` scans for `@Tool` and `@Prompt` methods and registers each one.
9. When the model invokes a tool, the provider resolves the matching method by tool name.
10. The method is invoked with arguments extracted from the model's call and any injected values.
11. The return value is sent back through the provider so the response can continue.

This design keeps tool registration modular, discoverable, and easy to package.

## Web Search

### Configuration

Web search is enabled automatically during `init(...)` when `WebSearchTool.type` is present in configuration.

- `WebSearchTool.type`
- `WebSearchTool.city`
- `WebSearchTool.country`
- `WebSearchTool.region`

### Property reference

- `WebSearchTool.type`: required to enable web search. Defines the web-search tool type. Use `"default"` to select `web_search_preview` automatically, or supply an explicit type string.
- `WebSearchTool.city`: optional city used for approximate user location.
- `WebSearchTool.country`: optional country used for approximate user location.
- `WebSearchTool.region`: optional region or state used for approximate user location.

### Example

```
WebSearchTool.type = web_search_preview
WebSearchTool.city = Prague
WebSearchTool.country = CZ
WebSearchTool.region = Prague
```

### When to use it

Use web search when the model should be able to retrieve current public web information instead of relying only on model knowledge.

## MCP Servers

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

Each numbered group that defines `.name` can be used to register another MCP server.

### Property reference

- `MCP.url`: URL of the first MCP server endpoint.
- `MCP.name`: label of the first MCP server shown to the provider and model tooling layer. Required to trigger registration of the first server.
- `MCP.description`: optional description for the first MCP server.
- `MCP.authorization`: optional authorization value sent with the first MCP server definition.
- `MCP_1.url`, `MCP_2.url`, and higher: endpoint URLs for additional MCP servers.
- `MCP_1.name`, `MCP_2.name`, and higher: labels for additional MCP servers. Required to trigger registration of each numbered server.
- `MCP_1.description`, `MCP_2.description`, and higher: optional descriptions for additional MCP servers.
- `MCP_1.authorization`, `MCP_2.authorization`, and higher: optional authorization values for additional MCP servers.

### Example for one MCP server

```
MCP.url = https://example.org/mcp
MCP.name = Project MCP
MCP.description = MCP server for project-specific tools
MCP.authorization = Bearer your-token
```

### Example for multiple MCP servers

```
MCP.url = https://example.org/mcp
MCP.name = Primary MCP
MCP.description = Primary project tools
MCP.authorization = Bearer primary-token

MCP_1.url = https://example.org/mcp-admin
MCP_1.name = Admin MCP
MCP_1.description = Administrative MCP tools
MCP_1.authorization = Bearer admin-token
```

### When to use it

Use MCP integration when the provider should expose tools from external Model Context Protocol servers instead of implementing those tools directly in the local Java process.

## Host-managed function tools with `addTools(...)`

Host-managed Java-backed tools can be added either through the annotation-based API (preferred) or programmatically.

### Annotation-based registration

The recommended way to register tools is by implementing `FunctionTools`, annotating methods with `@Tool`, and letting `AbstractAIProvider.addTools(FunctionTools)` do the rest.

```java
provider.addTools(new MyFunctionTools());
```

The provider scans all public methods on the instance, finds those annotated with `@Tool`, generates the JSON schema from `@Param` annotations, and registers each one. Methods annotated with `@Prompt` are also discovered and registered in the same pass.

### Programmatic registration

For cases where annotation-based registration is not suitable, `Provider.addTools(...)` accepts explicit parameter descriptors:

```java
addTools(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc)
```

`Provider.addTools(...)` converts `ParamDescriptor` entries into an object-style JSON schema definition, creates an `FunctionTool`, and stores that tool together with its `ToolFunction` callback.

The generated parameter definition includes:

- a `properties` object built from parameter descriptors,
- a top-level `type` value of `object`,
- and a `required` array for parameters whose `isRequired()` returns `true`.

Parameters whose name equals `PROJECT_DIR_PARAM_NAME` (`"project_dir"`) are excluded from the schema and are injected by the provider at runtime instead.

The tool is created with `strict(false)` and then stored in the provider tool map.

### Runtime invocation flow

When the model calls a host-managed function tool:

1. `Provider` receives the tool call from the response.
2. The provider parses the JSON arguments into a `JsonNode`.
3. The provider searches registered function tools by normalized function name.
4. The matching `ToolFunction` is invoked with the parsed parameters, current `projectDir`, and `Configurator`.
5. The returned value is attached as function output.
6. The provider sends a follow-up request so the model can continue using the tool result.

If JSON argument parsing fails, the provider throws an `IllegalArgumentException`.

## How to create a custom functional tool

To create a custom functional tool, implement `FunctionTools`, annotate your tool methods with `@Tool` and `@Param`, register the implementation through Java `ServiceLoader`, and apply it during provider setup.

### Step 1: Create a tool installer

```java
package com.example.tools;

import java.io.File;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.ai.tools.Param;

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

### Step 4: Design the tool carefully

When creating a custom tool, follow these recommendations:

- use a short, stable tool name,
- write a description that clearly explains the tool purpose,
- annotate parameters with accurate descriptions,
- use `defaultValue` on `@Param` to make optional parameters optional in the schema,
- use the reserved `project_dir` parameter name when the tool needs the working directory injected by the provider,
- use an injected `Configurator` parameter to access runtime configuration instead of hard-coding values,
- return simple structured output when possible,
- and apply security restrictions before exposing file, network, or command capabilities.

## `@SupportedFor` Annotation

The `@SupportedFor` annotation allows you to specify which application classes a `FunctionTools` implementation is compatible with. This helps ensure that tools are only registered for appropriate contexts, making your tool bundles more modular and preventing accidental misuse.

### Purpose

Use `@SupportedFor` to restrict a tool installer to specific processor or provider classes. If the annotation is absent, the tool is considered compatible with all application classes.

### How it works

- Annotate your `FunctionTools` class with `@SupportedFor`, passing one or more class types.
- During tool registration, `FunctionToolsLoader` checks this annotation using `isAssignableFrom` and only applies the tool if the current `appClass` is assignable to one of the supported types. Subclasses of a listed type are therefore also accepted.

### Example

```java
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.gw.processor.ActProcessor;

@SupportedFor({ ActProcessor.class })
public class ActSpecFunctionTools implements FunctionTools {
    // Tool registration logic...
}
```

In this example, `ActSpecFunctionTools` will only be registered for applications of type `ActProcessor`.

### When to use it

- When your tool bundle is only relevant for certain processors or provider types.
- To avoid registering tools in unsupported contexts, improving reliability and clarity.

### Annotation definition

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedFor {
    Class<?>[] value();
}
```

### Loader behavior

`FunctionToolsLoader` automatically checks for `@SupportedFor` and applies each tool installer only if the current application class is compatible.

**Tip:**  
Use `@SupportedFor` to make your tool bundles more robust and context-aware, especially in larger projects with multiple processor types.
