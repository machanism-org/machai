Ghostwriter CLI - README

1. Application Overview

Ghostwriter is a CLI documentation engine that scans a project and updates documentation artifacts based on embedded @guidance blocks plus optional additional instructions.

Typical use cases:
- Regenerate or refresh documentation pages (for example, Maven Site Markdown) from guidance embedded in files.
- Apply repeatable documentation updates locally, in scripts, or in CI.
- Run guided file-processing tasks across a repository while excluding folders that should not be scanned.

Key features:
- Processes files using embedded @guidance directives and optional additional instructions.
- Supports directory/file exclusions.
- Supports an optional root directory for scanning (can be a parent folder that contains multiple projects).
- Designed to run non-interactively (scripts/CI) via a single command.
- Pluggable GenAI providers.

Supported GenAI providers (as configured in gw.properties):
- CodeMie
- OpenAI and OpenAI-compatible services (via OPENAI_API_KEY and optional OPENAI_BASE_URL)


2. Installation Instructions

Prerequisites:
- Java 17 runtime (recommended; used to build/run Machai modules).
- A GenAI provider account and credentials:
  - CodeMie: GENAI_USERNAME and GENAI_PASSWORD
  - OpenAI-compatible: OPENAI_API_KEY (and optionally OPENAI_BASE_URL)

What is included in this folder:
- gw.jar (placed next to these scripts when packaged)
- gw.properties (example configuration)
- gw.sh (Unix launcher)
- gw.bat (Windows launcher)

Build from source (to produce gw.jar):
1) From the repository root:
   mvn -U clean install
2) Package Ghostwriter:
   cd ghostwriter
   mvn -Ppack package
3) The packaged application is produced as:
   ghostwriter/target/gw.jar

Deploy/run layout:
- Place gw.jar in the same directory as gw.sh/gw.bat and gw.properties (this directory).


3. How to Run

Basic (direct Java):
- From the directory containing gw.jar:
  java -jar gw.jar [options]

Using the provided launchers:
- Windows:
  gw.bat [options]

- Unix:
  ./gw.sh [options]

Configuration

A) Using environment variables
You can define any property from gw.properties as an environment variable.
Common variables:
- GENAI_USERNAME
- GENAI_PASSWORD
- OPENAI_API_KEY
- OPENAI_BASE_URL

Windows example:
  set GENAI_USERNAME=your_codemie_username
  set GENAI_PASSWORD=your_codemie_password
  gw.bat --root ..

Unix example:
  export GENAI_USERNAME=your_codemie_username
  export GENAI_PASSWORD=your_codemie_password
  ./gw.sh --root ..

B) Using Java system properties (-D)
You can also pass settings as Java system properties:
- Windows:
  java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar %~dp0\gw.jar --root ..

- Unix:
  java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar "$(dirname "$0")/gw.jar" --root ..

Provider selection (gw.properties)
- Select provider + model with:
  genai=CodeMie:gpt-5-2-2025-12-11

- For OpenAI-compatible providers, set:
  OPENAI_API_KEY=...
  OPENAI_BASE_URL=https://your-openai-compatible-endpoint

Common options
The following options are supported as configuration keys (see gw.properties) and are typically also provided as CLI arguments depending on your packaging:
- root: (Optional) Root directory for processing.
- instructions: (Optional) Additional instructions. If multiple, separate by commas.
- excludes: (Optional) Directories/files to exclude. If multiple, separate by commas.

Examples

1) Process a parent directory as the root
- Windows:
  gw.bat --root ..
- Unix:
  ./gw.sh --root ..

2) Add one or more additional instructions
- Windows:
  gw.bat --root .. --instructions "review,generate report"
- Unix:
  ./gw.sh --root .. --instructions "review,generate report"

3) Exclude directories/files
- Windows:
  gw.bat --root .. --excludes "target,.git,node_modules"
- Unix:
  ./gw.sh --root .. --excludes "target,.git,node_modules"

4) Combine root, instructions, and excludes
- Windows:
  gw.bat --root .. --instructions "review,fix security issues" --excludes "target,.git"
- Unix:
  ./gw.sh --root .. --instructions "review,fix security issues" --excludes "target,.git"


4. Troubleshooting and Support

Common issues:
- Authentication failures:
  - Verify provider credentials are set (GENAI_USERNAME/GENAI_PASSWORD for CodeMie; OPENAI_API_KEY for OpenAI-compatible).
  - If using an OpenAI-compatible endpoint, verify OPENAI_BASE_URL.
- Wrong provider/model:
  - Confirm the genai=... value in gw.properties matches the provider you intend to use.
- No files updated / missing expected output:
  - Check that the target files contain embedded @guidance blocks.
  - Ensure --root points to the correct directory.
  - Ensure excludes are not filtering out the relevant folders.

Logs and debug:
- Run from a terminal and capture stdout/stderr for logs.
- If your distribution supports debug flags, re-run with the application debug option(s) enabled.

Support:
- Issue tracker: https://github.com/machanism-org/machai/issues


5. Contact and Documentation

Project website:
- https://machai.machanism.org

Repository and issues:
- https://github.com/machanism-org/machai
- https://github.com/machanism-org/machai/issues

Maintainer:
- Viktor Tovstyi (viktor.tovstyi@gmail.com)
