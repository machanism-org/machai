Ghostwriter CLI (gw)

1) Application Overview

Ghostwriter is an AI-assisted documentation engine and command-line tool that scans a project, extracts embedded @guidance directives from files, and uses a configured GenAI provider/model to generate or refine content. It can process all types of project files (source code, documentation, project site content, and other artifacts).

Key features
- Scan a directory or a path pattern:
  - raw path
  - glob: patterns
  - regex: patterns
- Extract embedded @guidance blocks from supported files
- Apply system-wide instructions (--instructions)
- Apply directory-level default guidance (--guidance) as a fallback and as a final directory step
- Configurable GenAI provider and model (e.g., OpenAI:gpt-5.1)
- Optional multi-threaded module processing (--threads)
- Optional per-file logging of composed LLM request inputs (--logInputs)
- Exclude directories via comma-separated --excludes

Typical use cases
- Project site / README generation
- API documentation enrichment
- Ongoing documentation maintenance aligned with the codebase

Supported GenAI providers (as configured in gw.properties)
- CodeMie (example: CodeMie:gpt-5-2-2025-12-11)
- OpenAI-compatible services (via OPENAI_API_KEY and optional OPENAI_BASE_URL)


2) Installation Instructions

Prerequisites
- Java 11 or later
- Network access and credentials for your chosen GenAI provider
- A configuration file (gw.properties) and/or environment variables / Java system properties

Download / install
- Download the Ghostwriter CLI bundle (includes gw.jar and helper scripts):
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Build (if building from source)
- This project uses Maven. Build steps vary by repository setup; use your standard Maven build for the Ghostwriter module/artifact.

Included files in this folder
- gw.properties  : default configuration template
- gw.bat         : Windows launcher wrapper for gw.jar
- gw.sh          : Unix launcher wrapper for gw.jar
- gw.jar         : the CLI application jar (expected to be present alongside the scripts)


3) How to Run

A) Run the jar directly

Windows (cmd.exe)
  java -jar gw.jar src\main\java

Unix (bash)
  java -jar gw.jar src/main/java

B) Use the provided launchers

Windows
  gw.bat src\main\java

Unix
  ./gw.sh src/main/java

C) Configuration sources

1. gw.properties
- The CLI can be configured using a properties file (default: gw.properties).
- You can also point to a config path using a Java system property:
  -Dgw.config=<path>

2. Environment variables
- You can define any property from gw.properties as an environment variable.
- The provided scripts show how to set credentials using environment variables.

Windows example (cmd.exe)
  SET GENAI_USERNAME=your_codemie_username
  SET GENAI_PASSWORD=your_codemie_password
  gw.bat src\main\java

Unix example (bash)
  export GENAI_USERNAME=your_codemie_username
  export GENAI_PASSWORD=your_codemie_password
  ./gw.sh src/main/java

3. Java system properties (-D...)
- The scripts also show passing credentials as Java system properties.

Windows example (cmd.exe)
  java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar src\main\java

Unix example (bash)
  java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar src/main/java

D) Common CLI options and examples

Notes
- <scanDir> can be a raw path, a glob pattern, or a regex pattern.
- Use --root to set the project boundary/base for scanning.
- Use --excludes to skip directories (comma-separated, e.g., target,.git).
- Use --instructions to add system instructions to every prompt.
- Use --guidance to provide default directory-level guidance (also used as a fallback for files without embedded @guidance).

Example (glob scan, set root, enable threads, set model, exclude directories, log inputs)
Windows (cmd.exe)
  java -jar gw.jar "glob:**\*.java" -r . -t true -a "OpenAI:gpt-5.1" -e "target,.git" -l

Unix (bash)
  java -jar gw.jar "glob:**/*.java" -r . -t true -a "OpenAI:gpt-5.1" -e "target,.git" -l

Example: set root and excludes
Windows (cmd.exe)
  gw.bat "glob:**\*.md" --root . --excludes "target,.git"

Unix (bash)
  ./gw.sh "glob:**/*.md" --root . --excludes "target,.git"

Example: provide --instructions
- Each line is processed as follows:
  - blank lines preserved
  - http(s)://... lines are fetched and inlined
  - file:... lines are read and inlined
  - other lines used as-is
- If --instructions is provided without a value, Ghostwriter reads instructions from stdin until EOF.

Windows (cmd.exe) - inline value
  gw.bat src\main\java --instructions "Keep changes minimal and accurate."

Unix (bash) - inline value
  ./gw.sh src/main/java --instructions "Keep changes minimal and accurate."

Example: provide default guidance via --guidance
- Used when a file has no embedded @guidance.
- Also applied as a final step to the directory being processed.

Windows (cmd.exe)
  gw.bat src\main\java --guidance "Generate concise, developer-focused documentation."

Unix (bash)
  ./gw.sh src/main/java --guidance "Generate concise, developer-focused documentation."


4) Troubleshooting & Support

Common issues
- Authentication / authorization errors
  - Verify provider credentials are set (CodeMie: GENAI_USERNAME/GENAI_PASSWORD; OpenAI-compatible: OPENAI_API_KEY and optional OPENAI_BASE_URL).
  - Confirm the selected provider/model in gw.properties (genai=...).
- Nothing changes / no output
  - Ensure the scanned files contain embedded @guidance blocks, or set --guidance as a fallback.
  - Confirm your scan path/pattern matches files (raw path vs glob: vs regex:).
- Unexpected files processed
  - Use --root to restrict the project boundary.
  - Use --excludes to omit folders like target, .git, build output, etc.

Logs and debugging
- Use --logInputs (-l) to write per-file logs containing the composed LLM request inputs (useful for auditing/debugging prompts).


5) Contact & Documentation

- Project repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download bundle: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
