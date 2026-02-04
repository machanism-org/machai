GHOSTWRITER CLI (gw)

1) APPLICATION OVERVIEW

Ghostwriter is a CLI documentation engine that scans and processes files in your repository and generates consistent documentation updates driven by embedded guidance tags and AI-powered synthesis.

Typical use cases:
- Keeping project documentation consistent and up to date across large codebases.
- Repeatable, scriptable documentation review/regeneration locally or in CI.
- Applying team instructions and default guidance across directories.

Key features:
- Scans directories and supports path patterns (glob/regex).
- Uses embedded guidance tags to drive deterministic documentation output.
- Configurable GenAI provider/model selection.
- Additional instructions via URL(s), file path(s), or interactively via stdin.
- Optional default guidance applied as a final step per directory.
- Excludes directories from processing.
- Optional multi-threaded processing for faster runs.
- Can log LLM request inputs to dedicated log files for audit/debug.

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI)


2) INSTALLATION INSTRUCTIONS

Prerequisites:
- Java 11 or later
- Network access to your configured GenAI provider
- (Optional) A gw.properties configuration file placed next to the executable (or set -Dgw.config=...)

Installation:
- Download and unzip the Ghostwriter distribution:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Files included in this folder:
- gw.bat        Windows launcher
- gw.sh         Unix/macOS launcher
- gw.properties Default configuration template (provider/model, credentials, root, instructions, excludes)
- g/            Example instruction packs (see below)

Configuration notes:
- By default, Ghostwriter loads configuration from gw.properties in the execution directory.
- To load a different properties file, set:
  -Dgw.config=path/to/your.properties

Example instruction packs (in g/):
- g/create_tests  Prompt for generating high-quality unit tests in the matching test folder structure.
- g/to_java21     Prompt for migrating a codebase from Java 17 to Java 21.


3) HOW TO RUN

A) Run the JAR directly

Basic usage:

  java -jar gw.jar <path | path_pattern>

Examples (Windows):

  java -jar gw.jar C:\projects\project
  java -r C:\projects\project -jar gw.jar src/project
  java -r C:\projects\project -jar gw.jar "glob:**/*.java"
  java -r C:\projects\project -jar gw.jar "regex:^.*\/[^\/]+\.java$"

Examples (Unix/macOS):

  java -jar gw.jar /path/to/project
  java -r /path/to/project -jar gw.jar src/project
  java -r /path/to/project -jar gw.jar 'glob:**/*.md'
  java -r /path/to/project -jar gw.jar 'regex:^.*\/[^\/]+\.java$'

B) Use the launch scripts

Windows:

  gw.bat <path | path_pattern> [options]

Unix/macOS:

  ./gw.sh <path | path_pattern> [options]

C) Configure via environment variables or Java system properties

Environment variables (examples):
- CodeMie:
  - GENAI_USERNAME
  - GENAI_PASSWORD
- OpenAI / OpenAI-compatible services:
  - OPENAI_API_KEY
  - (optional) OPENAI_BASE_URL

Windows (cmd.exe):

  set GENAI_USERNAME=your_codemie_username
  set GENAI_PASSWORD=your_codemie_password
  gw.bat "glob:**/*.md" --root C:\projects\repo

Unix/macOS:

  export GENAI_USERNAME=your_codemie_username
  export GENAI_PASSWORD=your_codemie_password
  ./gw.sh 'glob:**/*.md' --root /path/to/repo

Alternatively, pass them as Java system properties:

Windows:

  java -DGENAI_USERNAME=... -DGENAI_PASSWORD=... -jar gw.jar "glob:**/*.md" --root C:\projects\repo

Unix/macOS:

  java -DGENAI_USERNAME=... -DGENAI_PASSWORD=... -jar gw.jar 'glob:**/*.md' --root /path/to/repo

D) Common options (examples)

- --instructions (URL/file path, comma-separated)
  - If specified without a value, Ghostwriter reads instruction text from stdin (until EOF).
  - Relative file paths are resolved against the execution directory.
- --excludes (comma-separated list of directories to skip)
- --root (root directory used to compute/validate scan targets; targets must be within this root)

Windows example:

  gw.bat "glob:**/*.md" ^
    --root C:\projects\repo ^
    --genai "OpenAI:gpt-5.1" ^
    --instructions "https://example.com/team-guidelines.md,docs/extra-instructions.md" ^
    --guidance docs/default-guidance.md ^
    --excludes "target,node_modules" ^
    --threads=false ^
    --logInputs

Unix/macOS example:

  ./gw.sh 'glob:**/*.md' \
    --root "/path/to/repo" \
    --genai "OpenAI:gpt-5.1" \
    --instructions "https://example.com/team-guidelines.md,docs/extra-instructions.md" \
    --guidance docs/default-guidance.md \
    --excludes "target,node_modules" \
    --threads=false \
    --logInputs


4) TROUBLESHOOTING & SUPPORT

Common issues:
- Authentication failures:
  - Ensure the required credentials are set (e.g., GENAI_USERNAME/GENAI_PASSWORD for CodeMie, or OPENAI_API_KEY for OpenAI).
  - If using an OpenAI-compatible endpoint, verify OPENAI_BASE_URL.
- Instructions/guidance files not found:
  - Relative paths for --instructions/--guidance are resolved against the execution directory.
  - Ensure you run from the directory containing the referenced files, or use absolute/fully qualified paths.
- Scan target rejected:
  - Targets must be within --root (or within user.dir if --root is not set).

Logs / debug:
- Use --logInputs to log LLM request inputs to dedicated log files for audit/debug.
- Review console output and any generated log files in the execution directory.


5) CONTACT & DOCUMENTATION

Resources:
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
