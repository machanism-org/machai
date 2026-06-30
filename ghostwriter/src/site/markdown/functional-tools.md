---
<!-- @guidance: 
Create the `Function Tolls` page:
- Analyze classes in the folder: `/src/main/java/org/machanism/machai/gw/tools` and use this information to create the page content but don not mentionad this as a package details.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/ghostwriter/functional-tools.html
---

# Function Tools

Function tools are controlled capabilities that Ghostwriter can expose to an AI workflow. They help a workflow inspect and run Acts, manage Act episodes, store project state, work with project files, run approved commands, process guidance-tagged files, and retrieve information from web or REST resources.

Each tool has a focused purpose and accepts structured input parameters. This makes tool usage easier to understand, safer to automate, and simpler to audit.

## Tool groups

- **Act tools** inspect Act definitions, run Acts, and retrieve Act results.
- **Episode control tools** move to another Act episode or repeat the current episode.
- **Project context tools** store and retrieve project-scoped workflow variables.
- **File system tools** read, write, patch, and list project files and folders.
- **Command and task tools** run approved commands, inspect command logs, and control task completion.
- **Guidance tools** find and process files that contain `@guidance` tags.
- **Web and API tools** fetch web content and call REST endpoints.

## Act tools

### `load_act_details`

Loads information about a named Act template, including its instructions, input template, and configuration options.

**Use it when** you need to inspect an Act before running it, verify that an Act exists, or understand what input and configuration a workflow expects.

**Features**
- Searches for a named Act definition.
- Returns available custom and built-in Act details.
- Normalizes Act names by ignoring trailing fragments after `#` or a space.
- Returns a clear `act not found` message with diagnostic context when no Act is available.

**Input parameters**
- `act_name` *(string, required)*: Name of the Act to load.

### `perform_act`

Runs a named Act workflow against the current project.

**Use it when** you want Ghostwriter to execute a predefined workflow. The tool can run synchronously and return the result immediately, or asynchronously and return a process ID that can be polled later.

**Features**
- Executes the selected Act in the active project context.
- Supports synchronous and asynchronous execution.
- Accepts Act-specific property overrides.
- Resolves configuration placeholders in supplied property values.
- Uses the configured scan directory when available, or the current project as the default scan target.
- Returns either the Act result or a `processing` status with a `process_id`.

**Input parameters**
- `act_name` *(string, required)*: Name of the Act to perform.
- `properties` *(object, optional)*: Act properties that override default configuration values.
- `async` *(boolean, optional)*: If `true`, starts the Act in the background. If `false`, waits for completion. Default: `false`.

### `get_act_result`

Retrieves the result of an Act that was started asynchronously.

**Use it when** `perform_act` returned a `process_id` and you need to check whether the Act has completed.

**Features**
- Looks up a stored Act result by process ID.
- Returns `processing` when the result is not ready yet.
- Returns `done` with the Act result when processing has completed.
- Reports read failures as clear I/O errors.

**Input parameters**
- `process_id` *(string, required)*: Identifier returned by `perform_act`.

## Episode control tools

### `move_to_episode`

Moves an episode-based Act workflow to another episode.

**Use it when** an Act needs to branch to a specific step, skip ahead, return to a previous step, or continue at a named episode.

**Features**
- Signals an episode transition in the current Act workflow.
- Supports navigation by numeric episode ID.
- Supports navigation by episode name.
- Keeps episode control explicit and managed by the workflow engine.

**Input parameters**
- `id` *(integer, optional)*: ID of the episode to move to.
- `name` *(string, optional)*: Name of the episode to move to.

### `repeat_episode`

Repeats the current episode in an episode-based Act workflow.

**Use it when** the current episode should be retried, such as after failed validation, missing input, or an incomplete intermediate result.

**Features**
- Signals that the current episode should run again.
- Preserves the current workflow context.
- Can log a custom message before the episode is repeated.

**Input parameters**
- `message` *(string, optional)*: Custom response message to output before repeating the episode. Default: empty string.

## Project context tools

### `put_project_context_variable`

Stores or updates a named variable in the current project context.

