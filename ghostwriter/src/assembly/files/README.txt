Ghostwriter CLI (Machai)

1) Application Overview

Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans a repository (source code, documentation, project-site Markdown, build metadata, and other artifacts), extracts embedded "@guidance:" directives, composes a structured prompt per file (including environment constraints and optional global instructions), and submits the request to a configured GenAI provider to apply consistent improvements across many files.

Typical use cases:
- Repository-wide documentation refreshes (project site, READMEs, guides)
- Consistent convention enforcement via version-controlled, per-file guidance
- Repeatable refactors and transformations across many files
- CI-friendly batch processing with auditability (optional request input logging)

Supported GenAI providers (as configured via gw.genai):
- CodeMie
- OpenAI-compatible services (including OpenAI itself and compatible endpoints)


2) Installation Instructions

Prerequisites
- Java:
  - Build target: Java 8
  - Runtime: may be Java 8+ depending on your chosen GenAI provider/client libraries
- GenAI provider access and credentials (varies by provider)
- Network access to the provider endpoint (if applicable)
- Configuration file (recommended): gw.properties

Install / Build
- Download the CLI distribution (includes gw.jar and example configuration):
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

- Or build from source (Maven project):
  - Run Maven to produce the executable jar, then use it as shown below.

Configuration file location
- This folder (src/assembly/files) contains an example gw.properties you can copy next to the gw.jar.
- By default, Ghostwriter looks for gw.properties in the "home" directory (gw.home).

Included files in this folder
- gw.properties
  - Example configuration showing provider selection and credential environment variables.
- README.txt
  - This usage guide.


3) How to Run

Basic usage
  java -jar gw.jar <scanDir> [options]

Scan target (<scanDir>)
- A directory path (relative to the configured project root), OR
- A pattern supported by java.nio.file.FileSystems#getPathMatcher:
  - glob:... (example: "glob:**/*.java")
  - regex:... (example: "regex:^.*/[^/]+\\.java$")

Windows examples (.bat / cmd.exe)
- Scan a folder:
  java -jar gw.jar src\main\java

- Scan using glob:
  java -jar gw.jar "glob:**/*.java" -a OpenAI:gpt-5.1

- Full example (threads, instructions, default guidance, excludes, and input logging):
  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Unix examples (.sh)
- Scan a folder:
  java -jar gw.jar src/main/java

- Scan using glob:
  java -jar gw.jar 'glob:**/*.java' -a OpenAI:gpt-5.1

- Full example:
  java -jar gw.jar 'glob:**/*.java' -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Notes on root vs scan target
- The root directory is the base for relative scan targets.
- If an absolute scan path is provided, it must be located within the root project directory.


Configuration properties (from org.machanism.machai.gw.processor.Ghostwriter)

Ghostwriter reads configuration from a properties file via PropertiesConfigurator, plus supports overriding via CLI options.

Where properties come from
- Properties file: gw.properties (default name)
- Java system properties:
  -Dgw.home=...          (sets the configuration home directory)
  -Dgw.config=...        (sets the config file name or path relative to gw.home)
- Environment variables:
  - Used primarily by provider SDKs (examples are shown in gw.properties)

How to set Java system properties
- Windows:
  java -Dgw.home=C:\path\to\gw-home -Dgw.config=gw.properties -jar gw.jar src\main\java
- Unix:
  java -Dgw.home=/path/to/gw-home -Dgw.config=gw.properties -jar gw.jar src/main/java

Properties
- gw.config
  - Description: Name/path of the properties file to load from gw.home.
  - Default: gw.properties
  - Usage: Set as a Java system property (-Dgw.config=...).

- gw.home
  - Description: Home directory used as the base for loading the configuration file.
  - Default: root directory (if provided), otherwise current working directory.
  - Usage: Set as a Java system property (-Dgw.home=...).

