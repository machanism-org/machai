Ghostwriter CLI — README
=========================

1) Application Overview
-----------------------
Ghostwriter CLI ("gw") is a command-line application that helps you generate, improve, and maintain project documentation and developer-facing text using Generative AI (GenAI). It is designed to run locally, read and analyze files from a chosen root directory, and then produce updated or new textual outputs based on your instructions.

Main purpose
- Turn high-level instructions into consistent documentation or text outputs.
- Automate repetitive writing tasks (release notes, READMEs, usage guides, API docs stubs, changelog entries, etc.).
- Apply project conventions across many files while respecting excludes and scoped roots.

Key features
- CLI-first workflow (suitable for local use and CI automation).
- Configurable root directory for file scanning and output.
- Exclude patterns to avoid processing build outputs, vendor folders, or other undesired paths.
- Provider abstraction to support multiple GenAI backends.

Typical use cases
- Generate/refresh README and usage documentation.
- Create documentation bundles from existing code and resources.
- Apply standardized wording and structure across a repository.
- Produce “draft” text for review (human-in-the-loop).

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (any provider exposing an OpenAI-compatible REST API)


2) Installation Instructions
----------------------------
Prerequisites
- Java: a supported LTS JDK (recommended: Java 17+).
- Network access to the configured GenAI provider endpoint.
- Provider credentials, typically via environment variables and/or gw.properties.

Optional/typical prerequisites
- A configuration file: gw.properties (see Configuration section).
- For OpenAI-compatible services: an API key and (optionally) a base URL.

Build / install
- If you have a distribution archive (zip/tar) containing scripts, unpack it to a folder.
- If building from source (typical Maven layout):
  - Build with Maven:
    - mvn -DskipTests package
  - The packaged distribution/JAR location depends on your project’s build configuration.


3) How to Run
-------------
Ghostwriter is executed from the command line. Depending on your distribution, you may have platform scripts:
- Windows: gw.bat
- Unix/Linux/macOS: gw.sh

Basic usage examples
- Windows (PowerShell / CMD):
  - gw.bat --root . --instructions "Update the README and usage docs"

- Unix:
  - ./gw.sh --root . --instructions "Update the README and usage docs"

Configuration via environment variables
- Windows (PowerShell):
  - $env:GW_PROVIDER = "openai"
  - $env:GW_API_KEY = "<your-key>"
  - gw.bat --root . --instructions "Generate docs"

- Windows (CMD):
  - set GW_PROVIDER=openai
  - set GW_API_KEY=<your-key>
  - gw.bat --root . --instructions "Generate docs"

- Unix:
  - export GW_PROVIDER=openai
  - export GW_API_KEY="<your-key>"
  - ./gw.sh --root . --instructions "Generate docs"

Configuration via Java system properties
- Windows:
  - gw.bat -Dgw.provider=openai -Dgw.apiKey=<your-key> --root . --instructions "Generate docs"

- Unix:
  - ./gw.sh -Dgw.provider=openai -Dgw.apiKey=<your-key> --root . --instructions "Generate docs"

Passing options: instructions, excludes, root directory
- Root directory (where Ghostwriter reads/writes):
  - --root <path>

- Instructions (what you want the tool to do):
  - --instructions "<text>"

- Excludes (repeatable or comma-separated, depending on your CLI build):
  - --exclude "target/**" --exclude ".git/**" --exclude "node_modules/**"

Examples
- Windows:
  - gw.bat --root C:\work\my-repo --instructions "Create a CONTRIBUTING guide" --exclude "target/**" --exclude ".git/**"

- Unix:
  - ./gw.sh --root /home/me/my-repo --instructions "Create a CONTRIBUTING guide" --exclude "target/**" --exclude ".git/**"


4) Configuration
----------------
Ghostwriter can be configured using a gw.properties file, environment variables, and/or Java system properties.

4.1 gw.properties
- Purpose:
  - Provide a stable, versionable configuration for provider selection, credentials, defaults, and common settings.
  - Avoid long command lines by setting defaults.

- Location:
  - Common approaches include:
    - In the project root
    - In a dedicated config folder
  - The effective search path depends on how your distribution is wired; if your build expects a specific location, place gw.properties there.

4.2 Configurable properties
The exact set of properties may vary by version/build, but typical properties include:

Provider selection
- gw.provider
  - Values: codemie | openai (and other OpenAI-compatible providers)
  - Selects the GenAI backend.

OpenAI-compatible settings
- gw.openai.baseUrl
  - Base URL of the OpenAI-compatible endpoint (optional if using the default).
