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

Function tools extend Ghostwriter with practical operations that can inspect projects, manage files, run approved commands, call web resources, process guidance tags, and control Act workflows. Each tool is designed for a specific task and accepts structured input parameters so it can be used reliably in automated or interactive workflows.

## Act Tools

Act tools manage reusable named workflows called Acts. Use them to inspect Act definitions, execute Acts, and retrieve asynchronous Act results.

### `load_act_details`

Loads the details of a specific Act template, including instructions, input templates, and configuration options. This is useful when you need to inspect an Act before running it or understand which settings it supports.

**Input parameters**

- `act_name` - The name of the Act to load.

### `perform_act`

Runs a predefined Act by name. It can execute synchronously and return the result immediately, or asynchronously and return a process identifier that can be checked later.

**Input parameters**

- `act_name` - The name of the Act to perform.
- `properties` - Optional Act properties that override default configuration values.
- `async` - Optional boolean flag. When `true`, the Act runs in the background and returns a process ID. When `false`, the tool waits for completion.

### `get_act_result`

Retrieves the result of an Act that was started asynchronously with `perform_act`.

**Input parameters**

- `process_id` - The process ID returned when the Act was started.

## Act Episode Control Tools

These tools are supported for Act processing workflows. They control navigation between Act episodes and can restart an episode when the workflow needs another pass.

### `move_to_episode`

Moves execution to the next episode, or to a specific episode when an ID or name is provided. Use this when an Act workflow needs to branch or skip directly to another step.

**Supported for**: Act processor workflows.

**Input parameters**

- `id` - The ID of the episode to move to.
- `name` - The name of the episode to move to.

### `repeate_episode`

Repeats the current episode while preserving workflow context. This is useful after validation failures, when additional input is required, or when the current step should be re-run.

**Supported for**: Act processor workflows.

**Input parameters**

- `message` - Optional message to output before repeating the episode.

## Command Tools

Command tools safely execute approved system commands and provide access to command logs. They are intended for controlled project automation, diagnostics, and build or test execution.

### `run_sys_command`

Executes a system command in a project-relative working directory. Commands are security checked before execution, output is captured, and a log report is returned.

**Input parameters**

- `command` - The command to execute.
- `env` - Optional environment variables for the subprocess.
- `dir` - Optional working directory relative to the project root. Defaults to `.`.
- `tail_result_size` - Optional maximum number of output characters to return from the end of the log. Defaults to `1024`.
- `charset_name` - Optional character encoding for command output. Defaults to `UTF-8`.

### `get_log_chunk`

Returns an earlier fragment of a stored command log. Use it to page through command output when only the tail of the log was returned initially.

**Input parameters**

- `log_id` - The command execution log identifier.
- `current_tail_offset` - The offset where the current tail result starts.
- `tail_result_size` - Optional size of the fragment to extract. Defaults to `1024`.
- `charset_name` - Optional encoding for reading the log. Defaults to `UTF-8`.

### `get_log_matches`

Searches a stored command log for text matching a Java regular expression. It returns match details such as text, line number, and character positions.

**Input parameters**

- `log_id` - The command execution log identifier.
- `regexp` - The Java regular expression to search for.
- `charset_name` - Optional encoding for reading the log. Defaults to `UTF-8`.

## Execution Control Tools

These tools are supported for file-processing workflows. They control task completion and process termination.

### `terminate_execution`

Terminates the application with an exit code. Use this only when explicitly requested or when a workflow must abort intentionally.

**Supported for**: AI file processing workflows.

**Input parameters**

- `message` - Optional termination message. Defaults to `Execution terminated by function tool.`
- `exit_code` - Optional exit code. Defaults to `0`.

### `end_task`

Ends the current task without terminating the application. This is useful in interactive sessions when the user asks to finish the current task but keep the application available.

**Supported for**: AI file processing workflows.

**Input parameters**

- `message` - Optional completion message.

## File Tools

File tools read, write, patch, and list project files and folders. Paths are interpreted relative to the project context unless otherwise stated.

### `get_recursive_file_list`

Lists files recursively below a directory, including files in all subdirectories.

**Input parameters**

- `dir` - Optional directory path to scan recursively.

### `get_recursive_folder_list`

Lists folders recursively below a directory.

**Input parameters**

- `dir` - Optional directory path to scan recursively.

### `list_files_in_directory`

Lists the immediate files and directories inside a specified folder.

