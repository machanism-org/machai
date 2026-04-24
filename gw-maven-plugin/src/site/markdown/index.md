---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.

**VERY IMPORTANT NOTE:** Ghostwriter works with **all types of project files—including source code, documentation, project site content, and other relevant files**.
Ensure that your content generation and documentation efforts consider the full range of file types present in the project.
**GW Maven Plugin is the primary adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html).**  
It serves as the main integration point, enabling Ghostwriter’s features and automation within Maven-based projects.
# Page Structure
1. Header
   - **Project Title:**  
     - Automatically extract the project title from `pom.xml`.
   - **Maven Central Badge:**  
     - Display the Maven Central badge using the following Markdown:  
       `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`
     - Replace `[groupId]` and `[artifactId]` with values from `pom.xml`.
2. Introduction
   - Provide a comprehensive description of the GW Maven plugin, including its purpose and benefits.
   - Analyze java files in the `/src/main/java/org/machanism/machai/gw/maven` to inform the description.
   - Reference [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) as the conceptual foundation for Machai Ghostwriter.
3. Overview
   - Clearly explain the main functions and value proposition of the GW Maven plugin.
   - Summarize how the plugin enhances project workflows and documentation.
4. Key Features
   - Present a bulleted list of the primary capabilities and unique features of the plugin.
5. Getting Started
   - **Prerequisites:**  
     - List all required software, services, and environment settings needed to use the plugin.
   - **Java Version:**  
     Note that the required Java version is defined in `pom.xml`, but actual functional requirements may differ. Clearly state both.
   - **Basic Usage:**  
     - Provide an example command for running the plugin.
   - **Typical Workflow:**  
     - Outline the step-by-step process for using the plugin and its artifacts.
6. Configuration
   - Include a table of common configuration parameters, with columns for parameter name, description, and default value.
   - Ensure descriptions are clear and concise.
7. Resources
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
canonical: https://machai.machanism.org/gw-maven-plugin/index.html
---

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

## Introduction

GW Maven Plugin is the primary adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It integrates MachAI Ghostwriter’s [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) approach into Maven builds so that project assets (source code, documentation, project site content, and other scanned files) can be evaluated against embedded `@guidance:` blocks and updated consistently over time.

The plugin provides Maven goals (Mojos) in `org.machanism.machai.gw.maven` that delegate to Ghostwriter processors:

- **Guided processing** (`gw:gw`, `gw:gw-per-module`) using `GuidanceProcessor` for scanning a directory tree and applying guidance-driven updates.
- **Action processing** (`gw:act`, `gw:act-per-module`) using `ActProcessor` for applying an interactive or predefined “act” prompt across a scanned document set.

Credentials can optionally be sourced from Maven `settings.xml` via `-Dgenai.serverId=...`, keeping secrets out of source control while still enabling CI-friendly execution.

## Overview

The GW Maven Plugin makes Ghostwriter execution repeatable and Maven-native:

- Run documentation and content automation in **local development** and **CI** using standard Maven invocations.
- Choose between **aggregator-style processing** (CLI-like module discovery and reverse module order) and **reactor ordering** (standard Maven dependency build order).
- Configure scan roots, instructions, exclusions, model/provider selection, and logging through **system properties** (`-D...`) or plugin `<configuration>`.
- Use **guided** automation for repeatable, embedded rules, or **acts** for one-off, prompt-driven rewrites.

In practice, you point the plugin at a scan root (commonly `src/site`, but any folder can be scanned), it discovers files, applies guidance or actions, and writes updates back to disk.

## Key Features

- **Guided File Processing integration** via Ghostwriter processors.
- **Guided goals**:
  - `gw:gw`: aggregator goal that can run without a `pom.xml` and processes modules in reverse order (sub-modules first, then parents), similar to the Ghostwriter CLI.
  - `gw:gw-per-module`: processes projects using standard Maven reactor dependency ordering.
