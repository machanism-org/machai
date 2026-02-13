GHOSTWRITER CLI - DISTRIBUTION FILES

1) Application Overview

Ghostwriter is an AI-assisted documentation engine and CLI that scans a project, applies mandatory @guidance constraints embedded in source and documentation files, and generates or updates documentation so it stays consistent with the current project state.

Typical use cases:
- Keep project documentation up to date (README, site pages, reports)
- Review documentation and code-related artifacts with language-aware rules
- Run repeatable documentation generation/update passes across many file types

Key features:
- Scans many file types, including source code, Markdown, and other project artifacts
- Treats inline @guidance blocks as mandatory constraints during generation
- Generates or updates documentation in repeatable runs
- Provides language-aware reviewers for multiple formats (for example: Java, Markdown, HTML, Python, TypeScript)

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI and compatible endpoints)


2) Installation Instructions

Prerequisites:
- Java 11 or newer
- (Optional) Maven 3.9+ for building from source
- Network access to the configured GenAI provider (if enabled)

Getting the CLI distribution:
- This folder is intended to be used with a packaged distribution that includes:
  - gw.jar (the runnable Ghostwriter CLI JAR)
  - gw.bat (Windows launcher)
  - gw.sh (Unix launcher)
  - gw.properties (configuration template)

Build from source (Maven):
1. Clone the repository:
   - git clone https://github.com/machanism-org/machai.git
   - cd machai
2. Build the Ghostwriter module:
   - mvn -pl ghostwriter -am clean verify
3. Locate the built JAR:
   - ghostwriter\target\ghostwriter-<version>.jar

Configuration prerequisites:
- Choose a provider/model in gw.properties:
  - genai=CodeMie:<model>
- Provide credentials via environment variables or Java system properties:
  - CodeMie: GENAI_USERNAME, GENAI_PASSWORD
  - OpenAI-compatible: OPENAI_API_KEY (and optionally OPENAI_BASE_URL)


3) How to Run

Files in this folder:
- gw.bat: Windows launcher for gw.jar
- gw.sh: Unix launcher for gw.jar
- gw.properties: configuration template (provider/model, root, instructions, excludes)

A) Windows (.bat) examples

Run (pass the target project directory as an argument):

  gw.bat C:\projects\my-project

Run with an explicit root directory (-r):

  gw.bat -r C:\projects\my-project

Target specific files with a glob:

  gw.bat "glob:**\*.md"

Set provider credentials as environment variables (in the same console session):

  set GENAI_USERNAME=your_codemie_username
  set GENAI_PASSWORD=your_codemie_password
  gw.bat C:\projects\my-project

Pass credentials as Java system properties (advanced):
- Edit gw.bat and use -DNAME=VALUE as needed, for example:

  java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar %~dp0\gw.jar C:\projects\my-project

B) Unix (.sh) examples

Run (pass the target project directory as an argument):

  ./gw.sh /path/to/my-project

Run with an explicit root directory (-r):

  ./gw.sh -r /path/to/my-project

Target specific files with a glob:

  ./gw.sh "glob:**/*.md"

Set provider credentials as environment variables:

  export GENAI_USERNAME=your_codemie_username
  export GENAI_PASSWORD=your_codemie_password
  ./gw.sh /path/to/my-project

C) Using options in gw.properties

You can set common options in gw.properties (and keep the command line short):
- root: root directory for processing (can be a parent folder containing multiple projects)
- instructions: additional instructions, separated by commas when providing more than one
- excludes: directories or files to exclude, separated by commas

Examples (gw.properties):
- root=/path/to/root
- instructions=review,fix security issues,generate report
- excludes=dir1,dir2,file1


4) Troubleshooting and Support

Common issues:
- Authentication errors:
  - Verify provider credentials are set (environment variables or -D system properties)
  - Verify the selected provider/model in gw.properties (genai=...)
  - For OpenAI-compatible providers, verify OPENAI_BASE_URL if you are not using the default OpenAI endpoint

- Nothing happens / no files updated:
  - Confirm the root/target path exists and points to the intended project
  - Check excludes settings in gw.properties to ensure you are not skipping everything
  - If using globs, verify the pattern matches (Windows example: "glob:**\\*.md")

- Missing gw.jar:
  - Ensure you are using a complete distribution that includes gw.jar
  - If building from source, confirm the module build produced ghostwriter\target\ghostwriter-<version>.jar

Logs and debug output:
- When reporting issues, include:
  - OS, Java version, how you launched the CLI (gw.bat/gw.sh/java -jar), and the exact arguments used
  - Any console output produced by the run


5) Contact and Documentation

Further information:
- Source repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Downloads: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
- Project site/documentation entry point: src\site\markdown\index.md
