[@guidance]: # (This is a home page of `Machai` project.)
![](src/site/resources/images/machai-logo.png)

# Machai

Machai is an innovative project designed to harness Generative AI (GenAI) for the creation, registration, and enhancement of software projects within the [Machanism](https://machanism.org) ecosystem. With Machai, teams can streamline project assembly, automate repetitive tasks, and continuously improve their applications through intelligent recommendations and metadata-driven insights. Each library on the platform is accompanied by a structured metadata file, `bindex.json`, optimized for semantic search and natural language queries, accelerating integration and development.

For more information, visit [AI Assembly](https://machanism.org/ai-assembly).

[@guidance]: # (Below, list all sub-projects with a short description and provide references to their README pages. When creating links to the sub-projects' README files, do not include `/README.md` in the URL; instead, link to the directory or appropriate documentation page.)

## Project Modules Overview

Machai comprises several powerful modules, each addressing specific development or documentation needs within the Machanism ecosystem:

### [Machai CLI](./cli/)
A feature-rich command-line tool for generating, registering, and managing `bindex.json` metadata, performing semantic search, and assembling applications via terminal commands.

### [Assembly Maven Plugin](./assembly-maven-plugin/)
A Maven plugin for AI-powered project assembly. Use natural language queries to automatically select libraries, generate structure, and configure dependencies for new Java projects.

### [Bindex Maven Plugin](./bindex-maven-plugin/)
Automates the creation and management of `bindex.json` metadata for Java libraries using GenAI. Enables library registration and semantic search integration in Machanism.

### [Bindex Core](./bindex-core/)
Core library for bindex metadata creation, processing, and registration. Supplies builder and processor implementations to support language-specific bindex file generation for various project types.

### [Machai Docs](./machai-docs/)
Documentation engine and generator for Machai projects. Uses AI-driven parsing and guidance-tag extraction to assemble, check, and present documentation from source and markdown files.

### [GenAI Client](./genai-client/)
GenAI provider and client library powering Machai’s AI-driven analysis, prompt management, model selection, and integration with third-party AI services for both code assembly and documentation.

> For detailed usage, configuration, and examples, refer to each module’s README or the [Machanism Platform](https://machanism.org).

## License

Machai is licensed under the Apache License 2.0.  
You can view the full license text [here](LICENSE).

## Contact

If you have any questions or need support, feel free to reach out:
- Official Website: [Machanism](https://machanism.org)
- Email: [develop@machanism.org](mailto:develop@machanism.org)

Machai simplifies and accelerates application assembly, empowering developers to focus on innovation.
