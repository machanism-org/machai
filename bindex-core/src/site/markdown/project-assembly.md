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

AI Assembly is a Machanism solution that helps you create an initial working application by combining:

- Your natural-language request (what you want to build)
- A curated catalog of libraries, each described by a `bindex.json` metadata file
- An AI assistant that recommends and integrates the most relevant libraries based on intent (semantic search), not just keyword matches

Unlike generic “generate code” workflows, AI Assembly is designed to start from structured library metadata so the generated project is practical, maintainable, and grounded in real, reusable components. You stay in control by reviewing the selected libraries and the generated code.

Reference: https://machanism.org/ai-assembly/index.html

## How it works (high level)

1. **Describe your goal**: Provide a short request describing the application or feature.
2. **Pick libraries**: The assistant searches the indexed `bindex.json` metadata and recommends libraries that match the intent of your request.
3. **Retrieve details**: For selected candidates, the assistant fetches detailed Bindex metadata (features, integration notes, examples).
4. **Assemble the project**: The assistant generates or updates project files (dependencies, configuration, code, docs) and wires the libraries together.
5. **Verify and iterate**: You review the output and refine requirements; the assistant can rebuild and fix issues until the project builds and behaves as expected.

# Act: Assembly

This project includes an Act named **Assembly** (defined in `src/main/resources/acts/assembly.toml`).

## What it is

The **Assembly** act guides the assistant to implement a user task by **selecting and integrating recommended libraries** using Bindex metadata.

It is intended for creating a working starting point: a project structure, dependencies, configuration, and initial code that uses established libraries whenever possible.

## Purpose

Use **Assembly** when you want to build something new (or add a major feature) and want the assistant to:

- Recommend relevant libraries from the curated ecosystem
- Use each library’s `bindex.json` details (including examples) to integrate it correctly
- Produce a functional, buildable project instead of isolated snippets

## When to use it

Use **Assembly** when you:

- Have a natural-language description of what you want (e.g., “REST API for …”, “CLI tool that …”, “integration with …”)
- Prefer using established libraries over custom code
- Want the assistant to generate and update multiple project files (build config, source code, documentation)
- Want an interactive workflow where the assistant can ask follow-up questions to clarify missing requirements

## What it does (from `assembly.toml`)

The act instructs the assistant to:

- Read the Bindex schema (`get_bindex_schema`) to understand the metadata format
- Request library recommendations from the catalog (`pick_libraries`) using the user’s query
- Fetch detailed metadata for chosen libraries (`get_bindex`)
- Implement the requested functionality using those libraries when possible
- Create/update all necessary files in the current project folder
- Clean and build the project and fix any errors introduced during changes
- Update `README.md` with a detailed description of what was created
- Ask for missing information when needed (the act is interactive: `gw.interactive = true`)

## Notes

- The act includes a recommendation threshold (`pick.score = 0.86`) to keep suggestions focused on strong matches.
- It is designed to work with function tools for filesystem and command-line actions (use `cmd /c` on Windows).
