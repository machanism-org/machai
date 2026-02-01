Ghostwriter CLI — README
=========================

1) Application Overview
-----------------------
Ghostwriter CLI ("gw") is a command-line application that helps you generate, update, and refine project artifacts (e.g., documentation, code-adjacent text files, configuration snippets) using a configured Generative AI (GenAI) provider. It is designed for repeatable, automation-friendly runs (local use or CI) and supports operating on a project root directory with include/exclude controls.

Main purpose
- Automate creation and maintenance of project text artifacts via prompts/instructions.
- Make GenAI-assisted edits reproducible by keeping configuration in a properties file and/or environment variables.

Key features
- Provider-based GenAI integration.
- Configurable instructions (system/user style prompts) and reusable defaults.
- File and directory exclusion rules to avoid unwanted changes.
- Root directory targeting so you can run against different projects.
- CLI-friendly output suitable for scripts.

Typical use cases
- Generate or refresh README / docs across a repository.
- Create standardized templates (CONTRIBUTING, CHANGELOG, ADRs).
- Apply consistent writing style / policy updates across documentation.
- Assist with batch updates where controlled exclusions are required.

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (any provider exposing an OpenAI-compatible API)


2) Installation Instructions
----------------------------
Prerequisites
- Java: a supported LTS Java runtime (commonly Java 17+).
- Network access to your configured GenAI provider endpoint.
- Credentials for your provider (typically an API key).
- A configuration file (gw.properties) or equivalent environment variables / Java system properties.

Obtain / build
Depending on how your project is distributed, use one of the following:

A) Run from a packaged distribution
- Download the release artifact for your OS.
- Unpack it.
- Ensure the launch scripts are executable (Unix) and that Java is available on PATH.

B) Build from source
- Build using the project’s build tool (for example, Maven or Gradle).
  - Maven (example):
    mvn -DskipTests package
  - Gradle (example):
    gradle build
- After building, run the produced CLI launcher (e.g., gw.bat / gw.sh) or the generated JAR.


3) How to Run
-------------
General concept
- You run Ghostwriter from the command line.
- You supply configuration via gw.properties and/or environment variables and/or Java system properties.
- You pass CLI options to control instructions, excludes, and the root directory.

Command-line usage examples
(Exact flags may vary by distribution; use --help to confirm.)

Show help
- Windows:
  gw.bat --help
- Unix:
  ./gw.sh --help

Run with an explicit root directory and inline instructions
- Windows:
  gw.bat --root "C:\work\my-project" --instructions "Update project docs to match the new release." \
         --exclude "**/target/**" --exclude "**/.git/**"
- Unix:
  ./gw.sh --root "/home/me/work/my-project" --instructions "Update project docs to match the new release." \
          --exclude "**/target/**" --exclude "**/.git/**"

Run using a properties file
- Windows:
  gw.bat --root "C:\work\my-project" --config "C:\work\my-project\gw.properties"
- Unix:
  ./gw.sh --root "/home/me/work/my-project" --config "/home/me/work/my-project/gw.properties"

Environment variables and Java system properties
- Environment variables are convenient for secrets (API keys).
- Java system properties can override configuration at runtime.

Examples (Windows PowerShell)
  setx GW_PROVIDER "openai"
  setx GW_API_KEY "<your-api-key>"
  gw.bat --root "C:\work\my-project" --instructions "Draft a CHANGELOG entry for version 1.2.3."

Examples (Unix shell)
  export GW_PROVIDER="openai"
  export GW_API_KEY="<your-api-key>"
  ./gw.sh --root "/home/me/work/my-project" --instructions "Draft a CHANGELOG entry for version 1.2.3."

Examples (Java system properties)
- Windows:
  gw.bat -Dgw.provider=openai -Dgw.apiKey=<your-api-key> --root "C:\work\my-project"
- Unix:
  ./gw.sh -Dgw.provider=openai -Dgw.apiKey=<your-api-key> --root "/home/me/work/my-project"

Passing options: instructions, excludes, root directory
- --root: sets the directory Ghostwriter treats as the project root.
- --instructions: sets/overrides the run instructions.
- --exclude: repeats to add multiple exclude patterns (glob-style).


4) Configuration
----------------
The gw.properties file
- gw.properties is the primary configuration file for Ghostwriter CLI.
- It defines which GenAI provider to use, how to authenticate, and how default behavior should work (instructions, exclusions, etc.).
- You can keep a gw.properties file in the project root and reference it with --config, or rely on a default lookup depending on your distribution.

Configurable properties
(Names may vary by version; the list below describes the typical/expected configuration surface.)

