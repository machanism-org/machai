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

Ghostwriter function tools are host-side capabilities that a model can call during a workflow. They help inspect and run Acts, navigate Act episodes, store project-scoped state, work with files, execute approved commands, inspect command logs, discover and process `@guidance` files, and retrieve content from web pages or REST APIs.

The tools described on this page are implemented in `src/main/java/org/machanism/machai/gw/tools`.

## Tool groups

- **Act tools** inspect Act templates, start Act execution, and retrieve asynchronous Act results.
- **Episode control tools** move to another Act episode or repeat the current episode.
- **Project context tools** store, retrieve, push, and pop project-scoped workflow variables.
- **File system tools** read, write, patch, and list files or folders under the active project.
- **Command and task tools** run approved system commands, page or search command logs, and control task termination.
- **Guidance tools** find files that contain `@guidance` tags and process them asynchronously.
- **Web and API tools** fetch web pages, local `file://` resources, and REST endpoint responses.

## Act tools

### `load_act_details`

Loads the definition details for a specific Act template.

**Use case**
Use this tool when you need to inspect an Act before running it, compare a custom Act with the built-in version, or understand the instructions and configuration that an Act uses.

**Features**
- Searches the configured custom Acts location.
- Searches built-in classpath Acts.
- Returns available definitions in a structured object.
- Helps diagnose whether an Act is overridden by a custom definition.

**Input parameters**
- `act_name` *(string, required)*: Name of the Act to load.

### `perform_act`

Starts execution of an Act as an asynchronous background operation.

**Use case**
Use this tool when a workflow should run a predefined Act and continue without waiting for completion. The response contains a GUID that can be used with `get_act_result`.

**Features**
- Runs an Act by name in the current project context.
- Accepts optional Act properties.
- Resolves the model from supplied properties or application configuration.
- Applies the configured scan directory, defaulting to the current project when not supplied.
- Scans project documents before collecting Act results.
- Stores the asynchronous result in a temporary result file.

**Input parameters**
- `act_name` *(string, required)*: Name of the Act to perform.
- `properties` *(object/map, optional)*: Act properties to apply before execution.

### `get_act_result`

Retrieves the result of a previously started Act.

**Use case**
Use this tool after `perform_act` to check whether an Act is still running or to obtain its completed result.

**Features**
- Looks up an asynchronous Act result by GUID.
- Returns `processing` when the result file is not available yet.
- Returns `done` with the stored result when processing has completed.
- Reports that the result is not ready when no result file exists for the GUID.

**Input parameters**
- `guid` *(string, required)*: GUID returned by `perform_act`.

## Episode control tools

### `move_to_episode`

Moves an Act workflow to another episode.

**Use case**
Use this tool from inside an episode-based Act when the workflow needs to branch, skip forward, or continue at a known episode.

**Features**
- Signals an episode transition to the Act processor.
- Supports selecting an episode by ID.
- Supports selecting an episode by name.
- Intended only for Act processor workflows.

**Input parameters**
- `id` *(integer, required)*: ID of the episode to move to.
- `name` *(string, required)*: Name of the episode to move to.

### `repeate_episode`

Repeats the current Act episode.

**Use case**
Use this tool when the current episode should run again, for example after a validation failure, missing input, or a correction request.

**Features**
- Restarts the current episode.
- Preserves workflow context.
- Can log an optional user-facing message before repeating.
- Intended only for Act processor workflows.

**Input parameters**
- `message` *(string, optional)*: Message to output before repeating the episode.

## Project context tools

### `put_project_context_variable`

Stores or updates a named value in the current project context.

**Use case**
Use this tool when one step needs to save state for later Acts, episodes, or tool calls in the same project.

**Features**
- Stores a value under a project-scoped variable name.
- Replaces the previous value when the name already exists.
- Serializes non-string values to JSON when used internally.
- Returns a confirmation or an error message.

**Input parameters**
- `name` *(string, required)*: Context variable name.
- `value` *(string, required)*: Value to assign.

### `get_project_context_variable`

Reads a named value from the current project context.

**Use case**
Use this tool when a workflow needs to reuse data that was saved earlier for the active project.

**Features**
- Retrieves project-scoped values by name.
- Returns the stored value as text.
- Reports when no context exists for the project.
- Reports failures as readable messages.

**Input parameters**
- `name` *(string, required)*: Context variable name to retrieve.

