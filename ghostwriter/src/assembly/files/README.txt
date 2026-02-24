Ghostwriter CLI (Machai)

1) Application Overview

Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans a repository (source code, docs, project-site Markdown, build metadata, and other artifacts), extracts embedded “@guidance:” directives, and uses a configured GenAI provider to apply consistent improvements across many files in a repeatable, reviewable, CI-friendly way.

Typical use cases
- Repository-wide documentation updates and consistency fixes
- Convention enforcement and repeatable refactors across many files
- Batch improvements driven by per-file, version-controlled guidance

Key features
- Processes many project file types (not just Java), including documentation and project-site Markdown
- Extracts embedded “@guidance:” directives via pluggable, file-type-aware reviewers
- Supports scan targets as a directory, “glob:” matcher, or “regex:” matcher
- Maven multi-module traversal (child modules first)
- Optional multi-threaded module processing (when the provider is thread-safe)
- Optional logging of composed LLM request inputs
- Supports global instructions and default guidance loaded from plain text, URLs, or local files

Supported GenAI providers
- CodeMie (example configuration is provided in gw.properties)
- OpenAI-compatible services (via OPENAI_API_KEY and optional OPENAI_BASE_URL)


2) Installation Instructions

Prerequisites
- Java
  - Build target: Java 8 (from pom.xml: maven.compiler.release=8)
  - Runtime: a newer JRE may be used if required by the selected provider SDK while still building at release 8
- GenAI provider access and credentials (provider-dependent)
  - Common sources: gw.properties, environment variables, or Java “-D” system properties
- Network access to the provider endpoint (if applicable)

Obtain the CLI
- Download the Ghostwriter CLI distribution (contains gw.jar, scripts, and gw.properties):
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Or build from source
- Build the Maven project to produce the runnable jar (gw.jar) and package scripts/resources.
  (Exact build steps depend on your Maven setup and packaging configuration.)

Included files in this folder
- gw.bat
  - Windows launcher for gw.jar
  - Demonstrates setting configuration via environment variables or Java “-D” system properties
- gw.sh
  - Unix-like launcher for gw.jar
  - Demonstrates setting configuration via environment variables or Java “-D” system properties
- gw.properties
  - Example configuration for provider selection and common settings


3) How to Run

Basic usage

  java -jar gw.jar <scanTarget> [options]

