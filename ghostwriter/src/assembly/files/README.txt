Ghostwriter CLI (ghostwriter-cli)
===============================

1) Application Overview
-----------------------
Ghostwriter CLI is a command-line application that helps automate source-code and documentation changes using Generative AI (GenAI). It is designed for developer workflows where you want to:

- Apply change requests across a codebase (create/update files, refactor, add docs).
- Keep changes consistent with project conventions.
- Exclude files/folders that must not be modified.
- Run repeatable transformations using a configuration file and command-line options.

Typical use cases:
- Implementing small-to-medium feature requests across multiple files.
- Generating or updating README / docs for a project.
- Applying formatting or standards updates to a repository.
- Assisting with repetitive changes while retaining human review.

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (any provider exposing an OpenAI-compatible REST API)


2) Installation Instructions
----------------------------
Prerequisites
- Java: A recent LTS JDK is recommended (Java 17+).
- Network access to your GenAI provider endpoint.
- Credentials for the selected provider (API key / token).
- A configuration file (typically gw.properties) OR environment variables / Java system properties.

Obtain / build
- If you received a distribution ZIP/TAR:
  - Extract it.
  - Use the provided launcher scripts (.bat for Windows, .sh for Unix).

- If building from source (typical Maven build):
  - From the project root:
    - mvn -DskipTests package
  - Run using the produced artifact (JAR) or the generated scripts (if included in your distribution).


3) How to Run
-------------
The application can be executed via provided scripts or by running the JAR with Java. Configuration can be supplied through:
- gw.properties (recommended)
- Environment variables
- Java system properties (-D...)
- Command-line options

Common options (names may vary by distribution)
- --root <path>        Root directory to operate on
- --instructions <txt> High-level instructions / request
- --exclude <pattern>  Exclude glob(s)/path(s); repeatable or comma-separated
- --config <path>      Path to gw.properties
- --debug              Enable verbose/debug logging

Windows examples (.bat)
- Using a launcher script (if provided):
  - ghostwriter.bat --root . --instructions "Update README" --exclude "**/target/**"

- Running a JAR directly:
  - set GW_CONFIG=%CD%\gw.properties
  - set GW_PROVIDER=codemie
  - set GW_API_KEY=YOUR_KEY_HERE
  - java -Dgw.config=%GW_CONFIG% -jar ghostwriter-cli.jar --root . --instructions "Fix failing tests" --exclude "**/target/**"

Unix examples (.sh)
- Using a launcher script (if provided):
  - ./ghostwriter.sh --root . --instructions "Update README" --exclude "**/target/**"

- Running a JAR directly:
  - export GW_CONFIG="$PWD/gw.properties"
  - export GW_PROVIDER=codemie
  - export GW_API_KEY="YOUR_KEY_HERE"
  - java -Dgw.config="$GW_CONFIG" -jar ghostwriter-cli.jar --root . --instructions "Fix failing tests" --exclude "**/target/**"

Passing configuration via environment variables / Java system properties
- Environment variables are useful for secrets (API keys) and CI.
- Java system properties are useful for overriding values at runtime:
  - java -Dgw.provider=openai -Dgw.apiKey=... -jar ghostwriter-cli.jar ...

Notes
- Prefer quoting instructions to preserve spaces.
- For large instruction text, place it in a file and reference it if your build supports it (e.g., --instructions-file path), or store it in gw.properties.


4) Configuration
----------------
The gw.properties file
- Purpose: Central place to define provider settings, credentials (optionally), defaults for instructions, excludes, and runtime behavior.
- Location:
  - Default: project root (recommended)
  - Override via:
    - Command-line: --config <path> (if supported)
    - Java property: -Dgw.config=<path>
    - Environment variable: GW_CONFIG=<path>

Configurable properties
The following properties are commonly supported in Ghostwriter CLI distributions.
If your build includes additional settings, they can be added to gw.properties as needed.

Provider selection
- gw.provider
  - Values: codemie | openai | openai-compatible
  - Chooses the GenAI backend.

