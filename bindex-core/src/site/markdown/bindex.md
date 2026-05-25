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

Bindex means **Brick Index**. It is a `bindex.json` file that gives the Machanism platform a structured, machine-readable description of a software library.

Like a profile for a library, it explains what the library is, where it can be found, and how it can be used. This helps the platform discover libraries, understand their purpose, and recommend them during automated application assembly.

## What Bindex is used for

A Bindex file is designed to answer three practical questions.

### 1. What is this library?
It describes the library itself, including information such as:

- name and unique identity
- version
- description
- major features and capabilities
- classification details such as type, domain, and supported programming languages

This information is especially important for semantic search, because it helps the platform match libraries to real user needs instead of relying only on names or keywords.

### 2. Where is it located?
It explains how the library can be found and retrieved, including:

- repository type and repository URL
- artifact coordinates such as group, artifact, and version
- license information

### 3. How can it be used?
It provides practical usage guidance, including:

- constructors or setup details
- customizations and extension points
- studs, which are interfaces or abstract types meant for extension or implementation
- features and integration points
- examples that show installation, configuration, and real usage scenarios

## Schema overview

A `bindex.json` file follows a standard schema so library descriptions stay consistent and can be validated automatically.

Common schema properties include:

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

This structure makes Bindex files easier to generate, review, register, search, and reuse across the Machanism platform.

## How a Bindex file is created

A `bindex.json` file is typically generated with AI assistance by analyzing project information and producing a structured description of the library.

The overall process usually looks like this:

1. A user requests creation of a `bindex.json` file.
2. The project is analyzed to collect relevant metadata and technical details.
3. A complete `bindex.json` file is generated.
4. The result is reviewed and updated if needed.
5. The file is then ready for registration and semantic search.

Even when generation is automated, review is still important. Developers should confirm that the description, classification, examples, and metadata are accurate and complete before registration.

## Registering a Bindex file

After review, a `bindex.json` file can be registered so the platform can index it for discovery and recommendation.

The registration process typically includes:

1. opening the `bindex.json` file from the project root
2. generating an embedding from the description
3. collecting and normalizing programming language information
4. generating domain-related embedding data
5. saving the Bindex information to the database
6. confirming that the library has been stored successfully

Once registered, the library becomes easier to find through semantic search and natural-language queries.

## Act: bindex

### Purpose
The **bindex** act creates or updates the `bindex.json` file for a project.

Its purpose is to produce a complete JSON description of a software library that follows the official Bindex schema and can be used by the Machanism platform for discovery, recommendation, and integration.

### Main functionality
Based on `src/main/resources/acts/bindex.toml`, this act is designed to:

- build Javadoc before generating library metadata
- review an existing `bindex.json` file and keep it unchanged if it is already correct
- stop when the current project is only a parent project with child modules
- use the published Bindex schema as the required structure
- rely on generated Javadoc in `target/reports/apidocs` for library analysis
- generate a valid `bindex.json` file with all required fields populated
- include optional fields when the information is relevant and available
- add realistic examples, especially practical how-to-use scenarios
- save the result as `bindex.json` in the project root
- register the file when registration is needed and a root-level `bindex.json` is present

### What information it tries to include
The act is intended to generate more than a minimal metadata file. It tries to include:

- project identity, version, and description
- author and license information
- classification data that is useful for embeddings and semantic search
- repository and artifact location details
- features, constructors, customizations, and studs
- example usage scenarios
- installation, configuration, and execution guidance for ready-to-use components such as CLI tools or Maven plugins

### When to use it
Use the **bindex** act when:

- your project does not yet have a `bindex.json` file
- your current `bindex.json` file is outdated
- you want to prepare a library for registration in the Machanism platform
- you want richer metadata to improve search, recommendation, and reuse
- you want documentation-oriented usage examples included in the generated file

### Why it is useful
A well-prepared Bindex file helps the platform understand a library in a practical way.

That improves:

- semantic search quality
- recommendation accuracy
- automated application assembly
- developer understanding of how the library should be installed, configured, and used

## Reference

- https://machanism.org/bindex/index.html
