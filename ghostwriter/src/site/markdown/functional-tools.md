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

Function tools extend Ghostwriter with project-aware capabilities for workflow automation, command execution, file management, guidance processing, context sharing, and web access. The runtime supplies host-context parameters such as the project directory and configuration; those parameters are identified below but are normally not entered by the caller.

## Act tools

Act tools inspect and run reusable named workflows. `perform_act` can run synchronously or return a process ID for background execution; use the corresponding result tool to poll a background run.

### `load_act_details`

Loads a named Act's instructions, input template, and configuration options. It checks custom and built-in Act definitions and reports `act not found` when neither is available. Use it before execution when you need to inspect an Act or confirm its name.

**Input parameters:** `act_name` (Act name). Runtime context: `project_dir` and configuration.

### `perform_act`

Runs a named Act in the project. Optional property overrides are resolved against the current configuration. With `async: false` (the default), it returns the Act result; with `async: true`, it returns `process_id` and `status: processing` so the run can be retrieved later.

**Input parameters:** `act_name` (Act name); `properties` (optional configuration overrides); `async` (optional background-execution flag, default `false`). Runtime context: `project_dir` and configuration.

### `get_act_result`

Retrieves the result of an asynchronous Act by process ID. It returns `status: done` and the result when available, or `status: processing` while the temporary result is not ready.

**Input parameters:** `process_id` (ID returned by `perform_act` with `async: true`).

## Act episode-control tools

These control tools are registered for and supported by **`ActProcessor` workflows** through `@SupportedFor({ ActProcessor.class })`. They signal navigation by throwing a workflow-control exception rather than returning ordinary data.

### `move_to_episode`

Moves to the next episode, or to the episode identified by `id` or `name`. Use it to branch, skip ahead, or continue a multi-step Act from a named episode.

**Input parameters:** `id` (episode ID; `0` can represent the default next-episode behavior); `name` (episode name). **Supported for:** `ActProcessor` workflows.

### `repeate_episode`

Repeats the current episode while preserving workflow context. An optional message is logged before the repeat, which is useful after validation failure or when more input is required. The tool name intentionally uses the implementation's spelling, `repeate_episode`.

**Input parameters:** `message` (optional message, default empty string). **Supported for:** `ActProcessor` workflows.

## Command and log tools

Command tools execute approved operating-system commands in a project-relative directory and persist output for later inspection. Commands are security-checked, and output is bounded or searchable to keep diagnostics manageable.

### `run_sys_command`

Runs an approved command for the current operating system. It supports environment variables, a project-relative working directory, output-tail sizing, and a character set. Command and environment values can use configuration placeholders. The result includes an exit code and captured log information; failures are reported as tool errors.

**Input parameters:** `command` (command to execute); `env` (optional environment variables); `dir` (optional relative working directory, default `.`); `tail_result_size` (optional output-tail limit, default `1024`); `charset_name` (optional output encoding, default `UTF-8`). Runtime context: `project_dir` and configuration.

### `get_log_chunk`

Reads the portion of a persisted command log immediately preceding the current output window. Use it to page backward through long command output.

**Input parameters:** `command_log_id` (command session ID); `current_tail_offset` (start position of the current tail); `tail_result_size` (optional chunk size, default `1024`); `charset_name` (optional log encoding, default `UTF-8`).

### `get_log_matches`

Searches a persisted command log for every match of a Java regular expression and returns each matched text with its line and character positions. Use it to locate errors, warnings, or structured diagnostics.

**Input parameters:** `command_log_id` (command session ID); `regexp` (Java regular expression); `charset_name` (optional log encoding, default `UTF-8`).

## Execution-control tools

These tools are registered for and supported by **`AIFileProcessor` workflows** through `@SupportedFor({ AIFileProcessor.class })`. They deliberately interrupt or complete workflow execution and should be used only for the indicated control-flow case.

### `terminate_execution`

Stops the application with a supplied exit code and message. Use only when the user explicitly requests termination or the workflow must intentionally abort; do not call it merely because a task completed successfully.

**Input parameters:** `message` (optional termination message, default `Execution terminated by function tool.`); `exit_code` (optional exit code, default `0`). Runtime context: `project_dir`. **Supported for:** `AIFileProcessor` workflows.

### `end_task`

Completes the current task without terminating the host application. Use when the user asks to end the current task while leaving the application available for later work.

**Input parameters:** `message` (optional completion message, default `Execution terminated by function tool.`). **Supported for:** `AIFileProcessor` workflows.

## File tools

File tools operate on project files. Relative paths are resolved from the project directory, and file access is restricted to the project context by the implementation. Text operations accept a configurable character set.

### `list_files_in_directory`

Lists the immediate files and directories in a folder. It is useful for quickly inspecting a project area without a recursive scan.

**Input parameters:** `dir_path` (directory path, default `.`). Runtime context: `project_dir`.

### `get_recursive_file_list`

