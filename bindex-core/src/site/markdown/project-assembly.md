---
<!-- @guidance: 
- Generate or update the content as follows.  
- If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
- Analyze additional information from page: `https://machanism.org/ai-assembly/index.html` (selector:`.md-content`) and use it to create a content the current page.
- Add `https://machanism.org/ai-assembly/index.html` link as a reference to additional information.
# Page content
- Analyze the `src/main/resources/acts/assembly.toml` file.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- A clear, concise description of the act's purpose and when it should be used.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/bindex-core/project-assembly.html
---

# AI Assembly

AI Assembly is a Machanism feature that helps create the first practical version of an application from a plain-language request. Instead of only producing isolated code snippets, it works with the Machanism library ecosystem and structured library metadata so the generated result is closer to a real project that can be built, reviewed, and extended.

In simple terms, you describe what you want to build, AI Assembly finds libraries that fit the request, reads their metadata, and generates the initial project structure, configuration, and code needed to get started.

Reference: https://machanism.org/ai-assembly/index.html

## How it works

AI Assembly is built around `bindex.json`, a structured description file generated for libraries in the Machanism ecosystem. These files can include information such as:

- what a library does
- how it can be integrated
- example usage
- build and dependency information
- authorship, licensing, and related metadata

This metadata is indexed for semantic search. That means the system can search by meaning and intent, not only by exact words. When you submit a request, AI Assembly uses that indexed information to identify libraries that best match the goal.

The overall flow is straightforward:

1. **You describe the application or feature**
   - The request is written in natural language and can include the purpose, important features, and any known technical preferences.
2. **Relevant libraries are picked**
   - The system searches the indexed `bindex.json` data and recommends libraries that appear to fit the request.
3. **Detailed library metadata is analyzed**
   - The selected library descriptions are retrieved so the assistant can understand integration details, examples, and supported capabilities.
4. **The project is assembled**
   - The assistant generates the initial project structure, build configuration, source files, and integration points.
5. **The developer reviews the result**
   - The generated project is a practical starting point, but the developer is still responsible for validating functionality, quality, and security.

## Why use AI Assembly

AI Assembly is useful when you want more than a small example and need a realistic starting project. It helps combine:

- a natural-language project request
- recommended libraries from the Machanism ecosystem
- structured metadata from `bindex.json`
- AI-assisted generation of files and initial implementation

This makes it helpful for quickly producing a project foundation that can then be refined by the developer.

# Act: Assembly

This project includes an Act named **Assembly**, defined in `src/main/resources/acts/assembly.toml`.

## Purpose

The **Assembly** act is designed to implement a user request by finding suitable libraries and using them to build the requested application or feature.

Its main goal is to help generate a functional project foundation quickly. Instead of starting from nothing, it encourages the assistant to reuse appropriate public libraries described in Bindex metadata and assemble the required project around them.

## When to use it

Use the **Assembly** act when you:

- want to create a new application or a significant new feature
- have a request written in natural language
- want help identifying suitable libraries for the task
- want the assistant to generate or update multiple project files
- need a practical, buildable starting point rather than a simple code sample

## What the act does

According to `assembly.toml`, the **Assembly** act guides the assistant to:

- implement the user task comprehensively and correctly
- use `get_bindex_schema` to understand the Bindex JSON structure
- use `pick_libraries` with the user's request to find recommended libraries
- analyze the recommended libraries and decide which ones best fit the request
- use `get_bindex` to retrieve detailed metadata for suitable libraries
- rely on those libraries when possible instead of writing everything from scratch
- create all necessary files in the project folder
- add the required code and publicly available dependencies
- clean and build the project, then fix errors after making changes
- ask the user for missing information when needed
- create a detailed description in `README.md`

## Important behavior

The `assembly.toml` configuration also shows several practical details:

- the act is **interactive** (`gw.interactive = true`), which means it can ask follow-up questions when important information is missing
- it uses a recommendation score threshold (`pick.score = 0.86`) so the library suggestions stay focused on stronger matches
- it is intended to work with both file-system and command-line tools during implementation
- it explicitly reminds the assistant to use `cmd /c` on Windows and `sh -c` on Unix-like systems for shell execution

## In simple words

If you know what you want to build but are not sure which libraries to use, **Assembly** is the act for that situation.

It helps turn requests such as:

- “Create a REST API for user login”
- “Build a command-line tool for processing files”
- “Generate an application that integrates with a specific platform”

into an initial project with recommended libraries, configuration, source code, and documentation.

## Summary

The **Assembly** act is intended for project creation and structured implementation. It helps move from a high-level request to a practical starting application by:

- finding relevant libraries
- reading their metadata and examples
- generating the required project files
- producing an initial implementation that the developer can review and continue building
