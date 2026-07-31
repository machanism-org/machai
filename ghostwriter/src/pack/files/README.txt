Ghostwriter CLI Pack README
===========================

Application Overview
--------------------

Ghostwriter is a Java command-line application in the Machai ecosystem for repository-wide AI-assisted file processing. It scans project files, detects embedded @guidance instructions, prepares project-aware prompts, invokes a configured GenAI model, and writes approved generated updates back to the working tree.

Ghostwriter is intended for repeatable automation across source code, documentation, project site content, configuration files, diagrams, scripts, and other project artifacts. Typical use cases include:

- Keeping documentation aligned with code and configuration.
- Applying embedded file-specific maintenance instructions.
- Running project-wide or pattern-based AI updates locally or in CI/CD.
- Executing reusable Act workflows for controlled GenAI-assisted tasks.
- Generating or refreshing documentation, examples, and project metadata.

Supported GenAI providers include:

- CodeMie, as shown by the default pack configuration.
- OpenAI and OpenAI-compatible services.
- Other providers supported by the Machai AI provider layer when configured through the provider:model syntax.

Installation Instructions
-------------------------

Prerequisites:

- Java 8 or later.
- Network access to the selected GenAI provider.
- Provider credentials, depending on the selected provider.
- A project directory containing files to process.
- Recommended: version control, so generated changes can be reviewed before committing.

Files included in this pack directory:

- gw.properties - sample/default Ghostwriter configuration.
- README.txt - this usage and configuration guide.

To install from the CLI delivery pack:

1. Download the Ghostwriter CLI pack:
   https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download
2. Extract the archive to a local directory.
3. Ensure Java is available on PATH:

   java -version

4. Edit gw.properties or provide equivalent Java system properties/environment variables for your provider.
5. Run Ghostwriter with gw.jar from the extracted pack.

If building from source is applicable in your environment, use the project Maven build and then run the generated Ghostwriter artifact with its runtime dependencies.

How to Run
----------

Basic syntax:

   java -jar gw.jar <path> [options]

The <path> argument is the scan target. It may be:

- A relative path under the current project directory, such as src.
- An absolute path, if it is located within the configured project root.
- A raw directory name.
- A glob pattern, such as glob:**/*.java.
- A regex pattern, such as regex:^.*/[^/]+\.java$.

If <path> is omitted, Ghostwriter uses gw.path from configuration. If gw.path is also absent, it scans . by default.

Command-line examples:

   java -jar gw.jar src
   java -jar gw.jar "glob:**/*.java"
   java -jar gw.jar "regex:^.*/[^/]+\.java$"

Windows example:

   gw.bat src -d . -m CodeMie:gpt-5-2-2025-12-11 -e ".git,target" -i "file:instructions.txt" -l

Equivalent direct Windows command:

   java -Dgw.config=gw.properties -jar gw.jar src -d . -m CodeMie:gpt-5-2-2025-12-11 -e ".git,target" -i "file:instructions.txt" -l

Unix example:

   ./gw.sh src -d . -m CodeMie:gpt-5-2-2025-12-11 -e ".git,target" -i "file:instructions.txt" -l

Equivalent direct Unix command:

   java -Dgw.config=gw.properties -jar gw.jar src \
     -d . \
     -m CodeMie:gpt-5-2-2025-12-11 \
     -e ".git,target" \
     -i "file:instructions.txt" \
     -l

Act mode example:

   java -jar gw.jar src -a "Summarize the repository" -as ./acts

Configuration Properties and Options
------------------------------------

Ghostwriter reads configuration from gw.properties in the Ghostwriter home directory. The home directory is resolved from the gw.home Java system property when set; otherwise it defaults to the current user directory. The configuration file name is resolved from the gw.config Java system property and defaults to gw.properties.

You can configure Ghostwriter by using:

- Command-line options.
- Java system properties, for example -Dgw.config=gw.properties or -Dgw.home=.
- Properties in gw.properties.
- Environment variables required by the selected provider, such as GENAI_USERNAME, GENAI_PASSWORD, OPENAI_API_KEY, or OPENAI_BASE_URL.

Available CLI options and related properties:

- -h, --help
  Description: Show the built-in help message and exit.
  Default: Not enabled.
  Usage context: Use when you need the current syntax and examples.

- -d, --project.dir <path>
  Description: Specify the root directory for file processing.
  Default: project.dir from configuration, otherwise the current user directory.
  Usage context: Defines the root boundary for relative paths and valid absolute scan paths.

- -t, --threads <n>
  Description: Set the degree of concurrency for processing.
  Default: gw.threads from configuration, otherwise processor default.
  Usage context: Increase for faster batch processing when provider limits and machine resources allow it.

