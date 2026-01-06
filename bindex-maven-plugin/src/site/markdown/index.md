# bindex-maven-plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the `---` separator between sections. -->

[![bindex-maven-plugin](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

The `bindex-maven-plugin` is a Maven plugin designed to automate the generation and management of `bindex.json` files for your Java libraries and projects. It leverages GenAI and the Machanism platform to analyze your project’s metadata, source code, and build files, producing a comprehensive descriptor that enables intelligent library discovery, semantic search, and streamlined integration.

## Features

- **Automated bindex.json Generation:**  
  Analyzes your Maven project and generates a standardized `bindex.json` file containing all relevant metadata, features, integration points, and usage examples.

- **GenAI-Powered Analysis:**  
  Utilizes Generative AI to extract and summarize project details, ensuring accurate and meaningful descriptors.

- **Seamless Integration with Machanism Platform:**  
  Prepares your library for registration and semantic search within the Machanism ecosystem.

- **Customizable Output:**  
  Allows manual review and editing of the generated `bindex.json` file before registration.

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6.x or higher
- Access to Machanism platform (optional for registration)

### Installation

Add the plugin to your Maven project’s `pom.xml`:

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

#### 1. Set Environment Variables

Before using the Machai CLI for generating or registering `bindex.json` files, you need to configure the following environment variables to ensure proper functionality:

| **Variable Name**    | **Description**                                                                                 |
|----------------------|-------------------------------------------------------------------------------------------------|
| BINDEX_REG_PASSWORD  | The password for write access to the registration database. Not required for assembly commands. |
| OPENAI_API_KEY       | Your OpenAI API key, required for AI-powered features.                                          |

#### 2. Generate bindex.json

Run the following command in your project directory:

```bash
mvn bindex:bindex
```

This will analyze your project and generate a `bindex.json` file in the designated output directory (by default, in the project root).

#### 3. Review and Edit

After generation, review the `bindex.json` file to ensure all metadata, descriptions, and integration points are accurate. You can manually edit the file to refine details as needed.

#### 4. Register Goal

The plugin provides a `register` goal to upload and register your `bindex.json` file with the Machanism platform. This step makes your library discoverable via semantic search and available for automated assembly in other projects.

To register your library, run:

```bash
mvn bindex:register
```

**Requirements:**
- Ensure your `bindex.json` file is up to date and located in the expected directory.
- You may need to provide authentication or API credentials for the Machanism platform (see platform documentation for details).

**What happens during registration:**
- The plugin validates your `bindex.json` file.
- Metadata and semantic embeddings are uploaded to the Machanism platform.
- Upon success, your library becomes available for discovery and integration by other developers.

## Configuration

You can customize the plugin’s behavior using the following parameters:

| Parameter             | Description                                                                                                                | Default   |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------|-----------|
| `bindex.inputs.only`  | If set to `true`, activates debug mode: only essential calls are sent to the LLM model. Large bindex context requests are saved to an `inputs.txt` file for review, without being sent to the LLM. | `false`     |
| `bindex.chatModel`    | Specifies the AI model to use for bindex.json generation and analysis.                                                      | `gpt-5`     |
| `update`              | If set to `true`, updates the existing `bindex.json` file if one is present; otherwise, generates a new file.               | `true`      |

**Example:**

```bash
mvn bindex:bindex -Dbindex.chatModel=gpt-5
```

## Example Workflow

1. Create or update your Maven project.
2. Add the plugin to your `pom.xml`.
3. Run the plugin to generate `bindex.json`.
4. Review and edit the file as needed.
5. Register the library with Machanism (optional).

## License

This project is licensed under the Apache 2.0 License.

## Links

- [Machanism Platform](https://machanism.org)
- [AI Assembly Documentation](https://machanism.org/ai-assembly)
- [Machai Project on GitHub](https://github.com/machanism-org/machai)
- [Maven Official Website](https://maven.apache.org)
