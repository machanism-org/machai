<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\site\markdown\index.md` content summary.
   - Add `![](src/site/resources/images/machai-logo.png)` before the title.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)` after the title as a new paragraph.
2. **Module List:**  
   - List all modules in the project.
   - For each module, include its name, a short description, and a link to its module
3. **Installation Instructions:**  
   - Describe how to clone the repository and build the project (e.g., using Maven or Gradle).
   - Include prerequisites such as Java version and build tools.
4. **Usage:**  
   - Explain how to run or use the project and its modules.
   - Provide example commands or code snippets if applicable.
5. **Contributing:**  
   - Outline guidelines for contributing to the project, including code style, pull request process, and issue reporting.
6. **License:**  
   - State the project's license and provide a link to the license file.
7. **Contact and Support:**  
   - Include contact information or links for support and further questions.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

![](src/site/resources/images/machai-logo.png)

# Machai Project

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

Machai is a multi-module toolkit for GenAI-enabled developer automation. It provides Java libraries and Maven tooling that unify access to multiple GenAI providers, generate and consume Bindex metadata for library discovery and reuse, and automate repository-scale documentation updates from embedded guidance using Ghostwriter.

## Modules

| Module | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility library for describing and working with conventional project directory layouts in a consistent way across tools and plugins. |
| [GenAI Client](genai-client/) | Java library for provider-agnostic integration with Generative AI services, including prompts, tools, file inputs, and embeddings. |
| [Bindex Core](bindex-core/) | Core library for Bindex metadata generation, registration, semantic library selection, and project assembly workflows. |
| [Ghostwriter](ghostwriter/) | Guidance-driven documentation and repository automation engine for scanning files, applying embedded guidance, and writing AI-assisted updates. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that integrates Ghostwriter workflows into Maven builds for guided and act-based automation. |

## Installation

### Prerequisites

- Git
- Java 17 recommended for building the full project
- Maven 3.8.1 or later

### Clone and build

```bat
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -U clean install
```

To build and stage the Maven Site:

```bat
mvn clean install site site:stage
```

## Usage

### Build a specific module

```bat
mvn -pl genai-client clean install
```

### Run Ghostwriter CLI

```bat
cd ghostwriter
mvn -Ppack package
java -jar target\gw.jar src\site\markdown
```

### Run Maven plugin goals

Guided processing:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.1.0-SNAPSHOT:gw -Dgw.scanDir=src\site
```

Act mode:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.1.0-SNAPSHOT:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src\site
```

### Use the libraries in Java projects

Add the required module as a Maven dependency and use it for project layout handling, GenAI integration, Bindex workflows, or guided repository automation.

## Contributing

- Follow the existing code style and conventions used in the repository.
- Keep changes focused and include tests where applicable.
- Use GitHub Issues for bug reports and feature requests: https://github.com/machanism-org/machai/issues
- Submit pull requests with a clear description, rationale, and reproduction details when fixing bugs.
- Review generated documentation and site changes before submitting a pull request.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](LICENSE.txt).

## Contact and Support

- Website: https://machai.machanism.org
- Source repository: https://github.com/machanism-org/machai
- Issue tracker: https://github.com/machanism-org/machai/issues
- Maintainer: Viktor Tovstyi (viktor.tovstyi@gmail.com)
