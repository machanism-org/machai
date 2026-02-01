Ghostwriter CLI (gw)
====================

1) Application Overview
-----------------------
Ghostwriter CLI is a command-line application that helps you generate or update project files using GenAI.

Main purpose
- Apply a set of instructions to a target project directory and produce consistent file updates.

Key features
- Works from the command line (batch/shell friendly).
- Reads configuration from a gw.properties file, environment variables, and/or Java system properties.
- Supports common workflows such as:
  - generating or refining documentation (e.g., README files)
  - creating or updating source code stubs
  - applying repo-wide content rules (e.g., include/exclude patterns)

Typical use cases
- Automate writing/updating documentation across modules.
- Generate boilerplate code or project scaffolding.
- Run repeatable “instruction packs” in CI or locally.

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (any provider exposing an OpenAI-compatible API)


2) Installation Instructions
----------------------------
Prerequisites
- Java: a recent LTS JDK (Java 17+ recommended unless your distribution states otherwise).
- Network access to your GenAI provider endpoint.
- Credentials for your chosen provider (API key and, if applicable, base URL / model name).
- Optional: a gw.properties configuration file.

Install / Build
- If you have a release distribution:
  1. Download the Ghostwriter CLI distribution archive.
  2. Extract it to a folder of your choice.
  3. Verify the launcher scripts are present (gw.bat for Windows, gw.sh for Unix).

- If building from source (typical Maven build):
  1. Install Java and Maven.
  2. From the project root, run:
     mvn clean package
  3. Locate the produced distribution/JAR in the build output (depending on the project packaging).


3) How to Run
-------------
Basic usage (conceptual)
- The CLI runs against a “root directory” (the project folder to read/update).
- You can pass instructions directly, or reference instruction files.
- You can also pass exclude patterns to skip files/folders.

Windows (.bat) examples
- Run against the current directory with inline instructions:
  gw.bat --root . --instructions "Update documentation for the project"

- Run with exclude patterns:
  gw.bat --root . --instructions "Apply formatting rules" --exclude "**/target/**" --exclude "**/.git/**"

- Run with an explicit properties file:
  gw.bat --root . --config "C:\path\to\gw.properties" --instructions "Generate README"

Unix (.sh) examples
- Run against the current directory with inline instructions:
  ./gw.sh --root . --instructions "Update documentation for the project"

- Run with exclude patterns:
  ./gw.sh --root . --instructions "Apply formatting rules" --exclude "**/target/**" --exclude "**/.git/**"

- Run with an explicit properties file:
  ./gw.sh --root . --config /path/to/gw.properties --instructions "Generate README"

Environment variables / Java system properties
- You can configure provider settings using either environment variables or Java system properties.
- Environment variable example (Windows PowerShell):
  $env:GW_PROVIDER="openai"
  $env:GW_API_KEY="..."
  gw.bat --root . --instructions "..."

- Environment variable example (Unix):
  export GW_PROVIDER=openai
  export GW_API_KEY=...
  ./gw.sh --root . --instructions "..."

- Java system properties example:
  gw.bat -Dgw.provider=openai -Dgw.apiKey=... --root . --instructions "..."
  ./gw.sh -Dgw.provider=openai -Dgw.apiKey=... --root . --instructions "..."

Notes
- Exact option names may vary by release. Use the help option to view available flags:
  gw.bat --help
  ./gw.sh --help


4) Configuration
----------------
The gw.properties file
- gw.properties centralizes Ghostwriter configuration so you can run repeatable jobs.
- The CLI typically loads properties from:
  1. Java system properties (-D...)
  2. Environment variables (GW_...)
  3. gw.properties (if present / specified)
  4. Command-line options (if provided)

Common properties (gw.properties)
- Provider selection
  - gw.provider
    Selects which GenAI provider implementation to use.
    Examples: codemie, openai

