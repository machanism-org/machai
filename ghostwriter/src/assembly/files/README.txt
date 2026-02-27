Ghostwriter CLI (Machai)
=======================

Application Overview
--------------------
Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans a repository (source code, documentation, project-site Markdown, build metadata, and other artifacts), extracts embedded "@guidance:" directives, and uses a configured GenAI provider to apply consistent improvements across many files in a repeatable, reviewable, CI-friendly way.

Typical use cases:
- Keeping documentation consistent across a repository.
- Applying guided refactors and convention enforcement across many files.
- Repository-scale transformations where intent must be explicit and version-controlled.

Key features:
- Processes many project file types (not just Java), including documentation and project-site Markdown.
- Extracts embedded "@guidance:" directives via pluggable, file-type-aware reviewers.
- Supports scan targets as a directory, "glob:" matcher, or "regex:" matcher.
- Maven multi-module traversal (child modules first).
- Optional multi-threaded module processing (when the provider is thread-safe).
- Optional logging of composed LLM request inputs.
- Supports global instructions and default guidance loaded from plain text, URLs, or local files.

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (including OpenAI)


Installation Instructions
-------------------------
Prerequisites
- Java runtime:
  - Build target: Java 8 (the project is compiled with maven.compiler.release=8).
  - You may run with a newer JVM if required by your selected provider/client.
- GenAI provider access and credentials (see gw.properties below).
- Network access to the provider endpoint (if applicable).

Install / Configure
1) Obtain the Ghostwriter CLI distribution (gw.jar and supporting files).
2) Place the configuration file "gw.properties" next to the jar, or set a custom path.
3) Configure your provider/model and credentials.

Files in this folder
- gw.properties
  - Sample configuration file for selecting the provider/model and supplying provider credentials.
- README.txt
  - This file.


How to Run
----------
Basic usage
  java -jar gw.jar <scanDir> [options]

scanDir rules
- May be a directory path (relative to the project root), or a pattern supported by Java PathMatcher:
  - glob patterns:  glob:**/*.java
  - regex patterns: regex:^.*/[^/]+\.java$
- If an absolute path is provided, it must be located within the root project directory.

Windows examples
  REM scan a folder
  java -jar gw.jar src\main\java

  REM scan with a glob
  java -jar gw.jar "glob:**/*.java"

  REM scan with a regex
  java -jar gw.jar "regex:^.*/[^/]+\\.java$"

Unix/macOS examples
  # scan a folder
  java -jar gw.jar src/main/java

  # scan with a glob
  java -jar gw.jar 'glob:**/*.java'

  # scan with a regex
  java -jar gw.jar 'regex:^.*/[^/]+\.java$'


Configuration properties (Java system properties + gw.properties)
---------------------------------------------------------------
Ghostwriter reads configuration from a properties file (default: "gw.properties" in the resolved GW home directory) and from Java system properties.

System properties
- gw.home
  - Description: Ghostwriter home directory used as the base for resolving the default config file.
  - Default: the provided --root value (if any); otherwise the current working directory.
  - Usage:
      java -Dgw.home=C:\path\to\gw -jar gw.jar <scanDir>

- gw.config
  - Description: Overrides the configuration file path/name that will be loaded from gw.home.
  - Default: gw.properties
  - Usage:
      java -Dgw.home=C:\path\to\gw -Dgw.config=gw.properties -jar gw.jar <scanDir>
      java -Dgw.home=C:\path\to\gw -Dgw.config=conf\custom.properties -jar gw.jar <scanDir>

Properties in gw.properties (and/or set by other configurators)
- gw.rootDir
  - Description: Root directory used as the base for relative scan targets and for resolving "file:" includes.
  - Default: current working directory.
  - Context: Used when --root is not provided.

- gw.genai
  - Description: GenAI provider/model identifier.
  - Default: none (required).
  - Context: May be set in gw.properties or via --genai.
  - Example:
      gw.genai=CodeMie:gpt-5-2-2025-12-11
      gw.genai=OpenAI:gpt-5.1

- gw.instructions
  - Description: Optional global system instructions appended to every prompt.
  - Default: none.
  - Context: May be set in gw.properties or via --instructions.
  - Value format: plain text, URL(s), and/or file references. Each line is processed as:
    - blank lines preserved
    - http:// or https:// loaded from URL
    - file: loaded from local file path
    - otherwise used as-is

- gw.guidance
  - Description: Optional default guidance used when a file contains no embedded "@guidance:" directives.
  - Default: none.
  - Context: May be set in gw.properties or via --guidance.
  - Value format: same line-by-line processing rules as gw.instructions.

- gw.excludes
  - Description: Comma-separated list of directories to exclude from processing.
  - Default: none.
  - Context: May be set in gw.properties or via --excludes.

- gw.threads
  - Description: Enables multi-threaded module processing.
  - Default: false.
  - Context: May be set in gw.properties or via --threads.

- gw.logInputs
  - Description: Enables logging of composed LLM request inputs to dedicated log files.
  - Default: false.
  - Context: May be set in gw.properties or via --logInputs.


Command-line options
--------------------
-h, --help
  Show help and exit.

-r, --root <path>
  Root directory for file processing.

-t, --threads[=<true|false>]
  Enable multi-threaded module processing. If provided without a value, it enables it.
  Default: false.

-a, --genai <provider:model>
  Set the GenAI provider and model (e.g., OpenAI:gpt-5.1).

-i, --instructions[=<text|url|file:...>]
  Provide global system instructions.
  If used without a value, Ghostwriter reads multi-line text from stdin until EOF.
  Windows EOF: Ctrl+Z then Enter
  Unix EOF:    Ctrl+D

-g, --guidance[=<text|url|file:...>]
  Provide default guidance (fallback) when no embedded "@guidance:" is present.
  If used without a value, Ghostwriter reads multi-line text from stdin until EOF.

-e, --excludes <csv>
  Comma-separated list of directories to exclude.

-l, --logInputs
  Log composed LLM request inputs to dedicated log files.

Examples
--------
Windows (.bat style)
  REM Scan Java sources via glob, enable threads, set provider/model, add instructions and default guidance,
  REM exclude common folders, and log inputs:
  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Unix (.sh style)
  # Same idea on Unix/macOS
  java -jar gw.jar 'glob:**/*.java' -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Using --root
  java -jar gw.jar -r C:\projects\myrepo src\site
  java -jar gw.jar -r /home/me/myrepo src/site


Troubleshooting & Support
-------------------------
Common issues
- "No GenAI provider/model configured":
  - Set gw.genai in gw.properties, or pass -a/--genai.

- Authentication / authorization errors:
  - Ensure the provider credentials are set.
  - For CodeMie: set GENAI_USERNAME / GENAI_PASSWORD.
  - For OpenAI / compatible providers: set OPENAI_API_KEY (and OPENAI_BASE_URL if required).

- Files not being processed:
  - Confirm the scanDir is correct and within the project root.
  - If using glob/regex, verify the pattern matches expected files.
  - Check gw.excludes / --excludes values.

Logs and debugging
- Application logs are written via SLF4J (logging backend determines output location).
- Enable "-l/--logInputs" (or set gw.logInputs=true) to persist composed LLM request inputs to dedicated log files.


Contact & Documentation
-----------------------
- Official platform: https://machai.machanism.org/ghostwriter/
- Source repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
