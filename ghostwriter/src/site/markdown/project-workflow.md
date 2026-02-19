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
gw
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
  gw
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

## Default Guidance

Ghostwriter supports a **default guidance** feature, which provides fallback instructions for files that do not contain embedded `@guidance` directives. This ensures that every file can be processed with meaningful instructions, even if explicit guidance is missing.

### How Default Guidance Works

- If a file lacks an `@guidance` comment, Ghostwriter automatically applies the default guidance content.
- The default guidance can be specified as:
  - Plain text instructions
  - A file reference (e.g., `file:default-guidance.txt`)
  - A URL (e.g., `https://example.com/guidance.md`)

### Sensitivity to `scanDir`

- **If `scanDir` is a folder:**  
  Default guidance will be applied only to that folder (not to individual files within it).
- **If `scanDir` is a pattern (e.g., glob or regex):**  
  Default guidance will be applied to all files that match the pattern.

This allows you to control the scope of default guidance:
- Use a folder as `scanDir` to apply guidance at the directory level.
- Use a pattern as `scanDir` to apply guidance to each matched file.

### Setting Default Guidance

You can set the default guidance using the `--guidance` option:

```sh
java -jar gw.jar src/main/java --guidance="file:default-guidance.txt"
```

Or with plain text:

```sh
java -jar gw.jar src/main/java --guidance="Please review and document this file according to project standards."
```

Or from a URL:

```sh
java -jar gw.jar src/main/java --guidance="https://example.com/guidance.md"
```

If your project does not contain embedded `@guidance` tags in its files, you can leverage Ghostwriter’s **default guidance** feature as a starting point. This approach allows you to begin generating documentation and prompts without modifying your source files.

### Why Use Default Guidance?

- **Quick Start:**  
  Default guidance enables you to use Ghostwriter immediately, even if your project hasn’t adopted embedded `@guidance` comments yet.
- **Incremental Adoption:**  
  As your project evolves, you can gradually add embedded `@guidance` tags for more granular control, while default guidance continues to cover files without them.

### Recommended Workflow

1. **Set up default guidance** to cover your project’s documentation needs.
2. **Run Ghostwriter** to generate documentation for all files, even those without embedded guidance.
3. **Review the output** and identify files or modules that require more specific instructions.
4. **Add embedded `@guidance` tags** to those files for tailored documentation prompts.

Using default guidance is an effective first step to integrate Ghostwriter into your project, providing immediate value and a path toward more advanced, file-specific documentation workflows.

### Best Practices

- Use default guidance to enforce consistent documentation standards across your project.
- Store reusable guidance templates in a file or at a URL for easy updates and sharing.
- Combine default guidance with project-specific variables and templates for maximum flexibility.

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
