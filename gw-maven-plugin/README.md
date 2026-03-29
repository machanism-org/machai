# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

[GW Maven Plugin](https://machai.machanism.org/gw-maven-plugin/index.html)

## Project Overview

GW Maven Plugin is the primary Maven adapter for the MachAI [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It integrates MachAI Ghostwriter’s [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) approach into Maven builds, enabling documentation (commonly under `src/site`) and other project assets to be scanned, evaluated against embedded `@guidance:` blocks, and updated consistently over time.

At its core, the plugin exposes Maven goals (Mojos) that configure and invoke Ghostwriter processors:

- **Guided processing** using `GuidanceProcessor` (`gw:gw`, `gw:reactor`) for scanning a tree and applying guidance-driven updates.
- **Action processing** using `ActProcessor` (`gw:act`, `gw:act-reactor`) for applying an interactive or predefined “act” prompt across a scanned document set.

Credentials can optionally be sourced from Maven `settings.xml` via `-Dgenai.serverId=...`, keeping secrets out of source control while still enabling CI-friendly execution.

## Installation Instructions

### Prerequisites

- Java installed and available on `PATH`.
  - Build-level requirement (from `pom.xml`): compiled with `maven.compiler.release=8`.
  - Practical runtime requirement: a newer LTS JDK may be needed depending on your GenAI provider and TLS/runtime environment.
- Maven 3.x
- Network access and credentials for your chosen GenAI provider (optionally via Maven `settings.xml`).

### Checkout

```bash
git clone https://github.com/machanism-org/machai.git
cd machai
```

### Build

```bash
mvn -pl gw-maven-plugin -am clean verify
```

## Usage

GW Maven Plugin provides Maven goals (Mojos) that orchestrate Ghostwriter processors:

- `gw:gw`: aggregator goal; processes modules in reverse order (sub-modules first, then parents) and can run without a `pom.xml`.
- `gw:reactor`: processes modules using Maven reactor dependency ordering; can optionally defer execution-root processing.
- `gw:act`: interactive action prompt applied across scanned documents.
- `gw:act-reactor`: reactor-friendly variant of `gw:act` for execution-root processing.
- `gw:clean`: deletes temporary artifacts created during GW processing.

### Examples

Run guided processing:

```bash
mvn gw:gw
```

Run against a specific scan root (for example, Maven Site sources):

```bash
mvn gw:gw -Dgw.scanDir=src\\site
```

Load GenAI credentials from Maven `settings.xml`:

```bash
mvn gw:gw -Dgenai.serverId=my-genai-server
```

Run with a specific provider/model:

```bash
mvn gw:gw -Dgw.model=openai:gpt-4o-mini -Dgw.scanDir=src\\site
```

Apply a one-off action across scanned files:

```bash
mvn gw:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src\\site
```

## Resources

- Project site: https://machai.machanism.org/gw-maven-plugin/index.html
- Ghostwriter: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing (concept): https://www.machanism.org/guided-file-processing/index.html
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- GitHub (SCM): https://github.com/machanism-org/machai

<!--
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Use `src/site/markdown/index.md` as the primary source of information for generating the project description. Summarize and adapt its content as needed for clarity and conciseness.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])` after the title as a new paragraph. [groupId] and [artifactId] need to use from pom.xml.
   - Add a clickable link to the project site: [GW Maven Plugin](https://machai.machanism.org/[artifactId]/index.html).
2. **Installation Instructions:**  
   - Describe how to checkout the repository and build the project using Maven.
   - Include prerequisites such as Java version and build tools.
3. **Usage:**  
   - Explain how to run or use the project and its modules.
   - Provide examples of usage with configuration.
4. **Other Rules:**
   - Do not use the horizontal rule separator between sections.	

**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->
