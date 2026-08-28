<!-- @guidance: 
Create or update the `Function Tolls` page:
- Analyze classes in the folder: `/src/main/java/org/machanism/machai/gw/tools` and use this information to create the page content but do not mentionad this as a package details.
- If the function tool class is annotated with the `@SupportedFor` annotation, specify this in the description of the function tool methods.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->

# Function Tools

Function tools extend Ghostwriter with project-aware capabilities for automation, file updates, workflow control, command execution, guidance processing, context sharing, and web access. They are designed to be called by AI-assisted workflows and expose predictable inputs so each operation can be used safely and consistently. Several implementations also receive the project directory and configuration from the runtime; those host-context values are not normally supplied by the caller and are therefore omitted from the tool-specific parameter lists below.

Use this page to quickly identify what each tool does, when to use it, and which input parameters it accepts.

## Act Tools

Act tools work with reusable named workflows called Acts. They help you inspect Act definitions, run Acts, and retrieve results from Acts that were started in the background.

### `load-act-details`

Loads the details of a specific Act template, including its instructions, input template, and configuration options. The tool checks for both custom and built-in Act definitions and returns a helpful `act not found` message when no matching Act exists.

Use this when you want to inspect an Act before execution or verify that a named Act is available.

**Input parameters**

- `act-name` - The name of the Act to load.

### `perform-act`

Runs a named Act in the current project context. It supports synchronous execution, where the final Act result is returned immediately, and asynchronous execution, where the tool returns a `process-id` while the Act continues in the background.

Property overrides are applied before execution and may be used to change runtime configuration such as model, path, or Act location. Property values can include runtime placeholders, which are resolved by the application.

**Input parameters**

- `act-name` - The name of the Act to perform.
- `properties` - Optional Act properties used to override default configuration values.
- `async` - Optional boolean flag. When `true`, execution starts in the background and returns a `process-id`. When `false`, the tool waits for completion. Defaults to `false`.

### `get-act-result`

Retrieves the result of a previously started asynchronous Act. If processing is complete, the response contains `status: done` and the stored result. If processing is still running or its result file is not available yet, the response contains `status: processing` and a message.

**Input parameters**

- `process-id` - The process ID returned when the Act was started.

## Act Episode Control Tools

Act episode control tools are supported for `ActProcessor` workflows. They are used inside multi-episode Act flows to redirect execution or repeat the current episode.

### `move-to-episode`

Moves execution to a specific episode identified by its ID or name. This tool signals episode navigation to the workflow engine and is useful for an explicit branch or jump to a named step. Do not use it for normal sequential movement to the next episode: the workflow engine performs that automatically.

**Supported for**: `ActProcessor` workflows.

**Input parameters**

- `id` - The ID of the episode to move to.
- `name` - The name of the episode to move to.

### `repeate-episode`

Repeats the current episode while preserving the workflow context. Use it when the current step needs another pass, such as after validation fails or when additional input has been collected. The tool can log a custom message before repeating the episode.

**Supported for**: `ActProcessor` workflows.

**Input parameters**

- `message` - Optional response message to output before repeating the episode. Defaults to an empty string.

## Command Tools

Command tools execute approved system commands and provide access to captured command logs. They are intended for controlled project automation such as builds, tests, diagnostics, and log analysis.

### `run-sys-command`

Executes a system command in a project-relative working directory. Commands are checked by the command security rules before execution, and the working directory must remain inside the project directory. The tool captures stdout and stderr, stores a command log, and returns an exit code plus a bounded tail report.

Environment variables can be passed to the subprocess. The command string and environment values may contain runtime placeholders such as `${OS_NAME}`, which are preserved in documentation and resolved by the application at runtime.

**Input parameters**

- `command` - The command to execute.
- `env` - Optional environment variables for the subprocess. If omitted, the subprocess inherits the current process environment.
- `dir` - Optional working directory for the subprocess. Must be relative to the project directory. Defaults to `.`.
- `tail-result-size` - Optional maximum number of characters to display from the end of command output. Defaults to `1024`.
- `charset-name` - Optional character encoding for reading command output. Defaults to `UTF-8`.

### `get-log-chunk`

Extracts a fragment from a previously captured command log. Use it when a command produced a long log and the initial command response only included the tail. The returned chunk is calculated from the current tail offset and requested chunk size.

**Input parameters**

- `command-log-id` - The identifier of the command execution session.
- `current-tail-offset` - The offset or position in the log where the current tail result starts.
- `tail-result-size` - Optional size of the log fragment to extract in characters. Defaults to `1024`.
- `charset-name` - Optional character encoding for reading log output. Defaults to `UTF-8`.

### `get-log-matches`

Searches a persisted command log for all text matching a Java regular expression. The result is a list of matches with the matched text, line number, and start and end positions within the line.

