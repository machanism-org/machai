# Ghostwriter CLI

## Overview

Ghostwriter is a Java command-line application in the Machai ecosystem for repository-wide, GenAI-assisted file processing. It scans a project, discovers embedded `@guidance` directives, and applies guided updates to source code, documentation, project-site content, configuration, diagrams, and other project artifacts. It can also execute reusable or ad-hoc Act prompts.

Typical uses include keeping generated documentation synchronized with code, applying repeatable repository maintenance rules, processing selected files in batch, and running guided updates locally or in CI/CD. Review generated changes in version control before committing them.

The model is selected as a `provider:model` identifier. The CLI source demonstrates `OpenAI:gpt-5.1`; therefore OpenAI-compatible services are supported when configured by the installed GenAI client. CodeMie may be used when it is available as a provider in that client/runtime. Provider availability and credential requirements depend on the GenAI client and delivery-pack version.

## Installation

### Prerequisites

- Java 8 or later. The project is compiled with `maven.compiler.release` set to `8`.
- Access to a configured GenAI provider, model, credentials, and network endpoint as required by that provider.
- A project directory containing the files to scan.
- A delivery pack containing `gw.jar` and its runtime dependencies, or a Maven checkout with the required Machai dependencies available.
- Version control is recommended so generated edits can be reviewed and reverted.

### Use the delivery pack

1. Download the [Ghostwriter CLI pack](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download).
2. Extract the archive into a directory on the machine running Ghostwriter.
3. Put `gw.properties` in the Ghostwriter home directory, or prepare another properties file and select it with `-Dgw.config`.
4. Configure the provider/model and credentials according to the selected GenAI client.
5. Run `java -jar gw.jar --help` to verify the installation.

### Build from source

From the repository checkout, build the project with Maven:

```sh
mvn clean install
```

The `pack` profile builds the assembled `gw.jar` delivery artifact and requires the project’s parent/dependency artifacts. A delivery-pack build also expects the `MACHANISM_PACK_DIR` environment variable used by the Maven packaging configuration:

```sh
MACHANISM_PACK_DIR=/path/to/pack mvn -Ppack clean install
```

On Windows, set the variable before building:

```bat
set MACHANISM_PACK_DIR=C:\path\to\pack
mvn -Ppack clean install
```

## How to run

The general form is:

```text
java -jar gw.jar <path> [options]
```

`<path>` is optional. It can be a relative path, a path within the project directory, a directory, a file, a glob such as `glob:**/*.java`, or a regex such as `regex:^.*/[^/]+\.java$`. If omitted, Ghostwriter uses `gw.path` and then `.`.

### Command-line options

These options are defined by `org.machanism.machai.gw.processor.Ghostwriter`:

| Option | Description | Default / precedence |
|---|---|---|
| `-h`, `--help` | Prints built-in usage, options, and examples, then exits. | None |
| `-d`, `--projectDir <path>` | Sets the project root used to resolve and scan paths. | `project.dir`; otherwise the current user directory. CLI wins over the properties file. |
| `-t`, `--threads <n>` | Sets the number of concurrent processing threads. The value must be a positive integer accepted by the processor. | `gw.threads`; if absent, the processor’s default applies (normally single-threaded). |
| `-m`, `--model <provider:model>` | Selects the GenAI provider and model, for example `OpenAI:gpt-5.1`. | `gw.model`; otherwise the provider/client default or unset model behavior applies. |
| `-i`, `--instructions [text]` | Sets system instructions. A value beginning with `http://` or `https://` is loaded as a URL; `file:` loads a file; other text is used directly. With no value, input is read from stdin. | `gw.instructions`; otherwise unset. CLI wins. |
| `-e`, `--excludes <csv>` | Sets comma-separated directory/path patterns to exclude. | `gw.excludes`; otherwise no configured excludes. CLI wins. |
| `-as`, `--acts <path>` | Sets the directory, path, or URL containing predefined Act definitions. | `gw.acts`; otherwise the Act processor default applies. |
| `-a`, `--act [text]` | Enables Act mode. The supplied value selects the Act or prompt; with no value, Ghostwriter reads it from stdin. | No Act mode unless this option is present. `gw.act` can supply the configured Act value. |

The CLI also reads these configuration properties from `GWConstants`:

