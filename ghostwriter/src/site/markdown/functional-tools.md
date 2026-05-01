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

Ghostwriter provides functional tools that let a model inspect Act templates, manage project-scoped context, navigate multi-episode workflows, work with files inside the active project, run approved command-line processes, and retrieve content from web and REST endpoints.

## Act and workflow tools

### `build_in_list_acts`
Lists the built-in Act templates packaged with Ghostwriter.

**What it does**
- Scans the built-in `acts` resources bundled with the application.
- Detects Act definitions stored as `.toml` files.
- Loads each Act description and formats the result as a readable list.

**Features**
- Helps you quickly discover which built-in Acts are available.
- Returns a concise summary rather than raw TOML content.
- Useful when choosing an Act for a workflow or exploring Ghostwriter capabilities.

**Input parameters**
This tool does not take any input parameters.

### `load_act_details`
Loads the details of a specific Act template.

**What it does**
- Resolves an Act by the provided name.
- Can load the effective Act, only the custom Act, or only the built-in Act.
- Returns the collected Act properties for inspection.

**Features**
- Helps review an Act before running or editing it.
- Useful for comparing built-in and custom versions of the same Act.
- Supports troubleshooting when Act resolution does not behave as expected.

**Input parameters**
- `actName` *(string, required)*: Name of the Act to load.
- `custom` *(boolean, optional)*:
  - `true`: Load only the user-defined Act from the configured Acts directory.
  - `false`: Load only the built-in packaged Act.
  - omitted: Load the effective Act using the normal resolution process.

### `put_project_context_variable`
Stores or updates a variable in the current project context.

**What it does**
- Saves a name/value pair for the active project directory.
- Keeps the value in project-scoped context storage.
- Makes the saved value available to later workflow steps.

**Features**
- Useful for passing values between tool calls.
- Helps multi-step Acts keep temporary state.
- Keeps values tied to the current project instead of sharing them globally.

**Input parameters**
- `name` *(string, required)*: Name of the context variable.
- `value` *(string, required)*: Value to store.

### `get_project_context_variable`
Retrieves a value from the current project context.

**What it does**
- Looks up a previously stored variable by name.
- Searches only within the context associated with the active project directory.
- Returns the stored value when it exists.

**Features**
- Useful for continuing multi-step workflows.
- Lets Acts and prompts reuse values saved earlier.
- Returns clear messages when the project context or variable is missing.

**Input parameters**
- `name` *(string, required)*: Name of the context variable to retrieve.

### `move_to_episode`
Moves execution to the next episode or to a specific episode.

**What it does**
- Signals Ghostwriter to change workflow execution to another episode.
- Moves to the next episode when no identifier is provided.
- Targets a specific episode when an ID is supplied.
- Uses an internal exception-based mechanism to trigger navigation.

**Features**
- Supports branching and controlled multi-episode flows.
- Works for both sequential progression and explicit episode jumps.
- Provides a simple control mechanism for Act orchestration.

**Input parameters**
- `id` *(string, optional)*: ID of the episode to move to. If omitted, Ghostwriter moves to the next episode.

## File system tools

### `read_file_from_file_system`
Reads a text file from the file system.

**What it does**
- Opens a file relative to the current project working directory.
- Reads the entire file as text.
- Uses the requested character encoding, or `UTF-8` by default.

**Features**
- Useful for reviewing source files, configuration files, templates, and documentation.
- Returns the exact current content of the selected file.
- Returns `File not found.` when the target file does not exist.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to read.
- `charsetName` *(string, optional)*: Character encoding to use. Default: `UTF-8`.

### `write_file_to_file_system`
Writes text content to a file on disk.

**What it does**
- Writes the provided text to a file relative to the current project working directory.
- Updates an existing file or creates a new one when needed.
- Creates missing parent directories before writing a new file.
- Uses the requested character encoding, or `UTF-8` by default.

**Features**
- Supports full-file creation and replacement.
- Useful for generating or updating source code, documentation, and configuration files.
- Returns a clear success message for both new and updated files.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to create or update.
- `text` *(string, required)*: Full text content to write.
- `charsetName` *(string, optional)*: Character encoding to use. Default: `UTF-8`.

### `list_files_in_directory`
Lists files and directories directly inside a specific folder.

**What it does**
- Reads the immediate contents of a directory.
- Returns project-relative paths.
- Does not recurse into nested folders.
- Uses the current working directory when no path is provided.

**Features**
- Useful for getting a quick overview of a folder.
- Helps identify candidate files before reading or updating them.
- Returns a compact comma-separated list.

**Input parameters**
- `dir_path` *(string, optional)*: Path to the directory to inspect. If omitted or blank, the current working directory is used.

### `get_recursive_file_list`
Recursively lists files under a directory.