- -m, --model <provider:model>
  Description: Set the GenAI provider and model, for example CodeMie:gpt-5-2-2025-12-11 or OpenAI:gpt-5.1.
  Default: gw.model from configuration.
  Usage context: Selects the AI backend used for guidance or Act processing.

- -i, --instructions [value]
  Description: Provide additional system instructions as plain text, URL, or file reference. Lines beginning with http:// or https:// are loaded from the URL. Lines beginning with file: are loaded from the referenced file. Blank lines are preserved. Other lines are used as-is. If the option is supplied without a value, Ghostwriter prompts on standard input.
  Default: instructions from configuration.
  Usage context: Add run-specific rules without editing every @guidance block.

- -e, --excludes <csv>
  Description: Specify a comma-separated list of directories, files, or patterns to exclude from processing.
  Default: gw.excludes from configuration.
  Usage context: Skip build output, VCS metadata, generated files, secrets, or other content that should not be scanned.

- -as, --acts <path>
  Description: Specify the directory containing predefined Act prompt files.
  Default: gw.acts from configuration.
  Usage context: Used with Act mode for reusable workflows.

- -a, --act [value]
  Description: Run Ghostwriter in Act mode to execute a predefined or ad-hoc prompt. If supplied without a value, Ghostwriter prompts for act text.
  Default: No Act mode unless supplied. When Act mode is active, gw.act may provide the default act text/name.
  Usage context: Use for controlled prompt workflows rather than normal @guidance scanning.

Additional configuration names used by Ghostwriter:

- gw.home
  Description: Ghostwriter home directory used to locate the configuration file.
  Default: current user directory.
  Example: java -Dgw.home=. -jar gw.jar src

- gw.config
  Description: Configuration file name under gw.home.
  Default: gw.properties.
  Example: java -Dgw.config=custom-gw.properties -jar gw.jar src

- gw.path
  Description: Default scan path or pattern when no positional <path> is provided.
  Default: .

- gw.model
  Description: Default provider:model selection.
  Default in this pack: CodeMie:gpt-5-2-2025-12-11.

- gw.excludes
  Description: Default comma-separated exclude list.
  Default: not set in this pack.

- gw.threads
  Description: Default concurrency value.
  Default: not set in this pack.
  Note: The sample gw.properties comments show gw.threads=true, but the CLI expects a numeric thread count.

- gw.acts
  Description: Default acts directory.
  Default: not set in this pack.

- gw.act
  Description: Default Act mode prompt or act name when Act mode is enabled.
  Default: not set in this pack.

Provider Credentials
--------------------

For CodeMie, configure credentials as environment variables or as supported by your runtime configuration:

   GENAI_USERNAME=your_codemie_username
   GENAI_PASSWORD=your_codemie_password

For OpenAI or OpenAI-compatible services:

   OPENAI_API_KEY=your_openai_api_key
   OPENAI_BASE_URL=https://your-openai-compatible-endpoint

For original OpenAI, OPENAI_BASE_URL is typically not required.

Troubleshooting and Support
---------------------------

Authentication errors:

- Verify that the selected provider in gw.model matches your configured credentials.
- Check GENAI_USERNAME and GENAI_PASSWORD for CodeMie.
- Check OPENAI_API_KEY and, for compatible providers, OPENAI_BASE_URL.
- Confirm network access to the provider endpoint.

Missing files or no files processed:

- Confirm the scan path is relative to the configured project root.
- Use -d or --project.dir to set the root explicitly.
- Check gw.path if no positional path is supplied.
- Review gw.excludes and -e values to ensure the target files are not excluded.
- For absolute scan paths, ensure the path is inside the configured project root.

Configuration not loaded:

- Confirm gw.properties is in the Ghostwriter home directory.
- Use -Dgw.home=. to make the current directory the Ghostwriter home.
- Use -Dgw.config=gw.properties or another file name when using a custom configuration file.

Instruction problems:

- For file-based instructions, use file: followed by the path, for example file:instructions.txt.
- Ensure referenced instruction files exist and are readable.
- Ensure URL-based instructions are reachable from the machine running Ghostwriter.

Debugging and logs:

- Ghostwriter logs startup information including the home directory, root directory, scan start, scan finish, usage statistics, and processing errors.
- If using a logging configuration, enable DEBUG for org.machanism.machai or org.machanism.machai.gw to increase verbosity.
- Protect logs because they may contain prompt context, project content, or credentials if the runtime environment is misconfigured.

Contact and Documentation
-------------------------

- Ghostwriter documentation: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- CLI pack download: https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download
