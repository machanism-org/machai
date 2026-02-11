Ghostwriter CLI - README

1) Application Overview

Ghostwriter is a documentation engine and CLI that scans a project, enforces mandatory @guidance constraints embedded in source and documentation files, and uses GenAI to generate or update documentation artifacts consistently across a repository.

Typical use cases:
- Generate or refresh project documentation (for example Markdown files) from the current code and repo state
- Apply mandatory @guidance constraints to keep output consistent and compliant
- Repeatable runs to keep documentation aligned with the current project state

Key capabilities:
- Scans a project for documentation targets across multiple file types (source code, docs, site content, and other relevant assets)
- Interprets inline @guidance blocks as mandatory constraints
- Generates and updates Markdown and other documentation artifacts
- Supports repeatable runs

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (OpenAI or compatible endpoints)


2) Installation Instructions

Prerequisites:
- Java 11 or newer
- Maven 3.9+ recommended (for building from source)
- Network access to the configured GenAI provider

Obtain Ghostwriter:
- Prebuilt distribution: see Downloads
  https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
- From source (repository):
  https://github.com/machanism-org/machai

Build from source (Maven):
- Clone the repository:
  cmd.exe
    git clone https://github.com/machanism-org/machai.git
    cd machai

- Build the Ghostwriter module:
  cmd.exe
    mvn -pl ghostwriter -am clean verify


3) How to Run

This folder typically contains a runnable gw.jar plus helper scripts and configuration:
- gw.bat   (Windows launcher)
- gw.sh    (Unix launcher)
- gw.properties (configuration)
- g\*      (ready-to-use instruction prompts)

Configuration options

A) gw.properties
Edit gw.properties next to gw.jar to configure the provider and common options.

Provider selection:
- genai=CodeMie:<model>
  Example:
    genai=CodeMie:gpt-5-2-2025-12-11

CodeMie credentials (if using CodeMie):
- GENAI_USERNAME
- GENAI_PASSWORD

OpenAI-compatible settings (if using OpenAI or compatible providers):
- OPENAI_API_KEY
- OPENAI_BASE_URL (optional; required for non-OpenAI endpoints)

Additional processing options (may be used by the CLI):
- root: root directory for processing
- instructions: additional instruction names/phrases (comma-separated)
- excludes: directories/files to exclude (comma-separated)

B) Environment variables
You can set any property from gw.properties as environment variables.

Windows (cmd.exe):
  set GENAI_USERNAME=your_codemie_username
  set GENAI_PASSWORD=your_codemie_password

Unix (bash):
  export GENAI_USERNAME=your_codemie_username
  export GENAI_PASSWORD=your_codemie_password

C) Java system properties
Alternatively, pass properties via -D.

Windows:
  java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar <args>

Unix:
  java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar gw.jar <args>


Running via the helper scripts

Windows (.bat):
- Run with any arguments forwarded to the jar:
  gw.bat <args>

Unix (.sh):
- Make executable and run:
  chmod +x gw.sh
  ./gw.sh <args>


Running the jar directly

Example (Windows) running against a local project directory:
  java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar C:\projects\my-project

Example using an explicit root directory:
  java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar -r C:\projects\my-project

Example targeting files with a glob:
  java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar "glob:**\*.md"


Options and examples (root, instructions, excludes)

Note: The distribution configuration file documents these options:
- root=/path/to/root
- instructions=review,fix security issues,generate report
- excludes=dir1,dir2,file1

Example (Windows, cmd.exe):
  set GENAI_USERNAME=your_codemie_username
  set GENAI_PASSWORD=your_codemie_password
  gw.bat -r C:\projects\my-project -instructions "review,generate report" -excludes "target,.git"

Example (Unix, bash):
  export GENAI_USERNAME=your_codemie_username
  export GENAI_PASSWORD=your_codemie_password
  ./gw.sh -r /home/me/my-project -instructions "review,generate report" -excludes "target,.git"


Using the included instruction prompts (g\*)

This distribution includes example instruction prompt files under g\:
- g\create_tests
  Create unit tests under src/test/java for the corresponding src/main/java package, targeting at least 90% coverage.
- g\to_java21
  Migrate a Java 17 codebase to Java 21 (including build/config updates, testing, and documentation of changes).

How you use these files depends on your CLI arguments and workflow. Common approaches are:
- Copy/paste the prompt text into your invocation as "instructions" input.
- Maintain your own instruction files and reference them in your run configuration.


4) Troubleshooting and Support

Common issues
- Authentication errors (CodeMie/OpenAI):
  - Verify GENAI_USERNAME/GENAI_PASSWORD or OPENAI_API_KEY are set.
  - If using an OpenAI-compatible provider, confirm OPENAI_BASE_URL is correct.
- Provider/model errors:
  - Ensure genai in gw.properties matches the provider and an available model.
- No output or missing updates:
  - Confirm the root directory and any globs point at the intended project files.
  - Review excludes to ensure you did not exclude required folders.

Logs and debug
- If the CLI provides a debug/verbose flag, enable it to see more detail.
- Otherwise, re-run with additional diagnostics by adding more context to your instructions and verifying configuration values.


5) Contact and Documentation

Documentation and project links:
- Source repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Downloads: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
