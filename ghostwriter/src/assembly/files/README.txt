Ghostwriter CLI (gw)
====================

1) Application Overview
----------------------
Ghostwriter is a documentation engine that scans project files for embedded `@guidance` blocks and uses GenAI to generate or update documentation artifacts in a repeatable, reviewable way.

Typical use cases:
- Keep README and site documentation consistent and up to date.
- Enforce a consistent documentation structure across repositories.
- Periodically regenerate documentation locally or in CI.

Key features:
- Scans directories and supports `glob:` / `regex:` path patterns to target files.
- Treats embedded `@guidance` instructions as mandatory constraints.
- Accepts additional runtime instructions and default guidance via CLI (literal text, URL, file, or stdin).
- Optional multi-threaded processing for improved throughput.
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
- g/             : Optional task instruction templates you can pass via --instructions
  - g/create_tests : Create unit tests for a source folder (coverage-focused)
  - g/to_java21    : Migrate a codebase from Java 17 to Java 21

Configuration:
- By default, Ghostwriter loads gw.properties located next to gw.jar.
- Or specify a config file explicitly via Java system property:

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

Run with explicit root, excludes, and a glob pattern:

Windows:

  gw.bat -r C:\projects\my-project -e target,.git,node_modules "glob:**/*.md"

Unix/macOS:

  ./gw.sh -r /path/to/my-project -e target,.git,node_modules "glob:**/*.md"

Use a properties file explicitly:

Windows:

  gw.bat -Dgw.config=gw.properties -r C:\projects\my-project

Unix/macOS:

  ./gw.sh -Dgw.config=gw.properties -r /path/to/my-project

Override provider/model (CLI):

  gw.bat -r C:\projects\my-project -a OpenAI:gpt-5.1

Common options:
- -r, --root <dir>
  Root directory used as the base for scanning.

- -e, --excludes <list>
  Comma-separated list of directories to exclude (example: target,.git,node_modules).

- -i, --instructions [value]
  Additional instructions to apply during processing.
  If provided without a value, Ghostwriter reads from stdin until EOF.
  Each input line is interpreted as:
  - http(s)://...  -> load from URL
  - file:...       -> load from file path
  - otherwise      -> literal text

- -g, --guidance [value]
  Default guidance applied as a final step for the current directory (supports stdin/URL/file like --instructions).

Examples using --instructions:

Windows (PowerShell) via stdin:

  @"
  Prefer concise docs.
  Keep headings stable.
  "@ | gw.bat -r C:\projects\my-project -i

Unix/macOS via stdin:

  cat <<'EOF' | ./gw.sh -r /path/to/my-project -i
  Prefer concise docs.
  Keep headings stable.
  EOF

Examples using a file or URL:

Windows:

  gw.bat -r C:\projects\my-project -i file:C:\path\to\instructions.txt
  gw.bat -r C:\projects\my-project -i https://example.com/instructions.txt

Unix/macOS:

  ./gw.sh -r /path/to/my-project -i file:/path/to/instructions.txt
  ./gw.sh -r /path/to/my-project -i https://example.com/instructions.txt

Enable/disable multi-threading:

  gw.bat -r C:\projects\my-project -t true
  gw.bat -r C:\projects\my-project -t false

Log LLM request inputs (for traceability):

  gw.bat -r C:\projects\my-project -l


4) Troubleshooting & Support
----------------------------
Authentication errors:
- Confirm the configured provider/model:
  - gw.properties: genai=Provider:Model
  - or CLI: -a Provider:Model
- Verify credentials are set for your provider:
  - CodeMie: GENAI_USERNAME / GENAI_PASSWORD
  - OpenAI-compatible: OPENAI_API_KEY (and OPENAI_BASE_URL if required)
- If passing credentials via Java system properties (-D...), ensure they are passed to the Java process.

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
