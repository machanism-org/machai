Ghostwriter CLI (gw)
====================

1) Application Overview
------------------------
Ghostwriter CLI is a command-line application that generates or updates project documentation and other text artifacts by combining:
- Your local project files (source, resources, docs)
- Your instructions (prompts)
- A configured GenAI provider

Main purpose
- Automate repeatable, project-aware writing tasks (e.g., READMEs, release notes, design notes, codebase summaries) from the terminal.

Key features
- CLI-first workflow designed for automation (local runs and CI).
- Uses your project directory as context (root directory configurable).
- Supports excluding files/directories from context collection.
- Provider-agnostic GenAI integration:
  - CodeMie
  - OpenAI-compatible services (any provider exposing OpenAI-compatible REST endpoints)

Typical use cases
- Generate or update documentation for a repository.
- Produce standard project artifacts (README, ADRs, changelogs).
- Summarize a codebase or selected modules.
- Batch generation in CI pipelines.


2) Installation Instructions
----------------------------
Prerequisites
- Java: a supported JDK installed on your machine (use the project’s required Java version; if unsure, start with Java 17+).
- Network access to the configured GenAI provider endpoint.
- Credentials for the GenAI provider (API key / token) set via:
  - Environment variables, and/or
  - gw.properties, and/or
  - Java system properties (-D...)

Optional prerequisites
- A gw.properties file (recommended for repeatable runs).

Build / install (typical)
- Build with your project build tool (e.g., Maven/Gradle) to produce the runnable distribution/scripts.
- Ensure the produced scripts are available:
  - Windows: gw.bat
  - Unix-like: gw.sh

Note
- Exact build steps depend on the project’s build configuration. Refer to the repository documentation (e.g., root README or build files) for the authoritative build commands.


3) How to Run
-------------
Basic usage (conceptual)
- Run the provided launcher script and pass options for:
  - root directory (project context)
  - instructions (prompt text or a file)
  - excludes (paths/globs to omit)
  - provider configuration (via properties / env / -D)

Windows (.bat) examples
- Run with a specific root directory and inline instructions:
  gw.bat --root-dir "C:\work\my-project" --instructions "Generate a concise README based on the project." 

- Run with excludes (repeat option or comma-separated, depending on CLI support):
  gw.bat --root-dir "C:\work\my-project" --instructions "Summarize modules." --exclude "target" --exclude ".git" --exclude "**\\*.log"

- Run using a instructions file:
  gw.bat --root-dir "C:\work\my-project" --instructions-file "C:\work\prompts\doc-update.txt"

Unix (.sh) examples
- Run with a specific root directory and inline instructions:
  ./gw.sh --root-dir "/home/user/my-project" --instructions "Generate a concise README based on the project."

- Run with excludes:
  ./gw.sh --root-dir "/home/user/my-project" --instructions "Summarize modules." --exclude "target" --exclude ".git" --exclude "**/*.log"

- Run using an instructions file:
  ./gw.sh --root-dir "/home/user/my-project" --instructions-file "/home/user/prompts/doc-update.txt"

Configuration via environment variables
- Set provider credentials via environment variables (names depend on your gw.properties / provider):
  Windows (PowerShell):
    $env:GW_PROVIDER="codemie"
    $env:GW_API_KEY="..."
    gw.bat --root-dir "C:\work\my-project" --instructions "..."

  Windows (cmd.exe):
    set GW_PROVIDER=codemie
    set GW_API_KEY=...
    gw.bat --root-dir "C:\work\my-project" --instructions "..."

  Unix:
    export GW_PROVIDER=codemie
    export GW_API_KEY="..."
    ./gw.sh --root-dir "/home/user/my-project" --instructions "..."

Configuration via Java system properties
- Pass -D properties through the script (if supported) or run the Java main class directly:
  Windows:
    gw.bat -Dgw.provider=codemie -Dgw.apiKey=... --root-dir "C:\work\my-project" --instructions "..."

  Unix:
    ./gw.sh -Dgw.provider=codemie -Dgw.apiKey=... --root-dir "/home/user/my-project" --instructions "..."


4) Configuration
----------------
gw.properties
- gw.properties is the primary configuration file for Ghostwriter CLI.
- It provides default values so you don’t have to pass everything on the command line.
- The file is typically placed where the application can find it (commonly the working directory, a config folder, or a path provided to the app). The exact discovery rules depend on the project’s implementation.

