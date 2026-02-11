<!-- @guidance:
**VERY IMPORTANT NOTE:**  
Ghostwriter works with **all types of project files—including source code, documentation, project site content, and other relevant files**.
Ensure that your content generation and documentation efforts consider the full range of file types present in the project.

Generate a content:
1. **Project Title and Overview:**  
   - Provide the project name and a brief description of its purpose and main features.
2. **Module List:**  
   Generate a table listing all modules in the project with the following columns:
   **Name**: Display the module name as a clickable link in the format `[name]([artifactId]/)`, the [name] and [artifactId] values should be obtained from the module pom.xml file.
   **Description**: Provide a comprehensive description for each module, using the content from `[module_dir]/src/site/markdown/index.md`.
3. **Installation Instructions:**  
   - Describe how to clone the repository and build the project using Maven.
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
- Do not use UTF symbols in the content.
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# Ghostwriter

Ghostwriter is a documentation engine that scans a project, applies mandatory `@guidance` constraints embedded in source and documentation files, and uses GenAI to generate or update documentation consistently across source code, documentation, project site content, and other relevant project assets.

Key capabilities include:

- Project scanning for documentation targets across multiple file types
- Interpretation of inline `@guidance` blocks as mandatory constraints
- Generation and update of Markdown and other documentation artifacts
- Repeatable runs to keep documentation aligned with current project state

## Module List

This project is a single-module build.

| Name | Description |
|---|---|
| [Ghostwriter](ghostwriter/) | Ghostwriter CLI and core engine for scanning project files, enforcing `@guidance` constraints, and generating or updating documentation artifacts in a consistent, automated way. |

## Installation Instructions

### Prerequisites

- Java 11 or newer
- Maven 3.9+ recommended
- Network access to the configured GenAI provider

### Clone the repository

```cmd
git clone https://github.com/machanism-org/machai.git
cd machai
```

### Build with Maven

```cmd
mvn -pl ghostwriter -am clean verify
```

## Usage

### Run the CLI

Example of running Ghostwriter against a local project directory:

```cmd
java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar C:\projects\my-project
```

### Common options

Set an explicit root directory:

```cmd
java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar -r C:\projects\my-project
```

Target files with glob patterns:

```cmd
java -jar ghostwriter\target\ghostwriter-0.0.9-SNAPSHOT.jar "glob:**\*.md"
```

## Contributing

- Follow standard Java conventions and keep changes focused.
- Add or update tests under `src/test/java` when behavior changes.
- Open pull requests with a clear description, rationale, and reproduction steps when relevant.
- Report issues with logs, environment details (OS, Java, Maven), and minimal steps to reproduce.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](../../LICENSE).

## Contact and Support

- Source repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Downloads: https://sourceforge.net/projects/machanism/files/machai/gw.zip/download
