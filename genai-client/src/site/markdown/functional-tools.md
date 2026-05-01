---
<!-- @guidance: 
Create the `Function Tolls` page:
- Analyze classes in the folder: `src/main/java/org/machanism/machai/ai/tools`.
- Describe the feature.
- Write a general description how to create a custom functional tool.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/genai-client/functional-tools.html
---

# Functional Tools

Functional tools allow the host application to expose selected local capabilities to a `Genai` provider as callable tools. This keeps provider integrations focused on model communication while tool-specific behavior is implemented, discovered, and registered through a dedicated extension mechanism.

These tools are useful when AI-assisted workflows need controlled access to host-side operations such as file handling, HTTP requests, command execution, or other application-defined actions.

## Feature overview

The `org.machanism.machai.ai.tools` package defines the infrastructure for:

- contributing tool installers,
- discovering them automatically with Java `ServiceLoader`,
- optionally injecting runtime configuration,
- and registering executable tool functions with a `Genai` provider.

In practice, this means the application can add or remove tool sets without changing the core provider integration code.

## Package contents

### `FunctionTools`

`FunctionTools` is the main extension interface for contributing one or more functional tools.

#### Purpose

Use this interface when you want to package related tool registrations into a reusable installer that can be discovered automatically.

#### Main responsibilities

- Register tools with a provider through `applyTools(Genai provider)`.
- Optionally accept a `Configurator` through `setConfigurator(Configurator configurator)`.
- Resolve configuration placeholders with `replace(String value, Configurator conf)`.

#### Important behavior

The `replace(...)` helper scans text for `${...}` placeholders and tries to resolve them from the provided `Configurator`.

- If a placeholder can be resolved, the value is substituted.
- If a placeholder cannot be resolved, it remains unchanged.
- Resolution is repeated for multiple passes, which allows nested configuration values to be expanded.
- If no `Configurator` is available, the original value is returned as-is.

#### When to use it

Use `FunctionTools` when you need a clean, reusable way to register one or more host-side tools such as:

- a file utility tool set,
- an internal service integration,
- a project automation tool,
- a command execution helper,
- or any application-specific action exposed to the model.

### `FunctionToolsLoader`

`FunctionToolsLoader` is the runtime loader responsible for discovering and applying all available `FunctionTools` implementations.

#### Purpose

It acts as the central entry point for installing functional tools into a `Genai` provider.

#### Main responsibilities

- Discover `FunctionTools` implementations from the classpath using `ServiceLoader`.
- Expose a singleton instance through `getInstance()`.
- Pass shared configuration to all discovered implementations through `setConfiguration(Configurator configurator)`.
- Apply each discovered tool installer to a provider through `applyTools(Genai provider)`.

#### Important behavior

When the loader is created, it scans the classpath for service implementations of `FunctionTools`. Each discovered implementation is stored and later reused when configuration is set or tools are applied.

#### When to use it

Use `FunctionToolsLoader` during application startup or provider initialization when you want all available tool installers to be discovered and registered automatically.

### `ToolFunction`

`ToolFunction` is the functional interface representing the executable logic behind a registered tool.

#### Purpose

It provides the callable operation that runs when the provider invokes a tool.

#### Main responsibilities

- Accept tool parameters as a Jackson `JsonNode`.
- Receive the current working directory as a `File`.
- Execute the requested host-side operation.
- Return a provider-specific result object.
- Allow failures to be reported through `IOException`.

#### Method shape

A `ToolFunction` implementation uses this contract:

```java
Object apply(JsonNode params, File workingDir) throws IOException;
```

#### When to use it

Use `ToolFunction` when implementing the actual action a tool should perform, especially when tool input is naturally expressed as structured JSON and execution may depend on the working directory.

## How the feature works

The functional tools mechanism typically follows this flow:

1. Create one or more classes that implement `FunctionTools`.
2. Register those classes through Java `ServiceLoader`.
3. Start the application and obtain `FunctionToolsLoader.getInstance()`.
4. Provide configuration with `setConfiguration(...)` if needed.
5. Apply discovered tool installers to the active `Genai` provider with `applyTools(...)`.
6. Let the provider call the registered `ToolFunction` implementations when a tool is invoked.

This design separates tool registration from provider logic and makes local capabilities easier to extend, test, and maintain.

## Typical use cases

Functional tools are a good fit when a model needs controlled access to host-side capabilities such as:

- reading or processing local project files,
- invoking local automation commands,
- calling internal or external HTTP services,
- gathering environment-specific information,
- integrating with application APIs,
- or performing targeted utility actions during a model interaction.

## How to create a custom functional tool

To create a custom functional tool, implement `FunctionTools`, register it for discovery, and add one or more provider tools inside `applyTools(...)`.

### Step 1: Create a `FunctionTools` implementation

```java
package com.example.tools;

import java.io.File;
import java.io.IOException;

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
            "Processes a text value and returns a simple response.",
            (JsonNode params, File workingDir) -> {
                String input = params != null && params.has("input") ? params.get("input").asText() : "";
                return "Processed: " + input;
            },
            "input",
            "Text value to process"
        );
    }
}
```

### Step 2: Register the implementation with `ServiceLoader`

Create the following file:

`src/main/resources/META-INF/services/org.machanism.machai.ai.tools.FunctionTools`

Add the fully qualified class name of your implementation:

```text
com.example.tools.ExampleFunctionTools
```

If the file contains multiple implementation names, each discovered installer can be applied by the loader.

### Step 3: Apply the tools at runtime

```java
Configurator conf = ...;
Genai provider = ...;

FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
loader.setConfiguration(conf);
loader.applyTools(provider);
```

### Step 4: Resolve configuration values when needed

If your tool needs runtime values such as endpoints, tokens, headers, or feature flags, store the provided `Configurator` and use the `replace(...)` helper.

```java
String endpoint = replace("${my.service.url}", configurator);
```

If `${my.service.url}` cannot be resolved, it remains unchanged.

## Practical design guidance

When implementing custom functional tools, follow these recommendations:

- Use stable, descriptive tool names.
- Keep each tool focused on one clear responsibility.
- Write short descriptions that help the provider choose the right tool.
- Validate incoming JSON parameters before using them.
- Use the supplied working directory intentionally and safely.
- Prefer configuration-based values over hard-coded environment details.
- Handle `IOException` and other failures in a predictable way.
- Apply security controls before exposing file, command, or network operations.
- Return results in a format that is easy for the provider and model to interpret.

## Choosing the right type

- Use `FunctionTools` to define and register a reusable set of related tools.
- Use `FunctionToolsLoader` to discover all available tool installers and apply them centrally.
- Use `ToolFunction` to implement the executable body of a single registered tool.

## Summary

The `org.machanism.machai.ai.tools` package provides the extension layer for host-managed tool capabilities in the `GenAI Client`. It enables the application to discover tool installers, inject configuration, register callable functions, and expose controlled local behavior to a `Genai` provider in a structured and extensible way.
