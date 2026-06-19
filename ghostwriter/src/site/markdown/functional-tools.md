---
<!-- @guidance: 
Create the `Function Tolls` page:
- Analyze classes in the folder: `/src/main/java/org/machanism/machai/gw/tools`.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/ghostwriter/functional-tools.html
---

# Function Tools

Ghostwriter function tools are host-side capabilities that an AI provider can expose to a model during a workflow. They provide controlled access to Act templates, episode navigation, project context variables, project files, command execution, guidance-tag processing, and HTTP resources.

The tools on this page are implemented in `src/main/java/org/machanism/machai/gw/tools`.

## Tool groups

- **Act tools** load Act definitions, start asynchronous Act execution, and retrieve Act results.
- **Episode control tools** move between Act episodes or repeat the current episode.
- **Project context tools** store, retrieve, push, and pop project-scoped variables.
- **File system tools** read, write, patch, and list files or folders in the active project.
- **Command and task tools** run approved commands, inspect command logs, end tasks, and terminate execution.
- **Guidance tools** find files with `@guidance` tags and process them asynchronously.
- **Web and API tools** fetch web content and call REST endpoints.

## Act tools

### `load_act_details`

Loads the definition details for a named Act template.

**Use case**
Use this tool when you need to inspect an Act before running it, review its instructions and input template, or compare custom and built-in Act definitions.

**Features**
- Searches the configured custom Acts directory.
- Searches built-in classpath Acts.
- Returns a structured map with available `custom` and `build-in` definitions.
- Helps identify which Act definition will be available to a workflow.

**Input parameters**
- `act_name` *(string, required)*: Name of the Act to load.

### `perform_act`

Starts a named Act as an asynchronous background operation.

**Use case**
Use this tool when a predefined Ghostwriter workflow should run in the current project context. The tool immediately returns a GUID that can be used with `get_act_result`.

**Features**
- Runs an Act in the active project.
- Accepts optional Act properties.
- Resolves property placeholders at runtime where supported by configuration.
- Uses the configured model, or the model supplied in the properties.
- Uses the configured scan directory, defaulting to the current project when no scan directory is supplied.
- Scans project documents and stores the Act result in a temporary file.
- Returns `status: processing` with a result GUID.

**Input parameters**
- `act_name` *(string, required)*: Name of the Act to perform.
- `properties` *(object/map, optional)*: Act properties to apply before execution.

### `get_act_result`

Retrieves the result of a previously started Act.

**Use case**
Use this tool after `perform_act` to check whether the Act is still running or to obtain its completed result.

**Features**
- Looks up an Act result by GUID.
- Returns `processing` while the result file is not available.
- Returns `done` with the stored result when processing has completed.
- Includes an informational message when the result is not ready.

**Input parameters**
- `guid` *(string, required)*: GUID returned by `perform_act`.

## Episode control tools

### `move_to_episode`

Signals an Act workflow to move to another episode.

**Use case**
Use this tool inside an episode-based Act when the workflow needs to branch, skip ahead, or continue at a specific episode.

**Features**
- Signals episode navigation by throwing a controlled `MoveToEpisodeException`.
- Supports selecting an episode by numeric ID.
- Supports selecting an episode by name.
- Available for `ActProcessor` workflows.

**Input parameters**
- `id` *(integer, required)*: ID of the episode to move to.
- `name` *(string, required)*: Name of the episode to move to.

### `repeate_episode`

Repeats the current Act episode.

**Use case**
Use this tool when the current episode should run again, such as after validation fails, required information is missing, or a workflow intentionally retries a step.

**Features**
- Restarts the current episode by throwing a controlled `RepeatEpisodeException`.
- Preserves the workflow context.
- Can log an optional message before repeating.
- Available for `ActProcessor` workflows.

**Input parameters**
- `message` *(string, optional)*: Message to output before repeating the episode. Default: empty string.

## Project context tools

### `put_project_context_variable`

Stores or updates a named value in the current project context.

**Use case**
Use this tool to save state for later tool calls, Acts, prompt templates, or episodes in the same project.

