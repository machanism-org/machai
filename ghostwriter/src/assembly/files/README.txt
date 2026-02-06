Ghostwriter CLI
==============

1) Application Overview
-----------------------
Ghostwriter is a CLI documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis.

It helps teams keep documentation accurate and consistent by combining:
- project structure and source scanning
- embedded, file-local guidance directives
- configurable AI prompts (instructions and default guidance)
- reproducible CLI-driven generation suitable for local use and CI

Key features:
- Scans one or more directories or path patterns to process documentation sources
- Supports default configuration via gw.properties (or -Dgw.config override)
- Selects a GenAI provider/model for synthesis (for example: OpenAI:gpt-5.1)
- Accepts additional instructions and default guidance from text, URLs, or files
- Excludes directories from processing
- Optional multi-threaded processing
- Optional logging of LLM request inputs into dedicated log files

Typical use cases:
- Generate or refresh README/site docs from source + embedded guidance
- Apply organization documentation standards via default guidance
- Run repeatable documentation updates locally or in CI

Supported GenAI providers (as configured):
- CodeMie
- OpenAI and OpenAI-compatible services (via API key and optional base URL)


2) Installation Instructions
----------------------------
Prerequisites:
- Java 11 or later
- Network access for the selected GenAI provider (as configured in your environment)
- (Optional) gw.properties configuration file (see below)

Get the CLI distribution:
- Download: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

What’s in this folder:
- gw.jar         (the application)
- gw.properties  (default configuration file)
- gw.bat         (Windows launcher)
- gw.sh          (Unix launcher)

Configuration notes:
- By default, Ghostwriter loads configuration from gw.properties located next to gw.jar.
- To use an alternate config file, pass a Java system property:
  -Dgw.config=<path>

Credentials (set via environment variables or properties):
- CodeMie:
  - GENAI_USERNAME
  - GENAI_PASSWORD
- OpenAI / OpenAI-compatible:
  - OPENAI_API_KEY
  - (Optional) OPENAI_BASE_URL


3) How to Run
-------------
Basic usage:

  java -jar gw.jar <path | path_pattern>

Example (Windows):

  java -jar gw.jar C:\projects\my-project

Recommended: run via the provided scripts.

Windows (.bat):

  gw.bat C:\projects\my-project

Unix (.sh):

  ./gw.sh /home/me/projects/my-project

Providing configuration via environment variables:
- Windows (cmd.exe):

  set GENAI_USERNAME=your_codemie_username
  set GENAI_PASSWORD=your_codemie_password
  gw.bat C:\projects\my-project

- Unix (bash/zsh):

  export GENAI_USERNAME=your_codemie_username
  export GENAI_PASSWORD=your_codemie_password
  ./gw.sh /home/me/projects/my-project

Providing configuration via Java system properties:

  java -Dgw.config=gw.properties -jar gw.jar <path | path_pattern>

Common CLI options:
- -r, --root <path>
  Specify the root directory used as the base for resolving scan targets.

- -i, --instructions [value]
  Additional instructions as plain text, URL, or file path.
  - If used without a value, Ghostwriter prompts for text via stdin (EOF to finish).
  - To provide multiple locations, separate by comma (,).
  - Each line is processed as follows:
    - blank lines preserved
    - http(s)://... loaded from URL
    - file:... loaded from file path
    - other lines used as-is

- -g, --guidance [value]
  Default guidance applied as a final step for the current directory.
  Input handling matches --instructions.

- -e, --excludes <list>
  Directories to exclude from processing. Provide multiple directories separated by commas or by repeating the option.

Example (Windows, cmd.exe):

  gw.bat -r C:\projects\my-project -a OpenAI:gpt-5.1 -t true -e target,.git,node_modules -l ^
    -i "file:docs/instructions.txt,https://example.com/team-instructions.txt" ^
    -g "file:docs/default-guidance.txt" ^
    C:\projects\my-project

Example (Unix):

  ./gw.sh -r /home/me/projects/my-project -a OpenAI:gpt-5.1 -t true -e target,.git,node_modules -l \
    -i "file:docs/instructions.txt,https://example.com/team-instructions.txt" \
    -g "file:docs/default-guidance.txt" \
    /home/me/projects/my-project

See all options:

  java -jar gw.jar --help


4) Troubleshooting & Support
----------------------------
Common issues:
- Authentication/authorization errors:
  - Verify the selected provider/model (genai=... or --genai ...).
  - For CodeMie, ensure GENAI_USERNAME/GENAI_PASSWORD are set.
  - For OpenAI-compatible services, ensure OPENAI_API_KEY is set; set OPENAI_BASE_URL if required.

- Configuration not being picked up:
  - Ensure gw.properties is next to gw.jar (or set -Dgw.config=... explicitly).
  - If using gw.bat/gw.sh, run the script from the distribution folder or rely on its built-in jar path.

- Missing files / unexpected scan results:
  - Confirm the path or path_pattern exists and is accessible.
  - Use --root to control the base directory.
  - Use --excludes to skip build outputs and VCS directories (for example: target,.git,node_modules).

Logs and debug:
- Use --logInputs (-l) to capture LLM request inputs in dedicated log files.
- For command-line help:

  java -jar gw.jar --help


5) Contact & Documentation
--------------------------
Resources:
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download (CLI distribution): https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
