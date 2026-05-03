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

Ghostwriter provides function tools that let a model inspect available Acts, manage project-scoped workflow state, control episode flow, work with files in the active project, run approved shell commands, and retrieve content from web pages or REST APIs.

## Tool groups

- **Act and workflow tools** help discover Act templates, inspect their definitions, store temporary workflow values, and control episode transitions.
- **File system tools** help read, write, and enumerate files relative to the active project directory.
- **Command and process tools** help run validated commands inside the project and stop execution when necessary.
- **Web and API tools** help download page content and call HTTP endpoints.

## Act and workflow tools

### `build_in_list_acts`
Lists the built-in Act templates packaged with Ghostwriter.

**Description**
Use this tool when you want a quick overview of the built-in Acts that ship with Ghostwriter. It reads the packaged `acts/*.toml` resources, loads each Act description, and returns a readable list.

**Features**
- Discovers built-in Acts bundled with the application.
- Returns a user-friendly list instead of raw TOML files.
- Includes each Act name together with its description.
- Useful when choosing an Act to inspect or run next.

**Input parameters**
This tool does not take any input parameters.

### `load_act_details`
Loads the details of a specific Act template.

**Description**
Use this tool when you need to inspect one Act in detail. It resolves the requested Act by name and returns its collected properties, such as instructions, prompts, and other Act configuration values. It can load the effective Act, only the custom Act, or only the built-in version.

**Features**
- Loads one Act by name.
- Supports effective resolution or explicit built-in/custom lookup.
- Useful for reviewing an Act before editing or executing it.
- Returns a readable message if the Act cannot be resolved.

**Input parameters**
- `actName` *(string, required)*: Name of the Act to load.
- `custom` *(boolean, optional)*:
  - `true`: Load only the user-defined Act.
  - `false`: Load only the built-in packaged Act.
  - omitted: Load the effective Act using normal resolution.

### `put_project_context_variable`
Stores or updates a variable in the current project context.

**Description**
Use this tool to save a named value for the active project so later steps in the same workflow can reuse it. The value is stored only for the current project context, which makes it useful for passing temporary state between tool calls or episodes.

**Features**
- Saves a name/value pair for the current project.
- Supports updating an existing variable.
- Helps multi-step workflows share state.
- Keeps values isolated to the active project directory.

**Input parameters**
- `name` *(string, required)*: Name of the context variable.
- `value` *(string, required)*: Value to store.

### `get_project_context_variable`
Retrieves a value from the current project context.

**Description**
Use this tool to read a value that was previously stored with `put_project_context_variable`. It searches the project-scoped context for the active working directory and returns the stored value or a readable message when the variable is missing.

**Features**
- Reads previously stored workflow values.
- Looks up variables only inside the active project context.
- Useful for continuing multi-step or multi-episode flows.
- Returns clear feedback when a context or variable does not exist.

**Input parameters**
- `name` *(string, required)*: Name of the context variable to retrieve.

### `move_to_episode`
Moves execution to the next episode or to a specific episode.

**Description**
Use this tool to control episode-based workflow navigation. If no ID is supplied, Ghostwriter advances to the next episode. If an ID is provided, execution jumps directly to that episode.

**Features**
- Supports sequential workflow progression.
- Supports explicit jumps to a named episode.
- Useful for branching and guided workflow control.
- Designed for Act orchestration rather than data retrieval.

**Input parameters**
- `id` *(string, optional)*: ID of the episode to move to. If omitted, Ghostwriter moves to the next episode.

### `repeate_episode`
Repeats the current episode.

**Description**
Use this tool when the current episode should be retried. Ghostwriter restarts the same episode and preserves the existing project context, which allows a workflow to retry after updating temporary state.

**Features**
- Repeats the current episode.
- Preserves project-scoped context values.
- Useful for retry and loop-like workflow behavior.
- Can log a custom message before the repeat happens.

**Input parameters**
- `message` *(string, optional)*: Custom response message to output before repeating the episode.

## File system tools

### `read_file_from_file_system`
Reads a text file from the file system.

**Description**
Use this tool to inspect the current contents of a file in the active project. The path is resolved relative to the working directory provided by Ghostwriter, and the file is returned as text using the selected character set.

**Features**
- Reads full file content as text.
- Works with paths relative to the current project.
- Supports custom text decoding.
- Returns `File not found.` when the target file does not exist.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to read.
- `charsetName` *(string, optional)*: Character encoding to use. Default: `UTF-8`.

### `write_file_to_file_system`
Writes text content to a file on disk.

**Description**
Use this tool to create a new file or fully replace the contents of an existing file. The path is resolved relative to the active project directory, and missing parent directories are created automatically when needed.

**Features**
- Creates new files when they do not exist.
- Replaces the full content of existing files.
- Creates missing parent directories automatically.
- Supports configurable character encoding.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to create or update.
- `text` *(string, required)*: Full text content to write.
- `charsetName` *(string, optional)*: Character encoding to use. Default: `UTF-8`.