Provider selection
- gw.provider
  - Purpose: Select the GenAI provider implementation.
  - Examples: codemie, openai (or other OpenAI-compatible)

Authentication and connectivity
- gw.apiKey
  - Purpose: API key/token used to authenticate to the provider.
  - Recommended source: environment variable (avoid committing secrets).
- gw.baseUrl
  - Purpose: Base URL for OpenAI-compatible endpoints (if applicable).
  - Example: https://api.openai.com/v1 or a self-hosted compatible endpoint.
- gw.model
  - Purpose: Default model name to use.
  - Examples: gpt-4o-mini, gpt-4.1-mini, provider-specific model ids.
- gw.timeoutSeconds
  - Purpose: Request timeout.

Behavior and content
- gw.instructions
  - Purpose: Default instructions/prompt used when --instructions is not supplied.
  - Tip: Keep stable, reusable guidance here.
- gw.excludes
  - Purpose: Comma-separated or line-separated glob patterns to exclude files/directories.
  - Examples: **/target/**, **/.git/**, **/node_modules/**
- gw.rootDir
  - Purpose: Default root directory if --root is not provided.

Optional output/logging
- gw.logLevel
  - Purpose: Control verbosity (e.g., INFO, DEBUG).
- gw.debug
  - Purpose: Enable debug output (true/false).

How properties can be set / overridden
Precedence is typically:
1) CLI options (e.g., --instructions, --root, --exclude)
2) Java system properties (e.g., -Dgw.apiKey=...)
3) Environment variables (e.g., GW_API_KEY=...)
4) gw.properties values

Environment variable mapping (typical)
- GW_PROVIDER -> gw.provider
- GW_API_KEY -> gw.apiKey
- GW_BASE_URL -> gw.baseUrl
- GW_MODEL -> gw.model
- GW_INSTRUCTIONS -> gw.instructions
- GW_EXCLUDES -> gw.excludes
- GW_LOG_LEVEL -> gw.logLevel
- GW_DEBUG -> gw.debug


5) Examples
-----------
Example gw.properties (OpenAI-compatible)
----------------------------------------
# Provider

gw.provider=openai

# Auth / endpoint

gw.apiKey=${GW_API_KEY}
# If using a non-default OpenAI-compatible endpoint:
# gw.baseUrl=https://your-openai-compatible.example/v1

gw.model=gpt-4o-mini

gw.timeoutSeconds=60

# Behavior

gw.instructions=Update documentation to match the current release and keep changes minimal.

gw.excludes=**/target/**,**/.git/**,**/node_modules/**

# Logging

gw.logLevel=INFO
# gw.debug=false

Example: generate/update docs while excluding build output
- Windows:
  set GW_API_KEY=<your-api-key>
  gw.bat --root "C:\work\my-project" --instructions "Update README and docs for the new configuration keys." \
         --exclude "**/target/**" --exclude "**/build/**"
- Unix:
  export GW_API_KEY=<your-api-key>
  ./gw.sh --root "/home/me/work/my-project" --instructions "Update README and docs for the new configuration keys." \
          --exclude "**/target/**" --exclude "**/build/**"

Example: keep instructions in config, override model at runtime
- Windows:
  set GW_API_KEY=<your-api-key>
  gw.bat -Dgw.model=gpt-4.1-mini --root "C:\work\my-project"
- Unix:
  export GW_API_KEY=<your-api-key>
  ./gw.sh -Dgw.model=gpt-4.1-mini --root "/home/me/work/my-project"

Example: CodeMie provider
- gw.properties:
  gw.provider=codemie
  gw.apiKey=${GW_API_KEY}
  gw.model=<codemie-model-id>


6) Troubleshooting & Support
----------------------------
Common issues
- Authentication errors (401/403)
  - Verify the API key is correct and accessible to the process.
  - Confirm the provider/base URL is correct.
  - Ensure your key has access to the selected model.

- Connection/timeouts
  - Check network connectivity and proxy/VPN settings.
  - Increase gw.timeoutSeconds.
  - Validate gw.baseUrl for OpenAI-compatible services.

- Missing files / unexpected changes
  - Confirm --root points to the intended project directory.
  - Add exclude patterns for generated content (target/, build/, node_modules/, .git/).
  - Ensure file paths are correct on Windows (escaping backslashes where needed).

Logs and debug output
- If the CLI supports --debug or gw.debug=true, enable it for more detail.
- If gw.logLevel is supported, set it to DEBUG.
- Check the console output and any configured log file locations (if your distribution writes logs).


7) Contact & Documentation
--------------------------
- Project documentation: see the repository’s docs (commonly under src/site or the project website).
- Support: use the project’s issue tracker / support channel as defined by your organization or repository.
