Ghostwriter CLI

Application Overview

Machai Ghostwriter is a guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.

Ghostwriter scans an entire repository (source code, tests, documentation, and other relevant assets), extracts embedded "@guidance:" directives, and turns them into actionable prompts for a configured GenAI provider. Its conceptual foundation is Guided File Processing:
  https://www.machanism.org/guided-file-processing/index.html

Key features
- Repository-aware scanning of directories and patterns:
  - directory paths
  - glob: patterns (e.g., glob:**/*.java)
  - regex: patterns
- Per-file-type reviewers extract embedded "@guidance:" directives.
- Injects project structure context into provider prompts.
- Optional system instructions and default guidance (plain text, URLs, or file: references).
- Excludes support (exact paths or glob:/regex: patterns, depending on processor implementation).
- Optional multi-threaded processing.
- Optional logging of provider inputs per processed file.
- Act mode for executing predefined prompts (--act).

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (via provider/model selection, e.g., OpenAI:gpt-5.1)


Installation Instructions

Prerequisites
- Java 8
- A configured GenAI provider/model:
  - Property: gw.model
  - Or CLI option: -m / --model
- Credentials for your selected provider, for example:
  - CodeMie:
    - GENAI_USERNAME
    - GENAI_PASSWORD
  - OpenAI-compatible:
    - OPENAI_API_KEY
    - (Optional) OPENAI_BASE_URL
- (Optional) A configuration file:
  - gw.properties
  - You may override the configuration file path with: -Dgw.config=...

Build / install
- If you are building from source, build the project using Maven in the project root.
- If you downloaded the distribution, place the included gw.properties alongside the CLI or in the configured Ghostwriter home directory.

Configuration file location
- Default configuration file name: gw.properties
- Ghostwriter home directory resolution:
  1) -Dgw.home=<path> if set
  2) otherwise, projectDir (if provided)
  3) otherwise, current working directory
- Override config file name/path (relative to gw.home):
  -Dgw.config=gw.properties
  -Dgw.config=conf/gw.properties


How to Run

Basic usage
- The CLI entry point is:
  org.machanism.machai.gw.processor.Ghostwriter

- Typical jar invocation:
  java -jar gw.jar <scanDir> [options]

scanDir rules
- <scanDir> may be:
  - a relative directory path (relative to the current project directory)
  - a glob: matcher
  - a regex: matcher
- If an absolute scan path is provided, it must be located within the root project directory.

Windows examples (.bat / cmd)
- Scan a directory:
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Scan by glob pattern:
  java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1

- Scan by regex pattern:
  java -jar gw.jar "regex:^.*/[^/]+\\.java$" -m OpenAI:gpt-5.1

- Provide default guidance via stdin (end input when a line does not end with "\"):
  java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

Unix examples (.sh)
- Scan a directory:
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Scan by glob pattern:
  java -jar gw.jar 'glob:**/*.md' -m OpenAI:gpt-5.1

- Scan by regex pattern:
  java -jar gw.jar 'regex:^.*/[^/]+\\.java$' -m OpenAI:gpt-5.1

Setting configuration via Java system properties
- Override config file path:
  -Dgw.config=<path>
- Set Ghostwriter home directory:
  -Dgw.home=<path>

Setting configuration via gw.properties
- Example file is included in this folder: gw.properties
- Common properties:
  - project.dir
  - gw.model
  - gw.instructions
  - gw.guidance
  - gw.excludes
  - gw.threads
  - logInputs
  - gw.scanDir

Configuration properties (from org.machanism.machai.gw.processor.Ghostwriter)

1) project.dir
- Description: Root directory for file processing when -d/--projectDir is not provided.
- Default: Current working directory (SystemUtils.getUserDir()) if not set.
- Usage: Determines the root directory used for scanning and for validating absolute scan paths.

2) gw.model
- Description: GenAI provider and model to use, in the form provider:model (e.g., OpenAI:gpt-5.1).
- Default: No default; the CLI fails fast if not configured.
- Usage:
  - Set in gw.properties, or override via -m/--model.

