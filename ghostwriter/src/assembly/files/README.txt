Ghostwriter CLI
===============

Application Overview
--------------------
Ghostwriter is a Java-based guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.

It scans an entire repository (source code, tests, documentation, and other relevant assets), discovers embedded `@guidance:` directives, and turns them into actionable prompts executed against a configured GenAI provider.

Key features:
- Repository-wide scanning by directory, `glob:` patterns, or `regex:` patterns.
- Per-file-type reviewers extract embedded `@guidance:` directives.
- Adds project structure context to prompts.
- Supports system instructions and default guidance from plain text, URLs, or `file:` references.
- Supports excludes (exact paths or `glob:` / `regex:` patterns).
- Optional multi-threaded processing.
- Optional logging of provider request inputs per processed file.
- “Act mode” (`--act`) for executing predefined prompts.

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI-style endpoints)


Installation Instructions
-------------------------
Prerequisites:
- Java 8.
- A configured GenAI provider/model via `gw.model` or the CLI `-m/--model` option.
- (Optional) A `gw.properties` file to persist configuration.

Download:
- https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Build from source (Maven):
- Run a Maven build and use the generated CLI artifact/JAR.

Configuration file loading:
- Default config file name: `gw.properties`.
- Override config file path with Java system property:
  - `-Dgw.config=path\to\gw.properties`
- Set the Ghostwriter “home” directory (base used to resolve the config file) with:
  - `-Dgw.home=path\to\dir`


How to Run
----------
Basic usage:

```bash
java -jar gw.jar <scanDir> -m OpenAI:gpt-5.1
```

`<scanDir>` may be:
- A directory path (relative to the current project directory)
- A `glob:` matcher, e.g. `"glob:**/*.md"`
- A `regex:` matcher, e.g. `"regex:^.*/[^/]+\\.java$"`

From the built-in help:
- If an absolute scan path is provided, it must be located within the root project directory.
- If `-i/--instructions`, `-g/--guidance`, or `-a/--act` is used without a value, Ghostwriter reads multi-line input from stdin.
- When entering multi-line input, end input when a line does not end with `\` (a trailing backslash continues the next line).

Windows examples (.bat):

```bat
REM Scan a folder
java -jar gw.jar src -m OpenAI:gpt-5.1

REM Scan by glob
java -jar gw.jar "glob:**/*.java" -m OpenAI:gpt-5.1

REM Provide default guidance from stdin and enable input logging
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

REM Use an explicit root directory
java -jar gw.jar src -r C:\projects\my-repo -m OpenAI:gpt-5.1
```

Unix examples (.sh):

```sh
# Scan a folder
java -jar gw.jar src -m OpenAI:gpt-5.1

# Scan by glob
java -jar gw.jar "glob:**/*.java" -m OpenAI:gpt-5.1

# Provide default guidance from stdin and enable input logging
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

# Use an explicit root directory
java -jar gw.jar src -r /path/to/my-repo -m OpenAI:gpt-5.1
```

Setting configuration:
- Provider credentials are typically configured via environment variables (e.g., `OPENAI_API_KEY`, `OPENAI_BASE_URL`, `GENAI_USERNAME`, `GENAI_PASSWORD`).
- Ghostwriter runtime overrides use Java system properties (e.g., `-Dgw.config=...`, `-Dgw.home=...`).

Configuration properties (from `org.machanism.machai.gw.processor.Ghostwriter`):

- `gw.config` (Java system property)
  - Description: Overrides the Ghostwriter configuration file path.
  - Default: `gw.properties` (resolved under `gw.home`).

- `gw.home` (Java system property)
  - Description: Ghostwriter home directory used as the execution base for resolving the config file.
  - Default: if set, that value; otherwise the configured `gw.rootDir` (if provided); otherwise current working directory.

- `gw.rootDir`
  - Description: Root directory for file processing.
  - Default: current working directory.
  - Usage context: used by the processor as the root for scanning and to resolve `file:` references.

- `gw.model`
  - Description: GenAI provider and model identifier.
  - Default: none (required; Ghostwriter fails fast if missing).
  - Example: `OpenAI:gpt-5.1`.

- `gw.instructions`
  - Description: Optional system instructions. Supports plain text plus line-based URL and `file:` inclusion.
  - Default: not set.

- `gw.excludes`
  - Description: Comma-separated scan exclusions.
  - Default: not set.

- `gw.guidance`
  - Description: Default guidance applied when a file does not contain embedded `@guidance:` directives; may also perform a folder-level step (processor-dependent).
  - Default: not set.

- `gw.threads`
  - Description: Degree of concurrency for processing.
  - Default: not set.

- `gw.logInputs`
  - Description: Log provider request inputs to dedicated log files.
  - Default: `false`.

- `gw.scanDir`
  - Description: Default scan directory/pattern used when no `<scanDir>` argument is provided.
  - Default: not set; if absent, the CLI falls back to scanning the current working directory.

CLI options (from `Ghostwriter`):

- `-h, --help`
  - Show help message and exit.

- `-r, --root <path>`
  - Root directory for file processing.
  - Default: `gw.rootDir` or current working directory.

- `-t, --threads <count>`
  - Degree of concurrency for processing.
  - Default: `gw.threads` or unset.

- `-m, --model <provider:model>`
  - Set the GenAI provider and model.
  - Default: `gw.model`.

- `-i, --instructions[=<text|url|file:...>]`
  - System instructions (plain text, URL, or `file:`).
  - Default: `gw.instructions`.
  - Notes: if used without a value, reads multi-line stdin until a line does not end with `\`.

- `-g, --guidance[=<text|url|file:...>]`
  - Default guidance (plain text, URL, or `file:`).
  - Default: `gw.guidance`.
  - Notes: if used without a value, reads multi-line stdin until a line does not end with `\`.

- `-e, --excludes <csv>`
  - Comma-separated list of directories to exclude from processing.
  - Default: `gw.excludes`.

- `-l, --logInputs`
  - Log LLM request inputs to dedicated log files.
  - Default: `false` (`gw.logInputs`).

- `-as, --acts <path>`
  - Directory containing predefined act prompt files.
  - Default: not set.

- `-a, --act[=<...>]`
  - Act mode (interactive execution of predefined prompts).
  - Default: disabled.
  - Notes: if used without a value, reads multi-line stdin until a line does not end with `\`.


Troubleshooting & Support
-------------------------
Common issues:
- Missing model/provider:
  - Set `gw.model` in `gw.properties` or pass `-m/--model`.

- Authentication errors:
  - For CodeMie: set `GENAI_USERNAME` and `GENAI_PASSWORD`.
  - For OpenAI-compatible providers: set `OPENAI_API_KEY` (and optionally `OPENAI_BASE_URL`).

- Files not being scanned:
  - Verify `<scanDir>` (or `gw.scanDir`) points to the correct folder/pattern.
  - Check `gw.excludes` / `-e` for unintended exclusions.

Logs and debug:
- Enable input logging with `-l/--logInputs` or `gw.logInputs=true` to capture provider request inputs.
- Standard logging output is emitted to the console via SLF4J.


Contact & Documentation
-----------------------
- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