Use this to locate errors, warnings, stack traces, build summaries, or any other structured pattern in command output.

**Input parameters**

- `command-log-id` - The identifier of the command execution session.
- `regexp` - The Java regular expression to search for in the log.
- `charset-name` - Optional character encoding for reading the log. Defaults to `UTF-8`.

## Execution Control Tools

Execution control tools are supported for `AIFileProcessor` workflows. They intentionally stop execution or gracefully complete the current task.

### `terminate-execution`

Terminates the application by sending a controlled exit code. This tool should only be used when the user explicitly requests termination or when the workflow must intentionally abort. It should not be called automatically just because a task completed successfully.

**Supported for**: `AIFileProcessor` workflows.

**Input parameters**

- `message` - Optional exception message to use. Defaults to `Execution terminated by function tool.`
- `exit-code` - Optional exit code returned when terminating execution. Defaults to `0`.

### `end-task`

Ends the current task without terminating the application. This is useful for interactive workflows where the user asks to finish the current task while keeping the host application available for future work.

**Supported for**: `AIFileProcessor` workflows.

**Input parameters**

- `message` - Optional completion message. Defaults to `Execution terminated by function tool.`

## File Tools

File tools list directories, read files, write files, and apply targeted patches. Relative paths are interpreted from the project directory supplied by the runtime; absolute paths are accepted only when they resolve inside that project directory.

### `list-files-in-directory`

Lists the immediate files and directories inside a specified folder. Returned paths are project-relative and use forward slashes for consistency across platforms.

**Input parameters**

- `dir-path` - Optional path to the directory to list. Defaults to `.`.

### `get-recursive-file-list`

Lists files recursively under a project-relative directory, including files in its subdirectories. The result is limited by `max-count` to prevent an unexpectedly large response; when no files are found, the tool returns an explanatory message.

Use this when you need an inventory of project files beyond the immediate contents of one directory.

**Input parameters**

- `dir` - Optional path to the folder to scan recursively. Defaults to an empty path, representing the project directory.
- `max-count` - Optional maximum number of files allowed in the result. Defaults to `50`; exceeding the limit raises an error.

### `get-recursive-folder-list`

Lists folders recursively under a project-relative directory, including nested directories. The result is limited by `max-count` to keep responses manageable; when no folders are found, the tool returns an explanatory message.

Use this when you need to understand the directory structure of a project.

**Input parameters**

- `dir` - Optional path to the folder to scan recursively. Defaults to an empty path, representing the project directory.
- `max-count` - Optional maximum number of folders allowed in the result. Defaults to `50`; exceeding the limit raises an error.

### `write-file`

Writes text content to a file. Existing files are replaced with the supplied content, while new files are created automatically, including parent directories when needed.

Use this for complete file creation or full-file replacement.

**Input parameters**

- `file-path` - The path to the file to create or update.
- `text` - The content to write to the file.
- `charset-name` - Optional character encoding. Defaults to `UTF-8`.

### `read-file`

Reads a text file from disk using the requested character encoding and returns its content.

**Input parameters**

- `file-path` - The path to the file to read.
- `charset-name` - Optional character encoding. Defaults to `UTF-8`.

### `apply-patch-to-file`

Applies a targeted diff patch to an existing file. This is useful when a small edit is safer and easier to review than rewriting the whole file.

The tool supports two patch formats:

1. Standard unified diff patches with `@@` coordinates, such as patches generated by `diff -u` or `git diff`.
2. Simplified search-and-replace patches with a plain `@@` header and exact line-matching blocks beginning with `-` and `+`.

For best results, the patch should include enough surrounding context to match the intended location uniquely.

**Input parameters**

- `file` - The path to the file to patch.
- `patch` - The unified diff or simplified search-and-replace patch to apply.
- `charset-name` - Optional character encoding. Defaults to `UTF-8`.

## Guidance Tools

Guidance tools discover and process files that contain guidance tags. They support guided documentation generation, source updates, project scans, and asynchronous processing workflows. The guidance function-tool class is supported for `ActProcessor`; its function-tool methods are intended for ActProcessor workflows.

### `get-files-with-guidance-tags`

Scans a root directory for files containing guidance tags and returns a mapping of project directories to the matching files. The scan can be limited by a raw path, glob pattern, or regular expression pattern.

**Supported for**: `ActProcessor` workflows.

Use this to identify which files contain guidance-driven instructions before processing them.

**Input parameters**

- `root-dir` - The absolute path to the root project directory or a folder containing multiple projects. Scanning is performed relative to this directory.
- `path` - Optional scan path or pattern. Supports raw directory names, `glob:` patterns, and `regex:` patterns. Defaults to `glob:**/*.*`.

### `process-files-with-guidance-tag`

Processes files with guidance tags using the configured model. The tool scans matching files in the project or root context and applies guidance processing to each discovered file.