**Use it when** a workflow needs to save state for later tool calls, Acts, prompt templates, or Act episodes.

**Features**
- Stores a value under a project-scoped variable name.
- Replaces any existing value with the same name.
- Serializes non-string values to JSON when used internally.
- Returns a confirmation message or a readable failure message.

**Input parameters**
- `name` *(string, required)*: Name of the context variable.
- `value` *(string, required)*: Value to assign to the context variable.

### `get_project_context_variable`

Retrieves a named variable from the current project context.

**Use it when** a workflow needs to read state that was saved earlier in the same project context.

**Features**
- Reads a project-scoped value by variable name.
- Returns the stored value as text.
- Reports when no context exists for the project.
- Reports retrieval errors in a readable form.

**Input parameters**
- `name` *(string, required)*: Name of the context variable to retrieve.

### `push_project_context_variable`

Adds a value to a project context variable.

**Use it when** a workflow needs to accumulate multiple values, such as pending files, generated IDs, validation notes, or follow-up tasks.

**Features**
- Creates a new list when the variable does not exist.
- Converts an existing string value into a list.
- Appends to an existing list.
- Reports unsupported variable types.

**Input parameters**
- `name` *(string, required)*: Name of the context variable.
- `value` *(string, required)*: Value to push to the context variable.

### `pop_project_context_variable`

Removes and returns a value from a project context variable.

**Use it when** a variable should behave like a stack or queue.

**Features**
- Pops list values in LIFO mode by default.
- Supports FIFO mode for queue-style processing.
- Removes and returns string values directly.
- Removes an empty list variable after its last value is popped.
- Converts a one-item list back to a string after popping from a longer list.
- Reports missing variables, empty lists, and unsupported variable types.

**Input parameters**
- `name` *(string, required)*: Name of the context variable.
- `mode` *(string, optional)*: Pop mode, either `LIFO` or `FIFO`. Default: LIFO behavior when omitted or not `FIFO`.

## File system tools

### `read_file`

Reads the full text content of a file.

**Use it when** a workflow needs to inspect source code, configuration, documentation, or another text file in the active project.

**Features**
- Reads a file relative to the project directory.
- Returns the file content as text.
- Supports configurable character decoding.
- Raises an I/O error if the file cannot be read.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to read.
- `charset_name` *(string, optional)*: Character set used to decode the file. Default: `UTF-8`.

### `write_file`

Creates a new file or replaces the complete content of an existing file.

**Use it when** the workflow has the full desired file content, such as when creating documentation, regenerating configuration, or replacing a generated file.

**Features**
- Writes the supplied text as the complete file content.
- Creates missing parent directories for new files.
- Updates existing files in place.
- Supports configurable character encoding.
- Returns a success message or an error message.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to create or update.
- `text` *(string, required)*: Content to write.
- `charset_name` *(string, optional)*: Character set used to write the file. Default: `UTF-8`.

### `list_files_in_directory`

Lists the immediate files and folders in a directory.

**Use it when** you need a quick, non-recursive view of a project folder.

**Features**
- Lists direct children of the requested directory.
- Uses the project root when `dir_path` is `.`.
- Returns project-relative paths using forward slashes.
- Returns an empty list when the directory is missing or has no readable children.

**Input parameters**
- `dir_path` *(string, optional)*: Directory to list. Default: `.`.

### `get_recursive_file_list`

Lists files recursively under a directory.

**Use it when** you need to discover all files under a folder, including nested files.

**Features**
- Recursively scans the selected directory.
- Returns files only.
- Returns project-relative paths using forward slashes.
- Returns `No files found in directory.` when no files are found.

**Input parameters**
- `dir` *(string, optional)*: Folder to scan recursively. Default: empty path, which represents the project root.

### `get_recursive_folder_list`

Lists folders recursively under a directory.

**Use it when** you need to inspect a project structure or discover nested directories.

**Features**
- Recursively scans the selected directory.
- Returns folders only.
- Returns project-relative paths using forward slashes.
- Returns `No folders found in directory.` when no folders are found.

**Input parameters**
- `dir` *(string, optional)*: Folder to scan recursively. Default: empty path, which represents the project root.

### `apply_patch_to_file`

