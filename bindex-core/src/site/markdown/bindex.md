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

# Bindex Act

The **Bindex Act** helps create and register a `bindex.json` file for a software library. A `bindex.json` file is a structured metadata document that describes what a library is, where it can be found, and how it can be used.

Bindex stands for **Brick Index**. The idea is similar to LEGO bricks: each library is described as a reusable building block that can be discovered, selected, and assembled into larger applications. By creating a clear Bindex descriptor, a library becomes easier for people and AI-powered tools to understand and reuse.

![Bindex Act workflow](images/bindex-act-workflow.png)

## What the Bindex Act does

The Bindex Act generates a Bindex-compliant JSON metadata object for a project. It focuses on practical library documentation, including installation details, configuration instructions, and usage examples.

The generated `bindex.json` file can include information such as:

- The library name, version, description, authors, and license.
- Repository and coordinate information, such as Maven group ID, artifact ID, and version.
- Classification details used for semantic search, including library type, domain, and supported programming languages.
- Main features and practical examples.
- Constructors, customization points, extension points, and interfaces intended for implementation.
- Ready-to-use instructions for components such as CLI tools, Maven plugins, or other reusable modules when relevant.

## When to use it

Use the Bindex Act when you want to make a project easier to discover, recommend, and integrate as a reusable library.

Typical use cases include:

- Creating a new `bindex.json` file for a library project.
- Updating an existing `bindex.json` file when project metadata, version, features, or usage examples have changed.
- Preparing a library for registration in a vector database so it can be found through semantic search.
- Documenting how other developers or AI assembly tools should install, configure, and use the library.

The act should be used for real library modules, not parent projects. If a project is only a parent or aggregator project, the act is not intended to generate a Bindex file for it.

## Act workflow

The Bindex Act is organized into three main stages.

### 1. Build Javadoc

The first stage builds the project Javadoc documentation:

```bash
mvn clean javadoc:javadoc
```

This step creates API documentation under `target/reports/apidocs`. The generated Javadoc is used as the primary source for understanding the public API, package descriptions, classes, and available usage patterns.

### 2. Generate `bindex.json`

The generation stage analyzes the project documentation and build metadata, then creates or updates the `bindex.json` file in the project root.

During this stage, the act is instructed to:

- Use generated Javadoc files such as `target/reports/apidocs/index.html`, `allclasses-index.html`, and package summary pages.
- Use the effective project build file, such as the Maven effective POM.
- Review an existing `bindex.json` file if one already exists.
- Correct outdated or inconsistent information, such as version numbers or feature descriptions.
- Follow the official Bindex schema strictly.
- Save only valid JSON without comments, markdown, or extra explanatory text.

The resulting file is designed to answer three important questions:

1. **What is this library?**  
   It describes the library name, version, purpose, features, examples, and classification.

2. **Where is it located?**  
   It records repository information, coordinates, and license details so the library can be retrieved correctly.

3. **How can it be used?**  
   It provides constructors, customization points, extension points, examples, and practical integration guidance.

### 3. Register `bindex.json`

The registration stage is used when the generated `bindex.json` file needs to be registered.

In this stage, the act checks that `bindex.json` exists in the project root, verifies the file, and registers it through the Bindex registration tool. After registration, the system returns a record identifier and a status message.

Registration makes the library available for semantic search and retrieval. The registered metadata can be stored with generated embeddings for the description, domains, and programming language information, helping AI tools recommend the library based on user intent.

## Why Bindex is useful

A well-prepared `bindex.json` file gives both developers and automated tools a consistent way to understand a library. It improves discoverability, reduces guesswork during integration, and helps ensure that reusable components are described in a predictable format.

For users, this means:

- Faster understanding of what a library does.
- Clearer installation and usage instructions.
- Better search and recommendation results.
- Easier integration into larger applications.

For library maintainers, it provides a structured way to keep important project information, usage examples, and integration details in one place.

## Reference

Additional information about Bindex is available in the official documentation:

- [The Bindex](https://machanism.org/bindex/index.html)
