Ghostwriter CLI (gw)
====================

1) Application Overview
----------------------
Ghostwriter is an advanced documentation engine that automatically scans and analyzes project files for embedded `@guidance` blocks, then uses GenAI-powered synthesis to generate or update documentation artifacts in a repeatable, reviewable way.

Typical use cases:
- Keep README and site documentation consistent and up to date.
- Enforce consistent documentation structure across repositories.
- Periodically regenerate documentation locally or in CI.

Key features:
- Scans directories or supports `glob:` / `regex:` path patterns to target files.
- Treats embedded `@guidance` instructions as mandatory constraints.
- Accepts additional runtime instructions and default guidance via CLI (plain text, URL, file path, or stdin).
- Optional multi-threaded processing for improved throughput on larger repositories.
- Optional logging of LLM request inputs to dedicated log files for traceability.

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

What’s in this folder:
- gw.properties  : Default configuration (provider/model and credential placeholders)
- gw.bat         : Windows launcher (runs gw.jar)
- gw.sh          : Unix/macOS launcher (runs gw.jar)
- g\             : Optional instruction templates you can pass via --instructions

Configuration sources:
- gw.properties located next to gw.jar (default)
- Java system property to choose a config file:

  -Dgw.config=<file>

Provider/model selection:
- In gw.properties (or via CLI):

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
Basic usage (scan a directory):

Windows:

  gw.bat C:\projects\my-project

Unix/macOS:

  ./gw.sh /path/to/my-project

Scan targets:
- Positional arguments after options are treated as scan targets.
- Targets may be directories or patterns prefixed with "glob:" or "regex:".
- If no targets are provided, Ghostwriter scans the current user directory.

Run with explicit root, excludes, and a glob pattern:

Windows:

  gw.bat -r C:\projects\my-project -e target,.git,node_modules "glob:**\*.md"

Unix/macOS:

  ./gw.sh -r /path/to/my-project -e target,.git,node_modules "glob:**/*.md"

Use a properties file explicitly (call Java directly):

Windows:

  java -Dgw.config=gw.properties -jar gw.jar -r C:\projects\my-project

Unix/macOS:

  java -Dgw.config=gw.properties -jar gw.jar -r /path/to/my-project

Override provider/model (CLI):

  gw.bat -r C:\projects\my-project -a OpenAI:gpt-5.1

Common options:
- -r, --root <dir>
  Root directory used as the base for scanning; if not set, defaults to the current user directory.

- -e, --excludes <list>
  Comma-separated list of directories to exclude (example: target,.git,node_modules).

- -i, --instructions [value]
  Additional system instructions (plain text, URL, or file path).
  If provided without a value, Ghostwriter reads from stdin until EOF.

- -g, --guidance [value]
  Default guidance (plain text, URL, or file path) applied as a final step for the current directory.
  If provided without a value, Ghostwriter reads from stdin until EOF.

- -l, --logInputs
  Log LLM request inputs to dedicated log files.

- -t, --threads [value]
  Enable multi-threaded processing.

Examples using --instructions:

Windows (cmd.exe) via file:

  gw.bat -r C:\projects\my-project -i file:C:\path\to\instructions.txt

Windows (PowerShell) via stdin:

  @"
  Prefer concise docs.
  Keep headings stable.
  "@ | .\gw.bat -r C:\projects\my-project -i

Unix/macOS via stdin:

  printf "%s\n" "Prefer concise docs." "Keep headings stable." | ./gw.sh -r /path/to/my-project -i

Using the bundled templates under g\:

Windows:

  gw.bat -r C:\projects\my-project -i file:%~dp0g\create_tests
  gw.bat -r C:\projects\my-project -i file:%~dp0g\to_java21

Unix/macOS:

  ./gw.sh -r /path/to/my-project -i file:./g/create_tests
  ./gw.sh -r /path/to/my-project -i file:./g/to_java21


4) Troubleshooting & Support
----------------------------
Authentication errors:
- Confirm provider/model:
  - gw.properties: genai=Provider:Model
  - or CLI: -a Provider:Model
- Verify credentials:
  - CodeMie: GENAI_USERNAME / GENAI_PASSWORD
  - OpenAI-compatible: OPENAI_API_KEY (and OPENAI_BASE_URL if required)
- If passing values via -D..., ensure they are passed to the Java process.

No files updated / nothing found:
- Verify you are scanning the correct root (-r / --root).
- If using patterns, ensure you used the correct prefix: "glob:..." or "regex:...".
- Check excludes (-e / --excludes) are not filtering your target files.

Missing config:
- Ensure gw.properties is next to gw.jar, or pass: -Dgw.config=<file>

Logs / debug:
- Enable LLM request input logging with: -l / --logInputs
- If you need more verbosity, run the underlying java command with your preferred JVM/logging options.


5) Contact & Documentation
--------------------------
- Project repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download (CLI package): https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
