Ghostwriter CLI (gw)
===================

1) Application Overview
-----------------------
Ghostwriter is a documentation engine and CLI that scans your project, extracts embedded `@guidance` directives, and uses a configured GenAI provider to generate or refine documentation.

It supports all types of project files—including source code, documentation, project site content, and other relevant artifacts—so teams can keep docs aligned with the evolving codebase.

Typical use cases
- Project site/README generation
- API documentation enrichment
- Continuous documentation maintenance

Key features
- Scans directories or patterns (raw paths, glob patterns, or regex patterns)
- Extracts embedded `@guidance` directives (optionally combined with system instructions)
- Invokes a configured GenAI provider/model to synthesize changes
- Writes improved content back to the project files
- Optional multi-threaded processing
- Optional logging of LLM request inputs for auditing/debugging

Supported GenAI providers
- CodeMie
- OpenAI and OpenAI-compatible services (optional base URL)


2) Installation Instructions
----------------------------
Prerequisites
- Java 11 or later
- Network access and credentials for your selected GenAI provider
- Configuration via gw.properties and/or environment variables / Java system properties

Download / install
- Download the Ghostwriter CLI bundle:
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Build (from source)
- In the project root folder:
  mvn clean package

What’s included in this folder
- gw.properties
  - Sample configuration: provider/model selection and credential placeholders.
- gw.bat
  - Windows launcher (runs gw.jar and forwards all args).
- gw.sh
  - Unix-like launcher (runs gw.jar and forwards all args).
- g\
  - Folder containing example instruction prompt files (used with -i file:...).


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
- Environment variables (recommended for credentials)
- Java system properties (-D...)
- CLI options

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
  - Enable/disable multi-threaded processing (default: true).
- -r <path>, --root <path>
  - Root directory used as the project boundary and base for scanning.
- -a <provider:model>, --genai <provider:model>
  - GenAI provider and model (e.g., OpenAI:gpt-5.1).
- -i [text], --instructions [text]
  - System instructions.
  - Each line is processed: blank lines preserved; http(s)://... lines loaded and inlined; file:... lines loaded and inlined; other lines used as-is.
  - If used without a value, Ghostwriter reads instructions from stdin until EOF.
- -g [text], --guidance [text]
  - Default directory-level guidance applied as a final step for the current directory.
  - Same loading rules as --instructions. If used without a value, reads from stdin until EOF.
- -e <dirs>, --excludes <dirs>
  - Comma-separated list of directories to exclude from processing (e.g., target,.git).

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

Notes on --root and scanning
- <scanDir> can be a raw path, a glob pattern, or a regex pattern.
- Use --root to define the project boundary/base directory.

Example (glob scan)

Windows
```bat
java -jar gw.jar "glob:**\*.java" -r . -e "target,.git" -l
```


4) Troubleshooting & Support
----------------------------
Common issues
- Authentication / authorization errors
  - Verify provider credentials (CodeMie: GENAI_USERNAME/GENAI_PASSWORD; OpenAI-compatible: OPENAI_API_KEY).
  - If using an OpenAI-compatible endpoint, confirm OPENAI_BASE_URL is correct.
- Nothing changes / no files processed
  - Ensure the target directory contains files with embedded @guidance directives.
  - Check --root and --excludes aren’t filtering intended content.
- Missing configuration
  - Place gw.properties next to gw.jar, or pass -Dgw.config=<path>.

Logs and debug output
- Use --logInputs to write LLM request inputs to dedicated log files (useful for auditing/debugging).
- Run --help to confirm available options and defaults.


5) Contact & Documentation
--------------------------
- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Downloads: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
- Project site source: src\site\markdown\index.md