Applies a unified diff patch to a file.

**Use it when** only a focused part of a file should change and a patch is safer than rewriting the full file.

**Features**
- Accepts unified diff content, such as output from `diff -u` or `git diff`.
- Applies targeted changes to the selected file.
- Supports configurable character encoding.
- Returns a success message or a patch failure message.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to patch.
- `patch` *(string, required)*: Unified diff patch to apply.
- `charset_name` *(string, optional)*: Character set used while applying the patch. Default: `UTF-8`.

## Command and task tools

### `run_sys_command`

Runs an approved system command in a project-confined working directory.

**Use it when** a workflow needs to run build, test, inspection, formatting, or other command-line tasks that are allowed by the host security policy.

**Features**
- Resolves the working directory relative to the project root.
- Rejects absolute working directories and paths outside the project.
- Applies command security checks before execution.
- Supports environment variable overrides.
- Resolves configuration placeholders in commands and environment values.
- Captures standard output and standard error.
- Stores command output in a log for later retrieval or search.
- Returns the process exit code and a bounded tail report.
- Enforces a command timeout and forcibly terminates timed-out processes.

**Input parameters**
- `command` *(string, required)*: Command to execute.
- `env` *(object, optional)*: Environment variables for the subprocess. If omitted, the subprocess inherits the current environment.
- `dir` *(string, optional)*: Working directory relative to the project directory. Default: `.`.
- `tail_result_size` *(integer, optional)*: Maximum number of characters to display from the end of command output. Default: `1024`.
- `charset_name` *(string, optional)*: Character encoding used to read command output. Default: `UTF-8`.

### `get_log_chunk`

Retrieves an earlier fragment of a stored command log.

**Use it when** `run_sys_command` returned only the tail of a long log and you need to page backward through earlier output.

**Features**
- Reads a command log by `log_id`.
- Returns the fragment immediately before the current tail offset.
- Supports configurable chunk size and character decoding.
- Reports an error when the log file cannot be found.

**Input parameters**
- `log_id` *(string, required)*: Identifier of the command execution session.
- `current_tail_offset` *(integer, required)*: Offset where the current visible tail starts.
- `tail_result_size` *(integer, optional)*: Number of characters to retrieve. Default: `1024`.
- `charset_name` *(string, optional)*: Character encoding used to read the log. Default: `UTF-8`.

### `get_log_matches`

Searches a stored command log for regular expression matches.

**Use it when** you need to extract errors, warnings, generated IDs, test summaries, or other structured details from command output.

**Features**
- Reads a command log by `log_id`.
- Uses Java regular expression syntax.
- Returns each match with matched text, start index, end index, and line number.
- Supports configurable character decoding.
- Reports an error when the log file cannot be found.

**Input parameters**
- `log_id` *(string, required)*: Identifier of the command execution session.
- `regexp` *(string, required)*: Java regular expression to search for.
- `charset_name` *(string, optional)*: Character encoding used to read the log. Default: `UTF-8`.

### `end_task`

Ends the current task without terminating the host application.

**Use it when** the user explicitly asks to end the current task, or when an interactive workflow should finish gracefully while leaving the application available.

**Features**
- Ends only the active task.
- Leaves the surrounding application or session running.
- Supports a custom completion message.

**Input parameters**
- `message` *(string, optional)*: Message to use upon completion. Default: `Execution terminated by function tool.`

### `terminate_execution`

Terminates the running workflow or application with an exit code.

**Use it when** the user explicitly requests termination or a fatal workflow condition requires stopping execution.

**Features**
- Stops the overall execution flow.
- Supports a custom termination message.
- Supports a custom exit code.
- Should not be called automatically after a successful task.

**Input parameters**
- `message` *(string, optional)*: Exception message to use. Default: `Execution terminated by function tool.`
- `exit_code` *(integer, optional)*: Exit code to return. Default: `0`.

## Guidance tools

### `get_files_with_guidance_tags`

Finds files that contain `@guidance` tags and groups them by project.

**Use it when** you need to discover guidance-tagged files before processing them or before planning a bulk update.

