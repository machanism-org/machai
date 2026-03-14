Ghostwriter CLI
==============

Application Overview
--------------------
Ghostwriter is a guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.

It scans an entire project (source, tests, docs, and other assets), discovers embedded `@guidance:` directives, and turns them into actionable prompts for a configured GenAI provider. Ghostwriter is based on the Guided File Processing approach: it treats a repository as a structured system rather than a collection of isolated files, and orchestrates consistent processing across the project.

Key features:
- Repository-wide scanning using directory paths or matchers (`glob:` / `regex:`).
- Per-file-type reviewers that extract embedded `@guidance:` directives.
- Prompt composition that includes project layout/structure context.
- Optional system instructions and default guidance loaded from plain text, URLs, or `file:` references.
- Excludes support (exact paths or `glob:` / `regex:` patterns).
- Optional multi-threaded processing.
- Optional logging of provider inputs per processed file.
- “Act mode” for executing predefined prompts (`--act`).

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI itself)


Installation Instructions
-------------------------
Prerequisites:
- Java 8 (as configured by `maven.compiler.release` in the project).
- A configured GenAI provider/model:
  - Property: `gw.model` (e.g., `OpenAI:gpt-5.1`), or
  - CLI override: `-m` / `--model`.
- (Optional) A `gw.properties` file to persist configuration.

Download / install:
- This distribution folder includes an example configuration file: `gw.properties`.
- Obtain/build the Ghostwriter CLI jar (commonly referenced as `gw.jar`).

Configuration file location:
- By default, Ghostwriter loads configuration from `gw.properties` located in the resolved “home” directory.
- Override the config file path via Java system property:
  - `-Dgw.config=<path-to-properties>`

Home directory resolution:
- `gw.home` (if set) is used as home.
- Otherwise, `--root` (if provided) is used.
- Otherwise, the current working directory is used.

Provider credentials (examples):
- CodeMie:
  - `GENAI_USERNAME` and `GENAI_PASSWORD`
- OpenAI / OpenAI-compatible:
  - `OPENAI_API_KEY`
  - `OPENAI_BASE_URL` (optional; required for OpenAI-compatible endpoints)


How to Run
----------
Basic usage:

Windows (cmd.exe):

```
java -jar gw.jar <scanDir> -m OpenAI:gpt-5.1
```

Unix/macOS:

```
java -jar gw.jar <scanDir> -m OpenAI:gpt-5.1
```

`<scanDir>` can be:
- A relative path (with respect to the current project directory), or
- A `glob:` matcher (e.g., `"glob:**/*.java"`), or
- A `regex:` matcher (e.g., `"regex:^.*/[^/]+\\.java$"`).

If an absolute path is provided, it must be located within the root project directory.


Configuration properties (from `org.machanism.machai.gw.processor.Ghostwriter`)
--------------------------------------------------------------------------
These properties are read from `gw.properties` unless overridden by CLI args or Java system properties.

- `gw.config` (Java system property)
  - Description: Overrides the Ghostwriter configuration file path.
  - Default: `gw.properties` (in the resolved home directory).
  - Usage: `java -Dgw.config=path\to\gw.properties -jar gw.jar ...`

- `gw.home`
  - Description: Sets the Ghostwriter home directory (used to locate `gw.properties` by default).
  - Default: `--root` value, or current working directory.

- `gw.rootDir`
  - Description: Root directory for file processing.
  - Default: current working directory.
  - Usage context: Used to resolve scan inputs and `file:` references.

- `gw.scanDir`
  - Description: Default scan directory/pattern when no `<scanDir>` arguments are provided.
  - Default: unset (falls back to current working directory if nothing is provided).

- `gw.model`
  - Description: GenAI provider and model in `provider:model` form.
  - Default: none (required; Ghostwriter fails fast if missing and no `-m/--model` is provided).

- `gw.instructions`
  - Description: Optional system instructions. May be plain text, URL(s), or `file:` references. Each line is parsed:
    - Blank lines preserved
    - `http://` / `https://` lines are loaded and included
    - `file:` lines are loaded (relative paths resolve from the configured root directory)
    - Other lines used as-is
  - Default: unset.

- `gw.guidance`
  - Description: Default guidance applied when a file does not contain embedded `@guidance:` directives.
  - Default: unset.

