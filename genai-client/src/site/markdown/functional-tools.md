---
<!-- @guidance: 
Create the `Function Tolls` page:
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

Functional tools let the host application expose controlled local capabilities to a `Genai` provider as callable tools. They create the bridge between the model and application-side actions such as file access, HTTP calls, command execution, or project-specific automation.

The feature combines the tool SPI in `org.machanism.machai.ai.tools` with OpenAI-specific native tool support in `OpenAIProvider`. Together, they allow the application to discover tool installers, resolve configuration values, register host-managed function tools, and optionally enable OpenAI-native web search and MCP servers from TOML configuration.

## What the feature provides

Functional tools provide a structured way to:

- contribute reusable tool sets,
- discover tool installers through Java `ServiceLoader`,
- execute tool logic through a common callback contract,
- resolve configuration-driven values during tool setup,
- register host-managed tools through `Genai.addTool(...)`,
- enable OpenAI web search,
- and connect the OpenAI provider to one or more MCP servers.

This separation keeps provider communication concerns inside the provider implementation while tool behavior stays inside dedicated Java classes.

## Core package: `org.machanism.machai.ai.tools`

The package contains the main extension points used to publish functional tools.

### `FunctionTools`

`FunctionTools` is the main service-provider interface for installing tools into a provider.

#### Purpose

Each implementation represents a tool set installer. Inside `applyTools(Genai provider)`, it registers one or more tools that the provider can expose to the model.

#### Main members

- `applyTools(Genai provider)` registers the tools for one implementation.
- `setConfigurator(Configurator configurator)` optionally receives shared configuration before registration.
- `replace(String value, Configurator conf)` resolves `${...}` placeholders using configuration values.

#### Placeholder resolution behavior

The default `replace(...)` implementation:

- returns the original value if the input or configurator is `null`,
- scans the string for `${...}` placeholders,
- replaces only placeholders that have a matching configuration key,
- keeps unresolved placeholders unchanged,
- and repeats resolution for up to 10 passes to support chained values.

This is useful when a tool installer needs configurable URLs, headers, tokens, labels, or other deployment-specific values.

#### When to use it

Implement `FunctionTools` when you want to contribute a related set of host-managed capabilities, for example:

- file tools,
- HTTP utilities,
- command execution helpers,
- repository automation,
- or domain-specific integration tools.

### `FunctionToolsLoader`

`FunctionToolsLoader` is the runtime loader responsible for discovering and applying all available `FunctionTools` implementations.

#### Purpose

It uses Java `ServiceLoader` to scan the classpath, collect all tool installers, optionally propagate configuration into them, and then apply them to a `Genai` provider.

#### Main members

- `getInstance()` returns the singleton loader.
- `setConfiguration(Configurator configurator)` passes shared configuration to all discovered installers.
- `applyTools(Genai provider)` invokes `applyTools(...)` on each discovered installer.

#### Runtime behavior

Discovery happens when the singleton loader is initialized. Every implementation available through `META-INF/services` can then be configured and applied in discovery order.

#### When to use it

Use `FunctionToolsLoader` during provider initialization when all available tool installers on the classpath should be activated automatically.

### `ToolFunction`

`ToolFunction` is the functional interface for executable host-managed tool logic.

#### Method contract

```java
Object apply(JsonNode params, File workingDir) throws IOException;
```

#### Parameters

- `params` contains the tool arguments as structured JSON.
- `workingDir` provides the current working directory context for the tool invocation.

#### Return value and errors

- The returned object becomes the tool result that is sent back through the provider.
- `IOException` can be thrown when execution fails.

#### When to use it

Use `ToolFunction` to implement the actual behavior of a functional tool, especially when the tool receives structured JSON input and may need access to the current project folder.

## How functional tools work

A typical flow looks like this:

1. Create one or more classes that implement `FunctionTools`.
2. Register those classes with Java `ServiceLoader`.
3. Create and initialize the AI provider.
4. Pass configuration into `FunctionToolsLoader`.
5. Apply all discovered tools to the provider.
6. When the model calls a registered tool, the provider executes the matching `ToolFunction`.
7. The tool output is returned to the model as part of the response loop.

This design keeps registration modular and makes tool sets easy to package, test, and reuse.

## OpenAI provider support

`OpenAIProvider` supports two kinds of tools:

- host-managed function tools added through `addTool(...)`,
- and OpenAI-native tools added by `addWebSearch()` and `addMcpServer()`.

The `init(Configurator config)` method loads normal provider settings, then calls both native-tool methods so they can register any configured OpenAI-native tools immediately.

## OpenAI web search

The method `addWebSearch()` enables the OpenAI web search tool when the corresponding configuration is present.

### How it works

The method reads these properties from the provider `Configurator`:

- `WebSearchTool.type`
- `WebSearchTool.city`
- `WebSearchTool.country`
- `WebSearchTool.region`

If `WebSearchTool.type` is configured, `OpenAIProvider` creates a `WebSearchTool`, sets its type, creates an approximate `UserLocation`, and adds the tool to the provider tool map. The location object is always created when web search is enabled, while `city`, `country`, and `region` are included only when provided.

### TOML configuration

```toml
WebSearchTool.type = "web_search_preview"
WebSearchTool.city = "Prague"
WebSearchTool.country = "CZ"
WebSearchTool.region = "Prague"
```

### Property reference

- `WebSearchTool.type`: required to enable web search; defines the OpenAI web search tool type.
- `WebSearchTool.city`: optional city value added to the approximate user location.
- `WebSearchTool.country`: optional country value added to the approximate user location.
- `WebSearchTool.region`: optional region or state value added to the approximate user location.

