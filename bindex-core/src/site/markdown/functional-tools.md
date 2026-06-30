---
<!-- @guidance: 
Create the `Function Tolls` page:
- **Download:**  
   Add a download link for the bindex.jar:  
   [![Download Bindex-Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex/bindex.jar/download)
   You can use it by classpass as a bindex related functinal tools for [MCP Machai Server](https://machai.machanism.org/mcp-machai-server/index.html).
- Analyze classes in the folder: `src/main/java/org/machanism/machai/bindex/ai/tools` and use this information to create the page content but don not mentionad this as a package details.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/bindex-core/functional-tools.html
---

# Function Tools

## Overview

Bindex function tools provide AI-callable operations for library discovery, Bindex metadata retrieval, response filtering, and Bindex descriptor registration. They are designed for AI-assisted development workflows where an agent needs to find suitable libraries, inspect metadata for a known component, or publish Bindex information for later discovery.

These tools are useful when you need to:

- Recommend libraries from a natural-language project requirement.
- Retrieve Bindex metadata by a known identifier or from a remote descriptor URL.
- Limit large metadata responses to only the fields needed by the caller.
- Register Bindex descriptors from a local project file, a remote URL, or a JSON object.

## Download

[![Download Bindex-Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex/bindex.jar/download)

You can use this JAR via classpath as Bindex-related functional tools for [MCP Machai Server](https://machai.machanism.org/mcp-machai-server/index.html).

## Available Function Tools

### `get_bindex`

Retrieves Bindex metadata for a project or library.

Use this tool when you already know the Bindex identifier for a library, or when the metadata is available as a remote `bindex.json` file. The tool can return the complete descriptor or a smaller response filtered with a GraphQL-style selection query.

**Features:**

- Loads Bindex metadata from the configured Bindex repository by id.
- Supports direct loading from `http://` and `https://` descriptor URLs.
- Returns complete Bindex metadata when no filter is supplied.
- Supports optional GraphQL-style field selection to reduce response size.
- Reports an error if the requested Bindex descriptor cannot be found or read.

**Input parameters:**

- `id` *(string, required)*: The unique Bindex id, such as `groupId:artifactId:version`, or a direct HTTP/HTTPS URL pointing to a remote `bindex.json` file.
- `graphql_query` *(string, optional)*: A GraphQL-style selection query used to include only specific fields in the returned metadata. Example: `{ name classification { languages } }`.

**Typical use cases:**

- Inspect metadata for a known library or project component.
- Fetch a Bindex descriptor from a remote location without registering it first.
- Reduce the amount of metadata passed to an AI model or downstream tool.

### `pick_libraries`

Recommends libraries based on a natural-language prompt describing project needs.

Use this tool when you know what capability you need but do not yet know which library or Bindex id is appropriate. The tool searches the configured Bindex repository and returns matching library recommendations.

**Features:**

- Accepts plain-language requirements, goals, or feature descriptions.
- Searches for libraries that match the requested functionality.
- Supports a minimum relevance score threshold.
- Supports a configurable maximum number of vector-search results.
- Returns recommended Bindex entries that an agent can inspect or select from.

**Input parameters:**

- `prompt` *(string, required)*: A natural-language description of the required functionality, technology stack, project needs, or feature to implement.
- `score` *(number, optional)*: The minimum relevance score for returned recommendations. If omitted, the configured default score is used.
- `search_limits` *(integer, optional; default: `25`)*: The maximum number of recommendations to retrieve from vector search.

**Typical use cases:**

- Find candidate dependencies for a new feature.
- Compare relevant libraries before adding a dependency.
- Help an AI agent choose libraries that match stated requirements.

### `register_bindex`

Registers a Bindex descriptor from a project file or remote URL.

Use this tool when a Bindex JSON descriptor already exists on disk in the current project or is hosted at an HTTP/HTTPS location. During registration, the descriptor is saved through the configured Bindex integration and the generated record id is returned.

**Features:**

- Reads a Bindex JSON descriptor from a relative path in the project directory.
- Supports registration from a direct `http://` or `https://` URL.
- Uses `bindex.json` as the default file name when no path is provided.
- Ensures local file paths resolve within the project directory.
- Applies the official Bindex schema URL before saving the descriptor.
- Returns the unique record id assigned to the registered entry.

**Input parameters:**

- `bindex_file_path` *(string, optional; default: `bindex.json`)*: The relative path of the Bindex file to register from the project directory, or a direct HTTP/HTTPS URL to a Bindex JSON descriptor.

**Typical use cases:**

- Register a project's `bindex.json` descriptor.
- Add or update metadata hosted at a remote URL.
- Make Bindex metadata available for future retrieval and recommendation.

### `register_bindex_json`

Registers a Bindex descriptor directly from a JSON object.

Use this tool when an AI workflow has already created or received a structured Bindex descriptor and should save it without first writing it to a file. The tool stores the descriptor and returns the generated record id in a result object.

**Features:**

- Accepts a complete Bindex JSON object as input.
- Does not require a local file or remote URL.
- Applies the official Bindex schema URL before saving the descriptor.
- Saves the descriptor through the configured Bindex integration.
- Returns a result containing the generated `RecordId`.

**Input parameters:**

- `bindex_json` *(object, required)*: The Bindex descriptor to register, provided as a JSON object.

**Typical use cases:**

- Register generated Bindex metadata directly from an AI workflow.
- Save an in-memory descriptor without creating a temporary file.
- Automate Bindex registration after metadata extraction or transformation.

## Response Filtering

The `get_bindex` tool can reduce returned metadata with a GraphQL-style field selection query. This is helpful when the full Bindex descriptor is larger than needed.

Example query:

```graphql
{ name classification { languages } }
```

When a filter is provided, only matching fields that exist in the Bindex metadata are included in the response.
