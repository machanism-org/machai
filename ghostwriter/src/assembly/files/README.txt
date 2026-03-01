Ghostwriter CLI
==============

1) Application Overview
-----------------------
Ghostwriter is a guided file processing engine for generating and maintaining
project-wide documentation and code improvements with AI.

It scans a repository (source code, tests, documentation, and other assets),
discovers embedded "@guidance:" directives, and turns them into actionable
prompts for a configured GenAI provider.

Key features:
- Repository-wide scanning (directories or patterns: glob:/regex:).
- Per-file-type reviewers extract "@guidance:" directives.
- Adds project-structure context to provider prompts.
- Supports system instructions and default guidance from plain text, URLs, or
  file: references.
- Supports excludes (exact paths or glob:/regex: patterns).
- Optional multi-threaded processing.
- Optional logging of provider inputs per processed file.
- "Act mode" for executing predefined prompts (--act).

Supported GenAI providers (examples):
- CodeMie
- OpenAI and OpenAI-compatible services


2) Installation Instructions
----------------------------
Prerequisites:
- Java 8
- A configured GenAI provider/model (gw.genai or -a/--genai)
- (Optional) a configuration file: gw.properties

Download:
- Ghostwriter CLI distribution (gw.zip):
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download

Build from source (Maven):
  mvn -DskipTests package

Configuration file:
- A sample configuration file is included in this distribution:
  gw.properties

- By default Ghostwriter reads configuration from:
  <gw.home>\gw.properties

- Override the configuration file path with:
  -Dgw.config=<path-or-file-name>

- Override the Ghostwriter home directory with:
  -Dgw.home=<dir>


3) How to Run
-------------
Basic usage:
  java -jar gw.jar <scanDir> [options]

Where <scanDir> can be:
- A directory path (relative to the root directory), OR
- A matcher expression supported by Java PathMatcher:
  - glob:...   (example: "glob:**/*.md")
  - regex:...  (example: "regex:^.*/[^/]+\\.java$")

Configuration can be provided in 3 ways:
- gw.properties (default)
- Java system properties (-D...)
- CLI options (override file configuration)


3.1 Configuration properties
----------------------------
These properties may be set in gw.properties and/or overridden with -D...:

- gw.config
  Description: System property to override the Ghostwriter configuration file
  path.
  Default: gw.properties
  Usage: -Dgw.config=custom.properties

- gw.home
  Description: System property defining the Ghostwriter home directory used to
  resolve gw.properties.
  Default: (if not set) rootDir if provided, else current working directory.
  Usage: -Dgw.home=C:\\tools\\ghostwriter

- gw.rootDir
  Description: Project root directory for scanning.
  Default: gw.rootDir (if set), else current working directory.
  Usage: set in gw.properties or pass -r/--root.

- gw.genai
  Description: GenAI provider and model (provider:model).
  Default: none (required).
  Usage: gw.genai=OpenAI:gpt-5.1 or gw.genai=CodeMie:...

- gw.instructions
  Description: System instructions applied to provider execution. Accepts plain
  text, URL lines, or file: references (processed line-by-line).
  Default: none.
  Usage: gw.instructions=file:instructions.txt

