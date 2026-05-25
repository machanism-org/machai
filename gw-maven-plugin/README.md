# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

[GW Maven Plugin](https://machai.machanism.org/gw-maven-plugin/index.html)

## Project Overview

GW Maven Plugin is the primary Maven adapter for the MachAI [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings MachAI Ghostwriter’s [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) approach into Maven so teams can run guided updates, prompt-driven actions, and repeatable maintenance directly from standard build workflows.

The plugin provides Maven goals in `org.machanism.machai.gw.maven` that bridge Maven execution with Ghostwriter processors:

- **Guided processing** with `gw:gw` and `gw:gw-per-module` for scanning project files that contain embedded `@guidance:` instructions and applying targeted updates.
- **Action processing** with `gw:act` and `gw:act-per-module` for running an interactive or predefined act prompt across scanned project content.
- **Cleanup support** with `gw:clean` for removing temporary workflow artifacts generated during processing.

It understands Maven reactor structure, execution-root behavior, scan locations, excludes, optional instruction sources, and provider credentials loaded from Maven `settings.xml` through `genai.serverId`.

## Installation Instructions

### Prerequisites

- Java installed and available on `PATH`.
  - Build-level requirement from `pom.xml`: compiled with `maven.compiler.release=8`.
  - Practical runtime requirement: actual needs can vary depending on the selected GenAI provider, dependency stack, and runtime TLS environment.
- Apache Maven 3.x.
- Network access and credentials for your chosen GenAI provider when running model-backed workflows.
- Optional Maven `settings.xml` server configuration if you want to load provider credentials with `-Dgenai.serverId=...`.

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

GW Maven Plugin provides Maven goals that make Ghostwriter automation part of Maven-based development workflows:

- `gw:gw`: execution-root guided processing that can run without a `pom.xml` in the current directory and processes modules in reverse order so submodules can be handled before parents.
- `gw:gw-per-module`: reactor-oriented guided processing for module-by-module execution.
- `gw:act`: execution-root action processing across scanned documents.
- `gw:act-per-module`: reactor-friendly action processing variant.
- `gw:clean`: removes temporary artifacts created during GW processing.

### Examples

Run guided processing:

```bash
mvn gw:gw
```

Run against a specific scan root such as Maven Site sources:

```bash
mvn gw:gw -Dgw.scanDir=src/site
```

Load GenAI credentials from Maven `settings.xml`:

```bash
mvn gw:gw -Dgenai.serverId=my-genai
```

Run with a specific provider/model:

```bash
mvn gw:gw -Dgw.model=openai:gpt-4o-mini -Dgw.scanDir=src/site
```

Apply a one-off action across scanned files:

```bash
mvn gw:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src/site
```

Run module-oriented guided processing:

```bash
mvn gw:gw-per-module
```

Clean temporary workflow artifacts:

```bash
mvn gw:clean
```

## Resources

- Project site: https://machai.machanism.org/gw-maven-plugin/index.html
- Ghostwriter: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- GitHub repository: https://github.com/machanism-org/machai.git

<!-- @guidance:
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
