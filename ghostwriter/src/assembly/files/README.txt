Ghostwriter CLI (Machai)
========================

Application Overview
--------------------
Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans a repository (source code, documentation, project-site Markdown, build metadata, and other artifacts), extracts embedded "@guidance:" directives, composes a structured prompt per file (optionally including global instructions and default guidance), and submits the request to a configured GenAI provider to apply consistent, repeatable improvements across many files.

Typical use cases
- Repository-wide documentation updates (including src\site content)
- Enforcing conventions via version-controlled guidance embedded in files
- Large-scale refactors and formatting/consistency passes across many files
- CI-friendly, repeatable batch processing with auditable changes

Key features
- Processes many project file types (not just Java)
- Extracts embedded "@guidance:" directives via file-type-aware reviewers
- Scan targets can be a directory, a glob matcher (glob:...), or a regex matcher (regex:...)
- Maven multi-module traversal (child modules first)
- Optional multi-threaded module processing (when provider is thread-safe)
- Optional logging of composed LLM request inputs
- Supports global instructions and default guidance loaded from plain text, URLs, or local files

Supported GenAI providers
- CodeMie (example in gw.properties)
- OpenAI-compatible services (via OPENAI_API_KEY and optional OPENAI_BASE_URL)


Installation Instructions
-------------------------
Prerequisites
- Java:
  - Build target: Java 8
  - Runtime: may be newer if required by the chosen provider/client libraries
- GenAI provider credentials and access:
  - CodeMie: GENAI_USERNAME / GENAI_PASSWORD (environment variables)
  - OpenAI-compatible: OPENAI_API_KEY (and optional OPENAI_BASE_URL)
- Network access to the provider endpoint (if applicable)
- Configuration file:
  - gw.properties (provided in this folder)

Install / obtain the CLI
- Download the Ghostwriter CLI bundle:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
- Or build from source (Maven project):
  - Run Maven from the repository root to produce the CLI artifact as documented by the project.

Recommended layout
- Put gw.jar and gw.properties in the same folder.
- Run the CLI with the current working directory set to your project root, or specify -r/--root.

Included files in this folder
- gw.properties
  - Example configuration for selecting provider/model and setting common options
- README.txt
  - This file


How to Run
----------
Basic usage
  java -jar gw.jar <scanDir> [options]

<scanDir> may be:
- A directory path (relative to the configured project root)
- A glob pattern supported by Java PathMatcher, prefixed with "glob:"
  Example: "glob:**/*.java"
- A regex pattern supported by Java PathMatcher, prefixed with "regex:"
  Example: "regex:^.*/[^/]+\\.java$"

Important scanDir rules (from built-in help)
- Relative paths are resolved with respect to the current project directory (or configured root).
- If an absolute path is provided, it must be located within the root project directory.

Windows examples (CMD)
  java -jar gw.jar src\main\java
  java -jar gw.jar "glob:**/*.java"
  java -jar gw.jar "regex:^.*/[^/]+\\.java$"

Unix examples (sh)
  java -jar gw.jar src/main/java
  java -jar gw.jar 'glob:**/*.java'
  java -jar gw.jar 'regex:^.*/[^/]+\\.java$'


Configuration properties (from Ghostwriter.java)
------------------------------------------------
Ghostwriter reads configuration from gw.properties by default, and can be overridden by command-line options.

How configuration is loaded
- gw.home (system property):
  - Defines the "home" directory used to resolve the default gw.properties.
  - If not set, defaults to the root directory passed on the CLI; otherwise the current working directory.
- gw.config (system property):
  - Overrides the configuration filename/path (resolved relative to gw.home).
  - Default is "gw.properties".

Notes on setting values
- Java system properties: pass with -Dkey=value
  Example: -Dgw.home=.
- Environment variables: used by provider clients (examples shown in gw.properties).

Property reference
- gw.genai
  - Description: GenAI provider/model identifier, e.g. OpenAI:gpt-5.1
  - Default: none (must be configured via gw.properties or -a/--genai)
  - Usage: selects the provider and model used for processing

- gw.rootDir
  - Description: root directory used as the base for relative scan targets and file references
  - Default: current working directory (user.dir)
  - Usage: establishes project root for scanning

- gw.instructions
  - Description: optional global system instructions appended to every prompt
  - Default: none
  - Usage: complements per-file @guidance directives
  - Value format:
    - Plain text, or multiple lines (see --instructions)
    - Lines beginning with http:// or https:// are fetched and included
    - Lines beginning with file: are read from the referenced file and included
    - Blank lines are preserved

- gw.guidance
  - Description: default guidance (fallback) used when a file has no embedded @guidance directives
  - Default: none
  - Usage: provides project-wide baseline behavior
  - Value format: same line-by-line rules as gw.instructions

