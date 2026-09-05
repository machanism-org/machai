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

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/gw-maven-plugin/bindex.json)

GW Maven Plugin is the Maven adapter for Machai Ghostwriter. It brings guided, AI-assisted maintenance of source code, tests, documentation, site content, configuration, and other project files into Maven workflows.

## Introduction

The plugin implements the [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) approach: embedded `@guidance` comments describe intended changes, and Ghostwriter processes selected files accordingly. The `gw:gw` goal runs guidance-based processing, while `gw:act` runs a reusable act or a direct prompt.

It supplies Maven-aware project, reactor, session, and settings context to Ghostwriter; supports project-wide and per-module execution; and can expose class-discovery and class-metadata tools for Java projects. Provider credentials and configuration can come from Maven `settings.xml` or a Ghostwriter configuration file.

## Key Features

- Guidance-driven processing with `gw:gw`.
- Reusable acts and direct prompts with `gw:act`.
- Project-wide aggregator and reactor-aware per-module goals.
- Maven settings, scan paths, exclusions, instructions, and model configuration.
- Java classpath introspection for class discovery and metadata.
- Parallel processing support for aggregator workflows.

## Usage

### Prerequisites

- JDK and Maven available on `PATH`.
- Network access to Maven repositories and the configured GenAI provider.
- A Ghostwriter-compatible provider/model configuration, preferably with credentials stored in Maven `settings.xml`.
- A Maven project for per-module goals; guidance mode requires project context in the current implementation.

The plugin is compiled for Java 8 (`maven.compiler.release=8`). The JDK used to run Maven must also be supported by Maven, Ghostwriter, the selected provider, and their dependencies.

### Run guidance processing

Invoke the plugin by its Maven coordinate, replacing `VERSION` with the required version:

```bash
mvn org.machanism.machai:gw-maven-plugin:VERSION:gw \
  -Dgw.path=src/site/markdown \
  -Dgw.excludes=target,node_modules
```

If the plugin is configured in the build, use its goal prefix:

```bash
mvn gw:gw -Dgw.path=src
```

### Run an act

Pass a reusable act name, an act with additional prompt text, or a prompt-only value beginning with `>`:

```bash
mvn gw:act -Dgw.act=review
mvn gw:act '-Dgw.act=review Improve the API documentation'
mvn gw:act '-Dgw.act=>Update the project documentation'
```

For Maven reactor scheduling, use `gw:gw-per-module` or `gw:act-per-module`. Project-wide goals can use Maven parallelism when appropriate:

```bash
mvn -T 4 gw:gw
```

## Configuration

| Property | Purpose |
|---|---|
| `gw.model` | Provider/model identifier. |
| `gw.path` | File, directory, glob, or pattern to process. |
| `gw.instructions` | Additional inline instructions or an instruction-file location. |
| `gw.excludes` | Paths or patterns excluded from scanning. |
| `genai.serverId` | Maven `settings.xml` server ID for provider credentials and configuration. |
| `gw.config` | Ghostwriter configuration file when no server ID is used. |
| `gw.act` | Act name, act plus prompt, or a prompt-only value beginning with `>`. |
| `gw.acts` | Directory or URL containing reusable act definitions. |

Enable component-specific debug logging with Maven SimpleLogger:

```bash
mvn -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.maven=DEBUG gw:gw
```

## Resources

- [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html)
- [GW Maven Plugin documentation](https://machai.machanism.org/gw-maven-plugin/)
- [GW Maven Plugin on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
