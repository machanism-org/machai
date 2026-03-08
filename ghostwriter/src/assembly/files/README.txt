Ghostwriter CLI

1) Application Overview

Ghostwriter is a guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.

It scans a repository (source code, tests, docs, and other assets), extracts embedded `@guidance:` directives, and turns them into actionable prompts for a configured GenAI provider.

Key features:
- Scan directories or match patterns using `glob:` / `regex:`.
- Per-file-type guidance extraction via reviewers keyed by file extension.
- Adds project structure/layout context into provider prompts.
- Supports system instructions and default guidance from plain text, URLs, or `file:` references.
- Supports excludes (exact paths or `glob:` / `regex:` patterns).
- Optional multi-threaded module processing.
- Optional logging of provider request inputs per processed file.
- “Act mode” for executing predefined prompts (`--act`).

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI)


2) Installation Instructions

Prerequisites
- Java 8
- A configured GenAI provider/model (set `gw.model` in `gw.properties` or pass `-m/--model`).
- (Optional) `gw.properties` to persist configuration (see `gw.properties` in this folder).

Download / Build
- Download the packaged CLI (see the project site resources), or build from source using Maven.
- Place a `gw.properties` file alongside the executable OR point to it via `-Dgw.config=...`.


3) How to Run

Basic syntax
  java -jar gw.jar <scanDir> [options]

Scan target (`<scanDir>`) forms
- Relative path with respect to the current project directory.
- Absolute path, but it must be located within the root project directory.
- Pattern matchers:
  - `glob:` patterns (example: "glob:**/*.java")
  - `regex:` patterns (example: "regex:^.*/[^/]+\\.java$")

Configuration properties (from `org.machanism.machai.gw.processor.Ghostwriter`)

- `gw.config`
  - Description: System property that overrides the Ghostwriter configuration file path.
  - Default: `gw.properties` (resolved under `gw.home`).
  - Usage context: choose a non-default properties file.
  - Example:
      -Dgw.config=path\\to\\gw.properties

- `gw.home`
  - Description: System property indicating the Ghostwriter home directory; used to resolve `gw.properties`.
  - Default: if set, use `gw.home`; else use `gw.rootDir` if provided; else `user.dir`.
  - Usage context: control where the CLI looks for `gw.properties`.
  - Example:
      -Dgw.home=.

- `gw.rootDir`
  - Description: Root directory for file processing.
  - Default: `user.dir` (current working directory).
  - Usage context: sets the project root used for scanning and path resolution.

- `gw.model`
  - Description: GenAI provider/model in `provider:model` form.
  - Default: none (required).
  - Usage context: selects which provider and model to use.
  - Examples:
      OpenAI:gpt-5.1
      CodeMie:gpt-5-2-2025-12-11

- `gw.instructions`
  - Description: Optional system instructions (plain text, URL, or `file:` reference).
  - Default: not set.
  - Usage context: influences all provider calls as system instructions.
  - Parsing rules (processed line-by-line):
    - Blank lines are preserved.
    - Lines starting with `http://` or `https://` are loaded from the URL.
    - Lines starting with `file:` load content from the specified file.
    - Other lines are used as-is.

- `gw.guidance`
  - Description: Default guidance applied when a file has no embedded `@guidance:` directives.
  - Default: not set.
  - Usage context: provides a fallback prompt and may trigger a folder-level step depending on the processor.
  - Parsing rules: same as `gw.instructions`.

- `gw.excludes`
  - Description: Comma-separated list of directories/files to exclude from processing.
  - Default: not set.
  - Usage context: omit paths from scanning.

- `gw.threads`
  - Description: Enable/disable multi-threaded module processing.
  - Default: `false`.
  - Usage context: improve performance on multi-module projects.

- `gw.logInputs`
  - Description: Enable logging of provider request inputs to dedicated log files.
  - Default: `false`.
  - Usage context: troubleshooting; review the exact provider inputs.

