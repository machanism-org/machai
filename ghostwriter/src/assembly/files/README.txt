Ghostwriter CLI (gw)
====================

1) Application Overview
----------------------
Ghostwriter is a documentation engine that scans a project, applies mandatory @guidance constraints embedded in source and documentation files, and uses GenAI to generate or update documentation consistently.

Typical use cases:
- Keep README and site documentation consistent and up to date.
- Enforce a consistent documentation structure across repositories.
- Periodically regenerate documentation locally or in CI.

Key features:
- Scans directories or supports glob: / regex: path patterns to target files.
- Treats embedded @guidance instructions as mandatory constraints.
- Runs from a single jar with simple launch scripts.

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI)


2) Installation Instructions
----------------------------
Prerequisites:
- Java 11 or newer
- Network access to the configured GenAI provider

Download / Install:
- Download the Ghostwriter CLI package:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Build from source (Maven):

  git clone https://github.com/machanism-org/machai.git
  cd machai
  mvn -pl ghostwriter -am clean verify

What is in this folder:
- gw.properties  : Default configuration (provider/model and credential placeholders)
- gw.bat         : Windows launcher (runs gw.jar)
- gw.sh          : Unix/macOS launcher (runs gw.jar)
- g\             : Optional instruction templates you can pass to -i / --instructions

Configuration:
- Default config file: gw.properties (next to gw.jar)
- Optional Java system property to choose a config file:

  -Dgw.config=<file>

Provider/model selection:
- In gw.properties:

  genai=Provider:Model

  Examples:
  - genai=CodeMie:gpt-5-2-2025-12-11
  - genai=OpenAI:gpt-5.1

Credentials (recommended via environment variables):
- CodeMie:
  - GENAI_USERNAME
  - GENAI_PASSWORD
- OpenAI / OpenAI-compatible:
  - OPENAI_API_KEY
  - (optional) OPENAI_BASE_URL


3) How to Run
-------------
Run the jar directly (example):

Windows:

  java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar C:\projects\my-project

Or use the packaged launchers (recommended for gw.zip):

Windows:

  gw.bat C:\projects\my-project

Unix/macOS:

  ./gw.sh /path/to/my-project

Common options:
- Root directory:

  Windows:
    gw.bat -r C:\projects\my-project

  Unix/macOS:
    ./gw.sh -r /path/to/my-project

- Excludes (comma-separated):

  Windows:
    gw.bat -r C:\projects\my-project -e target,.git,node_modules

  Unix/macOS:
    ./gw.sh -r /path/to/my-project -e target,.git,node_modules

- Target files with glob patterns:

  Windows:
    gw.bat "glob:**\*.md"

  Unix/macOS:
    ./gw.sh "glob:**/*.md"

- Instructions (additional runtime guidance):
  - Provide a value as plain text, a URL, or a file path.
  - If -i / --instructions is provided without a value, Ghostwriter reads from stdin until EOF.

  Windows (cmd.exe) via file:
    gw.bat -r C:\projects\my-project -i file:C:\path\to\instructions.txt

  Windows (PowerShell) via stdin:
    @"
    Prefer concise docs.
    Keep headings stable.
    "@ | .\gw.bat -r C:\projects\my-project -i

  Unix/macOS via stdin:
    printf "%s\n" "Prefer concise docs." "Keep headings stable." | ./gw.sh -r /path/to/my-project -i

Using bundled instruction templates under g\:

  Windows:
    gw.bat -r C:\projects\my-project -i file:%~dp0g\create_tests

  Unix/macOS:
    ./gw.sh -r /path/to/my-project -i file:./g/create_tests


4) Troubleshooting & Support
----------------------------
Authentication errors:
- Confirm provider/model:
  - gw.properties: genai=Provider:Model
- Verify credentials:
  - CodeMie: GENAI_USERNAME / GENAI_PASSWORD
  - OpenAI-compatible: OPENAI_API_KEY (and OPENAI_BASE_URL if required)

No files updated / nothing found:
- Verify you are scanning the correct root (-r / --root).
- If using patterns, ensure you used the correct prefix: glob:... or regex:...
- Check excludes (-e / --excludes) are not filtering your target files.

Missing configuration:
- Ensure gw.properties is next to gw.jar, or pass: -Dgw.config=<file>

Logs / debug:
- Enable LLM request input logging with: -l / --logInputs
- For additional verbosity, run the underlying java command with your preferred JVM/logging options.


5) Contact & Documentation
--------------------------
- Project repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download (CLI package): https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
