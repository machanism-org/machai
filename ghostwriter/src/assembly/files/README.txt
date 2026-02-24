Ghostwriter CLI (Machai)
========================

1) Application Overview
-----------------------
Ghostwriter is a guidance-driven, repository-scale documentation and transformation engine.
It scans your repository (source code, docs, project-site Markdown, build metadata, and other
artifacts), extracts embedded "@guidance:" directives, and uses a configured GenAI provider to
apply consistent improvements across many files in a repeatable, reviewable, CI-friendly way.

Typical use cases:
- Repository-wide documentation updates and alignment.
- Consistent refactoring and conventions enforcement across many files.
- Batch processing of project-site content (e.g., src/site Markdown) alongside code.

Key features:
- Processes many project file types (not just Java).
- Extracts embedded "@guidance:" directives via pluggable, file-type-aware reviewers.
- Scan targets can be a directory, "glob:..." matcher, or "regex:..." matcher.
- Maven multi-module traversal (child modules first).
- Optional multi-threaded module processing.
- Optional logging of composed LLM request inputs per processed file.
- Supports global instructions and default guidance loaded from plain text, URLs, or local files.

Supported GenAI providers:
- CodeMie
- OpenAI-compatible services (including OpenAI)


2) Installation Instructions
----------------------------
Prerequisites:
- Java:
  - Build target: Java 8.
  - Runtime: a Java runtime compatible with your selected GenAI provider/client (you may run on
    newer JREs while still building for Java 8).
- GenAI provider credentials and access.
- Network access to the provider endpoint (when applicable).

Configuration files:
- gw.properties (see "files/gw.properties").
  - By default Ghostwriter looks for "gw.properties".

How to obtain/build:
- If you have a prebuilt distribution, run the provided gw.jar.
- If building from source (Maven project):
  - mvn -DskipTests package
  - Use the produced CLI jar (name/location depends on your build output).


3) How to Run
-------------
Basic usage:
  java -jar gw.jar <scanTarget> [options]

Scan target (<scanTarget>) may be:
- A directory path (relative to the project root), or
- A glob path matcher:   "glob:**/*.java"
- A regex path matcher:  "regex:^.*\\/[^\\/]+\\.java$"

Windows examples (.bat/cmd.exe):
- Scan a folder:
  java -jar gw.jar src\main\java

- Scan with glob pattern:
  java -jar gw.jar "glob:**/*.java"

- Full example:
  java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l

Unix examples (.sh):
- Scan a folder:
  java -jar gw.jar src/main/java

- Scan with glob pattern:
  java -jar gw.jar 'glob:**/*.java'

- Full example:
  java -jar gw.jar 'glob:**/*.java' -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l


3.1) Configuration properties (system properties / gw.properties)
----------------------------------------------------------------
Ghostwriter reads configuration from system properties and/or a gw.properties file.

Property keys (from org.machanism.machai.gw.processor.Ghostwriter):

- gw.config
  Description: Name/path of the properties file to load from the home directory.
  Default: gw.properties
  Context: Used to locate the configuration file under gw.home.

- gw.home
  Description: Home directory used as the execution base for resolving the configuration file.
  Default: If not set: gw.rootDir; otherwise the current user directory.
  Context: Ghostwriter sets this system property internally once resolved.

- gw.rootDir
  Description: Root directory for file processing.
  Default: Current user directory if not set.
  Context: Used to constrain/resolve scan targets; also used as a default scan target if none is provided.

- gw.genai
  Description: GenAI provider/model identifier.
  Default: None (must be set via gw.properties or -a/--genai).
  Context: Required; format is provider:model (e.g., OpenAI:gpt-5.1).

- gw.instructions
  Description: Global system instructions appended to every prompt.
  Default: None.
  Context: Can be set via config or CLI; supports plain text, URL includes, and file includes.

- gw.guidance
  Description: Default (fallback) guidance used when a file contains no embedded "@guidance:" directives.
  Default: None.
  Context: Can be set via config or CLI; supports plain text, URL includes, and file includes.

