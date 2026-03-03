Ghostwriter CLI

Application Overview
- Ghostwriter is a guided, project-wide file processing engine for generating and maintaining documentation and code improvements with AI.
- It scans your repository (source, tests, docs, and other assets), extracts embedded "@guidance:" directives, and turns them into prompts executed by a configured GenAI provider.
- Typical use cases:
  - Generate/refresh documentation (e.g., README files, project site pages)
  - Apply consistent improvements across many files driven by local guidance blocks
  - Run repeatable repo-scale review/update passes in scripted runs or CI
- Supported GenAI providers:
  - CodeMie
  - OpenAI-compatible services (including OpenAI)

Installation Instructions
Prerequisites
- Java 8 (required)
- A configured provider/model (required): set gw.model or pass -m/--model (e.g., OpenAI:gpt-5.1)
- (Optional) gw.properties to persist configuration

Download / Install
- Obtain the Ghostwriter distribution (gw.jar plus example configuration). If you build from source, produce the CLI jar and run it with Java.
- Place gw.properties alongside gw.jar (or set -Dgw.config=... to point to it).

How to Run
Basic usage
- The CLI entry point is: org.machanism.machai.gw.processor.Ghostwriter
- Command shape:
  java -jar gw.jar <scanDir> [options]

Scan target (<scanDir>)
- May be:
  - A directory path (relative to the current project directory), or
  - A Java PathMatcher expression using:
    - glob: (e.g., "glob:**/*.md")
    - regex: (e.g., "regex:^.*/[^/]+\\.java$")
- If an absolute path is provided, it must be located within the root project directory.

Configuration properties (from Ghostwriter.java)
- gw.config (System property)
  - Description: Override the configuration file path/name used by the CLI.
  - Default: gw.properties (in the resolved home directory)
  - Usage: -Dgw.config=path\to\gw.properties

- gw.home (System property)
  - Description: Sets the Ghostwriter home directory used as the execution base for relative configuration files.
  - Default: If not set, uses --root if provided; otherwise current working directory.
  - Usage: -Dgw.home=path\to\home

- gw.rootDir
  - Description: Root directory for file processing.
  - Default: Current working directory (if not set and not provided via --root)
  - Usage: in gw.properties or via -r/--root

- gw.model
  - Description: GenAI provider and model in the form provider:model.
  - Default: none (required; Ghostwriter fails fast if missing)
  - Usage: set in gw.properties or pass -m/--model

- gw.instructions
  - Description: Optional system instructions. Value may be plain text, URL(s), or file: references.
  - Default: not set
  - Usage: in gw.properties or via -i/--instructions

- gw.guidance
  - Description: Default guidance applied when a file has no embedded @guidance: directives.
  - Default: not set
  - Usage: in gw.properties or via -g/--guidance

- gw.excludes
  - Description: Comma-separated list of directories/files/patterns to exclude.
  - Default: not set
  - Usage: in gw.properties or via -e/--excludes

- gw.threads
  - Description: Enable multi-threaded module processing.
  - Default: false
  - Usage: in gw.properties or via -t/--threads[=<true|false>]

- gw.logInputs
  - Description: Log LLM request inputs to dedicated log files.
  - Default: false
  - Usage: in gw.properties or via -l/--logInputs

- gw.scanDir
  - Description: Default scan directory/pattern when no <scanDir> argument is provided.
  - Default: not set (falls back to current working directory)
  - Usage: in gw.properties

How to set configuration
- Java system properties (for JVM-level settings):
  - -Dgw.config=... (override properties file)
  - -Dgw.home=... (set home directory)
- gw.properties file:
  - Set gw.* keys (see src/assembly/files/gw.properties for an example)
- CLI options override configuration file values where applicable.

Command-line options
- -h, --help
  - Show help message and exit.

- -r, --root <path>
  - Root directory for file processing.
  - Default: gw.rootDir or current working directory.

- -t, --threads[=<true|false>]
  - Enable multi-threaded processing.
  - Default: false (gw.threads). If provided with no value, enables it.

- -m, --model <provider:model>
  - Set GenAI provider and model.
  - Default: gw.model (required if not set elsewhere).

- -i, --instructions[=<text|url|file:...>]
  - Provide system instructions.
  - If used without a value, Ghostwriter reads multi-line text from stdin until EOF.

- -g, --guidance[=<text|url|file:...>]
  - Provide default guidance.
  - If used without a value, Ghostwriter reads multi-line text from stdin until EOF.

- -e, --excludes <csv>
  - Comma-separated list of excludes.

- -l, --logInputs
  - Log LLM request inputs to dedicated log files.

- -as, --acts <path>
  - Directory containing predefined act prompt files.

- -a, --act[=<name and prompt>]
  - Run in Act mode (interactive execution of predefined prompts).
  - If used without a value, Ghostwriter reads the action from stdin until EOF.

Examples
Windows (cmd.exe)
- Scan a directory:
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Scan with a glob pattern:
  java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

- Provide instructions from a file and exclude folders:
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i file:instructions.txt -e target,.git

- Use a specific properties file:
  java -Dgw.config=src\assembly\files\gw.properties -jar gw.jar src

Unix (sh)
- Scan a directory:
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Scan with a glob pattern:
  java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

- Provide instructions from a file and exclude folders:
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i file:instructions.txt -e target,.git

Troubleshooting & Support
- "No GenAI provider/model configured":
  - Set gw.model in gw.properties or pass -m/--model (e.g., OpenAI:gpt-5.1).

- Authentication errors:
  - Ensure required provider environment variables are set (see gw.properties example):
    - CodeMie: GENAI_USERNAME / GENAI_PASSWORD
    - OpenAI-compatible: OPENAI_API_KEY (and optionally OPENAI_BASE_URL)

- Missing or unexpected scan results:
  - Verify <scanDir> (directory vs glob:/regex: matcher).
  - Check excludes (gw.excludes / -e).
  - Confirm --root points at the intended repository root.

- Logs and debugging:
  - Standard logs are emitted via SLF4J.
  - If -l/--logInputs (or gw.logInputs=true) is enabled, Ghostwriter writes provider input payloads to dedicated log files for each processed file.

Contact & Documentation
- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
