<!-- @guidance: >>> ${guidances}/readme-content.md -->

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/gw-maven-plugin/refs/heads/main/bindex.json)

GW Maven Plugin integrates [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html) with Maven. It lets Maven projects run guided, AI-assisted processing against source code, tests, documentation, site content, configuration, and other relevant project files.

## Introduction

The plugin is the Maven-facing adapter for Ghostwriter and follows the [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) approach. Guidance comments embedded in files describe repeatable changes, while acts apply a named operation or a direct prompt to selected content.

It provides project-wide and reactor-oriented goals for both workflows. The shared goal infrastructure resolves Maven project, session, settings, reactor, and dependency context; configures provider credentials and options; and exposes Java class-discovery and metadata tools to Ghostwriter processors. This gives AI-assisted processing useful Maven-aware context while keeping provider configuration and credentials outside project content when Maven settings are used.

## Project Structure

The project is a Maven plugin organized around shared goal support, guidance-processing goals, and act-processing goals. Shared infrastructure handles common Maven parameters, provider configuration, scan selection, usage logging, and Java class metadata. Project-wide goals coordinate processing across a project hierarchy, while per-module goals run within Maven's reactor scheduling.

Guidance processing locates embedded instructions and applies requested file updates. Act processing resolves a predefined act or direct prompt before applying it to selected files. Both flows use Ghostwriter's layout detection and configuration services, can consult an external AI provider, and can inspect project classes and resolved compile dependencies for richer Java metadata.

## Usage

Run the plugin by its Maven coordinate, replacing `VERSION` with the version to use:

```bash
mvn org.machanism.machai:gw-maven-plugin:VERSION:gw
mvn org.machanism.machai:gw-maven-plugin:VERSION:act -Dgw.act=review
```

When the plugin is configured in the build, use the short goal names:

```bash
mvn gw:gw -Dgw.path=src -Dgw.excludes=target,node_modules
mvn gw:act -Dgw.act='>Update the project documentation'
```

Use a predefined act name directly, or start a prompt-only act value with `>`:

```bash
mvn gw:act -Dgw.act=review
mvn gw:act '-Dgw.act=review Improve the API documentation'
mvn gw:act '-Dgw.act=>Add missing Javadocs to public classes'
```

For multi-module work, project-wide goals can use Maven parallel execution when appropriate:

```bash
mvn -T 4 gw:gw
mvn -T 4 gw:act
```

Use `gw:gw-per-module` or `gw:act-per-module` when Maven should execute processing module by module in reactor order.

## Configuration

Common command-line properties include:

| Property | Purpose |
|---|---|
| `gw.model` | Selects the provider/model passed to Ghostwriter. |
| `gw.path` | Selects a file, directory, glob, or supported pattern to scan. |
| `gw.instructions` | Supplies additional instructions or an instruction-file location. |
| `gw.excludes` | Specifies comma-separated paths or patterns to skip. |
| `genai.serverId` | Selects a Maven `settings.xml` server containing provider credentials and configuration. |
| `gw.config` | Specifies a Ghostwriter properties file when no server ID is used. |
| `gw.act` | Supplies a predefined act, an act plus additional prompt text, or a prompt-only value beginning with `>`. |
| `gw.acts` | Selects a directory or URL containing act definitions. |

Store provider credentials in Maven `settings.xml` where possible. Enable targeted debug logging with Maven SimpleLogger by replacing the class name and level as needed:

```bash
mvn -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.maven=DEBUG gw:gw
```

The general form is `-Dorg.slf4j.simpleLogger.log.[fully-qualified-class-name]=[LEVEL]`.

## Requirements

The module compiles with Java 8 (`maven.compiler.release` is `8`). Maven, Ghostwriter libraries, the configured AI provider, and transitive dependencies may require a newer JDK at runtime, so use a JDK supported by those components. Maven, network access for dependencies and the selected provider, and a compatible Ghostwriter provider/model configuration are also required.

## Resources

- [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html)
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)
- [Machai documentation](https://machai.machanism.org/)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [GW Maven Plugin on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)
