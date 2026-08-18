# Ghostwriter CLI

## Application overview

Ghostwriter is the Machai Java command-line application for project-wide, GenAI-assisted file processing. It scans selected files, directories, glob patterns, or regular-expression paths and applies guidance to source code, tests, documentation, site content, configuration, diagrams, and other project artifacts. Guidance mode is the default; Act mode runs a selected reusable prompt against the project.

Typical uses include maintaining documentation, applying repeatable repository-wide transformations, processing selected file types in batch, and running controlled updates locally or in CI/CD. Review the working-tree diff before committing generated changes.

The delivery-pack configuration demonstrates these provider/model forms:

- **CodeMie**, for example `CodeMie:gpt-5-2-2025-12-11`.
- **OpenAI-compatible services**, for example `OpenAI:gpt-5.1`. OpenAI-compatible endpoints can be selected with `OPENAI_BASE_URL` as supported by the bundled GenAI client.

The exact provider/model availability, authentication behavior, and endpoint settings are supplied by the GenAI client packaged with Ghostwriter.

## Installation

### Prerequisites

- Java 8 or later. The Maven project targets Java release 8 (`maven.compiler.release=8`).
- A reachable GenAI provider, an available model, valid credentials, and any provider-specific endpoint configuration.
- Read/write access to the project being processed.
- Either the Ghostwriter delivery pack (`gw.jar` and its runtime dependencies) or a source checkout with Maven and access to the required dependency repositories.
- Version control is strongly recommended so generated edits can be reviewed and reverted.

### Use the delivery pack

1. Download the [Ghostwriter CLI pack](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download).
2. Extract it to a local directory.
3. Use the included `gw.properties` as a starting configuration file. By default Ghostwriter loads `gw.properties`; use `-c` or the `gw.config` Java system property to select another file.
4. Set the provider/model and credentials. The supplied example selects `CodeMie:gpt-5-2-2025-12-11` and includes commented examples for CodeMie and OpenAI-compatible credentials.
5. Verify the installation:

```text
java -jar gw.jar --help
```

The configuration file uses Java properties syntax. Do not commit provider secrets to a project repository.

### Build from source

From the repository checkout, build the project with:

```sh
mvn clean install
```

The `pack` profile creates `target/gw.jar` and the delivery-pack output. It expects `MACHANISM_PACK_DIR` to be set:

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

`<path>` may be a relative file or directory, a path within the project root, a glob such as `glob:**/*.java`, or a regex such as `regex:^.*/[^/]+\\.java$`. Multiple positional paths are accepted. If no path is supplied, Ghostwriter uses `gw.path`, then `.`. An absolute path must be inside the project directory.

There is no separate `--root` option: use `-d`/`--projectDir` (or `project.dir`) to set the project root. Relative scan paths are interpreted in that root.

### Command-line options

The options below are defined by `org.machanism.machai.gw.processor.Ghostwriter`; command-line values take precedence over configuration values.

| Option | Description | Default and usage |
|---|---|---|
| `-h`, `--help` | Print usage, options, and examples, then exit without processing. | Off. |
| `-d`, `--projectDir <path>` | Set the project root used to resolve and scan paths. | `project.dir`; otherwise the current user directory. |
| `-c`, `--config <file>` | Select the Java properties configuration file. | `gw.properties`; `gw.config` can override this default, and `-c` takes precedence. Relative paths are resolved from the initial project directory. |
| `-t`, `--threads <n>` | Set the number of concurrent processing threads. | `gw.threads`; otherwise the processor default. The value must be an integer accepted by the processor. |
| `-m`, `--model <provider:model>` | Select the GenAI provider and model, such as `CodeMie:gpt-5-2-2025-12-11` or `OpenAI:gpt-5.1`. | `gw.model`; otherwise unset/provider behavior. |
| `-i`, `--instructions [text]` | Set system instructions. If the option has no value, read the instructions from standard input. | `gw.instructions`; otherwise unset. |
| `-e`, `--excludes <csv>` | Set comma-separated paths, files, or directories to exclude. | `gw.excludes`; otherwise no configured exclusions. |
| `-as`, `--acts <path>` | Set the location of predefined Act definitions. The value may be a path or an HTTP(S) URL supported by the Act processor. | `gw.acts`; otherwise the Act processor default. |
| `-a`, `--act [name or prompt]` | Enable Act mode and select an Act or prompt. With no value, read it from standard input. | Guidance mode unless present; the value is taken from `gw.act` when configured for Act mode. |

### Configuration properties

These are the configuration names defined by `GWConstants` and used or declared by the CLI:

| Property | Description | Default / context |
|---|---|---|
| `project.dir` | Base project directory (the project root). | Current user directory when absent; overridden by `-d`. |
| `gw.config` | Java system property naming the configuration file. | `gw.properties` when absent; overridden by `-c`. |
| `gw.model` | GenAI provider/model identifier. | Unset unless configured; overridden by `-m`. |
| `gw.instructions` | Default system instructions. | Unset unless configured; overridden by `-i`. |
| `gw.excludes` | Comma-separated exclusions. | Unset unless configured; overridden by `-e`. |
| `gw.acts` | Location of external Act definitions. | Processor default unless configured; overridden by `-as`. |
| `gw.act` | Default Act name or prompt. | Used when Act mode is selected; `-a` takes precedence. |
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

The pack also contains commented provider setting names. Set them as environment variables in the process environment, as required by the bundled GenAI client:

- CodeMie: `GENAI_USERNAME` and `GENAI_PASSWORD`.
- OpenAI-compatible services: `OPENAI_API_KEY` and, for a non-default endpoint, `OPENAI_BASE_URL`.

Only `gw.config` is read as a Java system-property override by the `Ghostwriter` entry point. For example:

```sh
java -Dgw.config=production.properties -jar gw.jar src
```

### Unix examples

```sh
# Guidance mode: project root, model, instructions, exclusions, and concurrency
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
rem Guidance mode with project root, model, instructions, exclusions, and concurrency
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
- **Configuration not loaded:** check the selected `-c` path or `-Dgw.config` value. A missing default `gw.properties` is tolerated, but an explicitly selected configuration file that cannot be loaded causes startup failure.
- **Act not found:** verify `--acts`/`gw.acts` points to the Act definitions and that the requested `--act` value is valid.
- **Thread errors:** ensure `--threads`/`gw.threads` is a valid integer; the processor requires a positive thread count.
- **Logs and debug output:** startup, configuration, scan progress, failures, and usage statistics are emitted through SLF4J and the logging backend packaged with the application. Configure that backend to enable DEBUG or TRACE for `org.machanism.machai.gw`; enable provider-client request logging only when safe. The CLI does not define a fixed log-file location.
- **Review changes:** inspect `git diff` or equivalent after processing and before publishing results.

## Documentation and contact

- Ghostwriter documentation: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- Source repository and issue/support entry point: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Bindex Core: https://machai.machanism.org/bindex-core/index.html
- CLI download: https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download

Ghostwriter is released under the Apache License 2.0. For provider-specific authentication, consult the documentation for the selected GenAI client/provider.
