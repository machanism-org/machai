Ghostwriter CLI (gw.jar)
======================

1) Application Overview
-----------------------
Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans a repository (source code, documentation, project-site content under src/site, build metadata like pom.xml, and other artifacts), extracts embedded “@guidance:” directives, and uses a configured GenAI provider to apply consistent improvements across many files in a repeatable, CI-friendly way.

Typical use cases:
- Repository-wide documentation updates (README/site pages) driven by in-file “@guidance:” rules.
- Systematic refactors or convention enforcement across many files.
- Repeatable batch transformations suitable for scripted or CI runs.

Key features:
- Processes many project file types (not just Java), including documentation and project-site Markdown.
- Extracts embedded “@guidance:” directives via pluggable, file-type-aware reviewers.
- Scan targets can be a directory, glob: pattern, or regex: pattern.
- Maven multi-module traversal.
- Optional multi-threaded processing.
- Optional logging of composed LLM request inputs.
- Supports global system instructions and default guidance loaded from plain text, URLs, or local files.
- Includes an Act mode for executing predefined prompt bundles.

Supported GenAI providers (examples):
- CodeMie (configured in gw.properties by default)
- OpenAI-compatible services (OpenAI or compatible endpoints)


2) Installation Instructions
----------------------------
Prerequisites:
- Java:
  - Build target: Java 8.
  - Runtime: Java 8+ typically works; some provider/client libraries may require a newer JVM.
- GenAI provider access and credentials (provider-specific):
  - CodeMie: set GENAI_USERNAME / GENAI_PASSWORD (typically as environment variables).
  - OpenAI / compatible: set OPENAI_API_KEY (and optionally OPENAI_BASE_URL for compatible endpoints).
- Network access to the provider endpoint (if applicable).

Artifacts in this folder:
- gw.properties
  Default configuration template for selecting the provider/model and (commented) credential hints.
- README.txt
  This document.

Getting the CLI:
- Download the packaged distribution (includes gw.jar and configuration) from:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Or build from source (Maven):
- From the repository root:
  mvn -DskipTests package


3) How to Run
-------------
Basic usage:
  java -jar gw.jar <scanDir> [options]

scanDir forms:
- A directory path (relative to the configured root directory), OR
- A path matcher expression:
  - glob:...  (example: "glob:**/*.java")
  - regex:... (example: "regex:^.*/[^/]+\\.java$")

Setting configuration values
- Java system properties (recommended for gw.home / gw.config):
  -Dgw.home=<dir>   Sets the Ghostwriter “home” directory used to locate gw.properties by default.
  -Dgw.config=<fileNameOrPath>  Overrides the configuration file name/path (resolved under gw.home).

- Configuration file (gw.properties):
  Located by default at: <gw.home>\gw.properties
  Keys below can be set in gw.properties; CLI options override file values.

- Environment variables:
  Provider libraries typically read credentials from environment variables.
  See gw.properties for common variable names.

Command-line options (from org.machanism.machai.gw.processor.Ghostwriter)

Option: -h, --help
- Description: Show help message and exit.
- Default: n/a

Option: -r, --root <path>
- Description: Root directory used as the base for relative scan targets and “file:” includes.
- Default (in code): gw.rootDir from config; otherwise current working directory.
- Config key: gw.rootDir

Option: -a, --genai <provider:model>
- Description: GenAI provider/model identifier (example: OpenAI:gpt-5.1).
- Default (in code): gw.genai from config; otherwise REQUIRED.
- Config key: gw.genai

Option: -t, --threads[=<true|false>]
- Description: Enable multi-threaded processing. If specified without a value, it enables it.
- Default (in code): gw.threads from config; otherwise false.
- Config key: gw.threads

Option: -i, --instructions[=<text|url|file:...>]
- Description: Global system instructions appended to every prompt.
  Input expansion rules (line-by-line):
  - blank lines preserved
  - lines starting with http:// or https:// are fetched and included
  - lines starting with file: are read from disk and included
  - other lines included as-is
  If used without a value, Ghostwriter prompts for multi-line input via stdin until EOF.
- Default (in code): gw.instructions from config; otherwise none.
- Config key: gw.instructions

Option: -g, --guidance[=<text|url|file:...>]
- Description: Default guidance used when a file has no embedded “@guidance:” directives.
  Supports the same input expansion rules as --instructions.
  If used without a value, Ghostwriter prompts for multi-line input via stdin until EOF.
- Default (in code): gw.guidance from config; otherwise none.
- Config key: gw.guidance

Option: -e, --excludes <csv>
- Description: Comma-separated list of directories to exclude from processing.
- Default (in code): gw.excludes from config; otherwise none.
- Config key: gw.excludes

Option: -l, --logInputs
- Description: Log composed LLM request inputs to dedicated log files.
- Default (in code): gw.logInputs from config; otherwise false.
- Config key: gw.logInputs

Option: --act[=<text>]
- Description: Act mode (interactive) for executing predefined prompt bundles.
  If used without a value, Ghostwriter prompts via stdin.
  The action string format is:
    <name> <prompt>
  Where <name> selects a resource bundle under classpath: act/<name>.
- Default: disabled

Examples

A) Windows (PowerShell / cmd.exe)

1) Run using the current folder as root and a simple directory scan:
  java -jar gw.jar src\main\java

2) Scan using a glob matcher, enable threads, set provider/model, add instructions and default guidance,
   exclude common folders, and log inputs:
  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

3) Point Ghostwriter at a specific gw.properties location:
  java -Dgw.home=C:\path\to\gw -jar gw.jar "glob:**/*.md"

4) Enter multi-line instructions via stdin (end with Ctrl+Z then Enter on Windows):
  java -jar gw.jar src\site -i

B) Unix (.sh)

1) Directory scan:
  java -jar gw.jar src/main/java

2) Glob scan with options:
  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

3) Enter multi-line guidance via stdin (end with Ctrl+D on Unix):
  java -jar gw.jar . -g

Notes on root, excludes, and scan targets
- root:
  Use -r/--root to set the base directory for relative scan targets.
- excludes:
  Use -e/--excludes to skip directories (e.g., target,.git,node_modules). Excludes are a comma-separated list.
- scan target:
  Provide a directory, or a glob:/regex: expression. If no scanDir argument is provided, Ghostwriter scans the root directory.


4) Troubleshooting & Support
----------------------------
Common issues:
- “No GenAI provider/model configured”:
  - Set gw.genai in gw.properties or pass -a/--genai.
- Authentication/authorization errors:
  - Ensure the provider credential environment variables are set (see gw.properties).
  - Verify any required base URL (OPENAI_BASE_URL) for OpenAI-compatible providers.
- Nothing changes / no files processed:
  - Confirm your scan target matches files (try a broader target or a glob).
  - Ensure files contain “@guidance:” directives or provide default guidance via -g/--guidance.
  - Check excludes are not filtering out your intended paths.
- File/path issues on Windows:
  - Use quotes around glob/regex patterns.
  - Prefer relative paths under the chosen root directory.

Logs and debug:
- Ghostwriter logs progress and errors via SLF4J.
- If --logInputs is enabled, Ghostwriter writes composed LLM request inputs to dedicated log files (location depends on runtime/config).
- For additional verbosity, enable your logging backend’s DEBUG level for the Ghostwriter packages.


5) Contact & Documentation
--------------------------
Documentation:
- Project site: https://machai.machanism.org/ghostwriter/
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
