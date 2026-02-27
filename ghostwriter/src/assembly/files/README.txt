Ghostwriter CLI
==============

1. Application Overview
-----------------------
Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans your repository (source code, documentation, project-site Markdown, build metadata, and other artifacts), extracts embedded "@guidance:" directives, and uses a configured GenAI provider to apply consistent improvements across many files in a repeatable, reviewable, CI-friendly way.

Typical use cases:
- Repository-wide documentation updates aligned to mandatory guidance blocks.
- Consistent refactors or convention enforcement across many files.
- Batch improvements to project-site content (e.g., src/site Markdown) and other non-code artifacts.

Key features:
- Processes many project file types (not just Java), including documentation and project-site Markdown.
- Extracts embedded "@guidance:" directives via pluggable, file-type-aware reviewers.
- Supports scan targets as a directory, "glob:" matcher, or "regex:" matcher.
- Maven multi-module traversal (child modules first).
- Optional multi-threaded module processing.
- Optional logging of composed LLM request inputs.
- Supports global instructions and default guidance loaded from plain text, URLs, or local files.

Supported GenAI providers (by configuration):
- CodeMie (example in gw.properties)
- OpenAI and OpenAI-compatible services (via OPENAI_API_KEY and optional OPENAI_BASE_URL)


2. Installation Instructions
----------------------------
Prerequisites:
- Java:
  - Build target: Java 8 (maven.compiler.release=8)
  - Runtime: may be newer if required by the selected GenAI provider SDK
- GenAI provider credentials and network access (as applicable)
- Configuration file:
  - gw.properties (included in this folder)

Install / obtain the CLI:
- Download the packaged distribution (contains gw.jar and this files folder), or
- Build from source using Maven and use the resulting CLI jar.

Configuration placement:
- Put gw.properties in the Ghostwriter home directory (GW_HOME). If not set, Ghostwriter uses:
  - the -r/--root value, otherwise
  - the current working directory.

Files in this folder:
- gw.properties
  - Example configuration template for selecting a provider/model and setting provider credentials.
- README.txt
  - This usage guide.


3. How to Run
-------------
Basic usage:

  java -jar gw.jar <scanDir> [options]

Where <scanDir> can be:
- A directory path (relative to the configured root), or
- A path matcher expression supported by Java PathMatcher:
  - glob patterns:  glob:**/*.java
  - regex patterns: regex:^.*/[^/]+\\.java$

If <scanDir> is not provided, Ghostwriter scans the resolved root directory.


3.1 Command-line options
------------------------
From org.machanism.machai.gw.processor.Ghostwriter:

- -h, --help
  Description: Show help and exit.
  Default: n/a
  Context: Use to print usage, supported scan target formats, and examples.

- -r, --root <path>
  Description: Root directory used as the base for relative scan targets and file: includes.
  Default: From config property "gw.rootDir"; otherwise the current working directory.
  Context: Useful when running the CLI from a different folder than the project root.

- -t, --threads[=<true|false>]
  Description: Enable multi-threaded module processing to improve performance.
  Default: From config property "gw.threads" (default false).
  Context: For Maven multi-module repositories; only enable if your provider/client is thread-safe.
  Notes:
  - If specified without a value (-t), it enables threads.
  - If specified with a value (-t false), it disables threads.

- -a, --genai <provider:model>
  Description: GenAI provider/model identifier (example: OpenAI:gpt-5.1).
  Default: From config property "gw.genai"; otherwise required.
  Context: Must be set either in gw.properties or on the command line.

- -i, --instructions[=<text|url|file:...>]
  Description: Global system instructions appended to every prompt.
  Default: From config property "gw.instructions"; otherwise none.
  Context: Use to apply a global policy across all processed files.
  Input handling (line-by-line):
  - Blank lines are preserved.
  - Lines starting with http:// or https:// are fetched and included.
  - Lines starting with file: are read from the referenced file.
  - Other lines are included as-is.
  Stdin mode:
  - If provided without a value (-i), Ghostwriter prompts for multi-line input until EOF.
    - Windows: Ctrl+Z then Enter
    - Unix: Ctrl+D

- -g, --guidance[=<text|url|file:...>]
  Description: Default guidance (fallback) used when a file contains no embedded "@guidance:" directives.
  Default: From config property "gw.guidance"; otherwise none.
  Context: Establishes a project-wide baseline for files that lack embedded guidance.
  Input handling: same as --instructions.
  Stdin mode:
  - If provided without a value (-g), Ghostwriter prompts for multi-line input until EOF.

- -e, --excludes <csv>
  Description: Comma-separated list of directories to exclude from processing.
  Default: From config property "gw.excludes"; otherwise none.
  Context: Common excludes include: target,.git,.idea,node_modules

