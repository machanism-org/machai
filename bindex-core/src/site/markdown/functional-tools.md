---
<!-- @guidance: 
Create the `Function Tolls` page:
- **Download:**  
   Add a download link for the bindex.jar:  
   [![Download Bindex-Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex/bindex.jar/download)
   You can use it by classpass as a bindex related functinal tools for [MCP Machai Server](https://machai.machanism.org/mcp-machai-server/index.html).
- Analyze classes in the folder: `src/main/java/org/machanism/machai/bindex/ai/tools`.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/bindex-core/functional-tools.html
---

# Function Tools

## Overview

The Bindex function tools provide AI-callable operations for discovering libraries, retrieving Bindex metadata, filtering metadata responses, and registering Bindex descriptors. They are implemented in `src/main/java/org/machanism/machai/bindex/ai/tools` and are intended for use in AI-assisted workflows such as [MCP Machai Server](https://machai.machanism.org/mcp-machai-server/index.html).

These tools help an AI agent answer common dependency-management questions:

- Which libraries match a project requirement?
- What metadata is available for a known Bindex identifier?
- How can a Bindex descriptor be registered from a file, URL, or JSON object?
- How can a large Bindex response be reduced to only the fields needed by the caller?

## Download

[![Download Bindex-Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex/bindex.jar/download)

You can use this JAR via classpath as a set of Bindex-related functional tools for [MCP Machai Server](https://machai.machanism.org/mcp-machai-server/index.html).

## Package Components

### `BindexFunctionTools`

`BindexFunctionTools` is the main AI-facing tool set. It implements the MachAI `FunctionTools` interface and exposes annotated tool methods for Bindex repository access, library recommendation, and descriptor registration.

### `GraphqlJsonFilter`

`GraphqlJsonFilter` is an internal helper used by `get_bindex`. It applies a GraphQL-style selection query to a Bindex object so callers can request only selected top-level fields and reduce response size.

## Available Tools

### `get_bindex`

Retrieves Bindex metadata for a project or library.

Use this tool when you already know the Bindex identifier for a library, or when you have a direct HTTP or HTTPS URL to a remote `bindex.json` descriptor. The tool can return the complete Bindex document or a smaller response filtered by a GraphQL-style field selection query.

**Features:**

- Loads Bindex metadata from the configured Bindex repository by id.
- Supports direct remote descriptor loading from `http://` and `https://` URLs.
- Returns a `Bindex` object containing the matching metadata.
- Supports optional GraphQL-style response filtering through `graphql_query`.
- Throws an error when the requested Bindex descriptor cannot be found.

**Input parameters:**

- `id` *(string, required)*: The unique Bindex id, such as `groupId:artifactId:version`, or a direct HTTP/HTTPS URL pointing to a remote `bindex.json` file.
- `graphql_query` *(string, optional)*: A GraphQL-style selection query used to return only specific fields. Example: `{ name classification { languages } }`.

**Typical use cases:**

- Inspect metadata for a known library.
- Fetch a remote Bindex descriptor without registering it first.
- Reduce a large Bindex response before passing it to an AI model.

### `pick_libraries`

Recommends libraries based on a natural-language prompt describing project requirements.

Use this tool when you need dependency suggestions but do not yet know which library or Bindex id to use. The tool uses the configured Bindex repository and picker model to search for relevant libraries and returns matching `BindexInfo` recommendations.

**Features:**

- Accepts a plain-language project requirement or technical goal.
- Searches for libraries that match the requested capability.
- Supports a relevance score threshold.
- Supports a configurable maximum number of vector-search results.
- Returns a collection of recommended Bindex entries.

**Input parameters:**

- `prompt` *(string, required)*: A natural-language description of the project needs, required functionality, technology stack, or feature to implement.
- `score` *(number, optional)*: The minimum relevance score for returned recommendations. If omitted, the configured default is used.
- `search_limits` *(integer, optional; default: `25`)*: The maximum number of recommendations to retrieve from vector search.

**Typical use cases:**

- Find candidate libraries for a new feature.
- Compare available libraries before adding dependencies.
- Help an AI agent choose dependencies that match a stated requirement.

### `register_bindex`

Registers a Bindex descriptor from a local project file or from a remote URL.

Use this tool when a Bindex JSON descriptor already exists as a file or is available at an HTTP/HTTPS location. Before saving, the tool sets the descriptor schema to the official Bindex schema URL and then stores the descriptor through the configured picker and repository integration.

**Features:**

- Reads a Bindex JSON descriptor from the project directory.
- Supports registration from a direct `http://` or `https://` URL.
- Uses `bindex.json` as the default file path.
- Prevents absolute-path registration outside the project directory.
- Saves the descriptor and returns the assigned record id.
- Throws an error if the project directory is unavailable for local-file registration.

**Input parameters:**

- `bindex_file_path` *(string, optional; default: `bindex.json`)*: The relative path of the Bindex file to register from the project directory, or a direct HTTP/HTTPS URL to a Bindex JSON descriptor.

**Typical use cases:**

- Register a project's `bindex.json` file.
- Register metadata hosted at a remote URL.
- Add or update Bindex metadata so it can be discovered by recommendation tools.

### `register_bindex_json`

Registers a Bindex descriptor directly from a JSON object.

Use this tool when an AI workflow has already built or received a structured Bindex object and should register it without first writing it to disk. The tool sets the official Bindex schema URL, saves the descriptor, and returns the generated record id in a result map.

**Features:**

- Accepts a complete Bindex JSON object as input.
- Does not require a local file or remote URL.
- Saves the descriptor through the configured picker and repository integration.
- Returns a map containing the generated `RecordId`.

**Input parameters:**

- `bindex_json` *(object, required)*: The Bindex descriptor to register, provided as a JSON object.

**Typical use cases:**

- Register generated Bindex metadata directly from an AI workflow.
- Save an in-memory descriptor without creating a temporary file.
- Automate Bindex registration after metadata extraction or transformation.