### `push_project_context_variable`

Appends a value to a project context variable.

**Use case**
Use this tool when a workflow needs to accumulate multiple values, such as files to process, validation notes, or pending tasks.

**Features**
- Creates a new list when the variable does not exist.
- Converts an existing string value into a list.
- Appends to an existing list.
- Returns a confirmation or an unsupported-type message.

**Input parameters**
- `name` *(string, required)*: Context variable name.
- `value` *(string, required)*: Value to append.

### `pop_project_context_variable`

Removes and returns one value from a project context variable.

**Use case**
Use this tool when a context variable should behave like a stack or queue.

**Features**
- Pops from a list in `LIFO` mode by default.
- Supports `FIFO` mode for queue-style processing.
- Removes and returns plain string values directly.
- Removes the variable when its list becomes empty.
- Converts a one-item remaining list back to a string.

**Input parameters**
- `name` *(string, required)*: Context variable name.
- `mode` *(string, optional)*: Pop mode. Use `LIFO` for last-in-first-out or `FIFO` for first-in-first-out.

## File system tools

### `read_file`

Reads a text file from the current project.

**Use case**
Use this tool to inspect a source file, documentation file, configuration file, or any other project text file.

**Features**
- Reads the full file content.
- Resolves the file path relative to the active project directory.
- Supports configurable character decoding.
- Returns `File not found.` when the file does not exist.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to read.
- `charset_name` *(string, optional)*: Charset used to decode the file. Default: `UTF-8`.

### `write_file`

Creates a new file or replaces an existing file's full contents.

**Use case**
Use this tool when a workflow needs to create a project file or rewrite a complete file with known content.

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

Lists the immediate children of a directory.

**Use case**
Use this tool for a quick, non-recursive view of files and folders in a project directory.

**Features**
- Lists files and directories directly inside the requested folder.
- Does not recurse into subdirectories.
- Uses the project root when the directory path is omitted.
- Returns project-relative paths with forward slashes.

**Input parameters**
- `dir_path` *(string, optional)*: Directory to list. Default: `.`.

### `get_recursive_file_list`

Lists files recursively under a directory.

**Use case**
Use this tool when you need an inventory of all files under a project folder.

**Features**
- Recursively scans nested directories.
- Returns files only.
- Uses project layout rules while scanning.
- Returns project-relative paths.
- Returns `No files found in directory.` when no files are found.

**Input parameters**
- `dir` *(string, optional)*: Folder to scan recursively. Defaults to the project root when omitted.

### `get_recursive_folder_list`

Lists folders recursively under a directory.

**Use case**
Use this tool to inspect the directory structure of a project or a selected subfolder.

**Features**
- Recursively scans nested directories.
- Returns folders only.
- Uses project layout directory discovery.
- Returns project-relative paths.
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
Use this tool for build, test, inspection, and other command-line operations that are allowed by the host security policy.

**Features**
- Runs commands from a project-confined working directory.
- Rejects absolute or outside-project working directories.
- Applies command deny-list checks before execution.
- Supports custom environment variables.
- Captures stdout and stderr.
- Stores a persisted command log.
- Returns a command ID and a bounded tail report.
- Enforces a process timeout.

**Input parameters**
- `command` *(string, required)*: Command to execute.
- `env` *(object/map, optional)*: Environment variables for the subprocess.
- `dir` *(string, optional)*: Working directory relative to the project. Default: `.`.
- `tail_result_size` *(integer, optional)*: Maximum characters returned from the end of the output. Default: `1024`.
- `charset_name` *(string, optional)*: Charset used to read command output. Default: `UTF-8`.

### `get_previous_log_chunk`

Retrieves an earlier fragment of a stored command log.

**Use case**
Use this tool after `run_sys_command` when the returned log tail was truncated and you need to page backward through older output.

**Features**
- Reads from the persisted command log for a command ID.
- Returns the log segment immediately before the current tail offset.
- Supports configurable chunk size.
- Supports configurable character decoding.

**Input parameters**
- `commandId` *(string, required)*: Command execution session ID.
- `tail_result_size` *(integer, optional)*: Size of the previous log fragment. Default: `1024`.
- `current_tail_offset` *(integer, required)*: Offset where the current visible tail starts.
- `charset_name` *(string, optional)*: Charset used to read the log. Default: `UTF-8`.

### `get_command_log_matches`

