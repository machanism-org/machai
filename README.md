<!--
@guidance:
**ADD FOLLOWING SECTIONS TO THIS README FILE:**

1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\site\markdown\index.md` content summary.
   - Add `![](src/site/resources/images/machai-logo.png)` before the title.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)` after the title as a new paragraph.
   
2. **Module List:**  
   - List all modules in the project.
   - For each module, include its name, a short description, and a link to its module.

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

![](src/site/resources/images/machai-logo.png)

# Machai

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

Machai is a modular, AI-driven toolkit for automating software project setup, semantic indexing, build orchestration, and lifecycle management. Powered by GenAI agents (Ghostwriter, semantic search, metadata indexing, and automated code/documentation generation), Machai streamlines project integration and assembly. While it is designed to work seamlessly within the Machanism ecosystem, Machai can also be used independently for a wide range of software automation tasks.

## Modules

- [genai-client](/genai-client): Provides remote GenAI endpoint interfaces for contextual automations and AI integrations.
- [bindex-core](/bindex-core): Supplies core utilities for semantic indexing, metadata management, and project data operations.
- [cli](/cli): CLI entrypoint for interactive project initialization and Machai task execution.
- [bindex-maven-plugin](/bindex-maven-plugin): Maven plugin for project semantic indexing and metadata enrichment.
- [assembly-maven-plugin](/assembly-maven-plugin): Maven plugin for artifact assembly, packaging, and release handling.
- [ghostwriter](/ghostwriter): Integrates with GenAI for code and documentation generation automation.
- [gw-maven-plugin](/gw-maven-plugin): Maven extension for incorporating Ghostwriter outputs into the build lifecycle.

## Installation Instructions

To clone and build Machai:

~~~bash
# Prerequisites:
# - Java 9 or newer
# - Maven 3.6 or newer

git clone https://github.com/machanism-org/machai.git
cd machai
mvn clean install
~~~

## Usage

To run the CLI:

~~~bash
cd cli
mvn exec:java
~~~

To use Machai Maven plugins, add to your `pom.xml`:

~~~xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</plugin>
~~~

Module directories provide individual usage documentation and examples.

## Contributing

- Follow code style and commit conventions adopted in the repository.
- Reference issues in all pull requests when relevant.
- Submit bugs or feature requests via [GitHub Issues](https://github.com/machanism-org/machai/issues).
- Ensure all contributions are documented and comprehensively tested.

## License

Machai is distributed under the [Apache License, Version 2.0](LICENSE.txt).

## Contact and Support

- Homepage: [machanism.org/machai](https://machanism.org/machai)
- Issues: [GitHub Issues](https://github.com/machanism-org/machai/issues)
- Maintainer: Viktor Tovstyi (<viktor.tovstyi@gmail.com>)