- `gw.excludes`
  - Description: Comma-separated list of directories/files/patterns to exclude.
  - Default: unset.

- `gw.threads`
  - Description: Degree of concurrency for processing.
  - Default: unset.

- `gw.logInputs`
  - Description: Enable logging of provider request inputs to dedicated log files.
  - Default: `false`.


CLI options (from `Ghostwriter`)
--------------------------------
- `-h, --help`
  - Description: Show help message and exit.
  - Default: `false`.

- `-r, --root <path>`
  - Description: Specify the path to the root directory for file processing.
  - Default: `gw.rootDir` or current working directory.

- `-t, --threads <count>`
  - Description: Degree of concurrency for processing.
  - Default: `gw.threads` or unset.

- `-m, --model <provider:model>`
  - Description: Set the GenAI provider and model (e.g., `OpenAI:gpt-5.1`).
  - Default: `gw.model`.

- `-i, --instructions[=<text|url|file:...>]`
  - Description: System instructions as plain text, URL, or `file:`. If provided without a value, Ghostwriter reads multi-line input from stdin.
  - Default: `gw.instructions`.
  - Stdin format: end input when a line does not end with `\` (a trailing backslash continues the next line).

- `-g, --guidance[=<text|url|file:...>]`
  - Description: Default guidance as plain text, URL, or `file:`. If provided without a value, Ghostwriter reads multi-line input from stdin.
  - Default: `gw.guidance`.
  - Stdin format: end input when a line does not end with `\`.

- `-e, --excludes <csv>`
  - Description: Comma-separated excludes list.
  - Default: `gw.excludes`.

- `-l, --logInputs`
  - Description: Log LLM request inputs to dedicated log files.
  - Default: `false` (or `gw.logInputs=true`).

- `-as, --acts <path>`
  - Description: Directory containing predefined act prompt files.
  - Default: unset.

- `-a, --act[=<name and prompt>]`
  - Description: Run in Act mode (interactive execution of predefined prompts). If provided without a value, Ghostwriter reads multi-line input from stdin.
  - Default: disabled.
  - Stdin format: end input when a line does not end with `\`.


Examples
--------
Windows:

```
:: Scan all Java files
java -jar gw.jar "glob:**/*.java" -m OpenAI:gpt-5.1

:: Use gw.properties in a custom location
java -Dgw.config=conf\gw.properties -jar gw.jar src -m OpenAI:gpt-5.1

:: Provide default guidance via stdin (multi-line; trailing \ continues)
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g

:: Exclude multiple directories
java -jar gw.jar src -m OpenAI:gpt-5.1 -e target,node_modules

:: Set project root explicitly
java -jar gw.jar src -m OpenAI:gpt-5.1 -r C:\\projects\\my-repo
```

Unix/macOS:

```
# Scan all Java files
java -jar gw.jar "glob:**/*.java" -m OpenAI:gpt-5.1

# Provide system instructions from a file reference
java -jar gw.jar src -m OpenAI:gpt-5.1 -i file:instructions.txt

# Default guidance + input logging
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l

# Exclude multiple directories
java -jar gw.jar src -m OpenAI:gpt-5.1 -e target,node_modules

# Set project root explicitly
java -jar gw.jar src -m OpenAI:gpt-5.1 -r /path/to/my-repo
```


Troubleshooting & Support
-------------------------
- “No GenAI provider/model configured”
  - Set `gw.model` in `gw.properties`, or pass `-m/--model`.

- Authentication errors
  - Verify provider environment variables are set (e.g., `GENAI_USERNAME/GENAI_PASSWORD` for CodeMie, `OPENAI_API_KEY` for OpenAI-compatible providers).
  - If using an OpenAI-compatible endpoint, confirm `OPENAI_BASE_URL`.

- Nothing scanned / unexpected matches
  - Confirm `<scanDir>` is correct.
  - For patterns, ensure you include the `glob:` or `regex:` prefix.
  - Review excludes (`gw.excludes` / `-e`).

- Missing `file:` references
  - `file:` paths are resolved relative to the configured root directory (`--root` / `gw.rootDir`).

- Logs / debug
  - Standard execution uses SLF4J logging.
  - If `--logInputs` (or `gw.logInputs=true`) is enabled, provider request inputs are written to dedicated log files.


Contact & Documentation
-----------------------
- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
