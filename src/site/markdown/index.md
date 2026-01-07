<!-- @guidance:
Generate a content:

1. **Project Title and Overview:**  
   - Provide the project name and a brief description of its purpose and main features.

2. **Module List:**  
   - List all modules in the project.
   - For each module, include its name, a short description, and a link to its subdirectory.
   - Links to the modules shuld have following format: [{{project.name}}](/{{project.artifactId}}).

3. **Installation Instructions:**  
   - Describe how to clone the repository and build the project (e.g., using Maven or Gradle).
   - Include prerequisites such as Java version and build tools.

4. **Usage:**  
   - Explain how to run or use the project and its modules.
   - Provide example commands or code snippets if applicable.

5. **Contributing:**  
   - Outline guidelines for contributing to the project, including code style, pull request process, and issue reporting.

6. **License:**  
   - State the project’s license and provide a link to the license file.

7. **Contact and Support:**  
   - Include contact information or links for support and further questions.

**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# Machai Project

Machai is a GenAI-powered tool for automated creation, registration, and enhancement of software projects. It streamlines project assembly and library integration within the Machanism ecosystem using AI-driven semantic search and metadata management. The project consists of multiple modular components related to a GenAI client, core utilities, command-line interfaces, and Maven plugins for enhanced project automation.

## Modules

- [genai-client](/genai-client): GenAI client for interacting with AI-powered services and endpoints.
- [bindex-core](/bindex-core): Core utilities and abstraction for metadata indexing and semantic search.
- [cli](/cli): Command-line interface for executing project automation tasks.
- [bindex-maven-plugin](/bindex-maven-plugin): Maven plugin for integrating indexing and semantic features in the build process.
- [assembly-maven-plugin](/assembly-maven-plugin): Maven plugin for advanced project assembly and packaging.
- [ghostwriter](/ghostwriter): Automated project content generation using AI.
- [gw-maven-plugin](/gw-maven-plugin): Maven plugin to integrate Ghostwriter AI features and automation.

## Installation

Clone the repository and build with Maven:

```bash
# Prerequisites
# - Java 9 or newer
# - Maven 3.6.0 or newer

git clone https://github.com/machanism-org/machai.git
cd machai
mvn clean install
```

## Usage

Each module can be used separately or composed as part of broader automation:

- To run the CLI:

```bash
cd cli
mvn exec:java
```

- Maven plugins (`bindex-maven-plugin`, `assembly-maven-plugin`, `gw-maven-plugin`) can be added to your project pom.xml:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</plugin>
```

Refer to each submodule's README for details and advanced usage instructions.

## Contributing

We welcome contributions!

- Follow code style and best practices adopted in the project.
- Submit pull requests with clear descriptions and reference related issues if applicable.
- Use [GitHub Issues](https://github.com/machanism-org/machai/issues) for bug reports and feature requests.
- Review code and documentation before submitting.

## License

This project is licensed under the [Apache License, Version 2.0](../../LICENSE.txt).

## Contact & Support

- Project website: [machanism.org/machai](https://machanism.org/machai)
- Issue Tracker: [GitHub Issues](https://github.com/machanism-org/machai/issues)
- Maintainer: Viktor Tovstyi ([viktor.tovstyi@gmail.com](mailto:viktor.tovstyi@gmail.com))
