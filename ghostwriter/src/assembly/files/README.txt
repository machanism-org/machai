Ghostwriter CLI (gw)
===================

1) Application Overview
-----------------------
Ghostwriter CLI ("gw") is a command-line application that scans a source tree and uses a GenAI provider to generate and/or refine project documentation and text assets in a consistent, automated way.

Main purpose
- Automate creation and maintenance of text-based artifacts (e.g., README files, changelogs, docs, release notes, ADRs, code summaries) based on your repository content and explicit instructions.

Key features
- Runs from the command line with a configurable root directory.
- Uses a pluggable GenAI provider (e.g., CodeMie, OpenAI-compatible services).
- Supports instructions/prompts for controlling output.
- Supports excludes to omit files/directories from processing.
- Works well in CI/CD pipelines or local developer workflows.

Typical use cases
- Generate documentation for a repository before release.
- Keep documentation synchronized with code changes.
- Produce consistent content across multiple modules.
- Batch-process large projects while excluding build output and vendor folders.

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (any provider exposing an OpenAI-compatible API)


2) Installation Instructions
----------------------------
Prerequisites
- Java: a supported JDK/JRE installed (use the project-required Java version; commonly Java 17+).
- Network access to the configured GenAI provider endpoint.
- Provider credentials available via environment variables, Java system properties, or gw.properties.
- Optional: a gw.properties configuration file (recommended).

Build / install
- If you have a packaged distribution (ZIP/TGZ), extract it and use the provided startup scripts.
- If building from source, build the project with your standard build tool (e.g., Maven/Gradle) to produce the runnable artifact and scripts.

Configuration files
- gw.properties: place in the working directory, project root, or another location supported by your setup, then point to it via environment variable or Java system property as needed.


3) How to Run
-------------
Basic usage (conceptual)
- Run the gw launcher script and provide:
  - root directory to process
  - instructions (prompt) to guide output
  - excludes (glob/paths) to skip certain content

Windows (.bat) examples
- Run with a root directory:
  gw.bat --root "C:\work\my-repo"

- Run with instructions and excludes:
  gw.bat --root "C:\work\my-repo" --instructions "Generate README files" --exclude "**/target/**" --exclude "**/.git/**"

- Specify a custom properties file:
  set GW_PROPERTIES=C:\work\my-repo\gw.properties
  gw.bat --root "C:\work\my-repo"

- Provide configuration via Java system properties (if running java directly):
  java -Dgw.root="C:\work\my-repo" -Dgw.instructions="Generate docs" -jar ghostwriter-cli.jar

Unix (.sh) examples
- Run with a root directory:
  ./gw.sh --root "/home/user/my-repo"

- Run with instructions and excludes:
  ./gw.sh --root "/home/user/my-repo" --instructions "Generate README files" --exclude "**/target/**" --exclude "**/.git/**"

- Specify a custom properties file:
  export GW_PROPERTIES=/home/user/my-repo/gw.properties
  ./gw.sh --root "/home/user/my-repo"

- Provide configuration via Java system properties (if running java directly):
  java -Dgw.root=/home/user/my-repo -Dgw.instructions="Generate docs" -jar ghostwriter-cli.jar

Environment variables and Java system properties
- You can configure Ghostwriter via:
  - Environment variables (e.g., GW_PROVIDER, GW_API_KEY, GW_BASE_URL, GW_INSTRUCTIONS, GW_EXCLUDES, GW_ROOT)
  - Java system properties prefixed with gw.* (e.g., -Dgw.provider=..., -Dgw.apiKey=..., -Dgw.baseUrl=...)
  - gw.properties entries (see next section)

Options
- --root: root directory to scan/process.
- --instructions: instruction text or reference used to guide output.
- --exclude: repeatable; one or more patterns/paths to skip.

Note: the exact option names supported by your build may differ. Prefer gw.properties for stable configuration and use CLI flags for overrides.


4) Configuration
----------------
The gw.properties file
- gw.properties is the primary configuration file for Ghostwriter CLI.
- It defines the default GenAI provider and all required connection/authentication settings.
- It also defines default instructions, exclude rules, and other behavior.

