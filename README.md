# Machai Project

Machai is a GenAI-powered tool for automated creation, registration, and enhancement of software projects. It streamlines project assembly and library integration within the Machanism ecosystem using AI-driven semantic search and metadata management. The project consists of multiple modular components related to GenAI client, core utilities, command-line interfaces, and Maven plugins for enhanced project automation.

## Modules

- [genai-client](genai-client): Java library for convenient integration with GenAI APIs, enabling flexible configuration and usage through a unified interface, including command-function tools, file operations, and prompt management.
- [bindex-core](bindex-core): Core library for bindex metadata management, providing functionality for automated generation, registration, library selection, and project assembly.
- [cli](cli): Command Line Interface tool for generation, registration, and management of library metadata with AI-powered features, including project assembly and integration workflows.
- [bindex-maven-plugin](bindex-maven-plugin): Maven plugin that automates `bindex.json` generation and management for Java libraries, powered by GenAI and integration with Machanism platform.
- [assembly-maven-plugin](assembly-maven-plugin): Plugin for seamless AI-powered assembly of Maven projects using natural language queries and semantic search for optimal library selection and application setup.
- [ghostwriter](ghostwriter): Documentation engine that automates scanning, analysis, and assembly of project documentation using embedded rules, guidance, and AI synthesis.
- [gw-maven-plugin](gw-maven-plugin): Plugin for smart, GenAI-powered document processing and assistance in Maven-based projects, automating documentation review and updates.

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

Each module can be used separately or composed for broader project automation:

- To run the CLI:
  ```bash
  cd cli
  mvn exec:java
  ```

- Using Maven plugins (add to your pom.xml):

  ```xml
  <plugin>
    <groupId>org.machanism.machai</groupId>
    <artifactId>gw-maven-plugin</artifactId>
    <version>0.0.2-SNAPSHOT</version>
  </plugin>
  ```

Refer to each submodule's README for module-specific details and advanced usage instructions.

## Contributing

We welcome contributions! Please:

- Follow code style and best practices adopted in the project.
- Submit pull requests with clear descriptions and reference related issues if applicable.
- Use [GitHub Issues](https://github.com/machanism-org/machai/issues) for bug reports and feature requests.
- Review code and documentation before submitting.

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE.txt).

## Contact and Support

- Project website: [machanism.org/machai](https://machanism.org/machai)
- Issue Tracker: [GitHub Issues](https://github.com/machanism-org/machai/issues)
- Maintainer: Viktor Tovstyi ([viktor.tovstyi@gmail.com](mailto:viktor.tovstyi@gmail.com))
