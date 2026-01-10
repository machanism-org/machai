# bindex-maven-plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

[![bindex-maven-plugin](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

Welcome to the bindex-maven-plugin project, a modern Maven extension for automating the creation and management of bindex.json files in Java projects. While purpose-built for the Machanism ecosystem, this plugin leverages Generative AI via Machai to extract, document, and register project metadata for semantic search, integration, and platform interoperability. The plugin’s capabilities extend beyond Machanism, supporting a wide range of AI-driven automation and metadata management scenarios.

## Key Features

- **Automated Metadata Generation:** Parse and analyze Maven projects to produce standardized `bindex.json` descriptors including metadata, features, entry points, and illustrative examples.
- **GenAI-Driven Analysis:** AI-powered summarization ensures accuracy and completeness for descriptors and documentation.
- **Machanism Registry Integration:** Seamless workflow for exposing libraries/modules in the Machanism registry.
- **Editable Output:** Modify and validate generated descriptors before official registration.

## Getting Started

### Prerequisites
- Java 8 or newer
- Apache Maven 3.6.x or newer
- Machanism account for registration (optional for local usage)

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

#### Review & Edit
Examine the generated `bindex.json` for project accuracy and completeness. The file may be modified prior to registration.

#### Register Descriptor
To publish to the Machanism registry, run:
```bash
mvn bindex:register
```
Requirements:
- Ensure `bindex.json` is current.
- Authentication credentials for Machanism (see platform documentation).

## Plugin Configuration

Parameters:
| Parameter             | Description                                                                                                                             | Default   |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------|-----------|
| bindex.inputs.only    | Debug mode for critical LLM input/output, context output to `inputs.txt`.                                                               | false     |
| bindex.chatModel      | Select AI model for GenAI analysis and descriptor generation.                                                                           | gpt-5     |
| update               | Update existing `bindex.json` when true.                                                                                                | true      |

Example:
```bash
mvn bindex:bindex -Dbindex.chatModel=gpt-5
```

## Typical Workflow
1. Prepare or update your Java project.
2. Add the plugin to your `pom.xml`.
3. Run the plugin to generate a `bindex.json` descriptor.
4. Review and optionally edit the output descriptor.
5. Register with Machanism if desired.

## License
Apache License 2.0

## Useful Links
- [Machanism Platform](https://machanism.org)
- [AI Assembly Documentation](https://machanism.org/ai-assembly)
- [Machai Project on GitHub](https://github.com/machanism-org/machai)
- [Maven Official Website](https://maven.apache.org)