- -l, --logInputs
  Description: Log composed LLM request inputs to dedicated log files.
  Default: From config property "gw.logInputs" (default false).
  Context: Useful for auditing and troubleshooting.


3.2 Configuration properties (gw.properties / System properties)
---------------------------------------------------------------
Ghostwriter reads configuration via a properties configurator. Relevant keys include:

- gw.config
  Description: Configuration filename to load from GW_HOME.
  Default: gw.properties
  Usage: Set as a Java system property: -Dgw.config=some.properties

- gw.home
  Description: Ghostwriter home directory used to resolve gw.properties.
  Default: If not set, uses --root if provided; otherwise current working directory.
  Usage:
  - Java system property: -Dgw.home=path\\to\\folder

- gw.rootDir
  Description: Root directory used as the base for relative scan targets and file: includes.
  Default: current working directory
  Usage:
  - In gw.properties: gw.rootDir=...
  - Or CLI: --root ... (takes precedence for runtime root resolution)

- gw.genai
  Description: GenAI provider/model identifier.
  Default: none (must be provided by config or -a/--genai)
  Usage:
  - In gw.properties: gw.genai=CodeMie:gpt-5-2-2025-12-11
  - Or CLI: -a OpenAI:gpt-5.1

- gw.instructions
  Description: Global instruction content for all prompts.
  Default: none
  Usage:
  - In gw.properties: gw.instructions=file:instructions.txt
  - Or CLI: -i file:instructions.txt

- gw.guidance
  Description: Default (fallback) guidance content.
  Default: none
  Usage:
  - In gw.properties: gw.guidance=file:guidance.txt
  - Or CLI: -g file:guidance.txt

- gw.excludes
  Description: Comma-separated excludes list.
  Default: none
  Usage:
  - In gw.properties: gw.excludes=target,.git
  - Or CLI: -e target,.git

- gw.threads
  Description: Enable/disable multi-threaded module processing.
  Default: false
  Usage:
  - In gw.properties: gw.threads=true
  - Or CLI: -t  (enables)

- gw.logInputs
  Description: Enable/disable input logging.
  Default: false
  Usage:
  - In gw.properties: gw.logInputs=true
  - Or CLI: -l

Provider credential environment variables (as shown in gw.properties):
- CodeMie:
  - GENAI_USERNAME
  - GENAI_PASSWORD
- OpenAI / OpenAI-compatible:
  - OPENAI_API_KEY
  - OPENAI_BASE_URL (optional; not required for original OpenAI)

Setting configuration values:
- Java system properties:
  - java -Dgw.home=. -Dgw.config=gw.properties -jar gw.jar ...
- Environment variables:
  - Set provider credentials (e.g., OPENAI_API_KEY) via your shell/CI secrets.
- gw.properties:
  - Place in GW_HOME and edit values.


3.3 Examples
------------
Windows (.bat / cmd.exe):

  REM Scan a directory
  java -jar gw.jar src\main\java -a OpenAI:gpt-5.1 -e target,.git

  REM Scan using glob; enable threads; apply instructions and default guidance from files; log inputs
  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

  REM Use gw.properties from a specific home directory
  java -Dgw.home=%CD%\src\assembly\files -jar gw.jar "glob:**/*.md"

Unix (.sh):

  # Scan a directory
  java -jar gw.jar src/main/java -a OpenAI:gpt-5.1 -e target,.git

  # Scan using glob; enable threads; apply instructions and default guidance from files; log inputs
  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

  # Use gw.properties from a specific home directory
  java -Dgw.home=./src/assembly/files -jar gw.jar "glob:**/*.md"


4. Troubleshooting & Support
----------------------------
Common issues:
- "No GenAI provider/model configured":
  - Set gw.genai in gw.properties, or pass -a/--genai.
- Authentication / 401 errors:
  - Verify provider credentials (GENAI_USERNAME/GENAI_PASSWORD or OPENAI_API_KEY).
  - For OpenAI-compatible endpoints, confirm OPENAI_BASE_URL.
- "Missing" guidance or instructions content:
  - If using file:..., confirm the referenced file exists and is readable.
  - Remember that file: paths are resolved relative to the root/home context you run with.
- Nothing is processed:
  - Verify your scan target matches files (especially for glob:/regex: patterns).
  - Reduce excludes and try a narrower scan target first.

Logs and debug:
- Standard logs are written to the console (stderr/stdout depending on your logging setup).
- Use -l/--logInputs or set gw.logInputs=true to persist the composed LLM request inputs per file.
  These logs are intended to help diagnose prompt composition and provider responses.


5. Contact & Documentation
--------------------------
Further documentation and links:
- Official platform: https://machai.machanism.org/ghostwriter/
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central (artifact): https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