Searches a stored command log with a Java regular expression.

**Use case**
Use this tool to extract errors, warnings, generated IDs, test summaries, or any other structured text from command output.

**Features**
- Searches all lines in a persisted command log.
- Uses Java regular expression syntax.
- Returns every match.
- Includes matched text, start position, end position, and line number.

**Input parameters**
- `commandId` *(string, required)*: Command execution session ID.
- `regexp` *(string, required)*: Java regular expression to search for.
- `charset_name` *(string, optional)*: Charset used to read the log. Default: `UTF-8`.

### `end_task`

Ends the current task without terminating the application.

**Use case**
Use this tool only when the user explicitly asks to end the current task or when workflow logic requires graceful task completion while keeping the application available.

**Features**
- Ends only the active task.
- Keeps the host application running.
- Supports a custom completion message.
- Signals completion through a controlled task exception.

**Input parameters**
- `message` *(string, optional)*: Completion message. Default: `Execution terminated by function tool.`

### `terminate_execution`

Terminates the application with an exit code.

**Use case**
Use this tool only when the user explicitly requests termination or when a fatal workflow condition requires stopping the application.

**Features**
- Aborts the overall workflow.
- Supports a custom termination message.
- Supports a custom exit code.
- Signals termination through a controlled process exception.

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

**Input parameters**
- `root_dir` *(string, required)*: Absolute path to the root project directory or a folder containing multiple projects.
- `path` *(string, optional)*: Scanning path or pattern. Default: `glob:**/*.*`.

### `process_files_with_guidance_tag`

Starts asynchronous processing of files with `@guidance` tags.

**Use case**
Use this tool when files that match a path or pattern should be processed by the configured guidance workflow.

**Features**
- Processes matching guidance-tagged files in the background.
- Accepts optional processing properties.
- Resolves the model from properties or configuration.
- Stores a processing report in a temporary result file.
- Returns a GUID for later status/result retrieval.

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
- Reports that the result is not ready when no result file exists for the GUID.

**Input parameters**
- `guid` *(string, required)*: GUID returned by `process_files_with_guidance_tag`.

## Web and API tools

### `get_web_content`

Fetches content from a web page or local `file://` resource.

**Use case**
Use this tool when a workflow needs to read external web content, extract part of an HTML document, convert HTML to readable text, or load a local file URI.

**Features**
- Performs HTTP or HTTPS `GET` requests.
- Reads local resources through `file://` URIs.
- Supports Basic authentication through URL user info.
- Supports custom request headers.
- Supports request timeout configuration.
- Supports CSS selector extraction for HTML responses.
- Can render HTML as plain text.
- Returns an HTTP status line followed by response content for HTTP requests.

**Input parameters**
- `url` *(string, required)*: URL to fetch. May include user info for Basic authentication.
- `headers` *(string, optional)*: HTTP headers as `NAME=VALUE` lines separated by LF line breaks.
- `timeout` *(integer, optional)*: Maximum time to wait in milliseconds. Use `0` for the default connection behavior.
- `charset_name` *(string, optional)*: Charset used to decode the response. Default: `UTF-8`.
- `text_only` *(boolean, optional)*: If `true`, returns plain text for HTML content. Default: `false`.
- `selector` *(string, optional)*: CSS selector used to extract matching HTML elements.

### `call_rest_api`

Executes an HTTP request against a REST endpoint.

**Use case**
Use this tool when a workflow needs to call an API directly, including read requests and write-oriented requests with a request body.

**Features**
- Supports HTTP methods such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- Supports Basic authentication through URL user info.
- Supports custom request headers.
- Supports request bodies for `POST`, `PUT`, and `PATCH`.
- Supports request timeout configuration.
- Returns an HTTP status line followed by response content when available.

**Input parameters**
- `url` *(string, required)*: REST endpoint URL. May include user info for Basic authentication.
- `method` *(string, optional)*: HTTP method to use. Default behavior is intended for `GET`.
- `headers` *(string, optional)*: HTTP headers as `NAME=VALUE` lines separated by LF line breaks.
- `body` *(string, optional)*: Request body for `POST`, `PUT`, or `PATCH` requests.
- `timeout` *(integer, optional)*: Maximum time to wait in milliseconds. Use `0` for the default connection behavior.
- `charset_name` *(string, optional)*: Charset used to encode request bodies and decode responses. Default: `UTF-8`.
