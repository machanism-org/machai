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

Functional tools let the host application expose controlled local capabilities to a `Genai` provider as callable tools. They connect model-side tool calls with application-side Java code, built-in OpenAI-native tools, and external MCP servers.

The feature is centered on the SPI in `org.machanism.machai.ai.tools` and the OpenAI-specific implementation in `OpenAIProvider`. Together, these pieces let the application discover tool installers, register Java-backed function tools, optionally enable OpenAI web search, and attach MCP servers through TOML ACL configuration.

## Feature overview

Functional tools provide a structured way to:

- register host-managed Java function tools,
- discover tool installers with Java `ServiceLoader`,
- execute tool logic through a shared callback contract,
- group related tools into reusable installer classes,
- enable OpenAI-native web search,
- and connect the OpenAI provider to external MCP servers.

This design keeps tool registration modular. Provider communication stays inside the provider implementation, while tool behavior remains in focused Java classes.

## Package: `org.machanism.machai.ai.tools`

This package contains the extension points used to publish host-managed functional tools.

### `FunctionTools`

`FunctionTools` is the service-provider interface for installing host-provided function tools into a `Genai` provider.

#### Purpose

Each implementation contributes one or more related tools. In `applyTools(Genai provider)`, it registers those tools on the target provider.

#### Main methods

- `applyTools(Genai provider)` registers all tools contributed by the implementation.
- `setConfigurator(Configurator configurator)` optionally receives shared configuration before registration. The default implementation is a no-op.

#### How it behaves

Implementations are typically discovered through Java `ServiceLoader`. Before tools are applied, the runtime can pass a shared `Configurator` into the installer so it can resolve runtime values such as feature flags, API tokens, URLs, names, or other configuration-dependent settings.

#### Good use cases

Implement `FunctionTools` when you want to contribute a reusable set of related capabilities, such as:

- file tools,
- HTTP tools,
- command-line helpers,
- repository automation,
- or domain-specific integrations.

### `FunctionToolsLoader`

`FunctionToolsLoader` is the runtime bootstrap class responsible for discovering and applying all available `FunctionTools` implementations.

#### Purpose

It loads tool installers from the classpath with Java `ServiceLoader` and applies them to a `Genai` provider.

#### Main behavior

- The constructor discovers `FunctionTools` implementations available on the classpath.
- `applyTools(Genai provider, Configurator configurator)` iterates over the discovered installers, creates a fresh instance of each installer class, passes in the `Configurator`, and calls `applyTools(...)`.

#### Why this matters

This design keeps provider initialization modular. Tool bundles can be packaged independently and activated automatically without hard-coding each installer into provider setup.

#### Good use cases

Use `FunctionToolsLoader` during provider initialization when all available tool installers on the classpath should be activated automatically.

### `ToolFunction`

`ToolFunction` is the functional interface used for executable host-managed tool logic.

#### Method contract

```java
Object apply(JsonNode params, File workingDir) throws IOException;
```

#### Parameters

- `params` contains the tool arguments as structured JSON.
- `workingDir` provides the current working directory for the invocation and may be `null`.

#### Return value and errors

- The returned object becomes the tool result sent back through the provider.
- `IOException` can be thrown when execution fails.

#### Good use cases

Use `ToolFunction` when a tool needs structured JSON input and optional access to the current project directory.

## How functional tools work

A typical lifecycle looks like this:

1. Create one or more classes that implement `FunctionTools`.
2. Register those classes with Java `ServiceLoader`.
3. Create and initialize the AI provider.
4. Call `FunctionToolsLoader.applyTools(provider, configurator)`.
5. Each installer registers one or more tools using `Genai.addTool(...)`.
6. When the model calls a tool, the provider resolves the matching tool by name.
7. The matching `ToolFunction` is executed and its result is returned to the model.

This makes tool registration modular, reusable, and easy to package.

## OpenAI provider integration

`OpenAIProvider` supports three tool styles:

- host-managed function tools added through `addTool(...)`,
- OpenAI-native web search added through `addWebSearch(...)`,
- and OpenAI-native MCP tools added through `addMcpServer(...)`.

During initialization, the provider reads configuration and can register native OpenAI tools immediately.

## OpenAI web search

The method `addWebSearch(String type, String city, String country, String region)` enables the OpenAI web search tool.

### How it works

The method builds a `WebSearchTool` and sets its type from the configured `type` value. It also creates a `UserLocation` and always sets the location type to `APPROXIMATE`.

If values are provided, the method adds:

- `city`,
- `country`,
- and `region`

into the approximate user location. The completed web search tool is then wrapped as an OpenAI `Tool` and stored in the provider tool map.

### TOML ACL configuration properties

Web search is enabled when `WebSearchTool.type` is configured.

- `WebSearchTool.type`
- `WebSearchTool.city`
- `WebSearchTool.country`
- `WebSearchTool.region`

### TOML example

