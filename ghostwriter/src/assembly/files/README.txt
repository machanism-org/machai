Ghostwriter CLI (gw)
===================

1) Application Overview
-----------------------
Ghostwriter is an advanced documentation engine that runs as a CLI. It scans one or more targets (directories or path patterns) under a chosen root directory and updates/assembles project documentation using embedded guidance tags and AI-powered synthesis.

Typical use cases:
- Keep README/docs consistent and up to date across large codebases.
- Automate documentation review/regeneration locally or in CI pipelines.
- Apply repeatable team guidance and ad-hoc instructions to produce consistent doc updates.

Key features:
- Scans directories and supports path patterns (e.g., "glob:" and "regex:" targets).
- Uses embedded guidance tags to drive consistent documentation output.
- Supports configurable GenAI provider/model selection.
- Accepts external instructions via URL(s) or file path(s), or via interactive stdin.
- Optional default guidance that can be applied as a final step per directory.
- Excludes directories from processing.
- Optional multi-threaded processing.
- Optional logging of LLM request inputs to dedicated log files for audit/debug.

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI endpoints)


2) Installation Instructions
----------------------------
Prerequisites:
- Java 11 or later
- Network access to your configured GenAI provider
- (Optional) A gw.properties configuration file placed next to the executable (or set -Dgw.config=...)

Download:
- Distribution ZIP:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Install / setup:
1. Download and unzip the distribution.
2. In the extracted folder, you should have:
   - gw.jar
   - gw.properties (optional; sample included)
   - gw.bat (Windows launcher)
   - gw.sh  (Unix launcher)
   - g/     (instruction packs)

Configuration (gw.properties):
- Select provider/model:
  genai=CodeMie:gpt-5-2-2025-12-11

- Credentials (examples; keep secrets out of source control):
  - CodeMie:
    GENAI_USERNAME=...
    GENAI_PASSWORD=...
  - OpenAI / compatible:
    OPENAI_API_KEY=...
    OPENAI_BASE_URL=https://your-openai-compatible-endpoint   (optional)


3) How to Run
-------------
Basic usage:

  java -jar gw.jar <path | path_pattern>

Command-line options (summary):
- -h, --help
  Show help and exit.

- -r, --root <path>
  Root directory used to compute and validate scan targets. Scan targets must be within this root.

- -t, --threads[=true|false]
  Enable/disable multi-threaded processing. If provided without a value, it is treated as enabled.
  Use --threads=false to disable.

- -a, --genai <provider:model>
  Set the GenAI provider and model (e.g., OpenAI:gpt-5.1).

- -i, --instructions[=<url|file>[,<url|file>...]]
  Additional instruction locations (URL or file path). Multiple values are comma-separated.
  If used without a value, Ghostwriter reads instruction text from stdin (EOF-terminated).
  Relative file paths are resolved against the execution directory.

- -g, --guidance[=<file>]
  Default guidance applied as a final step per directory.
  If provided with a value, it is treated as a guidance file path (relative paths resolved against the execution directory).
  If used without a value, Ghostwriter reads guidance text from stdin (EOF-terminated).

- -e, --excludes <dir[,dir...]>
  Comma-separated list of directories to exclude from processing.

- -l, --logInputs
  Log LLM request inputs to dedicated log files.

Notes:
- Use quotes around glob/regex patterns.

Windows (.bat) examples:
- Run against a directory:
  gw.bat C:\projects\my-repo

- Run with an explicit root and a subpath:
  gw.bat --root C:\projects\my-repo src

- Run with glob/regex patterns:
  gw.bat --root C:\projects\my-repo "glob:**/*.java"
  gw.bat --root C:\projects\my-repo "regex:^.*\/[^\/]+\.java$"

- Provide instructions via URL/file list:
  gw.bat --root C:\projects\my-repo --instructions "https://example.com/team-guidelines.md,docs/extra-instructions.md" "glob:**/*.md"

- Provide excludes and disable threading:
  gw.bat --root C:\projects\my-repo --excludes "target,node_modules" --threads=false "glob:**/*.md"

- Provide instructions interactively (stdin):
  REM Start gw and then paste/type instructions; end with Ctrl+Z then Enter
  gw.bat --instructions "glob:**/*.md"

Unix (.sh) examples:
- Run against a directory:
  ./gw.sh /path/to/my-repo

- Run with an explicit root and a subpath:
  ./gw.sh --root /path/to/my-repo src

- Run with glob/regex patterns:
  ./gw.sh --root /path/to/my-repo "glob:**/*.java"
  ./gw.sh --root /path/to/my-repo "regex:^.*/[^/]+\\.java$"

- Provide guidance from a file:
  ./gw.sh --root /path/to/my-repo --guidance docs/default-guidance.md "glob:**/*.md"

- Provide instructions interactively (stdin):
  # Start gw and then paste/type instructions; end with Ctrl+D
  ./gw.sh --instructions "glob:**/*.md"

Environment variables / Java system properties:
- You can define properties (notably credentials) as environment variables, or pass them as Java system properties.

  Windows:
    set GENAI_USERNAME=your_codemie_username
    set GENAI_PASSWORD=your_codemie_password

  Unix:
    export GENAI_USERNAME=your_codemie_username
    export GENAI_PASSWORD=your_codemie_password

  Java system properties (example):
    java -DGENAI_USERNAME=... -DGENAI_PASSWORD=... -jar gw.jar <args>

Included files in this distribution folder:
- gw.bat: Windows launcher (wraps "java -jar gw.jar ...").
- gw.sh: Unix launcher (wraps "java -jar gw.jar ...").
- gw.properties: Sample configuration (provider/model selection and credential placeholders).
- g/: Instruction packs you can reference via --instructions.

Instruction packs (folder: g/):
- g/create_tests
  Instruction template for generating high-quality unit tests and targeting 90%+ coverage.

- g/to_java21
  Instruction template for migrating a Java codebase from Java 17 to Java 21.

To use an instruction pack as --instructions, pass its file path:
- Windows:
  gw.bat --instructions g\create_tests "glob:**/*.java"
- Unix:
  ./gw.sh --instructions g/create_tests "glob:**/*.java"


4) Troubleshooting & Support
----------------------------
Common issues:
- Authentication/authorization failures:
  - Verify credentials (e.g., GENAI_USERNAME/GENAI_PASSWORD or OPENAI_API_KEY).
  - If using an OpenAI-compatible service, confirm OPENAI_BASE_URL.
  - Confirm your provider/model string matches the expected format (provider:model).

- No files updated:
  - Ensure the scan target is within --root.
  - Check --excludes for unintended exclusions.
  - If using glob/regex patterns, quote the pattern and verify it matches files.

- Missing configuration:
  - Place gw.properties next to gw.jar, or set -Dgw.config=path/to/gw.properties.

Logs / debug:
- Review console output for progress and errors.
- Use --logInputs to log LLM request inputs to dedicated log files (useful for auditing/debugging prompts).


5) Contact & Documentation
--------------------------
Resources:
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
