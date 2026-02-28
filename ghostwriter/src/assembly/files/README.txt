Ghostwriter CLI (Machai)
========================

1) Application Overview
-----------------------
Ghostwriter is a guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.

What it does:
- Scans a project directory tree (or a glob:/regex: matcher).
- Discovers supported file types and extracts embedded "@guidance:" directives.
- Builds prompts with project structure context plus optional system instructions.
- Sends each prompt to a configured GenAI provider and applies results.

Typical use cases:
- Repository-wide documentation generation/maintenance (e.g., Markdown, project site content).
- Batch code reviews and guided refactors driven by file-local @guidance blocks.
- CI/CD-friendly, repeatable processing over selected paths/patterns.

Supported GenAI providers (configuration determines what is available in your environment):
- CodeMie (example default in gw.properties).
- OpenAI-compatible services (OpenAI, or compatible endpoints via base URL + API key).


2) Installation Instructions
----------------------------
Prerequisites
- Java 8 runtime (Ghostwriter targets Java 8).
- A GenAI provider + model configured (required):
  - Property: gw.genai
  - Or CLI: -a / --genai <provider:model>
- Provider credentials via environment variables (see src/assembly/files/gw.properties).
- (Optional) A gw.properties configuration file.

Get / build
- Download the distribution (contains gw.jar and supporting files):
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

- Or build from source (Maven project):
  - Run Maven package in the repository root to produce the CLI artifact.
  - Use the produced jar as "gw.jar" in the commands below.

Configuration file location
- By default Ghostwriter loads "gw.properties" from its home directory.
- Home directory resolution:
  - System property gw.home if set, else
  - -r/--root if provided, else
  - current working directory.
- You can override the config file name/path (relative to gw.home) via:
  - Java system property: -Dgw.config=<path>

Files in this folder
- gw.properties: sample configuration (provider selection + credential env vars + options).
- README.txt: this file.


3) How to Run
-------------
Basic syntax
  java -jar gw.jar <scanDir> [options]

Where <scanDir> can be:
- A directory path (often relative to the project root), OR
- A path matcher:
  - glob:...  (example: "glob:**/*.java")
  - regex:... (example: "regex:^.*/[^/]+\\.java$")

Windows (.bat / cmd.exe) examples
- Scan a directory:
  java -jar gw.jar src -a OpenAI:gpt-5.1

- Scan with glob matcher:
  java -jar gw.jar "glob:**/*.md" -a OpenAI:gpt-5.1

- Provide default guidance via stdin (finish with Ctrl+Z then Enter):
  java -jar gw.jar "glob:**/*.md" -a OpenAI:gpt-5.1 -g

- Provide system instructions via file reference:
  java -jar gw.jar src -a OpenAI:gpt-5.1 -i file:instructions.txt

- Exclude directories/files (comma-separated):
  java -jar gw.jar src -a OpenAI:gpt-5.1 -e target,.git,src\\generated

Unix (.sh / bash) examples
- Scan a directory:
  java -jar gw.jar src -a OpenAI:gpt-5.1

- Scan with glob matcher:
  java -jar gw.jar "glob:**/*.md" -a OpenAI:gpt-5.1

- Provide default guidance via stdin (finish with Ctrl+D):
  java -jar gw.jar "glob:**/*.md" -a OpenAI:gpt-5.1 -g

- Provide system instructions via URL:
  java -jar gw.jar src -a OpenAI:gpt-5.1 -i https://example.com/instructions.txt


Configuration properties (from org.machanism.machai.gw.processor.Ghostwriter)

Ghostwriter reads configuration from gw.properties (or override file) and allows overrides via CLI.

Property: gw.config (Java system property)
- Description: Overrides which properties file to load.
- Default: gw.properties
- Usage: -Dgw.config=path\to\gw.properties

Property: gw.home (Java system property)
- Description: Sets the Ghostwriter home directory used as the base for locating the config file.
- Default: derived (see "Installation Instructions" -> "Configuration file location")
- Usage: -Dgw.home=path\to\home

Property: gw.rootDir
- Description: Root directory for file processing.
- Default: current working directory (if not provided via -r or gw.rootDir)
- Usage context: used to resolve/validate scan targets and project context.