Common configurable properties
- Provider selection
  - gw.provider
    - Values: codemie | openai (or another OpenAI-compatible identifier)

- Provider endpoint (for OpenAI-compatible services)
  - gw.baseUrl
    - Example: https://api.openai.com/v1
    - Example (self-hosted / gateway): https://your-openai-compatible.example.com/v1

- Credentials
  - gw.apiKey
    - API key / token used to authenticate to the provider.

- Model selection
  - gw.model
    - Example: gpt-4.1-mini (OpenAI-compatible)
    - Example: provider-specific model id

- Instructions and behavior
  - gw.instructions
    - Default instruction text.
  - gw.instructionsFile
    - Path to a file containing instructions.
  - gw.excludes
    - Comma-separated list of excluded paths/globs.
    - Example: target,.git,**/*.log
  - gw.rootDir
    - Default root directory for project context.

- Output and logging
  - gw.logLevel
    - Example: INFO | DEBUG
  - gw.debug
    - Example: true | false

How properties are resolved
- Properties can typically be supplied in one or more of these ways:
  1) Command-line options (highest precedence)
  2) Java system properties (-Dgw.*)
  3) Environment variables (mapped to gw.*; common pattern is GW_* names)
  4) gw.properties (defaults)

Environment variable mapping (typical conventions)
- GW_PROVIDER -> gw.provider
- GW_BASE_URL -> gw.baseUrl
- GW_API_KEY -> gw.apiKey
- GW_MODEL -> gw.model
- GW_ROOT_DIR -> gw.rootDir
- GW_EXCLUDES -> gw.excludes
- GW_LOG_LEVEL -> gw.logLevel
- GW_DEBUG -> gw.debug

Note
- Actual supported property names and precedence are determined by the application. Use the CLI help (e.g., --help) and the project documentation for the authoritative list.


5) Examples
-----------
Example A: CodeMie provider with minimal config
- gw.properties:
  gw.provider=codemie
  gw.apiKey=YOUR_TOKEN
  gw.model=codemie-default

- Run:
  ./gw.sh --root-dir "/path/to/project" --instructions "Create release notes for the last sprint."

Example B: OpenAI-compatible endpoint
- gw.properties:
  gw.provider=openai
  gw.baseUrl=https://api.openai.com/v1
  gw.apiKey=YOUR_OPENAI_API_KEY
  gw.model=gpt-4.1-mini
  gw.excludes=target,.git,**/*.log

- Run (Windows):
  gw.bat --root-dir "C:\work\my-project" --instructions "Update docs to match the current code." 

Example C: Override model at runtime
- Unix:
  ./gw.sh -Dgw.model=gpt-4.1-mini --root-dir "/home/user/my-project" --instructions "Summarize architecture." 

Example D: Use an instructions file
- instructions.txt:
  - Update README.md
  - Include build and run steps
  - Keep it concise

- Run:
  ./gw.sh --root-dir "/home/user/my-project" --instructions-file "/home/user/instructions.txt"


6) Troubleshooting & Support
----------------------------
Authentication / authorization errors
- Verify the API key/token is correct and active.
- Confirm the key is being picked up from the intended source (env vs gw.properties vs -D).
- For OpenAI-compatible services, verify the base URL includes the correct /v1 path if required.

Provider/endpoint issues
- Ensure the configured endpoint is reachable (DNS, proxy, firewall).
- Confirm TLS/SSL requirements (corporate proxy certificates, etc.).

Missing files / empty context
- Verify --root-dir (or gw.rootDir) points to the repository root.
- Check excludes (gw.excludes / --exclude) are not excluding required folders.

Debugging and logs
- Enable debug output:
  - Set gw.debug=true (or GW_DEBUG=true) and/or gw.logLevel=DEBUG.
- Logs are typically written to standard output/stderr unless configured otherwise.
- In CI, capture console output as build logs.


7) Contact & Documentation
--------------------------
- Refer to the repository documentation (root README, docs under src/site, or project website if available).
- For support, use the project’s issue tracker and include:
  - Command used
  - Relevant configuration (redact secrets)
  - Logs with DEBUG enabled
