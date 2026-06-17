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

Ghostwriter provides function tools that let a model inspect Act templates, run Acts, manage project-scoped workflow state, navigate episode-based flows, work with files inside the active project, run approved command-line operations, inspect stored command logs, discover and process `@guidance` files, and retrieve content from web pages or REST endpoints.

## Tool groups

- **Act tools** load Act definitions, start Act execution, and retrieve asynchronous Act results.
- **Episode control tools** move execution to another episode or repeat the current one.
- **Project context tools** store and retrieve project-scoped values across workflow steps.
- **File system tools** read, write, patch, and enumerate files and folders relative to the active project directory.
- **Command and task tools** run approved commands, inspect saved command logs, and end or terminate execution when needed.
- **Guidance tools** find files with `@guidance` tags and process them asynchronously.
- **Web and API tools** fetch web content and call HTTP endpoints.

## Act tools

### `load_act_details`
Loads the details of a specific Act template.

**Description**
Use this tool when you need to inspect how an Act is defined before running it. Ghostwriter looks for the requested Act in the configured custom Acts location and in the built-in classpath resources, then returns whichever definitions exist.

**Features**
- Loads details for one Act by name.
- Checks both custom Acts and built-in packaged Acts.
- Returns structured Act metadata when found.
- Helps compare overridden custom Acts with built-in defaults.

**Input parameters**
- `act_name` *(string, required)*: The name of the Act to load.

### `perform_act`
Starts execution of a specific Act by name.

**Description**
Use this tool when a workflow needs to trigger an Act as a background operation. Ghostwriter builds an `ActProcessor`, applies optional properties, resolves the model and scan directory, scans project documents, and starts execution asynchronously.

**Features**
- Starts an Act by name.
- Uses the current project as the Act execution context.
- Accepts optional Act properties as newline-separated `NAME=VALUE` pairs.
- Resolves the model from properties or configuration.
- Scans project documents before collecting Act results.
- Returns a GUID so the result can be retrieved later.

**Input parameters**
- `act_name` *(string, required)*: The name of the Act to perform.
- `properties` *(string, optional)*: Act properties as `NAME=VALUE` pairs separated by LF line breaks.

### `get_act_result`
Retrieves the result of a previously started Act.

**Description**
Use this tool after `perform_act` when you want to check whether an asynchronous Act has finished. Ghostwriter looks for a temporary result file associated with the GUID and returns either the completed result or a processing status.

**Features**
- Reads the stored result of a previously started Act.
- Returns `processing` until the result file is available.
- Returns `done` together with the Act result when finished.
- Uses a GUID returned by `perform_act`.

**Input parameters**
- `guid` *(string, required)*: The GUID returned when the Act was started.

## Episode control tools

### `move_to_episode`
Moves execution to the next episode or to a specified episode.

**Description**
Use this tool inside an episode-based Act when workflow control needs to jump forward or branch. Ghostwriter signals the episode transition internally by throwing a workflow-control exception.

**Features**
- Supports moving to a specific episode by numeric ID.
- Supports moving to a specific episode by name.
- Can be used for branching or guided progression.
- Integrates with episode-based Act execution.

**Input parameters**
- `id` *(integer, required by implementation when used positionally)*: The ID of the episode to move to.
- `name` *(string, required by implementation when used positionally)*: The name of the episode to move to.

### `repeate_episode`
Repeats the current episode.

**Description**
Use this tool when the current episode should be retried without losing workflow context. It optionally logs a message, then signals Ghostwriter to restart the same episode.

**Features**
- Repeats the current episode.
- Preserves the existing workflow context.
- Supports an optional message before the repeat occurs.
- Useful for retry loops and correction flows.

**Input parameters**
- `message` *(string, optional)*: A custom response message to output before repeating the episode.

## Project context tools

### `put_project_context_variable`
Stores or updates a variable in the current project context.

**Description**
Use this tool to save a named value that later steps in the same project can read. It is useful for passing state between Acts, episodes, or multiple tool calls.

**Features**
- Stores a project-scoped variable by name.
- Replaces the existing value when the name already exists.
- Keeps workflow state associated with the current project.
- Accepts string values directly.

