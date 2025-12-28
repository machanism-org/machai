# assembly-maven-plugin

[![assembly-maven-plugin](https://img.shields.io/maven-central/v/org.machanism.machai/assembly-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)

The `assembly-maven-plugin` is a submodule of the Machai project that enables seamless AI-powered assembly of Maven projects using the Machanism platform. This plugin provides the `assembly` goal, which leverages natural language queries and semantic search to pick and assemble the most relevant libraries for your Maven-based application.

## Features

- **AI-Driven Library Selection:** Use natural language queries to describe your project requirements. The plugin utilizes Machanism’s semantic search to recommend and select the best libraries.
- **Automated Project Assembly:** Automatically generates and configures your Maven project structure, dependencies, and initial code based on selected libraries.
- **Integration with Machanism Platform:** Works with the Machanism ecosystem and bindex.json metadata for precise, maintainable, and scalable project assembly.
- **Developer Oversight:** Generated code and configurations can be reviewed and customized to fit your specific needs.

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6.x or higher
- Access to the Machanism platform and required API keys (e.g., OpenAI)

### Usage


#### 1. Set Environment Variables

   Before using assembly-maven-plugin you need to configure the following environment variables to ensure proper functionality:

   | **Variable Name**    | **Description**                                                                                   |
   |----------------------|---------------------------------------------------------------------------------------------------|
   | OPENAI_API_KEY       | Your OpenAI API key, required for AI-powered features.                                            |

#### 2. Assembly Goal

The main goal provided by this plugin is `assembly`. It allows you to pick and assemble your Maven project using a natural language query.

Run the following command in your project directory:

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly:0.0.1
```

#### 3. Review and Customize

After execution, the plugin will:

- Select relevant libraries using Machanism’s semantic search.
- Generate or update your `pom.xml` with the required dependencies.
- Create initial source files and configuration as needed.

Review the generated files and make any necessary adjustments to fit your project’s needs.


## Configuration

You can customize the plugin’s behavior using additional parameters:

| Parameter                      | Description                                                                                    | Default      |
|--------------------------------|------------------------------------------------------------------------------------------------|--------------|
| `assembly.inputs.only`   | If set to `true`, only input files specified by the user will be considered during assembly. | false        |
| `assembly.chatModel`     | Specifies the AI model to use for the assembly process.                                        | gpt-5        |
| `pick.chatModel`          | Specifies the AI model to use for the pick (library selection) process.                        | gpt-5-mini   |
| `assembly.prompt.file`  | Path to the file containing the prompt or requirements for the assembly process.               | project.txt  |
| `assembly.score`          | Minimum relevance score threshold for including libraries in the assembly.                     | 0.80         |


**Example:**

1. Create a `project.txt` file in your current directory with your natural language project requirements, for example:

   ```
   Create a Spring Boot web application with MongoDB integration
   ```

2. Run the plugin with the following command:

   ```bash
   mvn org.machanism:assembly-maven-plugin:assembly -Dassembly.prompt.file=project.txt
   ```

*The plugin will read your requirements from `project.txt` and assemble the Maven project accordingly.*

Or, if you need to customize parameters:

```bash
mvn org.machanism:assembly-maven-plugin:assembly\
 -Dassembly.prompt.file=project.txt\
 -Dassembly.inputs.only=false\
 -Dassembly.chatModel=gpt-5\
 -Dpick.chatModel=gpt-5-mini\
 -Dassembly.score=0.80
```

## License

This project is licensed under the Apache 2.0 License.

## Links

- [Machanism Platform](https://machanism.org)
- [AI Assembly Documentation](https://machanism.org/ai-assembly)
- [Machai Project on GitHub](https://github.com/machanism-org/machai)
