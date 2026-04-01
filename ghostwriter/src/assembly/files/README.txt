Ghostwriter CLI

Application Overview

Machai Ghostwriter is a guided, AI-assisted processing engine delivered as a Java CLI. It scans a repository (source code, tests, documentation, diagrams, and other assets), extracts embedded "@guidance:" directives (or applies a default prompt when guidance is absent), and executes the resulting prompts against a configured GenAI provider to generate and maintain project-wide documentation and code improvements.

Conceptual foundation (Guided File Processing):
https://www.machanism.org/guided-file-processing/index.html

Key features
- Guidance-first prompting via embedded "@guidance:" directives in project files.
- Repository-scale scanning of directories and matchers:
  - directory paths
  - glob: patterns (e.g., glob:**/*.java)
  - regex: patterns
- Project-aware prompts with injected repository context (layout/structure).
- Optional system instructions and default prompts (plain text, URLs, or file: references).
- Excludes support (comma-separated; exact paths plus glob:/regex: matchers).
- Optional multi-threaded processing (--threads).
- Optional logging of provider inputs per processed file (--logInputs).
- Act mode for running predefined prompts (--act) and custom act bundles (--acts).

Typical use cases
- Generate or update documentation across a repository.
- Apply consistent, guidance-driven refactors or improvements across many files.
- Run as part of a scripted workflow or CI/CD pipeline.

Supported GenAI providers
- CodeMie
- OpenAI-compatible services (provider/model selection like OpenAI:gpt-5.1)


Installation Instructions

Prerequisites
- Java 8
- A configured GenAI provider/model:
  - Property: gw.model
  - Or CLI option: -m / --model
- Credentials for your selected provider (exact names depend on provider implementation)
- (Optional) A configuration file:
  - gw.properties
  - You may override the configuration file path with: -Dgw.config=...

Build / install
- Build from source with Maven from the project root.
- If you downloaded the distribution, place gw.properties in the Ghostwriter home directory (see below), or point to it with -Dgw.config=....

Configuration file location
- Default configuration file name: gw.properties
- Ghostwriter home directory resolution:
  1) -Dgw.home=<path> if set
  2) otherwise, projectDir (if provided)
  3) otherwise, current working directory
- Override config file name/path (resolved relative to gw.home):
  -Dgw.config=gw.properties
  -Dgw.config=conf/gw.properties


How to Run

Entry point
- org.machanism.machai.gw.processor.Ghostwriter

Jar invocation
java -jar gw.jar <scanDir> [options]

scanDir rules
- <scanDir> may be:
  - a relative directory path (relative to the current project directory)
  - a glob: matcher
  - a regex: matcher
- If an absolute scan path is provided, it must be located within the root project directory.
- If no <scanDir> is provided, Ghostwriter uses gw.scanDir if configured; otherwise it scans the current working directory.

Configuration properties (from org.machanism.machai.gw.processor.Ghostwriter)

1) project.dir (ProjectLayout.PROJECT_DIR_PROP_NAME)
- Description: Root directory for file processing when -d/--projectDir is not provided.
- Default: If not set, current working directory (SystemUtils.getUserDir()).
- Usage context: Determines the root directory used for scanning and for validating absolute scan paths.

2) gw.model (Ghostwriter.MODEL_PROP_NAME)
- Description: GenAI provider and model to use, in the form provider:model (e.g., OpenAI:gpt-5.1).
- Default: None; the CLI fails fast if not configured.
- Usage context: Set in gw.properties, or override via -m/--model.

3) instructions (Ghostwriter.INSTRUCTIONS_PROP_NAME)
- Description: Optional system instructions provided to the GenAI provider.
- Default: Unset.
- Usage context:
  - Set in gw.properties.
  - Override with -i/--instructions.
  - If -i/--instructions is used without a value, Ghostwriter reads multi-line instructions from stdin.
  - Each line is processed: blank lines preserved; http(s):// lines fetched and included; file: lines loaded; other lines included as-is.
  - Multi-line stdin input ends when a line does not end with "\\".

4) gw.excludes (Ghostwriter.EXCLUDES_PROP_NAME)
- Description: Comma-separated list of directories/files to exclude from processing.
- Default: Unset.
- Usage context: Set in gw.properties, or override with -e/--excludes.

5) acts.location (Ghostwriter.ACTS_LOCATION_PROP_NAME)
- Description: Directory containing predefined act prompt files.
- Default: Unset.
- Usage context: Set in gw.properties; can be overridden with -as/--acts.

6) gw.act (Ghostwriter.ACT_PROP_NAME)
- Description: Default act prompt used for Act mode and for the default prompt when Act mode is enabled.
- Default: Unset.
- Usage context:
  - Used when running with -a/--act; can be set in gw.properties.
  - If -a/--act is used without a value, Ghostwriter reads a multi-line act prompt from stdin.

7) gw.threads (Ghostwriter.THREADS_PROP_NAME)
- Description: Degree of concurrency for processing (worker thread count).
- Default: Unset.
- Usage context: Set in gw.properties; can be overridden with -t/--threads <count>.

8) gw.scanDir (Ghostwriter.SCAN_DIR_PROP_NAME)
- Description: Default scan directory/pattern if no <scanDir> argument is provided.
- Default: If unset and no scanDir args are provided, Ghostwriter uses the current working directory absolute path.
- Usage context: Set in gw.properties to avoid passing <scanDir> every run.

9) gw.nonRecursive (Ghostwriter.NONRECURSIVE_PROP_NAME)
- Description: Controls whether scanning should be non-recursive.
- Default: Unset.
- Usage context: Used by processor implementation(s) to adjust scan behavior.

10) inputs (Ghostwriter.INPUTS_PROPERTY_NAME / Genai.LOG_INPUTS_PROP_NAME)
- Description: Enables request input logging to dedicated log files.
- Default: false.
- Usage context:
  - Set in gw.properties (inputs=true).
  - Or enable via -l/--logInputs.

11) gw.interactive (Ghostwriter.INTERACTIVE_MODE_PROP_NAME)
- Description: Toggles interactive behavior in the processor layer (if supported by the configured processor).
- Default: Unset.
- Usage context: Read by processor implementation(s).

Additional Java system properties
- gw.config (Ghostwriter.CONFIG_PROP_NAME)
  - Description: Override the Ghostwriter configuration file path (resolved relative to gw.home).
  - Default: gw.properties
  - Usage: java -Dgw.config=conf/gw.properties -jar gw.jar ...

- gw.home (Ghostwriter.HOME_PROP_NAME)
  - Description: Home directory used to locate gw.properties by default.
  - Default: projectDir if provided; otherwise current working directory.
  - Usage: java -Dgw.home=. -jar gw.jar ...

CLI options summary
- -h, --help: Show help and exit.
- -d, --projectDir <path>: Root directory for file processing.
- -t, --threads <count>: Degree of concurrency for processing.
- -m, --model <provider:model>: GenAI provider/model selection.
- -i, --instructions[=<text|url|file:...>]: System instructions (or multi-line stdin if no value).
- -e, --excludes <csv>: Comma-separated excludes.
- -l, --logInputs: Log LLM request inputs to dedicated log files.
- -as, --acts <path>: Directory containing predefined act prompt files.
- -a, --act[=<prompt>]: Enable Act mode. If no value: multi-line stdin (line continuation: "\\").

Examples

Windows (cmd)
- Scan a directory:
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Scan by glob pattern:
  java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1

- Scan by regex pattern:
  java -jar gw.jar "regex:^.*/[^/]+\\.java$" -m OpenAI:gpt-5.1

- Provide system instructions from a file:
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i "file:./instructions.txt"

- Provide system instructions via stdin (input ends when a line does not end with "\\"):
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i

- Exclude build outputs and VCS metadata:
  java -jar gw.jar src -m OpenAI:gpt-5.1 -e "glob:**/target/**,glob:**/.git/**"

- Use custom project root:
  java -jar gw.jar "glob:**/*.md" -d . -m OpenAI:gpt-5.1

- Act mode with prompt from config (gw.act):
  java -jar gw.jar src -m OpenAI:gpt-5.1 -a

- Act mode with inline prompt:
  java -jar gw.jar src -m OpenAI:gpt-5.1 -a "Update the README files across the project"

Unix (sh)
- Scan a directory:
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Scan by glob pattern:
  java -jar gw.jar 'glob:**/*.md' -m OpenAI:gpt-5.1

- Scan by regex pattern:
  java -jar gw.jar 'regex:^.*/[^/]+\\.java$' -m OpenAI:gpt-5.1

- Provide system instructions from a file:
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i 'file:./instructions.txt'

- Exclude build outputs and VCS metadata:
  java -jar gw.jar src -m OpenAI:gpt-5.1 -e 'glob:**/target/**,glob:**/.git/**'


Troubleshooting & Support

Common issues
- No GenAI provider/model configured
  - Set gw.model in gw.properties, or pass -m/--model.

- Authentication errors
  - Ensure provider credentials are set for the selected provider.

- Scan path errors
  - If providing an absolute scan path, ensure it is inside the configured root project directory.
  - Prefer quoting glob:/regex: expressions.

Logs and debug
- Standard logs are written via SLF4J (logging backend configuration depends on the runtime classpath).
- Provider usage is logged at the end of execution.
- If -l/--logInputs is enabled, Ghostwriter writes dedicated log files containing provider request inputs.


Contact & Documentation

Project site
https://machai.machanism.org/ghostwriter/index.html

Source repository (Machai mono-repo)
https://github.com/machanism-org/machai

Maven Central
https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
