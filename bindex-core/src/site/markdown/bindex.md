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

A Bindex file gives a library a clear profile. It explains what the library is, where it can be found, and how it can be used.

## What information Bindex contains

A Bindex file is designed to answer three basic questions.

### 1. What is this library?
It includes information such as:

- the library identity, name, and version
- a description of the library and its purpose
- the main features, often with examples
- classification details such as type, domain, and supported programming languages

### 2. Where is it located?
It includes location and delivery details such as:

- repository type and repository URL
- artifact coordinates such as group, artifact, and version
- license information

### 3. How can it be used?
It includes practical usage details such as:

- constructors or setup details
- customization and extension points
- studs, which are interfaces or abstract types meant to be implemented or extended
- usage examples and integration guidance

The file follows a standard schema so different libraries can be described in a consistent way. It is usually generated automatically by analyzing project metadata and source files, then reviewed by a user to make sure the result is accurate and complete.

## How Bindex is created and used

The Bindex process usually works like this:

1. A user requests creation of a `bindex.json` file.
2. The project is analyzed, including build files, source code, and available metadata.
3. A structured `bindex.json` file is generated.
4. The generated file is reviewed and corrected if needed.
5. The file can then be registered so it becomes available for semantic search and discovery.

This review step is important because automatically generated content should always be checked for correctness before it is registered.

## Act: bindex

### Purpose
The **bindex** act creates or updates a `bindex.json` file for a software library.

Its purpose is to produce a complete JSON description that follows the Bindex schema and gives enough detail for search, discovery, recommendation, and integration in the Machanism ecosystem.

### What it does
The act analyzes project information and generates a structured JSON object with realistic, useful metadata.

It is designed to include:

- library identity, name, and version
- a detailed description
- authors and license information
- classification details such as type, domains, languages, layers, usage context, target environment, and integrations
- repository information and artifact coordinates
- constructors, features, customizations, and studs
- practical examples that explain how to install, configure, and use the library
- dependency information when relevant

The act aims to create a complete and helpful result rather than a minimal placeholder.

### When to use it
Use the **bindex** act when:

- your project does not yet have a `bindex.json` file
- an existing `bindex.json` file needs to be refreshed after project changes
- you want to prepare a library for registration in the Machanism platform
- you want practical usage examples included in the generated metadata
- your library provides a ready-to-use component such as a CLI tool, plugin, or similar executable feature and you want installation and configuration guidance documented clearly

### How it works
The act instructs the system to generate valid JSON that matches the Bindex structure.

It expects the output to include required fields and useful optional fields when they add value. It also emphasizes:

- realistic values instead of placeholders
- clear descriptions that help semantic search
- practical code examples with explanations
- step-by-step usage scenarios
- installation, configuration, and execution instructions for ready-to-use components

If registration is needed, the act also includes rules for checking whether `bindex.json` exists in the project root and registering it when available.

### Why it matters
A well-prepared Bindex file helps the platform understand a library beyond its name alone. This improves discovery, recommendations, and automated assembly because the system has structured information about the library's purpose, features, domains, and usage patterns.

It is especially valuable for semantic search, where accurate descriptions and classifications help match libraries to real development needs.

## Reference

- https://machanism.org/bindex/index.html
