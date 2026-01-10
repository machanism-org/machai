# Machai CLI
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule separator between sections. 
- Scan `org/machanism/machai/cli` source folder and describe all supported Commands.
-->

The Machai Command Line Interface (CLI) empowers developers and teams to generate, register, and manage library metadata for the Machanism platform directly from the terminal. Enjoy powerful AI-enabled workflows for seamless project assembly, metadata integration, and document processing, all with minimal manual steps.

![Machai CLI Screenshot](src/site/resources/images/machai-screenshot.png)

**Key Features**
- **Automated Metadata Generation** — Instantly analyze and produce `bindex.json` files for Java projects using GenAI.
- **Semantic Library Search** — Discover, select, and integrate libraries using natural language or query files.
- **Artifact Registration** — Register library metadata securely to make it visible within the Machanism ecosystem.
- **GenAI Document Processing** — Process project files and documents for insights or automation tasks via GenAI.
- **Resource Cleanup** — Recursively remove all `.machai` temporary folders from your workspaces for efficient resource management.

## Getting Started

1. **Download Machai CLI**  
   Download the latest CLI `.jar` file:
   [![Download zip](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai.jar/download)
2. **Configure Environment Variables**  
   Set the following environment variables before running commands:
   
   | Variable Name         | Description                                                        |
   |----------------------|--------------------------------------------------------------------|
   | OPENAI_API_KEY       | Your OpenAI API key for AI-powered operations.                     |
   | BINDEX_REG_PASSWORD  | Password for database write access (required for `register`).       |

3. **Run Machai CLI**  
   In terminal:
   ```bash
   java -jar machai.jar
   ```
   Enter `help` after launch to see available commands.

## Supported Commands

The CLI provides the following commands (all implemented in `org/machanism/machai/cli`):

### `bindex`
Create or update a `bindex.json` metadata file for a directory using GenAI analysis.  
**Usage:** `bindex --dir <project-path> [--update=true] [--genai <model>]`
- `--dir` — Directory to scan
- `--update` — Update mode (default: true)
- `--genai` — GenAI provider/model (default: OpenAI:gpt-5.1)

### `register`
Upload the generated `bindex.json` to the Machanism platform's metadata database.  
**Usage:** `register --dir <project-path> [--registerUrl <url>] [--update=true]`
- `--dir` — Directory with source `bindex.json`
- `--registerUrl` — Target database URL
- `--update` — Update mode (default: true)

### `pick`
Discover and select libraries (“bricks”) using a prompt or query file.  
**Usage:** `pick <prompt or file> [--score <min-score>] [--registerUrl <url>]`
- `--score` — Minimum similarity score  
- `--registerUrl` — Metadata database URL

### `assembly`
Generate a new project from selected libraries and an assembly prompt.  
**Usage:** `assembly --query <prompt> --dir <output-folder> [--score <min-score>] [--genai <model>] [--registerUrl <url>]`
- `--query` — Assembly prompt (or use previous pick)
- `--dir` — Output directory
- `--score` — Library similarity score filter
- `--genai` — GenAI provider/model
- `--registerUrl` — Database URL

### `prompt`
Send a prompt directly to GenAI for suggestions or guidance.  
**Usage:** `prompt --genai <model> --prompt <query>`

### `process`
Scan and process documents in a directory using GenAI features.  
**Usage:** `process --scan <scan-folder> [--root <root-folder>] [--genai <model>]`
- `--scan` — Directory to scan
- `--root` — Root context directory
- `--genai` — GenAI provider/model (default: OpenAI:gpt-5.1)

### `clean`
Recursively delete all `.machai` temporary folders from a target directory.  
**Usage:** `clean [--dir <project-root>]`
- `--dir` — Root directory (defaults to user directory)

<!-- @guidance: DO NOT REMOVE OR MODIFY GUIDANCE TAG CONTENT. KEEP IT AS IS. -->
