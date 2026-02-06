Ghostwriter CLI
==============

1) Application Overview
----------------------
Ghostwriter is a command-line documentation engine that scans a project (or selected paths/patterns), analyzes embedded guidance tags in the repository, and uses GenAI synthesis to generate consistent, repeatable documentation updates.

Typical use cases:
- Regenerating/refreshing documentation across large codebases.
- Enforcing consistent docs output based on in-repo guidance.
- Running scripted documentation checks/updates locally or in CI.

Key features:
- Scan targets can be directories or patterns (supports "glob:" and "regex:").
- Optional external instructions via URL(s), file path(s), or stdin.
- Optional default guidance applied as a final step per directory.
- Exclude directories from processing.
- Optional multi-threaded processing.
- Optional logging of LLM request inputs for audit/debug.

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI)


2) Installation Instructions
----------------------------
Prerequisites:
- Java 11 or later.
- Network access to your configured GenAI provider.
- (Optional) Configuration file "gw.properties" placed next to the executable, or set a custom config location with: -Dgw.config=...

Download:
- Distribution zip: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Install:
1. Download the zip from the link above.
2. Unzip it to a directory of your choice.
3. In the extracted folder, you will typically have:
   - gw.jar          (the application)
   - gw.properties   (defaults/config)
   - gw.bat          (Windows launcher)
   - gw.sh           (Unix launcher)
   - g/              (example guidance/instruction snippets)

Provider configuration (from gw.properties and/or environment variables):
- Select provider/model:
  - genai=CodeMie:gpt-5-2-2025-12-11
  - genai=OpenAI:gpt-5.1 (example)

- CodeMie credentials (set as environment variables or Java -D system properties):
  - GENAI_USERNAME
  - GENAI_PASSWORD

- OpenAI / OpenAI-compatible settings (environment variables or -D system properties):
  - OPENAI_API_KEY
  - OPENAI_BASE_URL (optional; not required for original OpenAI)

Notes about gw.properties:
- You can set defaults such as root, instructions, and excludes.
- Properties can be overridden via environment variables or by passing -DKEY=value to Java.


3) How to Run
-------------
Basic usage:

  java -jar gw.jar <path | path_pattern>

Where:
- <path> is a directory under the root.
- <path_pattern> can be a pattern target such as:
  - glob:**/*.java
  - regex:^.*\/[^\/]+\.java$

Command-line options (from project documentation):
- -h, --help
    Show help and exit.

- -r, --root <path>
    Root directory used to compute and validate scan targets.
    Scan targets must be within this root.
    Default: from gw.properties key "root", otherwise: current user directory (user.dir).

- -t, --threads[=true|false]
    Enable/disable multi-threaded processing.
    If provided without a value, it is treated as enabled.
    Use --threads=false to disable.
    Default: true.

- -a, --genai <provider:model>
    Set the GenAI provider and model.
    Default: from gw.properties key "genai", otherwise: OpenAI:gpt-5-mini.

- -i, --instructions[=<url|file>]
    Provide additional instruction location(s) (URL or file path).
    If present without a value, Ghostwriter reads instruction text from stdin (EOF-terminated).
    Default: from gw.properties key "instructions", otherwise: none.

- -g, --guidance[=<file>]
    Provide default guidance applied as a final step per directory.
    If present with a value, it is treated as a guidance file path (relative paths resolved against the execution directory).
    If present without a value, Ghostwriter reads guidance text from stdin (EOF-terminated).
    Default: from gw.properties key "guidance", otherwise: none.

- -e, --excludes <dir[,dir...]>
    Comma-separated list of directories to exclude from processing.
    Default: from gw.properties key "excludes" (comma-separated), otherwise: none.

- -l, --logInputs
    Log LLM request inputs to dedicated log files.
    Default: false.

Windows examples (.bat)
----------------------
Run with the Windows launcher:

  gw.bat C:\projects\project

Specify a root and a scan target under it:

  gw.bat -r C:\projects\project src\project

Scan by glob/regex pattern:

  gw.bat -r C:\projects\project "glob:**/*.java"
  gw.bat -r C:\projects\project "regex:^.*\/[^\/]+\.java$"

Provide instructions from a file or URL:

  gw.bat -r C:\projects\repo -i C:\projects\repo\docs\team-instructions.md "glob:**/*.md"
  gw.bat -r C:\projects\repo -i "https://example.com/team-guidelines.md" "glob:**/*.md"

Provide instructions via stdin (option present with no value):

  type instructions.txt | gw.bat -r C:\projects\repo -i "glob:**/*.md"

Exclude directories and disable threads:

  gw.bat -r C:\projects\repo -e "target,node_modules" --threads=false "glob:**/*.md"

Enable LLM input logging:

  gw.bat -r C:\projects\repo --logInputs "glob:**/*.md"

Unix examples (.sh)
------------------
Ensure the script is executable:

  chmod +x gw.sh

Run:

  ./gw.sh /path/to/project

Root + glob pattern:

  ./gw.sh --root "/path/to/repo" "glob:**/*.md"

Provide instructions:

  ./gw.sh --root "/path/to/repo" --instructions "https://example.com/team-guidelines.md" "glob:**/*.md"

Provide instructions via stdin:

  cat instructions.txt | ./gw.sh --root "/path/to/repo" --instructions "glob:**/*.md"

Exclude directories, disable threads, and log inputs:

  ./gw.sh --root "/path/to/repo" --excludes "target,node_modules" --threads=false --logInputs "glob:**/*.md"


Bundled guidance snippets (g/)
-----------------------------
This distribution includes example instruction/guidance snippets under the "g/" folder:
- g/create_tests
  Guidance for creating high-quality unit tests (coverage and structure requirements).
- g/to_java21
  Guidance for migrating a Java codebase from Java 17 to Java 21.

You can reference these files with --instructions or --guidance, for example:
- Windows:
  gw.bat --root C:\projects\repo --instructions g\create_tests "glob:**/*.java"
- Unix:
  ./gw.sh --root "/path/to/repo" --instructions g/create_tests "glob:**/*.java"


4) Troubleshooting & Support
----------------------------
Common issues:
- Authentication / 401 / 403 errors:
  - Verify provider credentials (e.g., GENAI_USERNAME/GENAI_PASSWORD for CodeMie, OPENAI_API_KEY for OpenAI).
  - If using an OpenAI-compatible service, confirm OPENAI_BASE_URL is correct.
  - Ensure your network/proxy settings allow outbound requests to the provider.

- "No such file" / missing guidance or instructions:
  - Verify the file path you pass to --instructions/--guidance.
  - Remember: relative paths are resolved against the execution directory.

- Targets rejected / outside root:
  - Ensure the scan target path (or files matched by your pattern) is within --root.

Logs and debug:
- Run with --logInputs to write LLM request inputs to dedicated log files (useful for audit/debug).
- Check console output for progress and error details.


5) Contact & Documentation
--------------------------
Documentation and links:
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
