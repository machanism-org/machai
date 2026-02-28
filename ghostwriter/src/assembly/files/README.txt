Ghostwriter CLI (Machai)

1) Application Overview

Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans a repository (source code, documentation, project-site content under src/site, build metadata like pom.xml, and other artifacts), extracts embedded “@guidance:” directives, and uses a configured GenAI provider to apply consistent improvements across many files in a repeatable, reviewable, CI-friendly way.

Key features
- Repository-scale processing across many file types (not just Java)
- Guidance-first operation via embedded “@guidance:” directives
- Scan targets support:
  - directory paths
  - glob: patterns (FileSystems path matcher)
  - regex: patterns (FileSystems path matcher)
- Maven multi-module traversal (child modules first)
- Optional multi-threaded module processing
- Optional logging of composed LLM request inputs
- Supports global instructions and default guidance loaded from plain text, URLs, or local files
- Interactive Act mode for executing predefined prompts

Typical use cases
- Keep documentation, conventions, and refactors aligned across an entire repository
- Apply deterministic, version-controlled improvements guided by per-file directives
- Run repeatable batch updates locally or in CI pipelines

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (including OpenAI)


2) Installation Instructions

Prerequisites
- Java:
  - Build target: Java 8
  - Runtime: Java 8+ generally works; some provider/client libraries may require a newer JVM
- GenAI provider access and credentials (as required by your selected provider)
- Network access to the provider endpoint (if applicable)
- A Ghostwriter configuration file (recommended): gw.properties

Getting the application
- Download the Ghostwriter distribution (gw.zip) and extract it.
- Alternatively, build from source using Maven (project is Maven-based).

Configuration files in this folder
- gw.properties
  - Example configuration for selecting provider/model and configuring provider credentials.

Important configuration location notes
- By default, Ghostwriter loads gw.properties from the “home directory”, resolved as:
  - gw.home system property (if set), else
  - the CLI root directory argument (if provided), else
  - the current working directory
- You can override the properties file name/path via system property:
  - -Dgw.config=<fileNameOrPath>


3) How to Run

Basic usage
  java -jar gw.jar <scanDir> [options]

Scan targets (<scanDir>)
- A directory path (relative to the configured root)
- A glob matcher:  glob:**/*.java
- A regex matcher: regex:^.*/[^/]+\.java$

If no <scanDir> is provided, Ghostwriter defaults to the current working directory.

Windows examples (.bat / cmd.exe)
- Scan a directory:
  java -jar gw.jar src\main\java

- Scan by glob (quote to avoid shell expansion issues):
  java -jar gw.jar "glob:**/*.java"

- Full example (glob scan, enable threads, set provider/model, set instructions and default guidance from files, exclude folders, and log inputs):
  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Unix examples (.sh / bash)
- Scan a directory:
  java -jar gw.jar src/main/java

- Scan by glob:
  java -jar gw.jar "glob:**/*.java"

- Full example:
  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l


Configuration properties (from org.machanism.machai.gw.processor.Ghostwriter)

You can configure Ghostwriter using:
- Java system properties:     -D<name>=<value>
- Environment variables:      provider-specific (see gw.properties examples)
- The gw.properties file (recommended)

Core Ghostwriter properties
- gw.config
  - Description: System property that overrides the Ghostwriter configuration file path/name.
  - Default: gw.properties (resolved relative to the home directory)
  - Usage context: Java system property only.
  - Example:
    java -Dgw.config=gw.properties -jar gw.jar src\main\java

- gw.home
  - Description: System property that defines the Ghostwriter home directory used to resolve the configuration file.
  - Default: root directory argument if provided; else current working directory.
  - Usage context: Java system property.
  - Example:
    java -Dgw.home=C:\tools\gw -jar gw.jar src\main\java

- gw.rootDir
  - Description: Root directory used as the base for relative scan targets and file: includes.
  - Default: current working directory.
  - Usage context: gw.properties or via -r/--root.
  - Example (CLI):
    java -jar gw.jar "glob:**/*.md" -r C:\projects\myrepo

- gw.genai
  - Description: GenAI provider/model identifier.
  - Default: none (required).
  - Usage context: gw.properties or via -a/--genai.
  - Example:
    gw.genai=CodeMie:gpt-5-2-2025-12-11