### Use case

Enable web search when the model should be able to work with current public web information instead of relying only on training-time knowledge.

## OpenAI MCP servers

The method `addMcpServer()` enables one or more external MCP servers.

### How it works

The method supports a base property group and indexed property groups.

#### Base MCP property group

The first server is read from:

- `MCP.url`
- `MCP.label`
- `MCP.description`
- `MCP.authorization`

If `MCP.url` is present, the provider creates an MCP tool entry and optionally adds label, description, and authorization.

#### Additional MCP server groups

The method then continues scanning indexed groups using this pattern:

- `MCP_1.url`
- `MCP_1.label`
- `MCP_1.description`
- `MCP_1.authorization`
- `MCP_2.url`
- `MCP_2.label`
- `MCP_2.description`
- `MCP_2.authorization`
- and so on.

Each group with a configured `.url` becomes a separate MCP tool registration.

### TOML configuration for one MCP server

```toml
MCP.url = "https://example.org/mcp"
MCP.label = "Project MCP"
MCP.description = "MCP server for project-specific tools"
MCP.authorization = "Bearer your-token"
```

### TOML configuration for multiple MCP servers

```toml
MCP.url = "https://example.org/mcp"
MCP.label = "Primary MCP"
MCP.description = "Primary project tools"
MCP.authorization = "Bearer primary-token"

MCP_1.url = "https://example.org/mcp-admin"
MCP_1.label = "Admin MCP"
MCP_1.description = "Administrative MCP tools"
MCP_1.authorization = "Bearer admin-token"
```

### Property reference

- `MCP.url`: required for the first MCP server; enables that server entry.
- `MCP.label`: optional human-readable label for the first MCP server.
- `MCP.description`: optional description for the first MCP server.
- `MCP.authorization`: optional authorization value for the first MCP server.
- `MCP_1.url`, `MCP_2.url`, ...: required for additional MCP server entries.
- `MCP_1.label`, `MCP_2.label`, ...: optional labels for additional MCP server entries.
- `MCP_1.description`, `MCP_2.description`, ...: optional descriptions for additional MCP server entries.
- `MCP_1.authorization`, `MCP_2.authorization`, ...: optional authorization values for additional MCP server entries.

### Use case

Use MCP support when the OpenAI provider should connect to external Model Context Protocol servers that expose tools outside the local Java process.

## Host-managed function tools with `addTool(...)`

Host-managed tools are added through the provider method:

```java
addTool(String name, String description, ToolFunction function, String... paramsDesc)
```

### What this does

`OpenAIProvider.addTool(...)` converts the parameter descriptions into a JSON Schema object of type `object`, creates an OpenAI `FunctionTool`, and stores the tool together with its `ToolFunction` callback.

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

Any parameter whose third segment is `required` is added to the JSON Schema `required` list.

## How to create a custom functional tool

To create a custom functional tool, implement `FunctionTools`, register the implementation through `ServiceLoader`, and add one or more tools in `applyTools(...)`.

### Step 1: Create the tool installer

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
                String prefix = replace("${example.prefix}", configurator);
                return prefix + input;
            },
            "input:string:required:Text value to process"
        );
    }
}
```

This pattern is the standard approach:

- keep any needed `Configurator` reference,
- register tools in `applyTools(...)`,
- use clear names and descriptions,
- and implement the behavior with a `ToolFunction` lambda or method reference.

### Step 2: Register the implementation

Create this file:

`src/main/resources/META-INF/services/org.machanism.machai.ai.tools.FunctionTools`

Add the fully qualified class name of the implementation:

```text
com.example.tools.ExampleFunctionTools
```

If the file contains multiple class names, all of them can be discovered and applied.

### Step 3: Apply tools at runtime

```java
Configurator conf = ...;
Genai provider = ...;

FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
loader.setConfiguration(conf);
loader.applyTools(provider);
```

### Step 4: Understand runtime invocation

When the model selects a registered function tool:

1. `OpenAIProvider` receives the tool call.
2. The JSON arguments are parsed into a `JsonNode`.
3. The matching tool is found by name.
4. The associated `ToolFunction` is called with the parsed parameters and current working directory.
5. The returned value is sent back as tool output.

If the tool throws an `IOException`, the provider returns a readable error message to the model instead of failing silently.

## Practical guidance

When designing custom tools, follow these recommendations:

- Use short, stable, descriptive tool names.
- Keep each tool focused on one clear responsibility.
- Validate JSON inputs before using them.
- Use the provided `workingDir` carefully.
- Prefer configuration values over hard-coded environment-specific data.
- Use `replace(...)` when configuration values may contain `${...}` placeholders.
- Return simple, structured results that are easy for the model to interpret.
- Handle failures predictably.
- Apply security restrictions before exposing file, network, or command capabilities.

## When to use each part

- Use `FunctionTools` to define a reusable tool installer.
- Use `FunctionToolsLoader` to discover and apply all installers.
- Use `ToolFunction` for the executable logic of each host-managed tool.
- Use `OpenAIProvider.addTool(...)` for local Java-backed function tools.
- Use `addWebSearch()` when OpenAI web search should be available.
- Use `addMcpServer()` when external MCP servers should be attached.

## Summary

Functional tools give `GenAI Client` a consistent way to expose controlled application capabilities to AI providers. The `org.machanism.machai.ai.tools` package handles discovery, configuration, and host-managed tool registration, while `OpenAIProvider` adds OpenAI-native web search and MCP connectivity through TOML configuration. Together, these mechanisms make the platform extensible and practical for real-world AI-assisted workflows.
