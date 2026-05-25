Ghostwriter CLI

Application Overview

Ghostwriter is a repository-wide AI automation and documentation CLI in the Machai ecosystem. It scans project content, detects embedded "@guidance" directives, and applies GenAI-assisted processing to source code, documentation, project site content, configuration, diagrams, and other repository artifacts.

Its main purpose is to keep maintenance intent inside the repository, close to the files it governs, so updates become repeatable, reviewable, and suitable for both local execution and CI/CD workflows. In addition to guidance-driven processing, Ghostwriter also supports Act mode for reusable prompt workflows.

Key features
- Scans directories, raw path targets, glob patterns, and regex-based targets.
- Detects embedded "@guidance" directives in repository files.
- Processes source code, documentation, project site pages, configuration, diagrams, and related artifacts.
- Supports additional system instructions from plain text, URLs, files, or standard input.
- Applies exclusion rules for selective processing.
- Supports reusable Act mode workflows.
- Supports configurable concurrency.
- Can log LLM request inputs for diagnostics and auditing.
- Fits both local development and CI/CD automation scenarios.

Typical use cases
- Generate or refresh repository documentation.
- Apply guidance-driven maintenance across many files.
- Run repeatable AI-assisted repository updates in local scripts or CI/CD pipelines.
- Execute reusable act-driven prompts against a project.

Supported GenAI providers
- CodeMie
- OpenAI-compatible services

Installation Instructions

Prerequisites
- Java 8 or later.
- Access to a supported GenAI provider and any required credentials.
- Network connectivity to the selected provider when applicable.
- A project or working directory containing files to scan and update.
- Optional gw.properties configuration in the Ghostwriter home directory, or a custom configuration path supplied with -Dgw.config=....
- Optional acts directory when using Act mode.

Provider configuration examples
- CodeMie
  - gw.model=CodeMie:gpt-5-2-2025-12-11
  - GENAI_USERNAME=your_codemie_username
  - GENAI_PASSWORD=your_codemie_password
- OpenAI-compatible services
  - gw.model=OpenAI:gpt-5.1
  - OPENAI_API_KEY=your_openai_api_key
  - OPENAI_BASE_URL=https://your-openai-compatible-endpoint

Build or download
- Build from source with Maven from the repository root.
- Or download the packaged CLI distribution:
  https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download

Typical Maven build commands
- Windows
  mvn clean package
- Unix
  mvn clean package

Configuration file location
- Default configuration file name: gw.properties
- Ghostwriter resolves the home directory from gw.home when defined.
- If gw.home is not defined, it uses the current user directory.
- Ghostwriter loads gw.properties from the resolved home directory unless overridden with -Dgw.config=....

How to Run

Entry point
- org.machanism.machai.gw.processor.Ghostwriter

Basic invocation
- java -jar gw.jar <scanDir> [options]

Scan target rules
- <scanDir> may be:
  - a relative path with respect to the current project directory
  - an absolute path located within the root project directory
  - a raw directory name
  - a glob pattern such as "glob:**/*.java"
  - a regex pattern such as "regex:^.*/[^/]+\\.java$"
- If no scan target is supplied, Ghostwriter falls back to gw.scanDir from configuration and then to .

Configuration properties and options

1) project.dir
- Description: Root directory for file processing.
- Default value: project.dir from configuration, otherwise the current user directory.
- Usage context: Set with -d or --project.dir to control the root directory used for scanning and processing.

2) gw.model
- Description: GenAI provider and model identifier.
- Default value: gw.model from configuration.
- Usage context: Set with -m or --model. Example: OpenAI:gpt-5.1 or CodeMie:gpt-5-2-2025-12-11.

3) instructions
- Description: Additional system instructions supplied as plain text, URL content, file content, or stdin input.
- Default value: instructions from configuration.
- Usage context: Set with -i or --instructions. If the option is used without a value, Ghostwriter prompts for input from standard input.

4) gw.excludes
- Description: Comma-separated list of directories to exclude from processing.
- Default value: gw.excludes from configuration.
- Usage context: Set with -e or --excludes to skip selected directories or files.

5) gw.threads
- Description: Degree of concurrency for processing.
- Default value: gw.threads from configuration.
- Usage context: Set with -t or --threads to improve processing performance.

6) inputs
- Description: Enables logging of LLM request inputs to dedicated log files.
- Default value: false unless enabled in configuration.
- Usage context: Enable with -l or --logInputs, or through the corresponding configuration property used by the runtime.

7) gw.acts
- Description: Path to the directory containing predefined act prompt files.
- Default value: gw.acts from configuration.
- Usage context: Set with -as or --acts when using Act mode and custom act definitions.

8) gw.act
- Description: Default act prompt used by Act mode.
- Default value: gw.act from configuration when applicable.
- Usage context: Set with -a or --act. If -a is used without a value, Ghostwriter prompts for act text interactively.

9) gw.scanDir
- Description: Default scan directory or scan pattern used when no positional scan target is supplied.
- Default value: .
- Usage context: Configure a default processing target in gw.properties.

