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

Ghostwriter provides functional tools that help a model discover reusable Act templates and safely work with files inside the current project working directory. These tools support prompt-driven workflows, project exploration, and controlled file operations.

## Act tools

### `build_in_list_acts`
Returns the built-in Act templates packaged with Ghostwriter.

**What it does**
- Scans the built-in `acts` resources available from the application package.
- Finds Act definitions stored as `.toml` files.
- Loads each Act description and formats the results as a readable list.

**Feature highlights**
- Quick discovery of reusable workflow templates.
- Useful for browsing available Acts before choosing one.
- Returns a simple human-readable list instead of a large structured payload.

**Input parameters**
This tool does not require any input parameters.

**Result**
- A formatted list of built-in Act names and descriptions.

### `load_act_details`
Loads the details of a specific Act template by name.

**What it does**
- Retrieves an Act definition using the provided Act name.
- Can load the effective Act, only a custom user-defined Act, or only the built-in Act.
- Returns the resolved Act properties, such as instructions, description, and template-related values.

**Feature highlights**
- Supports inspecting a single Act in detail.
- Helps compare custom Acts with built-in Acts.
- Useful for troubleshooting Act resolution and configuration.

**Input parameters**
- `actName` *(string, required)*: Name of the Act to load.
- `custom` *(boolean, optional)*:
  - `true`: load only the custom Act from the configured Acts directory.
  - `false`: load only the built-in packaged Act.
  - omitted: load the effective Act using normal Ghostwriter Act resolution.

**Result**
- A structured object containing the loaded Act properties.
- If the Act cannot be resolved, the tool returns an error message.

## File system tools

### `read_file_from_file_system`
Reads a text file from the file system.

**What it does**
- Opens a file relative to the current working directory.
- Reads the entire file as text.
- Uses the requested character encoding, or `UTF-8` by default.

**Feature highlights**
- Good for reviewing source files, configuration, templates, and documentation.
- Returns the exact current file content.
- Keeps file access scoped to the host-controlled working directory.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to read.
- `charsetName` *(string, optional)*: Character encoding to use. Default: `UTF-8`.

**Result**
- The full file content as text.
- If the file does not exist, the tool returns `File not found.`

### `write_file_to_file_system`
Writes text content to a file on disk.

**What it does**
- Writes the provided text to a file relative to the current working directory.
- Updates an existing file or creates a new one if it does not exist.
- Creates missing parent directories before writing a new file.
- Uses the requested character encoding, or `UTF-8` by default.

**Feature highlights**
- Supports both file creation and full-content replacement.
- Helpful for generating or updating source code, documentation, and configuration files.
- Returns a clear success message indicating whether a file was written or updated.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to create or update.
- `text` *(string, required)*: Full text content to write.
- `charsetName` *(string, optional)*: Character encoding to use. Default: `UTF-8`.

**Result**
- `File written successfully: ...` when a new file is created.
- `File updated successfully: ...` when an existing file is overwritten.
- An error message if writing fails.

### `list_files_in_directory`
Lists files and directories directly inside a specific folder.

**What it does**
- Reads the immediate contents of a directory.
- Returns project-relative paths.
- Does not recurse into nested directories.
- Uses the current working directory when no path is provided.

**Feature highlights**
- Good for a quick overview of a folder.
- Useful before opening or editing files.
- Returns a compact comma-separated result.

**Input parameters**
- `dir_path` *(string, optional)*: Path to the directory to inspect. If omitted or blank, the current working directory is used.

**Result**
- A comma-separated list of project-relative files and directories.
- If the directory is missing, invalid, or empty, the tool returns `No files found in directory.`

### `get_recursive_file_list`
Recursively lists files under a directory.

**What it does**
- Traverses the selected directory and all nested subdirectories.
- Returns files only, not directories.
- Produces project-relative paths with forward slashes.
- Skips excluded directories defined by the project layout.
- Uses the current working directory when no path is provided.

**Feature highlights**
- Useful for collecting a full file inventory under a source, test, or documentation folder.
- Helps locate candidate files before analysis or updates.
- Filters out excluded project directories during recursion.

**Input parameters**
- `dir_path` *(string, optional)*: Root directory to scan recursively. If omitted or blank, the current working directory is used.

**Result**
- A list of project-relative file paths found under the selected directory.
- If no files are found, the tool returns `No files found in directory.`
