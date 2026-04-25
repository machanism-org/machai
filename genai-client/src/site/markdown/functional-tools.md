---
<!-- @guidance: 
Create the `Function Tolls` page:
- Analyze classes in the folder: `src/main/java/org/machanism/machai/ai/tools`.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/genai-client/functional-tools.html
---

# Functional Tools

Functional tools are host-provided capabilities that can be registered with the GenAI provider and executed locally by the application. They are designed to let an AI workflow interact with the local environment in a controlled way, especially for command execution and HTTP-based access.

The tools in this project are installed through the `org.machanism.machai.ai.tools` package. The main user-facing tools currently come from two tool sets:

- `CommandFunctionTools` for command execution and controlled termination
- `WebFunctionTools` for web page retrieval and REST API calls

## Tool Set Overview

### `CommandFunctionTools`
Provides tools for running shell commands inside the project context and for stopping execution intentionally.

**Main features**
- Runs commands through Java `ProcessBuilder`
- Restricts execution to a working directory inside the project
- Supports custom environment variables
- Limits returned output to the tail of the captured result
- Applies deny-list based command validation
- Can terminate the current process with a chosen exit code

**Included tools**
- `run_command_line_tool`
- `terminate_process`

### `WebFunctionTools`
Provides tools for retrieving content from web pages and calling REST endpoints.

**Main features**
- Performs HTTP GET requests
- Performs generic REST calls with configurable HTTP methods
- Supports optional headers and request body
- Supports HTTP Basic authentication through URL user info
- Can extract selected HTML fragments using CSS selectors
- Can convert HTML responses into plain text
- Supports configurable timeout and charset

**Included tools**
- `get_web_content`
- `call_rest_api`

## Functional Tool Reference

### `run_command_line_tool`
Executes a system command using Java's `ProcessBuilder`.

**Best use cases**
- Running project-local build commands
- Launching safe development utilities
- Inspecting generated output from tools
- Automating controlled command-line tasks from an AI workflow

**Behavior**
- The command must be provided explicitly.
- The working directory must remain within the current project directory.
- Output is captured from both standard output and error output.
- If the result is too large, only the last part of the output is returned.
- The command is checked against deny-list rules before execution.
- If a command runs too long, it may be forcibly terminated.
- On Windows, shell commands should be executed with `cmd /c`.
- On Unix-like systems, shell commands should be executed with `sh -c`.

**Input parameters**
- `command` *(required, string)*: The command to execute.
- `env` *(optional, string)*: Environment variables as `NAME=VALUE` pairs separated by newline characters.
- `dir` *(optional, string)*: Relative working directory inside the project. Defaults to the project root.
- `tailResultSize` *(optional, integer)*: Maximum number of characters returned from the end of command output. Default is `1024`.
- `charsetName` *(optional, string)*: Character encoding used to read process output. Default is `UTF-8`.

### `terminate_process`
Immediately terminates execution by throwing a host-side termination exception.

**Best use cases**
- Stopping a workflow after a fatal validation error
- Ending execution intentionally when a required condition is not met
- Returning a specific process exit code to the host application

**Behavior**
- Always ends the current tool flow by throwing a termination exception.
- Can include a human-readable message.
- Can include a wrapped cause message.
- Can return a custom exit code.

**Input parameters**
- `message` *(optional, string)*: Exception message. Default is `Process terminated by function tool.`
- `cause` *(optional, string)*: Optional cause message used to create an underlying exception.
- `exitCode` *(optional, integer)*: Exit code to return. Default is `1`.

### `get_web_content`
Fetches the content of a web page using an HTTP GET request.

**Best use cases**
- Downloading page content for analysis
- Extracting part of an HTML page with a CSS selector
- Converting web content to readable plain text
- Reading local content through a `file:` URI when supported by the host runtime

**Behavior**
- Sends an HTTP GET request to the provided URL.
- Supports optional request headers.
- Supports HTTP Basic authentication through URL user info such as `https://user:password@host/path`.
- Can return full HTML or plain text.
- Can limit the returned result to elements matching a CSS selector.
- Can read from a `file:` URI as well as HTTP(S).
- Returns the HTTP status line at the beginning of the response for network calls.

**Input parameters**
- `url` *(required, string)*: The target URL.
- `headers` *(optional, string)*: HTTP headers as `NAME=VALUE` pairs separated by newline characters.
- `timeout` *(optional, integer)*: Maximum wait time in milliseconds. Default is `10000`.
- `charsetName` *(optional, string)*: Character set used to decode the response. Default is `UTF-8`.
- `textOnly` *(optional, boolean)*: If `true`, strips HTML and returns plain text.
- `selector` *(optional, string)*: CSS selector used to extract matching content before rendering.

### `call_rest_api`
Executes a REST API request to a target URL using a configurable HTTP method.

**Best use cases**
- Calling JSON APIs from a workflow
- Sending POST, PUT, PATCH, or DELETE requests
- Testing or inspecting HTTP endpoints
- Passing custom headers and request payloads to remote services

**Behavior**
- Uses `GET` by default when no method is provided.
- Supports custom headers.
- Supports request body content for methods such as `POST`, `PUT`, and `PATCH`.
- Supports configurable timeout and charset.
- Supports HTTP Basic authentication through URL user info.
- Returns the HTTP status line followed by the response body when available.

**Input parameters**
- `url` *(required, string)*: The REST endpoint URL.
- `method` *(optional, string)*: HTTP method such as `GET`, `POST`, `PUT`, `PATCH`, or `DELETE`. Default is `GET`.
- `headers` *(optional, string)*: HTTP headers as `NAME=VALUE` pairs separated by newline characters.
- `body` *(optional, string)*: Request body to send for supported methods.
- `timeout` *(optional, integer)*: Maximum wait time in milliseconds. Default is `10000`.
- `charsetName` *(optional, string)*: Character set used for request and response handling. Default is `UTF-8`.

## Supporting Classes

These classes are part of the functional tool infrastructure and help the main tools work correctly.

### `FunctionTools`
The service-provider interface for tool sets. Implementations register one or more named tools with the GenAI provider.

### `FunctionToolsLoader`
Discovers `FunctionTools` implementations through Java `ServiceLoader` and applies them to the provider.

### `ToolFunction`
A functional interface representing the executable logic behind a tool call.

### `CommandSecurityChecker`
Loads and evaluates deny-list rules used to reject unsafe commands before execution.

### `DenyException`
Signals that a command matched a deny-list rule and should not be executed.

### `LimitedStringBuilder`
Stores only the last part of long output so that command results remain bounded and manageable.
