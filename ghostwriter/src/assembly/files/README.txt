Ghostwriter CLI - README

1. Application Overview

Ghostwriter is an AI-assisted documentation engine that scans a project workspace, extracts embedded `@guidance` instructions from files, and assembles consistent, review-ready documentation.
It can be run locally or in CI to keep documentation aligned with the codebase and project requirements.

Key features:
- CLI-driven scanning of directories and glob patterns.
- Embedded `@guidance` discovery and application during documentation processing.
- Optional external instructions via URL(s), file path(s), or stdin.
- Optional default guidance applied as a final step.
- Configurable GenAI provider/model selection.
- Optional multi-threaded processing.
- Directory exclusion support.
- Optional logging of LLM request inputs.

Typical use cases:
- Keep docs aligned with code changes by embedding `@guidance` requirements in the repo.
- Run repeatable documentation updates in scripts or CI.
- Process large repositories while excluding build/output folders.

Supported GenAI providers:
- CodeMie
- OpenAI and OpenAI-compatible services


2. Installation Instructions

Prerequisites:
- Java 11+
- Network access to your chosen GenAI provider (as required)
- (Optional) A `gw.properties` file to provide defaults (see option defaults below)

Provider credentials/configuration (as required by provider):
- CodeMie (example): `GENAI_USERNAME`, `GENAI_PASSWORD`
- OpenAI-compatible providers (example): `OPENAI_API_KEY` (optional: `OPENAI_BASE_URL`)

Download/install:
- Download the Ghostwriter CLI distribution:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

This distribution folder typically contains:
- `gw.jar` (the CLI application; placed next to these scripts when packaged)
- `gw.properties` (example/default configuration)
- `gw.sh` (Unix launcher)
- `gw.bat` (Windows launcher)
- `g/` (helper prompt templates you can feed into Ghostwriter via `--instructions`)
  - `g/create_tests`: prompt template for generating unit tests in a target folder
  - `g/to_java21`: prompt template for migrating a Java codebase to Java 21

Build from source (to produce the jar):

```bash
mvn -U clean install
```


3. How to Run

Basic usage:

```bash
java -jar gw.jar <scanDir | glob_path_pattern>
```

Examples (Windows):

```bash
# scan a directory
java -jar gw.jar C:\projects\project

# specify root explicitly
java -jar gw.jar -r C:\projects\project src\project

# scan with a glob pattern
java -jar gw.jar -r C:\projects\project "**/*.java"
```

Using the provided launchers:

Windows (`gw.bat`):

```bat
gw.bat <scanDir|glob> [options]
```

Unix (`gw.sh`):

```sh
./gw.sh <scanDir|glob> [options]
```

Configuration via `gw.properties`:
- Place `gw.properties` next to `gw.jar` to provide defaults.
- The provided example sets the default GenAI provider/model:
  - `genai=CodeMie:gpt-5-2-2025-12-11`

Configuration via environment variables:
- You can define any property from `gw.properties` as an environment variable.

Windows example:

```bat
set GENAI_USERNAME=your_codemie_username
set GENAI_PASSWORD=your_codemie_password
gw.bat C:\projects\project -r C:\projects\project
```

Unix example:

```sh
export GENAI_USERNAME=your_codemie_username
export GENAI_PASSWORD=your_codemie_password
./gw.sh /path/to/project -r /path/to/project
```

Configuration via Java system properties (-D):

Windows example:

```bat
java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar %~dp0\gw.jar C:\projects\project -r C:\projects\project
```

Unix example:

```sh
java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar "$(dirname "$0")/gw.jar" /path/to/project -r /path/to/project
```

Common CLI options:
- `-h`, `--help`
  - Show help and exit.
- `-l`, `--logInputs`
  - Log LLM request inputs to dedicated log files.
  - Default: `false`.
- `-r`, `--root <path>`
  - Root directory used to validate scan targets and compute related paths.
  - Default: from `gw.properties` (`root`); otherwise the current user directory.
- `-t`, `--threads [true|false]`
  - Enable/disable multi-threaded processing.
  - Default: `true` (if provided without a value, defaults to `true`).
- `-a`, `--genai <Provider:Model>`
  - GenAI provider and model selector (format: `Provider:Model`).
  - Default: from `gw.properties` (`genai`); otherwise `OpenAI:gpt-5-mini`.
- `-i`, `--instructions [url-or-file[,url-or-file...]]`
  - Additional instruction source(s): comma-separated URLs/file paths, or pass the option without a value to enter instruction text via stdin.
  - Default: from `gw.properties` (`instructions`).
- `-g`, `--guidance [file]`
  - Default guidance applied as a final step for the current directory; provide a file path or pass without a value to enter guidance via stdin.
- `-e`, `--excludes <dir[,dir...]>`
  - Comma-separated list of directories to exclude.
  - Default: from `gw.properties` (`excludes`).

Notes:
- For `--instructions` and `--guidance`, relative file paths are resolved from the executable directory (the folder containing `gw.jar`).

Examples using `--instructions`, `--excludes`, and `--root`:

Windows:

```bat
REM use a local instruction template shipped in this distribution
REM (paths are resolved relative to the folder containing gw.jar)
gw.bat C:\projects\project -r C:\projects\project -i g\create_tests -e target,.git,node_modules
```

Unix:

```sh
# use a local instruction template shipped in this distribution
./gw.sh /path/to/project -r /path/to/project -i g/create_tests -e target,.git,node_modules
```

End-to-end example (direct Java invocation):

```bash
java -jar gw.jar C:\projects\project \
  -r C:\projects\project \
  -a OpenAI:gpt-5-mini \
  -t true \
  -i https://example.com/instructions.md,local-instructions.md \
  -g default-guidance.md \
  -e target,.git,node_modules \
  -l
```


4. Troubleshooting & Support

Common issues:
- Authentication failures
  - Verify the required credentials are set for your selected provider:
    - CodeMie: `GENAI_USERNAME` / `GENAI_PASSWORD`
    - OpenAI-compatible: `OPENAI_API_KEY` (and `OPENAI_BASE_URL` if applicable)
- No files updated / missing expected output
  - Ensure target files contain embedded `@guidance` blocks.
  - Ensure `--root` points to the intended workspace.
  - Ensure `--excludes` is not filtering relevant directories.
- Instructions/guidance file not found
  - If using relative paths for `--instructions`/`--guidance`, place the referenced files next to `gw.jar` or pass absolute paths.

Logs and debug:
- Run from a terminal and capture stdout/stderr.
- To record the LLM prompt payloads sent to the provider, run with `--logInputs`.

Support:
- Issue tracker: https://github.com/machanism-org/machai/issues


5. Contact & Documentation

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- CLI download: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