**Features**
- Scans a root directory or a folder containing multiple projects.
- Supports raw paths, glob patterns, and regex patterns.
- Collects files that contain guidance annotations.
- Groups discovered files by their project directory.

**Input parameters**
- `root_dir` *(string, required)*: Absolute path to the root project directory or to a folder containing multiple projects.
- `path` *(string, optional)*: Scanning path or pattern. Supports raw directory names, `glob:` patterns, and `regex:` patterns. Default: `glob:**/*.*`.

### `process_files_with_guidance_tag`

Processes files that contain `@guidance` tags.

**Use it when** guidance-tagged files should be updated or reviewed according to their embedded guidance instructions.

**Features**
- Scans files matching the supplied path or pattern.
- Processes discovered guidance tags using the configured model.
- Supports synchronous and asynchronous execution.
- Accepts processing properties and configuration overrides.
- Resolves configuration placeholders in property values.
- Returns either a processing report or a `processing` status with a `process_id`.

**Input parameters**
- `path` *(string, optional)*: Scanning path or pattern relative to the current project, or an allowed absolute path within the configured scan root.
- `properties` *(object, optional)*: Processing properties or configuration overrides.
- `async` *(boolean, optional)*: If `true`, starts processing in the background. If `false`, waits for completion. Default: `false`.

### `get_process_guidance_tag_files_result`

Retrieves the result of a guidance-tag processing run that was started asynchronously.

**Use it when** `process_files_with_guidance_tag` returned a `process_id` and you need to check whether processing has completed.

**Features**
- Looks up a stored guidance processing result by process ID.
- Returns `processing` when the result is not ready yet.
- Returns `done` with the processing report when available.
- Includes the original `process_id` in the response.

**Input parameters**
- `process_id` *(string, required)*: Identifier returned by `process_files_with_guidance_tag`.

## Web and API tools

### `get_web_content`

Fetches content from a web page or a local `file:` URI.

**Use it when** a workflow needs to read external documentation, inspect a web page, extract part of an HTML document, convert HTML to plain text, or load a local file URI.

**Features**
- Performs HTTP and HTTPS `GET` requests.
- Reads local resources through `file:` URIs.
- Supports Basic authentication through URL user information.
- Supports custom request headers.
- Resolves configuration placeholders in the URL and header values.
- Supports request timeout configuration.
- Supports CSS selector extraction for HTML responses.
- Can return selected HTML or rendered plain text.
- Includes the HTTP status line for HTTP responses.

**Input parameters**
- `url` *(string, required)*: URL to fetch. May include user information for Basic authentication.
- `headers` *(object, optional)*: HTTP headers to send. If omitted, no additional headers are sent.
- `timeout` *(integer, optional)*: Maximum time to wait in milliseconds. A value of `0` uses the connection defaults.
- `charset_name` *(string, optional)*: Character set used to decode the response. Default: `UTF-8`.
- `text_only` *(boolean, optional)*: If `true`, returns plain text instead of HTML. Default: `false`.
- `selector` *(string, optional)*: CSS selector used to extract matching HTML elements. Default: empty string.

### `call_rest_api`

Executes an HTTP request against a REST endpoint.

**Use it when** a workflow needs to call an API directly, including read requests and write-oriented requests with a body.

**Features**
- Supports HTTP methods such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- Supports Basic authentication through URL user information.
- Supports custom request headers.
- Resolves configuration placeholders in the URL and header values.
- Supports request bodies for `POST`, `PUT`, and `PATCH`.
- Supports request timeout configuration.
- Returns an HTTP status line followed by the response body when available.
- Returns a clear I/O error message when the call fails.

**Input parameters**
- `url` *(string, required)*: REST endpoint URL. May include user information for Basic authentication.
- `method` *(string, optional)*: HTTP method to use. Common values include `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- `headers` *(object, optional)*: HTTP headers to send. If omitted, no additional headers are sent.
- `body` *(string, optional)*: Request body for methods that support content.
- `timeout` *(integer, optional)*: Maximum time to wait in milliseconds. A value of `0` uses the connection defaults.
- `charset_name` *(string, optional)*: Character set used to encode request bodies and decode responses. Default: `UTF-8`.
