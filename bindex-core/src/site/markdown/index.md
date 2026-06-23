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
8. Throubleshooting
   - Possible to add the following command-line argument to your Java startup command or environment variables: `--add-exports jdk.naming.dns/com.sun.jndi.dns=java.naming`.
9. Resources
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

Bindex Core provides the core services for managing Bindex metadata in the Machanism ecosystem. It helps Ghostwriter and other AI-assisted development workflows discover, register, retrieve, and recommend software libraries through structured metadata stored in Bindex JSON format.

The project combines MongoDB-backed metadata storage, vector-search based semantic retrieval, and GenAI-powered classification. A user or automation process can describe a software need in natural language, and Bindex Core classifies the request, creates embeddings, searches registered library metadata, and returns matching artifacts with concise descriptions. It also exposes function tools that AI agents can call to retrieve a specific Bindex document, register a Bindex definition from a project file, register a Bindex definition from JSON, or pick suitable libraries for a task.

This makes Bindex Core useful for build-time automation, AI coding assistants, library discovery, and project assembly workflows where the assistant should use existing, documented components instead of reinventing functionality from scratch.

## Overview

Bindex Core is organized around three main responsibilities:

- exposing AI-callable function tools for Bindex lookup, registration, and recommendation;
- persisting and retrieving Bindex records in a MongoDB repository;
- classifying user requirements and performing semantic vector searches against registered metadata.

In a typical workflow, a developer, build process, Ghostwriter CLI, or Maven plugin invokes a Bindex tool. The tool delegates to the picker component for registration or recommendation. During registration, the Bindex document is serialized, normalized by classification fields, embedded, and stored. During recommendation, the user request is classified by a GenAI provider, transformed into embeddings, matched against repository records, filtered by score, and returned as a list of candidate libraries.

The following diagram shows the component-level architecture and the relationships with external services:

![Bindex Core C4 Component Diagram](./images/c4-diagram.png)

The architecture keeps metadata access, recommendation logic, and AI-tool integration separated. This separation allows the same core functionality to be reused by command-line tools, Maven plugins, and AI-agent workflows while sharing a common repository and classification model.

## Key Features

- AI-callable tools for retrieving, registering, and recommending Bindex metadata.
- Natural-language library recommendation using GenAI classification and embedding-based vector search.
- MongoDB-backed storage for serialized Bindex documents and searchable classification metadata.
- File-based and JSON-based Bindex registration workflows.
- Score threshold support for tuning recommendation relevance.
- Automatic normalization of classification attributes such as languages and integrations.
- Version-aware result consolidation for library recommendations.
- Ready-to-use act definitions for project assembly, Bindex generation and registration, and library selection.
- Integration-friendly design for Ghostwriter CLI and `gw-maven-plugin` users.

## How to use

This library is included in [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download) by default.

If you use `gw-maven-plugin`, add this library as a plugin dependency:

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

Once available to Ghostwriter, the Bindex tools can be used by acts and AI workflows to:

- register the current project's `bindex.json` metadata;
- register a Bindex record directly from a JSON object;
- retrieve detailed metadata for a known Bindex id;
- request recommended libraries for a natural-language project requirement.

## Acts

### assembly

The `assembly` act helps implement user tasks by first finding suitable libraries through Bindex recommendations. It should be used when an assistant needs to create or update an application and should rely on available library metadata instead of writing everything from scratch. The act instructs the assistant to call the library picker, inspect matching Bindex records, use the recommended libraries, create required project files, build the project, fix errors, and document the result.

### bindex

The `bindex` act generates, validates, and optionally registers a Bindex JSON metadata file for a library project. It should be used when a project needs a `bindex.json` descriptor that conforms to the published Bindex schema. The act focuses on using generated Javadoc as the source of truth, producing valid JSON metadata, including practical usage examples, and registering the file when registration is requested.

### pick

The `pick` act selects libraries that are relevant to a user's query. It should be used when an assistant needs to identify useful dependencies or components before implementing a task. The act calls the Bindex library picker, reviews the recommended metadata, and presents the relevant library candidates to the user.

## Configuration

| Parameter name | Description | Default value |
| --- | --- | --- |
| `gw.model` | General GenAI model used by Ghostwriter workflows and as a fallback for picking when `pick.model` is not configured. | Not set |
| `pick.model` | GenAI model used to classify natural-language library selection requests. | Falls back to `gw.model` |
| `embedding.model` | Embedding provider model used to create classification embeddings for registration and vector search. | Not set |
| `pick.score` | Minimum relevance score used by the exposed library recommendation tool. | `0.85` in picker logic, commonly configured as `0.86` in acts |
| `picker.classificationInstruction` | Prompt template used to convert a user request into Bindex classification JSON. | Built-in classification prompt |
| `BINDEX_REPO_URL` | MongoDB connection URI for the Bindex repository. | `mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0` |
| `BINDEX_USER` | MongoDB user name to inject into the repository URI. | Public user when the default URI is used without a password |
| `BINDEX_PASSWORD` | MongoDB password used for repository access or registration access. | Public password for default read access when unset |
| `gw.interactive` | Enables interactive assistant behavior for acts that may need user input. | Act-specific |
| `gw.nonRecursive` | Limits act processing to the current task scope instead of recursively applying workflows. | Act-specific |
| `gw.path` | File or directory path pattern used by an act. | Act-specific |

## Troubleshooting

If DNS resolution or MongoDB connectivity fails on newer Java runtimes, add the following command-line argument to your Java startup command or environment variables:

```bash
--add-exports jdk.naming.dns/com.sun.jndi.dns=java.naming
```

Additional troubleshooting tips:

- Ensure `embedding.model` is configured before using registration or recommendation workflows.
- Ensure `gw.model` or `pick.model` is configured before calling library recommendation.
- Set `BINDEX_PASSWORD` when registration requires write access to the shared repository.
- Verify that a Bindex file exists at the expected path before using file-based registration.

## Resources

- [Machai documentation](https://machai.machanism.org/)
- [Ghostwriter CLI download](https://machai.machanism.org/ghostwriter/index.html#Download)
- [GitHub repository](https://github.com/machanism-org/machai)
- [GitHub issues](https://github.com/machanism-org/machai/issues)
- [Maven Central: bindex-core](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)
- [Bindex schema](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json)
