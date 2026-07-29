---
<!-- @guidance: 
Create or update the `Function Tolls` page:
- Analyze classes in the folder: `/src/main/java/org/machanism/machai/gw/tools` and use this information to create the page content but do not mentionad this as a package details.
- If the function tool class is annotated with the `@SupportedFor` annotation, specify this in the description of the function tool methods.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/ghostwriter/functional-tools.html
---

# Function Tools

Function tools extend Ghostwriter with project-aware capabilities for running reusable Acts, navigating Act episodes, executing approved commands, working with files, processing guidance tags, managing workflow state, and calling web resources. Each function tool exposes a clear purpose and a structured set of input parameters so it can be used safely in automated and interactive workflows.

## Act Tools

Act tools manage reusable named workflows called Acts. Use them to inspect Act definitions, run an Act, and retrieve the results of Acts that were started asynchronously.

### `load_act_details`

Loads details for a specific Act template, including its instructions, input template, and configuration options. This is helpful when you want to inspect an Act before running it or understand whether a custom or built-in Act definition is available.

**Input parameters**

- `act_name` - The name of the Act to load.

### `perform_act`

Runs the specified Act by name. The tool can execute the Act synchronously and return the final result, or execute it asynchronously and return a `process_id` that can be checked later. Optional properties can override Act configuration values before execution.

**Input parameters**

- `act_name` - The name of the Act to perform.
- `properties` - Optional Act properties used to override default configuration values.
- `async` - Optional boolean flag. When `true`, the Act runs in the background and returns a process ID. When `false`, execution waits for completion.

### `get_act_result`

Retrieves the result of a previously started asynchronous Act. If the result is ready, the response reports `done` and includes the Act output. If not, the response reports `processing`.

**Input parameters**

- `process_id` - The process ID returned when the Act was started.

## Act Episode Control Tools

Act episode control tools are supported for `ActProcessor` workflows. They let an Act move between workflow episodes or repeat the current episode when the workflow needs another pass.

### `move_to_episode`

Moves execution to the next episode, or to a specific episode when an ID or name is provided. Use this tool when an Act workflow needs to branch, skip ahead, or continue from a named step.

**Supported for**: `ActProcessor` workflows.

**Input parameters**

- `id` - The ID of the episode to move to.
- `name` - The name of the episode to move to.

### `repeate_episode`

Repeats the current episode while preserving workflow context. This is useful after validation failures, when additional input is required, or when the current step should be re-run with updated information.

**Supported for**: `ActProcessor` workflows.

**Input parameters**

- `message` - Optional message to output before repeating the episode.

## Command Tools

Command tools safely execute approved system commands and provide access to command logs. They are intended for controlled project automation, diagnostics, builds, tests, and log analysis.

### `run_sys_command`

Executes a system command in a project-relative working directory. The command is checked against security rules before execution, optional environment variables can be supplied, and output is captured in a command log. The response includes the process exit code and a log report with the requested tail of the output.

**Input parameters**

- `command` - The command to execute. Runtime configuration placeholders such as `${OS_NAME}` are preserved for application substitution.
- `env` - Optional environment variables for the subprocess. If omitted, the subprocess inherits the current process environment.
- `dir` - Optional working directory relative to the project root. Defaults to `.`.
- `tail_result_size` - Optional maximum number of output characters to return from the end of the log. Defaults to `1024`.
- `charset_name` - Optional character encoding for command output. Defaults to `UTF-8`.

### `get_log_chunk`

Extracts a previous fragment of a stored command log. Use it to page or scroll backward through command output when an earlier command returned only the tail of a long log.

**Input parameters**

- `command_log_id` - The identifier of the command execution session.
- `current_tail_offset` - The offset or position in the log where the current tail result starts.
- `tail_result_size` - Optional size of the log fragment to extract in characters. Defaults to `1024`.
- `charset_name` - Optional encoding for reading the log. Defaults to `UTF-8`.

### `get_log_matches`

Searches a persisted command log for all text matching a Java regular expression. It returns match entries that include the matched text, line number, and character positions.

**Input parameters**

- `command_log_id` - The identifier of the command execution session.
- `regexp` - The Java regular expression to search for.
- `charset_name` - Optional encoding for reading the log. Defaults to `UTF-8`.

## Execution Control Tools

Execution control tools are supported for `AIFileProcessor` workflows. They allow a workflow to stop processing intentionally or finish the current task without shutting down the host application.

### `terminate_execution`

Terminates the application by raising a controlled process termination signal with an exit code. This tool should be used only when explicitly requested by the user or when a workflow must abort intentionally.

**Supported for**: `AIFileProcessor` workflows.

**Input parameters**

- `message` - Optional termination message. Defaults to `Execution terminated by function tool.`
- `exit_code` - Optional exit code returned by the terminating process. Defaults to `0`.

### `end_task`

Ends the current task without terminating the application. This is useful in interactive sessions when the user asks to finish the current task while leaving the application available for future work.

**Supported for**: `AIFileProcessor` workflows.

**Input parameters**

- `message` - Optional completion message. Defaults to `Execution terminated by function tool.`

## File Tools

File tools list, read, write, and patch project files. Paths are interpreted relative to the project context supplied by the runtime.

### `list_files_in_directory`