It can run synchronously and return the processing report immediately, or asynchronously and return a `process-id` for later retrieval. Optional properties can override processing configuration, and property values may include runtime placeholders resolved by the application.

**Supported for**: `ActProcessor` workflows.

**Input parameters**

- `properties` - Optional processing properties and configuration overrides.
- `path` - Optional scan path or pattern. Supports raw directory names, `glob:` patterns, and `regex:` patterns.
- `async` - Optional boolean flag. When `true`, processing runs in the background and returns a `process-id`. When `false`, the tool waits for completion. Defaults to `false`.

### `get-process-guidance-tag-files-result`

Retrieves the result of guidance tag processing that was started asynchronously. If the result is ready, the response contains `status: done` and the processing report. Otherwise, it returns `status: processing` with an informational message.

**Supported for**: `ActProcessor` workflows.

**Input parameters**

- `process-id` - The process ID returned when guidance processing was started.

## Project Context Tools

Project context tools store, retrieve, push, and pop project-specific variables. They make it possible to share state between Acts, episodes, prompt templates, and workflow steps.

### `put-project-context-variable`

Sets or updates a named context variable for the current project. String values are stored directly, while non-string values used internally can be serialized to JSON before storage.

Use this to pass values to later workflow steps or make state available to prompt templates.

**Input parameters**

- `name` - The context variable name.
- `value` - The value to assign to the context variable.

### `get-project-context-variables`

Retrieves the requested context variables for the current project and returns a map from each requested name to its stored value. Use it to make shared workflow state available to an Act or prompt template. If no context has been created for the project, the tool reports an error; names that have not been stored are returned with a null value.

**Input parameters**

- `names` - The names of the context variables to retrieve.

### `push-project-context-variable`

Pushes a value into a project context variable. If the variable does not exist, a new list is created. If the existing value is a string, it is converted to a list containing the original value and the pushed value. If the existing value is already a list, the new value is appended.

Use this for accumulating values across workflow steps.

**Input parameters**

- `name` - The context variable name.
- `value` - The value to push to the context variable.

### `pop-project-context-variable`

Removes and returns a value from a project context variable. If the variable is a string, it is removed and returned. If it is a list, a value is removed using either last-in, first-out or first-in, first-out behavior. Empty lists are removed from the context, and single-item lists may be simplified back to a string.

**Input parameters**

- `name` - The context variable name.
- `mode` - Optional pop mode: `LIFO` or `FIFO`. Defaults to LIFO behavior.

## Web Tools

Web tools fetch web pages and call REST APIs. They support custom headers, configurable timeouts, response character sets, URL-based HTTP Basic authentication, and runtime placeholder substitution in URLs and headers.

### `get-web-content`

Fetches content using an HTTP GET request, or reads a file URL. The URL can include user credentials in the user-info format, such as `https://user:password@host/path`, which are converted to an HTTP Basic authentication header.

For HTTP responses, the returned content begins with an HTTP status line followed by the response body. The tool can return the complete response, render it as plain text, or extract content matching a CSS selector. It can also read `file:` URLs; relative file paths are resolved against the project context.

**Input parameters**

- `url` - The URL of the web page to fetch. User-info URLs such as `https://user:password@host/path` are supported for Basic authentication.
- `headers` - Optional HTTP headers. Header values may include runtime placeholders such as `${propertyName}`.
- `timeout` - Optional maximum time in milliseconds to wait for the HTTP response. Defaults to `0`, meaning no custom timeout is applied.
- `charset-name` - Optional response character encoding. Defaults to `UTF-8`.
- `text-only` - Optional boolean. When `true`, HTML content is rendered as plain text. Defaults to `false`.
- `selector` - Optional CSS selector. When supplied, only matching content is returned. If `text-only` is also `true`, only the text of selected elements is returned.

### `call-rest-api`

Executes a REST API request using the specified HTTP method. The URL can include user credentials in the user-info format for HTTP Basic authentication. The response includes an initial HTTP status line followed by the response body.

Use this for API calls that need custom methods, headers, request bodies, timeouts, or configurable response decoding.

**Input parameters**

- `url` - The REST endpoint URL. User-info URLs such as `https://user:password@host/path` are supported for Basic authentication.
- `method` - Optional HTTP method, such as `GET`, `POST`, `PUT`, `PATCH`, or `DELETE`. Defaults to `GET`.
- `headers` - Optional HTTP headers. Header values may include runtime placeholders such as `${propertyName}`.
- `body` - Optional request body for methods such as `POST`, `PUT`, and `PATCH`. Defaults to an empty string.
- `timeout` - Optional maximum time in milliseconds to wait for the HTTP response. Defaults to `0`, meaning no custom timeout is applied.
- `charset-name` - Optional response character encoding. Defaults to `UTF-8`.
