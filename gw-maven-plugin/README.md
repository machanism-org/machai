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

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

[GW Maven Plugin](https://machai.machanism.org/gw-maven-plugin/index.html) is the primary Maven adapter for the [Machai Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings guided, AI-assisted processing to Maven projects, allowing teams to analyze and maintain source code, tests, documentation, site content, configuration, and other project files. The plugin follows [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): guidance comments describe intended changes, and Ghostwriter applies them to selected files. It also supports reusable acts, direct prompts, Maven-aware project context, Java class-introspection tools, and usage diagnostics.

## Installation

### Prerequisites

- Git for checking out the repository.
- JDK 8 or later and Apache Maven 3.8 or later available on `PATH`.
- Network access to download dependencies and reach the configured Ghostwriter-compatible AI provider.
- Provider/model configuration and credentials. Maven `settings.xml` can hold credentials securely.

The module declares `<maven.compiler.release>8</maven.compiler.release>` in `pom.xml`, so the plugin targets Java 8 bytecode. The runtime JDK may need to be newer when required by Maven, Ghostwriter, the selected provider, or transitive dependencies.

### Build from source

```bash
git clone https://github.com/machanism-org/machai.git
cd machai
git checkout <revision-or-branch>
mvn clean install
```

To build only this module after the repository has been checked out, run the command from its directory:

```bash
cd gw-maven-plugin
mvn clean install
```

## Usage

The plugin provides four goals:

- `gw:gw` — process guidance-tagged files, coordinating the project or reactor.
- `gw:gw-per-module` — process each Maven module through the standard reactor.
- `gw:act` — run a named act or a direct user prompt across the project.
- `gw:act-per-module` — run an act independently in each Maven module.

The aggregator guidance goal can also process a directory without a `pom.xml`; per-module goals require a Maven project. Add the plugin to a build, or invoke it by its fully qualified coordinate:

```bash
mvn org.machanism.machai:gw-maven-plugin:1.3.0-SNAPSHOT:gw
mvn org.machanism.machai:gw-maven-plugin:1.3.0-SNAPSHOT:act -Dgw.act=review
```

When configured with a plugin prefix, the shorter form is available:

```bash
mvn gw:gw -Dgw.path=src -Dgw.excludes=target,node_modules
mvn gw:gw-per-module
mvn gw:act -Dgw.act='>Update the project documentation'
mvn gw:act '-Dgw.act=review Improve the API documentation'
```

For aggregator goals, Maven parallel execution can be used when appropriate:

```bash
mvn -T 4 gw:gw
mvn -T 4 gw:act -Dgw.act=review
```

For a typical workflow, configure the provider, add `@guidance` comments or choose an act, select paths and exclusions, run the appropriate goal, review the generated changes, and then build and test the project. A prompt-only act must begin with `>`; additional prompt text may follow an act name.

## Configuration

Properties can be supplied on the command line or in the plugin's Maven `<configuration>` element.

| Parameter | Description | Default |
|---|---|---|
| `gw.model` / `model` | Provider or model identifier. | Provider or processor default |
| `gw.path` / `path` | File, directory, glob, or supported pattern to scan. | Execution-root or module base directory |
| `gw.instructions` / `instructions` | Additional inline instructions or an instruction-file location. | Unset |
| `gw.excludes` / `excludes` | Paths or patterns to omit from scanning. | Unset |
| `genai.serverId` / `serverId` | `settings.xml` server ID for provider credentials and configuration. | Unset; local Ghostwriter configuration is used |
| `gw.config` / `configFile` | Ghostwriter properties configuration file. | Ghostwriter default location |
| `gw.act` / `act` | Act name, act plus prompt, or a prompt beginning with `>`. | Unset |
| `gw.acts` / `acts` | Directory or URL containing act definitions. | Act processor default |
| `gw.interactive` / `interactive` | Enable or disable interactive prompting. | Processor/configuration default |

A Maven server can provide credentials and provider-specific settings:

```xml
<server>
  <id>my-ai-provider</id>
  <username>provider-user</username>
  <password>provider-secret</password>
  <configuration>
    <AUTH_URL>https://provider.example/auth</AUTH_URL>
  </configuration>
</server>
```

Enable component-level diagnostics with SLF4J SimpleLogger:

```bash
mvn -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.maven=DEBUG gw:gw
mvn -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.processor=DEBUG gw:act -Dgw.act=review
```

Replace the package with a fully qualified class name and use `TRACE`, `DEBUG`, `INFO`, `WARN`, or `ERROR` as needed:

```text
-Dorg.slf4j.simpleLogger.log.[fully-qualified-class-name]=[LEVEL]
```

## Resources

- [GW Maven Plugin site](https://machai.machanism.org/gw-maven-plugin/index.html)
- [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [GW Maven Plugin on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)
- [Maven plugin configuration guide](https://maven.apache.org/guides/mini/guide-configuring-plugins.html)
