<!-- @guidance:
The Home Project page should have following structure:
1. Project Introduction  
2. Reference to Further Information  
3. Project Modules Overview  
4. Usage and Documentation  
5. License Information  
6. Contact Information  
-->

![](src/site/resources/images/machai-logo.png)

# Machai

Machai is an innovative project designed to harness Generative AI (GenAI) for the creation, registration, and enhancement of software projects within the [Machanism](https://machanism.org) ecosystem. With Machai, teams can streamline project assembly, automate repetitive tasks, and continuously improve their applications through intelligent recommendations and metadata-driven insights. Each library on the platform is accompanied by a structured metadata file, `bindex.json`, optimized for semantic search and natural language queries, accelerating integration and development.

## Reference to Further Information

For further details about Machai, its philosophy, and ecosystem, please visit:
- [AI Assembly](https://machanism.org/ai-assembly)
- [Machanism Platform](https://machanism.org)

<!-- @guidance:
List all sub-projects below, providing for each:
- The sub-project name as a heading; For getting name need to open sub-module pom.xml file, read the project.name and use it for the heading text.
- A concise description summarizing its purpose and functionality.
- A reference link to the sub-project’s directory or main documentation page (do not include `/README.md` in the URL; link only to the directory or appropriate documentation page).
Place the reference link immediately after the description.

Review the current sub-projects and ensure this section is accurate and complete. Update the list as needed to reflect all existing sub-projects. 
--> 

## Project Modules Overview

### Bindex Core
bindex-core provides core functionality for bindex metadata management, including generation, registration, library selection, and project assembly. It enables automated handling of library metadata to support efficient discovery, integration, and assembly workflows within the Machanism ecosystem.  
[Explore Bindex Core](./bindex-core/)

### Machai CLI
Machai CLI is a command-line tool for generating, registering, and managing library metadata within the Machanism ecosystem. It leverages GenAI to automate project assembly and enable semantic search for efficient library discovery and integration.  
[Explore Machai CLI](./cli/)

### Bindex Maven Plugin
bindex-maven-plugin enables automated generation and registration of bindex metadata for Maven projects. It facilitates library discovery, integration, and assembly by leveraging structured metadata and GenAI-powered semantic search within the Machanism ecosystem.  
[Explore Bindex Maven Plugin](./bindex-maven-plugin/)

### Project Assembly Maven Plugin
assembly-maven-plugin automates the assembly of projects within the Machanism ecosystem by integrating libraries based on bindex metadata. It streamlines dependency resolution, library selection, and project packaging using GenAI-powered semantic search and metadata-driven workflows.  
[Explore Project Assembly Maven Plugin](./assembly-maven-plugin/)

### Ghostwriter
Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis.  
[Explore Ghostwriter](./ghostwriter/)

### GenAI Client
Java library for interacting with Generative AI providers. Provides foundational prompt and embedding capabilities to power AI-based features in other Machai modules.  
[Explore GenAI Client](./genai-client/)

## Usage and Documentation

To get started:
- See example usage and configuration details in each module’s README within its directory.
- Explore general platform documentation at [Machanism Platform](https://machanism.org).
- For semantic search, project assembly, and metadata generation, refer to the [Machai CLI](./cli/), [Assembly Maven Plugin](./assembly-maven-plugin/), and other modules.

## License Information

Machai is licensed under the Apache License 2.0.  
You can view the full license text [here](LICENSE).

## Contact Information

If you have any questions or need support, feel free to reach out:
- Official Website: [Machanism](https://machanism.org)
- Email: [develop@machanism.org](mailto:develop@machanism.org)
