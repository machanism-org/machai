# bindex-maven-plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

[![bindex-maven-plugin](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

The **bindex-maven-plugin** generates and maintains a `bindex.json` descriptor for a Maven project. The descriptor is used by Machanism/Machai tooling to power metadata management, discovery, and automation workflows.

## Getting started

### Requirements

- Java 8+
- Maven 3.6+

### Add the plugin

Add to your `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-maven-plugin</artifactId>
      <version>${bindex-maven-plugin.version}</version>
      <executions>
        <execution>
          <goals>
            <goal>bindex</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Run the goals

Generate (or update) `bindex.json`:

```bash
mvn bindex:bindex
```

Register/publish the descriptor (optional):

```bash
mvn bindex:register
```

## What it does

- Generates a `bindex.json` file for the current Maven module.
- Updates an existing descriptor when present.
- Can register/publish the descriptor to a registry when configured.

## Configuration

The plugin supports configuring behavior either via standard Maven plugin configuration in your `pom.xml` or via system properties on the command line.

Common parameters:

| Parameter | Description | Default |
|---|---|---|
| `update` | Update an existing `bindex.json` if present. | `true` |

Example:

```bash
mvn bindex:bindex -Dupdate=false
```

## Environment variables

Some features may require credentials/configuration depending on how you configure registration or publishing.

| Variable name | Description |
|---|---|
| `BINDEX_REG_PASSWORD` | Required only when writing to the registration database/registry. |

## Links

- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)
- [Machai](https://github.com/machanism-org/machai)
- [Maven](https://maven.apache.org)
