# Machai CLI
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

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

## Generate `bindex.json`

For developers creating libraries for the Machanism platform, **bindex** files are essential metadata that describe your library and enable efficient library discovery and integration via Machai's AI-powered system. These files are automatically generated and registered in the Machanism platform using the **Machai Command-Line Interface (CLI)**. The CLI provides an easy, flexible way for library developers to manage and generate `bindex.json` files locally, whether for publishing new libraries or updating existing ones.
 
 1. **Run `bindex` Command** 
   Use the CLI to analyze your library's project files and automatically generate the corresponding `bindex.json`. Navigate to your library directory and run:  
   ```bash
   > bindex <project-path>
   ```  
   Replace `<project-path>` with the path to your library project root directory. This command analyzes files such as `pom.xml`, `packaje.json` or `pyproject.toml`, source code, and other metadata to create a structured `bindex.json` file.

2. **Validate and Edit the `bindex.json`**  
   After generating the file, it is recommended to inspect and verify the contents of the `bindex.json` to ensure accuracy. You can manually edit the file to add or adjust descriptions, metadata, or integration details.

## Bindex Registration

Once you are satisfied with the metadata, you can register your library and its `bindex.json` file into the Machanism platform via the Machai CLI:  
```bash
shell:> register <project-path>
```  
Replace `<project-path>` with the path to the project with `bindex.json` file. This command uploads your library metadata to the platform and makes it discoverable for users.

## Pick Libraries

To verify the registration process and ensure the library is successfully uploaded, use the `pick` command:  
```bash
shell:> pick <prompt>
```  
Replace `<prompt>` with the description of the application to be created. This command will pick the "bricks" and confirm the presence of the library in the system.


## Assembly

**Assemble an Application Using CLI**:
   Provide your project's requirements as a query:
   ```text
   shell:> assembly
   ```

## License

This project is licensed under the Apache 2.0 License.

## Links

- [Machanism Platform](https://machanism.org)
- [AI Assembly Documentation](https://machanism.org/ai-assembly)
- [Machai Project on GitHub](https://github.com/machanism-org/machai)

