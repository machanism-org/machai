---
<!-- @guidance: 
Create or update the `Function Tolls` page:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
- Analyze classes in the folder: `src/main/java/org/machanism/machai/ai/tools`.
- Analize methods: addMcpServer() and addWebSearch() in the class `/src/main/java/org/machanism/machai/ai/provider/openai/OpenAIProvider.java` and describe toml act configuration properties for use it.
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

This feature keeps tool registration modular. Tool discovery and registration are handled by the SPI in `org.machanism.machai.ai.tools`, while OpenAI-native tool support is implemented in `OpenAIProvider`.

## Feature overview

Functional tools provide a structured way to:

- expose controlled application capabilities to the model,
- group related tools into reusable installer classes,
- discover tool installers automatically through Java `ServiceLoader`,
- execute Java methods annotated with `@Function` and `@Param`,
- enable OpenAI web search from configuration,
- and connect one or more external MCP servers.

This separation makes the system easier to maintain. Tool logic stays in focused Java classes, while provider-specific registration details stay inside the provider implementation.

## Package: `org.machanism.machai.ai.tools`

The package `org.machanism.machai.ai.tools` contains the host-side SPI and runtime contracts used to contribute functional tools.

### `FunctionTools`

`FunctionTools` is the service-provider interface for contributing host-managed tools to a `Genai` provider.

#### Purpose

Implement this interface when you want to contribute a reusable bundle of related tools. Each public method annotated with `@Function` is automatically registered as a callable tool. A single implementation can register one tool or many tools.

#### How it behaves

A `FunctionTools` implementation is typically discovered from the classpath through Java `ServiceLoader`. The framework scans public methods on the implementation class for `@Function` annotations, converts each annotated method into a tool registration, and adds it to the provider.

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

It scans the classpath with Java `ServiceLoader`, collects tool installers, creates a fresh instance of each installer class, and then applies the tools to the target `Genai` instance.

#### Main behavior

- The constructor loads available `FunctionTools` implementations from the classpath.
- `applyTools(Genai provider, Configurator configurator, Class<?> appClass)` iterates over the discovered installers.
- For each discovered installer class, it creates a new instance.
- The `@SupportedFor` annotation is checked; only installers compatible with `appClass` are applied.
- Each new instance is registered by calling `provider.addTool(newInstance)`.

#### Why a fresh instance matters

The loader does not reuse the instance returned by `ServiceLoader` for registration. Instead, it creates a new instance for each discovered installer class. That helps keep provider setup isolated and avoids leaking state between registrations.

#### Good use cases

Use `FunctionToolsLoader` during provider initialization when all available tool bundles on the classpath should be activated automatically.

### `ToolFunction`

`ToolFunction` is the functional callback contract for executable host-managed tool logic.

#### Method contract

```java
Object apply(JsonNode params, File projectDir, Configurator config) throws IOException;
```

#### Parameters

- `params` contains the parsed tool arguments as structured JSON.
- `projectDir` provides the current provider working directory context and may be `null`.
- `config` provides access to the runtime `Configurator`.

#### Return value and errors

- The returned object becomes the tool result sent back through the provider.
- `IOException` can be thrown when tool execution fails.

#### Good use cases

Use `ToolFunction` directly when you need full control over tool execution logic and want to work with raw JSON parameters. For most cases, the annotation-based approach with `@Function` and `@Param` is preferred.

### `@Function`

`@Function` is a method-level annotation that marks a public method on a `FunctionTools` implementation as a callable tool.

#### Attributes

- `name`: the tool name passed to the provider and visible to the model.
- `description`: human-readable description of what the tool does.

#### Example

```java
@Function(name = "read_file", description = "Reads the content of a file.")
public String readFile(@Param(name = "path", description = "File path to read") String path) {
    // ...
}
```

### `@Param`

`@Param` is a parameter-level annotation that describes a method parameter for tool schema generation.

#### Attributes

- `name`: the parameter name as seen by the model.
- `description`: human-readable description of the parameter.
- `defaultValue`: optional default value. If omitted, the parameter is treated as required.

The special parameter name `project_dir` is reserved. When a parameter is named `project_dir`, the provider injects the current working directory rather than reading it from the model's tool call arguments.

#### Type mapping

Java types are mapped to JSON schema types automatically:

- `String` and `File` map to `string`,
- `int` and `Integer` map to `integer`,
- `boolean` and `Boolean` map to `boolean`.

#### Unannotated parameters

Method parameters without `@Param` can still receive injected values. If the parameter type is `Configurator`, the runtime configurator is injected. If the parameter type is `File`, the project directory is injected.

### `ParamDescriptor`

`ParamDescriptor` carries structured metadata for a single tool parameter: name, type, required flag, and description. It is produced by the framework when processing `@Param` annotations and passed to `addTool(...)` to build the OpenAI JSON schema.

### `SupportedFor`

