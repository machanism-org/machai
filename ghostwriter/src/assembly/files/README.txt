Ghostwriter CLI - README

1) Application Overview

Ghostwriter is a guidance-driven documentation engine that scans project files, extracts embedded `@guidance` directives, and uses a configured GenAI provider to synthesize and apply updates. It is designed for real-world repositories where documentation spans many formats (source code, Markdown, HTML, configuration, and site content), enabling teams to keep artifacts accurate and consistent with less manual effort.

Typical use cases:
- Generate and refresh documentation from file-embedded guidance.
- Keep code, docs, and project site content consistent across a repository.
- Run repository-wide review/update passes using path patterns and exclusions.

Key features:
- Multi-format processing via pluggable reviewers (e.g., Java, Markdown, HTML, etc.).
- Guidance-driven generation based on embedded `@guidance` directives.
- Pattern-based scanning with `glob:` and `regex:` path matchers.
- Module-aware scanning for multi-module project layouts.
- Optional multi-threaded processing.
- Optional logging of composed LLM request inputs per processed file.
- Project-relative scan safety: absolute scan paths must be within the configured root directory.

Supported GenAI providers (as configured in properties / CLI):
- CodeMie
- OpenAI-compatible services (including OpenAI)


2) Installation Instructions

Prerequisites:
- Java 11+
- Network access to your configured GenAI provider (as applicable)
- A project directory containing files with embedded `@guidance` directives (or use `--guidance` as a fallback)

Download:
- Download the Ghostwriter CLI package:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Package contents (this folder):
- gw.jar (expected alongside these scripts)
- gw.properties (configuration defaults)
- gw.bat (Windows launcher)
- gw.sh (Unix/macOS launcher)

Configuration:
- Edit gw.properties to select your provider/model and set defaults.
  Example (already present):
    genai=CodeMie:gpt-5-2-2025-12-11

- Credentials can be supplied via environment variables (recommended) or Java system properties (-D...).
  See gw.properties, gw.bat, and gw.sh for examples.


3) How to Run

Basic usage:

Windows (cmd.exe):
  gw.bat src

Unix/macOS:
  ./gw.sh src

Directly with Java:
  java -jar gw.jar src

Configuration via environment variables (examples):

Windows (cmd.exe):
  set OPENAI_API_KEY=your_openai_api_key
  set OPENAI_BASE_URL=https://your-openai-compatible-endpoint
  gw.bat src

Unix/macOS:
  export OPENAI_API_KEY=your_openai_api_key
  export OPENAI_BASE_URL=https://your-openai-compatible-endpoint
  ./gw.sh src

Configuration via Java system properties (-D) (examples):

Windows (cmd.exe):
  java -DOPENAI_API_KEY=your_openai_api_key -jar gw.jar src

Unix/macOS:
  java -DOPENAI_API_KEY=your_openai_api_key -jar gw.jar src

Common CLI options:
- -h, --help
  Show help message and exit.

- -r, --root <path>
  Root directory used as the base for scan path validation and project-relative resolution.
  If not set: root from gw.properties; otherwise user directory.

- -t, --threads [true|false]
  Enable multi-threaded processing. If present with no value, enables threading.
  Default comes from gw.properties key `threads` (default false if not set).

- -a, --genai <provider:model>
  GenAI provider and model identifier (example: OpenAI:gpt-5.1).
  Default is OpenAI:gpt-5-mini (or `genai` from gw.properties).

- -i, --instructions [text | URL | file:...]
  System instructions appended to each prompt.
  If used without a value, Ghostwriter reads multi-line input from stdin until EOF.
  Input lines may include http(s)://... to include remote content and file:... to include file content.

- -g, --guidance [text | URL | file:...]
  Default guidance used when embedded guidance is absent; may also be applied as a final step for a matched directory.
  If used without a value, Ghostwriter reads multi-line input from stdin until EOF.
  Input lines may include http(s)://... to include remote content and file:... to include file content.

- -e, --excludes <comma-separated list>
  Exclude paths or patterns from processing. Provide a comma-separated list (repeatable).

- -l, --logInputs
  Log composed LLM request inputs to dedicated log files.

Scan target examples (from built-in help):

Windows (cmd.exe):
  java -jar gw.jar C:\projects\project
  java -jar gw.jar src\project
  java -jar gw.jar "glob:**/*.java"
  java -jar gw.jar "regex:^.*\\/[^\\/]+\\.java$"

Example run with explicit configuration:

Windows (cmd.exe):
  java -jar gw.jar src -r C:\projects\project -a "OpenAI:gpt-5-mini" -t -e ".machai,target" -l

Unix/macOS:
  ./gw.sh src -r /path/to/project -a "OpenAI:gpt-5-mini" -t -e ".machai,target" -l

Notes on `--instructions` and `--guidance`:
- Use --instructions to add system-level instructions that apply to every processed file.
- Use --guidance as a fallback when files do not contain embedded `@guidance` directives.
- Both options support composing content from plain text, stdin multi-line input, URLs, and file: references.


4) Troubleshooting & Support

Common issues:
- Authentication errors:
  - Ensure the correct credentials are set for the provider you selected (CodeMie username/password or OPENAI_API_KEY).
  - If using an OpenAI-compatible service, ensure OPENAI_BASE_URL is correct.

- Nothing happens / no files changed:
  - Verify your scan target points at the intended folder/file/pattern.
  - Confirm target files include embedded `@guidance` directives or provide fallback guidance via --guidance.
  - Ensure exclusions are not filtering out your targets.

- Scan path safety errors:
  - If using an absolute path, it must be within the configured --root directory.

Logs / debugging:
- Use --logInputs (-l) to log composed LLM request inputs per processed file.
- Increase diagnostic output by re-running with a smaller scan target to isolate problematic files and confirm configuration.


5) Contact & Documentation

Further documentation:
- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
