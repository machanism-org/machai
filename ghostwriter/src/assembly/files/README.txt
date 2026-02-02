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
  - $env:GENAI_USERNAME=<your-username>
  - $env:GENAI_PASSWORD=<your-password>
  - gw.bat 

- Windows (CMD):
  - set GENAI_USERNAME=<your-username>
  - set GENAI_PASSWORD=<your-password>
  - gw.bat 

- Unix:
  - export GENAI_USERNAME=<your-username>
  - export GENAI_PASSWORD=<your-password>
  - ./gw.sh 

Passing options: instructions, excludes, root directory
- Root directory (where Ghostwriter reads/writes):
  - --root <path>

- Instructions (what you want the tool to do):
  - --instructions "<path/url>"

- Excludes (repeatable or comma-separated, depending on your CLI build):
  - --exclude "target,logs" 

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

4.3 Precedence (recommended)
When the same setting is provided in multiple places, a common precedence order is:
1) Command-line options
2) Java system properties (-D...)
3) Environment variables
4) gw.properties

4.4 Environment variable mapping
If your build supports environment variables, common mappings are:
- GW_ROOT -> gw.root
- GW_INSTRUCTIONS -> gw.instructions
- GW_EXCLUDES -> gw.excludes
...

4.5 Java system property examples
- -Dgenai=...
- -Droot=...
- -Dgw.excludes=target/**,.git/**
...


5) Troubleshooting & Support
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

6) Contact & Documentation
--------------------------
- Refer to your project’s main documentation site (often under src/site or a published docs site): http://machai.machanism.org/ghostwriter/index.html
- If this project is maintained internally, follow your organization’s standard support channels (issue tracker, internal chat, or service desk).
