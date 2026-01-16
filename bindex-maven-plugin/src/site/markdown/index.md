# bindex-maven-plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

[![bindex-maven-plugin](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

The **bindex-maven-plugin** generates and maintains a `bindex.json` descriptor for a Maven project. The descriptor is used by Machanism/Machai tooling to support metadata management, discovery, and automation workflows.

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

## Goals

- `bindex` — Generate (or update) `bindex.json` for the current module.
- `register` — Register/publish the descriptor when registry settings are configured.

## What it does

- Generates a `bindex.json` file for the current Maven module.
- Updates an existing descriptor when present.
- Can register/publish the descriptor to a registry when configured.

## Configuration

Configure the plugin in your `pom.xml` using standard Maven plugin configuration.

Common parameters:

| Parameter | Description | Default |
|---|---|---|
| `update` | Update an existing `bindex.json` if present. | `true` |

Example (system property):

```bash
mvn bindex:bindex -Dupdate=false
```

## Environment variables

Registration/publishing may require credentials depending on your registry configuration.

| Variable name | Description |
|---|---|
| `BINDEX_REG_PASSWORD` | Required only when writing to the registration database/registry. |

## Links

- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)
- [Machai](https://github.com/machanism-org/machai)
- [Maven](https://maven.apache.org)
