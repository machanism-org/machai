# Machai CLI
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule separator between sections. 
- Scan `org/machanism/machai/cli` source folder and describe all supported Commands.
-->

The Machai Command Line Interface (CLI) is a Spring Shell-based tool for generating, registering, and managing Machai library metadata and for running GenAI-assisted workflows from your terminal.

![Machai CLI Screenshot](src/site/resources/images/machai-screenshot.png)

## Key Features

- Generate or update `bindex.json` metadata for Java projects.
- Register generated metadata into a metadata database.
- Search (“pick”) relevant libraries using a natural-language prompt or a prompt file.
- Assemble a new project from picked libraries.
- Process project files/documents using GenAI.
- Clean `.machai` temporary folders from a workspace.

## Getting Started

1. **Download Machai CLI**

   Download the latest CLI JAR:

   [![Download zip](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai.jar/download)

2. **Configure environment variables**

   | Variable Name        | Description |
   |---------------------|-------------|
   | `OPENAI_API_KEY`     | API key used by the GenAI provider for AI-powered operations. |
   | `BINDEX_REG_PASSWORD`| Password for database write access (required for metadata registration). |

3. **Run Machai CLI**

   ```bash
   java -jar machai.jar
   ```

   Then enter `help` to see the available commands.

## Supported Commands

The CLI commands are implemented under `org/machanism/machai/cli`.

### `assembly`

Creates a project in an output directory using a picked set of libraries (“bricks”). If a query is provided, it will pick libraries first; otherwise it reuses the previous `pick` results.

**Usage**

```bash
assembly --query "<prompt>" --dir <output-folder> [--registerUrl <url>] [--score <min-score>] [--genai <provider:model>]
```

**Options**

- `--query` — Assembly prompt. If omitted, the previous pick query is reused (if available).
- `--dir` — Output directory (defaults to the current user directory).
- `--registerUrl` — Metadata database URL (optional).
- `--score` — Minimum similarity threshold for library search.
- `--genai` — GenAI provider/model (defaults to `Ghostwriter.CHAT_MODEL` when not provided).

### `pick`

Searches the metadata database for libraries matching a natural-language prompt. The first argument can be either plain text or a path to a file containing the prompt.

**Usage**

```bash
pick "<prompt or file path>" [--registerUrl <url>] [--score <min-score>]
```

**Options**

- `--registerUrl` — Metadata database URL (optional).
- `--score` — Minimum similarity threshold for search results.

### `prompt`

Sends a prompt directly to the configured GenAI provider and prints the response.

**Usage**

```bash
prompt --prompt "<text>" [--genai <provider:model>]
```

**Options**

- `--prompt` — The user prompt to send.
- `--genai` — GenAI provider/model (defaults to `Ghostwriter.CHAT_MODEL` when not provided).

### `bindex`

Generates (or updates) `bindex.json` metadata by scanning a directory.

**Usage**

```bash
bindex --dir <project-path> [--update=true|false] [--genai <provider:model>]
```

**Options**

- `--dir` — Directory to scan (defaults to the current user directory).
- `--update` — Update mode.
- `--genai` — GenAI provider/model (defaults to `Ghostwriter.CHAT_MODEL` when not provided).

### `register`

Registers the generated `bindex.json` to a metadata database.

**Usage**

```bash
register --dir <project-path> [--registerUrl <url>] [--update=true|false]
```

**Options**

- `--dir` — Directory containing the project (defaults to the current user directory).
- `--registerUrl` — Target metadata database URL (optional).
- `--update` — Update mode.

### `process`

Scans and processes documents/files using GenAI. You can optionally provide a root directory to define the project context.

**Usage**

```bash
process --scan <scan-folder> [--root <root-folder>] [--genai <provider:model>]
```

**Options**

- `--scan` — Directory to scan (defaults to `--root`, or the user directory if `--root` is not provided).
- `--root` — Root context directory (defaults to the current user directory).
- `--genai` — GenAI provider/model (defaults to `Ghostwriter.CHAT_MODEL` when not provided).

### `clean`

Recursively removes all `.machai` temporary folders under the given directory.

**Usage**

```bash
clean [--dir <project-root>]
```

**Options**

- `--dir` — Root directory to clean (defaults to the current user directory).

<!-- @guidance: DO NOT REMOVE OR MODIFY GUIDANCE TAG CONTENT. KEEP IT AS IS. -->
