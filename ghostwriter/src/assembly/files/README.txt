Ghostwriter CLI (Ghostwriter)
==========================

1) Application Overview
-----------------------
Ghostwriter is a command-line application that generates and updates documentation and other text artifacts for a codebase using a GenAI provider.

Main purpose
- Turn a local project/repository into a structured “context” for a GenAI model and produce consistent, repeatable outputs (e.g., READMEs, summaries, migration notes) based on configurable instructions.

Key features
- CLI-first workflow suitable for automation (local runs, CI jobs).
- Configurable instructions/prompts for consistent output.
- File and directory include/exclude controls to limit what is scanned.
- Support for multiple GenAI providers via a provider setting.
- Works with OpenAI-compatible HTTP APIs.

Typical use cases
- Generate a project README or module docs.
- Update existing docs to match a standard template.
- Create release notes / change summaries from a repository snapshot.
- Produce documentation for specific directories while excluding vendor/build output.

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (any provider exposing an OpenAI-style API endpoint)


2) Installation Instructions
----------------------------
Prerequisites
- Java: a modern JDK/JRE (Java 17+ recommended unless your build specifies otherwise).
- Network access to the configured GenAI provider endpoint.
- Credentials for your chosen provider (API key/token and, if required, organization/project identifiers).
- A configuration file (gw.properties) OR environment variables/system properties providing the same settings.

Install / build options (depending on your distribution)
A) Use a packaged distribution
- Download the Ghostwriter CLI distribution archive for your platform.
- Extract it to a directory of your choice.
- Ensure the scripts are executable (Unix/macOS):
  chmod +x bin/gw.sh

B) Build from source (typical Maven build)
- From the repository root:
  mvn -DskipTests package
- Locate the built distribution/artifacts produced by the build.

Environment variables (common)
- GW_PROVIDER: provider id (e.g., codemie, openai)
- GW_API_KEY: API key/token for the provider
- GW_BASE_URL: base URL for OpenAI-compatible services (if applicable)
- GW_MODEL: model name/id


3) How to Run
-------------
Ghostwriter is typically started via the provided platform scripts.

Windows (.bat)
- Basic run:
  bin\gw.bat

- Run with a specific project root and instructions:
  bin\gw.bat --root "C:\work\my-repo" --instructions "Generate a high-level README for this repository."

- Run with excludes:
  bin\gw.bat --root "C:\work\my-repo" --excludes "**/target/**,**/node_modules/**,**/.git/**"

Unix/macOS (.sh)
- Basic run:
  ./bin/gw.sh

- Run with a specific project root and instructions:
  ./bin/gw.sh --root /home/me/my-repo --instructions "Generate a high-level README for this repository."

- Run with excludes:
  ./bin/gw.sh --root /home/me/my-repo --excludes "**/target/**,**/node_modules/**,**/.git/**"

Configuration via environment variables
- Windows (PowerShell):
  setx GW_PROVIDER "openai"
  setx GW_API_KEY "<your-api-key>"
  setx GW_BASE_URL "https://api.example.com/v1"
  setx GW_MODEL "gpt-4o-mini"

- Unix/macOS:
  export GW_PROVIDER=openai
  export GW_API_KEY="<your-api-key>"
  export GW_BASE_URL="https://api.example.com/v1"
  export GW_MODEL="gpt-4o-mini"

Configuration via Java system properties
If you run the CLI as a Java process, you can pass -D properties. Example:
- Windows:
  java -Dgw.provider=openai -Dgw.apiKey=<your-api-key> -Dgw.baseUrl=https://api.example.com/v1 -Dgw.model=gpt-4o-mini -jar ghostwriter-cli.jar

- Unix/macOS:
  java -Dgw.provider=openai -Dgw.apiKey=<your-api-key> -Dgw.baseUrl=https://api.example.com/v1 -Dgw.model=gpt-4o-mini -jar ghostwriter-cli.jar

Common CLI options (names may vary by distribution)
- --root <dir>           Root directory to scan.
- --instructions <text>  Instructions/prompt for the generation.
- --instructionsFile <f> Read instructions from a file.
- --excludes <patterns>  Comma-separated glob patterns to exclude.
- --includes <patterns>  Comma-separated glob patterns to include.
- --config <file>        Path to gw.properties.
- --dryRun               Show what would be processed without calling GenAI.
- --debug                Enable verbose/debug output.


4) Configuration
----------------
gw.properties
The gw.properties file is the primary configuration mechanism for Ghostwriter. It lets you:
- Select a GenAI provider.
- Provide credentials and endpoint configuration.
- Define default instructions and scanning rules (includes/excludes).
- Set defaults so CI/local runs are consistent.

