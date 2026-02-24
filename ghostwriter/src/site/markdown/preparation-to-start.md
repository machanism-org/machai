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

> The `gw.properties` file is **optional**. It provides default values for configuration settings, but any of these values can be overridden at runtime using Java system properties (e.g., `-Dproperty=value`), environment variables, or command-line options.  
> **By default,** Ghostwriter loads the configuration file from the directory computed as `gwHomeDir` during startup:
>
> - If Java system property `gw.home` is set, it is used as `gwHomeDir`.
> - Otherwise, the CLI `--root/-r` option (or `gw.rootDir`) is used as `gwHomeDir`.
> - If neither is set, it falls back to the current working directory (`user.dir`).
>
> The configuration file name is selected by the Java system property `gw.config` (default: `gw.properties`) and is resolved relative to `gwHomeDir`.
>
> To use a custom configuration file, specify its name or relative path using the Java system property `gw.config`, for example:  
> `java -Dgw.config=custom.properties -jar gw.jar ...`

**Where to place it:**  
Save your `gw.properties` file in the directory selected as `gwHomeDir` (see above).  
Ghostwriter automatically loads this file at startup.

## Configuration Properties Reference

The following properties are read by the `Ghostwriter` CLI bootstrap (`src/main/java/org/machanism/machai/gw/processor/Ghostwriter.java`). They can be supplied via `gw.properties` and/or overridden via Java system properties. Some values can also be set via CLI options.

| Property name | Description | Default value | Usage context |
|---|---|---|---|
| `gw.config` | Selects the configuration file name/path to load at startup. The file is resolved as `new File(gwHomeDir, System.getProperty("gw.config", "gw.properties"))`. | `gw.properties` | Read as a Java system property inside `initializeConfiguration(...)` before scanning begins. |
| `gw.home` | Overrides the computed `gwHomeDir` (the base directory used to resolve the configuration file). The CLI also sets `System.setProperty("gw.home", gwHomeDir.getAbsolutePath())` after it determines the value. | If not set: `gwHomeDir = rootDir`, else `user.dir` when `rootDir` is not provided. | Read in `initializeConfiguration(...)` before the config file is loaded; used to resolve the configuration file location. |
| `gw.rootDir` | Root project directory used to resolve and constrain scanning. | `user.dir` (when not set and not provided via `--root/-r`). | Read before `initializeConfiguration(...)` to compute defaults; passed into `new FileProcessor(rootDir, genai, config)` and used as the base for `processor.scanDocuments(rootDir, scanDir)`. Can be overridden by CLI option `-r/--root`. |
| `gw.genai` | Selects the GenAI provider and model identifier (example: `OpenAI:gpt-5.1`). | No default; required (the CLI throws if blank). | Read in `main(...)` after configuration init; passed into `FileProcessor` constructor. Can be overridden by CLI option `-a/--genai`. |
| `gw.instructions` | System instructions text to apply (may be plain text, URL, or `file:` reference depending on downstream handling). | `null` | Read in `main(...)`; when non-null it is passed to `FileProcessor.setInstructions(instructions)`. Can be overridden by CLI option `-i/--instructions` (if used without a value, the text is read from stdin until EOF). |
| `gw.excludes` | Comma-separated list of directories/patterns to exclude from processing. | `null` | Read and split on commas and then passed to `FileProcessor.setExcludes(excludes)`. Can be overridden by CLI option `-e/--excludes`. |
| `gw.guidance` | Default guidance to apply when a file does not contain embedded `@guidance:` directives. | `null` | Read in `main(...)`; when non-null it is passed to `FileProcessor.setDefaultGuidance(defaultGuidance)`. Can be overridden by CLI option `-g/--guidance` (if used without a value, the text is read from stdin until EOF). |
| `gw.threads` | Enables/disables multi-threaded module processing. | `false` | Read as a boolean and passed to `FileProcessor.setModuleMultiThread(multiThread)`. Can be overridden by CLI option `-t/--threads` (if present without a value, forces `true`; if a value is present, it is parsed as a boolean). |
| `gw.logInputs` | Enables logging of composed LLM request inputs to dedicated log files. | `false` | Read as a boolean and passed to `FileProcessor.setLogInputs(logInputs)`. Can be enabled via CLI flag `-l/--logInputs` (always sets to `true` when present). |

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
C:\projects\machanism.org\machai>gw -h
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
