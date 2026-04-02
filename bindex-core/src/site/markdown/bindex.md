---
<!-- @guidance: 
# Instructions
- Generate or update the content as follows.  
- If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
- Analyze additional information from page: `https://machanism.org/bindex/index.html` (selector:`.md-content`) and use it to create a content the current page.
- Add `https://machanism.org/bindex/index.html` link as a reference to additional information.
# Page content
- Analyze the `src/main/resources/acts/bindex.toml` file.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- A clear, concise description of the act's purpose and when it should be used.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/bindex-core/bindex.html
---

# Bindex

Bindex (short for “Brick Index”) is a structured descriptor file named `bindex.json` used by the Machanism platform to describe libraries as modular “bricks” that can be discovered, recommended, and assembled into applications.

A `bindex.json` answers three practical questions:

1. **What is this library?**
   - Name, version, and description
   - Main capabilities (**features**) with examples
   - **Classification** (type, domain, supported languages), which is critical for semantic search and recommendations
2. **Where is it located?**
   - Repository type and URL
   - Coordinates (for example Maven group/artifact/version)
   - License information
3. **How can it be used?**
   - Constructors and configuration/instantiation guidance
   - Customizations and extension points
   - Studs (interfaces/abstract types intended for implementation/extension)
   - Additional examples

The file is typically generated automatically from project metadata and source code, then reviewed and refined to ensure it accurately represents the library before registration.

Reference: https://machanism.org/bindex/index.html

## Act: bindex

### What it is
The **bindex** act creates, updates, and/or registers a project’s `bindex.json` so the project can be treated as a reusable library within the Machanism platform.

### Purpose
Use this act to produce a `bindex.json` that conforms to the official Bindex schema, capturing the library’s metadata, location, and usage/integration points.

### When to use
- When a project does not yet have a `bindex.json` and you want to generate one.
- After changes to code, features, metadata, dependencies, or documentation that should be reflected in `bindex.json`.
- When you are ready to register an existing `bindex.json` so it becomes searchable and retrievable.

### What it does
- Loads the official Bindex JSON schema (via the `get_bindex_schema` tool) and uses it as the contract.
- Scans the project (source/resources and documentation) to extract:
  - identification and metadata (name, version, description, authors, license)
  - classification (type/domain/languages)
  - location and coordinates (repository information)
  - usage information (features, constructors, customizations, studs, examples)
- Generates or updates `bindex.json` in the project root.
- If `bindex.json` is present and registration is requested, registers it (via the `register_bindex` tool) and returns the record id/status.

### Output behavior
- When generating/updating, the act outputs **valid JSON only** (no markdown, comments, or extra text) and saves it to `bindex.json`.
- If information for a field is not available, it uses `null` or empty arrays/objects as required by the schema.
- The `classification` section should be filled carefully because it directly affects embedding quality and search results.
