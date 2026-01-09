# Machai CLI
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule separator between sections. 
- Scan `org/machanism/machai/cli` source folder and describe all supported Commands.
-->

The Machai Command Line Interface (CLI) is a versatile tool designed to facilitate the generation, registration, and management of library metadata within the **Machanism** platform. It provides developers with direct access to Machai’s AI-powered features, enabling efficient project assembly and integration workflows from the terminal.

![](src/site/resources/images/machai-screenshot.png)

**Key Features:**
- Automated Metadata Generation: Analyze project files and source code to automatically create structured `bindex.json` metadata files for libraries.
- Semantic Search Integration: Leverage AI to match libraries with project requirements using natural language queries.
- Library Registration: Register new or updated libraries in the Machanism platform, ensuring they are discoverable and ready for integration.
- Flexible Command Set: Access a range of commands for generating metadata, searching libraries, registering artifacts, and more.

## Getting Started
1. **Download the Machai CLI**  
   Get the latest version of the Machai CLI as a `.jar` file from the link below:
   [![Download zip](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai.jar/download)  
2. **Set Environment Variables**
   Before using the Machai CLI for generating or registering `bindex.json` files, you need to configure the following environment variables to ensure proper functionality:
   | **Variable Name**    | **Description**                                                                                   |
   |----------------------|---------------------------------------------------------------------------------------------------|
   | OPENAI_API_KEY       | Your OpenAI API key, required for AI-powered features.                                            |
   | BINDEX_REG_PASSWORD  | The password for write access to the registration database, required for `register` command only. |
3. **Run the Machai CLI**  
   Open a terminal or command prompt and navigate to the directory where the `machai.jar` file is saved. Then execute the following command:
   ```bash
   java -jar machai.jar
   ```
   Upon starting, you will see the Machai CLI banner and a prompt where you can enter commands. Use the `help` command to see the list of available options.

## Supported Commands

The Machai CLI provides several commands for flexible management and assembly of libraries and projects. Below is a summary of the main commands found in the `org/machanism/machai/cli` source folder:

### `bindex`
Creates or updates a `bindex.json` metadata file for the specified project or library directory by analyzing source files with GenAI.  
**Usage:** `bindex --dir <project-path> [--update=true] [--genai <model>]`
- `--update`: Update an existing `bindex.json` (default: true)
- `--genai`: Specify GenAI model (default: OpenAI:gpt-5.1)

### `register`
Registers a previously generated `bindex.json` metadata file into the Machanism platform’s registration database.  
**Usage:** `register --dir <project-path> [--registerUrl <url>] [--update=true]`
- `--registerUrl`: Target registration database URL
- `--update`: Update mode (default: true)

### `pick`
Finds and selects libraries (“bricks”) using either a natural language prompt or a query file.  
**Usage:** `pick <prompt or file> [--score <min-score>] [--registerUrl <url>]`
- `--score`: Minimum similarity score for library results
- `--registerUrl`: Registration database URL

### `assembly`
Assembles a new project using selected libraries and an application prompt.  
**Usage:** `assembly --query <prompt> --dir <output-folder> [--score <min-score>] [--genai <model>] [--registerUrl <url>]`
- `--score`: Minimum similarity score
- `--genai`: Specify the GenAI model
- `--registerUrl`: Registration database URL

### `prompt`
Sends a free-form prompt to the GenAI provider for AI guidance or information.  
**Usage:** `prompt --genai <model> --prompt <query>`

### `process`
Scans and processes files or documents in a directory using GenAI.  
**Usage:** `process --scan <scan-folder> [--root <root-folder>] [--genai <model>]`
- `--scan`: The directory to scan for processing
- `--root`: The project root directory
- `--genai`: GenAI provider/model (default: OpenAI:gpt-5.1)

### `clean`
Removes all `.machai` temporary folders from the specified root directory.  
**Usage:** `clean [--dir <project-root>]`
- Removes all folders named `.machai` recursively under the given directory (defaults to user directory)

<!-- @guidance: DO NOT REMOVE OR MODIFY GUIDANCE TAG CONTENT. KEEP IT AS IS. -->
