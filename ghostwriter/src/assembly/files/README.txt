Ghostwriter CLI

1) Application Overview

Ghostwriter is a guidance-driven CLI documentation engine. It scans project files, extracts embedded @guidance directives, and uses a configured GenAI provider to synthesize and apply updates across your repository (source code, documentation, site content, configuration files, and other relevant artifacts).

Key features:
- Multi-format processing via pluggable reviewers (e.g., Java, Markdown, HTML).
- Guidance-driven generation from embedded @guidance directives, with optional default guidance.
- Pattern-based scanning using scan targets that can be a path, glob: pattern, or regex: pattern.
- Module-aware scanning for multi-module layouts.
- Optional multi-threaded processing.
- Optional logging of composed LLM request inputs per processed file.
- Project-relative scan safety: absolute scan paths must be within the configured root directory.

Typical use cases:
- Keep docs and code comments consistent with embedded requirements.
- Apply repo-wide changes driven by file-embedded guidance (e.g., update documentation, migrate code, add tests).

Supported GenAI providers:
- CodeMie
- OpenAI (and OpenAI-compatible services via base URL)


2) Installation Instructions

Prerequisites:
- Java 11+
- Network access to your configured GenAI provider
- A project directory containing files with embedded @guidance directives (or provide fallback guidance via --guidance)

Download:
- Download the Ghostwriter CLI package: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Install / unpack:
- Unzip the downloaded package to a folder of your choice.
- The package includes:
  - gw.jar (the CLI application)
  - gw.properties (configuration)
  - gw.bat / gw.sh (launch scripts)
  - g\* (example guidance files you can reuse as default guidance)

Configuration (gw.properties):
- Select provider/model:
  - genai=CodeMie:gpt-5-2-2025-12-11
  - (example alternative) genai=OpenAI:gpt-5.1
- Provider credentials (choose what matches your provider):
  - CodeMie:
    - GENAI_USERNAME
    - GENAI_PASSWORD
  - OpenAI / compatible:
    - OPENAI_API_KEY
    - (optional) OPENAI_BASE_URL

You may set these values in gw.properties, or supply them via environment variables or Java -D system properties.


3) How to Run

Launch scripts (recommended):
- Windows:
  - gw.bat passes all args through to: java -jar gw.jar ...
- Unix:
  - gw.sh passes all args through to: java -jar gw.jar ...

Basic usage:
- Windows (cmd.exe):
  - gw.bat src
  - or: java -jar gw.jar src

- Unix:
  - ./gw.sh src
  - or: java -jar gw.jar src

Scan targets:
- A directory or file path:
  - gw.bat C:\projects\my-repo
  - gw.bat src\project
- A glob pattern (quote recommended):
  - gw.bat "glob:**/*.java"
- A regex pattern (quote recommended; note escaping):
  - gw.bat "regex:^.*\\/[^\\/]+\\.java$"

Common CLI options:
- -h, --help
  - Show help and exit.
- -r, --root <path>
  - Root directory used for scan path validation and project-relative resolution.
  - If not set: root from gw.properties; otherwise user directory.
- -t, --threads[=true|false]
  - Enable multi-threaded processing.
  - If present with no value, it enables threading (true).
  - Default: false (or value from gw.properties key threads).
- -a, --genai <provider:model>
  - GenAI provider and model.
  - Default: OpenAI:gpt-5-mini (or value from gw.properties key genai).
- -i, --instructions [text|URL|file:...]
  - System instructions appended to each prompt.
  - If used without a value, reads multi-line input from stdin until EOF.
  - Supports line-based inclusion: http(s)://..., file:...; other lines are used as-is.
- -g, --guidance [text|URL|file:...]
  - Default guidance used when embedded guidance is absent.
  - When scanning a directory, also applied as a final step for the current directory.
  - If used without a value, reads multi-line input from stdin until EOF.
- -e, --excludes <comma,separated,list>
  - Exclude paths or patterns from processing.
  - Default: value from gw.properties key excludes (if set).
- -l, --logInputs
  - Log composed LLM request inputs to dedicated log files.
  - Default: false (or value from gw.properties key logInputs).

Environment variables vs Java system properties:
- Environment variables (examples):
  - Windows (cmd.exe):
    - SET GENAI_USERNAME=your_codemie_username
    - SET GENAI_PASSWORD=your_codemie_password
  - Unix:
    - export GENAI_USERNAME=your_codemie_username
    - export GENAI_PASSWORD=your_codemie_password

- Java system properties (examples):
  - Windows:
    - java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar src
  - Unix:
    - java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar src

Examples using instructions, excludes, and root:
- Windows:
  - gw.bat src -r C:\projects\my-repo -a "OpenAI:gpt-5-mini" -t -e ".machai,target" -l

- Unix:
  - ./gw.sh src -r /home/user/my-repo -a "OpenAI:gpt-5-mini" -t -e ".machai,target" -l

Using packaged guidance files (examples in this folder):
- Create tests guidance:
  - Windows:
    - gw.bat "glob:**/src/test/java/**" -g file:g\create_tests
  - Unix:
    - ./gw.sh "glob:**/src/test/java/**" -g file:g/create_tests

- Java migration guidance:
  - Windows:
    - gw.bat src -g file:g\to_java21
  - Unix:
    - ./gw.sh src -g file:g/to_java21


4) Troubleshooting & Support

Common issues:
- Authentication / authorization errors:
  - Verify provider credentials (GENAI_USERNAME/GENAI_PASSWORD for CodeMie; OPENAI_API_KEY for OpenAI).
  - If using an OpenAI-compatible service, verify OPENAI_BASE_URL.
  - Confirm the provider/model in genai=provider:model is valid.

- Nothing processed / missing expected changes:
  - Confirm your scan target matches the intended files (path vs glob: vs regex:).
  - Check --excludes (or excludes in gw.properties) for patterns that may skip files.
  - Ensure files contain embedded @guidance directives, or provide fallback guidance via --guidance.

- Root / path validation problems:
  - Ensure absolute scan paths are within the configured --root directory.
  - If running from a different working directory, explicitly set --root.

Logging and debug:
- Use --logInputs (-l) to write the composed LLM request inputs per processed file to dedicated log files.
- Also check standard output/error from the Java process and any logs produced by your configured logging setup.


5) Contact & Documentation

Documentation:
- Project site: https://machai.machanism.org/ghostwriter/index.html
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- GitHub: https://github.com/machanism-org/machai