**Features**
- Stores a value under a project-scoped variable name.
- Replaces an existing value with the same name.
- Serializes non-string values to JSON when used internally.
- Returns a confirmation or error message.

**Input parameters**
- `name` *(string, required)*: Context variable name.
- `value` *(string, required)*: Value to assign.

### `get_project_context_variable`

Reads a named value from the current project context.

**Use case**
Use this tool to reuse data that was saved earlier for the active project.

**Features**
- Retrieves a project-scoped value by name.
- Returns the stored value as text.
- Reports when no context exists for the project.
- Reports failures as readable messages.

**Input parameters**
- `name` *(string, required)*: Context variable name to retrieve.

### `push_project_context_variable`

Appends a value to a project context variable.

**Use case**
Use this tool to accumulate multiple values, such as files to process, validation notes, generated IDs, or pending tasks.

**Features**
- Creates a list when the variable does not exist.
- Converts an existing string value into a list.
- Appends to an existing list.
- Reports unsupported variable types.

**Input parameters**
- `name` *(string, required)*: Context variable name.
- `value` *(string, required)*: Value to append.

### `pop_project_context_variable`

Removes and returns one value from a project context variable.

**Use case**
Use this tool when a context variable should behave like a stack or queue.

**Features**
- Pops from a list in LIFO mode by default.
- Supports FIFO mode for queue-style processing.
- Removes and returns string values directly.
- Removes the variable when its list becomes empty.
- Converts a one-item remaining list back to a string.
- Reports missing context variables and unsupported variable types.

**Input parameters**
- `name` *(string, required)*: Context variable name.
- `mode` *(string, optional)*: Pop mode. Use `LIFO` for last-in-first-out or `FIFO` for first-in-first-out. Default: `LIFO`.

## File system tools

### `read_file`

Reads a text file from the current project.

**Use case**
Use this tool to inspect a source file, documentation file, configuration file, or other project text file.

**Features**
- Reads full file content.
- Resolves the file path relative to the active project directory.
- Supports configurable character decoding.
- Returns `File not found.` when the file does not exist.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to read.
- `charset_name` *(string, optional)*: Charset used to decode the file. Default: `UTF-8`.

### `write_file`

Creates a new file or replaces the full content of an existing file.

**Use case**
Use this tool to create a project file or rewrite a complete file with known content.

**Features**
- Writes complete text content to a file.
- Creates missing parent directories for new files.
- Updates existing files in place.
- Supports configurable character encoding.
- Returns a success or error message.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to create or update.
- `text` *(string, required)*: Complete text content to write.
- `charset_name` *(string, optional)*: Charset used to write the file. Default: `UTF-8`.

### `list_files_in_directory`

Lists the immediate files and directories in a folder.

**Use case**
Use this tool for a quick, non-recursive view of a project directory.

**Features**
- Lists files and directories directly inside the requested folder.
- Does not recurse into subdirectories.
- Uses the project root when the directory path is omitted.
- Returns project-relative paths with forward slashes and `./` prefixes.
- Returns an empty list when the directory is missing or has no readable children.

**Input parameters**
- `dir_path` *(string, optional)*: Directory to list. Default: `.`.

### `get_recursive_file_list`

Lists files recursively under a directory.

**Use case**
Use this tool to inventory all files under a project folder.

**Features**
- Recursively scans nested directories.
- Returns files only.
- Uses project layout rules while scanning.
- Returns project-relative paths with forward slashes and `./` prefixes.
- Returns `No files found in directory.` when no files are found.

**Input parameters**
- `dir` *(string, optional)*: Folder to scan recursively. Defaults to the project root when omitted.

### `get_recursive_folder_list`

Lists folders recursively under a directory.

**Use case**
Use this tool to inspect a project's directory structure or a selected subfolder.

**Features**
- Recursively scans nested directories.
- Returns folders only.
- Uses project layout directory discovery.
- Returns project-relative paths with forward slashes and `./` prefixes.
- Returns `No folders found in directory.` when no folders are found.

