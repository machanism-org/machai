# Ghostwriter CLI

## Overview

Ghostwriter is the Machai Java command-line application for project-wide, GenAI-assisted file processing. It scans selected files, directories, glob patterns, or regular-expression paths and applies guidance to source code, tests, documentation, site content, configuration, diagrams, and other project artifacts. Guidance mode is the default; Act mode runs a selected reusable prompt against the project.

Typical uses include maintaining documentation, applying repeatable repository-wide transformations, processing selected file types in batch, and running controlled updates locally or in CI/CD. Review the working-tree diff before committing generated changes.

Supported provider forms depend on the GenAI client included in the delivery pack. The supplied configuration demonstrates:

- **CodeMie**, for example `CodeMie:gpt-5-2-2025-12-11`.
- **OpenAI-compatible services**, for example `OpenAI:gpt-5.1`; an OpenAI-compatible endpoint can be supplied with `OPENAI_BASE_URL`.

## Installation

### Prerequisites

- Java 8 or later. The Maven project sets `maven.compiler.release` to `8`.
- A reachable GenAI provider, an available model, valid credentials, and any provider-specific endpoint configuration.
- Read/write access to the project being processed.
- Either the Ghostwriter delivery pack (`gw.jar` and its runtime dependencies) or a source checkout with Maven and the required Machai dependencies.
- Version control is strongly recommended for reviewing and reverting generated edits.

### Use the delivery pack

1. Download the [Ghostwriter CLI pack](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download).
2. Extract it to a local directory.
3. Copy `gw.properties` from this directory as a starting configuration file. By default Ghostwriter loads `gw.properties`; use `-c` or `-Dgw.config` to select another file.
4. Set the provider/model and credentials. The supplied example uses CodeMie; uncomment the relevant CodeMie or OpenAI-compatible settings in `gw.properties`.
5. Verify the installation:

```text
java -jar gw.jar --help
```

The configuration file uses Java properties syntax. The default pack configuration contains `gw.model=CodeMie:gpt-5-2-2025-12-11` and commented examples for `GENAI_USERNAME`, `GENAI_PASSWORD`, `OPENAI_API_KEY`, and `OPENAI_BASE_URL`.

### Build from source

From the repository checkout:

```sh
mvn clean install
```

The `pack` profile assembles `target/gw.jar` and the delivery pack. It expects `MACHANISM_PACK_DIR` to be set:

```sh
MACHANISM_PACK_DIR=/path/to/pack mvn -Ppack clean install
```

On Windows:

```bat
set MACHANISM_PACK_DIR=C:\path\to\pack
mvn -Ppack clean install
```

## How to run

```text
java -jar gw.jar <path> [options]
```

`<path>` may be a relative file or directory, a path within the project directory, a glob such as `glob:**/*.java`, or a regex such as `regex:^.*/[^/]+\\.java$`. Multiple positional paths are accepted. If no path is supplied, Ghostwriter uses `gw.path`, then `.`. An absolute path must be inside the project directory.

### Command-line options

The options below are defined by `org.machanism.machai.gw.processor.Ghostwriter`.

| Option | Description | Default and usage |
|---|---|---|
| `-h`, `--help` | Print usage, options, and examples, then exit. | Off. |
| `-d`, `--projectDir <path>` | Set the project root used to resolve and scan paths. | `project.dir`; otherwise the current user directory. CLI takes precedence. |
| `-c`, `--config <file>` | Select the Java properties configuration file. Relative paths are resolved against the initially resolved project directory. | `gw.properties`, unless `-c` or system property `gw.config` is supplied. |
| `-t`, `--threads <n>` | Set the number of concurrent processing threads. | `gw.threads`; otherwise the processor default. Must be an integer accepted by the processor. |
| `-m`, `--model <provider:model>` | Select the GenAI provider and model, such as `CodeMie:gpt-5-2-2025-12-11` or `OpenAI:gpt-5.1`. | `gw.model`; otherwise unset/provider behavior. |
| `-i`, `--instructions [text]` | Set system instructions. If the option has no value, read the instructions from standard input. | `gw.instructions`; otherwise unset. CLI takes precedence. |
| `-e`, `--excludes <csv>` | Set comma-separated path, file, or directory exclusions. | `gw.excludes`; otherwise no configured exclusions. CLI takes precedence. |
| `-as`, `--acts <path>` | Set the location of predefined Act definitions. | `gw.acts`; otherwise the Act processor default. |
| `-a`, `--act [name or prompt]` | Enable Act mode and select an Act or prompt. With no value, read it from standard input. | Guidance mode unless present; `gw.act` supplies a configured Act value. |

### Configuration properties

The properties below are the names used by `GWConstants`. Explicit command-line values override properties-file values. Properties that the `Ghostwriter` entry point does not currently read are identified as such.