### `list_files_in_directory`
Lists files and directories directly inside a specific folder.

**Description**
Use this tool when you need a quick overview of the immediate contents of a folder. It does not recurse into nested subdirectories and returns project-relative paths.

**Features**
- Lists immediate children of a directory.
- Returns both files and directories.
- Does not scan nested folders.
- Uses the current working directory when no path is provided.

**Input parameters**
- `dir_path` *(string, optional)*: Path to the directory to inspect. If omitted or blank, the current working directory is used.

### `get_recursive_file_list`
Recursively lists files under a directory.

**Description**
Use this tool when you need a deeper inventory of files under a folder. It traverses nested subdirectories, returns files only, and skips excluded directories defined by the project layout.

**Features**
- Recursively scans subdirectories.
- Returns files only, not directories.
- Produces project-relative paths.
- Skips excluded directories while scanning.

**Input parameters**
- `dir_path` *(string, optional)*: Root directory to scan recursively. If omitted or blank, the current working directory is used.

## Command and process tools

### `run_command_line_tool`
Executes a system command from inside the current project.

**Description**
Use this tool to run approved shell commands for tasks such as builds, tests, or project inspection. The command runs inside the current project or one of its subdirectories, captures both standard output and error output, and returns only the tail of the collected output when the result is large.

**Features**
- Runs commands inside the current project tree.
- Rejects unsafe command fragments through deny-list checks.
- Supports custom environment variables.
- Captures both stdout and stderr.
- Limits returned output to a configurable tail size.
- Reports the final process exit code.

**Input parameters**
- `command` *(string, required)*: Command to execute.
- `env` *(string, optional)*: Environment variables as `NAME=VALUE` pairs separated by LF line breaks.
- `dir` *(string, optional)*: Working directory relative to the project directory. Must remain inside the project. Default: current project directory.
- `tailResultSize` *(integer, optional)*: Maximum number of characters returned from the end of the output. Default: `1024`.
- `charsetName` *(string, optional)*: Character encoding used to read command output. Default: `UTF-8`.

### `terminate_process`
Terminates the current workflow by throwing a process termination exception.

**Description**
Use this tool when execution should stop immediately because of a fatal validation failure, an unsupported condition, or another controlled shutdown scenario.

**Features**
- Stops workflow execution immediately.
- Supports a custom message.
- Supports an optional cause message.
- Supports a custom exit code.

**Input parameters**
- `message` *(string, optional)*: Exception message to use. Default: `Process terminated by function tool.`
- `cause` *(string, optional)*: Optional cause message wrapped as the underlying exception cause.
- `exitCode` *(integer, optional)*: Exit code to return. Default: `1`.

## Web and API tools

### `get_web_content`
Fetches content from a web page by using an HTTP GET request.

**Description**
Use this tool to download content from an HTTP or HTTPS page, or to read a local `file:` URL when needed. It supports custom request headers, CSS selector extraction, and plain-text rendering for HTML responses.

**Features**
- Fetches content over HTTP or HTTPS with `GET`.
- Supports Basic authentication through URL user information.
- Can apply custom headers.
- Can extract matching HTML fragments with a CSS selector.
- Can convert HTML content to readable plain text.
- Includes the HTTP status line in the result for HTTP responses.

**Input parameters**
- `url` *(string, required)*: URL of the page to fetch. Supports user information for Basic authentication.
- `headers` *(string, optional)*: HTTP headers as `NAME=VALUE` pairs separated by LF line breaks.
- `timeout` *(integer, optional)*: Maximum time to wait for the response in milliseconds. Default internal timeout: `10000`.
- `charsetName` *(string, optional)*: Character encoding used to decode the response. Default: `UTF-8`.
- `textOnly` *(boolean, optional)*: If `true`, returns plain text instead of raw HTML.
- `selector` *(string, optional)*: CSS selector used to extract only matching elements from HTML content.

### `call_rest_api`
Executes a REST API call to a remote endpoint.

**Description**
Use this tool when a workflow needs to call an HTTP endpoint directly. It supports multiple HTTP methods, optional headers, and an optional request body for write-oriented operations such as `POST`, `PUT`, or `PATCH`.

**Features**
- Supports methods such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- Can send custom headers.
- Can send a request body for supported methods.
- Supports Basic authentication through URL user information.
- Returns the HTTP status line together with the response body.

**Input parameters**
- `url` *(string, required)*: URL of the REST endpoint. Supports user information for Basic authentication.
- `method` *(string, optional)*: HTTP method to use. Default: `GET`.
- `headers` *(string, optional)*: HTTP headers as `NAME=VALUE` pairs separated by LF line breaks.
- `body` *(string, optional)*: Request body to send for methods that support a payload.
- `timeout` *(integer, optional)*: Maximum time to wait for the response in milliseconds. Default internal timeout: `10000`.
- `charsetName` *(string, optional)*: Character encoding used to decode the response content. Default: `UTF-8`.