**Input parameters**
- `name` *(string, required)*: The name of the context variable.
- `value` *(string, required)*: The value to assign to the context variable.

### `get_project_context_variable`
Retrieves a value from the current project context.

**Description**
Use this tool when you need to read a value that was stored earlier for the active project. If the variable or project context is missing, Ghostwriter returns a readable status message.

**Features**
- Reads previously stored project-scoped variables.
- Returns the stored string value when present.
- Reports when the project has no context yet.
- Reports when the requested variable does not exist.

**Input parameters**
- `name` *(string, required)*: The name of the context variable to retrieve.

### `push_project_context_variable`
Appends a value to a project context variable.

**Description**
Use this tool when a context variable should collect multiple values over time. Ghostwriter creates a list when needed and automatically converts an existing string value into a list before appending the new item.

**Features**
- Appends values to a project-scoped list.
- Creates a new list automatically when the variable does not exist.
- Converts an existing string value into a list when needed.
- Useful for accumulating workflow items across steps.

**Input parameters**
- `name` *(string, required)*: The name of the context variable.
- `value` *(string, required)*: The value to push to the context variable.

### `pop_project_context_variable`
Removes and returns a value from a project context variable.

**Description**
Use this tool when a stored project variable should behave like a stack or queue. Ghostwriter supports both `LIFO` and `FIFO` removal for list values and also removes plain string variables directly.

**Features**
- Removes and returns one stored value.
- Supports `LIFO` and `FIFO` behavior for lists.
- Removes plain string values directly.
- Cleans up the variable when the last list item is removed.

**Input parameters**
- `name` *(string, required)*: The name of the context variable.
- `mode` *(string, optional)*: Pop mode, either `LIFO` or `FIFO`.

## File system tools

### `read_file`
Reads a text file from the project file system.

**Description**
Use this tool to inspect the contents of a project file. The path is resolved relative to the active project directory, and the file is decoded with the selected character set.

**Features**
- Reads an entire file as text.
- Resolves paths relative to the active project directory.
- Supports configurable text decoding.
- Returns `File not found.` when the file is missing.

**Input parameters**
- `file_path` *(string, required)*: The path to the file to be read.
- `charset_name` *(string, optional)*: The requested charset. Default: `UTF-8`.

### `write_file`
Creates a file or replaces the full contents of an existing file.

**Description**
Use this tool when you need to write complete text content to a file in the current project. Missing parent directories are created automatically for new files.

**Features**
- Creates new files when they do not exist.
- Replaces the full content of existing files.
- Creates missing parent directories automatically.
- Supports configurable text encoding.

**Input parameters**
- `file_path` *(string, required)*: The path to the file to create or update.
- `text` *(string, required)*: The full text content to write.
- `charset_name` *(string, optional)*: The requested charset. Default: `UTF-8`.

### `list_files_in_directory`
Lists the immediate contents of a folder.

**Description**
Use this tool when you need a quick view of files and subdirectories directly under one directory. It does not recurse into nested folders.

**Features**
- Lists immediate children of a directory.
- Includes both files and folders.
- Uses the project root when `dir_path` is omitted.
- Returns paths relative to the current project.

**Input parameters**
- `dir_path` *(string, optional)*: The path to the directory to inspect. Default: `.`.

### `get_recursive_file_list`
Recursively lists files under a directory.

**Description**
Use this tool when you need a deeper file inventory. Ghostwriter walks the directory tree by using project layout rules and returns relative paths for files found under the requested root.

**Features**
- Recursively scans nested directories.
- Returns files only.
- Produces project-relative paths.
- Uses project layout rules while scanning.

**Input parameters**
- `dir` *(string, optional)*: Path to the folder to list recursively. If omitted, the project root is used.

### `get_recursive_folder_list`
Recursively lists folders under a directory.

**Description**
Use this tool when you need to inspect the directory structure instead of individual files. Ghostwriter traverses nested directories and returns project-relative folder paths.

**Features**
- Recursively scans nested directories.
- Returns folders only.
- Produces project-relative paths.
- Uses project layout directory discovery.

