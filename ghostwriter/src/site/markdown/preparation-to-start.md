---
canonical: https://machai.machanism.org/ghostwriter/preparation-to-start.html
---

<!-- @guidance:  
Analyze the `src/main/java/org/machanism/machai/gw/processor/Ghostwriter.java` class to extract and document all available configuration properties.  
For each property, provide its name, description, default value (if any), and usage context.
-->

# Preparation to Start

## Prerequisites

Before you begin, ensure your environment meets the following requirements:

- **Java Development Kit (JDK) 8 or higher**  
  [Download JDK](https://adoptopenjdk.net/) or use your system’s package manager.

- **Active subscription to OpenAI or CodeMie API service**  
  You must have a valid and active subscription to either the OpenAI or CodeMie API service to access GenAI features in Ghostwriter.  
  Make sure you have your API credentials ready.

## Installation Steps

### 1. Download the Ghostwriter CLI Bundle

Get the latest Ghostwriter CLI bundle:  
[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### 2. Extract the Bundle

Unpack the downloaded archive (`gw.zip`) to a dedicated folder of your choice.

### 3. Add the Folder to Your System PATH

Update your system’s PATH environment variable to include the folder where you extracted Ghostwriter CLI.  
This allows you to run the CLI from any directory.

### 4. Set the `GW_HOME` Environment Variable

Create an environment variable named `GW_HOME` and set its value to the path of the extracted folder.

#### Example Setup

**Windows:**
1. Extract `gw.zip` to `C:\machai\ghostwriter`
2. Add `C:\machai\ghostwriter` to your PATH
3. Set `GW_HOME` to `C:\machai\ghostwriter`

**Linux/macOS:**
1. Extract `gw.zip` to `/opt/machai/ghostwriter`
2. Add `/opt/machai/ghostwriter` to your PATH (e.g., in `.bashrc` or `.zshrc`)
3. Set `GW_HOME` to `/opt/machai/ghostwriter`

### Configuration

#### Customizing Ghostwriter with `gw.properties`

Ghostwriter CLI offers flexible configuration using a `gw.properties` file. This file lets you define default options, paths, credentials, and behavior for your Ghostwriter environment.

> The `gw.properties` file is **optional**. It provides default values for configuration settings, but any of these values can be overridden at runtime using Java system properties (e.g., `-Dproperty=value`) or command-line options.
>
> **Configuration file resolution (`gw.home` and `gw.config`):**
>
> - `gw.home` (home directory) is resolved in this order:
>   - Configuration value read by `PropertiesConfigurator#getFile("gw.home", null)` (typically provided via `-Dgw.home=...`).
>   - Otherwise the CLI `--root/-r` option value, if provided.
>   - Otherwise the current working directory (`user.dir`).
> - The config file path is then resolved as:
>   - `new File(gwHomeDir, System.getProperty("gw.config", "gw.properties"))`
>
> To use a custom configuration file name (resolved relative to `gw.home`), set:
> `-Dgw.config=custom.properties`

**Where to place it:**  
Save your `gw.properties` file in the directory selected as `gw.home` (see above).  
Ghostwriter automatically loads this file at startup.

## Configuration Properties Reference

The following properties are read by the Ghostwriter CLI bootstrap (`src/main/java/org/machanism/machai/gw/processor/Ghostwriter.java`). They can be supplied via `gw.properties` and/or overridden via Java system properties. Some values can also be set via CLI options.

| Property name | Description | Default value | Usage context |
|---|---|---|---|
| `gw.config` | Configuration file name (resolved relative to `gw.home`) to load at startup. | `gw.properties` | Read as a Java system property in `initializeConfiguration(...)` when constructing `new File(gwHomeDir, System.getProperty("gw.config", "gw.properties"))`. |
| `gw.home` | Home directory (`gwHomeDir`) used to resolve `gw.config`. During bootstrap Ghostwriter sets `System.setProperty("gw.home", gwHomeDir.getAbsolutePath())` after resolution. | If not set: CLI `--root/-r` value (if provided); else `user.dir`. | Read via `PropertiesConfigurator#getFile("gw.home", null)` in `initializeConfiguration(...)`. Used as the base directory for the config file location. |
| `gw.rootDir` | Root project directory used as the base directory for file processing and to constrain/validate absolute scan paths. | If not provided: `user.dir`. | If the CLI `-r/--root` option is provided, its value is used. Otherwise loaded via `config.getFile("gw.rootDir", null)` and falls back to `SystemUtils.getUserDir()`. Used as the processor root (`processor.getRootDir()`). |
| `gw.genai` | GenAI provider and model identifier (example: `OpenAI:gpt-5.1`). | **No default**; required. | Loaded via `config.get("gw.genai", null)` and optionally overridden by CLI `-a/--genai`. Passed into `new GuidanceProcessor(rootDir, genai, config)` or `new AIFileProcessor(rootDir, config, genai)`. If blank, Ghostwriter throws `IllegalArgumentException`. |
| `gw.instructions` | Optional system instructions input. Supports plain text, URL lines (`http(s)://...`), and `file:` lines. | `null` | Loaded via `config.get("gw.instructions", null)` and optionally overridden by CLI `-i/--instructions`. If `-i` is specified without a value, instructions are read from stdin until EOF. Applied via `AIFileProcessor#setInstructions(...)`. |
| `gw.excludes` | Comma-separated list of directories to exclude from processing. | `null` | Loaded via `config.get("gw.excludes", null)` and split by `,`. Optionally overridden by CLI `-e/--excludes`. Applied via `AIFileProcessor#setExcludes(...)`. |
| `gw.guidance` | Default guidance to apply when files do not contain embedded `@guidance:` directives; also used as the default prompt text in `--act` mode when no text follows the act name. Supports plain text, URL lines (`http(s)://...`), and `file:` lines. | `null` | Loaded via `config.get("gw.guidance", null)` and optionally overridden by CLI `-g/--guidance`. If `-g` is specified without a value, guidance is read from stdin until EOF. In normal mode, applied via `AIFileProcessor#setDefaultGuidance(...)`. In Act mode, used as the default prompt text when formatting `act/<name>.properties` `inputs`. |
| `gw.threads` | Enables/disables multi-threaded module processing. | `false` | Loaded via `config.getBoolean("gw.threads", false)` and optionally overridden by CLI `-t/--threads` (no value forces `true`; otherwise parsed as boolean). Applied via `AIFileProcessor#setModuleMultiThread(...)`. |
| `gw.logInputs` | Enables logging of composed LLM request inputs to dedicated log files. | `false` | Loaded via `config.getBoolean("gw.logInputs", false)` and optionally overridden by CLI `-l/--logInputs` (presence forces `true`). Applied via `AIFileProcessor#setLogInputs(...)`. |

### Act Mode

Ghostwriter also supports **Act mode**, which loads predefined prompts from classpath resource bundles and executes them interactively.

- CLI option: `--act [<name> [prompt...]]`
- Prompt bundle location: `src/main/resources/act/<name>.properties` (packaged as `act/<name>.properties`)

In Act mode, `gw.guidance` is used as the default prompt text if you only supply the act name.

**Example:**

```text
java -jar gw.jar --act refactor "Improve readability and add tests"
```

**Sample `gw.properties`:**

```properties
# GenAI provider and model
# (required)
gw.genai=CodeMie:gpt-5-2-2025-12-11

# Enable logging of input prompts
gw.logInputs=true

# Default instructions and guidance (optional)
gw.instructions=file:C:\\projects\\MDDA-BPD\\instructions.txt
gw.guidance=file:C:\\projects\\MDDA-BPD\\guidance.txt

# Exclude directories (optional)
gw.excludes=target,.git

# Enable multi-threaded processing (optional; default is false)
gw.threads=true

# Override project root directory (optional)
gw.rootDir=C:\\projects\\machai

# Optional: choose a different properties file name (resolved relative to gw.home)
# gw.config=custom.properties

# Optional: override gwHomeDir (base directory used to resolve gw.config)
# gw.home=C:\\machai\\ghostwriter
```

### Overriding Settings: Command-Line Options

Ghostwriter CLI supports overriding several configuration properties via command-line options.

To view all available command-line options, run:

```text
C:\projects\machanism.org\machai>java -jar \opt\gw\gw.jar -h
usage: java -jar gw.jar <scanDir> [options] [-a <arg>] [-e <arg>] [-g
       <arg>] [-h] [-i <arg>] [-l] [-r <arg>] [-t <arg>]

Ghostwriter CLI - Scan and process directories or files using GenAI
guidance.

Usage:
  java -jar gw.jar <scanDir> [options]

  <scanDir> specifies the scanning path or pattern.
    - Use a relative path with respect to the current project directory.
    - If an absolute path is provided, it must be located within the root
project directory.
    - Supported patterns: raw directory names, glob patterns (e.g.,
"glob:**/*.java"), or regex patterns (e.g., "regex:^.*\/[^\/]+\.java$").

Options:
 -a,--genai <arg>          Set the GenAI provider and model (e.g.,
                           'OpenAI:gpt-5.1').
 -e,--excludes <arg>       Specify a comma-separated list of directories
                           to exclude from processing.
 -g,--guidance <arg>       Specify the default guidance as plain text, by
                           URL, or by file path to apply as a final step
                           for the current directory. Each line of input
                           is processed: blank lines are preserved, lines
                           starting with 'http://' or 'https://' are
                           loaded from the specified URL, lines starting
                           with 'file:' are loaded from the specified file
                           path, and other lines are used as-is. To
                           provide the guidance directly, use the option
                           without a value and you will be prompted to
                           enter the guidance text via standard input
                           (stdin).
 -h,--help                 Show this help message and exit.
 -i,--instructions <arg>   Specify system instructions as plain text, by
                           URL, or by file path. Each line of input is
                           processed: blank lines are preserved, lines
                           starting with 'http://' or 'https://' are
                           loaded from the specified URL, lines starting
                           with 'file:' are loaded from the specified file
                           path, and other lines are used as-is. If the
                           option is used without a value, you will be
                           prompted to enter instruction text via standard
                           input (stdin).
 -l,--logInputs            Log LLM request inputs to dedicated log files.
 -r,--root <arg>           Specify the path to the root directory for file
                           processing.
 -t,--threads <arg>        Enable multi-threaded processing to improve
                           performance (default: false).

Examples:
  java -jar gw.jar C:\projects\project
  java -jar gw.jar src\project
  java -jar gw.jar "glob:**/*.java"
  java -jar gw.jar "regex:^.*\/[^\/]+\.java$"
09:59:07.662 INFO  GenAI token usage information not found.
09:59:07.678 INFO  File processing completed.
```

**Advanced Configuration:**  
For a complete list of supported properties and detailed explanations, see the [Ghostwriter configuration reference](https://machai.machanism.org/ghostwriter/docs.html#configuration).