- gw.rootDir
  - Description: Root directory used as the base for relative scan targets and file includes.
  - Default: from config; otherwise current working directory.
  - Usage context: Used by the CLI to resolve scan targets and passed into GuidanceProcessor.

- gw.genai
  - Description: GenAI provider and model identifier.
  - Format: provider:model (example: OpenAI:gpt-5.1)
  - Default: none (required; if missing, the CLI fails)
  - Usage context: Determines which GenAI provider/model is used for processing.

- gw.instructions
  - Description: Global system instructions appended to every prompt.
  - Default: none
  - Usage context: Applied to all files processed in the run.
  - Value rules (line-by-line processing):
    - Blank lines are preserved.
    - Lines starting with http:// or https:// are fetched and included.
    - Lines starting with file: are loaded from the referenced file path.
    - Other lines are included as-is.

- gw.guidance
  - Description: Default guidance used as fallback when a file has no embedded "@guidance:" directives.
  - Default: none
  - Usage context: Provides a project-wide baseline for files without per-file guidance.
  - Value rules (line-by-line processing): same as gw.instructions.

- gw.excludes
  - Description: Comma-separated list of directories to exclude from processing.
  - Default: none
  - Usage context: Prevent scanning and processing of matched directories.

- gw.threads
  - Description: Enable multi-threaded module processing.
  - Default: false
  - Usage context: Improves performance when the provider is thread-safe.

- gw.logInputs
  - Description: Log composed LLM request inputs to dedicated log files.
  - Default: false
  - Usage context: Enables auditability by persisting per-file request inputs.


Command-line options
- -h, --help
  - Show help and exit.

- -r, --root <path>
  - Root directory used as the base for relative scan targets.
  - Default: value from gw.rootDir; otherwise current working directory.

- -t, --threads[=<true|false>]
  - Enable multi-threaded module processing.
  - Default: value from gw.threads; otherwise false.
  - Notes:
    - If provided without a value ("-t"), it enables multi-threading.

- -a, --genai <provider:model>
  - Set GenAI provider and model.
  - Default: value from gw.genai; otherwise required.

- -i, --instructions[=<text|url|file:...>]
  - Provide global system instructions.
  - Default: value from gw.instructions; otherwise none.
  - Notes:
    - If used without a value, Ghostwriter prompts for multi-line text via stdin until EOF.
    - EOF: Ctrl+Z on Windows, Ctrl+D on Unix.

- -g, --guidance[=<text|url|file:...>]
  - Provide default guidance (fallback).
  - Default: value from gw.guidance; otherwise none.
  - Notes:
    - If used without a value, Ghostwriter prompts for multi-line text via stdin until EOF.
    - EOF: Ctrl+Z on Windows, Ctrl+D on Unix.

- -e, --excludes <csv>
  - Comma-separated list of directories to exclude.
  - Default: value from gw.excludes; otherwise none.

- -l, --logInputs
  - Enable logging of composed LLM request inputs.
  - Default: value from gw.logInputs; otherwise false.


4) Troubleshooting & Support

Common issues
- "No GenAI provider/model configured"
  - Fix: set gw.genai in gw.properties or pass -a/--genai.

- Authentication errors (401/403)
  - Fix: ensure provider credentials are set.
  - For CodeMie: set GENAI_USERNAME and GENAI_PASSWORD.
  - For OpenAI-compatible: set OPENAI_API_KEY (and OPENAI_BASE_URL if required).

- Nothing is processed / unexpected files are skipped
  - Fix:
    - verify your <scanDir> (directory, glob:, or regex:)
    - check -e/--excludes and gw.excludes
    - ensure the root (-r/--root or gw.rootDir) is correct

Logs and debug
- Standard logs are emitted via SLF4J (backing implementation depends on the runtime/classpath).
- To troubleshoot request composition, enable input logging:
  - CLI: -l
  - Config: gw.logInputs=true


5) Contact & Documentation

- Official platform: https://machai.machanism.org/ghostwriter/
- GitHub (SCM): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
