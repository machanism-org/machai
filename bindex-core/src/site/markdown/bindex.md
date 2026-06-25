---
<!-- @guidance: 
# Instructions
- Generate or update the content as follows.  
- If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
- Analyze additional information from page: `https://machanism.org/bindex/index.html` (selector:`.md-content`) and use it to create a content the current page.
- Add `https://machanism.org/bindex/index.html` link as a reference to additional information.
# Page content
- Analyze the `src/main/resources/acts/bindex.toml` file and use diadram `images/bindex-act-workflow.png` in this section.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- A clear, concise description of the act's purpose and when it should be used.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->
canonical: https://machai.machanism.org/bindex-core/bindex.html
---

# Bindex

Bindex means **Brick Index**. The name is inspired by LEGO bricks: each software library is treated as a reusable building block that can fit with other components to create a larger application.

A `bindex.json` file is a structured descriptor for a library in the Machanism platform. It explains what the library is, where it is located, and how it can be used. This makes the library easier for the platform to discover, compare, recommend, register, and assemble into applications.

## What Bindex is used for

A Bindex file helps answer three practical questions.

### 1. What is this library?

It describes the library itself, including information such as:

- name and unique identifier
- version
- project description
- main features and capabilities
- classification details such as library type, domain, and supported programming languages

This information is especially important for semantic search. It helps the platform match libraries to real user needs instead of relying only on file names or keywords.

### 2. Where is it located?

It explains how the library can be found and retrieved, including:

- repository type, such as Maven, npm, or PyPI
- repository URL
- artifact coordinates, such as group ID, artifact ID, and version
- license information

### 3. How can it be used?

It provides practical usage guidance, including:

- constructors or setup details
- customizations and extension points
- studs, which are interfaces or abstract classes intended for extension or implementation
- features and integration points
- examples that show installation, configuration, and real usage scenarios

## Schema overview

A `bindex.json` file follows a standard schema so library descriptions stay consistent and can be validated automatically.

Common schema properties include:

| Property | Purpose |
| --- | --- |
| `id` | Unique artifact identifier, often in the form `groupId:artifactId:version`. |
| `name` | Full artifact name, often in the form `groupId:artifactId`. |
| `version` | Artifact version. |
| `description` | Human-readable description of the project. |
| `authors` | Author or organization information. |
| `license` | License that controls how the artifact can be used. |
| `classification` | Type, domain, and programming-language information used for semantic search and recommendations. |
| `location` | Repository information and artifact coordinates. |
| `features` | Main capabilities, often with code examples. |
| `constructors` | Ways to instantiate and configure library objects or services. |
| `customizations` | Extension points, configurable options, classes, or interfaces. |
| `studs` | Interfaces or abstract classes designed to be implemented or extended by other modules. |
| `examples` | Additional usage examples and practical scenarios. |

This structure makes Bindex files easier to generate, review, register, search, and reuse across the Machanism platform.

## How a Bindex file is created

A `bindex.json` file is usually generated with AI assistance. The generation process analyzes project metadata and API documentation, then creates a structured JSON description of the library.

The overall process usually looks like this:

1. A user requests creation or update of a `bindex.json` file.
2. The project is analyzed to collect relevant metadata and technical details.
3. A complete `bindex.json` file is generated according to the Bindex schema.
4. The result is reviewed and updated if needed.
5. The file is ready for registration and semantic search.

Automated generation saves time, but review is still important. Developers should confirm that descriptions, classifications, examples, metadata, integration points, and extension points are accurate before registration.

## Registering a Bindex file

After review, a `bindex.json` file can be registered so the platform can index it for discovery and recommendation.

The registration process typically includes:

1. opening the `bindex.json` file from the project root
2. generating an embedding from the description
3. collecting and normalizing programming language information
4. generating domain-related embedding data
5. saving the Bindex information to a vector database
6. confirming that the library has been stored successfully

Once registered, the library becomes available for semantic search and natural-language library selection.

## Act: bindex

The **bindex** act helps create, update, and optionally register a project's `bindex.json` file.

![Bindex act workflow](images/bindex-act-workflow.png)

### Purpose

Use the **bindex** act when you want to prepare a project so it can be understood by the Machanism platform as a reusable software library.

The act generates a Bindex-compliant JSON metadata file that describes the library, its location, its capabilities, and practical ways to use it.

### Main functionality

Based on `src/main/resources/acts/bindex.toml`, the act performs the following high-level work:

1. **Builds Javadoc** by running `mvn clean javadoc:javadoc`.
2. **Checks project suitability** and stops if the current project is a parent project with modules.
3. **Reviews any existing `bindex.json` file** and avoids changing it when it is already correct.
4. **Analyzes generated Javadoc** from `target/reports/apidocs`, including the index, all classes, and package summaries.
5. **Uses the official Bindex schema** to create a valid JSON file with all required fields populated.
6. **Adds useful optional information** when available, such as examples, constructors, customizations, and studs.
7. **Writes the result** to `bindex.json` in the project root.
8. **Registers the file when requested** by validating the root-level `bindex.json` file and calling the registration step.

### What information it tries to include

The act is designed to generate more than a minimal metadata file. It tries to include:

- project identity, version, and description
- author and license information
- classification data for semantic search and recommendation
- repository and artifact location details
- features and integration points
- constructors and configuration details
- customizations and extension points
- studs for interfaces or abstract classes intended for extension
- practical examples that explain how to install, configure, and use the library

### When to use it

Use the **bindex** act when:

- your project does not yet have a `bindex.json` file
- your existing `bindex.json` file is outdated or incomplete
- you want to prepare a library for registration in the Machanism platform
- you want better metadata for semantic search and recommendation
- you want practical usage examples included with the generated library descriptor

### Why it is useful

A well-prepared Bindex file helps the platform and developers understand a library in a practical way.

It improves:

- semantic search quality
- recommendation accuracy
- automated application assembly
- library registration consistency
- developer understanding of installation, configuration, and usage

## Reference

- [The Bindex](https://machanism.org/bindex/index.html)
- [Bindex schema](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json)