**Input parameters**
- `dir` *(string, optional)*: Folder to scan recursively. Defaults to the project root when omitted.

### `apply_patch_to_file`

Applies a unified diff patch to a file.

**Use case**
Use this tool for focused edits when only a small part of a file should change.

**Features**
- Accepts standard unified diff content.
- Applies targeted hunks to the requested file.
- Supports configurable character encoding.
- Returns a success or failure message.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to patch.
- `patch` *(string, required)*: Unified diff patch content.
- `charset_name` *(string, optional)*: Charset used while applying the patch. Default: `UTF-8`.

## Command and task tools

### `run_sys_command`

Executes an approved system command inside the current project.

**Use case**
Use this tool for build, test, inspection, and other command-line operations allowed by the host security policy.

**Features**
- Runs commands from a project-confined working directory.
- Rejects invalid, absolute, or outside-project working directories.
- Wraps commands with the platform shell (`cmd /c` on Windows, `sh -c` elsewhere).
- Applies command deny-list checks before execution.
- Supports custom environment variables.
- Resolves configured placeholders in the command and environment values.
- Captures stdout and stderr.
- Stores a persisted command log.
- Returns the process exit code and a bounded log report.
- Enforces a process timeout.

**Input parameters**
- `command` *(string, required)*: Command to execute.
- `env` *(object/map, optional)*: Environment variables for the subprocess. If omitted, the subprocess inherits the current process environment.
- `dir` *(string, optional)*: Working directory relative to the project. Default: `.`.
- `tail_result_size` *(integer, optional)*: Maximum characters returned from the end of the output. Default: `1024`.
- `charset_name` *(string, optional)*: Charset used to read command output. Default: `UTF-8`.

### `get_log_chunk`

Retrieves an earlier fragment of a stored command log.

**Use case**
Use this tool after `run_sys_command` when the returned log tail was truncated and you need to page backward through older output.

**Features**
- Reads from the persisted command log for a command ID.
- Returns the log segment immediately before the current tail offset.
- Supports configurable chunk size.
- Supports configurable character decoding.
- Throws an error when the requested log file does not exist.

**Input parameters**
- `logId` *(string, required)*: Command execution session ID.
- `tail_result_size` *(integer, optional)*: Size of the previous log fragment. Default: `1024`.
- `current_tail_offset` *(integer, required)*: Offset where the current visible tail starts.
- `charset_name` *(string, optional)*: Charset used to read the log. Default: `UTF-8`.

### `get_log_matches`

Searches a stored command log with a Java regular expression.

**Use case**
Use this tool to extract errors, warnings, generated IDs, test summaries, or other structured text from command output.

**Features**
- Searches every line in a persisted command log.
- Uses Java regular expression syntax.
- Returns every match.
- Includes matched text, start position, end position, and line number.
- Throws an error when the requested log file does not exist.

**Input parameters**
- `logId` *(string, required)*: Command execution session ID.
- `regexp` *(string, required)*: Java regular expression to search for.
- `charset_name` *(string, optional)*: Charset used to read the log. Default: `UTF-8`.

### `end_task`

Ends the current task without terminating the application.

**Use case**
Use this tool only when the user explicitly asks to end the current task, or when workflow logic requires graceful task completion while keeping the host application available.

**Features**
- Ends only the active task by throwing a controlled `EndTaskException`.
- Keeps the host application running.
- Supports a custom completion message.
- Available for `AIFileProcessor` workflows.

**Input parameters**
- `message` *(string, optional)*: Completion message. Default: `Execution terminated by function tool.`

### `terminate_execution`

Terminates the application with an exit code.

**Use case**
Use this tool only when the user explicitly requests termination or when a fatal workflow condition requires stopping the application.

**Features**
- Aborts the overall workflow by throwing a controlled `ProcessTerminationException`.
- Supports a custom termination message.
- Supports a custom exit code.
- Available for `AIFileProcessor` workflows.

**Input parameters**
- `message` *(string, optional)*: Termination message. Default: `Execution terminated by function tool.`
- `exit_code` *(integer, optional)*: Exit code to return. Default: `0`.

