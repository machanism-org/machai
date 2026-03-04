Ghostwriter CLI

================
Application Overview
================

Ghostwriter is a guided, project-wide file processing engine for generating and maintaining documentation and code improvements with AI.

It scans a repository (source code, tests, documentation, and other relevant assets), extracts embedded `@guidance:` directives, and turns them into actionable prompts executed by a configured GenAI provider.

Key features
- Repository-aware scanning (directory paths, `glob:` patterns, or `regex:` matchers)
- File-type reviewers that extract embedded `@guidance:` directives
- Project-structure context injected into prompts
- Optional system instructions and default guidance from plain text, URLs, or `file:` references
- Excludes via exact paths or `glob:` / `regex:` patterns
- Optional multi-threaded module processing
- Optional logging of provider inputs per processed file
- “Act mode” (`--act`) for executing predefined prompts

Typical use cases
- Generate or refresh project documentation (README files, site pages, etc.)
- Apply consistent repo-wide improvements driven by file-local guidance
- Run scripted or CI/CD-friendly review/update passes

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (including OpenAI)


========================
Installation Instructions
========================

Prerequisites
- Java 8
- A configured GenAI provider/model (required): `gw.model` or `-m/--model` (example: `OpenAI:gpt-5.1`)
- Optional: `gw.properties` to persist configuration

Download / Install
- Obtain the Ghostwriter distribution (for example: `gw.jar` plus `gw.properties`).
- Place `gw.properties` alongside `gw.jar`, or override its location with `-Dgw.config=...`.

Example `gw.properties`
- See: `src/assembly/files/gw.properties`


==========
How to Run
==========

Entry point
- `org.machanism.machai.gw.processor.Ghostwriter`

Basic usage

    java -jar gw.jar <scanDir> [options]

Scan target (`<scanDir>`)
- May be:
  - A directory path (relative to the current project directory), or
  - A Java PathMatcher expression:
    - `glob:` (example: `"glob:**/*.md"`)
    - `regex:` (example: `"regex:^.*/[^/]+\\.java$"`)
- If an absolute path is provided, it must be located within the root project directory.


-----------------------------------------
Configuration properties (Ghostwriter.java)
-----------------------------------------

Where settings can come from
- Java system properties (`-D...`) for JVM-level settings
- `gw.properties` for persistent configuration
- CLI options (override `gw.properties` where applicable)

Properties
- `gw.config` (system property)
  - Description: Override the configuration file path/name used by the CLI.
  - Default: `gw.properties` (in the resolved home directory)
  - Usage: `-Dgw.config=path\\to\\gw.properties`

- `gw.home` (system property)
  - Description: Sets the Ghostwriter home directory used as the execution base for relative configuration files.
  - Default: If not set, uses `--root` if provided; otherwise the current working directory.
  - Usage: `-Dgw.home=path\\to\\home`

- `gw.rootDir`
  - Description: Root directory for file processing.
  - Default: current working directory (if not set and not provided via `--root`)
  - Usage: `gw.rootDir=...` in `gw.properties` or `-r/--root`

- `gw.model`
  - Description: GenAI provider and model in the form `provider:model`.
  - Default: none (required; Ghostwriter fails fast if missing)
  - Usage: `gw.model=OpenAI:gpt-5.1` or `-m/--model OpenAI:gpt-5.1`

- `gw.instructions`
  - Description: Optional system instructions. Value may be plain text, URL(s), or `file:` references.
  - Default: not set
  - Usage: `gw.instructions=...` or `-i/--instructions`

- `gw.guidance`
  - Description: Default guidance applied when a file has no embedded `@guidance:` directives.
  - Default: not set
  - Usage: `gw.guidance=...` or `-g/--guidance`

- `gw.excludes`
  - Description: Comma-separated list of directories/files/patterns to exclude.
  - Default: not set
  - Usage: `gw.excludes=target,.git` or `-e/--excludes target,.git`

- `gw.threads`
  - Description: Enable multi-threaded module processing.
  - Default: `false`
  - Usage: `gw.threads=true` or `-t/--threads[=<true|false>]`

