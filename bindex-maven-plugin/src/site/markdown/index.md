# bindex-maven-plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

[![bindex-maven-plugin](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

The **bindex-maven-plugin** generates and maintains a `bindex.json` descriptor for a Maven project. The descriptor is used by the Machanism/Machai tooling to power metadata management, discovery, and automation workflows.

## What it does

- Generates a `bindex.json` file for the current Maven module.
- Updates an existing descriptor when present.
- Optionally registers/publishes the descriptor to a registry (when configured).

## Requirements

- Java 8+
- Maven 3.6+

## Add the plugin

Add to your `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-maven-plugin</artifactId>
      <version>${bindex-maven-plugin.version}</version>
    </plugin>
  </plugins>
</build>
```

## Use the plugin

Generate (or update) `bindex.json`:

```bash
mvn bindex:bindex
```

Register/publish the descriptor (optional):

```bash
mvn bindex:register
```

## Configuration

Common parameters:

| Parameter            | Description                                                                 | Default |
|---------------------|-----------------------------------------------------------------------------|---------|
| `bindex.inputs.only` | Emits additional debug output (LLM inputs/outputs) to `inputs.txt`.         | `false` |
| `bindex.chatModel`   | AI model used for analysis/descriptor generation (when enabled/available). | `gpt-5` |
| `update`             | Update an existing `bindex.json` if present.                                | `true`  |

Example:

```bash
mvn bindex:bindex -Dbindex.chatModel=gpt-5
```

## Environment variables

Some features require credentials/configuration:

| Variable name         | Description                                                      |
|----------------------|------------------------------------------------------------------|
| `OPENAI_API_KEY`      | Required for GenAI-powered analysis (if enabled).                 |
| `BINDEX_REG_PASSWORD` | Required only when writing to the registration database/registry. |

## Links

- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)
- [Machai](https://github.com/machanism-org/machai)
- [Maven](https://maven.apache.org)
