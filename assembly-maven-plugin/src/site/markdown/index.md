# assembly-maven-plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

[![assembly-maven-plugin](https://img.shields.io/maven-central/v/org.machanism.machai/assembly-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)

Welcome to the `assembly-maven-plugin` documentation. This plugin streamlines Maven project assembly by employing advanced AI-powered semantic search and automation capabilities from the Machanism platform.

## Overview

The `assembly-maven-plugin` leverages technology from Machanism to provide automatic and intelligent configuration for Maven projects. By interpreting developer goals and requirements, it accelerates dependency selection, initial setup, and project scaffolding.

## Key Features

- **Smart Library Suggestions:** Describe your goals in natural language and receive tailored library recommendations from Machanism AI.
- **Automated Assembly & Initialization:** Instantly set up Maven projects with optimal dependencies and starter files.
- **Rich Integration:** Works seamlessly with Machanism's metadata-driven assembly through files like `bindex.json`.
- **Developer Fine-Tuning:** All generated outputs are reviewable, editable, and customizable for precise control.

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6.x or newer
- Machanism platform account and API key (compatible with OpenAI or similar providers)

### Environment Variables

| Variable          | Description                                   |
|-------------------|-----------------------------------------------|
| OPENAI_API_KEY    | Required for accessing Machanism's AI features|

### Plugin Usage

To assemble your Maven project, use the command below in your project's root:

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly
```

#### What Happens
- Machanism AI interprets your requirements.
- Suitable libraries are selected and merged into your `pom.xml`.
- Source files and configurations are generated or updated for your project.

## Configuration

You can customize the plugin with the following parameters:

| Parameter                      | Description                                                 | Default        |
|--------------------------------|-------------------------------------------------------------|----------------|
| `assembly.inputs.only`         | Restricts request to essential data for debugging; saves context | `false`    |
| `assembly.chatModel`           | Selects the AI model for assembly tasks                     | `gpt-5`        |
| `pick.chatModel`               | Specifies the model used for library picking                | `gpt-5-mini`   |
| `assembly.prompt.file`         | Sets the path to your project prompt or concept file        | `project.txt`  |
| `assembly.score`               | Minimum confidence score for library inclusion              | `0.80`         |

### Example Workflow

1. Provide your project's goal (e.g., "Build a Spring Boot REST API for user login using Commercetools.") in `project.txt`.
2. Run the plugin:
   ```bash
   mvn org.machanism.machai:assembly-maven-plugin
   ```
3. Specify parameters for customization:
   ```bash
   mvn org.machanism.machai:assembly-maven-plugin:assembly \
     -Dassembly.prompt.file=project.txt \
     -Dassembly.inputs.only=false \
     -Dassembly.chatModel=gpt-5 \
     -Dpick.chatModel=gpt-5-mini \
     -Dassembly.score=0.80
   ```

## License

This project is licensed under the Apache 2.0 License.

## Resources
- [Machanism Platform](https://machanism.org)
- [AI Assembly Documentation](https://machanism.org/ai-assembly)
- [Machai Project on GitHub](https://github.com/machanism-org/machai)
- [Maven Official Website](https://maven.apache.org)
