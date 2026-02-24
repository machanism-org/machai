Ghostwriter CLI - README

1) Application Overview

Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans your repository (source code, docs, project-site Markdown under src\site, build metadata, and other artifacts), extracts embedded "@guidance:" directives, and uses a configured GenAI provider to apply consistent improvements across many files in a repeatable, reviewable, CI-friendly way.

Key features
- Repository-scale scanning with Maven multi-module traversal (child modules first).
- Works across many file types (not just Java): sources, documentation, project-site content, and other project artifacts.
- Guidance-first: uses per-file embedded "@guidance:" directives (plus optional default guidance).
- Scan targets can be:
  - a directory path
  - a PathMatcher pattern: "glob:..." or "regex:..."
- Optional multi-threaded module processing.
- Optional logging of composed LLM request inputs (for auditability).

Typical use cases
- Keeping documentation and project-site content consistent across a repository.
- Applying convention updates or refactors across many files with explicit, version-controlled intent.
- Running repeatable transformations as part of scripted workflows or CI.

Supported GenAI providers
- CodeMie (example configuration included in this folder)
- OpenAI-compatible providers (via OPENAI_API_KEY and optional OPENAI_BASE_URL)
- Additional providers supported by the Machai provider manager (configured via gw.genai)


2) Installation Instructions

Prerequisites
- Java:
  - Build target: Java 8 (maven.compiler.release=8)
  - Runtime: may be newer depending on the chosen provider SDK; Java 8-compatible build
- Network access to your provider endpoint (if applicable)
- Provider credentials and configuration (see gw.properties in this folder)

Artifacts in this folder
- gw.properties
  - Example configuration file for Ghostwriter CLI (provider selection, credentials env vars, and CLI-related properties).

Download / Build
- Download the Ghostwriter CLI distribution:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

- Or build from source (typical Maven build):
  mvn -DskipTests package
  (Then run the produced CLI jar as described below.)

Configuration files and locations
- By default, Ghostwriter looks for a properties file named:
  gw.properties
  in the "home" directory (gw.home).
- You can override the config file name/path (relative to gw.home) via:
  - Java system property: -Dgw.config=<fileNameOrPath>


3) How to Run

Basic syntax
  java -jar gw.jar <scanDir> [options]

Scan target (<scanDir>)
- Directory path (relative to root directory), or
- "glob:<pattern>" (e.g., "glob:**/*.java"), or
- "regex:<pattern>" (PathMatcher style)

Examples (as in built-in help)
- Windows absolute path:
  java -jar gw.jar C:\projects\project
- Relative path:
  java -jar gw.jar src\project
- Glob scan:
  java -jar gw.jar "glob:**/*.java"
- Regex scan:
  java -jar gw.jar "regex:^.*\\/[^\\/]+\\.java$"

Windows (.bat-friendly) examples
- Run with provider/model, glob scan, exclude folders, and log inputs:
  java -jar gw.jar "glob:**/*.java" -a OpenAI:gpt-5.1 -e target,.git -l

- Run with root dir, enable threads, add global instructions and default guidance from files:
  java -jar gw.jar "glob:**/*.md" -r . -t -a CodeMie:gpt-5-2-2025-12-11 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Unix (.sh-friendly) examples
- Run with provider/model and glob scan:
  java -jar gw.jar 'glob:**/*.java' -a OpenAI:gpt-5.1

- Provide instructions via stdin (end with Ctrl+D on Unix):
  java -jar gw.jar src/main/java -a OpenAI:gpt-5.1 -i

How configuration is resolved (properties, env vars, system properties)
- Ghostwriter loads configuration from a properties file using a configurable home directory:
  - gw.home (Java system property or config value)
  - If gw.home is not set:
    - uses -r/--root if provided; otherwise current working directory
  - Ghostwriter sets the system property gw.home to the resolved home directory.

- Properties can come from:
  - gw.properties under gw.home (default), or
  - -Dgw.config=<file> to select a different properties file name/path under gw.home

- Provider credentials are typically supplied via environment variables (see gw.properties):
  - CodeMie:
    - GENAI_USERNAME
    - GENAI_PASSWORD
  - OpenAI-compatible:
    - OPENAI_API_KEY
    - OPENAI_BASE_URL (optional)

Command-line options (from org.machanism.machai.gw.processor.Ghostwriter)
- -h, --help
  - Description: Show help message and exit.
  - Default: n/a

