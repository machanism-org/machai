<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-mcp-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
   - Add the Ghostwriter CLI application jar download link: [![Download Bindex Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex-core/releases/) to the installation section.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# Bindex Core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-mcp-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)

Bindex Core is the core Bindex metadata and library-discovery module for the Machanism AI development ecosystem. It provides AI-callable tools and services to retrieve registered Bindex records, register new library metadata, and recommend reusable libraries from natural-language requirements.

## Introduction

Bindex Core helps Ghostwriter, Maven plugin integrations, build automation, and AI-assisted development agents work with accurate library metadata instead of relying only on free-form model knowledge. A Bindex record can describe an artifact, its purpose, examples, installation guidance, and classification metadata, allowing agents to discover ready-to-use components and apply them consistently in project assembly workflows.

Internally, Bindex Core combines a function-tool facade, a picker orchestration service, a repository abstraction, and a MongoDB-backed repository implementation. Registration workflows normalize and enrich Bindex JSON with classification and embedding data, while recommendation workflows classify the user's prompt, create embeddings, perform semantic search, and return candidates that satisfy the configured relevance threshold.

## Overview

Bindex Core delivers three primary capabilities:

- expose Bindex operations as AI-callable tools for retrieval, registration, and recommendation;
- persist Bindex metadata and vector-search information in a repository;
- transform user requirements into searchable classifications and embeddings for semantic library matching.

A common workflow begins when a developer, build process, command-line session, Maven plugin, or AI agent invokes a Bindex operation. For lookup, the module reads a registered metadata document by id. For registration, it accepts a JSON object or reads a Bindex file from the working directory, classifies the metadata, generates embeddings, and stores the enriched record. For recommendation, it converts the user's prompt into structured classification data, embeds that classification, searches for similar registered libraries, and consolidates version-aware results for the caller.

The documentation and bundled acts support the same lifecycle: generating Bindex metadata, registering metadata, selecting libraries, and assembling projects with selected components.

## Usage

### Installation

This library is included in [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download) by default.

[![Download Bindex Core](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/bindex-core/releases/)

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

### Common operations

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
| `BINDEX_REPO_URL` | MongoDB connection URI used by the Bindex repository. | `mongodb+srv://cluster0.hivfnpr.mongodb.net/?appName=Cluster0` |
| `BINDEX_USER` | MongoDB user name injected into the repository URI. | Default public repository user when unset |
| `BINDEX_PASSWORD` | MongoDB password used for repository access and registration. | Default public repository password when unset |

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
