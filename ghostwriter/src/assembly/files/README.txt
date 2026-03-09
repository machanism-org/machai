Ghostwriter CLI
===============

Application Overview
--------------------
Ghostwriter is a Java-based, guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.

It scans a repository (source code, tests, documentation, and other assets), discovers embedded `@guidance:` directives in files, and turns them into actionable prompts sent to a configured GenAI provider.

Key features:
- Repository-wide scanning by directory, `glob:` patterns, or `regex:` patterns.
- Per-file-type reviewers extract embedded `@guidance:` directives.
- Injects project structure context into prompts.
- Supports optional system instructions and default guidance from plain text, URLs, or `file:` references.
- Supports excludes (exact paths or `glob:` / `regex:` patterns).
- Optional multi-threaded module processing.
- Optional logging of provider inputs per processed file.
- “Act mode” (`--act`) for executing predefined prompts.

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI-style endpoints)


Installation Instructions
-------------------------
Prerequisites:
- Java 8 (as configured by `maven.compiler.release` in the project `pom.xml`).
- A configured GenAI provider/model via `gw.model` or the CLI `-m/--model` option.
- (Optional) A `gw.properties` file to persist configuration.

Download / Build:
- Download the packaged CLI bundle from:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

- Or build from source (Maven):
  - Build the project with Maven and use the resulting CLI artifact/jar.

Configuration file:
- By default, Ghostwriter loads configuration from `gw.properties`.
- Override the configuration file path with:
  - Java system property: `-Dgw.config=path\to\gw.properties`
- Set the Ghostwriter home directory (base for config resolution) with:
  - Java system property: `-Dgw.home=path\to\dir`


How to Run
----------
Basic usage:

```bash
java -jar gw.jar <scanDir> -m OpenAI:gpt-5.1
```

`<scanDir>` may be:
- A directory path (relative to the current project directory).
- A `glob:` matcher, e.g. `"glob:**/*.md"`.
- A `regex:` matcher, e.g. `"regex:^.*/[^/]+\\.java$"`.

If an absolute scan path is provided, it must be located within the root project directory.

Windows examples:

```bat
REM Scan a folder
java -jar gw.jar src -m OpenAI:gpt-5.1

REM Scan by glob
java -jar gw.jar "glob:**/*.java" -m OpenAI:gpt-5.1

REM Provide default guidance from stdin (end input when a line does not end with a trailing backslash)
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

REM Use an explicit root directory
java -jar gw.jar src -r C:\projects\my-repo -m OpenAI:gpt-5.1
```

Unix examples:

```sh
# Scan a folder
java -jar gw.jar src -m OpenAI:gpt-5.1

# Scan by glob
java -jar gw.jar "glob:**/*.java" -m OpenAI:gpt-5.1

# Provide default guidance from stdin (end input when a line does not end with a trailing backslash)
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

# Use an explicit root directory
java -jar gw.jar src -r /path/to/my-repo -m OpenAI:gpt-5.1
```

Setting configuration (environment variables vs. Java system properties):
- Environment variables are typically used by providers (for example `OPENAI_API_KEY`, `OPENAI_BASE_URL`, `GENAI_USERNAME`, `GENAI_PASSWORD`).
- Java system properties are used for Ghostwriter runtime configuration overrides (for example `-Dgw.config=...`, `-Dgw.home=...`).

Configuration properties (from `org.machanism.machai.gw.processor.Ghostwriter`):

- `gw.config` (system property)
  - Description: Override the Ghostwriter configuration file path.
  - Default: `gw.properties` (resolved under `gw.home` / home directory).
  - Usage: `java -Dgw.config=path\to\gw.properties -jar gw.jar ...`

- `gw.home` (system property)
  - Description: Ghostwriter home directory used as the execution base for relative configuration files.
  - Default: `-Dgw.home` value if set, else the configured `gw.rootDir` (if provided), else current working directory.
  - Usage: `java -Dgw.home=path\to\dir -jar gw.jar ...`

- `gw.rootDir`
  - Description: Root directory for file processing.
  - Default: current working directory.
  - Usage context: used to resolve scan paths and `file:` references.

- `gw.model`
  - Description: GenAI provider and model identifier.
  - Default: none (required; Ghostwriter fails fast if missing).
  - Usage context: provider selection (e.g., `OpenAI:gpt-5.1`, `CodeMie:...`).

- `gw.instructions`
  - Description: Optional system instructions. Supports plain text, URL lines, and `file:` references.
  - Default: not set.
  - Usage context: sets processor instructions.

- `gw.guidance`
  - Description: Default guidance applied when a file has no embedded `@guidance:` directives; may also run a folder-level step (processor-dependent).
  - Default: not set.
  - Usage context: sets processor default prompt.

- `gw.excludes`
  - Description: Comma-separated list of directories/files to exclude.
  - Default: not set.
  - Usage context: passed to the processor exclude matcher.

- `gw.threads`
  - Description: Enable multi-threaded module processing.
  - Default: `false`.
  - Usage context: sets processor module multithreading.

- `gw.logInputs`
  - Description: Log provider request inputs to dedicated log files.
  - Default: `false`.
  - Usage context: helps with debugging and auditing provider prompts.

- `gw.scanDir`
  - Description: Default scan directory/pattern used when no `<scanDir>` argument is provided.
  - Default: not set; if absent, Ghostwriter scans the current working directory.
  - Usage context: convenience default for automated runs.

CLI options (from `Ghostwriter`):
- `-h, --help`
  - Show help message and exit.

- `-r, --root <path>`
  - Root directory for file processing.
  - Default: `gw.rootDir` or current working directory.

- `-t, --threads[=<true|false>]`
  - Enable multi-threaded processing.
  - Default: `false`.
  - Notes: if provided with no value, threading is enabled.

- `-m, --model <provider:model>`
  - Set GenAI provider and model (required unless `gw.model` is set).

- `-i, --instructions[=<text|url|file:...>]`
  - Set system instructions.
  - Default: `gw.instructions`.
  - Notes: if used without a value, Ghostwriter reads multi-line stdin. Input ends when a line does not end with `\`.

- `-g, --guidance[=<text|url|file:...>]`
  - Set default guidance.
  - Default: `gw.guidance`.
  - Notes: if used without a value, Ghostwriter reads multi-line stdin. Input ends when a line does not end with `\`.

- `-e, --excludes <csv>`
  - Comma-separated excludes.
  - Default: `gw.excludes`.

- `-l, --logInputs`
  - Enable request input logging.
  - Default: `false` (`gw.logInputs`).

- `-as, --acts <path>`
  - Directory containing predefined act prompt files.
  - Default: not set.

- `-a, --act[=<name and prompt>]`
  - Run in Act mode.
  - Default: disabled.
  - Notes: if used without a value, Ghostwriter reads multi-line stdin. Input ends when a line does not end with `\`.


Troubleshooting & Support
-------------------------
Common issues:
- Missing model/provider:
  - Ensure `gw.model` is set in `gw.properties` or pass `-m/--model`.

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
