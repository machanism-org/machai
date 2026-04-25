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

Bindex means **Brick Index**. It is a `bindex.json` file that describes a software library in a structured, machine-readable way so the Machanism platform can discover it, understand it, and recommend it during application assembly.

You can think of it as a profile for a library. It explains what the library does, where it can be found, and how developers can use it.

## What Bindex is used for

A Bindex file helps answer three important questions.

### 1. What is this library?
It describes the library itself, including:

- its name, version, and unique identity
- a description of its purpose
- its main features and capabilities
- classification details such as library type, domain, and supported programming languages

This information supports semantic search and helps the platform recommend suitable libraries more accurately.

### 2. Where is it located?
It tells the platform where the library can be obtained, including:

- repository type and repository URL
- artifact coordinates such as group, artifact, and version
- license information

### 3. How can it be used?
It gives practical usage information, including:

- constructors or setup details
- customizations and extension points
- studs, which are interfaces or abstract types designed for extension or implementation
- features and integration points
- examples that show how to install, configure, and use the library

## Schema overview

A `bindex.json` file follows a standard schema so all library descriptions stay consistent.

Common properties include:

- `id`
- `name`
- `version`
- `description`
- `authors`
- `license`
- `classification`
- `location`
- `features`
- `constructors`
- `customizations`
- `studs`
- `examples`

This schema makes Bindex files easier to validate, register, search, and reuse across the Machanism platform.

## How a Bindex file is created

A `bindex.json` file is usually generated automatically by analyzing project metadata and source files. Typical inputs include build files, source code, and available documentation.

The process normally looks like this:

1. A user requests creation of a `bindex.json` file.
2. The project is analyzed to collect metadata and technical details.
3. A structured `bindex.json` file is generated.
4. The result is reviewed and corrected if needed.
5. The file can then be registered so it becomes available for semantic search and retrieval.

This review step matters because automatically generated metadata should always be checked for accuracy and completeness before registration.

## Registering a Bindex file

After review, a `bindex.json` file can be registered so its information is stored for semantic search and recommendation.

The registration process typically includes:

1. opening the `bindex.json` file from the project root
2. generating embeddings from the description and domain information
3. collecting and normalizing programming language information
4. storing the Bindex data in the database
5. confirming that the library has been registered successfully

This makes the library easier to discover when users search by intent, topic, or use case.

## Act: bindex

### Purpose
The **bindex** act creates or updates a `bindex.json` file for a project.

Its purpose is to generate a complete JSON description of a software library that follows the Bindex schema and can be used by the Machanism platform for discovery, recommendation, and integration.

### Main functionality
Based on the act definition in `src/main/resources/acts/bindex.toml`, the act is designed to:

- inspect the project and review an existing `bindex.json` file if one is already present
- leave the file unchanged when the current content is already correct
- work only on projects that are not parent projects with child modules
- obtain the Bindex schema and use it as the required structure
- analyze the main source, resource, and documentation folders
- generate a valid `bindex.json` file with required fields and useful optional fields when relevant
- include realistic metadata, classification details, and usage information
- save the result as `bindex.json` in the project root
- register the file when registration is needed and a root-level `bindex.json` is available

The act is intended to produce a complete and useful metadata file rather than a minimal placeholder.

### What information it tries to include
The generated Bindex content is intended to cover areas such as:

- project identity, version, and description
- authors and license information
- classification data used for semantic search and embeddings
- repository and artifact location details
- features, constructors, customizations, and studs
- practical examples that explain how to use the library
- installation, configuration, and execution guidance for ready-to-use components such as CLI tools or plugins

### When to use it
Use the **bindex** act when:

- your project does not yet have a `bindex.json` file
- your existing `bindex.json` file needs to be refreshed after project changes
- you want to prepare a library for registration in the Machanism platform
- you want structured metadata that improves search, recommendation, and reuse
- you want practical examples included in the generated metadata

### Why it is useful
A good Bindex file helps the platform understand a library beyond its name alone.

That improves:

- semantic search
- recommendation quality
- automated application assembly
- developer understanding of how the library should be integrated and used

## Reference

- https://machanism.org/bindex/index.html
