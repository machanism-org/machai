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

The Machai CLI exposes a set of commands for working with libraries, files, and documents:

### bindex
Generates a `bindex.json` metadata file for the specified library/project directory. Uses GenAI to analyze source files.
- Usage: `bindex <project-path>`
- Options: `--update` (update mode), `--genai` (specify GenAI model)

### register
Registers a previously generated `bindex.json` file into the Machanism platform registration database.
- Usage: `register <project-path>`
- Options: `--registerUrl` (target database), `--update` (update mode)

### pick
Finds/picks libraries (bricks) by using a natural language prompt describing your project or query file.
- Usage: `pick <prompt or file>`
- Options: `--score` (min similarity score), `--registerUrl` (registration DB URL)

### assembly
Creates an assembled project from a set of picked libraries and an application prompt.
- Usage: `assembly` `--query <prompt>` `--dir <output-folder>`
- Options: `--score`, `--genai` (specify GenAI model), `--registerUrl`

### clean
Removes all `.machai` temporary folders from the specified root directory.
- Usage: `clean [--dir <project-root>]`

### process
Scans and processes files/documents in a directory using GenAI.
- Usage: `process --scan <scan-folder> [--root <root-folder>] [--genai <model>]`

### prompt
Sends a free-form prompt to the GenAI provider for guidance.
- Usage: `prompt --prompt <query>`