Location
- By default, place gw.properties in the working directory (or the project root), or pass its path via --config.

Configurable properties
Note: Property keys can be set using the gw.properties file, Java system properties (-D...), or environment variables.

Provider selection
- gw.provider
  Purpose: Selects the GenAI provider implementation.
  Examples: codemie, openai
  Env: GW_PROVIDER
  Java: -Dgw.provider=...

Credentials
- gw.apiKey
  Purpose: API key/token.
  Env: GW_API_KEY
  Java: -Dgw.apiKey=...

OpenAI-compatible endpoint settings (when using an OpenAI-style API)
- gw.baseUrl
  Purpose: Base URL for the API (e.g., https://api.example.com/v1).
  Env: GW_BASE_URL
  Java: -Dgw.baseUrl=...

- gw.model
  Purpose: Model identifier.
  Env: GW_MODEL
  Java: -Dgw.model=...

Generation defaults
- gw.instructions
  Purpose: Default instructions/prompt used when --instructions is not provided.
  Env: GW_INSTRUCTIONS
  Java: -Dgw.instructions=...

- gw.instructionsFile
  Purpose: Path to a file containing the instructions.
  Env: GW_INSTRUCTIONS_FILE
  Java: -Dgw.instructionsFile=...

Scanning rules
- gw.rootDir
  Purpose: Default root directory to scan.
  Env: GW_ROOT_DIR
  Java: -Dgw.rootDir=...

- gw.excludes
  Purpose: Comma-separated glob patterns to exclude from scanning.
  Example: **/target/**,**/node_modules/**,**/.git/**
  Env: GW_EXCLUDES
  Java: -Dgw.excludes=...

- gw.includes
  Purpose: Optional comma-separated glob patterns to include.
  Env: GW_INCLUDES
  Java: -Dgw.includes=...

Runtime/diagnostics
- gw.debug
  Purpose: Enables debug logging.
  Env: GW_DEBUG
  Java: -Dgw.debug=true

Configuration precedence (typical)
1. CLI options
2. Java system properties (-D...)
3. Environment variables
4. gw.properties


5) Examples
-----------
Example gw.properties (OpenAI-compatible)
----------------------------------------
# Provider
gw.provider=openai

# Auth
gw.apiKey=${GW_API_KEY}

# Endpoint/model
gw.baseUrl=https://api.example.com/v1
gw.model=gpt-4o-mini

# Defaults
gw.rootDir=.
gw.excludes=**/target/**,**/node_modules/**,**/.git/**
gw.instructions=Generate concise documentation for this repository.

Example: generate docs for a repository (Windows)
------------------------------------------------
bin\gw.bat --root "C:\work\repo" --instructionsFile "C:\work\repo\docs\instructions.txt" --excludes "**/target/**,**/.git/**"

Example: generate docs for a repository (Unix/macOS)
---------------------------------------------------
./bin/gw.sh --root /home/me/repo --instructionsFile /home/me/repo/docs/instructions.txt --excludes "**/target/**,**/.git/**"

Example: use environment variables for auth
------------------------------------------
- Windows (PowerShell):
  setx GW_API_KEY "<your-api-key>"
  bin\gw.bat --root "C:\work\repo" --instructions "Create a README.md overview."

- Unix/macOS:
  export GW_API_KEY="<your-api-key>"
  ./bin/gw.sh --root /home/me/repo --instructions "Create a README.md overview."


6) Troubleshooting & Support
----------------------------
Authentication errors (401/403)
- Verify gw.apiKey / GW_API_KEY is set and not expired.
- Confirm the provider (gw.provider) matches the credentials you are using.
- For OpenAI-compatible services, ensure gw.baseUrl is correct and includes the expected /v1 path if required.

Connection/timeouts
- Check network/proxy settings.
- Confirm the endpoint is reachable from your environment.

Missing files / unexpected output
- Ensure --root (or gw.rootDir) points to the intended directory.
- Review gw.excludes and gw.includes patterns; overly broad excludes can remove needed context.
- Run with --dryRun (if available) to see what would be scanned.

Logs and debug output
- Enable debug with --debug, GW_DEBUG=true, or -Dgw.debug=true.
- Logs are typically written to stdout/stderr unless your distribution configures a log file.
  If a log file is configured, check the distribution’s log directory (often ./logs) or your working directory.


7) Contact & Documentation
--------------------------
- Repository documentation: see src/site (if present) and any published project site.
- For issues/requests: use your organization’s standard support channel (e.g., GitHub Issues in the project repository) if available.