See the `@SupportedFor` annotation section below.

## How functional tools work

A typical lifecycle looks like this:

1. Create one or more classes that implement `FunctionTools`.
2. Annotate public methods with `@Function` and their parameters with `@Param`.
3. Register those classes with Java `ServiceLoader`.
4. Create and initialize the AI provider.
5. Call `FunctionToolsLoader.applyTools(provider, configurator, appClass)`.
6. The loader creates a fresh instance of each compatible installer.
7. `provider.addTool(instance)` scans for `@Function` methods and registers each one.
8. When the model invokes a tool, the provider resolves the matching method by tool name.
9. The method is invoked with arguments extracted from the model's call and any injected values.
10. The return value is sent back through the provider so the response can continue.

This design keeps tool registration modular, discoverable, and easy to package.

## OpenAI provider integration

`OpenAIProvider` supports three tool styles:

- host-managed function tools through `addTool(FunctionTools)` (annotation-based) or `addTool(String, String, ToolFunction, ParamDescriptor...)` (programmatic),
- OpenAI-native web search through `addWebSearch(...)`,
- OpenAI-native MCP server tools through `addMcpServer(...)`.

At request time, the provider sends the registered tools to the OpenAI Responses API. If the model issues a function call for a host-managed tool, `OpenAIProvider` parses the JSON arguments, invokes the matching `ToolFunction`, records the tool output, and submits a follow-up request until a final answer is produced.

## OpenAI web search

The method `addWebSearch(String type, String city, String country, String region)` enables the built-in OpenAI web search tool.

### How it works

`OpenAIProvider` builds a `WebSearchTool` and sets its tool type from the configured `type` value.

There is one compatibility rule in the implementation:

- if `type` matches the provider default web-search marker, it is converted to `web_search_preview` before the tool is registered.

The method also creates a `UserLocation` object and always sets the location type to `APPROXIMATE`.

If present, the following optional values are added to the approximate user location:

- `city`,
- `country`,
- `region`.

The completed `WebSearchTool` is then wrapped as an OpenAI `Tool` and stored in the provider tool map.

### ACT TOML configuration properties

Web search is enabled when `WebSearchTool.type` is configured.

- `WebSearchTool.type`
- `WebSearchTool.city`
- `WebSearchTool.country`
- `WebSearchTool.region`

### Property reference

- `WebSearchTool.type`: required to enable web search. Defines the OpenAI web-search tool type.
- `WebSearchTool.city`: optional city used for approximate user location.
- `WebSearchTool.country`: optional country used for approximate user location.
- `WebSearchTool.region`: optional region or state used for approximate user location.

### TOML example

```toml
WebSearchTool.type = "web_search_preview"
WebSearchTool.city = "Prague"
WebSearchTool.country = "CZ"
WebSearchTool.region = "Prague"
```

### When to use it

Use web search when the model should be able to retrieve current public web information instead of relying only on model knowledge.

## OpenAI MCP servers

The method `addMcpServer(String name, String url, String authorization, String description)` registers a single MCP server tool entry.

### How it works

`OpenAIProvider` creates a `Tool.Mcp` builder and maps method arguments to OpenAI MCP tool fields as follows:

- `name` -> `serverLabel`
- `url` -> `serverUrl`
- `description` -> `serverDescription` when present
- `authorization` -> `authorization` when present

The resulting MCP definition is wrapped as an OpenAI `Tool` and stored in the provider tool map.

At configuration level, the provider supports one base MCP group and additional indexed MCP groups, so multiple MCP servers can be registered.

### ACT TOML configuration properties for the first MCP server

- `MCP.url`
- `MCP.name`
- `MCP.description`
- `MCP.authorization`

### ACT TOML configuration properties for additional MCP servers

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
- `MCP.name`: label of the first MCP server shown to the provider and model tooling layer.
- `MCP.description`: optional description for the first MCP server.
- `MCP.authorization`: optional authorization value sent with the first MCP server definition.
- `MCP_1.url`, `MCP_2.url`, and higher: endpoint URLs for additional MCP servers.
- `MCP_1.name`, `MCP_2.name`, and higher: labels for additional MCP servers.
- `MCP_1.description`, `MCP_2.description`, and higher: optional descriptions for additional MCP servers.
- `MCP_1.authorization`, `MCP_2.authorization`, and higher: optional authorization values for additional MCP servers.

### TOML example for one MCP server

```toml
MCP.url = "https://example.org/mcp"
MCP.name = "Project MCP"
MCP.description = "MCP server for project-specific tools"
MCP.authorization = "Bearer your-token"
```

### TOML example for multiple MCP servers

```toml
MCP.url = "https://example.org/mcp"
MCP.name = "Primary MCP"
MCP.description = "Primary project tools"
MCP.authorization = "Bearer primary-token"

MCP_1.url = "https://example.org/mcp-admin"
MCP_1.name = "Admin MCP"
MCP_1.description = "Administrative MCP tools"
MCP_1.authorization = "Bearer admin-token"
```

