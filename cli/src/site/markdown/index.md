# Machai CLI
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule separator between sections. 
- Scan `org/machanism/machai/cli` source folder and describe all supported Commands.
-->

The Machai Command Line Interface (CLI) enables developers and teams to generate, register, and manage library metadata for the Machanism platform directly from the terminal. It is designed to simplify complex project assembly and library integration workflows by harnessing advanced AI-powered features and seamless command access.

![](src/site/resources/images/machai-screenshot.png)

**Key Features:**
- Automated Metadata Generation: Rapidly analyze source files to create or update structured `bindex.json` files for Java libraries.
- Semantic Library Search: Use natural language prompts to discover and select libraries relevant to project requirements.
- Artifact Registration: Securely register library metadata, making it discoverable and ready for integration within the Machanism ecosystem.
- GenAI Document Processing: Scan and process files or documents using advanced GenAI features to extract insights or automate tasks.
- Temporary Resource Cleanup: Remove all `.machai` temporary folders with a single command for streamlined resource management.

## Getting Started
1. **Download the Machai CLI**  
   You can obtain the latest CLI `.jar` file from:
   [![Download zip](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai.jar/download)
2. **Set Environment Variables**
   Ensure you configure these environment variables prior to running metadata generation or registration commands:
   
   | **Variable Name**    | **Description**                                                                                      |
   |----------------------|------------------------------------------------------------------------------------------------------|
   | OPENAI_API_KEY       | Your OpenAI API key needed for AI-based features.                                                    |
   | BINDEX_REG_PASSWORD  | Password for registration database write access (required by `register` command only).               |
   
3. **Run Machai CLI**  
   Navigate to the directory containing `machai.jar` and run:
   ```bash
   java -jar machai.jar
   ```
   The CLI will display its banner and prompt for commands. Enter `help` to view all available commands.

## Supported Commands

The Machai CLI supports a flexible set of commands for library and project management. All commands below are implemented in the `org/machanism/machai/cli` package:

### `bindex`
Generate or update a `bindex.json` metadata file for a specified directory. Uses GenAI to analyze source code and resources.  
**Usage:** `bindex --dir <project-path> [--update=true] [--genai <model>]`
- `--update`: If true, updates existing `bindex.json` (default: true)
- `--genai`: GenAI provider/model (default: OpenAI:gpt-5.1)

### `register`
Register a generated `bindex.json` file with the Machanism platform’s registration database.  
**Usage:** `register --dir <project-path> [--registerUrl <url>] [--update=true]`
- `--registerUrl`: Target registration database URL
- `--update`: Update mode (default: true)

### `pick`
Find and select libraries (“bricks”) using natural language, a prompt, or a query file.  
**Usage:** `pick <prompt or file> [--score <min-score>] [--registerUrl <url>]`
- `--score`: Min similarity score for results  
- `--registerUrl`: Registration database URL

### `assembly`
Create a new project using selected libraries and an application prompt.  
**Usage:** `assembly --query <prompt> --dir <output-folder> [--score <min-score>] [--genai <model>] [--registerUrl <url>]`
- `--score`: Min similarity score for library selection
- `--genai`: GenAI provider/model  
- `--registerUrl`: Registration database URL

### `prompt`
Send a free-form prompt to GenAI for guidance or answers.  
**Usage:** `prompt --genai <model> --prompt <query>`

### `process`
Scan and process files or documents in a given directory using GenAI.  
**Usage:** `process --scan <scan-folder> [--root <root-folder>] [--genai <model>]`
- `--scan`: Directory to scan
- `--root`: Root directory for context
- `--genai`: GenAI provider/model (default: OpenAI:gpt-5.1)

### `clean`
Remove all `.machai` temporary folders from a specified directory (recursive).  
**Usage:** `clean [--dir <project-root>]`
- Erases all folders named `.machai` within the given location (defaults to user directory)

<!-- @guidance: DO NOT REMOVE OR MODIFY GUIDANCE TAG CONTENT. KEEP IT AS IS. -->