- gw.excludes
  Description: Comma-separated excludes (directories/paths/patterns).
  Default: none.
  Usage: gw.excludes=target,.git,glob:**/generated/**

- gw.guidance
  Description: Default guidance. Accepts plain text, URL lines, or file:
  references (processed line-by-line).
  Default: none.
  Usage: gw.guidance=file:guidance.txt

- gw.threads
  Description: Enables multi-threaded processing.
  Default: false.
  Usage: gw.threads=true

- gw.logInputs
  Description: Logs provider request inputs to dedicated log files.
  Default: false.
  Usage: gw.logInputs=true

- gw.scanDir
  Description: Fallback scan target if no <scanDir> argument is provided.
  Default: none; if missing, Ghostwriter scans the root directory.
  Usage: gw.scanDir=src


3.2 CLI options
---------------
- -h, --help
  Show help and exit.

- -r, --root <path>
  Root directory for file processing.

- -t, --threads[=<true|false>]
  Enable multi-threaded processing (default: false).
  If provided with no value, it enables threading.

- -a, --genai <provider:model>
  GenAI provider/model (required unless configured via gw.genai).

- -i, --instructions[=<text|url|file:...>]
  System instructions. Accepts plain text, URL lines, or file: references.
  If used without a value, Ghostwriter reads multi-line text from stdin until
  EOF.

- -g, --guidance[=<text|url|file:...>]
  Default guidance. Accepts plain text, URL lines, or file: references.
  If used without a value, Ghostwriter reads multi-line text from stdin until
  EOF.

- -e, --excludes <csv>
  Comma-separated excludes.

- -l, --logInputs
  Log provider request inputs to dedicated log files.

- --act[=<name and prompt>]
  Run in Act mode. If used without a value, Ghostwriter reads the action from
  stdin until EOF.


3.3 Setting configuration via environment variables or Java properties
---------------------------------------------------------------------
Ghostwriter reads most settings from gw.properties and/or -D Java system
properties.

Provider credentials are typically supplied via environment variables (see the
sample gw.properties):
- CodeMie:
  GENAI_USERNAME
  GENAI_PASSWORD

- OpenAI-compatible providers:
  OPENAI_API_KEY
  OPENAI_BASE_URL  (optional for original OpenAI)

Examples:
- Java system properties:
  java -Dgw.config=gw.properties -Dgw.rootDir=. -jar gw.jar src -a OpenAI:gpt-5.1

- Windows (cmd.exe) environment variables:
  set OPENAI_API_KEY=your_key
  set OPENAI_BASE_URL=https://your-endpoint
  java -jar gw.jar "glob:**/*.md" -a OpenAI:gpt-5.1

- Unix (sh) environment variables:
  export OPENAI_API_KEY=your_key
  export OPENAI_BASE_URL=https://your-endpoint
  java -jar gw.jar "glob:**/*.md" -a OpenAI:gpt-5.1


3.4 Examples (Windows and Unix)
------------------------------
Windows examples:
- Scan a directory:
  java -jar gw.jar src -a OpenAI:gpt-5.1

- Scan using a glob pattern:
  java -jar gw.jar "glob:**/*.java" -a OpenAI:gpt-5.1 -t

- Provide guidance from stdin (finish with Ctrl+Z then Enter):
  java -jar gw.jar "glob:**/*.md" -a OpenAI:gpt-5.1 -g

- Act mode from stdin (finish with Ctrl+Z then Enter):
  java -jar gw.jar src -a OpenAI:gpt-5.1 --act

Unix examples:
- Scan a directory:
  java -jar gw.jar src -a OpenAI:gpt-5.1

- Provide instructions from a file reference:
  java -jar gw.jar "glob:**/*.java" -a OpenAI:gpt-5.1 -i file:instructions.txt

- Provide guidance from stdin (finish with Ctrl+D):
  java -jar gw.jar "glob:**/*.md" -a OpenAI:gpt-5.1 -g

- Act mode from stdin (finish with Ctrl+D):
  java -jar gw.jar src -a OpenAI:gpt-5.1 --act


4) Troubleshooting & Support
----------------------------
Common issues:
- "No GenAI provider/model configured"
  Set gw.genai in gw.properties or pass -a/--genai.

- Authentication errors
  Verify provider credentials are set (GENAI_USERNAME/GENAI_PASSWORD for
  CodeMie; OPENAI_API_KEY and optionally OPENAI_BASE_URL for OpenAI-compatible
  providers).

- Missing or unexpected scan results
  Confirm <scanDir> points to the right location and that your glob:/regex:
  patterns are correct. Also check gw.excludes.

Logs and debug:
- Ghostwriter uses SLF4J/Logback configuration (see logback.xml).
- Use --logInputs (or set gw.logInputs=true) to log provider inputs to
  dedicated log files.


5) Contact & Documentation
--------------------------
Further documentation:
- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
