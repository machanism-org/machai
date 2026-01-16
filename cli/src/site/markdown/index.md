# Machai CLI
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule separator between sections. 
- Scan `org/machanism/machai/cli` source folder and describe all supported Commands.
-->

The Machai Command Line Interface (CLI) is a Spring Boot + Spring Shell application for generating and registering Machai metadata, picking libraries from a metadata database, assembling projects, and running Ghostwriter (GenAI) file-processing workflows from the terminal.

![Machai CLI Screenshot](images/machai-screenshot.png)

## Key Features

- Generate or update `bindex.json` metadata for a project.
- Register generated metadata into a metadata database.
- Pick relevant libraries using a natural-language prompt (or a prompt file).
- Assemble a new project from previously picked libraries.
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
   | `OPENAI_API_KEY`      | API key used by the configured GenAI provider for AI-powered operations. |
   | `BINDEX_REG_PASSWORD` | Password for database write access (required for metadata registration). |

3. **Run Machai CLI**

   ```bash
   java -jar machai.jar
   ```

   Then enter `help` to see the available commands.

## Supported Commands

Commands are implemented in `org.machanism.machai.cli`.

### `pick`

Picks (searches) libraries in the metadata database that match a natural-language prompt. The prompt can be provided directly or as a path to a file.

**Usage**

```bash
pick --query "<prompt or file path>" [--registerUrl <url>] [--score <min-score>] [--genai <provider:model>]
```

**Notes**

- Results are cached in the current CLI session and can be reused by `assembly` when `--query` is omitted.

### `assembly`

Assembles a project in a working directory from a picked set of libraries (“bricks”). If `--query` is provided, the command runs a `pick` first; otherwise it reuses the previous `pick` results.

**Usage**

```bash
assembly [--query "<prompt or file path>"] [--dir <output-folder>] [--registerUrl <url>] [--score <min-score>] [--genai <provider:model>]
```

### `prompt`

Sends a prompt directly to the configured GenAI provider/model and prints the response.

**Usage**

```bash
prompt --query "<text>" --genai <provider:model> [--dir <working-directory>]
```

### `bindex`

Generates (or updates) `bindex.json` metadata by scanning a project directory.

**Usage**

```bash
bindex [--dir <project-path>] [--update true|false] [--genai <provider:model>]
```

### `register`

Registers a generated `bindex.json` file into a metadata database.

**Usage**

```bash
register [--dir <project-path>] [--registerUrl <url>] [--update true|false]
```

### `gw`

Runs Ghostwriter file processing for a directory. By default, the scan directory is the working directory.

**Usage**

```bash
gw [--dir <working-directory>] [--scan <scan-folder>] [--threads true|false] [--genai <provider:model>]
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
genai <provider:model>
```

### `dir`

Sets the default working directory used when `--dir` is omitted.

**Usage**

```bash
dir <path>
```

### `score`

Sets the default minimum similarity score used by commands that support `--score`.

**Usage**

```bash
score <min-score>
```

### `conf`

Shows the current configuration properties.

**Usage**

```bash
conf
```

<!-- @guidance: DO NOT REMOVE OR MODIFY GUIDANCE TAG CONTENT. KEEP IT AS IS. -->