```toml
WebSearchTool.type = "web_search_preview"
WebSearchTool.city = "Prague"
WebSearchTool.country = "CZ"
WebSearchTool.region = "Prague"
```

### Property reference

- `WebSearchTool.type`: required to enable web search. It defines the OpenAI web search tool type.
- `WebSearchTool.city`: optional city value added to the approximate user location.
- `WebSearchTool.country`: optional country value added to the approximate user location.
- `WebSearchTool.region`: optional region or state value added to the approximate user location.

### When to use it

Use web search when the model should be able to retrieve current public web information instead of relying only on training-time knowledge.

## OpenAI MCP servers

The method `addMcpServer(String name, String url, String authorization, String description)` registers a single MCP server tool entry.

### How it works

The method creates a `Tool.Mcp` builder and sets:

- `serverLabel` from `name`,
- `serverUrl` from `url`.

If values are provided, it also sets:

- `authorization`,
- `serverDescription`.

The resulting MCP tool is then wrapped as an OpenAI `Tool` and stored in the provider tool map.

At configuration level, the provider supports one base MCP group and additional indexed MCP groups, so multiple MCP servers can be registered.

### TOML ACL configuration properties for the first MCP server

- `MCP.url`
- `MCP.name`
- `MCP.description`
- `MCP.authorization`

If `MCP.name` is present, the provider registers the first MCP server. The loop continues to numbered groups as long as a `.url` value is found.

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

### Property reference

- `MCP.url`: server endpoint URL for the first MCP server.
- `MCP.name`: required human-readable label for the first MCP server. The server is registered only when this value is present.
- `MCP.description`: optional description for the first MCP server.
- `MCP.authorization`: optional authorization value for the first MCP server.
- `MCP_1.url`, `MCP_2.url`, and higher: server endpoint URLs for each additional MCP server. The discovery loop continues as long as a `.url` value is present.
- `MCP_1.name`, `MCP_2.name`, and higher: required labels for each additional MCP server. Each numbered server is registered only when its `.name` value is present.
- `MCP_1.description`, `MCP_2.description`, and higher: optional descriptions for additional MCP servers.
- `MCP_1.authorization`, `MCP_2.authorization`, and higher: optional authorization values for additional MCP servers.

### When to use it

Use MCP support when the provider should connect to external Model Context Protocol servers that expose tools outside the local Java process.

## Host-managed function tools with `addTool(...)`

Host-managed Java-backed tools are added through the provider method:

```java
addTool(String name, String description, ToolFunction function, String... paramsDesc)
```

### What `addTool(...)` does

`OpenAIProvider.addTool(...)` converts parameter descriptor strings into an object-style schema definition, creates an OpenAI `FunctionTool`, and stores the tool together with its `ToolFunction` callback.

The generated parameter definition includes:

- a `properties` object built from parameter descriptors,
- a top-level `type` value of `object`,
- and a `required` array for parameters marked as required.

The tool is created with `strict(false)` and stored in the provider tool map together with its executable callback.

### Parameter descriptor format

Each descriptor string uses this format:

```text
name:type:required:description
```

Examples:

```text
path:string:required:File path to read
limit:integer:optional:Maximum number of items
```

Any parameter whose third segment is `required` is added to the schema required list.

### Runtime invocation flow

When the model calls a host-managed function tool:

1. `OpenAIProvider` receives the tool call.
2. The JSON arguments are parsed into a `JsonNode`.
3. The provider searches the registered tools by normalized function name.
4. The matching `ToolFunction` is called with the parsed parameters and current `workingDir`.
5. The returned value is added as function output and sent back to the model.

If argument parsing fails, the provider throws an `IllegalArgumentException`. If the tool is not found, the result is `null`. If the tool implementation throws an `IOException`, the provider handles that failure through its execution flow.

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

When creating a custom tool, follow these practical recommendations:

- use a short and stable tool name,
- write a description that clearly explains the tool purpose,
- define parameter descriptors carefully,
- validate JSON inputs before using them,
- use `workingDir` carefully,
- prefer configuration values over hard-coded environment-specific data,
- return simple structured output when possible,
- and apply security restrictions before exposing file, network, or command capabilities.

## Choosing the right approach

- Use `FunctionTools` to define a reusable tool installer.
- Use `FunctionToolsLoader` to discover and apply all installers.
- Use `ToolFunction` for the executable logic of each host-managed tool.
- Use `OpenAIProvider.addTool(...)` for local Java-backed function tools.
- Use `addWebSearch(...)` when OpenAI web search should be available.
- Use `addMcpServer(...)` when external MCP servers should be attached.

## Summary

Functional tools give `GenAI Client` a consistent way to expose controlled application capabilities to AI providers. The `org.machanism.machai.ai.tools` package handles discovery and host-managed tool registration, while `OpenAIProvider` adds OpenAI-native web search and MCP connectivity through TOML ACL configuration. Together, these mechanisms make the platform extensible and practical for real-world AI-assisted workflows.
