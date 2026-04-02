Ghostwriter CLI

Application Overview

Machai Ghostwriter is a repository-wide automation engine delivered as a Java CLI. It scans a project directory (source code, tests, documentation, diagrams, and other assets), extracts embedded "@guidance" directives from files, and uses a configured GenAI provider/model to generate or update content in-place—turning your repository into a self-describing, continuously maintained knowledge base.

Conceptual foundation (Guided File Processing):
https://www.machanism.org/guided-file-processing/index.html

Key features
- Scans directories or patterns (raw paths, glob: patterns, regex: patterns).
- Extracts embedded "@guidance" directives from project files and uses them as the source of truth.
- Supports system instructions from inline text, URL content, or file: references.
- Excludes paths via configuration or CLI.
- Optional Act mode for executing predefined prompts.
- Configurable degree of concurrency via worker threads (--threads).
- Optional request input logging to dedicated log files (--logInputs).

Typical use cases
- Generate or update documentation across a repository.
- Apply consistent, guidance-driven refactors or improvements across many files.
- Run as part of a scripted workflow or CI/CD pipeline.

Supported GenAI providers
- CodeMie
- OpenAI-compatible services


Installation Instructions

Prerequisites
- Java 8.
- Network access to your selected GenAI provider endpoint (depending on provider).
- A configured GenAI provider/model:
  - Property: gw.model
  - Or CLI option: -m / --model
- (Optional) Configuration file gw.properties in the resolved Ghostwriter home directory.

Build / install
- Build from source with Maven from the repository root.
- If you downloaded the distribution, place gw.properties in the Ghostwriter home directory, or point to it with -Dgw.config=....

Configuration file location
- Default configuration file name: gw.properties
- Home directory resolution (used to locate gw.properties):
  1) -Dgw.home=<path>
  2) otherwise, the CLI project directory (-d/--project.dir) if provided
  3) otherwise, the current user directory
- Override config file path/name (resolved relative to gw.home):
  -Dgw.config=gw.properties
  -Dgw.config=conf/gw.properties


How to Run

Entry point
- org.machanism.machai.gw.processor.Ghostwriter

Jar invocation
java -jar gw.jar <scanDir> [options]

scanDir rules (from built-in help)
- <scanDir> may be:
  - a relative path with respect to the current project directory
  - an absolute path located within the root project directory
  - a glob: matcher (e.g., "glob:**/*.java")
  - a regex: matcher (e.g., "regex:^.*/[^/]+\\.java$")
- If no <scanDir> is provided, Ghostwriter uses gw.scanDir if configured; otherwise it scans the current user directory.

Configuration properties (from org.machanism.machai.gw.processor.Ghostwriter)

1) project.dir
- Description: Root directory for file processing.
- Default: from gw.properties (project.dir); otherwise the current user directory.
- Usage context: Can be overridden via -d/--project.dir.

2) gw.model
- Description: GenAI provider and model to use, in the form provider:model (e.g., OpenAI:gpt-5.1).
- Default: none (required to run).
- Usage context: from gw.properties; can be overridden via -m/--model.

3) instructions
- Description: Optional system instructions as plain text, URL, or file: reference.
- Default: unset.
- Usage context:
  - from gw.properties; can be overridden via -i/--instructions
  - if -i is used without a value, Ghostwriter reads from stdin

4) gw.excludes
- Description: Comma-separated list of directories to exclude from processing.
- Default: unset.
- Usage context: from gw.properties; can be overridden via -e/--excludes.

5) acts.location
- Description: Directory containing predefined act prompt files.
- Default: unset.
- Usage context: from gw.properties; can be overridden via -as/--acts.

6) gw.act
- Description: Default act prompt used by Act mode.
- Default: unset.
- Usage context:
  - from gw.properties
  - if -a/--act is provided with a value, it overrides the configured prompt
  - if -a is used without a value, Ghostwriter reads the act prompt from stdin

7) gw.threads
- Description: Degree of concurrency (worker thread count).
- Default: unset.
- Usage context: from gw.properties; can be overridden via -t/--threads.

8) gw.scanDir
- Description: Default scan directory/pattern when no <scanDir> argument is provided.
- Default: if unset and no scanDir args are provided, uses the current user directory absolute path.
- Usage context: from gw.properties.

9) gw.nonRecursive
- Description: Controls whether scanning should be non-recursive.
- Default: unset.
- Usage context: used by processor implementation(s).

10) inputs (Genai.LOG_INPUTS_PROP_NAME)
- Description: Enables request input logging to dedicated log files.
- Default: false.
- Usage context: from gw.properties; can also be enabled via -l/--logInputs.

11) gw.interactive
- Description: Toggles interactive behavior in the processor layer (if supported).
- Default: unset.
- Usage context: used by processor implementation(s).

Additional Java system properties
- gw.config
  - Description: Override the Ghostwriter configuration file path (resolved relative to gw.home).
  - Default: gw.properties

- gw.home
  - Description: Home directory used to locate gw.properties.
  - Default: projectDir if provided; otherwise current user directory.

How configuration values are interpreted

Instructions (-i/--instructions and the instructions property)
- Input is processed line-by-line:
  - blank lines preserved
  - lines starting with http:// or https:// are loaded from the specified URL
  - lines starting with file: are loaded from the specified file path
  - other lines are used as-is
- Multi-line stdin input ends when a line does not end with "\\" (backslash).

CLI options summary
- -h, --help: Show help and exit.
- -d, --project.dir <path>: Root directory for file processing.
- -t, --threads <n>: Degree of concurrency for processing.
- -m, --model <provider:model>: GenAI provider/model selection.
- -i, --instructions[=<text|url|file:...>]: System instructions (or multi-line stdin if no value).
- -e, --excludes <csv>: Comma-separated excludes.
- -l, --logInputs: Log LLM request inputs to dedicated log files.
- -as, --acts <dir>: Directory containing predefined act prompt files.
- -a, --act[=<prompt>]: Enable Act mode; if no value, read prompt from stdin.

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

- Provide system instructions via stdin:
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i

- Exclude build outputs and VCS metadata:
  java -jar gw.jar src -m OpenAI:gpt-5.1 -e ".git,target"

- Use a custom root directory:
  java -jar gw.jar "glob:**/*.md" -d . -m OpenAI:gpt-5.1

- Act mode with prompt from config (gw.act) via stdin:
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
  java -jar gw.jar src -m OpenAI:gpt-5.1 -e '.git,target'


Troubleshooting & Support

Common issues
- No GenAI provider/model configured
  - Set gw.model in gw.properties, or pass -m/--model.

- Authentication errors
  - Ensure provider credentials are configured for the selected provider.

- Scan path errors
  - If providing an absolute scan path, ensure it is inside the configured root project directory.
  - Prefer quoting glob:/regex: expressions.

Logs and debug
- Standard logs are written via SLF4J (logging backend depends on the runtime classpath).
- Provider usage is logged at the end of execution.
- If -l/--logInputs is enabled (or inputs=true), Ghostwriter writes dedicated log files containing provider request inputs.


Contact & Documentation

Project site
https://machai.machanism.org/ghostwriter/index.html

Guided File Processing
https://www.machanism.org/guided-file-processing/index.html

Source repository (Machai mono-repo)
https://github.com/machanism-org/machai

Maven Central
https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
