---
canonical: https://machai.machanism.org/gw-maven-plugin/preparation-to-start.html
---

# Preparation to Start

## Prerequisites

Before you begin, ensure you have the following:

- **Java Development Kit (JDK):**  
  Java 8 or higher (as specified in your project’s `pom.xml`).  
  [Learn more about installing the JDK](https://adoptium.net/).

- **Maven:**  
  Version 3.6.0 or higher is recommended.  
  [Official Maven Installation Guide](https://maven.apache.org/install.html)  
  [Maven Getting Started Guide](https://maven.apache.org/guides/getting-started/)

- **Internet Access:**  
  Required for downloading dependencies and, if using GenAI features, for API access.

- **GW Maven Plugin Artifact:**  
  The plugin should be available in your Maven repository or as a direct JAR download.  
  [Learn more about Maven plugins](https://maven.apache.org/guides/mini/guide-configuring-plugins.html)


## Project Structure

Your project should follow a standard Maven layout:

```
project-root/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
└── ...
```

If you use a **multi-module Maven project**, ensure all modules are properly defined in the parent `pom.xml`:

```
project-root/
├── pom.xml                # Parent POM
├── module-a/
│   └── pom.xml            # Module A POM
├── module-b/
│   └── pom.xml            # Module B POM
└── ...
```

In the parent `pom.xml`, list all modules in the `<modules>` section:

```xml
<modules>
  <module>module-a</module>
  <module>module-b</module>
  <!-- Add additional modules here -->
</modules>
```

This structure allows Maven to build and manage all modules as part of a single project, supporting efficient dependency management and consistent build processes across your codebase.

## Configuration

### Adding the GW Maven Plugin to Your `pom.xml`

To enable Ghostwriter functionality in your Maven project, add the GW Maven Plugin to the `<plugins>` section of your `pom.xml`.  
You can also specify configuration options to customize the plugin’s behavior:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism.machai</groupId>
      <artifactId>gw-maven-plugin</artifactId>
      <version>0.0.11</version>
      <configuration>
        <genai>CodeMie:gpt-5-2-2025-12-11</genai>
        <serverId>CodeMie</serverId>
        <logInputs>true</logInputs>
        <threads>true</threads>
        <scanDir>src/main/java</scanDir>
        <instructions>file:instructions.txt</instructions>
        <guidance>file:guidance.txt</guidance>
        <excludes>
          <exclude>logs</exclude>
        </excludes>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### Configuration

| Parameter / Property | Description | Default |
|---|---|---|
| `gw.genai` | GenAI provider/model identifier passed to Ghostwriter processing. | (none) |
| `gw.scanDir` | Scan root override; when omitted, scans the Maven execution root directory (or the current module base directory when appropriate). | execution root directory |
| `gw.instructions` | Instruction locations (file paths or classpath locations) consumed by the workflow. | (none) |
| `gw.guidance` | Default guidance text forwarded to the workflow. | (none) |
| `gw.excludes` | Exclude patterns/paths to skip while scanning. | (none) |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to load `GENAI_USERNAME`/`GENAI_PASSWORD`. | (none) |
| `gw.logInputs` | If `true`, logs the list of input files passed to the workflow. | `false` |
| `gw.threads` | Enables/disables multi-threaded module processing for `gw:gw`. | `false` |
| `gw.rootProjectLast` | For `gw:reactor`, delays processing of the execution-root project until all other reactor projects complete. | `false` |

**Maven Settings Integration:**
- If you specify `<serverId>`, the plugin will look up credentials in your Maven `settings.xml` under the corresponding `<server>` entry.
- Example `settings.xml` entry:
  ```xml
  <server>
    <id>CodeMie</id>
    <username>your-genai-username</username>
    <password>your-genai-password</password>
  </server>
  ```

**Notes:**
- Make sure to use the latest available version of the plugin. Check [Maven Central](https://search.maven.org/search?q=g:org.machanism.machai%20AND%20a:gw-maven-plugin) for updates.
- For more information on configuring Maven plugins, see the [Maven Plugin Configuration Guide](https://maven.apache.org/guides/mini/guide-configuring-plugins.html).
- For multi-module projects, add the plugin to the parent POM or to the specific module(s) where you want to enable Ghostwriter processing.

### Configure Required Properties

- **GenAI Credentials:**  
  If using GenAI features, set the following environment variables or system properties:
  - `GENAI_USERNAME`
  - `GENAI_PASSWORD`

- **Guidance and Instructions:**  
  Prepare your default guidance and additional instructions files (e.g., `guidance.txt`, `instructions.txt`).

