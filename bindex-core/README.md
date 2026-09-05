<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId])` after the title as a new paragraph.
   - Add [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/[artifactId]/bindex.json)
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
- If used resources by uri: `src/site/resources/`, need to use project site location: `https://machai.machanism.org/[artifactId]/`.
-->

# Bindex Core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-core.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)

[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/bindex.json)

Bindex Core is the Machai library for creating, validating, registering, and discovering structured Bindex metadata for reusable software artifacts. It supports GenAI-assisted application assembly by combining schema-based descriptors, embeddings, semantic search, and MongoDB-backed storage.

## Introduction

Bindex Core helps applications and AI integrations select libraries from natural-language requirements rather than rebuilding capabilities from scratch. It stores metadata such as library coordinates, classifications, integrations, dependencies, examples, and configuration guidance, then uses classification embeddings and filters to find suitable matches.

Its Java API and AI-facing tools can retrieve metadata, recommend libraries, register descriptors from JSON, files, or URLs, and expose the Bindex schema and generation prompt. This makes library discovery, dependency selection, and integration guidance consistent across Maven projects, MCP servers, and Ghostwriter workflows.

## Usage

Bindex Core is assembled for the [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server) and is included by default in the [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download).

When using `gw-maven-plugin`, add Bindex Core as a plugin dependency:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>RELEASE</version>
  <dependencies>
    <dependency>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-core</artifactId>
      <version>RELEASE</version>
    </dependency>
  </dependencies>
</plugin>
```

For direct Maven use, add the library to your project:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>bindex-core</artifactId>
  <version>RELEASE</version>
</dependency>
```

The AI-facing operations include `get_bindex`, `pick_libraries`, `register_bindex`, and `register_bindex_json`. Configure the host application's GenAI and embedding providers, plus the MongoDB repository connection, before using semantic discovery or metadata registration.

## Resources

- [Bindex Core documentation](https://machai.machanism.org/bindex-core/index.html)
- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [Bindex Maven Plugin](https://github.com/machanism-org/bindex-maven-plugin)
- [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server)
