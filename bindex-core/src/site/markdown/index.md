---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([!\[Maven Central\](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
2. Introduction
   - Provide a comprehensive description of the project, including its purpose and benefits.
   - Analyze java files in the `src/main/java` to inform the description.
3. Overview
   - Clearly explain the main functions and value proposition of the project.
   - Summarize how the project workflows and documentation.
   Describe the project with diagrams bellow:
     - Create a project structure overview based on the `.puml` files below.
     - Describe the project without including file names in the description.
     - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
4. Key Features
   - Present a bulleted list of the primary capabilities and unique features of the project.
5. How to use
   - This library is included in [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download) as default. 
   - If you use `gw-maven-plugin`, you need to add this library by the dependency, e.g:
   ```xml
	<plugin>
		<groupId>org.machanism.machai</groupId>
		<artifactId>gw-maven-plugin</artifactId>
		<version>1.1.1</version>
		...
		<dependencies>
			<dependency>
				<groupId>org.machanism.machai</groupId>
				<artifactId>bindex-core</artifactId>
				<version>1.1.1</version>
			</dependency>
		</dependencies>
	</plugin>
  ```  
6. Acts
	- Analyze all act TOML files located in the `src/main/resources/acts` folder.
	- For each act, create a section that includes:
		 - The act's name.
		 - A clear, concise description of the act's purpose and when it should be used.
	- Organize your output so that each act is easy to identify and understand.
	- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
7. Configuration
   - Include a table of common configuration parameters, with columns for parameter name, description, and default value.
   - Ensure descriptions are clear and concise.
8. Resources
   - Provide a list of relevant links, including:
     - Official platform or documentation site
     - GitHub repository
     - Maven Central page
     - Any other useful resources
# General Instructions     
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, tables, code blocks, and links.
- Ensure clarity, conciseness, and easy navigation throughout the page.
-->
canonical: https://machai.machanism.org/bindex-core/index.html
---

# Bindex Core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-core.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)

## Introduction

Bindex Core is the metadata and recommendation engine behind Bindex-based library discovery in the MachAI ecosystem. It provides the runtime services needed to store structured library descriptors, retrieve them by identifier, recommend libraries from natural-language requirements, and expose these capabilities as AI-callable tools for Ghostwriter-driven workflows.

The project helps turn library selection and reuse into a repeatable, automation-friendly process. Instead of relying only on ad hoc search or free-form documentation, it works with schema-based metadata, persists that metadata in a shared repository, enriches classification data with embeddings, and makes the result available through reusable acts and function tools. This improves discoverability, supports consistent project assembly, and enables assistants to work from registered library knowledge instead of starting from scratch.

## Overview

Bindex Core supports a metadata-centered workflow for discovering, registering, and reusing software libraries.

1. **Repository access** manages MongoDB connectivity and stores serialized metadata records for registered libraries.
2. **Semantic recommendation** transforms natural-language requirements into structured classifications, generates embeddings, and performs vector-based search with language and layer filtering.
3. **AI tool integration** exposes metadata lookup, recommendation, and registration operations as callable tools that can be attached to GenAI providers.
4. **Workflow acts** provide reusable prompts for implementation assembly, metadata generation, and library picking scenarios.

### Architecture

![C4 Diagram](./images/c4-diagram.png)

The architecture centers on a compact collaboration model.

- A persistence layer opens connections to the shared MongoDB repository and handles direct metadata retrieval and deletion.
- A recommendation layer serializes library classifications, generates embeddings, performs semantic matching, and stores new registrations.
- A tool layer bridges these runtime capabilities into AI-assisted workflows, including library lookup, recommendation, and local metadata registration from the working directory.
- Supporting workflow templates guide assistants in choosing libraries, generating metadata, and assembling implementations from recommended components.

Together, these responsibilities move a request from natural-language intent to actionable, metadata-backed library selection and registration.

## Key Features

- Persist Bindex metadata as serialized JSON documents in MongoDB.
- Resolve repository access from configurable connection settings and optional registration credentials.
- Generate embeddings from classification metadata for semantic search.
- Convert natural-language prompts into schema-aligned classifications for recommendation workflows.
- Filter recommendations by programming language and architectural layer.
- Expose Bindex retrieval, recommendation, and registration as AI function tools.
- Register local `bindex.json` files from the working directory into the shared repository.
- Provide reusable acts for project assembly, metadata authoring, and library discovery.

## How to use

This library is included in [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download) as default.

If you use `gw-maven-plugin`, you need to add this library by the dependency, e.g:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>1.1.1</version>
  ...
  <dependencies>
    <dependency>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-core</artifactId>
      <version>1.1.1</version>
    </dependency>
  </dependencies>
</plugin>
```

After adding the dependency, the library can support Ghostwriter workflows that:

- retrieve registered metadata by Bindex identifier,
- recommend candidate libraries from a user requirement,
- register a local `bindex.json` document into the shared repository.

## Acts

### `assembly`

Use this act to implement a user request by selecting recommended libraries and building the solution around them. It is intended for end-to-end development flows where the assistant should ask Bindex for library candidates, inspect detailed metadata, modify the project, build it, and iterate until the requested behavior is complete.

### `bindex`

Use this act to create or refresh a `bindex.json` file for a project or reusable library. It is designed for metadata-authoring workflows where the assistant should build Javadoc, use the Bindex schema, generate a schema-compliant descriptor, include practical usage examples, and register the result when the metadata file exists in the project root.

### `pick`

Use this act when the goal is to recommend suitable libraries without implementing the full solution. It is best for discovery workflows where the assistant should query for recommendations, review the returned candidates, and present the most relevant options for the user’s requirement or project update.

## Configuration

| Parameter | Description | Default value |
|---|---|---|
| `BINDEX_REPO_URL` | MongoDB connection URI for the shared Bindex repository. | `mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0` |
| `BINDEX_REG_PASSWORD` | Password used to enable registration-capable repository access. | unset |
| `gw.model` | GenAI model used by Bindex-related tool and act workflows. | `CodeMie:gpt-5.4-2026-03-05` |
| `pick.model` | GenAI model used for library classification and recommendation. | `CodeMie:gpt-5.4-2026-03-05` |
| `embedding.model` | Embedding model used for semantic indexing and search. | `CodeMie:text-embedding-005` |
| `pick.score` | Minimum vector-search score accepted for recommendations. | `0.86` |
| `picker.classificationInstruction` | Optional custom prompt template for converting a user request into classification JSON. | built-in instruction |
| `gw.interactive` | Enables interactive execution for applicable acts. | `true` |
| `gw.nonRecursive` | Limits processing to the current project scope for applicable acts. | `true` |
| `gw.paths` | Scan scope used by the metadata-generation workflow. | `glob:.` |

## Resources

- [Project site](https://machai.machanism.org/bindex-core/index.html)
- [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html)
- [GW Maven Plugin](https://machai.machanism.org/gw-maven-plugin/index.html)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)
- [Issue tracker](https://github.com/machanism-org/machai/issues)
