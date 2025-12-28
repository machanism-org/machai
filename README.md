![](src/site/resources/images/machai-logo.png)

# Machai

Machai is an innovative project designed to harness Generative AI (GenAI) for the creation, registration, and enhancement of software projects within the [Machanism](https://machanism.org) ecosystem. With Machai, teams can streamline project assembly, automate repetitive tasks, and continuously improve their applications through intelligent recommendations and metadata-driven insights. Whether you are building new solutions or enhancing existing ones, Machai provides the tools and automation needed to accelerate development and ensure high-quality results.

Machai streamlines project assembly by automating repetitive development tasks through AI assistance. Each library on the platform is accompanied by a structured metadata file, `bindex.json`, which contains essential details such as features, integration points, technology stack, and usage examples.

These metadata files are stored in a **vector database** and optimized for semantic search, enabling Machai to efficiently match libraries to natural language queries. Developers can specify their project requirements in plain language, and Machai recommends suitable libraries along with comprehensive integration information, accelerating application assembly and development.

For more information, visit [AI Assembly](https://machanism.org/ai-assembly).

## Machai CLI

The **Machai Command Line Interface (CLI)** is a powerful tool for generating, registering, and managing library metadata within the [Machanism](https://machanism.org) platform. It gives developers direct access to Machai’s AI-powered features, streamlining project assembly and integration workflows from the terminal.

[![Download zip](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai.jar/download)  

**Key Features:**
- **Automated Metadata Generation:** Analyze project files and source code to create structured `bindex.json` metadata files.
- **Semantic Search Integration:** Use AI to match libraries with project requirements via natural language queries.
- **Library Registration:** Register new or updated libraries, making them discoverable and ready for integration.
- **Flexible Command Set:** Generate metadata, search libraries, register artifacts, and more.

For full details and usage instructions, see the [Machai CLI README](./cli/README.md).

## Assembly Maven Plugin

The **assembly-maven-plugin** extends Machai’s AI-powered assembly capabilities to Maven projects. This plugin enables developers to use natural language queries to select and integrate the most relevant libraries from the Machanism platform, automating the creation of project structure, configuration files, and initial code.

**Key Features:**
- **AI-Driven Library Selection:** Describe your project requirements in plain language and let Machai recommend the best libraries.
- **Automated Project Assembly:** Instantly generate Maven project structure, dependencies, and boilerplate code.
- **Semantic Search Integration:** Leverage Machanism’s vector database for precise library matching.
- **Customizable Workflow:** Review and refine generated files to fit your needs.

For installation, configuration, and usage details, see the [assembly-maven-plugin README](./assembly-maven-plugin/README.md).

## Bindex Maven Plugin

The **bindex-maven-plugin** automates the generation and management of `bindex.json` metadata files for your Java libraries and projects. By analyzing your project’s source code and build files, it produces comprehensive descriptors that enable intelligent library discovery and semantic search within the Machanism ecosystem.

**Key Features:**
- **Automated bindex.json Generation:** Create standardized metadata files with features, integration points, and usage examples.
- **GenAI-Powered Analysis:** Extract and summarize project details using Generative AI.
- **Seamless Registration:** Register your library metadata with the Machanism platform for enhanced discoverability.
- **Customizable Output:** Review and edit generated files before registration.

For installation, configuration, and usage details, see the [bindex-maven-plugin README](./bindex-maven-plugin/README.md).

## License

Machai is licensed under the Apache License 2.0.  
You can view the full license text [here](LICENSE).

## Contact

If you have any questions or need support, feel free to reach out:
- Official Website: [Machanism](https://machanism.org)
- Email: [develop@machanism.org](mailto:develop@machanism.org)

Machai simplifies and accelerates application assembly, empowering developers to focus on innovation.