| Property | Description | Default / usage |
|---|---|---|
| `project.dir` | Base project directory used for layout and path resolution. | Current user directory when unset; overridden by `-d`. |
| `gw.config` | Java system property naming the configuration file. | `gw.properties` in the project directory. |
| `gw.model` | Provider/model identifier. | Unset unless configured; overridden by `-m`. |
| `gw.instructions` | Default system instructions. | Unset unless configured; overridden by `-i`. |
| `gw.excludes` | Comma-separated exclusions. | Unset unless configured; overridden by `-e`. |
| `gw.acts` | Predefined Act definition location; may be a path or HTTP(S) URL. | Act processor default unless configured; overridden by `-as`. |
| `gw.act` | Default Act name or prompt. | Unset unless configured; used in Act mode. |
| `gw.threads` | Thread count for concurrent module processing. | Unset unless configured; overridden by `-t`. |
| `gw.path` | Default file, directory, glob, or regex scan target. | `.` when unset; positional paths take precedence. |
| `gw.nonRecursive` | Configuration key for recursive module traversal. | Declared by the shared constants, but not read by the `Ghostwriter` entry point shown here. |
| `gw.interactive` | Configuration key for interactive mode. | Declared by the shared constants, but not read by the `Ghostwriter` entry point shown here; `--instructions` and `--act` provide their own optional stdin prompts. |

A configuration file is Java properties syntax, for example:

```properties
project.dir=.
gw.model=OpenAI:gpt-5.1
gw.threads=4
gw.excludes=.git,target,node_modules
gw.path=src
gw.instructions=file:./instructions.txt
```

### Configuration precedence and system properties

For runtime settings, an explicit CLI option takes precedence over the corresponding properties-file value. The configuration file is resolved as follows:

- `gw.config` is a Java system property naming the file; if absent, `gw.properties` is used.

Set Java system properties before `-jar`:

```sh
java -Dgw.config=production.properties -jar gw.jar src
```

### Unix examples

```sh
# Guidance mode with a project root, model, exclusions, and four workers
java -jar gw.jar src -d . -m OpenAI:gpt-5.1 -t 4 -e ".git,target" \
  -i "file:./instructions.txt"

# Scan Java files selected by a glob
java -jar gw.jar "glob:**/*.java" -d /work/my-project

# Read instructions interactively
java -jar gw.jar src --instructions

# Run an Act from a local Act directory
java -jar gw.jar . --acts ./acts --act "Summarize the repository"
```

### Windows examples

```bat
rem Guidance mode with a project root, model, exclusions, and four workers
java -jar gw.jar src -d . -m OpenAI:gpt-5.1 -t 4 -e ".git,target" -i "file:instructions.txt"

rem Scan Java files selected by a glob
java -jar gw.jar "glob:**/*.java" -d C:\work\my-project

rem Read instructions interactively
java -jar gw.jar src --instructions

rem Run an Act from a local Act directory
java -jar gw.jar . --acts .\acts --act "Summarize the repository"
```

Use a trailing backslash in interactive input to continue a prompt onto the next line. The CLI prints startup information for the home and project directories and logs the selected settings at INFO level.

## Troubleshooting and support

- **Authentication or provider errors:** verify the provider name/model syntax, credentials, endpoint configuration, network access, and any provider-specific environment variables. Try the provider’s documented model identifier, such as `OpenAI:gpt-5.1`, only when that model is available to your account.
- **Missing files or unexpected scan results:** run from the intended project directory, set `-d` explicitly, verify the positional path/glob/regex, and inspect `gw.excludes`. Absolute scan paths must be within the project directory according to the built-in help.
- **Act not found:** check that `--acts` points to the directory or URL containing the Act definitions and that the requested `--act` name/prompt is valid.
- **Invalid thread count:** `gw.threads` and `--threads` must be parseable as a positive integer.
- **Instructions not loaded:** use `file:path` for a file, an `http://` or `https://` URL for remote text, or plain text for inline instructions. With `--instructions` and no argument, provide text on stdin.
- **Logs and debug output:** Ghostwriter uses SLF4J. Startup, scan progress, configuration summaries, failures, and usage statistics are logged through the configured logging backend. To obtain more detail, configure that backend’s logger for `org.machanism.machai.gw` at DEBUG/TRACE and enable any provider/client request logging supported by the installed pack. Avoid exposing credentials or sensitive prompts in logs.
- **Review generated changes:** processing updates the working tree; use `git diff` or another version-control review before accepting the result.

## Documentation and contact

- Official Ghostwriter documentation: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- Source repository and issue/support entry point: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Bindex Core documentation: https://machai.machanism.org/bindex-core/index.html

Ghostwriter is released under the Apache License 2.0. For provider authentication, consult the documentation for the selected GenAI provider/client.
