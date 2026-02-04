Ghostwriter CLI (Distribution Files)

1) Application Overview

Ghostwriter is an AI-assisted documentation engine that scans a project workspace, extracts embedded "@guidance" instructions from files, and assembles consistent, review-ready documentation. It can be run locally or in CI to keep documentation aligned with the codebase and project requirements.

Typical use cases:
- Generate or update documentation based on in-repo guidance.
- Apply additional run-time instructions (from URL/file/stdin) during documentation processing.
- Standardize documentation output across multiple modules/projects under a single root.

Key features:
- CLI-driven scanning of directories and glob patterns.
- Embedded "@guidance" discovery and application.
- Optional external instructions via URL(s), file path(s), or stdin.
- Optional default guidance applied as a final pass.
- Configurable GenAI provider/model selection.
- Optional multi-threaded processing.
- Directory exclusion support.
- Optional logging of LLM request inputs.

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI when used with OpenAI-compatible settings)


2) Installation Instructions

Prerequisites:
- Java 11+ installed and available on PATH.
- Network access to your chosen GenAI provider endpoint (as required).
- (Optional) A gw.properties file for defaults (included in this distribution).

Install / obtain the CLI:
- Download the Ghostwriter CLI distribution (zip):
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

What’s in this folder:
- gw.jar               (the application JAR; referenced by the scripts)
- gw.properties        (default configuration template)
- gw.bat               (Windows launcher)
- gw.sh                (Unix/macOS launcher)
- g/                   (example instruction templates)
  - g/create_tests     (template instructions for generating unit tests)
  - g/to_java21        (template instructions for migrating Java 17 -> Java 21)

Provider configuration:
- Most configuration is via CLI options and/or gw.properties.
- Credentials are provider-specific. The included gw.properties shows example keys for:
  - CodeMie: GENAI_USERNAME / GENAI_PASSWORD
  - OpenAI-compatible: OPENAI_API_KEY / OPENAI_BASE_URL


3) How to Run

Basic usage (run the JAR directly):

  java -jar gw.jar <scanDir | glob_path_pattern>

Recommended usage (via the scripts in this folder):

Windows (.bat):

  gw.bat <scanDir | glob_path_pattern>

Unix/macOS (.sh):

  ./gw.sh <scanDir | glob_path_pattern>

Examples (Windows):

  REM scan a directory
  gw.bat C:\projects\project

  REM specify root explicitly and scan a subpath
  gw.bat -r C:\projects\project src\project

  REM scan with a glob pattern (quote the pattern)
  gw.bat -r C:\projects\project "**/*.java"

Examples (Unix/macOS):

  # scan a directory
  ./gw.sh /home/me/project

  # specify root explicitly and scan a subpath
  ./gw.sh -r /home/me/project src/project

  # scan with a glob pattern (quote the pattern)
  ./gw.sh -r /home/me/project "**/*.java"

Command-line options (from the project documentation):
- -h, --help
  Show help and exit.

- -l, --logInputs
  Log LLM request inputs to dedicated log files.

- -r, --root <path>
  Root directory used to validate scan targets and compute related paths.
  Default: from gw.properties (root); otherwise the current user directory.

- -t, --threads [true|false]
  Enable/disable multi-threaded processing. If provided without a value, defaults to true.

- -a, --genai <Provider:Model>
  GenAI provider and model selector.
  Default: from gw.properties (genai); otherwise OpenAI:gpt-5-mini

- -i, --instructions [urlOrFile1,urlOrFile2,...]
  Additional instructions sources.
  - Provide a comma-separated list of URLs/file paths, OR
  - pass the option without a value to enter instruction text via stdin.
  Default: from gw.properties (instructions)
  Note: Relative file paths are resolved from the executable directory.

- -g, --guidance [filePath]
  Default guidance applied as a final step for the current directory.
  - Provide a file path, OR
  - pass the option without a value to enter guidance text via stdin.
  Note: Relative file paths are resolved from the executable directory.

- -e, --excludes <name1,name2,...>
  Directories to exclude from processing.
  Default: from gw.properties (excludes)

Using environment variables and Java system properties:
- The launcher scripts (gw.bat / gw.sh) show two common patterns:
  1) Set env vars before running (used by providers/config as applicable):
     - Windows:
       SET GENAI_USERNAME=your_codemie_username
       SET GENAI_PASSWORD=your_codemie_password
     - Unix/macOS:
       export GENAI_USERNAME=your_codemie_username
       export GENAI_PASSWORD=your_codemie_password

  2) Pass values as Java system properties (-D...):
     - Windows:
       java -DGENAI_USERNAME=... -DGENAI_PASSWORD=... -jar gw.jar ...
     - Unix/macOS:
       java -DGENAI_USERNAME=... -DGENAI_PASSWORD=... -jar gw.jar ...

Using included instruction templates:
- The g/ directory contains ready-to-use instruction text files.
- Example (Windows):

  gw.bat -r C:\projects\project "**/*.java" -i g\create_tests

- Example (Unix/macOS):

  ./gw.sh -r /home/me/project "**/*.java" -i g/create_tests

Example: run with custom parameters:

  java -jar gw.jar C:\projects\project \
    -r C:\projects\project \
    -a OpenAI:gpt-5-mini \
    -t true \
    -i https://example.com/instructions.md,local-instructions.md \
    -g default-guidance.md \
    -e target,.git,node_modules \
    -l


4) Troubleshooting & Support

Common issues:
- Authentication/401/403 errors:
  - Verify provider credentials (e.g., GENAI_USERNAME/GENAI_PASSWORD for CodeMie, or OPENAI_API_KEY for OpenAI-compatible).
  - If using an OpenAI-compatible endpoint, confirm OPENAI_BASE_URL is correct.

- Network/timeouts:
  - Confirm outbound network access from your machine/CI environment.
  - Check proxy/VPN settings if applicable.

- “Missing file” for instructions/guidance:
  - Remember: relative paths for -i/--instructions and -g/--guidance are resolved from the executable directory (this folder).
  - Use absolute paths if calling the JAR from elsewhere.

- Unexpected files processed:
  - Use -r/--root to set a stable root, and -e/--excludes to skip directories such as: target,.git,node_modules

Logs and debug:
- Progress and results are logged by the application.
- If you need to capture more detail of what was sent to the provider, enable:
  - -l / --logInputs


5) Contact & Documentation

Further documentation and resources:
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- CLI download: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
