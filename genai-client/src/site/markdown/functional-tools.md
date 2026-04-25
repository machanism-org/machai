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

Functional tools are host-provided capabilities that the GenAI client can register and execute locally. They let AI-assisted workflows interact with the operating system and remote resources in a controlled way. In this project, the functional tool infrastructure is implemented in `org.machanism.machai.ai.tools` and organized around tool installers, support classes, and runtime safety helpers.

## Functional Tool Sets

### `CommandFunctionTools`
Registers tools for command execution and intentional process termination.

**What it is for**
- Running command-line tasks from an AI workflow
- Executing project-local commands in a controlled working directory
- Stopping execution immediately when a workflow must fail fast

**Main features**
- Executes commands through Java `ProcessBuilder`
- Restricts the working directory to a relative location inside the project
- Captures both standard output and standard error
- Limits large output to a configurable tail section
- Supports environment variable injection
- Applies deny-list validation before execution
- Supports explicit process termination with a custom message and exit code

#### `run_command_line_tool`
Executes a system command locally.

**Typical use cases**
- Running build or test commands
- Inspecting generated project output
- Launching safe local utilities needed by an automated workflow

**Behavior**
- Reads the command from the tool input.
- Resolves `${...}` placeholders through the configured runtime configurator when available.
- Rejects invalid or unsafe commands using `CommandSecurityChecker`.
- Uses a working directory that must remain inside the current project.
- Reads command output concurrently from standard output and error streams.
- Returns only the last part of very large output when the configured limit is exceeded.
- Appends the process exit code to the returned result.
- On Windows, shell commands should be wrapped with `cmd /c`.
- On Unix-like systems, shell commands should be wrapped with `sh -c`.

**Input parameters**
- `command` *(required, string)*: The command to execute.
- `env` *(optional, string)*: Environment variables as `NAME=VALUE` pairs separated by newline characters.
- `dir` *(optional, string)*: Relative working directory inside the project. Defaults to the project root.
- `tailResultSize` *(optional, integer)*: Maximum number of characters returned from the end of the captured output. Default is `1024`.
- `charsetName` *(optional, string)*: Character encoding used to decode process output. Default is `UTF-8`.

#### `terminate_process`
Terminates the current process flow immediately.

**Typical use cases**
- Ending a workflow after a fatal validation failure
- Returning a non-zero exit code to the host environment
- Aborting execution intentionally when a required precondition is not met

**Behavior**
- Always throws a host-side termination exception.
- Supports a custom message.
- Supports an optional wrapped cause message.
- Supports a custom exit code.

**Input parameters**
- `message` *(optional, string)*: Exception message. Default is `Process terminated by function tool.`
- `cause` *(optional, string)*: Optional cause message wrapped in an exception.
- `exitCode` *(optional, integer)*: Exit code returned to the host. Default is `1`.

### `WebFunctionTools`
Registers tools for fetching web content and calling HTTP APIs.

**What it is for**
- Downloading web pages for analysis
- Extracting selected HTML fragments from a page
- Converting web content into readable plain text
- Calling REST endpoints with configurable methods, headers, and request bodies

**Main features**
- Performs HTTP GET requests for page retrieval
- Performs generic REST API calls using configurable HTTP methods
- Supports custom request headers
- Supports request bodies for methods such as `POST`, `PUT`, and `PATCH`
- Supports HTTP Basic authentication through URL user info
- Supports optional CSS selector extraction for HTML content
- Supports plain-text rendering of HTML responses
- Supports configurable timeout and character encoding
- Can resolve `${...}` placeholders in URLs and header values
- Supports reading `file:` URIs through the host environment

#### `get_web_content`
Fetches the content of a web page or supported `file:` URI.

**Typical use cases**
- Retrieving a full HTML page
- Extracting only the matching part of a page with a CSS selector
- Converting HTML to plain text for easier downstream processing
- Reading file-based content through a `file:` URI when supported by the runtime

**Behavior**
- Sends an HTTP `GET` request for network URLs.
- Supports optional request headers.
- Supports HTTP Basic authentication through URL user info such as `https://user:password@host/path`.
- Returns an HTTP status line followed by response content for network calls.
- Can restrict the result to elements matching a CSS selector.
- Can convert HTML to plain text when `textOnly` is enabled.
- For `file:` URIs, reads content from the resolved file instead of performing a network request.

**Input parameters**
- `url` *(required, string)*: The target URL or supported `file:` URI.
- `headers` *(optional, string)*: HTTP headers as `NAME=VALUE` pairs separated by newline characters.
- `timeout` *(optional, integer)*: Maximum wait time in milliseconds. Default is `10000`.
- `charsetName` *(optional, string)*: Character set used to decode the response. Default is `UTF-8`.
- `textOnly` *(optional, boolean)*: If `true`, returns plain text instead of HTML.
- `selector` *(optional, string)*: CSS selector used to extract matching content before rendering.

#### `call_rest_api`
Executes a REST-style HTTP request to a target endpoint.

**Typical use cases**
- Calling JSON or text-based APIs from a workflow
- Sending `POST`, `PUT`, `PATCH`, or `DELETE` requests
- Testing remote endpoints with custom headers and request bodies

**Behavior**
- Uses `GET` by default when no method is provided.
- Supports configurable headers and request body content.
- Supports timeout and character encoding options.
- Supports HTTP Basic authentication through URL user info.
- Returns an HTTP status line followed by the response body when available.

**Input parameters**
- `url` *(required, string)*: The REST endpoint URL.
- `method` *(optional, string)*: HTTP method such as `GET`, `POST`, `PUT`, `PATCH`, or `DELETE`. Default is `GET`.
- `headers` *(optional, string)*: HTTP headers as `NAME=VALUE` pairs separated by newline characters.
- `body` *(optional, string)*: Request body sent for supported methods.
- `timeout` *(optional, integer)*: Maximum wait time in milliseconds. Default is `10000`.
- `charsetName` *(optional, string)*: Character set used for request and response handling. Default is `UTF-8`.

## Supporting Infrastructure

### `FunctionTools`
Defines the service-provider interface for functional tool sets.

**Purpose**
- Gives each tool set a standard way to register its tools with `Genai`
- Provides optional configurator injection through `setConfigurator(...)`
- Provides placeholder replacement support for values containing `${...}`

### `FunctionToolsLoader`
Discovers and applies available `FunctionTools` implementations by using Java `ServiceLoader`.

**Purpose**
- Loads tool installers from the classpath
- Applies all discovered tool sets to the active GenAI provider
- Propagates shared configuration to tool installers

### `ToolFunction`
Represents the executable contract for a tool implementation.

**Purpose**
- Defines the callback shape used to execute a tool with runtime parameters
- Allows tool logic to return structured results and throw `IOException` when needed

### `CommandSecurityChecker`
Performs deny-list validation for command execution.

**Purpose**
- Loads operating-system-specific deny-list rules
- Supports regular-expression and keyword-based checks
- Rejects commands that match unsafe patterns before execution starts

### `DenyException`
Exception type raised when a command fails deny-list validation.

**Purpose**
- Signals that command execution must be blocked
- Carries the reason for the deny-list match

### `LimitedStringBuilder`
Keeps only the last part of a growing string.

**Purpose**
- Prevents command output capture from growing without limit
- Preserves the most recent output, which is usually the most useful diagnostic section
