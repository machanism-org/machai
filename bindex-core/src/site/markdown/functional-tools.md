---
<!-- @guidance: 
Create the `Function Tolls` page:
- **Download:**  
   Add a download link for the bindex.jar:  
   [![Download Bindex-Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex/bindex.jar/download)
   You can use it by classpass as a bindex related functinal tools for [MCP Machai Server](https://machai.machanism.org/mcp-machai-server/index.html).
- Analyze classes in the folder: `src/main/java/org/machanism/machai/bindex/ai/tools` and use this information to create the page content but do not mentionad this as a package details.
- If the function tool class is annotated with the `@SupportedFor` annotation, specify this in the description of the function tool methods.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/bindex-core/functional-tools.html
---

# Function Tools

Bindex-Core supplies AI-facing functions and supporting resources for discovering libraries, reading Bindex metadata, registering descriptors, validating descriptor structure, and generating Bindex files. They are suitable for agents and MCP-compatible integrations that need to search a catalog, inspect only the metadata they need, or publish metadata for later discovery.

## Download

[![Download Bindex-Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex/bindex.jar/download)

Add `bindex.jar` to the classpath to use these Bindex-related function tools with the [MCP Machai Server](https://machai.machanism.org/mcp-machai-server/index.html).

## Functions

### `get_bindex`

Retrieves a Bindex descriptor for a project or library. Supply a repository identifier when the descriptor is already registered, or provide an HTTP(S) URL or `file://` path when the descriptor should be read directly. An optional GraphQL-style selection query can reduce the returned payload to the requested fields.

**Use it when:** you need to inspect library metadata, load a remote or local descriptor, or limit a response to fields such as `name`, `version`, or `classification.languages`.

**Input parameters:**

| Parameter | Required | Description |
| --- | --- | --- |
| `id` | Yes | A Bindex identifier such as `groupId:artifactId:version`, an HTTP(S) URL to a `bindex.json` file, or a `file://` path. |
| `graphql_query` | No | A GraphQL-style selection query, such as `{ name classification { languages } }`, used to select fields in the response. |

### `pick_libraries`

Recommends libraries from a natural-language description of project needs or requirements. Results are selected through vector search and returned with the configured relevance criteria.

**Use it when:** you need dependency ideas for a feature, technology stack, or implementation goal and want recommendations ranked by semantic relevance.

**Input parameters:**

| Parameter | Required | Description |
| --- | --- | --- |
| `prompt` | Yes | The project need, desired functionality, technology stack, or feature for which libraries should be recommended. |
| `score` | No | The minimum relevance score; only results at or above this threshold are included. Default: `0.85`. |
| `search_limits` | No | The maximum number of recommendations returned by vector search. Default: `25`. |

### `register_bindex`

Reads a Bindex JSON descriptor from a project file or an HTTP(S) URL, normalizes its schema reference, and registers it in the Bindex repository. The function returns the identifier of the saved record.

**Use it when:** you want to add new metadata, update an existing descriptor, publish a project’s default `bindex.json`, or register a descriptor hosted remotely. Relative local paths are resolved from the active project directory.

**Input parameters:**

| Parameter | Required | Description |
| --- | --- | --- |
| `bindex_file_path` | No | A relative path within the project directory or an HTTP(S) URL. Defaults to `bindex.json`. |

### `register_bindex_json`

Registers a Bindex descriptor supplied directly as a structured JSON object. Before saving, the function applies the Bindex schema reference and returns the saved record identifier.

**Use it when:** an agent or integration already has the descriptor in memory and should publish it without first creating a local file or fetching a URL.

**Input parameters:**

| Parameter | Required | Description |
| --- | --- | --- |
| `bindex_json` | Yes | The structured Bindex JSON object to register. |

## Supporting Resources and Prompts

### `getBindexSchema` resource

Provides the Bindex v2 JSON Schema as UTF-8 `application/json` content. Consumers can use it to validate descriptor structure, properties, and metadata before registration.

**Input:** `uri` — the resource URI supplied by the tool framework; its path identifies the classpath schema resource. The resource is exposed at `file:///schema/bindex-schema-v2.json`.

### `generate_bindex` prompt

Loads the Markdown template containing the instructions and contextual prompts needed to generate a Bindex file. Use it as the starting context when an agent must create a descriptor that follows the Bindex format.

**Input:** none.

## Response and Usage Notes

- `get_bindex` returns a Bindex object; when `graphql_query` is supplied, the returned object contains the selected top-level fields that are present in the descriptor.
- `pick_libraries` returns a collection of recommended Bindex records.
- Both registration functions return the identifier assigned to the saved record.
- Registration normalizes the descriptor’s schema reference before saving it.
- Remote inputs must be accessible through HTTP(S); local file inputs are resolved relative to the active project directory where applicable.
