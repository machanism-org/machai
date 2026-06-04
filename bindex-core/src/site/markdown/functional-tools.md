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

The `BindexFunctionTools` package provides AI-callable tools for interacting with Bindex data and libraries. These tools are designed to streamline project setup, 
library discovery, and Bindex record management in AI-assisted workflows.

Each tool is described below with its features, input parameters, and recommended use cases.

## Download

[![Download Bindex-Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex/releases)

You can use this JAR via classpath as a set of Bindex-related functional tools for [MCP Machai Server](https://machai.machanism.org/mcp-machai-server/index.html).

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
- Returns empty result when no matching record exists.

**Input parameters:**
- `id` *(string, required)*: The Bindex identifier to retrieve.

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
- Includes the library id and description for each recommendation.

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
- Returns `RecordId: <id>` on success.
- Returns `file not found` when the specified file does not exist.
- Returns `Error: <message>` when file reading or registration fails.

**Input parameters:**
- `fileName` *(string, required)*: The name of the Bindex file to register. The file must exist in the current working directory.