3) gw.instructions
- Description: Optional system instructions provided to the GenAI provider.
- Default: null (unset).
- Usage:
  - Set in gw.properties.
  - Override with -i/--instructions.
  - If -i/--instructions is used without a value, Ghostwriter reads multi-line instructions from stdin.
  - Each line is processed:
    - blank lines preserved
    - http:// or https:// lines are fetched and included
    - file: lines are loaded from the referenced file path
    - other lines are included as-is
  - Multi-line stdin input ends when a line does not end with "\".

4) gw.guidance
- Description: Default guidance applied when embedded @guidance directives are not present (also used as the processor default prompt).
- Default: null (unset).
- Usage:
  - Set in gw.properties.
  - Override with -g/--guidance.
  - If -g/--guidance is used without a value, Ghostwriter reads multi-line guidance from stdin.
  - Lines are processed the same way as gw.instructions.

5) gw.excludes
- Description: Comma-separated list of directories/files to exclude from processing.
- Default: null (unset).
- Usage:
  - Set in gw.properties.
  - Override with -e/--excludes.

6) gw.threads
- Description: Degree of concurrency for processing (worker thread count).
- Default: unset (no multi-threading configured).
- Usage:
  - Set in gw.properties.
  - Override with -t/--threads <count>.

7) logInputs
- Description: Enable logging of LLM request inputs to dedicated log files.
- Default: false.
- Usage:
  - Set in gw.properties (logInputs=true).
  - Or enable via -l/--logInputs.

8) gw.scanDir
- Description: Default scan directory/pattern if no <scanDir> argument is provided.
- Default:
  - If unset and no scanDir args are provided, Ghostwriter uses the current working directory absolute path.
- Usage:
  - Set in gw.properties to avoid passing <scanDir> every run.

Additional system properties
- gw.config
  - Description: Override the Ghostwriter configuration file path (resolved relative to gw.home).
  - Default: gw.properties
  - Usage: java -Dgw.config=conf/gw.properties -jar gw.jar ...

- gw.home
  - Description: Home directory used to locate gw.properties by default.
  - Default: projectDir if provided, else current working directory.
  - Usage: java -Dgw.home=. -jar gw.jar ...

CLI options summary
- -h, --help
  - Show help and exit.

- -d, --projectDir <path>
  - Root directory for file processing.

- -t, --threads <count>
  - Degree of concurrency for processing.

- -m, --model <provider:model>
  - GenAI provider/model selection.

- -i, --instructions[=<text|url|file:...>]
  - System instructions.
  - If used without a value, reads multi-line stdin, continuing lines ending with "\".

- -g, --guidance[=<text|url|file:...>]
  - Default guidance.
  - If used without a value, reads multi-line stdin, continuing lines ending with "\".

- -e, --excludes <csv>
  - Comma-separated excludes.

- -l, --logInputs
  - Log provider input payloads per processed file.

- -as, --acts <path>
  - Directory containing predefined act prompt files.

- -a, --act[=<...>]
  - Act mode. If used without a value, reads multi-line stdin, continuing lines ending with "\".

Default guidance notes
- Default guidance (gw.guidance / -g) is used when a file contains no embedded "@guidance:" directives.
- Default guidance can also be used as a centralized, repeatable instruction set for a scan run.


Troubleshooting & Support

Common issues
- "No GenAI provider/model configured"
  - Set gw.model in gw.properties, or pass -m/--model.

- Authentication errors
  - Ensure provider credentials are set (e.g., GENAI_USERNAME/GENAI_PASSWORD for CodeMie, or OPENAI_API_KEY for OpenAI-compatible providers).

- "Scan dir" issues
  - If providing an absolute scan path, ensure it is inside the configured root project directory.
  - Prefer quoting glob:/regex: expressions.

Logs and debug
- Standard logs are written via SLF4J (logging backend configuration depends on the runtime classpath).
- Provider usage is logged at the end of execution.
- If -l/--logInputs is enabled (or logInputs=true), Ghostwriter writes dedicated log files containing provider request inputs.


Contact & Documentation

Project site
- https://machai.machanism.org/ghostwriter/index.html

Source repository
- https://github.com/machanism-org/machai

Maven Central
- https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
