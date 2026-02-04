Ghostwriter CLI (gw)
===================

1) Application Overview
-----------------------
Ghostwriter is a CLI documentation engine that scans a project (or matching file patterns) and updates/assembles documentation using embedded guidance tags and AI-powered synthesis.

Typical use cases:
- Keep README/docs consistent across large codebases.
- Automate documentation review/regeneration locally or in CI.
- Apply team “guidance” and ad-hoc “instructions” to produce repeatable doc updates.

Key features:
- Scan directories or path patterns (glob/regex) under a chosen root.
- Use embedded guidance tags to drive consistent output.
- Configurable GenAI provider/model selection.
- Accept instructions from URL(s), file path(s), or via interactive stdin.
- Optional default guidance applied as a final step per directory.
- Exclude directories from processing.
- Optional multi-threaded processing.
- Optional logging of LLM request inputs for audit/debug.

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI endpoints)


2) Installation Instructions
----------------------------
Prerequisites:
- Java 11 or later
- Network access to your configured GenAI provider
- (Optional) A gw.properties configuration file placed next to the executable
  - To use a different config location, set: -Dgw.config=...

Download:
- Distribution ZIP:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Install / setup:
1. Download and unzip the distribution.
2. Ensure the folder contains:
   - gw.jar
   - gw.properties (optional, but recommended for defaults)
   - gw.bat (Windows launcher)
   - gw.sh  (Unix launcher)
   - g/     (example instruction packs)

Configuration file (gw.properties):
- Select provider/model:
  genai=CodeMie:gpt-5-2-2025-12-11
- Credentials (examples, keep secrets out of source control):
  - CodeMie:
    GENAI_USERNAME=...
    GENAI_PASSWORD=...
  - OpenAI / compatible:
    OPENAI_API_KEY=...
    OPENAI_BASE_URL=https://your-openai-compatible-endpoint   (optional)


3) How to Run
-------------
Basic usage:
- Run the jar directly:
  java -jar gw.jar <path | path_pattern>

Common options (from project documentation):
- -h, --help
- -r, --root <dir>
- -t, --threads[=true|false]
- -a, --genai <Provider:Model>
- -i, --instructions[=<url|file>[,<url|file>...]]
- -g, --guidance[=<file>]
- -e, --excludes <comma,separated,paths>
- -l, --logInputs

Notes:
- Targets are validated to be within the configured --root.
- If --instructions is provided with no value, Ghostwriter reads instruction text from stdin (end with EOF).
- If --guidance is provided with no value, Ghostwriter reads guidance text from stdin (end with EOF).
- Relative instruction/guidance file paths are resolved against the execution directory.

Windows (.bat) examples:
- Run against a directory:
  gw.bat C:\projects\my-repo

- Run with an explicit root and a subpath:
  gw.bat --root C:\projects\my-repo src

- Run with glob/regex patterns (quote patterns):
  gw.bat --root C:\projects\my-repo "glob:**/*.java"
  gw.bat --root C:\projects\my-repo "regex:^.*\\/[^\\/]+\\.java$"

- Provide instructions via URL/file list:
  gw.bat --root C:\projects\my-repo --instructions "https://example.com/team-guidelines.md,docs/extra-instructions.md" "glob:**/*.md"

- Provide excludes and disable threading:
  gw.bat --root C:\projects\my-repo --excludes "target,node_modules" --threads=false "glob:**/*.md"

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

Environment variables / Java system properties:
- You may set any property from gw.properties as an environment variable.
  Examples:
  - CodeMie:
    Windows:
      set GENAI_USERNAME=your_codemie_username
      set GENAI_PASSWORD=your_codemie_password
    Unix:
      export GENAI_USERNAME=your_codemie_username
      export GENAI_PASSWORD=your_codemie_password

- Alternatively pass them as Java system properties:
  java -DGENAI_USERNAME=... -DGENAI_PASSWORD=... -jar gw.jar <args>

Included instruction packs (folder: g/):
- g/create_tests
  A ready-to-use instruction template for generating high-quality unit tests and targeting 90%+ coverage.

- g/to_java21
  A ready-to-use instruction template for migrating a Java codebase from Java 17 to Java 21.

To use one of these packs as --instructions, pass its file path:
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
  - Confirm your provider/model string matches the expected format (Provider:Model).

- Nothing happens / no files updated:
  - Ensure the scan target is within --root.
  - Check --excludes for unintended exclusions.
  - If using patterns, quote the pattern and verify it matches files.

- Missing configuration:
  - Place gw.properties next to gw.jar, or set -Dgw.config=path/to/gw.properties.

Logs / debug:
- Review console output for progress and errors.
- Use --logInputs to log LLM request inputs to dedicated log files (useful for auditing/debugging prompts).


5) Contact & Documentation
--------------------------
Project resources:
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
