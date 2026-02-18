---
canonical: http://machai.machanism.org/gw-maven-plugin/project-processing.html
---

# Project Processing

The **GW Maven Plugin** enables automated documentation and code review for your Maven projects using GenAI and customizable guidance. This guide explains how the plugin processes your project, how to control its behavior, and best practices for effective usage.

## How Project Processing Works

When you run the GW Maven Plugin, it performs the following steps:

1. **Project Scanning:**  
   The plugin scans your project directory (or specified subdirectories/patterns) for source files, documentation, and modules.

2. **Guidance Extraction:**  
   For each file, the plugin looks for embedded `@guidance` tags. If found, these tags provide file-specific instructions for GenAI.

3. **Default Guidance Application:**  
   If a file does not contain an embedded `@guidance` tag, the plugin applies the default guidance (if specified via the `--guidance` option or property).

4. **Prompt Construction:**  
   The plugin constructs a prompt for GenAI, combining:
   - Project structure and metadata
   - Guidance (embedded or default)
   - Any additional instructions (from the `--instructions` option or property)

5. **GenAI Processing:**  
   The constructed prompt is sent to the configured GenAI provider. The response is used to generate or update documentation, code comments, or other artifacts.

6. **Logging and Output:**  
   Optionally, the plugin logs all inputs and outputs for traceability and review.

## Controlling What Gets Processed

You can control which files and directories are processed using the following options:

- **scanDir:**  
  Specify a directory, glob, or regex pattern to limit processing scope.
  ```sh
  mvn org.machanism.machai:gw-maven-plugin:0.0.10:gw -Dgw.scanDir=src/main/java
  mvn org.machanism.machai:gw-maven-plugin:0.0.10:gw -Dgw.scanDir="glob:**/*.java"
  ```

- **Excludes:**  
  Exclude specific files or directories using the `-Dgw.excludes` property.
  ```sh
  mvn ... -Dgw.excludes=target,node_modules
  ```

- **Non-Recursive Mode:**  
  Limit processing to the current directory only.
  ```sh
  mvn ... -N
  ```

## Guidance and Instructions

- **Embedded Guidance:**  
  Add `@guidance:` comments directly in your source or documentation files for file-specific instructions.

- **Default Guidance:**  
  Use the `--guidance` option to provide fallback instructions for files without embedded guidance.

- **Additional Instructions:**  
  Use the `--instructions` option to append project-wide instructions to every prompt.

## Multi-Module and Child-First Processing

- The plugin supports both single-module and multi-module Maven projects.
- In multi-module projects, modules are processed in a **child-first** order:  
  Each module is scanned before the parent
 project directory.
- This ensures that module-specific documentation and guidance are handled before aggregating at the parent level.

## Logging and Review

- Enable input logging with the `--logInputs` option to save all prompts and responses for review.
- Logs are stored in a temporary directory under your project root.

## Best Practices

- Start with default guidance to quickly enable documentation for your project.
- Gradually add embedded `@guidance` tags for files or modules that require specific instructions.
- Use patterns and excludes to focus processing on relevant parts of your project.
- Regularly review logs and outputs to ensure quality and compliance.

## Example Command

```sh
mvn org.machanism.machai:gw-maven-plugin:0.0.10:gw \
  -Dgw.scanDir="glob:**/*.java" \
  -Dgw.guidance="file:default-guidance.txt" \
  -Dgw.instructions="file:project-instructions.txt" \
  -Dgw.logInputs=true
```