### When to use it

Use MCP integration when the provider should expose tools from external Model Context Protocol servers instead of implementing those tools directly in the local Java process.

## Host-managed function tools with `addTool(...)`

Host-managed Java-backed tools can be added either through the annotation-based API (preferred) or programmatically.

### Annotation-based registration

The recommended way to register tools is by implementing `FunctionTools`, annotating methods with `@Function`, and letting `AbstractAIProvider.addTool(FunctionTools)` do the rest.

```java
provider.addTool(new MyFunctionTools());
```

The provider scans all public methods on the instance, finds those annotated with `@Function`, generates the JSON schema from `@Param` annotations, and registers each one.

### Programmatic registration

For cases where annotation-based registration is not suitable, `OpenAIProvider.addTool(...)` accepts explicit parameter descriptors:

```java
addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc)
```

`OpenAIProvider.addTool(...)` converts `ParamDescriptor` entries into an object-style JSON schema definition, creates an OpenAI `FunctionTool`, and stores that tool together with its `ToolFunction` callback.

The generated parameter definition includes:

- a `properties` object built from parameter descriptors,
- a top-level `type` value of `object`,
- and a `required` array for parameters whose `isRequired()` returns `true`.

The tool is created with `strict(false)` and then stored in the provider tool map.

### Runtime invocation flow

When the model calls a host-managed function tool:

1. `OpenAIProvider` receives the tool call from the OpenAI response.
2. The provider parses the JSON arguments into a `JsonNode`.
3. The provider searches registered function tools by normalized function name.
4. The matching `ToolFunction` is invoked with the parsed parameters, current `projectDir`, and `Configurator`.
5. The returned value is attached as function output.
6. The provider sends a follow-up request so the model can continue using the tool result.

If JSON argument parsing fails, the provider throws an `IllegalArgumentException`.

## How to create a custom functional tool

To create a custom functional tool, implement `FunctionTools`, annotate your tool methods with `@Function` and `@Param`, register the implementation through Java `ServiceLoader`, and apply it during provider setup.

### Step 1: Create a tool installer

```java
package com.example.tools;

import java.io.File;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Function;
import org.machanism.machai.ai.tools.Param;

public class ExampleFunctionTools implements FunctionTools {

    @Function(name = "example_tool", description = "Processes an input value and returns a simple response.")
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
Configurator configurator = ...;
Genai provider = ...;
Class<?> appClass = MyProcessor.class;

FunctionToolsLoader loader = new FunctionToolsLoader();
loader.applyTools(provider, configurator, appClass);
```

### Step 4: Design the tool carefully

When creating a custom tool, follow these recommendations:

- use a short, stable tool name,
- write a description that clearly explains the tool purpose,
- annotate parameters with accurate descriptions,
- use `defaultValue` on `@Param` to make optional parameters optional in the schema,
- use the reserved `project_dir` parameter name when the tool needs the working directory,
- use an injected `Configurator` parameter to access runtime configuration instead of hard-coding values,
- return simple structured output when possible,
- and apply security restrictions before exposing file, network, or command capabilities.

## `@SupportedFor` Annotation

The `@SupportedFor` annotation allows you to specify which application classes a `FunctionTools` implementation is compatible with. This helps ensure that tools are only registered for appropriate contexts, making your tool bundles more modular and preventing accidental misuse.

### Purpose

Use `@SupportedFor` to restrict a tool installer to specific processor or provider classes. If the annotation is absent, the tool is considered compatible with all application classes.

### How it works

- Annotate your `FunctionTools` class with `@SupportedFor`, passing one or more class types.
- During tool registration, `FunctionToolsLoader` checks this annotation and only applies the tool if the current `appClass` is assignable to one of the supported types.

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

`FunctionToolsLoader` automatically checks for `@SupportedFor` and applies each tool installer only if the current application class is compatible. The check uses `isAssignableFrom`, so subclasses of a listed type are also accepted.

**Tip:**  
Use `@SupportedFor` to make your tool bundles more robust and context-aware, especially in larger projects with multiple processor types.


## Choosing the right approach

- Use `FunctionTools` with `@Function` and `@Param` to define a reusable, annotation-driven installer for one or more tools.
- Use `FunctionToolsLoader` to discover and apply all installers from the classpath, filtered by `appClass`.
- Use `ToolFunction` for the executable logic of an individual host-managed tool when the programmatic API is needed.
- Use `OpenAIProvider.addTool(FunctionTools)` to register an annotation-based tool bundle directly.
- Use `OpenAIProvider.addTool(name, description, function, paramsDesc...)` when you need full programmatic control over tool registration.
- Use `addWebSearch(...)` when OpenAI web search should be available.
- Use `addMcpServer(...)` when external MCP servers should be attached.
