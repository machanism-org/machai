# assembly-maven-plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/assembly-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)

The `assembly-maven-plugin` helps bootstrap and evolve Maven-based Java projects by automating common setup tasks and assisting with dependency selection.

## Overview

Use the plugin to assemble project structure and configuration from a simple project concept. It is intended to reduce manual setup while keeping all generated changes reviewable and editable.

## Key Features

- **Dependency suggestions:** Recommends libraries based on your goals.
- **Project bootstrap:** Creates or updates common project files and structure.
- **Reproducibility support:** Can integrate with Machanism metadata (for example `bindex.json`) to improve assembly control.
- **Reviewable output:** All changes are made in your project and can be refined as needed.

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6.x or newer
- Access to a compatible GenAI provider (for example OpenAI)

### Environment Variables

| Variable       | Description    |
|----------------|----------------|
| `OPENAI_API_KEY` | OpenAI API key |

### Basic Usage

Run the plugin goal:

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly
```

### Typical Workflow

1. Define your project concept in `project.txt` (or another file you reference).
2. Execute the plugin.
3. Review the updated `pom.xml` and generated/modified project files.

## Plugin Configuration

Common parameters:

| Parameter              | Description                                  | Default              |
|------------------------|----------------------------------------------|----------------------|
| `assembly.genai`       | AI model used for assembly tasks             | `OpenAI:gpt-5`       |
| `pick.genai`           | Model used for library selection             | `OpenAI:gpt-5-mini`  |
| `assembly.prompt.file` | Path to the project concept file             | `project.txt`        |
| `assembly.score`       | Minimum confidence score for recommendations | `0.80`               |

### Example

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly \
  -Dassembly.prompt.file=project.txt \
  -Dassembly.genai=OpenAI:gpt-5 \
  -Dpick.genai=OpenAI:gpt-5-mini \
  -Dassembly.score=0.80
```

## Resources

- [Machanism Platform](https://machanism.org)
- [Machai on GitHub](https://github.com/machanism-org/machai)
- [Maven](https://maven.apache.org)

## License

Licensed under the Apache 2.0 License.
