Ghostwriter CLI

1) Application Overview

Ghostwriter is a guidance-driven documentation and code review engine that runs as a CLI. It scans project files, reads embedded @guidance directives, and uses a configured GenAI provider to synthesize and apply updates across your repository (source code, documentation, project site content, configuration files, and other relevant artifacts).

Key features:
- Guidance-driven generation from embedded @guidance directives, with optional default guidance.
- Multi-format processing across typical project file types.
- Pattern-based scanning with path, glob:, and regex: scan targets.
- Module-aware scanning for multi-module layouts.
- Optional multi-threaded processing.
- Optional logging of composed LLM request inputs per processed file.
- Project-relative scan safety: absolute scan paths must be within the configured root directory.

Typical use cases:
- Keep documentation and project artifacts aligned by generating updates directly from embedded guidance.
- Apply repo-wide changes driven by file-embedded requirements (e.g., update documentation, migrate code, add tests).

Supported GenAI providers:
- CodeMie
- OpenAI (and OpenAI-compatible services)


2) Installation Instructions

Prerequisites:
- Java (required version is defined in the project pom.xml; functional requirements may differ)
- Network access to your configured GenAI provider (as applicable)
- Provider credentials (via environment variables, Java -D properties, or gw.properties)

Download:
- Ghostwriter CLI package:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Install / unpack:
- Unzip the downloaded package to a folder of your choice.
- The distribution folder contains:
  - gw.jar (the CLI application)
  - gw.properties (configuration)
  - gw.bat / gw.sh (launch scripts)
  - g\... (example default-guidance files)

Configuration (gw.properties):
- Provider/model:
  - genai=CodeMie:gpt-5-2-2025-12-11
  - (example alternative) genai=OpenAI:gpt-5-mini

- CodeMie credentials (uncomment and set if using CodeMie):
  - GENAI_USERNAME=your_codemie_username
  - GENAI_PASSWORD=your_codemie_password

- OpenAI / compatible credentials (uncomment and set if using OpenAI-compatible services):
  - OPENAI_API_KEY=your_openai_api_key
  - OPENAI_BASE_URL=https://your-openai-compatible-endpoint (optional)


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
  - Windows:
    - gw.bat C:\projects\my-repo
    - gw.bat src\project
  - Unix:
    - ./gw.sh /home/user/my-repo
    - ./gw.sh src/project

- A glob pattern (quote recommended):
  - Windows:
    - gw.bat "glob:**/*.java"
  - Unix:
    - ./gw.sh "glob:**/*.java"

- A regex pattern (quote recommended; note escaping):
  - Windows:
    - gw.bat "regex:^.*\\/[^\\/]+\\.java$"
  - Unix:
    - ./gw.sh "regex:^.*\\/[^\\/]+\\.java$"

Configuration sources:
- gw.properties keys (packaged):
  - genai (provider:model)
  - root
  - instructions
  - excludes
  - threads
  - logInputs

- Environment variables (examples):
  - Windows (cmd.exe):
    - SET GENAI_USERNAME=your_codemie_username
    - SET GENAI_PASSWORD=your_codemie_password
    - SET OPENAI_API_KEY=your_openai_api_key
    - SET OPENAI_BASE_URL=https://your-openai-compatible-endpoint
  - Unix:
    - export GENAI_USERNAME=your_codemie_username
    - export GENAI_PASSWORD=your_codemie_password
    - export OPENAI_API_KEY=your_openai_api_key
    - export OPENAI_BASE_URL=https://your-openai-compatible-endpoint

- Java system properties (examples):
  - Windows:
    - java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar src
  - Unix:
    - java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar src

Examples using instructions, excludes, and root:
- Windows:
  - gw.bat src -r C:\projects\my-repo -i "file:instructions.txt" -e ".machai,target" -t -l

- Unix:
  - ./gw.sh src -r /home/user/my-repo -i "file:instructions.txt" -e ".machai,target" -t -l

Using the packaged default-guidance files (g\):
- Create tests guidance (g\create_tests):
  - Windows:
    - gw.bat "glob:**/src/test/java/**" -g file:g\create_tests
  - Unix:
    - ./gw.sh "glob:**/src/test/java/**" -g file:g/create_tests

- Java migration guidance (g\to_java21):
  - Windows:
    - gw.bat src -g file:g\to_java21
  - Unix:
    - ./gw.sh src -g file:g/to_java21


4) Troubleshooting & Support

Common issues:
- Authentication / authorization errors:
  - Verify provider credentials (CodeMie: GENAI_USERNAME/GENAI_PASSWORD; OpenAI: OPENAI_API_KEY).
  - If using an OpenAI-compatible service, verify OPENAI_BASE_URL.
  - Confirm the provider/model in genai=provider:model is valid.

- Nothing processed / missing expected changes:
  - Confirm the scan target matches the intended files (path vs glob: vs regex:).
  - Check excludes (CLI --excludes or gw.properties excludes) for entries that may skip files.
  - Ensure files contain embedded @guidance directives, or provide fallback guidance via --guidance.

- Root / path validation problems:
  - Ensure absolute scan paths are within the configured --root directory.
  - If running from a different working directory, explicitly set --root.

Logs and debug:
- Use --logInputs (-l) to write the composed LLM request inputs per processed file to dedicated log files.
- Also check standard output/error from the Java process and any logs produced by your configured logging setup.


5) Contact & Documentation

- Project site:
  https://machai.machanism.org/ghostwriter/index.html
- Download:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
- Maven Central:
  https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- GitHub:
  https://github.com/machanism-org/machai