- gw.excludes
  - Description: comma-separated list of directories to exclude from processing
  - Default: none
  - Usage: omit build outputs, VCS folders, generated files, etc.

- gw.threads
  - Description: enable multi-threaded module processing
  - Default: false
  - Usage: improves performance when provider implementation is thread-safe

- gw.logInputs
  - Description: log composed LLM request inputs to dedicated log files
  - Default: false
  - Usage: auditability/debugging; produces per-file request input logs

- gw.home (system property)
  - Description: Ghostwriter home directory used to locate configuration
  - Default: derived from rootDir if provided; otherwise current working directory
  - Usage: allows you to keep gw.properties and related files in a separate folder

- gw.config (system property)
  - Description: configuration file name/path to use instead of gw.properties (resolved under gw.home)
  - Default: gw.properties
  - Usage: switch between config profiles


Command-line options (from Ghostwriter.java help and option definitions)
------------------------------------------------------------------------
- -h, --help
  - Show help message and exit.

- -r, --root <path>
  - Specify the path to the root directory for file processing.
  - Default: from gw.rootDir; otherwise current working directory.

- -t, --threads[=<true|false>]
  - Enable multi-threaded processing to improve performance.
  - If specified without a value, it enables threads.
  - Default: false (or from gw.threads).

- -a, --genai <provider:model>
  - Set the GenAI provider and model.
  - Example: OpenAI:gpt-5.1
  - Default: from gw.genai; otherwise required.

- -i, --instructions[=<text|url|file:...>]
  - Specify system instructions as plain text, by URL, or by file path.
  - If used without a value, you will be prompted to enter multi-line text via stdin.
  - Parsing rules (line-by-line):
    - blank lines preserved
    - http(s)://... lines fetched and included
    - file:... lines read and included
    - other lines used as-is
  - Default: from gw.instructions; otherwise none.

- -g, --guidance[=<text|url|file:...>]
  - Specify default guidance as plain text, by URL, or by file path.
  - If used without a value, you will be prompted to enter multi-line text via stdin.
  - Parsing rules: same as --instructions.
  - Default: from gw.guidance; otherwise none.

- -e, --excludes <csv>
  - Specify a comma-separated list of directories to exclude from processing.
  - Default: from gw.excludes; otherwise none.

- -l, --logInputs
  - Log LLM request inputs to dedicated log files.
  - Default: false (or from gw.logInputs).

- --act[=<text>]
  - Run Ghostwriter in Act mode: an interactive mode for executing predefined prompts.
  - If used without a value, you will be prompted via stdin.


Examples
--------
Windows (use gw.properties in current folder)
  set OPENAI_API_KEY=... 
  java -jar gw.jar "glob:**/*.java" -a OpenAI:gpt-5.1 -e target,.git -l

Windows (use a specific config folder and file)
  java -Dgw.home=C:\path\to\gw-home -Dgw.config=gw.properties -jar gw.jar src\site\markdown -t

Windows (instructions and guidance from files)
  java -jar gw.jar "glob:**/*.md" -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Unix (use env vars)
  export OPENAI_API_KEY=...
  java -jar gw.jar 'glob:**/*.md' -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Interactive multi-line instructions (stdin)
- Windows: end input with Ctrl+Z then Enter
- Unix: end input with Ctrl+D
  java -jar gw.jar src\main\java -a OpenAI:gpt-5.1 -i

Excludes, root, and patterns
  java -jar gw.jar -r . -e target,.git,node_modules "glob:**/*.java"


Troubleshooting & Support
--------------------------
Common issues
- "No GenAI provider/model configured":
  - Set gw.genai in gw.properties, or pass -a/--genai.

- Authentication/authorization failures:
  - Ensure provider credentials are set (e.g., OPENAI_API_KEY or GENAI_USERNAME/GENAI_PASSWORD).
  - Verify the endpoint/base URL if using an OpenAI-compatible service (OPENAI_BASE_URL).

- Files not being processed:
  - Check your scan target (directory vs glob:/regex:).
  - Ensure excluded directories are not filtering out your targets.
  - Ensure the provided absolute scan path is inside the configured root.

- Network errors/timeouts:
  - Verify connectivity to the provider endpoint.
  - Retry with a smaller scan target to isolate problematic requests.

Logs and debug
- Ghostwriter uses SLF4J logging; output typically appears on the console.
- When enabled, -l/--logInputs writes composed request inputs to dedicated log files (location depends on runtime/log configuration).
- To increase verbosity, configure your logging backend (e.g., Logback) as provided by your runtime environment.


Contact & Documentation
------------------------
Documentation and resources
- Project site: https://machai.machanism.org/ghostwriter/
- Guided File Processing concept: https://www.machanism.org/guided-file-processing/index.html
- Source repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
