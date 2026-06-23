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

Bindex Core is the core Bindex metadata and library-discovery module for the Machanism AI development ecosystem. It provides the services and AI-callable tools needed to retrieve registered Bindex records, register new library metadata, and recommend reusable libraries from a natural-language request.

The project helps Ghostwriter, Maven plugin integrations, build automation, and AI-assisted development agents work with accurate library metadata instead of relying only on free-form model knowledge. A Bindex record can describe an artifact, its purpose, examples, installation guidance, and classification metadata, allowing agents to discover ready-to-use components and apply them consistently in project assembly workflows.

Internally, Bindex Core combines a function-tool facade, a picker orchestration service, a repository abstraction, and a MongoDB-backed repository implementation. Registration workflows normalize and enrich Bindex JSON with classification and embedding data, while recommendation workflows classify the user's prompt, create embeddings, perform semantic search, and return candidates that satisfy the configured relevance threshold.

## Overview

Bindex Core delivers three primary capabilities:

- expose Bindex operations as AI-callable tools for retrieval, registration, and recommendation;
- persist Bindex metadata and vector-search information in a repository;
- transform user requirements into searchable classifications and embeddings for semantic library matching.

A common workflow begins when a developer, build process, command-line session, Maven plugin, or AI agent invokes a Bindex operation. For lookup, the module reads a registered metadata document by id. For registration, it accepts a JSON object or reads a Bindex file from the working directory, classifies the metadata, generates embeddings, and stores the enriched record. For recommendation, it converts the user's prompt into structured classification data, embeds that classification, searches for similar registered libraries, and consolidates version-aware results for the caller.

The documentation and bundled acts support the same lifecycle: generating Bindex metadata, registering metadata, selecting libraries, and assembling projects with selected components. The component architecture is summarized below:

![Bindex Core C4 Component Diagram](./images/c4-diagram.png)

The architecture separates tool exposure, orchestration, repository contracts, and persistence. This keeps AI-agent integration simple while allowing the same Bindex registration and recommendation behavior to be reused from command-line, Maven, and direct Java workflows.

## Key Features

- AI-callable tools for Bindex retrieval, file-based registration, JSON-based registration, and library recommendation.
- Semantic library picking from natural-language prompts using GenAI classification and embedding generation.
- MongoDB-backed Bindex storage with document persistence and vector-search support.
- Repository abstraction for lookup, save, delete, and semantic find operations.
- Classification normalization to improve recommendation consistency.
- Configurable relevance threshold for recommendation results.
- Version-aware consolidation of matching candidates.
- Bindex schema-oriented metadata generation and validation support through bundled acts.
- Ready integration with Ghostwriter CLI and `gw-maven-plugin` workflows.

## How to use

This library is included in [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download) as default.

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

After the dependency is available to Ghostwriter or the Maven plugin, Bindex-aware tools and acts can be used to:

- retrieve metadata for a known Bindex id;
- register the current project's `bindex.json` file;
- register a Bindex record directly from JSON;
- recommend libraries that match a natural-language requirement.

## Acts

### assembly

The `assembly` act guides an assistant through implementing a user task with help from Bindex library recommendations. Use it when a project should be created or updated and the assistant must search for reusable libraries, retrieve detailed Bindex metadata, apply documented usage examples, add required project files, build the project, fix errors, and document the completed result.

### bindex

The `bindex` act generates, updates, validates, and optionally registers a Bindex-compliant metadata file for a library project. Use it when a project needs a current `bindex.json` descriptor based on Javadoc, schema requirements, installation and configuration guidance, practical usage examples, and accurate classification data for embedding-based search.

### pick

The `pick` act helps select libraries for a user's request. Use it when an assistant needs to identify candidate dependencies or reusable components before implementation. It calls the Bindex picker, analyzes recommended libraries, retrieves detailed metadata when appropriate, and presents relevant options to the user.

## Configuration

| Parameter name | Description | Default value |
| --- | --- | --- |
| `gw.model` | General Ghostwriter GenAI model used by acts and as a fallback model for library-picking classification. | Not set |
| `pick.model` | GenAI model used to classify natural-language library selection prompts. | Falls back to `gw.model` |
| `embedding.model` | Embedding model used to create classification embeddings for registration and semantic search. | Not set |
| `pick.score` | Minimum relevance score for recommendation results returned by the picker tool. | `0.85` in picker logic; `0.86` in bundled acts |
| `picker.classificationInstruction` | Prompt template used to convert a user request into Bindex classification JSON. | Built-in classification prompt |
| `BINDEX_REPO_URL` | MongoDB connection URI used by the Bindex repository. | `mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0` |
| `BINDEX_USER` | MongoDB user name injected into the repository URI. | Default public repository user when unset |
| `BINDEX_PASSWORD` | MongoDB password used for repository access and registration. | Default public repository password when unset |
| `gw.interactive` | Enables interactive assistant behavior for acts that may need user input. | Act-specific |
| `gw.nonRecursive` | Prevents recursive act execution and keeps processing scoped to the current task. | Act-specific |
| `gw.path` | File or directory pattern used by an act when processing project content. | Act-specific |

## Troubleshooting

If DNS resolution or MongoDB connectivity fails on newer Java runtimes, add the following command-line argument to your Java startup command or environment variables:

```bash
--add-exports jdk.naming.dns/com.sun.jndi.dns=java.naming
```

Additional troubleshooting tips:

- Configure `embedding.model` before using registration or recommendation workflows.
- Configure `gw.model` or `pick.model` before calling library recommendation.
- Set `BINDEX_PASSWORD` when registration requires write access to the shared repository.
- Verify that a valid Bindex file exists at the expected path before using file-based registration.
- Ensure MongoDB network access is available from the runtime environment.

## Resources

- [Machai documentation](https://machai.machanism.org/)
- [Ghostwriter CLI download](https://machai.machanism.org/ghostwriter/index.html#Download)
- [GitHub repository](https://github.com/machanism-org/machai)
- [GitHub issues](https://github.com/machanism-org/machai/issues)
- [Maven Central: bindex-core](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)
- [Bindex schema](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json)
