Ghostwriter CLI (gw)
===================

1) Application Overview
-----------------------
Ghostwriter is a CLI documentation engine that scans, analyzes, and assembles project documentation using embedded `@guidance` directives and AI-powered synthesis.

It helps teams keep documentation accurate and up-to-date by:
- Generating or updating content from real project sources (code, docs, site pages, and other relevant artifacts)
- Applying embedded, file-local directives (`@guidance`) to control what gets generated
- Producing consistent results using a configurable GenAI provider/model

Common use cases
- Project site and README generation
- API and developer documentation enrichment
- Reviewing and improving existing Markdown/HTML/text documentation
- Keeping documentation aligned with the current codebase

Key features
- Scans directories and patterns (raw paths, `glob:` patterns, or `regex:` patterns) for supported project files
- Extracts embedded `@guidance` directives and applies them during processing
- Supports system-level instructions and directory-level default guidance
- Configurable GenAI provider/model via properties and/or CLI
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
- In the project root folder:
  mvn clean package

What’s included in this folder
- gw.properties
  - Sample/default configuration (GenAI provider/model selection and credential placeholders).
- gw.bat
  - Windows launcher script (runs gw.jar and forwards all args).
- gw.sh
  - Unix-like launcher script (runs gw.jar and forwards all args).
- g\create_tests
  - Example instructions prompt for generating unit tests.
- g\to_java21
  - Example instructions prompt for migrating a codebase from Java 17 to Java 21.


3) How to Run
-------------
Basic usage (run the JAR directly)

Windows
```bat
java -jar gw.jar src\main\java
```

Unix
```sh
java -jar gw.jar src/main/java
```

Using the included launcher scripts

Windows (.bat)
```bat
gw.bat C:\projects\my-project
```

Unix (.sh)
```sh
./gw.sh /path/to/my-project
```

Configuration sources
- Properties file: default is gw.properties, or pass -Dgw.config=<path>
- CLI options
- Environment variables (recommended for credentials)
- Java system properties (-D...)

Environment variables (auth)
- CodeMie:
  - GENAI_USERNAME
  - GENAI_PASSWORD
- OpenAI / OpenAI-compatible:
  - OPENAI_API_KEY
  - OPENAI_BASE_URL (optional)

Examples: setting environment variables

Windows
```bat
REM CodeMie
set GENAI_USERNAME=your_codemie_username
set GENAI_PASSWORD=your_codemie_password

REM OpenAI-compatible
set OPENAI_API_KEY=your_openai_api_key
set OPENAI_BASE_URL=https://your-openai-compatible-endpoint
```

Unix
```sh
# CodeMie
export GENAI_USERNAME=your_codemie_username
export GENAI_PASSWORD=your_codemie_password

# OpenAI-compatible
export OPENAI_API_KEY=your_openai_api_key
export OPENAI_BASE_URL=https://your-openai-compatible-endpoint
```

Common CLI options
- -h, --help
  - Show help and exit.
- -l, --logInputs
  - Log LLM request inputs to dedicated log files.
- -t [true|false], --threads [true|false]
  - Enable/disable multi-threaded processing.
- -r <path>, --root <path>
  - Root directory used as the project boundary and base for scanning.
- -a <provider:model>, --genai <provider:model>
  - GenAI provider and model (example: OpenAI:gpt-5.1).
- -i [text], --instructions [text]
  - System instructions text.
  - Each line is processed: http(s)://... lines are loaded and inlined, file:... lines are loaded and inlined, other lines are used as-is.
  - If used without a value, Ghostwriter reads instructions from stdin until EOF.
- -g [text], --guidance [text]
  - Default directory-level guidance applied as a final step for the current directory.
  - Same loading rules as --instructions.
  - If used without a value, Ghostwriter reads guidance from stdin until EOF.
- -e <dirs>, --excludes <dirs>
  - Comma-separated list of directories to exclude from processing.

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

Using included prompt files (g\...)

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
  - Verify provider credentials (CodeMie: GENAI_USERNAME/GENAI_PASSWORD; OpenAI-compatible: OPENAI_API_KEY).
  - If using an OpenAI-compatible endpoint, confirm OPENAI_BASE_URL is correct.
- Nothing changes / no files processed
  - Ensure the target directory contains files with embedded @guidance directives.
  - Check --root and --excludes aren’t filtering the intended content.
- Missing configuration
  - Place gw.properties next to gw.jar, or pass -Dgw.config=<path>.

Logs and debugging
- Use --logInputs to write LLM request inputs to dedicated log files (useful for auditing/debugging).
- Run --help to confirm available options and defaults.


5) Contact & Documentation
--------------------------
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Downloads: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
- Project site source: src\site\markdown\index.md