- **Action goals**:
  - `gw:act`: applies an action prompt; prompts interactively if `gw.act` is not provided.
  - `gw:act-per-module`: reactor-friendly variant intended to run in the execution-root project context.
- **Configurable scan root, instructions, and exclusions** to focus processing on specific trees (for example documentation sources).
- **Credential integration with Maven settings** via `-Dgenai.serverId=...` (forwards server credentials and optional extra fields to the workflow).
- **Optional input logging** via `-DlogInputs=true`.
- **Cleanup support** via `gw:clean` to remove temporary artifacts created during processing.

## Getting Started

### Prerequisites

- **Java** (see version notes below).
- **Apache Maven**.
- **A MachAI Ghostwriter-compatible GenAI provider** configured via `gw.model`.
- **Credentials (optional, but typical)** stored in `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>my-genai</id>
      <username>...</username>
      <password>...</password>
    </server>
  </servers>
</settings>
```

Use them at runtime:

```bash
mvn gw:gw -Dgenai.serverId=my-genai
```

### Java Version

- **Build/toolchain requirement (from `pom.xml`):** Java **8** (`maven.compiler.release=8`).
- **Functional/runtime requirements:** The plugin runs inside Maven, so you must also use a Java runtime compatible with your Maven version and the resolved MachAI Ghostwriter/GenAI provider stack. In practice, this may require a newer runtime even if the plugin itself is compiled for Java 8.

### Basic Usage

Run guided processing:

```bash
mvn gw:gw
```

Scan a specific directory:

```bash
mvn gw:gw -Dgw.scanDir=src\\site
```

Run with a specific provider/model:

```bash
mvn gw:gw -Dgw.model=openai:gpt-4o-mini -Dgw.scanDir=src\\site
```

Run a one-off action prompt across your documentation tree:

```bash
mvn gw:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src\\site
```

### Typical Workflow

1. **Add or update project content** (commonly documentation under `src/site`, but any scanned tree is supported).
2. **Embed `@guidance:` blocks** in files where you want repeatable automation.
3. **Run a goal**:
   - Use `gw:gw` for aggregator-style processing (reverse module order, CLI-like behavior).
   - Use `gw:gw-per-module` for reactor dependency ordering.
   - Use `gw:act` / `gw:act-per-module` for targeted prompt-driven rewrites.
4. **Review changes** in your VCS diff.
5. **Commit updates** to keep documentation and other assets synchronized with code.
6. (Optional) **Clean temporary artifacts**:

```bash
mvn gw:clean
```

## Configuration

Common configuration parameters (usable as `-D...` system properties or via `<configuration>` in your `pom.xml`):

| Parameter | Description | Default |
|---|---|---|
| `gw.model` | Provider/model identifier forwarded to Ghostwriter (example: `openai:gpt-4o-mini`). | *(none)* |
| `gw.scanDir` | Scan root directory to process. If not provided, defaults to the execution root directory for `gw:gw` and typically the module base directory for reactor-style usage. | *(varies by goal)* |
| `gw.instructions` | Instruction locations consumed by the workflow (for example, file paths or classpath locations). | *(none)* |
| `gw.excludes` | Exclude patterns/paths to skip while scanning. | *(none)* |
| `genai.serverId` | `settings.xml` `<server>` id used to load GenAI credentials (and optional provider-specific fields) into workflow properties. | *(none)* |
| `logInputs` | Logs the list of input files passed to the workflow. | `false` |
| `gw.act` | For `gw:act` / `gw:act-per-module`: the action prompt to apply (interactive if omitted for `gw:act`). | *(none)* |
| `gw.acts` | For `gw:act` / `gw:act-per-module`: directory containing predefined action definitions. | *(none)* |

## Resources

- Official documentation site: https://machai.machanism.org/
- Guided File Processing (concept): https://www.machanism.org/guided-file-processing/index.html
- Ghostwriter documentation: https://machai.machanism.org/ghostwriter/index.html
- Source repository (SCM): https://github.com/machanism-org/machai.git
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