Common properties (typical)
- gw.provider
  - GenAI provider identifier.
  - Examples: codeMie, openaiCompatible

- gw.baseUrl
  - API base URL for OpenAI-compatible services.
  - Example: https://api.example.com/v1

- gw.apiKey
  - API key/token for the provider.

- gw.model
  - Model name/identifier.
  - Examples: gpt-4o-mini, gpt-4.1, your-provider-model

- gw.timeoutSeconds
  - Network timeout in seconds.

- gw.root
  - Root directory to scan/process.

- gw.instructions
  - Default instructions to apply to the run.

- gw.excludes
  - Exclude patterns, separated by commas or newlines.
  - Examples: **/target/**, **/.git/**, **/node_modules/**

- gw.logLevel
  - Logging level.
  - Examples: INFO, DEBUG

Provider-specific properties
- CodeMie may use provider-specific endpoints/keys. Configure them via the same pattern (gw.baseUrl, gw.apiKey, gw.model) unless your distribution documents different keys.

Overriding properties
- Environment variables:
  - GW_PROVIDER overrides gw.provider
  - GW_BASE_URL overrides gw.baseUrl
  - GW_API_KEY overrides gw.apiKey
  - GW_MODEL overrides gw.model
  - GW_TIMEOUT_SECONDS overrides gw.timeoutSeconds
  - GW_ROOT overrides gw.root
  - GW_INSTRUCTIONS overrides gw.instructions
  - GW_EXCLUDES overrides gw.excludes
  - GW_LOG_LEVEL overrides gw.logLevel

- Java system properties:
  - -Dgw.provider=...
  - -Dgw.baseUrl=...
  - -Dgw.apiKey=...
  - -Dgw.model=...
  - -Dgw.timeoutSeconds=...
  - -Dgw.root=...
  - -Dgw.instructions=...
  - -Dgw.excludes=...
  - -Dgw.logLevel=...

Precedence (recommended)
- CLI flags override Java system properties override environment variables override gw.properties.


5) Examples
-----------
Example A: OpenAI-compatible provider via gw.properties
- gw.properties:
  gw.provider=openaiCompatible
  gw.baseUrl=https://api.example.com/v1
  gw.apiKey=${ENV:GW_API_KEY}
  gw.model=gpt-4o-mini
  gw.root=.
  gw.instructions=Generate/refresh README and docs. Keep changes minimal.
  gw.excludes=**/.git/**,**/target/**,**/node_modules/**

- Run:
  gw.bat --root "C:\work\my-repo"
  ./gw.sh --root "/home/user/my-repo"

Example B: Provide instructions ad hoc
- Windows:
  gw.bat --root "C:\work\my-repo" --instructions "Write a concise README for the project" --exclude "**/target/**"

- Unix:
  ./gw.sh --root "/home/user/my-repo" --instructions "Write a concise README for the project" --exclude "**/target/**"

Example C: Enable debug logging
- Windows:
  set GW_LOG_LEVEL=DEBUG
  gw.bat --root "C:\work\my-repo"

- Unix:
  export GW_LOG_LEVEL=DEBUG
  ./gw.sh --root "/home/user/my-repo"


6) Troubleshooting & Support
----------------------------
Authentication / authorization errors
- Verify the API key/token is set and has access to the configured model.
- Confirm you are pointing at the correct base URL for your provider.
- If using gw.properties, verify no trailing spaces and that property names match.

Missing files or unexpected omissions
- Check exclude patterns; broad globs (e.g., **/*) can unintentionally exclude content.
- Confirm --root points to the intended directory.

Network or timeout issues
- Verify outbound connectivity and proxy settings.
- Increase gw.timeoutSeconds.

Where to find logs
- Logs are typically written to stdout/stderr.
- To increase detail, set gw.logLevel=DEBUG (or GW_LOG_LEVEL=DEBUG).


7) Contact & Documentation
--------------------------
- Project documentation: see the repository docs (e.g., src/site or published site if available).
- Support: use your organization’s standard support channel for this project (issues tracker, internal chat, or helpdesk).