**Input parameters**

- `dir_path` - Optional directory to list. Defaults to `.`.

### `write_file`

Writes text to a file. If the file exists, its content is replaced. If it does not exist, parent directories are created as needed and the file is written.

**Input parameters**

- `file_path` - The path to the file to create or update.
- `text` - The content to write.
- `charset_name` - Optional character encoding. Defaults to `UTF-8`.

### `read_file`

Reads a file from disk and returns its text content.

**Input parameters**

- `file_path` - The path to the file to read.
- `charset_name` - Optional character encoding. Defaults to `UTF-8`.

### `apply_patch_to_file`

Applies a unified diff patch to a file. Use this when only a small section of a file should be changed and a patch is safer than rewriting the whole file.

**Input parameters**

- `file_path` - The file to patch.
- `patch` - The unified diff patch to apply.
- `charset_name` - Optional character encoding. Defaults to `UTF-8`.

## Guidance Tools

Guidance tools discover and process files containing guidance tags. They are useful for documentation generation, code updates, and other guided automation workflows.

### `get_files_with_guidance_tags`

Scans for files that contain guidance tags and returns a mapping of project directories to matching files.

**Input parameters**

- `root_dir` - The root project directory or a folder containing multiple projects.
- `path` - Optional scan path or pattern. Supports raw paths, `glob:` patterns, and `regex:` patterns.

### `process_files_with_guidance_tag`

Processes files that contain guidance tags using the configured model. It can run synchronously or asynchronously.

**Input parameters**

- `properties` - Optional processing properties and configuration overrides.
- `path` - Optional scan path or pattern. Supports raw paths, `glob:` patterns, and `regex:` patterns.
- `async` - Optional boolean flag. When `true`, processing runs in the background and returns a process ID. When `false`, the tool waits for completion.

### `get_process_guidance_tag_files_result`

Retrieves the result of guidance tag processing that was started asynchronously.

**Input parameters**

- `process_id` - The process ID returned when guidance processing was started.

## Project Context Tools

Project context tools store, retrieve, and manage project-specific variables. They make it possible to pass state between Acts, episodes, prompts, or workflow steps.

### `put_project_context_variable`

Sets or updates a named context variable for the current project.

**Input parameters**

- `name` - The context variable name.
- `value` - The value to assign.

### `get_project_context_variable`

Retrieves a named context variable for the current project.

**Input parameters**

- `name` - The context variable name to retrieve.

### `push_project_context_variable`

Pushes a value into a project context variable. If the variable is a string, it is converted to a list. If it is already a list, the value is appended.

**Input parameters**

- `name` - The context variable name.
- `value` - The value to push.

### `pop_project_context_variable`

Removes and returns a value from a project context variable. Strings are removed directly, while list values can be popped in LIFO or FIFO order.

**Input parameters**

- `name` - The context variable name.
- `mode` - Optional pop mode: `LIFO` or `FIFO`. Defaults to LIFO behavior.

## Web Tools

Web tools retrieve web pages and call REST APIs. They support timeouts, custom headers, configurable character sets, and HTTP Basic authentication through URL user information.

### `get_web_content`

Fetches web page content using HTTP GET. It can return raw HTML, plain text, or content selected by a CSS selector.

**Input parameters**

- `url` - The URL to fetch. User-info URLs such as `https://user:password@host/path` are supported for Basic authentication.
- `headers` - Optional HTTP headers.
- `timeout` - Optional response timeout in milliseconds.
- `charset_name` - Optional response character encoding. Defaults to `UTF-8`.
- `text_only` - Optional boolean. When `true`, HTML is rendered as plain text.
- `selector` - Optional CSS selector used to extract only matching content.

### `call_rest_api`

Executes a REST API request using the specified HTTP method. It supports request headers, request bodies, response decoding, timeouts, and Basic authentication through URL user information.

**Input parameters**

- `url` - The REST endpoint URL. User-info URLs such as `https://user:password@host/path` are supported for Basic authentication.
- `method` - Optional HTTP method such as `GET`, `POST`, `PUT`, `PATCH`, or `DELETE`.
- `headers` - Optional HTTP headers.
- `body` - Optional request body for methods such as `POST`, `PUT`, and `PATCH`.
- `timeout` - Optional response timeout in milliseconds.
- `charset_name` - Optional response character encoding. Defaults to `UTF-8`.