Scan targets
- A directory path (relative to the project/root directory)
- A “glob:” pattern (example: glob:**/*.java)
- A “regex:” pattern (example: regex:^.*\\/[^\\/]+\\.java$)

Windows examples (.bat)
- Run via the launcher script:

  gw.bat src\main\java

- Run directly with Java:

  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Unix-like examples (.sh)
- Run via the launcher script:

  ./gw.sh src/main/java

- Run directly with Java:

  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l


Configuration properties (from org.machanism.machai.gw.processor.Ghostwriter)

Configuration can be supplied via:
- gw.properties (default configuration file name: gw.properties)
- Java system properties (java -Dname=value ...)
- Environment variables (when supported by your configuration mapping)
- CLI options (override config values where applicable)

Home/config file resolution
The following properties control how Ghostwriter resolves its home and configuration files. 
These properties can be set via environment variables or Java system properties, but cannot be set in the configuration file itself:
- gw.home
  - Description: The home directory used as the base for locating the configuration file.
  - Default: If not set, falls back to `gw.rootDir`; if still not set, uses the current working directory.
  - Usage context: Determines where `gw.properties` (or the selected config file) is loaded from.

- gw.rootDir
  - Description: The root project directory used by the processor as the repository root.
  - Default: If not set, uses the current working directory.
  - Usage context: Defines the "root" directory for scanning targets and validating absolute scan paths.

- gw.config
  - Description: The name of the configuration file (within `gw.home`) to load.
  - Default: `gw.properties`
  - Usage context: Allows switching between different configuration profiles or files.

GenAI provider/model
- gw.genai
  - Description: GenAI provider/model identifier
  - Default: none (must be configured); documentation examples use values such as OpenAI:gpt-5-mini or OpenAI:gpt-5.1
  - CLI option: -a/--genai <provider:model>
  - Usage context: selects which provider/client/model Ghostwriter uses

Global instructions
- gw.instructions
  - Description: global system instructions appended to every prompt
  - Default: none
  - CLI option: -i/--instructions[=<text|url|file:...>]
  - Usage context: apply repository-wide constraints or policies in addition to per-file @guidance
  - Notes:
    - If -i is used without a value, Ghostwriter reads multi-line text from stdin until EOF
    - Each line is processed as:
      - blank lines preserved
      - http(s)://... fetched and included
      - file:... read and included
      - other lines used as-is

Excludes
- gw.excludes
  - Description: comma-separated list of directories to exclude from processing
  - Default: none
  - CLI option: -e/--excludes <csv>
  - Usage context: prevent scanning common output/vendor folders
  - Example: -e target,.git,node_modules

Default guidance
- gw.guidance
  - Description: fallback guidance used when a file contains no embedded “@guidance:” directives
  - Default: none
  - CLI option: -g/--gw.guidance[=<text|url|file:...>]
  - Usage context: ensure meaningful processing even when files do not contain @guidance
  - Notes:
    - If -g is used without a value, Ghostwriter reads multi-line text from stdin until EOF
    - Line handling supports http(s)://..., file:..., and literal text (blank lines preserved)

Multi-threading
- gw.threads
  - Description: enables multi-threaded module processing
  - Default: false
  - CLI option: -t/--threads[=<true|false>] (if specified without a value, it enables multi-threading)
  - Usage context: faster module processing when the configured provider is thread-safe

Input logging
- gw.logInputs
  - Description: logs composed LLM request inputs to dedicated log files
  - Default: false
  - CLI option: -l/--logInputs
  - Usage context: audit/debug what was sent to the provider

Help
- -h, --help
  - Description: print usage/options/examples and exit


How to set configuration

Using gw.properties
- Edit gw.properties and set values as needed. Examples:
  - genai=CodeMie:gpt-5-2-2025-12-11
  - OPENAI_API_KEY=... (for OpenAI/OpenAI-compatible providers)
  - OPENAI_BASE_URL=https://your-openai-compatible-endpoint

Using environment variables
- Windows (gw.bat example):
  - SET GENAI_USERNAME=your_codemie_username
  - SET GENAI_PASSWORD=your_codemie_password
- Unix-like (gw.sh example):
  - export GENAI_USERNAME=your_codemie_username
  - export GENAI_PASSWORD=your_codemie_password

Using Java system properties
- Example:
  - java -DGENAI_USERNAME=... -DGENAI_PASSWORD=... -jar gw.jar <scanTarget>


4) Troubleshooting & Support

Common issues
- “No GenAI provider/model configured”
  - Set gw.genai in gw.properties or pass -a/--genai (example: -a OpenAI:gpt-5.1)
- Authentication/authorization errors
  - Verify provider credentials (e.g., CodeMie GENAI_USERNAME/GENAI_PASSWORD or OPENAI_API_KEY)
  - If using an OpenAI-compatible endpoint, verify OPENAI_BASE_URL
  - Confirm network access to the provider endpoint
- No files updated / nothing processed
  - Ensure the scan target is correct (directory vs. glob:/regex:)
  - Confirm excluded folders are not filtering out your target content
  - Ensure files contain embedded “@guidance:” directives or provide default guidance with -g
- File/path issues
  - Prefer scan targets relative to the project/root directory
  - For file includes in -i/-g, ensure file: paths resolve correctly from the current execution context

Logs and debug
- Standard logs are written via SLF4J (logging backend depends on runtime configuration)
- Enable input logging for auditing/debugging composed requests:
  - CLI: -l
  - Config: gw.logInputs=true


5) Contact & Documentation

- Official platform: https://machai.machanism.org/ghostwriter/
- Guided File Processing (conceptual foundation): https://www.machanism.org/guided-file-processing/index.html
- GitHub (SCM): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
