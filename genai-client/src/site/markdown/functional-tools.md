---
<!-- @guidance: 
Create or update the `Function Tolls` page:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
- Analyze classes in the folder: `src/main/java/org/machanism/machai/ai/tools`.
- Analize methods: addMcpServer() and addWebSearch() in the class `/src/main/java/org/machanism/machai/ai/provider/openai/OpenAIProvider.java` and describe toml acl configuration properties for use it.
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
- execute Java callbacks with structured JSON parameters,
- enable OpenAI web search from configuration,
- and connect one or more external MCP servers.

This separation makes the system easier to maintain. Tool logic stays in focused Java classes, while provider-specific registration details stay inside the provider implementation.

## Package: `org.machanism.machai.ai.tools`

The package `org.machanism.machai.ai.tools` contains the host-side SPI and runtime contracts used to contribute functional tools.

### `FunctionTools`

`FunctionTools` is the service-provider interface for installing one or more host-managed tools into a `Genai` provider.

#### Purpose

Implement this interface when you want to contribute a reusable bundle of related tools. A single implementation can register one tool or many tools.

#### Main methods

- `applyTools(Genai provider)` registers the tool set on the target provider.
- `setConfigurator(Configurator configurator)` optionally receives shared runtime configuration before registration. The default implementation does nothing.

#### How it behaves

A `FunctionTools` implementation is typically discovered from the classpath through Java `ServiceLoader`. During setup, the application can pass a `Configurator` into the implementation so it can resolve runtime-dependent values such as URLs, feature flags, credentials, tokens, or naming preferences before calling `applyTools(...)`.

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

It scans the classpath with Java `ServiceLoader`, collects tool installers, creates a fresh instance of each installer class, injects the `Configurator`, and then applies the tools to the target `Genai` instance.

#### Main behavior

- The constructor loads available `FunctionTools` implementations from the classpath.
- `applyTools(Genai provider, Configurator configurator)` iterates over the discovered installers.
- For each discovered installer class, it creates a new instance.
- The new instance receives the shared `Configurator`.
- The new instance then registers its tools through `applyTools(...)`.

#### Why a fresh instance matters

The loader does not reuse the instance returned by `ServiceLoader` for registration. Instead, it creates a new instance for each discovered installer class. That helps keep provider setup isolated and avoids leaking state between registrations.

#### Good use cases

Use `FunctionToolsLoader` during provider initialization when all available tool bundles on the classpath should be activated automatically.

### `ToolFunction`

`ToolFunction` is the functional callback contract for executable host-managed tool logic.

#### Method contract

```java
Object apply(JsonNode params, File workingDir) throws IOException;
```

#### Parameters

- `params` contains the parsed tool arguments as structured JSON.
- `workingDir` provides the current provider working directory context and may be `null`.

#### Return value and errors

- The returned object becomes the tool result sent back through the provider.
- `IOException` can be thrown when tool execution fails.

#### Good use cases

Use `ToolFunction` when the tool needs structured input from the model and optional access to the current project or workspace directory.

## How functional tools work

A typical lifecycle looks like this:

1. Create one or more classes that implement `FunctionTools`.
2. Register those classes with Java `ServiceLoader`.
3. Create and initialize the AI provider.
4. Call `FunctionToolsLoader.applyTools(provider, configurator)`.
5. Each installer registers one or more tools using `Genai.addTool(...)`.
6. When the model invokes a tool, the provider resolves the matching tool by name.
7. The matching `ToolFunction` runs and returns its result to the provider.
8. The provider sends the tool output back to the model so the response can continue.

This design keeps tool registration modular, discoverable, and easy to package.

## OpenAI provider integration

`OpenAIProvider` supports three tool styles:

- host-managed function tools through `addTool(...)`,
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

### TOML ACL configuration properties

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

### TOML ACL configuration properties for the first MCP server

- `MCP.url`
- `MCP.name`
- `MCP.description`
- `MCP.authorization`

### TOML ACL configuration properties for additional MCP servers

The provider also supports numbered groups such as:

- `MCP_1.url`
- `MCP_1.name`
- `MCP_1.description`
- `MCP_1.authorization`
- `MCP_2.url`
- `MCP_2.name`
- `MCP_2.description`
- `MCP_2.authorization`

