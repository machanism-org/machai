---
<!-- @guidance: 
Create the `Function Tolls` page:
- Analyze classes in the folder: `src/main/java/org/machanism/machai/bindex/ai/tools`.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/bindex-core/functional-tools.html
---

# Function Tools

`BindexFunctionTools` provides a set of AI-accessible function tools for working with Bindex metadata. These tools help retrieve registered Bindex documents, inspect the Bindex schema, recommend libraries based on project needs, and register new Bindex records from files in the current working directory.

## Available Tools

### `get_bindex`
Retrieves Bindex metadata for a specific project or library by its identifier.

**Use this tool when:**
- You already know the Bindex id.
- You want the full stored metadata for a library or project.
- You need a JSON representation of a registered Bindex record.

**Feature:**
- Looks up a Bindex entry in the repository.
- Returns the serialized Bindex document as JSON.
- Returns `<not found>` when no matching record exists.

**Input parameters:**
- `id` *(string, required)*: The Bindex identifier to retrieve.

**Returns:**
- A JSON string containing the Bindex metadata, or `<not found>` if the id is not registered.

### `get_bindex_schema`
Returns the JSON schema that defines the structure of a Bindex document.

**Use this tool when:**
- You need to understand the expected Bindex format.
- You want to validate or generate Bindex documents.
- You need the schema before creating or editing metadata.

**Feature:**
- Loads the schema from the application resources.
- Returns the schema content as JSON text.
- Does not require any input parameters.

**Input parameters:**
- None.

**Returns:**
- A JSON string containing the Bindex schema.

### `pick_libraries`
Recommends libraries that match a user request or project requirement.

**Use this tool when:**
- You need library suggestions for a feature or technical requirement.
- You want to narrow down possible dependencies from a natural-language prompt.
- You are exploring suitable libraries for a new project or enhancement.

**Feature:**
- Accepts a prompt that describes the user’s needs.
- Uses the configured picker and model settings to find matching libraries.
- Returns a simplified list of recommended library ids and descriptions.

**Input parameters:**
- `prompt` *(string, required)*: A natural-language description of the project needs, goals, or technical requirements.

**Returns:**
- A list of recommended libraries, where each item contains:
  - `id`: The Bindex id.
  - `description`: A short description of the library.

### `register_bindex`
Registers a Bindex record from a file located in the current working directory.

**Use this tool when:**
- You have created a Bindex JSON file and want to add it to the repository.
- You want to register metadata from a local file during an AI-assisted workflow.
- You need to turn a valid Bindex document into a stored record.

**Feature:**
- Reads the specified file from the working directory.
- Parses the file as a Bindex document.
- Creates a new repository record and returns its record id.
- Reports an error if the file is missing or cannot be processed.

**Input parameters:**
- `fileName` *(string, required)*: The name of the Bindex file to register. The file must exist in the current working directory.

**Returns:**
- `RecordId: <value>` when registration succeeds.
- `file not found` if the file does not exist.
- `Error: <message>` if processing fails.
