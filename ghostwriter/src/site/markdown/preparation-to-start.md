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
> **By default,** Ghostwriter will look for `gw.properties` in the directory specified by the `GW_HOME` environment variable. If `GW_HOME` is not defined, Ghostwriter will attempt to find `gw.properties` in the folder where the `gw.jar` file is located.  
> If you want to use a custom configuration file, specify its path using the Java system property `gw.config`, for example:  
> `java -Dgw.config=C:\path\to\custom.properties -jar gw.jar ...`  
> This allows you to adapt Ghostwriter to different environments and workflows without modifying the properties file.

**Where to place it:**  
Save your `gw.properties` file in the directory specified by the `GW_HOME` environment variable.  
Ghostwriter automatically loads this file at startup.  
If `GW_HOME` is not set, place `gw.properties` in the same folder as your `gw.jar` file.  
To use a custom configuration file, provide its path with the `gw.config` system property.

## Configuration Properties Reference

The following properties are read by the `Ghostwriter` CLI bootstrap (`src/main/java/org/machanism/machai/gw/processor/Ghostwriter.java`). They can be supplied via `gw.properties` and/or overridden via Java system properties (for example: `java -Dgw.genai=OpenAI:gpt-5.1 -jar gw.jar ...`). Some values can also be set via CLI options.

| Property name | Description | Default value | Usage context |
|---|---|---|---|
| `gw.config` | Overrides the configuration file name/path loaded at startup. Ghostwriter loads the configuration file from: `<gwHomeDir>\<gw.config>` if set; otherwise `<gwHomeDir>\gw.properties`. | `gw.properties` | Read as a Java system property during static initialization, before scanning begins. |
| `gw.home` | Base “home directory” used to locate `gw.properties` (or the file specified by `gw.config`). | If not set, falls back to `gw.rootdir`; if that is not set, falls back to the current working directory (`user.dir`). | Read during static initialization to compute `gwHomeDir`. |
| `gw.rootdir` | Root project directory used to resolve and constrain scanning. Also used as a fallback to determine `gwHomeDir` when `gw.home` is not set. | If not set, defaults to the current working directory (`user.dir`). | Used to initialize the `FileProcessor` (`new FileProcessor(rootDir, genai, config)`) and as the base for scanning (`processor.scanDocuments(rootDir, scanDir)`). |
| `gw.genai` | Selects the GenAI provider and model identifier (example: `OpenAI:gpt-5.1`). | No default; must be provided (otherwise the CLI throws an error). | Required to run. Can be overridden by CLI option `-a` (long option name is `genai`). |
| `gw.instructions` | System instructions text to apply (may be plain text, URL, or `file:` reference depending on downstream handling). | `null` | Passed into `FileProcessor.setInstructions(instructions)` when present. Can be overridden by CLI option `-i/--instructions` (if specified without a value, instructions are read from stdin until EOF). |
| `gw.excludes` | Comma-separated list of directories to exclude from processing. | `null` | Split on commas and passed into `FileProcessor.setExcludes(excludes)`. Can be overridden by CLI option `-e/--excludes`. |
| `gw.guidance` | Default guidance to apply as a final step for the current directory (may be plain text, URL, or `file:` reference depending on downstream handling). | `null` | Passed into `FileProcessor.setDefaultGuidance(defaultGuidance)` when present. Can be overridden by CLI option `-g` (long option name is `gw.guidance`; if specified without a value, guidance is read from stdin until EOF). |
| `gw.threads` | Enables/disables multi-threaded processing. | `false` | Read as a boolean and passed into `FileProcessor.setModuleMultiThread(multiThread)`. Can be overridden by CLI option `-t/--threads` (if provided without a value, it forces `true`; if a value is provided, it is parsed as a boolean). |
| `gw.logInputs` | Enables logging of LLM request inputs to dedicated log files. | `false` | Read as a boolean and passed into `FileProcessor.setLogInputs(logInputs)`. Can be enabled via CLI flag `-l/--logInputs` (always sets to `true` when present). |

**Sample `gw.properties`:**
```properties
# Root project directory
# (Ghostwriter reads this as gw.rootdir)
gw.rootdir=C:\\projects\\machanism.org\\machai

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
```

### Overriding Settings: Environment Variables and Command-Line Options

Ghostwriter CLI offers flexible configuration management. You can override most settings defined in your `gw.properties` file using **environment variables** or **command-line options**. This is especially useful for automation, temporary changes, or secure handling of sensitive information.

#### **Priority Order**

Settings are applied in the following order (highest to lowest priority):

1. **Command-line options**
2. **Environment variables**
3. **gw.properties file**

If a setting is specified in multiple places, the value from the highest-priority source is used.

#### **Using Environment Variables**

Set environment variables before running Ghostwriter to override configuration properties.

**Examples (Windows PowerShell):**

- **Set the root directory:**
  ```powershell
  $env:gw_rootdir = "C:\projects\machai"
  gw
  ```

- **Set GenAI credentials securely:**
  ```powershell
  $env:GENAI_USERNAME = "your_username"
  $env:GENAI_PASSWORD = "your_password"
  gw
  ```

**Tip:**  
Environment variables are ideal for sensitive information and CI/CD pipelines.

#### **Using Command-Line Options**

Command-line options override both environment variables and `gw.properties` settings for a single run.

**Examples:**

- **Override the root directory:**
  ```text
  gw -Dgw.rootdir=C:\projects\machai
  ```

- **Specify a custom guidance file:**
  ```text
  gw --guidance "C:\projects\guidance.txt"
  ```

- **Override GenAI provider:**
  ```text
  gw --genai "CodeMie:gpt-5-2-2025-12-11"
  ```

**Tip:**  
Use command-line options for temporary or context-specific changes.

#### **Best Practices**

- Prefer environment variables for sensitive data (like credentials).
- Use command-line options for quick overrides or automation scripts.
- For reproducible builds, document your overrides in scripts or CI/CD configuration.
- To view all available command-line options, run:

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
 -e,--excludes <arg>       Specify a list of directories to exclude from
                           processing. You can provide multiple
                           directories by repeating the option.
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
 -t,--threads <arg>        Enable multi-threaded processing to improve
                           performance (default: true).

Examples:
  java -jar gw.jar C:\projects\project
  java -jar gw.jar src\project
  java -jar gw.jar "glob:**/*.java"
  java -jar gw.jar "regex:^.*\/[^\/]+\.java$"
09:59:07.662 INFO  GenAI token usage information not found.
09:59:07.678 INFO  File processing completed.
```

You can flexibly control Ghostwriter’s behavior by setting environment variables or passing command-line options, without modifying your `gw.properties` file. This makes it easy to adapt to different environments, workflows, and security requirements.

**Advanced Configuration:**  
For a complete list of supported properties and detailed explanations, see the [Ghostwriter configuration reference](https://machai.machanism.org/ghostwriter/docs.html#configuration).
