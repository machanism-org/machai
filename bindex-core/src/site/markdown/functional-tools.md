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

`BindexFunctionTools` defines the AI-callable tools available for working with Bindex data. These tools help retrieve stored Bindex records, inspect the Bindex schema, discover relevant libraries from a natural-language request, and register a Bindex document from a local file.

## Available Tools

### `get_bindex`
Retrieves Bindex metadata for a specific project or library by its Bindex id.

**Use this tool when:**
- You already know the Bindex id you want to inspect.
- You need to load a previously registered Bindex document.
- You want to fetch metadata for additional analysis or downstream processing.

**Features:**
- Accepts a Bindex id as input.
- Queries the configured Bindex repository.
- Returns the matched Bindex document as serialized JSON.
- Returns `<not found>` when no matching record exists.

**Input parameters:**
- `id` *(string, required)*: The Bindex identifier to retrieve.

### `get_bindex_schema`
Returns the JSON schema that defines the structure of a valid Bindex document.

**Use this tool when:**
- You need to understand the expected Bindex document format.
- You want to validate generated or edited Bindex JSON.
- You are preparing to create a new Bindex document and need the official schema.

**Features:**
- Loads the schema from the application resources.
- Does not require any input parameters.
- Returns the raw schema content as JSON text.

**Input parameters:**
- None.

### `pick_libraries`
Recommends libraries that match a natural-language description of project requirements.

**Use this tool when:**
- You need dependency suggestions for a feature, framework, or integration.
- You want to convert a plain-language requirement into a shortlist of candidate libraries.
- You are exploring suitable libraries before selecting dependencies.

**Features:**
- Accepts a natural-language prompt describing the required capabilities.
- Uses the configured picker model and score threshold to search for relevant libraries.
- Returns a simplified list of matches.
- Includes only the library id and description for each recommendation.

**Input parameters:**
- `prompt` *(string, required)*: A natural-language description of the project goals, required functionality, or technical needs.

### `register_bindex`
Registers a Bindex record from a JSON file located in the current working directory.

**Use this tool when:**
- You have prepared a Bindex JSON file locally and want to register it.
- You need to add a new Bindex record during an AI-assisted workflow.
- You want to convert a valid local Bindex document into a stored repository entry.

**Features:**
- Accepts the name of a file in the current working directory.
- Reads and parses the file as a Bindex document.
- Creates a new record using the configured picker integration.
- Returns the created record id on success.
- Returns clear error messages when the file is missing or processing fails.

**Input parameters:**
- `fileName` *(string, required)*: The name of the Bindex file to register. The file must exist in the current working directory.
