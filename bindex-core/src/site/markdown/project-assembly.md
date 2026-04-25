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

AI Assembly is a Machanism feature for turning a plain-language request into the first working version of an application or major feature. Instead of only generating isolated code snippets, it helps assemble a more practical project foundation by combining AI generation with curated library information from the Machanism ecosystem.

In simple terms, you describe what you want to build, AI Assembly looks for suitable libraries, reads their structured metadata, and prepares the initial files, dependencies, configuration, and implementation needed to get started.

Reference: https://machanism.org/ai-assembly/index.html

## What AI Assembly does

AI Assembly is designed to help developers move from an idea to an initial project more quickly. It focuses on:

- understanding a natural-language project request
- finding relevant libraries based on intent, not only keywords
- analyzing structured library metadata from `bindex.json`
- generating an initial project structure and configuration
- creating starter source code and integration points
- giving developers a buildable starting point that they can review and improve

Unlike generic code generation, AI Assembly is meant to work with the Machanism platform's curated library ecosystem. This makes the generated result more grounded in real libraries, known integration details, and practical setup requirements.

## How AI Assembly works

AI Assembly relies on `bindex.json`, a structured descriptor file used for libraries in the Machanism ecosystem.

According to the platform documentation, `bindex.json` files are created automatically by analyzing project artifacts such as build files, source code, and other metadata. These files can contain information such as:

- the purpose of a library
- supported features
- integration points
- usage examples
- dependency and build details
- authorship and licensing information

These descriptors are indexed for semantic search. This allows the system to search by meaning and use case, which helps it recommend libraries that fit the user's goal more accurately.

A typical AI Assembly flow is:

1. **The developer describes the request**
   - The request is written in natural language and explains the application's purpose, important features, and any specific requirements.
2. **Relevant libraries are picked**
   - Semantic search checks indexed `bindex.json` data and ranks libraries that best match the request.
3. **Library metadata is analyzed**
   - The assistant reads detailed Bindex information to understand capabilities, examples, setup requirements, and integration details.
4. **The project is assembled**
   - The assistant generates the initial project structure, configuration files, dependencies, and starter implementation.
5. **The developer reviews the output**
   - The generated result is a starting point. The developer is still responsible for reviewing functionality, quality, and security.

## Why it is useful

AI Assembly is useful when you need more than a short example and want a realistic starting project. It helps reduce manual setup work and supports better initial library selection by using structured metadata instead of relying only on general code generation.

This is especially helpful when:

- you know what you want to build but not which libraries to choose
- you want an initial implementation that already includes dependencies and configuration
- you need a project skeleton that can be built on instead of starting from a blank project

# Act: Assembly

This project includes an Act named **Assembly**, defined in `src/main/resources/acts/assembly.toml`.

## Purpose

The **Assembly** act is used to implement a user request by finding suitable libraries and using them to build the requested application or feature.

Its main purpose is to help the assistant create a functional project foundation instead of producing disconnected code snippets. It is best suited for requests that explain what should be built, while the exact libraries, setup, and implementation details still need to be chosen.

## When to use it

Use the **Assembly** act when you:

- want to create a new application
- want to add a major feature to an existing project
- have a request written in natural language
- need help selecting libraries that fit the task
- want the assistant to generate or update multiple project files
- want a practical, buildable starting point rather than a single code example

## Main functionality

Based on `assembly.toml`, the **Assembly** act guides the assistant to:

- implement the user's task comprehensively and correctly
- use `pick_libraries` with the user's query to find recommended libraries
- analyze which recommended libraries actually match the request
- use `get_bindex` to read detailed metadata for matching libraries
- use `get_bindex_schema` to understand the Bindex structure
- prefer suitable libraries instead of writing everything from scratch
- create all necessary files in the project folder
- add required code and public dependencies
- clean and build the project, then fix errors after changes
- make the project functional
- create a detailed `README.md`
- ask the user for missing information when needed
- use the default **Clean Architecture** template unless the user requests something else

## Important behavior

The act configuration also defines several important operating rules:

- **Interactive mode is enabled** (`gw.interactive = true`), so the assistant can ask follow-up questions when required information is missing.
- **Non-recursive mode is enabled** (`gw.nonRecursive = true`), which limits how the act is applied.
- **A recommendation threshold is defined** (`pick.score = 0.86`), which helps focus on stronger library matches.
- The instructions explicitly support working with the local file system and command-line tools.
- The configuration reminds the assistant to use `cmd /c` on Windows and `sh -c` on Unix-like systems for shell execution.

## In simple words

If you know what you want to build but do not know which libraries or setup to use, **Assembly** is the act for that situation.

It helps turn requests such as:

- “Create a REST API for user login”
- “Build a command-line tool for processing files”
- “Generate an application that integrates with a specific platform”

into an initial project with recommended libraries, configuration, source files, and documentation.

## Summary

The **Assembly** act is intended for project creation and structured implementation. It helps move from a high-level request to a practical starting application by:

- finding relevant libraries
- reading their metadata and examples
- generating the necessary project files
- producing an initial implementation that a developer can review and continue improving