**What it does**
- Traverses the selected directory and all nested subdirectories.
- Returns files only, not directories.
- Produces project-relative paths with forward slashes.
- Skips excluded directories defined by the project layout.
- Uses the current working directory when no path is provided.

**Features**
- Useful for collecting a full file inventory under a source, test, or documentation folder.
- Helps locate files before analysis or editing.
- Avoids excluded directories during recursive scanning.

**Input parameters**
- `dir_path` *(string, optional)*: Root directory to scan recursively. If omitted or blank, the current working directory is used.

## Command and process tools

### `run_command_line_tool`
Executes a system command from inside the current project.

**What it does**
- Runs a command by using Java process execution on the host machine.
- Restricts the working directory to the current project directory or one of its subdirectories.
- Can add environment variables for the subprocess.
- Captures both standard output and error output.
- Returns only the last part of the collected output when the result is larger than the configured limit.

**Features**
- Supports controlled shell access for build, test, and inspection commands.
- Applies security checks to reject unsafe command fragments.
- Prevents execution outside the active project tree.
- Adds an exit-code line to the returned output.
- Supports configurable output decoding and timeout handling.

**Input parameters**
- `command` *(string, required)*: Command to execute.
- `env` *(string, optional)*: Environment variables as `NAME=VALUE` pairs separated by LF line breaks.
- `dir` *(string, optional)*: Working directory relative to the project directory. Must stay inside the project. Default: current project directory.
- `tailResultSize` *(integer, optional)*: Maximum number of characters returned from the end of the output. Default: `1024`.
- `charsetName` *(string, optional)*: Character encoding used to read command output. Default: `UTF-8`.

### `terminate_process`
Terminates the current workflow by throwing a process termination exception.

**What it does**
- Stops execution immediately.
- Returns control to the host application with a message and exit code.
- Can attach a cause message for additional error context.

**Features**
- Useful for fatal validation failures or controlled shutdown conditions.
- Lets a tool invocation end the workflow intentionally.
- Supports a custom exit code instead of always using a generic failure value.

**Input parameters**
- `message` *(string, optional)*: Exception message to use. Default: `Process terminated by function tool.`
- `cause` *(string, optional)*: Optional cause message wrapped as the underlying exception cause.
- `exitCode` *(integer, optional)*: Exit code to return. Default: `1`.

## Web and API tools

### `get_web_content`
Fetches content from a web page by using an HTTP GET request.

**What it does**
- Downloads content from an HTTP or HTTPS URL.
- Supports Basic authentication through URL user information.
- Can apply custom request headers.
- Can return the full response, selected HTML content, or plain text only.
- Can also read from a `file:` URL relative to the current working directory when needed.

**Features**
- Useful for pulling reference pages, documentation, and structured page fragments.
- Supports CSS selector extraction for targeted content retrieval.
- Can strip HTML and return readable text when `textOnly` is enabled.
- Includes the HTTP status line in the response.
- Supports configurable timeout and response decoding.

**Input parameters**
- `url` *(string, required)*: URL of the page to fetch. Supports user information for Basic authentication.
- `headers` *(string, optional)*: HTTP headers as `NAME=VALUE` pairs separated by LF line breaks.
- `timeout` *(integer, optional)*: Maximum time to wait for the response in milliseconds. Default internal timeout: `10000`.
- `charsetName` *(string, optional)*: Character encoding used to decode the response. Default: `UTF-8`.
- `textOnly` *(boolean, optional)*: If `true`, returns plain text instead of raw HTML.
- `selector` *(string, optional)*: CSS selector used to extract only matching elements from HTML content.

### `call_rest_api`
Executes a REST API call to a remote endpoint.

**What it does**
- Sends an HTTP request to the specified URL.
- Supports configurable HTTP methods such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- Can include custom headers and an optional request body.
- Supports Basic authentication through URL user information.
- Returns the HTTP status line together with the response body.

**Features**
- Useful for calling JSON APIs, webhooks, and service endpoints from a workflow.
- Supports request timeouts and character-set configuration.
- Writes request bodies for `POST`, `PUT`, and `PATCH` requests.
- Reuses the same header handling used by the web content tool.

**Input parameters**
- `url` *(string, required)*: URL of the REST endpoint. Supports user information for Basic authentication.
- `method` *(string, optional)*: HTTP method to use. Default: `GET`.
- `headers` *(string, optional)*: HTTP headers as `NAME=VALUE` pairs separated by LF line breaks.
- `body` *(string, optional)*: Request body to send for methods that support a payload.
- `timeout` *(integer, optional)*: Maximum time to wait for the response in milliseconds. Default internal timeout: `10000`.
- `charsetName` *(string, optional)*: Character encoding used to decode the response content. Default: `UTF-8`.