Lists files in a directory and all subdirectories. The result is limited by `max_count`; exceeding the limit raises an error, while an empty scan returns `No files found in directory.`

**Input parameters:** `dir` (directory path, default empty/project root); `max_count` (maximum file count, default `50`). Runtime context: `project_dir`.

### `get_recursive_folder_list`

Lists directories recursively, including nested folders. It applies the same response-size protection as the recursive file tool and returns `No folders found in directory.` for an empty scan.

**Input parameters:** `dir` (directory path, default empty/project root); `max_count` (maximum folder count, default `50`). Runtime context: `project_dir`.

### `read_file`

Reads a text file using the requested character set. It rejects directories and missing paths, making it suitable for retrieving source, configuration, or documentation content before analysis.

**Input parameters:** `file_path` (file to read); `charset_name` (optional encoding, default `UTF-8`). Runtime context: `project_dir`.

### `write_file`

Creates or replaces a file with supplied text and creates missing parent directories for new files. Use it for a complete file update rather than a targeted edit.

**Input parameters:** `file_path` (file to create or update); `text` (new content); `charset_name` (optional encoding, default `UTF-8`). Runtime context: `project_dir`.

### `apply_patch_to_file`

Applies a targeted patch to an existing file. It accepts either a standard unified diff with coordinates or a simplified search-and-replace diff with exact removed and added lines, making small changes safer than rewriting a complete file.

**Input parameters:** `file` (file to patch); `patch` (unified or simplified diff); `charset_name` (optional encoding, default `UTF-8`). Runtime context: `project_dir`.

## Guidance-processing tools

Guidance tools find files containing `@guidance` tags and apply the configured processing workflow either synchronously or in the background.

### `get_files_with_guidance_tags`

Scans a root directory and returns project directories mapped to files containing guidance tags. The scan supports a raw path, `glob:` pattern, or `regex:` pattern.

**Input parameters:** `root_dir` (absolute scan root); `path` (optional scan path or pattern, default `glob:**/*.*`). Runtime context: project configuration.

### `process_files_with_guidance_tag`

Processes matching guidance-tagged files with the configured model. With `async: false` (default), it returns the processing report; with `async: true`, it returns a process ID for later retrieval. `properties` can override processing configuration.

**Input parameters:** `properties` (optional configuration overrides); `path` (scan path or pattern); `async` (optional background-execution flag, default `false`). Runtime context: `project_dir` and configuration.

### `get_process_guidance_tag_files_result`

Retrieves the report from an asynchronous guidance-processing run. It returns `status: done` with the result when complete, or `status: processing` while the result is unavailable.

**Input parameters:** `process_id` (ID returned by asynchronous guidance processing).

## Project context tools

Project context tools maintain named state for a project and allow workflows to share values between Acts, episodes, and prompt templates.

### `put_project_context_variable`

Sets or replaces a named project context variable. Values supplied by this function are stored as strings and can be read by later workflow steps.

**Input parameters:** `name` (variable name); `value` (value to store). Runtime context: `project_dir`.

### `get_project_context_variable`

Returns a named context value, or an explanatory message when the project context or variable does not exist.

**Input parameters:** `name` (variable name). Runtime context: `project_dir`.

### `push_project_context_variable`

Appends a value to a context variable. A missing variable becomes a list; an existing string is converted to a two-item list; an existing list receives the new value. Use it to accumulate results across steps.

**Input parameters:** `name` (variable name); `value` (value to append). Runtime context: `project_dir`.

### `pop_project_context_variable`

Removes and returns a context value. Strings are removed directly; lists use `LIFO` by default or `FIFO` when requested, and are simplified or removed as they shrink.

**Input parameters:** `name` (variable name); `mode` (optional `LIFO` or `FIFO`, default `LIFO` behavior). Runtime context: `project_dir`.

## Web and REST tools

Web tools retrieve HTTP content and call REST endpoints. They support custom headers, configuration placeholder substitution, timeouts, response encodings, and HTTP Basic authentication through URL user information.

### `get_web_content`

Fetches a page with HTTP GET, or reads a permitted `file:` URL. It can return HTML, render plain text, or select matching elements with a CSS selector; when both selector and text-only mode are used, only selected text is returned.

**Input parameters:** `url` (page or file URL); `headers` (optional request headers); `timeout` (optional milliseconds, default `0`); `charset_name` (optional response encoding, default `UTF-8`); `text_only` (optional plain-text flag, default `false`); `selector` (optional CSS selector). Runtime context: `project_dir` and configuration.

### `call_rest_api`

Sends a REST request with a configurable HTTP method, headers, body, timeout, and response encoding. The returned string begins with the HTTP status line and then contains the response body; URL user information supplies Basic authentication.

**Input parameters:** `url` (REST endpoint); `method` (HTTP method, default `GET`); `headers` (optional request headers); `body` (optional request body, default empty); `timeout` (optional milliseconds, default `0`); `charset_name` (optional response encoding, default `UTF-8`). Runtime context: `project_dir` and configuration.