OpenAI-compatible endpoint configuration
- gw.baseUrl
  - Base URL for OpenAI-compatible API (e.g., https://api.openai.com/v1 or a self-hosted gateway).
- gw.model
  - Model name (e.g., gpt-4.1-mini, gpt-4o-mini, or provider-specific).

Credentials
- gw.apiKey
  - API key/token.
  - Recommended: set via environment variable or secret manager.

Request / behavior defaults
- gw.instructions
  - Default instructions when --instructions is not supplied.
- gw.root
  - Default root directory.
- gw.excludes
  - Default exclude patterns (comma-separated).
  - Examples: **/target/**,**/.git/**,**/node_modules/**

Logging / diagnostics
- gw.logLevel
  - Examples: INFO, DEBUG
- gw.debug
  - true/false; enables verbose output.

How to override properties
- Environment variables (typical mapping):
  - GW_PROVIDER            -> gw.provider
  - GW_BASE_URL            -> gw.baseUrl
  - GW_MODEL               -> gw.model
  - GW_API_KEY             -> gw.apiKey
  - GW_INSTRUCTIONS        -> gw.instructions
  - GW_ROOT                -> gw.root
  - GW_EXCLUDES            -> gw.excludes
  - GW_LOG_LEVEL           -> gw.logLevel
  - GW_DEBUG               -> gw.debug

- Java system properties override gw.properties values:
  - -Dgw.provider=...
  - -Dgw.apiKey=...
  - -Dgw.excludes="**/target/**,**/.git/**"

- Command-line options override all defaults:
  - ghostwriter ... --root ... --instructions ... --exclude ...

Example gw.properties
---------------------
# Provider
# codemie | openai | openai-compatible
#gw.provider=codemie

# OpenAI-compatible
#gw.baseUrl=https://api.openai.com/v1
#gw.model=gpt-4o-mini

# Credentials (prefer env var GW_API_KEY)
#gw.apiKey=YOUR_KEY_HERE

# Defaults
#gw.root=.
#gw.excludes=**/target/**,**/.git/**,**/node_modules/**
#gw.instructions=Update documentation and keep formatting consistent.

# Logging
#gw.logLevel=INFO
#gw.debug=false


5) Examples
-----------
A) Run with CodeMie using environment variables
Windows:
  set GW_PROVIDER=codemie
  set GW_API_KEY=YOUR_KEY
  ghostwriter.bat --root . --instructions "Add a CONTRIBUTING.md" --exclude "**/target/**"

Unix:
  export GW_PROVIDER=codemie
  export GW_API_KEY="YOUR_KEY"
  ./ghostwriter.sh --root . --instructions "Add a CONTRIBUTING.md" --exclude "**/target/**"

B) Run with OpenAI-compatible endpoint
  export GW_PROVIDER=openai-compatible
  export GW_BASE_URL="https://api.openai.com/v1"
  export GW_MODEL="gpt-4o-mini"
  export GW_API_KEY="YOUR_KEY"
  ./ghostwriter.sh --root . --instructions "Refactor package names" --exclude "**/target/**"

C) Store defaults in gw.properties and only pass task text
  # gw.properties
  gw.provider=openai-compatible
  gw.baseUrl=https://api.openai.com/v1
  gw.model=gpt-4o-mini
  gw.excludes=**/target/**,**/.git/**

  # command
  ./ghostwriter.sh --root . --instructions "Update README to include installation steps."

D) Override excludes at runtime
  ./ghostwriter.sh --root . --instructions "Update licenses" --exclude "**/target/**" --exclude "**/dist/**"


6) Troubleshooting & Support
----------------------------
Common issues
- Authentication errors (401/403)
  - Verify GW_API_KEY (or gw.apiKey) is set and valid.
  - Confirm you are targeting the correct endpoint (gw.baseUrl).

- Connection / timeout issues
  - Check network access, proxy settings, and firewall.
  - Reduce request size or split instructions.

- Missing files / unexpected changes
  - Ensure --root points to the correct directory.
  - Review gw.excludes patterns; add **/.git/**, **/target/**, **/node_modules/** as needed.

- Model not found / invalid model
  - Verify gw.model is supported by the configured provider.

Logs and debug output
- Run with --debug (if available) or set:
  - GW_DEBUG=true
  - or -Dgw.debug=true
- Increase verbosity:
  - GW_LOG_LEVEL=DEBUG
  - or -Dgw.logLevel=DEBUG
- If your distribution writes log files, check the working directory or configured log location (if provided by your build).


7) Contact & Documentation
--------------------------
- Check your project repository documentation (e.g., root README, docs site under src/site) for detailed usage.
- If this CLI was provided as part of an internal platform, use your organization’s standard support channel (issue tracker, chat, or service desk).