- `gw.scanDir`
  - Description: Default scan target if no `<scanDir>` arguments are provided.
  - Default: not set (if unset, Ghostwriter falls back to `user.dir`).
  - Usage context: convenient default scan configuration.

Command-line options (from `Ghostwriter`)
- `-h, --help`
  - Show help message and exit.

- `-r, --root <path>`
  - Root directory for file processing.
  - Default: `gw.rootDir` or `user.dir`.

- `-t, --threads[=<true|false>]`
  - Enable multi-threaded processing.
  - Default: `false`.
  - If provided with no value, threading is enabled.

- `-m, --model <provider:model>`
  - Set the GenAI provider and model.
  - Default: `gw.model`.

- `-i, --instructions[=<text|url|file:...>]`
  - Provide system instructions.
  - Default: `gw.instructions`.
  - If used without a value, Ghostwriter reads multi-line input from stdin using `\\` as a line-continuation marker (input ends when a line does not end with `\\`).

- `-g, --guidance[=<text|url|file:...>]`
  - Provide default guidance.
  - Default: `gw.guidance`.
  - If used without a value, Ghostwriter reads multi-line input from stdin using `\\` as a line-continuation marker (input ends when a line does not end with `\\`).

- `-e, --excludes <csv>`
  - Comma-separated list of exclusions.
  - Default: `gw.excludes`.

- `-l, --logInputs`
  - Log provider request inputs to dedicated log files.
  - Default: `false` (or `gw.logInputs=false`).

- `-as, --acts <path>`
  - Directory containing predefined act prompt files.
  - Default: not set.

- `-a, --act[=<...>]`
  - Run in Act mode: interactive mode for executing predefined prompts.
  - Default: disabled.
  - If used without a value, Ghostwriter reads the act prompt from stdin using `\\` as a line-continuation marker.

How to set environment variables and Java system properties
- Java system properties use `-Dkey=value`:
  - `-Dgw.config=...`
  - `-Dgw.home=...`
- Provider credentials are usually supplied via environment variables (examples are included in `gw.properties`):
  - CodeMie: `GENAI_USERNAME`, `GENAI_PASSWORD`
  - OpenAI-compatible: `OPENAI_API_KEY`, optional `OPENAI_BASE_URL`

Examples

Windows (cmd.exe)
- Basic scan:
    java -jar gw.jar src -m OpenAI:gpt-5.1

- Glob scan:
    java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1

- Default guidance from stdin (end input when a line does not end with a trailing backslash):
    java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g

- Use configuration file override:
    java -Dgw.config=src\\assembly\\files\\gw.properties -jar gw.jar src

Unix (sh)
- Basic scan:
    java -jar gw.jar src -m OpenAI:gpt-5.1

- Glob scan:
    java -jar gw.jar 'glob:**/*.java' -m OpenAI:gpt-5.1

- Default guidance from stdin:
    java -jar gw.jar 'glob:**/*.md' -m OpenAI:gpt-5.1 -g

- Use configuration file override:
    java -Dgw.config=src/assembly/files/gw.properties -jar gw.jar src


4) Troubleshooting & Support

Common issues
- No model/provider configured
  - Symptom: "No GenAI provider/model configured..."
  - Fix: set `gw.model` in `gw.properties` or pass `-m/--model`.

- Authentication errors
  - Ensure the provider credentials are set (e.g., CodeMie username/password or `OPENAI_API_KEY`).
  - If using an OpenAI-compatible provider, verify `OPENAI_BASE_URL`.

- Scan finds no files
  - Verify `<scanDir>` is correct and located under the configured root directory.
  - For patterns, ensure you include the `glob:` or `regex:` prefix.

Logs / debug
- Use `-l/--logInputs` to write LLM request inputs to dedicated log files.
- Standard logs are emitted via the configured SLF4J backend.


5) Contact & Documentation

- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