- Credentials / endpoint
  - gw.apiKey
    API key/token for the provider.
  - gw.baseUrl
    Base URL for OpenAI-compatible services (if not using a default).
    Example: https://api.openai.com/v1
  - gw.model
    Model identifier.
    Examples: gpt-4o-mini, gpt-4.1, etc.

- Request/behavior tuning
  - gw.temperature
    Sampling temperature (provider-specific; typically 0.0–2.0).
  - gw.timeoutSeconds
    Request timeout.

- Instructions & input control
  - gw.instructions
    Default instructions to run when not provided on the command line.
  - gw.instructionsFile
    Path to a file containing instructions.
  - gw.rootDir
    Default root directory.
  - gw.excludes
    Comma-separated exclude patterns (glob).
    Examples: **/target/**,**/.git/**,**/node_modules/**

- Output / logging
  - gw.logLevel
    Logging level (e.g., INFO, DEBUG).
  - gw.debug
    Enables additional debug output (true/false).

How to set properties via environment variables
- Environment variables generally map from property keys by:
  - prefixing with GW_
  - using uppercase
  - replacing dots with underscores

Examples
- gw.provider        -> GW_PROVIDER
- gw.apiKey          -> GW_API_KEY
- gw.baseUrl         -> GW_BASE_URL
- gw.model           -> GW_MODEL
- gw.rootDir         -> GW_ROOT_DIR
- gw.excludes        -> GW_EXCLUDES
- gw.logLevel        -> GW_LOG_LEVEL
- gw.debug           -> GW_DEBUG

How to set properties via Java system properties
- Use -Dkey=value
  -Dgw.provider=openai
  -Dgw.apiKey=...
  -Dgw.baseUrl=https://example.com/v1


5) Examples
-----------
Example A: OpenAI-compatible provider with a properties file
- gw.properties:
  gw.provider=openai
  gw.baseUrl=https://api.openai.com/v1
  gw.apiKey=${GW_API_KEY}
  gw.model=gpt-4o-mini
  gw.temperature=0.2
  gw.excludes=**/target/**,**/.git/**,**/node_modules/**

- Run:
  ./gw.sh --root . --instructions "Update README and keep changes minimal"

Example B: CodeMie provider via environment variables
Windows (PowerShell):
  $env:GW_PROVIDER="codemie"
  $env:GW_API_KEY="..."
  gw.bat --root C:\work\my-project --instructions "Generate a CONTRIBUTING guide"

Unix:
  export GW_PROVIDER=codemie
  export GW_API_KEY=...
  ./gw.sh --root /work/my-project --instructions "Generate a CONTRIBUTING guide"

Example C: Override configuration at runtime using -D
  ./gw.sh -Dgw.provider=openai -Dgw.model=gpt-4o-mini -Dgw.debug=true \
    --root . --instructions "Refactor docs" --exclude "**/build/**"


6) Troubleshooting & Support
----------------------------
Authentication / authorization errors
- Confirm GW_API_KEY (or gw.apiKey) is set and valid.
- For OpenAI-compatible services, confirm gw.baseUrl matches your provider’s endpoint.
- Ensure the requested gw.model is available to your account.

Missing files / nothing changes
- Confirm --root (or gw.rootDir) points to the intended project directory.
- Review exclude patterns; overly broad globs can skip expected files.
- Ensure you have write permissions to the target directory.

Network / timeout issues
- Check proxy/firewall settings.
- Increase gw.timeoutSeconds.

Logs and debug output
- Enable debug output:
  - Set gw.debug=true (or GW_DEBUG=true)
  - Or set gw.logLevel=DEBUG (or GW_LOG_LEVEL=DEBUG)
- Log output is typically written to the console. If your distribution writes to a file, check the application’s logs directory or the location described by your distribution.


7) Contact & Documentation
--------------------------
- See the project documentation shipped with your distribution (e.g., in src/site or the published site, if available).
- If this project is hosted in a Git repository, use the repository’s Issues page for bug reports and feature requests.
- For internal/company distributions, follow your organization’s support channel and include:
  - Ghostwriter CLI version
  - command used
  - sanitized logs (with secrets removed)
