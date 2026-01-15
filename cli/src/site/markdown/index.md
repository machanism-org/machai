# Machai CLI
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule separator between sections. 
- Scan `org/machanism/machai/cli` source folder and describe all supported Commands.
-->

The Machai Command Line Interface (CLI) is a Spring Shell application that helps you generate and register Machai metadata and run GenAI-assisted workflows from your terminal.

![Machai CLI Screenshot](images/machai-screenshot.png)

## Key Features

- Generate or update `bindex.json` metadata for Java projects.
- Register generated metadata into a metadata database.
- Pick relevant libraries using a natural-language prompt (or a prompt file).
- Assemble a new project from picked libraries.
- Process project files/documents using Ghostwriter (GenAI).
- Clean `.machai` temporary folders from a workspace.
- Configure default CLI settings (working directory, GenAI model, similarity score).

## Getting Started

1. **Download Machai CLI**

   Download the latest CLI JAR:

   [![Download zip](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai.jar/download)

2. **Configure environment variables**

   | Variable Name         | Description |
   |----------------------|-------------|
   | `OPENAI_API_KEY`      | API key used by the GenAI provider for AI-powered operations. |
   | `BINDEX_REG_PASSWORD` | Password for database write access (required for metadata registration). |

3. **Run Machai CLI**

   ```bash
   java -jar machai.jar
   ```

   Then enter `help` to see the available commands.

## Supported Commands

The CLI commands are implemented under `org.machanism.machai.cli`.

### `pick`

Picks (searches) libraries in the metadata database that match a natural-language prompt. The argument can be plain text or a path to a file containing the prompt.

**Usage**

```bash
pick "<prompt or file path>" [--registerUrl <url>] [--score <min-score>] [--genai <provider:model>]
```

**Notes**

- The last successful `pick` query and results are cached in the current CLI session and can be reused by `assembly`.

### `assembly`

Assembles a project in an output directory from a picked set of libraries (“bricks”). If `--query` is provided, it runs a `pick` first; otherwise it reuses the previous `pick` results.

**Usage**

```bash
assembly [--query "<prompt or file path>"] [--dir <output-folder>] [--registerUrl <url>] [--score <min-score>] [--genai <provider:model>]
```

### `prompt`

Sends a prompt directly to the configured GenAI provider and prints the response.

**Usage**

```bash
prompt --prompt "<text>" [--genai <provider:model>] [--dir <working-directory>]
```

### `bindex`

Generates (or updates) `bindex.json` metadata by scanning a directory.

**Usage**

```bash
bindex [--dir <project-path>] [--update true|false] [--genai <provider:model>]
```

### `register`

Registers generated `bindex.json` data to a metadata database.

**Usage**

```bash
register [--dir <project-path>] [--registerUrl <url>] [--update true|false]
```

### `gw`

Scans and processes files/documents using Ghostwriter (GenAI). You can optionally provide a root directory to define the project context.

**Usage**

```bash
gw [--scan <scan-folder>] [--root <root-folder>] [--genai <provider:model>]
```

### `clean`

Recursively removes all `.machai` temporary folders under the given directory.

**Usage**

```bash
clean [--dir <project-root>]
```

### `genai`

Sets the default GenAI service provider/model used when commands omit `--genai`.

**Usage**

```bash
genai --genai <provider:model>
```

### `dir`

Sets the default working directory used by commands when `--dir` / `--root` is omitted.

**Usage**

```bash
dir --dir <path>
```

### `score`

Sets the default minimum similarity score used by commands that support `--score`.

**Usage**

```bash
score --score <min-score>
```

### `conf`

Shows the current configuration properties (working directory, default GenAI model, and default score).

**Usage**

```bash
conf
```

<!-- @guidance: DO NOT REMOVE OR MODIFY GUIDANCE TAG CONTENT. KEEP IT AS IS. -->
