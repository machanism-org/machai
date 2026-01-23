<!--
@guidance:
**ADD FOLLOWING SECTIONS TO THIS README FILE:**

1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Use `src/site/markdown/index.md` as the primary source of information for generating the project description. Summarize and adapt its content as needed for clarity and conciseness.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])` after the title as a new paragraph. [groupId] and [artifactId] need to use from pom.xml.
2. **Installation Instructions:**  
   - Describe how to checkout the repository and build the project using Maven.
   - Include prerequisites such as Java version and build tools.
3. **Usage:**  
   - Explain how to run or use the project and its modules.
   - Provide examples of usage with configuration.
4. **Other Rules**
   - Do not use the horizontal rule separator between sections.	
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# Bindex Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

The **Bindex Maven Plugin** generates and optionally registers/publishes `bindex.json` metadata for Maven modules in the Machanism ecosystem. By producing a consistent, structured descriptor per module, it improves artifact discovery, integration, and assembly workflows, and supports GenAI-powered semantic search scenarios that rely on rich, standardized metadata.

## Installation Instructions

### Prerequisites

- Java 8+
- Maven 3.6+
- Git

### Checkout and build

```bash
git clone https://github.com/machanism-org/machai.git
cd machai/bindex-maven-plugin
mvn -DskipTests package
```

## Usage

### Run goals directly

Generate (or update) `bindex.json`:

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex
```

Register/publish the descriptor (optional):

```bash
mvn org.machanism.machai:bindex-maven-plugin:register
```

### Typical workflow

1. Configure the plugin in your project `pom.xml` (optionally bind it to a lifecycle phase).
2. Run `mvn org.machanism.machai:bindex-maven-plugin:bindex` to generate or update `bindex.json`.
3. Commit `bindex.json` if your repository policy expects generated metadata to be versioned.
4. If you use a registry, configure credentials/settings and run `mvn org.machanism.machai:bindex-maven-plugin:register` (or run it in CI).

### Configuration

Registration/publishing may require credentials depending on your registry configuration.

| Environment variable | Description |
|---|---|
| `BINDEX_REG_PASSWORD` | Password/token used when writing to the registration database/registry (only required if registration is enabled). |

Common parameters:

| Parameter | Description | Default |
|---|---|---|
| `update` | Whether to update an existing `bindex.json` if present. | `true` |

Example (disable updates):

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex -Dupdate=false
```
