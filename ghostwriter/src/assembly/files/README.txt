Ghostwriter CLI
==============

1) Application Overview
-----------------------
Ghostwriter is a guidance-driven documentation engine that scans project files, extracts embedded `@guidance` directives, and uses a configured GenAI provider to synthesize and apply updates.

Typical use cases:
- Keep documentation and project artifacts aligned by generating updates directly from file-embedded guidance.
- Process heterogeneous repositories in one run (source code, Markdown, HTML, configuration, and site content).
- Run against a directory, a single file, or a path pattern (supports `glob:` and `regex:`).
- Multi-module projects: module-aware scanning (optionally child-first) and optional parallel module processing.

Key features:
- Multi-format processing via pluggable reviewers (e.g., Java, Markdown, HTML, etc.).
- Guidance-driven generation based on embedded `@guidance` directives.
- Pattern-based scanning with `glob:` and `regex:` path matchers.
- Module-aware scanning for multi-module project layouts.
- Optional multi-threaded module processing (provider must be thread-safe).
- Optional logging of composed LLM inputs per processed file.
- Project-relative path safety (absolute scan paths must be within the root directory).

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI)


2) Installation Instructions
----------------------------
Prerequisites:
- Java 11+
- Network access to your configured GenAI provider (as applicable)
- A project directory containing files with embedded `@guidance` directives (or use `--guidance` as a fallback)

Download:
- Ghostwriter CLI package: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Package contents in this folder:
- gw.bat   : Windows launcher script
- gw.sh    : Unix launcher script
- gw.properties : Default configuration template (provider/model and credentials)

Install / setup:
1. Download and unzip the Ghostwriter CLI package.
2. (Optional) Edit `gw.properties` to select the provider/model and configure credentials.
3. Ensure Java 11+ is available on PATH.


3) How to Run
-------------
Basic usage (direct JAR):

Windows (cmd.exe):
  java -jar gw.jar src

Unix:
  java -jar gw.jar src

Recommended (use the provided launchers):

Windows:
  gw.bat src

Unix:
  ./gw.sh src

Configuration via environment variables (credentials or any property from `gw.properties`):

Windows (cmd.exe):
  SET GENAI_USERNAME=your_codemie_username
  SET GENAI_PASSWORD=your_codemie_password
  gw.bat src

Unix:
  export GENAI_USERNAME=your_codemie_username
  export GENAI_PASSWORD=your_codemie_password
  ./gw.sh src

Configuration via Java system properties (alternative to environment variables):

Windows (cmd.exe):
  java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar src

Unix:
  java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar src

Common CLI options:
- -h, --help
  Show help and exit.

- -r, --root <path>
  Root directory used for scan path validation and project-relative resolution.

- -t, --threads [true|false]
  Enable multi-threaded module processing (only if the provider is thread-safe).

- -a, --genai <provider:model>
  Select GenAI provider and model (e.g., "OpenAI:gpt-5-mini").

- -i, --instructions [text|URL|file:...]
  System instructions appended to each prompt. If provided without a value, Ghostwriter reads multi-line input from stdin until EOF.
  Lines may reference http(s)://... or file:... to include external content.

- -g, --guidance [text|URL|file:...]
  Default (fallback) guidance for files/directories without embedded `@guidance`. If provided without a value, reads from stdin until EOF.

- -e, --excludes <comma-separated list>
  Exclude paths or patterns. Supports exact paths/file names and `glob:`/`regex:` patterns.

- -l, --logInputs
  Log composed LLM request inputs to dedicated log files under the MachAI temp directory.

Command examples:

Windows:
  java -jar gw.jar C:\projects\project
  java -jar gw.jar src\project
  java -jar gw.jar "glob:**/*.java"
  java -jar gw.jar "regex:^.*\\/[^\\/]+\\.java$"

  java -jar gw.jar src -r C:\projects\project -a "OpenAI:gpt-5-mini" -t -e ".machai,target" -l

Unix:
  java -jar gw.jar src/project
  java -jar gw.jar "glob:**/*.java"
  java -jar gw.jar "regex:^.*\\/[^\\/]+\\.java$"

  java -jar gw.jar src -r /path/to/project -a "OpenAI:gpt-5-mini" -t -e ".machai,target" -l

Using excludes (examples):
- Exclude directories/files by name:
  -e ".machai,target"

- Exclude by pattern:
  -e "glob:**/target/**" -e "regex:^.*\\/\.machai\\/.*$"

Using default guidance:
- Provide a fallback instruction set for files without embedded `@guidance` directives.
  This is useful when scanning a directory tree where some files have no directives.


4) Troubleshooting & Support
----------------------------
Common issues:
- Authentication / authorization errors
  - Verify provider credentials are set (environment variables, `gw.properties`, or -D system properties).
  - For OpenAI-compatible providers, confirm API key and (if needed) base URL settings.

- Nothing changes / files skipped
  - Ensure the scan target is correct (file/dir/pattern) and within `--root`.
  - Check `--excludes` patterns are not excluding your files.
  - Ensure files contain embedded `@guidance` directives, or provide `--guidance` as a fallback.

- Pattern matching surprises
  - Use `glob:` for filesystem-style matching (e.g., `glob:**/*.md`).
  - Use `regex:` for advanced matching.

Logs / debug:
- If `--logInputs` is enabled, Ghostwriter writes composed LLM request inputs to dedicated log files under the MachAI temp directory.


5) Contact & Documentation
--------------------------
Documentation and resources:
- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