**Input parameters**
- `dir` *(string, optional)*: Path to the folder to list recursively. If omitted, the project root is used.

### `apply_patch_to_file`
Applies a unified diff patch to a file.

**Description**
Use this tool when you want to make a small, targeted update instead of rewriting an entire file. The patch must be supplied in standard unified diff format.

**Features**
- Applies unified diff hunks to a target file.
- Designed for focused file updates.
- Supports configurable character encoding.
- Returns a success or failure message.

**Input parameters**
- `file_path` *(string, required)*: The path to the file to be patched.
- `patch` *(string, required)*: The unified diff patch content.
- `charset_name` *(string, optional)*: The requested charset. Default: `UTF-8`.

## Command and task tools

### `run_command_line_tool`
Executes a system command inside the current project.

**Description**
Use this tool for approved command-line tasks such as builds, tests, or repository inspection. Ghostwriter resolves the working directory, performs deny-list security checks, starts the process, captures stdout and stderr, persists the command log, and returns a structured report.

**Features**
- Runs commands inside the current project tree.
- Rejects invalid or unsafe commands.
- Supports custom environment variables.
- Supports choosing a working directory relative to the project.
- Captures and stores stdout and stderr.
- Returns a command ID and log report.
- Limits the returned log view to a configurable tail size.

**Input parameters**
- `command` *(string, required)*: The command to execute.
- `env` *(string, optional)*: Environment variables as `NAME=VALUE` pairs separated by LF line breaks.
- `dir` *(string, optional)*: Working directory relative to the project directory. Default: `.`.
- `tail_result_size` *(integer, optional)*: Maximum number of characters returned from the end of command output. Default: `1024`.
- `charset_name` *(string, optional)*: Character encoding used to read command output. Default: `UTF-8`.

### `get_previous_log_chunk`
Retrieves an earlier fragment of a stored command log.

**Description**
Use this tool after `run_command_line_tool` when the returned log was truncated and you need to page backward through older output.

**Features**
- Reads earlier output from a persisted command log.
- Supports backward paging through long command results.
- Uses the command execution session ID.
- Supports configurable character decoding.

**Input parameters**
- `commandId` *(string, required)*: The identifier of the command execution session.
- `tail_result_size` *(integer, optional)*: The size of the earlier log fragment to retrieve. Default: `1024`.
- `current_tail_offset` *(integer, required)*: The offset where the current visible tail starts.
- `charset_name` *(string, optional)*: The character encoding to use for reading log output. Default: `UTF-8`.

### `get_command_log_matches`
Searches a stored command log by regular expression.

**Description**
Use this tool when you need to extract matching text from the output of a previously executed command. Ghostwriter applies a Java regular expression to the persisted log and returns every match with metadata.

**Features**
- Searches saved command output using a Java regular expression.
- Returns all matches, not only the first one.
- Includes matched text and position details.
- Useful for extracting warnings, errors, IDs, or custom patterns.

**Input parameters**
- `commandId` *(string, required)*: The identifier of the command execution session.
- `regexp` *(string, required)*: The Java regular expression to search for in the log.
- `charset_name` *(string, optional)*: The character encoding to use for reading log output. Default: `UTF-8`.

### `end_task`
Ends the current task without terminating the application.

**Description**
Use this tool when the user explicitly wants to finish the current task but keep the application running for later work. Internally, Ghostwriter signals task completion through a control exception.

**Features**
- Ends only the current task.
- Keeps the application running.
- Supports a custom completion message.
- Intended for controlled workflow completion.

**Input parameters**
- `message` *(string, optional)*: The message to use upon completion.

### `terminate_execution`
Terminates the application with an exit code.

**Description**
Use this tool only when execution should stop immediately, such as during a fatal validation failure or an explicitly requested shutdown.

**Features**
- Stops execution immediately.
- Supports a custom exit message.
- Supports a custom exit code.
- Intended for controlled termination scenarios.

**Input parameters**
- `message` *(string, optional)*: The exception message to use. Default: `Execution terminated by function tool.`
- `exit_code` *(integer, optional)*: The exit code to return. Default: `0`.

