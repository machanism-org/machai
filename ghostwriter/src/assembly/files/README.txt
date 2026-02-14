Ghostwriter CLI (gw)
===================

1) Application Overview
-----------------------
Ghostwriter is a CLI documentation engine that scans a project’s files, reads embedded `@guidance` directives, and uses a configured GenAI provider to generate or update documentation in a consistent, repeatable way.

Typical use cases
- Generate or refresh README/project site pages from real project sources.
- Enrich API and developer docs.
- Review and improve existing Markdown/HTML/text documentation.
- Keep documentation aligned with the current codebase.

Key features
- Scans directories (and may support `glob:` / `regex:` patterns, depending on usage) for supported project files
- Extracts embedded `@guidance` directives and applies them during processing
- Supports additional system-level instructions and default directory-level guidance
- Configurable GenAI provider/model via CLI option or properties
- Optional multi-threaded processing
- Optional logging of LLM request inputs for auditing/debugging

Supported GenAI providers
- CodeMie
- OpenAI and OpenAI-compatible services (optional base URL)


2) Installation Instructions
----------------------------
Prerequisites
- Java 11 or later
- Network access to your selected GenAI provider

Download / install
- Download the Ghostwriter CLI bundle:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Build (from source)
- In the project root:
  mvn clean package

Included files in this folder
- `gw.properties`  : Sample/default configuration (provider selection, credentials placeholders, and common settings).
- `gw.bat`         : Windows launcher script.
- `gw.sh`          : Unix-like launcher script.
- `g\...`          : Example guidance prompts you can reference via `file:` when using `--guidance`/`--instructions`.


3) How to Run
-------------
Basic usage

Windows (run the JAR)
```bat
java -jar gw.jar src\main\java
```

Windows (script)
```bat
gw.bat C:\projects\my-project
```

Unix (run the JAR)
```sh
java -jar gw.jar src/main/java
```

Unix (script)
```sh
./gw.sh /path/to/my-project
```

Configuration sources
- `gw.properties` (default: `gw.properties`, or via `-Dgw.config=<path>`)
- CLI options
- Environment variables (recommended for credentials)
- Java system properties (`-D...`)

Environment variables (provider auth)
- CodeMie:
  - `GENAI_USERNAME`
  - `GENAI_PASSWORD`
- OpenAI / OpenAI-compatible:
  - `OPENAI_API_KEY`
  - `OPENAI_BASE_URL` (optional)

Windows examples
```bat
REM CodeMie
set GENAI_USERNAME=your_codemie_username
set GENAI_PASSWORD=your_codemie_password

REM OpenAI-compatible
set OPENAI_API_KEY=your_openai_api_key
set OPENAI_BASE_URL=https://your-openai-compatible-endpoint
```

Unix examples
```sh
# CodeMie
export GENAI_USERNAME=your_codemie_username
export GENAI_PASSWORD=your_codemie_password

# OpenAI-compatible
export OPENAI_API_KEY=your_openai_api_key
export OPENAI_BASE_URL=https://your-openai-compatible-endpoint
```

Common CLI options
- `-h`, `--help`
  - Show help and exit.
- `-l`, `--logInputs`
  - Log LLM request inputs to dedicated log files.
- `-t [true|false]`, `--threads [true|false]`
  - Enable/disable multi-threaded processing.
- `-r <path>`, `--root <path>`
  - Root directory used as the project boundary and base for scanning.
- `-a <provider:model>`, `--genai <provider:model>`
  - GenAI provider and model (for example: `OpenAI:gpt-5.1`).
- `-i [text]`, `--instructions [text]`
  - System instructions text. If used without a value, reads from stdin until EOF.
  - May include `http(s)://...` or `file:...` lines which are loaded and inlined.
- `-e <dirs>`, `--excludes <dirs>`
  - Comma-separated list of directories to exclude.

Examples

Windows: set root, excludes, instructions, and provider/model
```bat
java -Dgw.config=gw.properties -jar gw.jar C:\projects\my-project ^
  -r C:\projects\my-project ^
  -e target,.git,node_modules ^
  -i "Review docs for accuracy; keep changes minimal" ^
  -a OpenAI:gpt-5.1 ^
  -t true ^
  -l
```

Unix: same idea
```sh
java -Dgw.config=gw.properties -jar gw.jar /path/to/my-project \
  -r /path/to/my-project \
  -e target,.git,node_modules \
  -i "Review docs for accuracy; keep changes minimal" \
  -a OpenAI:gpt-5.1 \
  -t true \
  -l
```

Using included guidance examples
- Use the `g\...` prompt files as inputs via `file:`.

Windows
```bat
java -jar gw.jar C:\projects\my-project -i file:%CD%\g\create_tests
```

Unix
```sh
java -jar gw.jar /path/to/my-project -i file:./g/create_tests
```


4) Troubleshooting & Support
----------------------------
Common issues
- Authentication / authorization errors
  - Verify provider credentials (CodeMie: `GENAI_USERNAME`/`GENAI_PASSWORD`; OpenAI-compatible: `OPENAI_API_KEY`).
  - If using an OpenAI-compatible endpoint, confirm `OPENAI_BASE_URL` is correct.
- Nothing changes / no files processed
  - Ensure the target directory contains files with embedded `@guidance` directives.
  - Check `--root` and `--excludes` aren’t filtering the intended content.
- Missing configuration
  - Place `gw.properties` next to `gw.jar`, or pass `-Dgw.config=<path>`.

Logs and debugging
- Use `--logInputs` to write LLM request inputs to dedicated log files (useful for auditing/debugging).
- Run `--help` to confirm available options and defaults.


5) Contact & Documentation
--------------------------
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Downloads: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
- Project site source: src\site\markdown\index.md