## Guidance tools

### `get_files_with_guidance_tags`

Finds files that contain `@guidance` tags.

**Use case**
Use this tool before bulk processing to discover which files contain guidance annotations and which project each file belongs to.

**Features**
- Scans a root directory or a folder containing multiple projects.
- Supports raw paths, glob patterns, and regex patterns.
- Collects files that contain guidance tags.
- Groups discovered files by project directory.
- Returns a map from project directories to matching files.

**Input parameters**
- `root_dir` *(string, required)*: Absolute path to the root project directory or a folder containing multiple projects.
- `path` *(string, optional)*: Scanning path or pattern. Default: `glob:**/*.*`.

### `process_files_with_guidance_tag`

Starts asynchronous processing of files with `@guidance` tags.

**Use case**
Use this tool when files matching a path or pattern should be processed by the configured guidance workflow.

**Features**
- Processes matching guidance-tagged files in the background.
- Accepts optional processing properties.
- Resolves the model from properties or configuration.
- Scans the requested project path or pattern.
- Stores a processing report in a temporary file.
- Returns `status: processing` with a result GUID.

**Input parameters**
- `properties` *(object/map, optional)*: Processing properties to apply.
- `path` *(string, optional)*: Scanning path or pattern relative to the current project, or an allowed absolute path within the scan root.

### `get_process_guidance_tag_files_result`

Retrieves the result of a previously started guidance-processing run.

**Use case**
Use this tool after `process_files_with_guidance_tag` to check whether processing has finished and to obtain the generated report.

**Features**
- Looks up a guidance-processing result by GUID.
- Returns `processing` while the result file is not available.
- Returns `done` with the stored report when processing has completed.
- Includes an informational message when the result is not ready.

**Input parameters**
- `guid` *(string, required)*: GUID returned by `process_files_with_guidance_tag`.

## Web and API tools

### `get_web_content`

Fetches content from a web page or local `file://` resource.

**Use case**
Use this tool to read external web content, extract part of an HTML document, convert HTML to readable text, or load a local file URI.

**Features**
- Performs HTTP or HTTPS `GET` requests.
- Reads local resources through `file://` URIs.
- Supports Basic authentication through URL user info.
- Supports custom request headers.
- Resolves configured placeholders in the URL and header values.
- Supports request timeout configuration.
- Supports CSS selector extraction for HTML responses.
- Can render HTML as plain text.
- Returns an HTTP status line followed by response content for HTTP requests.

**Input parameters**
- `url` *(string, required)*: URL to fetch. May include user info for Basic authentication.
- `headers` *(object/map, optional)*: HTTP headers to send.
- `timeout` *(integer, optional)*: Maximum time to wait in milliseconds. Use `0` for default connection behavior.
- `charset_name` *(string, optional)*: Charset used to decode the response. Default: `UTF-8`.
- `text_only` *(boolean, optional)*: If `true`, returns plain text for HTML content. Default: `false`.
- `selector` *(string, optional)*: CSS selector used to extract matching HTML elements.

### `call_rest_api`

Executes an HTTP request against a REST endpoint.

**Use case**
Use this tool to call an API directly, including read requests and write-oriented requests with a request body.

**Features**
- Supports HTTP methods such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- Supports Basic authentication through URL user info.
- Supports custom request headers.
- Resolves configured placeholders in the URL and header values.
- Supports request bodies for `POST`, `PUT`, and `PATCH`.
- Supports request timeout configuration.
- Returns an HTTP status line followed by response content when available.

**Input parameters**
- `url` *(string, required)*: REST endpoint URL. May include user info for Basic authentication.
- `method` *(string, optional)*: HTTP method to use. Default: `GET`.
- `headers` *(object/map, optional)*: HTTP headers to send.
- `body` *(string, optional)*: Request body for `POST`, `PUT`, or `PATCH` requests.
- `timeout` *(integer, optional)*: Maximum time to wait in milliseconds. Use `0` for default connection behavior.
- `charset_name` *(string, optional)*: Charset used to encode request bodies and decode responses. Default: `UTF-8`.