- gw.instructions
  - Description: Optional global system instructions appended to every prompt.
  - Default: none.
  - Usage context: gw.properties or via -i/--instructions.
  - Value handling:
    - blank lines preserved
    - lines beginning with http:// or https:// are fetched and included
    - lines beginning with file: are read from the referenced file and included
    - other lines included as-is
  - Example:
    gw.instructions=file:project-instructions.txt

- gw.guidance
  - Description: Default guidance (fallback) used when files contain no embedded @guidance: directives.
  - Default: none.
  - Usage context: gw.properties or via -g/--guidance.
  - Value handling: same expansion rules as gw.instructions.
  - Example:
    gw.guidance=file:default-guidance.txt

- gw.excludes
  - Description: Comma-separated list of directories to exclude from processing.
  - Default: none.
  - Usage context: gw.properties or via -e/--excludes.
  - Example:
    gw.excludes=target,.git,node_modules

- gw.threads
  - Description: Enables multi-threaded module processing.
  - Default: false.
  - Usage context: gw.properties or via -t/--threads.
  - Example:
    gw.threads=true

- gw.logInputs
  - Description: Enables logging of composed LLM request inputs to dedicated log files.
  - Default: false.
  - Usage context: gw.properties or via -l/--logInputs.
  - Example:
    gw.logInputs=true

Provider credentials (examples from gw.properties)
- CodeMie:
  - GENAI_USERNAME
  - GENAI_PASSWORD
- OpenAI / OpenAI-compatible:
  - OPENAI_API_KEY
  - OPENAI_BASE_URL (optional for OpenAI; required for compatible endpoints)


Command-line options
- -h, --help
  - Show help and exit.

- -r, --root <path>
  - Specify the root directory used as the base for relative paths.
  - Default: gw.rootDir, else current working directory.

- -t, --threads[=<true|false>]
  - Enable multi-threaded processing.
  - If specified with no value, it enables threads.
  - Default: gw.threads (default false).

- -a, --genai <provider:model>
  - Set GenAI provider/model (example: OpenAI:gpt-5.1).
  - Default: gw.genai; otherwise required.

- -i, --instructions[=<text|url|file:...>]
  - Provide global system instructions.
  - If used without a value, prompts for multi-line text via stdin until EOF.

- -g, --guidance[=<text|url|file:...>]
  - Provide default guidance (fallback).
  - If used without a value, prompts for multi-line text via stdin until EOF.

- -e, --excludes <csv>
  - Comma-separated directories/patterns to exclude.

- -l, --logInputs
  - Enable logging of composed LLM request inputs.

- --act[=<text>]
  - Run Act mode (execute predefined prompts).
  - If used without a value, prompts via stdin.

Using stdin for instructions/guidance
- When -i/--instructions or -g/--guidance is provided without a value, Ghostwriter reads multi-line text from stdin until EOF:
  - Windows: Ctrl+Z then Enter
  - Unix: Ctrl+D


4) Troubleshooting & Support

Common issues
- “No GenAI provider/model configured”
  - Set gw.genai in gw.properties, or pass -a/--genai.

- Authentication / authorization failures
  - Verify provider credentials are set (e.g., OPENAI_API_KEY or GENAI_USERNAME/GENAI_PASSWORD).
  - Confirm OPENAI_BASE_URL if using an OpenAI-compatible endpoint.

- No files processed / unexpected scan results
  - Ensure <scanDir> is correct relative to the root directory.
  - For patterns, ensure you are using the required prefixes: glob: or regex:.
  - Check gw.excludes and -e/--excludes for accidentally excluded paths.

- “file:” includes not found
  - file: paths are resolved relative to the configured root/home context. Use absolute paths only when needed and ensure they reside within the root directory.

Logs and debugging
- Ghostwriter uses SLF4J logging.
- If -l/--logInputs (or gw.logInputs=true) is enabled, Ghostwriter writes composed LLM request inputs to dedicated log files (location depends on runtime configuration).
- If available in your environment, configure the logging backend (e.g., logback/log4j properties) to increase verbosity to DEBUG.


5) Contact & Documentation

Documentation
- Project site: https://machai.machanism.org/ghostwriter/
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter

Support
- Use the GitHub repository issues/discussions (if enabled) for questions and bug reports.
