# bindex-maven-plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

[![bindex-maven-plugin](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

Welcome to the `bindex-maven-plugin` project, a Maven extension that automates the creation and management of `bindex.json` files for Java projects. Purpose-built for the Machanism ecosystem, it leverages Generative AI via Machanism to extract, document, and register your project's metadata for integration, semantic search, and ecosystem interoperability.

## Key Features

- **Automated Metadata Generation:** Parse and analyze your Maven project to produce a standardized `bindex.json` descriptor including metadata, features, entry points, and examples.
- **GenAI-Driven Analysis:** Use AI-powered summarization to enhance accuracy and completeness of your project descriptor.
- **Machanism Platform Integration:** Seamless workflow to register and expose your libraries in the Machanism registry.
- **Editable Output:** Optionally review and modify generated descriptors before registration.

## Getting Started

### Prerequisites
- Java 8+
- Maven 3.6.x+
- Machanism platform account (optional for registration)

### Installation
Add to your `pom.xml`:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism</groupId>
      <artifactId>bindex-maven-plugin</artifactId>
      <version>0.0.1</version>
    </plugin>
  </plugins>
</build>
```

### Usage

#### Environment Variables
Configure before using Machai CLI:
| Variable Name        | Description                                                                |
|---------------------|----------------------------------------------------------------------------|
| BINDEX_REG_PASSWORD | Password for registration DB write access. Not required for assembly.      |
| OPENAI_API_KEY      | Required for GenAI-powered features.                                       |

#### Generate `bindex.json`
Run:
```bash
mvn bindex:bindex
```

#### Manual Review
Edit `bindex.json` for accuracy after generation if desired.

#### Register Descriptor
To register on Machanism, run:
```bash
mvn bindex:register
```
Requirements:
- `bindex.json` up to date
- Authentication credentials for Machanism (see platform docs)

## Configuration

Parameters:
| Parameter             | Description                                                                                                                             | Default   |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------|-----------|
| bindex.inputs.only    | If true, enables debug mode: only critical LLM calls, larger context output to `inputs.txt`.                                            | false     |
| bindex.chatModel      | Select AI model for GenAI analysis and descriptor generation.                                                                           | gpt-5     |
| update               | If true, updates existing `bindex.json`.                                                                                                 | true      |

Example:
```bash
mvn bindex:bindex -Dbindex.chatModel=gpt-5
```

## Example Workflow
1. Prepare/edit your project.
2. Add plugin to `pom.xml`.
3. Run the plugin to generate `bindex.json`.
4. Review/edit output.
5. Register with Machanism (optional).

## License
Apache 2.0

## Links
- [Machanism Platform](https://machanism.org)
- [AI Assembly Documentation](https://machanism.org/ai-assembly)
- [Machai Project on GitHub](https://github.com/machanism-org/machai)
- [Maven Official Website](https://maven.apache.org)
