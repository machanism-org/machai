# GW Maven Plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the `---` separator between sections. -->

Welcome to the GW Maven Plugin! This plugin provides powerful, automated document processing capabilities utilizing advanced GenAI-powered workflows within your Maven project's build lifecycle.

## Overview
GW Maven Plugin automates the scanning, processing, and assistance of documentation for Maven-based projects. Leveraging GenAI and Maven project layout abstraction, it streamlines your documentation workflow and ensures up-to-date, high-quality project docs with minimal effort.

## Key Features
- **Automated Document Scanning**: Integrates deeply with Maven to locate and process project documentation.
- **GenAI-Powered Assistance**: Utilizes external AI provider(s) for smart document improvement, generation, and updating.
- **Flexible Configuration**: Select which documents and models to use via Maven parameters (`docs.inputs.only`, `docs.chatModel`).
- **Lifecycle Integration**: Executes seamlessly within your build pipeline, making documentation processing part of your project automation.

## Getting Started
To use the GW Maven Plugin, add the following configuration to your Maven `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>${project.version}</version>
  <executions>
    <execution>
      <goals>
        <goal>docs</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

### Key Parameters
- `docs.inputs.only`  
  If set to `true`, only input documents (not outputs) are processed.
- `docs.chatModel`  
  Specifies the chat model for AI assistance in documentation (default: `OpenAI:gpt-5`).

## How It Works
The Docs goal scans documents in your Maven project and processes them using the selected AI provider. It is tailored to your project layout, including both single-module and multi-module configurations. Run your Maven lifecycle (e.g., `mvn install`) to trigger documentation assistance automatically.

## Typical Use Cases
- Keeping documentation up-to-date with the latest code changes
- Generating smart summaries or intros for project files
- Reviewing existing documentation for completeness and clarity

## Contributing & Feedback
Encounter an issue or have suggestions? Contributions and feedback are welcome! Visit the project repository or get in touch to help improve GW Maven Plugin.

## License
GW Maven Plugin is distributed under Open Source terms. See your project repository for specific licensing details.