Each numbered group that defines `.url` can be used to register another MCP server.

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

Host-managed Java-backed tools are added through the provider method:

```java
addTool(String name, String description, ToolFunction function, String... paramsDesc)
```

### What `addTool(...)` does

`OpenAIProvider.addTool(...)` converts parameter descriptor strings into an object-style JSON schema definition, creates an OpenAI `FunctionTool`, and stores that tool together with its `ToolFunction` callback.

The generated parameter definition includes:

- a `properties` object built from parameter descriptors,
- a top-level `type` value of `object`,
- and a `required` array for parameters marked as required.

The tool is created with `strict(false)` and then stored in the provider tool map.

### Parameter descriptor format

Each parameter descriptor string follows this format:

```text
name:type:required:description
```

Examples:

```text
path:string:required:File path to read
limit:integer:optional:Maximum number of items
```

Any parameter whose third segment is `required` is added to the schema `required` list.

### Runtime invocation flow

When the model calls a host-managed function tool:

1. `OpenAIProvider` receives the tool call from the OpenAI response.
2. The provider parses the JSON arguments into a `JsonNode`.
3. The provider searches registered function tools by normalized function name.
4. The matching `ToolFunction` is invoked with the parsed parameters and current `workingDir`.
5. The returned value is attached as function output.
6. The provider sends a follow-up request so the model can continue using the tool result.

If JSON argument parsing fails, the provider throws an `IllegalArgumentException`.

## How to create a custom functional tool

To create a custom functional tool, implement `FunctionTools`, register the implementation through Java `ServiceLoader`, and add one or more tools in `applyTools(...)`.

### Step 1: Create a tool installer

```java
package com.example.tools;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;

public class ExampleFunctionTools implements FunctionTools {

    private Configurator configurator;

    @Override
    public void setConfigurator(Configurator configurator) {
        this.configurator = configurator;
    }

    @Override
    public void applyTools(Genai provider) {
        provider.addTool(
            "example_tool",
            "Processes an input value and returns a simple response.",
            (JsonNode params, File workingDir) -> {
                String input = params != null && params.has("input") ? params.get("input").asText() : "";
                String prefix = configurator != null ? configurator.get("example.prefix", "") : "";
                return prefix + input;
            },
            "input:string:required:Text value to process"
        );
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

FunctionToolsLoader loader = new FunctionToolsLoader();
loader.applyTools(provider, configurator);
```

### Step 4: Design the tool carefully

When creating a custom tool, follow these recommendations:

- use a short, stable tool name,
- write a description that clearly explains the tool purpose,
- define parameter descriptors carefully,
- validate JSON inputs before using them,
- use `workingDir` carefully,
- prefer configuration values over hard-coded environment-specific data,
- return simple structured output when possible,
- and apply security restrictions before exposing file, network, or command capabilities.

## `@SupportedFor` Annotation

The `@SupportedFor` annotation allows you to specify which application classes a `FunctionTools` implementation is compatible with. This helps ensure that tools are only registered for appropriate contexts, making your tool bundles more modular and preventing accidental misuse.

### Purpose

Use `@SupportedFor` to restrict a tool installer to specific processor or provider classes. If the annotation is absent, the tool is considered compatible with all application classes.

### How it works

- Annotate your `FunctionTools` class with `@SupportedFor`, passing one or more class types.
- During tool registration, the loader checks this annotation and only applies the tool if the current application class matches one of the supported types.

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

The `FunctionToolsLoader` automatically checks for `@SupportedFor` and applies each tool installer only if the current application class is compatible.

**Tip:**  
Use `@SupportedFor` to make your tool bundles more robust and context-aware, especially in larger projects with multiple processor types.


## Choosing the right approach

- Use `FunctionTools` to define a reusable installer for one or more tools.
- Use `FunctionToolsLoader` to discover and apply all installers from the classpath.
- Use `ToolFunction` for the executable logic of an individual host-managed tool.
- Use `OpenAIProvider.addTool(...)` for local Java-backed function tools.
- Use `addWebSearch(...)` when OpenAI web search should be available.
- Use `addMcpServer(...)` when external MCP servers should be attached.