- `gw.logInputs`
  - Description: Log LLM request inputs to dedicated log files.
  - Default: `false`
  - Usage: `gw.logInputs=true` or `-l/--logInputs`

- `gw.scanDir`
  - Description: Default scan directory/pattern when no `<scanDir>` argument is provided.
  - Default: not set (falls back to current working directory)
  - Usage: `gw.scanDir=src`


--------------------------------
How to provide instructions/guidance
--------------------------------

`-i/--instructions` and `-g/--guidance` accept:
- Plain text (as the option value)
- A URL (lines beginning with `http://` or `https://`)
- A `file:` reference (lines beginning with `file:`)

If used without a value, Ghostwriter reads multi-line input from stdin using a trailing backslash (`\\`) to continue to the next line; input ends when a line does not end with `\\`.


--------------------
Command-line options
--------------------

- `-h, --help`
  - Show this help message and exit.

- `-r, --root <path>`
  - Specify the path to the root directory for file processing.
  - Default: `gw.rootDir` or current working directory.

- `-t, --threads[=<true|false>]`
  - Enable multi-threaded processing to improve performance.
  - Default: `false` (`gw.threads`). If provided with no value, enables it.

- `-m, --model <provider:model>`
  - Set the GenAI provider and model (example: `OpenAI:gpt-5.1`).
  - Default: `gw.model` (required if not set elsewhere).

- `-i, --instructions[=<text|url|file:...>]`
  - Specify system instructions as plain text, by URL, or by file path.
  - If used without a value, you will be prompted to enter instruction text via stdin.

- `-g, --guidance[=<text|url|file:...>]`
  - Specify default guidance as plain text, by URL, or by file path.
  - If used without a value, you will be prompted to enter the guidance text via stdin.

- `-e, --excludes <csv>`
  - Specify a comma-separated list of directories to exclude from processing.

- `-l, --logInputs`
  - Log LLM request inputs to dedicated log files.

- `-as, --acts <path>`
  - Specify the path to the directory containing predefined act prompt files.

- `-a, --act[=<...>]`
  - Run Ghostwriter in Act mode: an interactive mode for executing predefined prompts.
  - If used without a value, Ghostwriter reads the action from stdin.


--------
Examples
--------

Windows (cmd.exe)

- Scan a directory:

    java -jar gw.jar src -m OpenAI:gpt-5.1

- Scan with a glob pattern, provide guidance via stdin, and log inputs:

    java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

- Provide instructions from a file and exclude folders:

    java -jar gw.jar src -m OpenAI:gpt-5.1 -i file:instructions.txt -e target,.git

- Use a specific properties file:

    java -Dgw.config=src\\assembly\\files\\gw.properties -jar gw.jar src

Unix (sh)

- Scan a directory:

    java -jar gw.jar src -m OpenAI:gpt-5.1

- Scan with a glob pattern, provide guidance via stdin, and log inputs:

    java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

- Provide instructions from a file and exclude folders:

    java -jar gw.jar src -m OpenAI:gpt-5.1 -i file:instructions.txt -e target,.git


=========================
Troubleshooting & Support
=========================

Common issues
- "No GenAI provider/model configured":
  - Set `gw.model` in `gw.properties` or pass `-m/--model` (example: `OpenAI:gpt-5.1`).

- Authentication errors:
  - Ensure required provider environment variables are set (see `src/assembly/files/gw.properties`):
    - CodeMie: `GENAI_USERNAME` / `GENAI_PASSWORD`
    - OpenAI-compatible: `OPENAI_API_KEY` (and optionally `OPENAI_BASE_URL`)

- Missing or unexpected scan results:
  - Verify `<scanDir>` (directory vs `glob:`/`regex:` matcher).
  - Check excludes (`gw.excludes` / `-e`).
  - Confirm `--root` points at the intended repository root.

Logs and debugging
- Standard logs are emitted via SLF4J.
- If `-l/--logInputs` (or `gw.logInputs=true`) is enabled, Ghostwriter writes provider input payloads to dedicated log files for each processed file.


=======================
Contact & Documentation
=======================

- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