Property: gw.genai
- Description: GenAI provider and model to use, formatted as provider:model (e.g., OpenAI:gpt-5.1).
- Default: none (required)
- Usage context: required to run; also overridable with -a/--genai.

Property: gw.instructions
- Description: System instructions supplied to the provider.
- Default: none
- Value forms (line-by-line processing):
  - Plain text
  - URL lines starting with http:// or https:// (loaded)
  - file:... lines (loaded)
- Usage context: configured via gw.properties or -i/--instructions.

Property: gw.guidance
- Description: Default guidance used when embedded @guidance directives are not present; also used as a final step for the current directory.
- Default: none
- Value forms: plain text, URL, or file: references (processed line-by-line)
- Usage context: configured via gw.properties or -g/--guidance.

Property: gw.excludes
- Description: Comma-separated list of directories/paths/patterns to exclude from processing.
- Default: none
- Usage context: configured via gw.properties or -e/--excludes.

Property: gw.threads
- Description: Enable multi-threaded module processing.
- Default: false
- Usage context: configured via gw.properties or -t/--threads.

Property: gw.logInputs
- Description: Log provider request inputs to dedicated log files.
- Default: false
- Usage context: configured via gw.properties or -l/--logInputs.


CLI options (from Ghostwriter)

-h, --help
- Show help message and exit.

-r, --root <path>
- Specify the path to the root directory for file processing.
- Default: gw.rootDir or current working directory.

-t, --threads[=<true|false>]
- Enable multi-threaded processing (default false).
- If provided with no value, it enables threading.
- Default: false (or gw.threads)

-a, --genai <provider:model>
- Set the GenAI provider and model.
- Default: gw.genai (required if not set)

-i, --instructions[=<text|url|file:...>]
- Provide system instructions.
- If used without a value, reads multi-line text from stdin until EOF.
- Default: gw.instructions

-g, --guidance[=<text|url|file:...>]
- Provide default guidance.
- If used without a value, reads multi-line text from stdin until EOF.
- Default: gw.guidance

-e, --excludes <csv>
- Comma-separated excludes.
- Default: gw.excludes

-l, --logInputs
- Log LLM request inputs to dedicated log files.
- Default: false (or gw.logInputs)

--act[=<name and prompt>]
- Act mode: interactive execution of predefined prompts.
- If used without a value, reads the action from stdin until EOF.
- Notes:
  - Usage: --act <name> [prompt]
  - Requires a resource file: src/main/resources/act/<name>.properties
  - If no explicit prompt is provided, the prompt falls back to gw.guidance.


Environment variables (from src/assembly/files/gw.properties)

CodeMie (if using CodeMie provider)
- GENAI_USERNAME
- GENAI_PASSWORD

OpenAI / OpenAI-compatible
- OPENAI_API_KEY
- OPENAI_BASE_URL (optional; required for compatible endpoints)

How to set environment variables
- Windows (cmd.exe):
  set OPENAI_API_KEY=... 
  set OPENAI_BASE_URL=https://...

- Unix:
  export OPENAI_API_KEY=...
  export OPENAI_BASE_URL=https://...

How to set Java system properties
- Example:
  java -Dgw.config=gw.properties -Dgw.home=. -jar gw.jar src -a OpenAI:gpt-5.1


4) Troubleshooting & Support
----------------------------
Common issues
- "No GenAI provider/model configured":
  - Set gw.genai in gw.properties, or pass -a/--genai provider:model.

- Authentication errors:
  - Ensure the correct environment variables are set for your provider.
  - For OpenAI-compatible endpoints, verify OPENAI_BASE_URL.

- No files processed:
  - Confirm <scanDir> points to the intended directory.
  - If using glob:/regex:, validate the matcher syntax.
  - Check excludes (gw.excludes or -e) are not filtering everything.

- Instructions/guidance not applied:
  - If you used -i/-g without a value, ensure you terminated stdin with EOF:
    - Windows: Ctrl+Z then Enter
    - Unix: Ctrl+D

Logs / debug
- Ghostwriter uses SLF4J logging.
- If -l/--logInputs is enabled, Ghostwriter writes provider input logs to dedicated files (location depends on runtime logging configuration/output paths).
- Enable more verbose logging by adjusting the project’s logging configuration (e.g., logback/log4j settings if present in your runtime environment).


5) Contact & Documentation
--------------------------
Documentation and resources
- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