Additional Java system properties
- gw.home
  - Description: Home directory used to resolve gw.properties.
  - Default value: current user directory.
  - Usage context: Set with -Dgw.home=... when you want a custom Ghostwriter home location.
- gw.config
  - Description: Configuration file name or path resolved relative to gw.home.
  - Default value: gw.properties
  - Usage context: Set with -Dgw.config=... to use a custom configuration file.

How instructions are resolved
- Blank lines are preserved.
- Lines starting with http:// or https:// are loaded from the specified URL.
- Lines starting with file: are loaded from the specified file path.
- Other lines are used as-is.
- If -i is used without a value, Ghostwriter prompts for input from stdin.

CLI options summary
- -h, --help
  - Description: Show the help message and exit.
  - Default value: none.
- -d, --project.dir <path>
  - Description: Specify the path to the root directory for file processing.
  - Default value: project.dir from configuration, otherwise the current user directory.
- -t, --threads <n>
  - Description: The degree of concurrency for processing to improve performance.
  - Default value: gw.threads from configuration.
- -m, --model <provider:model>
  - Description: Set the GenAI provider and model.
  - Default value: gw.model from configuration.
- -i, --instructions [value]
  - Description: Specify system instructions as plain text, URL, file path, or stdin.
  - Default value: instructions from configuration.
- -e, --excludes <csv>
  - Description: Specify a comma-separated list of directories to exclude from processing.
  - Default value: gw.excludes from configuration.
- -l, --logInputs
  - Description: Log LLM request inputs to dedicated log files.
  - Default value: false unless enabled in configuration.
- -as, --acts <path>
  - Description: Specify the path to the directory containing predefined act prompt files.
  - Default value: gw.acts from configuration.
- -a, --act [value]
  - Description: Run Ghostwriter in Act mode.
  - Default value: gw.act from configuration when applicable.

Usage examples

Windows (.bat or cmd)
- Basic directory scan
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Use a custom root directory
  java -jar gw.jar "glob:**/*.md" -d . -m OpenAI:gpt-5.1

- Add instructions from a file
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i "file:./instructions.txt"

- Add inline instructions
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i "Update documentation and keep headings consistent."

- Exclude directories
  java -jar gw.jar src -m OpenAI:gpt-5.1 -e ".git,target,build,node_modules"

- Enable request input logging
  java -jar gw.jar src -m OpenAI:gpt-5.1 -l

- Use custom configuration and home directory
  java -Dgw.home=. -Dgw.config=src/pack/files/gw.properties -jar gw.jar src -m CodeMie:gpt-5-2-2025-12-11

- Act mode with inline prompt
  java -jar gw.jar src -m OpenAI:gpt-5.1 -a "Summarize the repository"

- Act mode with custom acts directory
  java -jar gw.jar src -m OpenAI:gpt-5.1 -as ./acts -a "Update README files"

Unix (.sh)
- Basic directory scan
  java -jar gw.jar src -m OpenAI:gpt-5.1

- Use a custom root directory
  java -jar gw.jar 'glob:**/*.md' -d . -m OpenAI:gpt-5.1

- Add instructions from a file
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i 'file:./instructions.txt'

- Add inline instructions
  java -jar gw.jar src -m OpenAI:gpt-5.1 -i 'Update documentation and keep headings consistent.'

- Exclude directories
  java -jar gw.jar src -m OpenAI:gpt-5.1 -e '.git,target,build,node_modules'

- Enable request input logging
  java -jar gw.jar src -m OpenAI:gpt-5.1 -l

- Use custom configuration and home directory
  java -Dgw.home=. -Dgw.config=src/pack/files/gw.properties -jar gw.jar src -m CodeMie:gpt-5-2-2025-12-11

- Act mode with inline prompt
  java -jar gw.jar src -m OpenAI:gpt-5.1 -a 'Summarize the repository'

Troubleshooting & Support

Common issues
- Authentication errors
  - Verify that provider credentials are configured correctly for the selected provider.
  - For CodeMie, check GENAI_USERNAME and GENAI_PASSWORD.
  - For OpenAI-compatible services, check OPENAI_API_KEY and OPENAI_BASE_URL when required.
- Missing or incorrect model configuration
  - Ensure gw.model is set in gw.properties or passed with -m.
- Missing files or scan path failures
  - Confirm that the scan target exists.
  - If an absolute scan path is used, ensure it is located within the configured root project directory.
  - Quote glob and regex patterns to prevent shell expansion issues.
- Configuration not being loaded
  - Verify gw.home and gw.config values.
  - Ensure gw.properties is placed in the resolved Ghostwriter home directory.

Logs and diagnostics
- Standard runtime logging is emitted through the configured SLF4J backend.
- Startup logs include the resolved home directory and root directory.
- Usage statistics are logged when processing completes.
- Enable request-input logging with -l or --logInputs for additional diagnostics.

Contact & Documentation
- Official platform
  https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing
  https://www.machanism.org/guided-file-processing/index.html
- GitHub repository
  https://github.com/machanism-org/machai
- Maven Central
  https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