- gw.openai.apiKey
  - API key used for authentication.
- gw.openai.model
  - Model identifier (e.g., gpt-4.1-mini, gpt-4o-mini, etc.), depending on your provider.

CodeMie settings
- gw.codemie.baseUrl
  - Base URL for CodeMie.
- gw.codemie.apiKey
  - API key/token for CodeMie authentication.
- gw.codemie.model
  - Model identifier supported by CodeMie.

General behavior
- gw.root
  - Default root directory.
- gw.instructions
  - Default instructions (can be overridden per run).
- gw.excludes
  - Default exclude patterns (comma-separated).
- gw.debug
  - true/false to enable debug logging.
- gw.timeoutSeconds
  - Request timeout to the provider.

4.3 Precedence (recommended)
When the same setting is provided in multiple places, a common precedence order is:
1) Command-line options
2) Java system properties (-D...)
3) Environment variables
4) gw.properties

4.4 Environment variable mapping
If your build supports environment variables, common mappings are:
- GW_PROVIDER -> gw.provider
- GW_ROOT -> gw.root
- GW_INSTRUCTIONS -> gw.instructions
- GW_EXCLUDES -> gw.excludes
- GW_DEBUG -> gw.debug
- GW_API_KEY -> provider API key (commonly mapped to gw.openai.apiKey or gw.codemie.apiKey depending on provider)
- GW_BASE_URL -> provider base URL (commonly mapped to gw.openai.baseUrl or gw.codemie.baseUrl)
- GW_MODEL -> provider model (commonly mapped to gw.openai.model or gw.codemie.model)

4.5 Java system property examples
- -Dgw.provider=openai
- -Dgw.openai.apiKey=...
- -Dgw.openai.baseUrl=https://api.example.com/v1
- -Dgw.openai.model=gpt-4o-mini
- -Dgw.excludes=target/**,.git/**
- -Dgw.debug=true


5) Examples
-----------
Example A: Use OpenAI-compatible provider with gw.properties
- gw.properties:
  - gw.provider=openai
  - gw.openai.apiKey=${GW_API_KEY}
  - gw.openai.baseUrl=https://api.example.com/v1
  - gw.openai.model=gpt-4o-mini
  - gw.excludes=target/**,.git/**,node_modules/**

- Run (Windows):
  - set GW_API_KEY=<your-key>
  - gw.bat --root . --instructions "Generate a developer guide in docs/"

- Run (Unix):
  - export GW_API_KEY="<your-key>"
  - ./gw.sh --root . --instructions "Generate a developer guide in docs/"

Example B: One-off run with explicit system properties (no gw.properties)
- Windows:
  - gw.bat -Dgw.provider=openai -Dgw.openai.apiKey=<key> -Dgw.openai.model=gpt-4o-mini --root . --instructions "Draft release notes"

- Unix:
  - ./gw.sh -Dgw.provider=openai -Dgw.openai.apiKey=<key> -Dgw.openai.model=gpt-4o-mini --root . --instructions "Draft release notes"

Example C: Excluding common folders
- Windows:
  - gw.bat --root . --instructions "Update documentation" --exclude "target/**" --exclude ".git/**" --exclude "node_modules/**"

- Unix:
  - ./gw.sh --root . --instructions "Update documentation" --exclude "target/**" --exclude ".git/**" --exclude "node_modules/**"


6) Troubleshooting & Support
----------------------------
Common issues
- Authentication errors (401/403)
  - Verify the API key/token is present and correct.
  - Confirm you are targeting the correct provider base URL.
  - Check whether the provider requires additional headers or organization/project IDs.

- Connection/timeouts
  - Confirm network connectivity and proxy settings.
  - Increase timeout (e.g., gw.timeoutSeconds) if supported.

- Missing files / unexpected scanning scope
  - Confirm --root is correct.
  - Review excludes; overly broad patterns can filter out needed files.

- Provider/model errors
  - Verify the configured model exists and is accessible for your account.

Logs and debug output
- Enable debug logging:
  - gw.properties: gw.debug=true
  - or environment: GW_DEBUG=true
  - or Java system property: -Dgw.debug=true
- Where logs appear depends on your packaging; typically they are printed to stdout/stderr. If your distribution writes to a log file, check the application’s working directory and any configured logging settings.


7) Contact & Documentation
--------------------------
- Refer to your project’s main documentation site (often under src/site or a published docs site) if available.
- If this project is maintained internally, follow your organization’s standard support channels (issue tracker, internal chat, or service desk).
