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

Ghostwriter provides a small set of functional tools that let the model inspect reusable act templates and interact with files inside the current working directory. These tools are designed to support prompt-driven workflows, project inspection, and controlled file operations.

## Act tools

### `build_in_list_acts`
Lists the built-in Act templates available to Ghostwriter.

**What it does**
- Scans the packaged built-in Act definitions stored as TOML files.
- Returns the Act name together with its description.
- Helps users quickly discover which reusable prompt templates are available.

**When to use it**
- When you want to browse the available Acts before selecting one.
- When building a workflow that depends on predefined prompt templates.

**Input parameters**
This tool does not require any input parameters.

**Result**
- A list of built-in Act names with short descriptions.

### `load_act_details`
Loads the details of a specific Act template.

**What it does**
- Retrieves the configuration of an Act by name.
- Can load the effective Act, a custom user-defined Act, or only the built-in version.
- Returns the Act properties, such as instructions, description, and input template values when available.

**When to use it**
- When you need to inspect the full definition of an Act.
- When comparing custom Acts with built-in Acts.
- When editing or debugging Act configuration.

**Input parameters**
- `actName` *(string, required)*: The name of the Act to load.
- `custom` *(boolean, optional)*:
  - `true`: load only the user-defined custom Act.
  - `false`: load only the built-in Act.
  - not provided: load the effective Act using the configured Act resolution.

**Result**
- A structured object containing the Act details.
- If the Act cannot be loaded, the tool returns an error message.

## File system tools

### `read_file_from_file_system`
Reads the contents of a text file from disk.

**What it does**
- Opens a file relative to the current working directory.
- Reads the file using the requested character set.
- Returns the full file content as text.

**When to use it**
- When reviewing source code, configuration files, documentation, or templates.
- When another task needs the exact current contents of a file.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to read.
- `charsetName` *(string, optional)*: Character encoding to use. Default: `UTF-8`.

**Result**
- The text content of the file.
- If the file does not exist, the tool returns `File not found.`

### `write_file_to_file_system`
Writes text content to a file on disk.

**What it does**
- Creates a new file if it does not exist.
- Overwrites the file content if the file already exists.
- Creates missing parent directories when needed.
- Writes using the requested character set.

**When to use it**
- When generating new files.
- When updating documentation, source code, or configuration files.
- When applying model-generated changes back into the project.

**Input parameters**
- `file_path` *(string, required)*: Path to the file to create or update.
- `text` *(string, required)*: Full text content to write.
- `charsetName` *(string, optional)*: Character encoding to use. Default: `UTF-8`.

**Result**
- A success message indicating whether the file was written or updated.
- If writing fails, the tool returns an error message.

### `list_files_in_directory`
Lists files and directories directly inside a folder.

**What it does**
- Reads the immediate contents of a directory.
- Returns project-relative paths.
- Does not recurse into subdirectories.

**When to use it**
- When exploring the top-level contents of a folder.
- When you need a quick overview before reading or editing files.

**Input parameters**
- `dir_path` *(string, optional)*: Directory path to inspect. If omitted or blank, the current working directory is used.

**Result**
- A comma-separated list of files and directories.
- If the directory is missing or empty, the tool returns `No files found in directory.`

### `get_recursive_file_list`
Recursively lists files under a directory.

**What it does**
- Traverses a directory and all nested subdirectories.
- Returns files only, not directories.
- Produces project-relative paths using forward slashes.
- Skips excluded directories defined by the project layout.

**When to use it**
- When searching a project area for candidate files.
- When collecting all files under a source, resource, or documentation folder.
- When you need a broader project inventory before making changes.

**Input parameters**
- `dir_path` *(string, optional)*: Root directory to scan recursively. If omitted or blank, the current working directory is used.

**Result**
- A list of file paths found under the selected directory.
- If no files are found, the tool returns `No files found in directory.`
