---
<!-- @guidance: 
# Instructions
- This is The Bindex page to describe What is the bindex json file and how to create it. 
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

# The Bindex

Bindex means **Brick Index**. The name comes from the idea that software libraries can work like LEGO bricks: each library is a reusable component that can be selected, connected, and assembled into a larger application.

A `bindex.json` file is a structured metadata file that describes a software library in a standard way. It helps tools and AI assistants understand what a library is, where it can be found, and how it should be used. This makes libraries easier to discover, recommend, register, and integrate into applications.

## What is a bindex.json file?

The `bindex.json` file is a machine-readable description of a library. It answers three important questions for developers and automated tools.

### 1. What is this library?

A Bindex file describes the identity and purpose of the library, including information such as:

- The library name, often in a format such as `groupId:artifactId`.
- The library version.
- A clear description of the project.
- The main features and capabilities.
- Classification details such as library type, domain, and supported programming languages.

This information is especially useful for semantic search and library recommendation.

### 2. Where is the library located?

A Bindex file explains how the library can be retrieved, including:

- Repository type, such as Maven, npm, or PyPI.
- Repository URL.
- Coordinates such as group ID, artifact ID, and version.
- License information.

This helps users and tools locate the exact artifact that should be installed or referenced.

### 3. How can the library be used?

A Bindex file provides practical usage information, including:

- Constructors or setup information for creating objects and services.
- Customization points, such as configuration options, extension classes, or interfaces.
- Studs, which are interfaces or abstract classes intended to be implemented or extended.
- Features and examples that show how the library can be used in real scenarios.

## Main Bindex schema properties

A valid `bindex.json` file follows the Bindex schema. Important properties include:

| Property | Purpose |
| --- | --- |
| `id` | A unique identifier for the artifact, often including group ID, artifact ID, and version. |
| `name` | The full artifact name. |
| `version` | The artifact version. |
| `description` | A summary of what the project does. |
| `authors` | Author or organization information. |
| `license` | License terms for the artifact. |
| `classification` | Type, domain, supported languages, and other information used for semantic search. |
| `location` | Repository and coordinate information. |
| `features` | Main library capabilities, usually with examples. |
| `constructors` | Information about how to create or configure objects and services. |
| `customizations` | Extension points and configurable behavior. |
| `studs` | Interfaces or abstract classes designed for implementation or extension. |
| `examples` | Practical usage scenarios. |

The full schema is available in the [Bindex schema v2](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json).

## Creating a bindex.json file

A `bindex.json` file can be generated with the Bindex Act. The generation process uses project documentation and build metadata to create a complete JSON descriptor that follows the Bindex schema.

For Java projects, the Bindex Act is designed to use generated Javadoc and the effective build file. This helps ensure the file describes the public API and project metadata instead of relying on implementation details.

A typical creation process includes:

1. Build the project Javadoc.
2. Read the generated API documentation.
3. Read the effective project build file.
4. Create or update `bindex.json`.
5. Validate that the JSON follows the Bindex schema.
6. Review the generated file for correctness.

Developers should always review the generated result. AI generation can save time, but the final metadata should be checked for accurate descriptions, correct versions, valid repository coordinates, useful examples, and complete classification details.

## Registering a bindex.json file

After the `bindex.json` file is created and reviewed, it can be registered. Registration stores the Bindex metadata so it can be used for discovery and semantic search.

During registration, the system typically:

1. Opens and checks the `bindex.json` file.
2. Generates semantic embeddings from the description.
3. Collects and normalizes programming language information.
4. Generates embeddings for domains and classification data.
5. Saves the metadata and embeddings to a vector database.
6. Returns a registration status and record identifier.

Once registered, the library becomes easier to find using natural language requirements, because the system can match user intent with the metadata stored from the Bindex file.

## Bindex Act

The Bindex Act helps users create, update, and optionally register a `bindex.json` file for a software library.

![Bindex Act workflow](images/bindex-act-workflow.png)

### Purpose

Use the Bindex Act when you want to make a project easier for AI tools and developers to discover and use as a library. The Act generates a Bindex-compliant JSON file that describes the library, its features, installation details, configuration options, usage examples, and integration points.

### When to use it

Use this Act when:

- A project should be published or consumed as a reusable library.
- You need a standard metadata file for library discovery.
- You want to improve semantic search and recommendation for the library.
- An existing `bindex.json` file may be outdated and needs to be refreshed.
- You want to register the library metadata after reviewing the generated file.

Do not use the generation step for parent or aggregator projects that only organize modules and are not themselves usable libraries.

### Main functionality

The Bindex Act performs three main jobs.

#### 1. Build Javadoc

The Act first builds the project Javadoc. For Maven projects, it runs a command similar to:

```bash
mvn clean javadoc:javadoc
```

This creates API documentation that can be analyzed to understand packages, classes, methods, and public usage patterns.

#### 2. Generate or update bindex.json

The Act generates a `bindex.json` file in the project root. If the file already exists, the Act checks whether it still matches the current project and updates outdated or inconsistent information.

The generated file should include:

- Required schema fields.
- Realistic descriptions from package and class documentation.
- Library classification data for semantic search.
- Repository and coordinate information from the build file.
- Practical examples that explain how to install, configure, and use the library.
- Details about constructors, customizations, studs, and features when relevant.

The output must be valid JSON and must conform to the official Bindex schema.

#### 3. Register bindex.json

If registration is requested, the Act checks that `bindex.json` exists and then registers it. After registration, it reports the record identifier and a status message.

Registration makes the library available for semantic search and future automated assembly workflows.

## Best practices

To get the best results from Bindex:

- Keep project Javadoc clear and complete.
- Make sure the build file contains accurate metadata, including version, license, and repository information.
- Review generated examples to ensure they are practical and correct.
- Check that classification details describe the library domain and supported languages accurately.
- Validate the JSON before registration.
- Update and re-register the Bindex file when the public API or project metadata changes.

## Additional information

For more details, see the original Bindex documentation: [https://machanism.org/bindex/index.html](https://machanism.org/bindex/index.html).
