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

Bindex-Core provides a set of AI-facing function tools for library discovery, Bindex metadata lookup, and Bindex record registration. These tools are intended for use by agents and MCP-compatible integrations that need to find relevant libraries, inspect Bindex descriptors, or publish Bindex metadata into a repository.

## Download

[![Download Bindex-Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex/bindex.jar/download)

You can add `bindex.jar` to the classpath and use it as a Bindex-related function tool provider for the [MCP Machai Server](https://machai.machanism.org/mcp-machai-server/index.html).

## Available Tools

### `get_bindex`

Retrieves Bindex metadata for a project or library. Use this tool when you already know a Bindex identifier or have a direct URL to a `bindex.json` descriptor and want to inspect the library metadata.

The tool can return the complete Bindex descriptor or a reduced response filtered by a GraphQL-style selection query. Filtering is useful when an agent only needs specific fields and should minimize response size.

**Typical use cases:**

- Look up a library by its Bindex coordinates, such as `groupId:artifactId:version`.
- Load a remote Bindex descriptor from an HTTP or HTTPS URL.
- Retrieve only selected descriptor fields, such as name, version, classification, or supported languages.

**Input parameters:**

| Parameter | Required | Description |
| --- | --- | --- |
| `id` | Yes | The unique Bindex ID, for example `groupId:artifactId:version`, or a direct HTTP/HTTPS URL pointing to a remote `bindex.json` file. |
| `graphql_query` | No | A GraphQL-style selection query used to filter the returned JSON structure. Example: `{ name classification { languages } }`. |

### `pick_libraries`

Recommends libraries based on a natural-language prompt that describes project needs or requirements. Use this tool when an agent needs help identifying relevant libraries for a feature, technology stack, or implementation goal.

The tool performs recommendation against Bindex metadata and returns matching library records that meet the configured relevance criteria.

**Typical use cases:**

- Find libraries for a specific feature, such as JSON processing, REST APIs, database access, testing, or AI integration.
- Recommend dependencies for a new project based on a short requirements description.
- Limit results by relevance score or maximum search count.

**Input parameters:**

| Parameter | Required | Description |
| --- | --- | --- |
| `prompt` | Yes | A natural-language description of the project need, desired functionality, technology stack, or feature to implement. |
| `score` | No | The minimum relevance score threshold. Only libraries with a score equal to or higher than this value are included. If omitted, the configured default is used. |
| `search_limits` | No | The maximum number of recommendations to retrieve from vector search. Default: `25`. |

### `register_bindex`

Registers a Bindex JSON descriptor from either a project file or a remote URL. Use this tool to add new Bindex metadata or update existing metadata so that libraries can be discovered and recommended by Bindex-aware agents.

When registration succeeds, the tool returns the unique record ID assigned to the registered Bindex entry.

**Typical use cases:**

- Register the default `bindex.json` file from the current project.
- Register a descriptor stored at a relative path inside the project directory.
- Register a descriptor hosted at an HTTP or HTTPS URL.
- Publish updated metadata after changing a library descriptor.

**Input parameters:**

| Parameter | Required | Description |
| --- | --- | --- |
| `bindex_file_path` | No | The relative path of the Bindex file to register, or an HTTP/HTTPS URL. The default value is `bindex.json`. Local paths must resolve within the project directory. |

### `register_bindex_json`

Registers a Bindex descriptor supplied directly as a JSON object. Use this tool when an agent already has a structured Bindex payload and does not need to read it from a file or URL.

When registration succeeds, the tool returns a result object containing the assigned `RecordId`.

**Typical use cases:**

- Register generated Bindex metadata directly from an agent workflow.
- Save a descriptor assembled in memory by another tool or integration.
- Update repository metadata without first writing a `bindex.json` file.

**Input parameters:**

| Parameter | Required | Description |
| --- | --- | --- |
| `bindex_json` | Yes | The Bindex JSON object to register. |

## Response and Filtering Notes

- Registration tools normalize the descriptor schema reference before saving the Bindex record.
- `get_bindex` can reduce returned metadata with `graphql_query`, helping agents avoid unnecessary token usage.
- Remote descriptor inputs must be accessible through HTTP or HTTPS URLs.
- Local descriptor registration expects paths to remain inside the active project directory.
