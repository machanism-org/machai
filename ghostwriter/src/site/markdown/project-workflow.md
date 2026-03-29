---
canonical: https://machai.machanism.org/ghostwriter/project-processing.html
---

# Project Workflow

## Overview

Project Processing in Ghostwriter enables you to automate the analysis, documentation, and enhancement of entire software projects using GenAI-powered tools. This feature is designed to streamline project-level operations, improve code quality, and facilitate intelligent metadata management across all components of your project.

## Project File Processing

When working with project processing, you have two convenient ways to configure Ghostwriter for project-level processing:

### Set the Project Root in `gw.properties`

For consistent and repeatable processing, specify your project’s root directory in the `gw.properties` file:

```properties
root=/path/to/your/project
```

- Place the `gw.properties` file in your `GW_HOME` directory or alongside `gw.jar`.
- Ghostwriter will automatically use this root directory for all operations, making it easy to run commands from any location.

**Usage Note:**  
If you use the `root` property, you must run `gw` from a folder that is inside the specified root directory.  
If you attempt to run `gw` from a folder outside the root, an error will be thrown and processing will not start.

Ghostwriter will process the current subfolder, but will always treat the path specified by the `root` property as the root of the project.

**Example usage:**

```sh
java -jar /opt/gw/gw.jar
```
*(Run this command from any subfolder within `/path/to/your/project`)*

### Use the Project Directory as the Current Working Directory

Alternatively, you can start Ghostwriter from within your project directory without setting the `root` property:

- Open a terminal and navigate to your project folder:

  ```sh
  cd /path/to/your/project
  ```

- Run Ghostwriter CLI:

  ```sh
  java -jar /opt/gw/gw.jar
  ```

Ghostwriter will treat the current directory as the project root and process all files and subfolders accordingly.

> If you don't use the `root` property, you should run `gw` only in project root folders to ensure correct processing.

**Tip:**  
- If you frequently switch between projects, using the current directory approach is flexible and quick.
- For automation or CI/CD pipelines, setting the `root` property in `gw.properties` ensures consistent behavior.

## Left-over Arguments

When running the Ghostwriter CLI (`gw.jar`), any arguments that are not recognized as options or parameters are treated as **left-over arguments**. 
In our case it is using as a `scanDir` list. These arguments are typically interpreted as paths, patterns, or files to be processed.

**Usage Example:**
```sh
java -jar gw.jar src/main/java src/test/java
```
In this example, `src/main/java` and `src/test/java` are left-over arguments. Ghostwriter will scan and process these directories according to the configured guidance and options.

**How Left-over Arguments Are Used:**
- If you provide one or more left-over arguments, Ghostwriter treats them as input paths or patterns.
- These can be:
  - Relative or absolute directory paths
  - File paths
  - Glob or regex patterns (e.g., `glob:**/*.java`)
- If no left-over arguments are provided, Ghostwriter may default to scanning the current directory or use a configured path.

**Best Practices:**
- Use left-over arguments to specify exactly which files or directories you want Ghostwriter to process.
- Combine left-over arguments with CLI options for fine-grained control.

**Example with Options:**
```sh
java -jar gw.jar --logInputs --instructions=file:instructions.txt src/main/java 
```
Here, `src/main/java` is a left-over argument, and `--logInputs` and `--instructions` are options.

## Additional Instructions

Ghostwriter allows you to provide **additional instructions** that are appended to every prompt sent to the GenAI provider. This feature is useful for enforcing project-wide standards, adding context, or supplying extra guidance for documentation generation.

### How to Specify Additional Instructions

You can provide additional instructions using the `--instructions` option when running the CLI:

```sh
java -jar gw.jar src/main/java --instructions="file:project-instructions.txt"
```

You can also use:
- **Plain text:**  
  ```sh
  java -jar gw.jar src/main/java --instructions="Please ensure all documentation follows the company style guide."
  ```
- **A URL:**  
  ```sh
  java -jar gw.jar src/main/java --instructions="https://example.com/instructions.md"
  ```

### How Additional Instructions Are Processed

- The instructions are parsed line-by-line.
  - Blank lines are preserved as line breaks.
  - Lines starting with `http://` or `https://` are fetched and included.
  - Lines starting with `file:` are read from the specified file and included.
  - All other lines are included as-is.
- The parsed instructions are appended to every GenAI prompt, in addition to any file-specific or default guidance.

### Best Practices

- Use additional instructions to enforce consistent documentation standards or provide important context for all files.
- Store reusable instructions in a file or at a URL for easy updates and sharing.
- Combine additional instructions with default guidance for maximum flexibility and control.
