Ghostwriter CLI

Application Overview
- Ghostwriter is a guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.
- It scans a repository (source, tests, docs, and other assets), extracts embedded @guidance: directives, and turns them into actionable prompts for a configured GenAI provider.
- Key features:
  - Scan directories or match patterns using glob: / regex:
  - Per-file-type guidance extraction via reviewer registry (keyed by file extension)
  - Adds project layout/context into provider prompts
  - Optional system instructions and default guidance (text, URL, or file:)
  - Excludes via exact paths or glob:/regex: patterns
  - Optional multi-threaded module processing
  - Optional logging of provider request inputs per processed file
  - Act mode for executing predefined prompts (--act)
- Supported GenAI providers:
  - CodeMie
  - OpenAI-compatible services (including OpenAI)

Installation Instructions
Prerequisites
- Java 8
- A configured GenAI provider/model (gw.model) or a CLI override (-m/--model)
- (Optional) A gw.properties file to persist configuration

Install / Build
- Download the packaged CLI (see project resources) or build from source using Maven.
- Provide a gw.properties file alongside the executable (or point to it with -Dgw.config=...).

How to Run
Basic usage
- Syntax:
  java -jar gw.jar <scanDir> [options]

Scan targets
- <scanDir> may be:
  - A directory path (relative to the current project directory), OR
  - A glob: pattern (e.g., "glob:**/*.java"), OR
  - A regex: pattern (e.g., "regex:^.*/[^/]+\\.java$")

Configuration properties (from org.machanism.machai.gw.processor.Ghostwriter)
- gw.config
  - Description: System property that overrides the Ghostwriter configuration file path.
  - Default: gw.properties (resolved under gw.home)
  - Usage: -Dgw.config=path\to\gw.properties

- gw.home
  - Description: System property indicating the Ghostwriter home directory; used to resolve gw.properties and relative config file references.
  - Default: gw.rootDir if provided; otherwise current working directory (user.dir)
  - Usage: -Dgw.home=path\to\home

- gw.rootDir
  - Description: Root directory for file processing.
  - Default: current working directory (user.dir)
  - Usage: set in gw.properties or via -r/--root

- gw.model
  - Description: GenAI provider/model in provider:model form.
  - Default: none (required)
  - Usage: set in gw.properties or via -m/--model (e.g., OpenAI:gpt-5.1)

- gw.instructions
  - Description: Optional system instructions (plain text, URL, or file: reference). Parsed line-by-line:
    - Blank lines preserved
    - http:// or https:// lines are fetched from the URL
    - file: lines load content from a file (relative paths resolve from the configured root directory)
    - Other lines used as-is
  - Default: not set
  - Usage: set in gw.properties or via -i/--instructions

- gw.guidance
  - Description: Default guidance applied when a file has no embedded @guidance: directives; may also trigger a folder-level step depending on processor behavior. Same parsing rules as gw.instructions.
  - Default: not set
  - Usage: set in gw.properties or via -g/--guidance

- gw.excludes
  - Description: Comma-separated list of directories/files to exclude from processing.
  - Default: not set
  - Usage: set in gw.properties or via -e/--excludes

- gw.threads
  - Description: Enable/disable multi-threaded module processing.
  - Default: false
  - Usage: set in gw.properties or via -t/--threads[=<true|false>] (if provided with no value, enables)

- gw.logInputs
  - Description: Enable logging of provider request inputs to dedicated log files.
  - Default: false
  - Usage: set in gw.properties or via -l/--logInputs

- gw.scanDir
  - Description: Default scan target if no <scanDir> arguments are provided.
  - Default: not set (if also missing, defaults to current working directory)
  - Usage: set in gw.properties

Command-line options
- -h, --help
  - Show help message and exit.

- -r, --root <path>
  - Root directory for file processing.
  - Default: gw.rootDir or current working directory.

- -t, --threads[=<true|false>]
  - Enable multi-threaded processing (default: false).
  - If provided with no value, threading is enabled.

- -m, --model <provider:model>
  - Set GenAI provider and model (e.g., OpenAI:gpt-5.1).

- -i, --instructions[=<text|url|file:...>]
  - Provide system instructions.
  - If used without a value, Ghostwriter prompts for multi-line input from stdin.

- -g, --guidance[=<text|url|file:...>]
  - Provide default guidance.
  - If used without a value, Ghostwriter prompts for multi-line input from stdin.

- -e, --excludes <csv>
  - Comma-separated exclusions.

- -l, --logInputs
  - Log LLM request inputs to dedicated log files.

- -as, --acts <path>
  - Directory containing predefined act prompt files.

- -a, --act[=<name and prompt>]
  - Run in Act mode (interactive execution of predefined prompts).
  - If used without a value, Ghostwriter reads the action from stdin.

Setting environment variables and Java system properties
- Java system properties:
  - Use -Dkey=value
  - Examples:
    - -Dgw.config=gw.properties
    - -Dgw.home=.
- Provider credentials are typically provided via environment variables referenced by the provider implementation.
  - See src/assembly/files/gw.properties for example variables for CodeMie and OpenAI-compatible providers.

Examples
Windows (cmd.exe)
- Basic scan:
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Glob scan:
  java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1

- Default guidance from stdin (end input when a line does not end with a trailing backslash):
  java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g

- Use configuration file override:
  java -Dgw.config=src\assembly\files\gw.properties -jar gw.jar src

Unix (sh)
- Basic scan:
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Glob scan:
  java -jar gw.jar 'glob:**/*.java' -m OpenAI:gpt-5.1

- Default guidance from stdin:
  java -jar gw.jar 'glob:**/*.md' -m OpenAI:gpt-5.1 -g

- Use configuration file override:
  java -Dgw.config=src/assembly/files/gw.properties -jar gw.jar src

Troubleshooting & Support
- Missing model/provider:
  - Error: No GenAI provider/model configured
  - Fix: set gw.model in gw.properties or pass -m/--model.

- Authentication errors:
  - Ensure provider credentials are set (e.g., CodeMie username/password or OPENAI_API_KEY).
  - Confirm any base URL configuration for OpenAI-compatible services.

- Scan finds no files:
  - Verify <scanDir> is correct and rooted within the project root.
  - For patterns, ensure you include the glob: or regex: prefix.

- Enable more diagnostics / locate logs:
  - Use -l/--logInputs to write LLM request inputs to dedicated log files.
  - Standard logs are emitted via the configured SLF4J backend.

Contact & Documentation
- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