| Property | Description | Default / context |
|---|---|---|
| `project.dir` | Base project directory. | Current user directory when absent; overridden by `-d`. |
| `gw.config` | Java system property naming the configuration file. | `gw.properties` when absent; overridden by `-c`. |
| `gw.model` | GenAI provider/model identifier. | Unset unless configured; overridden by `-m`. |
| `gw.instructions` | Default system instructions. | Unset unless configured; overridden by `-i`. |
| `gw.excludes` | Comma-separated exclusions. | Unset unless configured; overridden by `-e`. |
| `gw.acts` | Local or remote Act-definition location. | Processor default unless configured; overridden by `-as`. |
| `gw.act` | Default Act name or prompt. | Used only in Act mode; overridden by `-a`. |
| `gw.threads` | Concurrent processing thread count. | Processor default unless configured; overridden by `-t`. |
| `gw.path` | Default file, directory, glob, or regex scan target. | `.` when absent; positional paths take precedence. |
| `gw.nonRecursive` | Declared recursive-traversal setting. | Not read by this CLI entry point. |
| `gw.interactive` | Declared interactive-mode setting. | Not read by this CLI entry point; `--instructions` and `--act` independently support stdin prompts. |

Example `gw.properties`:

```properties
project.dir=.
gw.model=CodeMie:gpt-5-2-2025-12-11
gw.threads=4
gw.excludes=.git,target,node_modules
gw.path=src
gw.instructions=Keep headings consistent and preserve public links.
```

### System properties and provider environment variables

Set Java system properties before `-jar`:

```sh
java -Dgw.config=production.properties -jar gw.jar src
```

The pack’s example configuration documents these provider credential/environment names; their interpretation is performed by the installed GenAI client:

- CodeMie: `GENAI_USERNAME` and `GENAI_PASSWORD`.
- OpenAI-compatible services: `OPENAI_API_KEY` and, for a non-default endpoint, `OPENAI_BASE_URL`.

For example, on Unix:

```sh
export GENAI_USERNAME=my-user
export GENAI_PASSWORD=my-password
java -jar gw.jar . -m CodeMie:gpt-5-2-2025-12-11
```

On Windows:

```bat
set GENAI_USERNAME=my-user
set GENAI_PASSWORD=my-password
java -jar gw.jar . -m CodeMie:gpt-5-2-2025-12-11
```

### Unix examples

```sh
# Guidance mode: root, model, instructions, exclusions, and concurrency
java -jar gw.jar "glob:**/*.md" -d /work/my-project \
  -m OpenAI:gpt-5.1 -t 4 -e ".git,target" \
  -i "Keep headings consistent and preserve public links."

# Read instructions interactively
java -jar gw.jar src --instructions

# Run an Act from a local directory
java -jar gw.jar . --acts ./acts --act "Summarize the repository"
```

### Windows examples

```bat
rem Guidance mode with root, model, instructions, exclusions, and concurrency
java -jar gw.jar "glob:**/*.md" -d C:\work\my-project -m OpenAI:gpt-5.1 -t 4 -e ".git,target" -i "Keep headings consistent."

rem Read instructions interactively
java -jar gw.jar src --instructions

rem Run an Act from a local directory
java -jar gw.jar . --acts .\acts --act "Summarize the repository"
```

A trailing backslash in interactive input continues the value on the next line. Run `java -jar gw.jar --help` for the built-in path syntax and examples.

## Troubleshooting and support

- **Authentication or provider failures:** check the provider/model spelling, credentials, endpoint, network access, account permissions, and provider quota. Keep secrets out of `gw.properties` committed to source control.
- **Missing files or unexpected scans:** set `-d` explicitly, verify the positional path or `gw.path`, check glob/regex syntax, and inspect `gw.excludes`. Absolute paths must remain under the project directory.
- **Configuration not loaded:** check the selected `-c` path or `-Dgw.config` value and remember that relative configuration paths are resolved against the project directory used during startup.
- **Act not found:** verify `--acts`/`gw.acts` points to the Act definitions and that the requested `--act`/`gw.act` value is valid.
- **Thread errors:** ensure `--threads`/`gw.threads` is a valid integer; the processor rejects invalid or non-positive values.
- **Logs and debug output:** startup, configuration, scan progress, failures, and usage statistics are emitted through SLF4J and the logging backend packaged with the application. Configure that backend to enable DEBUG or TRACE for `org.machanism.machai.gw`; also enable provider-client request logging only when safe. The CLI does not define a fixed log-file location.
- **Review changes:** inspect `git diff` or equivalent after processing and before publishing results.

## Documentation and contact

- Ghostwriter documentation: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- Source repository and issue/support entry point: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Bindex Core: https://machai.machanism.org/bindex-core/index.html
- CLI download: https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download

Ghostwriter is released under the Apache License 2.0. For provider-specific authentication, consult the documentation for the selected GenAI client/provider.