- -r, --root <path>
  - Description: Root directory used as the base for relative paths.
  - Default: from property "gw.rootDir"; otherwise current working directory.
  - Context: also used as default gw.home if gw.home is not set.

- -t, --threads[=<true|false>]
  - Description: Enable multi-threaded module processing to improve performance.
  - Default: from property "gw.threads" (default false).
  - Notes: If specified without a value ("-t"), it enables threads.

- -a, --genai <provider:model>
  - Description: GenAI provider/model identifier (example: "OpenAI:gpt-5.1").
  - Default: from property "gw.genai"; otherwise required.

- -i, --instructions[=<text|url|file:...>]
  - Description: Global system instructions appended to every prompt.
  - Default: from property "gw.instructions"; otherwise none.
  - Value handling (line-by-line):
    - blank lines are preserved
    - http:// or https:// lines are fetched and included
    - file: lines are read from the referenced file path
    - other lines are included as-is
  - Notes: If used without a value ("-i"), Ghostwriter prompts for multi-line text via stdin.

- -g, --guidance[=<text|url|file:...>]
  - Description: Default guidance (fallback) used when files have no embedded "@guidance:" directives.
  - Default: from property "gw.guidance"; otherwise none.
  - Value handling: same rules as --instructions.
  - Notes: If used without a value ("-g"), Ghostwriter prompts for multi-line guidance via stdin.

- -e, --excludes <csv>
  - Description: Comma-separated list of directories to exclude from processing.
  - Default: from property "gw.excludes"; otherwise none.

- -l, --logInputs
  - Description: Log composed LLM request inputs to dedicated log files.
  - Default: from property "gw.logInputs" (default false).

Config properties (Java system property keys / gw.properties keys)
- gw.config
  - Purpose: override the properties file name/path (under gw.home).
  - Default: gw.properties
  - Usage: java -Dgw.config=my-gw.properties -jar gw.jar <scanDir> ...

- gw.home
  - Purpose: base directory used to locate the config file (gw.config) and for relative includes.
  - Default: resolved from --root (if provided), else current working directory.
  - Usage: java -Dgw.home=. -jar gw.jar <scanDir> ...

- gw.rootDir
  - Purpose: project root directory used as the base for relative scan targets.
  - Default: current working directory
  - Usage: set in gw.properties or pass -r/--root.

- gw.genai
  - Purpose: provider/model selection (provider:model).
  - Default: none (required unless provided via -a/--genai)

- gw.instructions
  - Purpose: global instructions.
  - Default: none

- gw.guidance
  - Purpose: default guidance (fallback).
  - Default: none

- gw.excludes
  - Purpose: exclude list (comma-separated).
  - Default: none

- gw.threads
  - Purpose: enable/disable module multi-threading.
  - Default: false

- gw.logInputs
  - Purpose: enable/disable logging of composed LLM inputs.
  - Default: false

Excludes, instructions, and root usage notes
- --root (-r) controls the base directory for relative scan targets and can influence where gw.properties is resolved (via gw.home defaulting).
- --excludes (-e) expects a comma-separated list like: target,.git,node_modules
- --instructions (-i) and --guidance (-g) can be passed as:
  - inline text
  - a URL line (http/https)
  - a file include (file:relative-or-absolute-path; resolved relative to gw.home/root usage)


4) Troubleshooting & Support

Common issues
- "No GenAI provider/model configured"
  - Fix: set gw.genai in gw.properties or pass -a/--genai.

- Authentication / 401 / 403 errors
  - Fix: ensure provider credentials are set (environment variables such as OPENAI_API_KEY, GENAI_USERNAME/GENAI_PASSWORD).
  - For OpenAI-compatible services, verify OPENAI_BASE_URL if required.

- Missing or not loaded properties file
  - Fix: ensure gw.properties exists under gw.home (defaults to --root or current directory).
  - Or explicitly set: -Dgw.home=<dir> and/or -Dgw.config=<file>

- File include issues for --instructions/--guidance
  - Fix: verify file: paths are correct and readable; prefer placing included files under gw.home.

Logs and debug
- Ghostwriter uses SLF4J logging; output is typically printed to the console depending on the logging backend present.
- Enable input logging for audit/debugging:
  - CLI: add -l/--logInputs
  - Config: set gw.logInputs=true


5) Contact & Documentation

Documentation
- https://machai.machanism.org/ghostwriter/
- Conceptual foundation (Guided File Processing):
  https://www.machanism.org/guided-file-processing/index.html

Source repository
- https://github.com/machanism-org/machai

Maven Central
- https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
