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

Functional tools let the host application expose selected local capabilities to a `Genai` provider as callable tools. They are the integration layer between the AI model and controlled application-side actions such as file access, HTTP calls, command execution, or custom business operations.

The feature is built around the package `org.machanism.machai.ai.tools` and the OpenAI provider support in `OpenAIProvider`. Together, these pieces allow the application to discover tool installers, pass configuration into them, register callable functions, and optionally attach OpenAI-native tools such as web search and MCP servers.

## Feature overview

The functional tools feature provides:

- a standard interface for contributing tool sets,
- a runtime loader based on Java `ServiceLoader`,
- a function contract for executable tool logic,
- configuration placeholder resolution for tool setup,
- registration of host-managed tools through `Genai.addTool(...)`,
- and OpenAI-specific support for native tools configured from TOML properties.

This design keeps provider communication separate from application-specific capabilities. The provider focuses on talking to the model, while tool implementations focus on what the host application can safely do.

## Package `org.machanism.machai.ai.tools`

The package contains the core infrastructure for functional tools.

### `FunctionTools`

`FunctionTools` is the main extension interface for contributing one or more tools to a provider.

#### What it does

A class implementing `FunctionTools` acts as a tool installer. Its job is to register one or more tools with a `Genai` instance inside `applyTools(Genai provider)`.

#### Key methods

- `applyTools(Genai provider)` registers the tools exposed by that implementation.
- `setConfigurator(Configurator configurator)` optionally receives configuration before tool registration.
- `replace(String value, Configurator conf)` resolves `${...}` placeholders using values from the configurator.

#### Important behavior

The default `replace(...)` method performs repeated placeholder resolution for up to 10 passes.

- If a placeholder has a matching configuration value, it is replaced.
- If a placeholder cannot be resolved, it stays unchanged.
- Nested or chained configuration values can be expanded across multiple passes.
- If no configurator is available, the original string is returned unchanged.

This is useful for tool installers that need configurable endpoints, headers, tokens, labels, or other runtime values.

#### Typical use case

Use `FunctionTools` when you want to package a related set of capabilities, such as:

- REST utility tools,
- file system helpers,
- project automation functions,
- command-line wrappers,
- or domain-specific integration tools.

### `FunctionToolsLoader`

`FunctionToolsLoader` is the runtime loader that discovers and applies all available `FunctionTools` implementations.

#### What it does

It uses Java `ServiceLoader` to scan the classpath for implementations of `FunctionTools`, stores them, optionally injects configuration, and applies them to a provider.

#### Key methods

- `getInstance()` returns the singleton loader.
- `setConfiguration(Configurator configurator)` passes shared configuration to all discovered installers.
- `applyTools(Genai provider)` calls `applyTools(...)` on each discovered installer.

#### Important behavior

Discovery happens when the singleton loader is created. Every implementation listed in the Java service registration file can then be configured and applied in discovery order.

#### Typical use case

Use `FunctionToolsLoader` during application startup or provider initialization when you want all functional tool installers on the classpath to be activated automatically.

### `ToolFunction`

`ToolFunction` is the functional interface for the executable logic behind a registered tool.

#### What it does

It represents the callback that is executed when the provider invokes a host-managed function tool.

#### Method contract

```java
Object apply(JsonNode params, File workingDir) throws IOException;
```

#### Parameters and return value

- `params` contains the tool arguments as JSON.
- `workingDir` gives the current working directory context.
- The return value is provider-specific and is typically serialized back to the model.
- `IOException` allows tool execution failures to be reported.

#### Typical use case

Use `ToolFunction` when implementing the actual action performed by a tool, especially when the input is structured JSON and execution may need access to the current project directory.

## How functional tools work

A typical execution flow looks like this:

1. Create one or more classes that implement `FunctionTools`.
2. Register them through Java `ServiceLoader`.
3. Initialize the AI provider.
4. Pass configuration to `FunctionToolsLoader`.
5. Apply all discovered tools to the provider.
6. When the model issues a function call, the provider executes the matching `ToolFunction`.
7. The tool result is returned to the provider and can be fed back into the conversation.

This separation makes tool registration modular, reusable, and easier to maintain.

## OpenAI provider support for native tools

The OpenAI implementation also supports two OpenAI-native tool types configured directly from provider properties:

- web search via `addWebSearch()`,
- and MCP server registration via `addMcpServer()`.

These tools are not registered through `Genai.addTool(...)` callbacks. Instead, they are added to the provider tool list as OpenAI-native tool definitions.

## OpenAI web search configuration

The method `addWebSearch()` reads configuration values from the provider `Configurator` and, when enabled, registers an OpenAI `WebSearchTool`.

### How it works

The method reads these properties:

- `WebSearchTool.type`
- `WebSearchTool.city`
- `WebSearchTool.country`
- `WebSearchTool.region`

If `WebSearchTool.type` is set, the provider creates a web search tool and attaches an approximate user location object. The city, country, and region values are optional and are only added when present.

### TOML configuration properties

Use properties like the following in your TOML configuration:

```toml
WebSearchTool.type = "web_search_preview"
WebSearchTool.city = "Prague"
WebSearchTool.country = "CZ"
WebSearchTool.region = "Prague"
```

### Property reference

- `WebSearchTool.type`: enables the feature and sets the OpenAI web search tool type.
- `WebSearchTool.city`: optional city value for approximate user location.
- `WebSearchTool.country`: optional country value for approximate user location.
- `WebSearchTool.region`: optional region or state value for approximate user location.

### When to use it

Use web search when model responses should be able to incorporate current public web information rather than relying only on the model’s built-in knowledge.

## OpenAI MCP server configuration

The method `addMcpServer()` reads configuration values from the provider `Configurator` and, when enabled, registers an OpenAI MCP tool definition.

### How it works

The method reads these properties:

- `MCP.url`
- `MCP.label`
- `MCP.description`
- `MCP.authorization`

If `MCP.url` is set, the provider creates an MCP tool entry pointing to that server. The label, description, and authorization values are optional and are included only when configured.

### TOML configuration properties

Use properties like the following in your TOML configuration:

```toml
MCP.url = "https://example.org/mcp"
MCP.label = "Project MCP"
MCP.description = "MCP server for project-specific tools"
MCP.authorization = "Bearer your-token"
```

### Property reference

- `MCP.url`: enables MCP support and sets the server URL.
- `MCP.label`: optional human-readable server label.
- `MCP.description`: optional server description.
- `MCP.authorization`: optional authorization value sent to the MCP endpoint.

### When to use it

Use MCP when you want the OpenAI provider to connect to an external Model Context Protocol server that exposes additional tools outside the local Java process.

## How to create a custom functional tool

To create a custom host-managed tool, implement `FunctionTools`, register it for discovery, and add tools through the provider.

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
                String prefix = replace("${example.prefix}", configurator);
                return prefix + input;
            },
            "input:string:required:Text value to process"
        );
    }
}
```

This example shows the most common pattern:

- store the configurator if your tools need runtime values,
- register tools inside `applyTools(...)`,
- define a clear tool name and description,
- and implement the tool body as a `ToolFunction` lambda.

### Step 2: Register the implementation with `ServiceLoader`

Create this service registration file:

`src/main/resources/META-INF/services/org.machanism.machai.ai.tools.FunctionTools`

Add the fully qualified class name of your implementation:

```text
com.example.tools.ExampleFunctionTools
```

If the file contains multiple implementation names, all of them can be discovered and applied.

### Step 3: Apply tools at runtime

```java
Configurator conf = ...;
Genai provider = ...;

FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
loader.setConfiguration(conf);
loader.applyTools(provider);
```

### Step 4: Understand parameter descriptors

The OpenAI provider method `addTool(String name, String description, ToolFunction function, String... paramsDesc)` expects parameter descriptors in this format:

```text
name:type:required:description
```

Example:

```text
input:string:required:Text value to process
limit:integer:optional:Maximum number of items
```

From these descriptors, the provider builds a JSON Schema object for the tool parameters and marks any parameter with `required` as required in the schema.

## Practical design guidance

When building custom tools, follow these recommendations:

- Use short, stable, descriptive tool names.
- Keep each tool focused on a single responsibility.
- Validate JSON input before using it.
- Use the provided `workingDir` carefully and intentionally.
- Prefer configuration over hard-coded environment values.
- Use `replace(...)` when runtime values may contain `${...}` placeholders.
- Return results in a format that the provider and model can interpret easily.
- Handle `IOException` and other failures in a predictable way.
- Apply security controls before exposing file, command, or network access.

## When to choose each mechanism

- Use `FunctionTools` for host-managed Java tools registered through `Genai.addTool(...)`.
- Use `FunctionToolsLoader` to discover and apply all tool installers centrally.
- Use `ToolFunction` for the executable body of a host-managed tool.
- Use `addWebSearch()` when you want the OpenAI provider to expose OpenAI web search.
- Use `addMcpServer()` when you want the OpenAI provider to expose an external MCP server.

## Summary

The functional tools feature gives the `GenAI Client` a structured way to expose controlled capabilities to AI providers. The `org.machanism.machai.ai.tools` package handles discovery, configuration, and registration of host-managed tools, while `OpenAIProvider` adds support for OpenAI-native web search and MCP server tools through TOML configuration. Together, these mechanisms make the platform extensible, configurable, and practical for real-world AI-assisted workflows.
