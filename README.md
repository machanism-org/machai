[@guidance]: <> (
The Home Project page should have following structure:
1. Project Introduction  
2. Reference to Further Information  
3. Project Modules Overview  
4. Usage and Documentation  
5. License Information  
6. Contact Information  
7. Closing Statement 
)
![](src/site/resources/images/machai-logo.png)

# Machai

Machai is an innovative project designed to harness Generative AI (GenAI) for the creation, registration, and enhancement of software projects within the [Machanism](https://machanism.org) ecosystem. With Machai, teams can streamline project assembly, automate repetitive tasks, and continuously improve their applications through intelligent recommendations and metadata-driven insights. Each library on the platform is accompanied by a structured metadata file, `bindex.json`, optimized for semantic search and natural language queries, accelerating integration and development.

## Reference to Further Information

For further details about Machai, its philosophy, and ecosystem, please visit:
- [AI Assembly](https://machanism.org/ai-assembly)
- [Machanism Platform](https://machanism.org)

[@guidance]: # (
List all sub-projects below, providing for each:
- The sub-project name as a heading.
- A concise description summarizing its purpose and functionality.
- A reference link to the sub-project’s directory or main documentation page (do not include `/README.md` in the URL; link only to the directory or appropriate documentation page).
Place the reference link immediately after the description.

Review the current sub-projects and ensure this section is accurate and complete. Update the list as needed to reflect all existing sub-projects.
)

## Project Modules Overview

### bindex-core
Central library for Machai metadata operations. Provides core APIs and tools for handling `bindex.json` files, semantic search contexts, and project descriptors. Powers metadata generation and parsing for all Machai modules.  
[Explore bindex-core](./bindex-core/)

### cli
The Machai Command Line Interface enables developers to generate, register, and manage project metadata directly from the terminal, integrating Machai's AI-powered assembly and semantic search features.  
[Explore CLI](./cli/)

### bindex-maven-plugin
Maven plugin that automates the generation of `bindex.json` files for Java projects. Integrates with GenAI providers and the Machanism platform to analyze code and generate comprehensive metadata descriptors for smart library discovery.  
[Explore bindex-maven-plugin](./bindex-maven-plugin/)

### assembly-maven-plugin
AI-powered Maven plugin for automated project assembly. Allows developers to use natural language queries to assemble Maven projects, select relevant libraries, and bootstrap code with semantic search from Machanism.  
[Explore assembly-maven-plugin](./assembly-maven-plugin/)

### ghostwriter
Automated documentation engine for Machai projects. Scans sources and resources, extracts embedded guidance, and generates rule-based documentation using AI, supporting multi-module and large codebases.  
[Explore ghostwriter](./ghostwriter/)

### genai-client
Java library providing pluggable interfaces and utilities to interact with Generative AI providers (primarily OpenAI). Powers AI-based prompts, embeddings, and tools consumed by other Machai modules and applications.  
[Explore genai-client](./genai-client/)

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

## Closing Statement

Machai simplifies and accelerates application assembly, empowering developers to focus on innovation.
