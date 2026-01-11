# GW Maven Plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

## Overview

**GW Maven Plugin** (Ghostwriter Maven Plugin) streamlines documentation for Maven-based Java projects by leveraging embedded guidance tags to generate and keep documentation current with your source code and requirements. Integrated with the Maven Site lifecycle, it ensures your documentation remains accurate and up-to-date.

## Features

- Automatic documentation generation for Java projects
- Customizable output using guidance tags
- Dynamic analysis of code and documentation sources
- Documentation always reflects recent project changes
- Compatible with modular and multi-module Maven project structures

## Getting Started

To add GW Maven Plugin to your Maven project, include the following in your `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</plugin>
```

How to run:

```sh
mvn org.machanism.machai:gw-maven-plugin:0.0.2-SNAPSHOT:process -Dgenai=Web:CodeMie
```

This will produce updated project documentation in your configured Maven site output directory.

## Requirements

- Maven 3+
- Java 8+

## License

Licensed under the Apache License, Version 2.0.
