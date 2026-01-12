# assembly-maven-plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/assembly-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)

Welcome to the documentation for the assembly-maven-plugin. This Maven plugin provides advanced AI-driven automation for assembling and initializing Java projects, utilizing semantic search and intelligent configuration capabilities powered by the Machai platform. With assembly-maven-plugin, you can streamline project setup, enhance build orchestration, and benefit from intelligent, context-aware assembly processes for your Java applications. The Machai project is versatile and can be used beyond the Machanism platform, supporting a wide range of AI-driven automation scenarios.

## Overview

The `assembly-maven-plugin` is designed to accelerate setup and streamline dependency selection in Java projects. Powered by Machanism’s metadata-driven architecture, the plugin interprets developer requirements, recommends suitable libraries, and automates essential project initialization tasks.

## Key Features

- **Intelligent Library Discovery:** Enter natural language goals and receive machine-curated, contextually accurate dependency suggestions.
- **Automated Assembly:** Instantly bootstrap your Maven project with recommended dependencies, configuration files, and source code structure.
- **Platform Integration:** Seamless integration with Machanism metadata files such as `bindex.json`, as well as support for other platforms, providing enhanced assembly control and reproducibility.
- **Full Customization:** All modifications are visible and editable—review, adjust, and refine generated outputs to suit your needs.
- **Versatile Application:** The Machai project supports a wide range of AI-driven automation scenarios and is not limited to use with the Machanism platform.

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6.x or newer
- Access to GenAI service (compatible with OpenAI or similar providers)

### Environment Variables

| Variable          | Description                                   |
|-------------------|-----------------------------------------------|
| OPENAI_API_KEY    | OpenAI API key                                |

### Basic Usage

To assemble your Maven project using the plugin, run:

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly
```

#### Workflow
- Your requirements are analyzed by Machanism AI.
- Recommended dependencies are merged into your `pom.xml`.
- Project sources and configurations are created or updated as necessary.

## Plugin Configuration

The following parameters can be set to customize plugin behavior:

| Parameter                      | Description                                                 | Default        |
|--------------------------------|-------------------------------------------------------------|----------------|
| `assembly.genai`           | AI model used for assembly tasks                           | `OpenAI:gpt-5`        |
| `pick.genai`               | Model used for library selection                            | `OpenAI:gpt-5-mini`   |
| `assembly.prompt.file`         | Path to your project concept file                    | `project.txt`  |
| `assembly.score`               | Minimum confidence score for library recommendations   | `0.80`         |

### Example Workflow

1. Define your project concept or goals in `project.txt` (e.g., "Build a Spring Boot REST API for user authentication using Commercetools.").
2. Run the plugin:
   ```bash
   mvn org.machanism.machai:assembly-maven-plugin
   ```
3. Use custom arguments as needed:
   ```bash
   mvn org.machanism.machai:assembly-maven-plugin:assembly \
     -Dassembly.prompt.file=project.txt \
     -Dassembly.genai=Opengpt-5 \
     -Dpick.chatModel=gpt-5-mini \
     -Dassembly.score=0.80
   ```

## License

Licensed under the Apache 2.0 License.

## Resources
- [Machanism Platform](https://machanism.org)
- [AI Assembly Documentation](https://machanism.org/ai-assembly)
- [Machai Project on GitHub](https://github.com/machanism-org/machai)
- [Maven Official Website](https://maven.apache.org)
