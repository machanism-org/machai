<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId])` and 
     [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/[artifactId]/bindex.json) in one line after the title as a new paragraph.
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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/gw-maven-plugin/bindex.json)

GW Maven Plugin is the primary Maven adapter for [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html). It brings guided, AI-assisted processing of source code, tests, documentation, site content, configuration, and other project files into Maven-based workflows.

## Introduction

The plugin implements [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): embedded `@guidance` comments describe intended changes, and Ghostwriter scans and updates selected files accordingly. The `gw:gw` goal processes those guidance comments; `gw:act` runs a reusable act or a direct user prompt.

It supplies Ghostwriter with Maven project, reactor, session, and settings context, including optional Java class-discovery and metadata tools. Project-wide goals let Ghostwriter coordinate traversal across a build, while `gw:gw-per-module` and `gw:act-per-module` use Maven's standard reactor scheduling. Provider credentials and configuration can be supplied through Maven `settings.xml` or a Ghostwriter configuration file.

## Key Features

- Guidance-driven maintenance with `gw:gw`.
- Reusable acts and direct prompts with `gw:act`.
- Project-wide and reactor-aware per-module execution.
- Support for source, test, documentation, site, configuration, and other project files.
- Maven settings integration, scan paths, exclusions, instructions, and model configuration.
- Java classpath introspection, usage diagnostics, and parallel aggregator workflows.

## Usage

### Prerequisites

- JDK and Maven installed and available on `PATH`.
- Network access to Maven repositories and the configured GenAI provider.
- A Ghostwriter-compatible provider/model configuration; credentials are preferably stored in Maven `settings.xml`.
- A Maven project for per-module goals. The current `gw:gw` guidance workflow also relies on Maven project context.

The plugin is compiled with `maven.compiler.release=8`, targeting Java 8 bytecode. The JDK running Maven must additionally be supported by Maven, Ghostwriter, the chosen provider, and their dependencies.

### Run guidance processing

Invoke the plugin by its Maven coordinate, replacing `VERSION` with the version to use:

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

Pass an act name, an act name followed by additional prompt text, or a prompt-only value beginning with `>`:

```bash
mvn gw:act -Dgw.act=review
mvn gw:act '-Dgw.act=review Improve the API documentation'
mvn gw:act '-Dgw.act=>Update the project documentation'
```

For Maven reactor scheduling, use `gw:gw-per-module` or `gw:act-per-module`. Project-wide goals can use Maven parallelism when appropriate:

```bash
mvn -T 4 gw:gw
```

### Typical workflow

1. Add the plugin to the build or invoke it by its Maven coordinate.
2. Configure the model and provider credentials, preferably through a Maven `settings.xml` server.
3. Select paths and exclusions, then add guidance comments or select an act.
4. Run the appropriate project-wide or per-module goal and review the generated changes.
5. Build and test the project before committing repeatable guidance and its resulting updates.

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

- [GW Maven Plugin documentation](https://machai.machanism.org/gw-maven-plugin/)
- [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html)
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)
- [GW Maven Plugin on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