- gw.excludes
  Description: Comma-separated list of directories to exclude from processing.
  Default: None.
  Context: Used to skip folders during scans.

- gw.threads
  Description: Enables multi-threaded module processing.
  Default: false
  Context: Improves performance when the provider implementation is thread-safe.

- gw.logInputs
  Description: Logs composed LLM request inputs to dedicated log files.
  Default: false
  Context: Useful for auditing and troubleshooting prompt composition.


3.2) Command-line options
-------------------------
Options (from Ghostwriter CLI):
- -h, --help
  Description: Show help and exit.

- -r, --root <path>
  Description: Specify the path to the root directory for file processing.
  Default: gw.rootDir (if set), else the current user directory.

- -t, --threads[=<true|false>]
  Description: Enable multi-threaded processing. If provided without a value, it enables it.
  Default: From gw.threads (default false).

- -a, --genai <provider:model>
  Description: Set the GenAI provider and model (e.g., OpenAI:gpt-5.1).
  Default: From gw.genai.

- -i, --instructions[=<text|url|file:...>]
  Description: Provide global system instructions.
  Default: From gw.instructions.
  Notes:
    - If used with no value, Ghostwriter prompts for multi-line text on stdin.
    - Input processing is line-based:
      * blank lines preserved
      * http:// or https:// lines are fetched and included
      * file:... lines are read and included
      * other lines are included as-is

- -g, --gw.guidance[=<text|url|file:...>]
  Description: Provide default (fallback) guidance.
  Default: From gw.guidance.
  Notes: Same include behavior as --instructions; stdin prompt is used if no value is provided.

- -e, --excludes <csv>
  Description: Comma-separated list of directories to exclude.
  Default: From gw.excludes.

- -l, --logInputs
  Description: Enable composed-input logging.
  Default: From gw.logInputs (default false).

Using environment variables / Java system properties:
- Java system properties:
  -Dgw.genai=OpenAI:gpt-5.1
  -Dgw.rootDir=.
  -Dgw.home=.
  -Dgw.config=gw.properties

  Example (Windows):
    java -Dgw.rootDir=. -Dgw.home=. -Dgw.genai=OpenAI:gpt-5.1 -jar gw.jar "glob:**/*.md" -i file:instructions.txt -g file:default-guidance.txt

  Example (Unix):
    java -Dgw.rootDir=. -Dgw.home=. -Dgw.genai=OpenAI:gpt-5.1 -jar gw.jar 'glob:**/*.md' -i file:instructions.txt -g file:default-guidance.txt

- Environment variables:
  Provider-specific variables can be set (see files/gw.properties), for example:
    * CodeMie: GENAI_USERNAME, GENAI_PASSWORD
    * OpenAI-compatible: OPENAI_API_KEY, OPENAI_BASE_URL


4) Troubleshooting & Support
----------------------------
Common issues:
- "No GenAI provider/model configured":
  - Set gw.genai in gw.properties, or pass -a/--genai, or use -Dgw.genai=...

- Authentication/authorization failures:
  - Ensure the provider credentials are set (e.g., GENAI_USERNAME/GENAI_PASSWORD or OPENAI_API_KEY).
  - Verify endpoint configuration for OpenAI-compatible services (OPENAI_BASE_URL).

- Scan target issues (files not found / unexpected scope):
  - Confirm -r/--root points at your repository root.
  - Prefer quoting glob/regex patterns.
  - Use -e/--excludes to skip build output folders (e.g., target) and VCS dirs.

Logs and debug:
- Ghostwriter logs through SLF4J; configure your logging backend (e.g., logback/log4j binding) to
  enable DEBUG/TRACE output.
- If -l/--logInputs (or gw.logInputs=true) is enabled, Ghostwriter will write composed request
  inputs to dedicated log files for auditing prompt composition.


5) Contact & Documentation
--------------------------
Further documentation:
- Official platform: https://machai.machanism.org/ghostwriter/
- Guided File Processing (conceptual foundation): https://www.machanism.org/guided-file-processing/index.html
- Source repository (SCM): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter

Included configuration example:
- files/gw.properties
