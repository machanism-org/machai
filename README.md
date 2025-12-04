![](src/site/resources/images/machai-logo.png)

# Machai

**Machai** is a critical component of the [Machanism](https://machanism.org) platform, designed to revolutionize how customer applications are developed and assembled. By leveraging Generative AI (GenAI) and metadata-driven insights, Machai automates intelligent library selection and integration processes using semantic search capabilities.

## Overview

Machai empowers developers with AI-assisted project assembly in the **Machanism** ecosystem by automating repetitive development tasks.  
Each library in the platform is associated with a structured metadata file called `bindex.json`. This file contains crucial information about the library's features, integration points, and example usage.  

Machai stores these metadata files in a **vector database**, enabling efficient semantic search to find and recommend libraries based on natural language queries. This automated process simplifies application assembly and accelerates development.

Machai uses a special metadata file called bindex.json, which includes detailed information about each library, such as its name, version, features, tech stack, and usage instructions. These files are optimized for semantic search and stored in a vector database, allowing AI to match them with user queries. Developers can describe their project needs in simple language, and Machai provides a list of recommended libraries with all necessary integration details, making application assembly fast and efficient.
How Machai Works

For each library on the Machanism platform, Machai automatically generates a bindex.json file by analyzing project files like pom.xml and source code. These files are indexed in a vector database, enabling AI to perform semantic searches based on natural language input. Developers simply describe their application requirements, and GenAI processes the query, retrieves relevant libraries, and generates a report with suggestions and integration details, simplifying the development process.

For more information, visit [AI Assembly](https://machanism.org/ai-assembly).

## Bindex

For developers creating libraries for the Machanism platform, **bindex** files are essential metadata that describe your library and enable efficient library discovery and integration via Machai's AI-powered system. These files are automatically generated and registered in the Machanism platform using the **Machai Command-Line Interface (CLI)**. The CLI provides an easy, flexible way for library developers to manage and generate `bindex.json` files locally, whether for publishing new libraries or updating existing ones.

### How to Generate and Register a `bindex.json`

To generate and register a `bindex.json` file for your library, follow these steps:

1. **Download the Machai CLI**  
   Get the latest version of the Machai CLI as a `.jar` file from the link below:  
   [Download machai.jar from SourceForge](https://sourceforge.net/projects/machanism/files/machai.jar/download)

2. **Run the Machai CLI**  
   Open a terminal or command prompt and navigate to the directory where the `machai.jar` file is saved. Then execute the following command:  
   ```bash
   java -jar machai.jar
   ```

   Upon starting, you will see the Machai CLI banner and a prompt where you can enter commands. Use the `help` command to see the list of available options.

3. **Generate `bindex.json` for Your Library**  
   Use the CLI to analyze your library's project files and automatically generate the corresponding `bindex.json`. Navigate to your library directory and run:  
   ```bash
   > bindex <path-to-your-library>
   ```  
   Replace `<path-to-your-library>` with the path to your library's root directory. This command analyzes files such as `pom.xml`, source code, and other metadata to create a structured `bindex.json` file.

4. **Validate and Edit the `bindex.json`**  
   After generating the file, it is recommended to inspect and verify the contents of the `src/bindex/bindex.json` to ensure accuracy. You can manually edit the file to add or adjust descriptions, metadata, or integration details.

5. **Register Your `bindex.json`**  
   Once you are satisfied with the metadata, you can register your library and its `bindex.json` file into the Machanism platform via the Machai CLI:  
   ```bash
   > bindex <path-to-bindex.json>
   ```  
   Replace `<path-to-bindex.json>` with the path to the generated `bindex.json` file. This command uploads your library metadata to the platform and makes it discoverable for users.

---

### CLI Overview Output

When starting the Machai CLI, you will see the banner and usage information, as shown below:
```text
C:\projects\test>java -jar machai.jar
     _ . __  __            _           _
  \`"' ' |  \/  | __ _  ___| |__   __ _(_)
 /'`  \  | |\/| |/ _` |/ __| '_ \ / _` | |
/<"\     | |  | | (_| | (__| | | | (_| | |
/  _.-.  .|_|  |_|\__,_|\___|_| |_|\__,_|_|
`-' f/  | The Machai CLI | Machanism.Org
     `-' Powered by Spring Boot 3.4.0

Starting Application using Java 22.0.2 with PID 20928 (C:\Users\vikto\Downloads\machai.jar started by Viktor in C:\Users\vikto\Downloads)
No active profile set, falling back to 1 default profile: "default"
Unable to create a system terminal, creating a dumb terminal (enable debug logging for more information)
Started Application in 3.135 seconds (process running for 3.881)
shell:> help
```
Type `help` in the CLI to get a full overview of the available commands and their usage.

By using the Machai CLI for generating and registering `bindex.json` files, developers can ensure seamless integration with the Machanism platform, making their libraries easily discoverable and accessible to users.

### Bindex Registration

### Assembly

**Assemble an Application Using CLI**:
   Provide your project's requirements as a query:
   ```text
   shell:> assembly "Create a spring application for user login by commercetool."
   ```

## License

Machai is licensed under the Apache License 2.0.  
You can view the full license text [here](LICENSE).

## Contact

If you have any questions or need support, feel free to reach out:
- Official Website: [Machanism](https://machanism.org)
- Email: [support@machanism.org](mailto:develop@machanism.org)

Machai simplifies and accelerates application assembly, empowering developers to focus on innovation.