## Guidance tools

### `get_files_with_guidance_tags`
Finds files that contain `@guidance` tags.

**Description**
Use this tool when you want to discover which files in a project or project root contain guidance annotations. Ghostwriter scans matching paths and returns a mapping from project directories to files containing guidance tags.

**Features**
- Scans for files that contain `@guidance` markers.
- Supports raw paths, glob patterns, and regex patterns.
- Groups results by project directory.
- Useful before bulk guidance processing.

**Input parameters**
- `root_dir` *(string, required)*: The root project directory or a folder containing multiple projects.
- `path` *(string, optional)*: Scanning path or pattern relative to the current project. Default: `glob:**/*.*`.

### `process_files_with_guidance_tag`
Processes files with `@guidance` tags asynchronously.

**Description**
Use this tool when matching files should be processed by the configured guidance workflow. Ghostwriter creates a `GuidanceProcessor`, applies optional properties, scans the requested path, and stores the result for later retrieval.

**Features**
- Starts asynchronous processing of guidance-tagged files.
- Accepts optional properties as newline-separated `NAME=VALUE` pairs.
- Scans matching files under the current project.
- Returns a GUID for later result retrieval.

**Input parameters**
- `properties` *(string, optional)*: Processing properties as `NAME=VALUE` pairs separated by LF line breaks.
- `path` *(string, optional)*: Scanning path or pattern relative to the current project.

### `get_process_guidance_tag_files_result`
Retrieves the result of a previously started guidance-processing run.

**Description**
Use this tool after `process_files_with_guidance_tag` to check whether asynchronous guidance processing has finished and to obtain the stored report when it is ready.

**Features**
- Reads the stored result of a guidance-processing task.
- Returns `processing` while the task is still running.
- Returns `done` together with the processing report when finished.
- Uses the GUID returned by the start tool.

**Input parameters**
- `guid` *(string, required)*: The GUID returned when the processing was started.

## Web and API tools

### `get_web_content`
Fetches content from a web page using HTTP GET.

**Description**
Use this tool to download web content or read a local `file://` resource. Ghostwriter supports optional headers, optional timeout, selector-based extraction for HTML, plain-text rendering, and Basic authentication through URL user information.

**Features**
- Fetches content over HTTP or HTTPS with `GET`.
- Can read local content through a `file://` URL.
- Supports custom request headers.
- Supports timeout configuration.
- Supports CSS selector extraction for HTML.
- Can render HTML as plain text.
- Supports Basic authentication through URL user information.
- Returns the HTTP status line together with response content for HTTP requests.

**Input parameters**
- `url` *(string, required)*: The URL of the web page to fetch.
- `headers` *(string, optional)*: HTTP headers as `NAME=VALUE` pairs separated by LF line breaks.
- `timeout` *(integer, optional)*: Maximum time to wait for the response in milliseconds.
- `charset_name` *(string, optional)*: Character set used to decode the response. Default: `UTF-8`.
- `text_only` *(boolean, optional)*: If `true`, returns plain text instead of raw HTML.
- `selector` *(string, optional)*: CSS selector used to extract matching elements from HTML content.

### `call_rest_api`
Executes a REST API call to a remote endpoint.

**Description**
Use this tool when a workflow needs to send an HTTP request directly to an API. Ghostwriter supports multiple HTTP methods, headers, optional request bodies, configurable timeouts, and Basic authentication through URL user information.

**Features**
- Supports methods such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- Supports custom headers.
- Supports request bodies for write-oriented methods.
- Supports configurable timeouts.
- Supports Basic authentication through URL user information.
- Returns the HTTP status line together with the response body.

**Input parameters**
- `url` *(string, required)*: The URL of the REST endpoint.
- `method` *(string, optional)*: The HTTP method to use.
- `headers` *(string, optional)*: HTTP headers as `NAME=VALUE` pairs separated by LF line breaks.
- `body` *(string, optional)*: The request body to send.
- `timeout` *(integer, optional)*: Maximum time to wait for the response in milliseconds.
- `charset_name` *(string, optional)*: Character set used to decode the response content. Default: `UTF-8`.