Lists the immediate files and directories inside a specified folder. The result contains project-relative paths for each child entry.

**Input parameters**

- `dir_path` - Optional directory to list. Defaults to `.`.

### `write_file`

Writes text to a file. If the file already exists, its content is replaced. If it does not exist, parent directories are created as needed before writing the new file.

**Input parameters**

- `file_path` - The path to the file to create or update.
- `text` - The content to write.
- `charset_name` - Optional character encoding. Defaults to `UTF-8`.

### `read_file`

Reads a file from disk and returns its text content using the requested character encoding.

**Input parameters**

- `file_path` - The path to the file to read.
- `charset_name` - Optional character encoding. Defaults to `UTF-8`.

### `apply_patch_to_file`

Applies a unified diff patch to a file. Use this when a small, targeted edit is safer and easier to review than rewriting the whole file.

**Input parameters**

- `file_path` - The path to the file to patch.
- `patch` - The unified diff patch to apply.
- `charset_name` - Optional character encoding. Defaults to `UTF-8`.

## Guidance Tools

Guidance tools discover and process files containing guidance tags. They support documentation generation, code updates, and other guided automation workflows that are driven by embedded guidance comments.

### `get_files_with_guidance_tags`

Scans a root directory for files containing guidance tags and returns a mapping of project directories to matching files. The scan can be restricted with a raw path, glob pattern, or regular expression pattern.

**Input parameters**

- `root_dir` - The root project directory or a folder containing multiple projects.
- `path` - Optional scan path or pattern. Supports raw paths, `glob:` patterns, and `regex:` patterns. Defaults to `glob:**/*.*`.

### `process_files_with_guidance_tag`

Processes files that contain guidance tags using the configured model. The tool scans matching files, applies guidance processing, and returns a processing report. It can run synchronously or asynchronously.

**Input parameters**

- `properties` - Optional processing properties and configuration overrides.
- `path` - Optional scan path or pattern. Supports raw paths, `glob:` patterns, and `regex:` patterns.
- `async` - Optional boolean flag. When `true`, processing runs in the background and returns a process ID. When `false`, the tool waits for completion.

### `get_process_guidance_tag_files_result`

Retrieves the result of guidance tag processing that was started asynchronously. If the result is ready, the response reports `done` and includes the processing report. If not, it reports `processing`.

**Input parameters**

- `process_id` - The process ID returned when guidance processing was started.

## Project Context Tools

Project context tools store, retrieve, and manage project-specific variables. They make it possible to share state between Acts, episodes, prompts, and workflow steps.

### `put_project_context_variable`

Sets or updates a named context variable for the current project. Values are stored in the project context and can be reused by later workflow steps.

**Input parameters**

- `name` - The context variable name.
- `value` - The value to assign.

### `get_project_context_variable`

Retrieves a named context variable for the current project. If the context or variable does not exist, the tool returns an explanatory message.

**Input parameters**

- `name` - The context variable name to retrieve.

### `push_project_context_variable`

Pushes a value into a project context variable. If the variable does not exist, a list is created. If the variable already contains a string, it is converted into a list before appending the new value.

**Input parameters**

- `name` - The context variable name.
- `value` - The value to push.

### `pop_project_context_variable`

Removes and returns a value from a project context variable. A string value is removed directly. A list value can be popped in LIFO or FIFO order.

**Input parameters**

- `name` - The context variable name.
- `mode` - Optional pop mode: `LIFO` or `FIFO`. Defaults to LIFO behavior.

## Web Tools

Web tools retrieve web pages and call REST APIs. They support timeouts, custom headers, configurable character sets, response decoding, and HTTP Basic authentication through URL user information. Header values and URLs may contain runtime configuration placeholders, which are resolved by the application.

### `get_web_content`

Fetches web page content using HTTP GET. The tool can return raw HTML, rendered plain text, or only content matching a CSS selector. It also supports reading `file:` URLs relative to the project context when appropriate.

**Input parameters**

- `url` - The URL to fetch. User-info URLs such as `https://user:password@host/path` are supported for Basic authentication.
- `headers` - Optional HTTP headers. Header values may include runtime placeholders such as `${propertyName}`.
- `timeout` - Optional response timeout in milliseconds.
- `charset_name` - Optional response character encoding. Defaults to `UTF-8`.
- `text_only` - Optional boolean. When `true`, HTML is rendered as plain text.
- `selector` - Optional CSS selector used to extract only matching content.

### `call_rest_api`

Executes a REST API request using the specified HTTP method. The response includes the HTTP status line and response body. Use it for API calls that require custom headers, request bodies, timeouts, or Basic authentication through URL user information.

**Input parameters**

- `url` - The REST endpoint URL. User-info URLs such as `https://user:password@host/path` are supported for Basic authentication.
- `method` - Optional HTTP method such as `GET`, `POST`, `PUT`, `PATCH`, or `DELETE`. Defaults to `GET`.
- `headers` - Optional HTTP headers. Header values may include runtime placeholders such as `${propertyName}`.
- `body` - Optional request body for methods such as `POST`, `PUT`, and `PATCH`.
- `timeout` - Optional response timeout in milliseconds.
- `charset_name` - Optional response character encoding. Defaults to `UTF-8`.
