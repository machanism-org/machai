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

## Project Modules Overview
<!-- This section must consistently reflect the most recent and complete list of submodules. Review and update this section whenever changes are made to the submodule configuration to ensure accuracy and alignment with the current project structure. Update according to current sub-module list.

Output format:

### [Sub-Project Name]
[Sub-Project Name] provides [brief description of core functionality and purpose].  
[Explore Sub-Project Name](./[sub-project-folder]/)
 --> 

### genai-client
Genai Client provides integration and connectivity to generative AI services for project intelligence and automation.
[Explore genai-client](./genai-client/)

### bindex-core
Bindex Core offers core project metadata management, semantic search, and metadata-driven integration tools.
[Explore bindex-core](./bindex-core/)

### cli
CLI delivers a command-line interface for project assembly, searching, and metadata manipulation in the Machai platform.
[Explore cli](./cli/)

### bindex-maven-plugin
Bindex Maven Plugin enables Maven-based extraction, publication, and management of project metadata.
[Explore bindex-maven-plugin](./bindex-maven-plugin/)

### assembly-maven-plugin
Assembly Maven Plugin automates the assembly and integration of projects using metadata and intelligent GenAI recommendations.
[Explore assembly-maven-plugin](./assembly-maven-plugin/)

### ghostwriter
Ghostwriter generates, maintains, and improves source code using generative AI models within the Machai ecosystem.
[Explore ghostwriter](./ghostwriter/)

### ghostwriter-maven-plugin
Ghostwriter Maven Plugin provides Maven integration for source code generation, improvement, and maintenance using GenAI.
[Explore ghostwriter-maven-plugin](./ghostwriter-maven-plugin/)


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

