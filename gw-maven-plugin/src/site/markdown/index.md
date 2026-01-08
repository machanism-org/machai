# GW Maven Plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

## Overview

The **GW Maven Plugin** (Ghostwriter Maven Plugin) is an advanced documentation automation tool for Java projects. It automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis. The plugin streamlines the creation and maintenance of high-quality, consistent documentation, ensuring best practices and up-to-date information across all modules.

## Features

- Automated project documentation synthesis
- Guidance tag recognition for custom & best-practices documentation
- Java source code analysis
- Maven plugin integration and seamless operation
- Consistency enforcement and best practices
- Support for modular and multi-module projects

## Getting Started

Add the plugin to your Maven project and configure it in your `pom.xml`. The plugin will automatically process your source files and generate up-to-date documentation in the Maven Site lifecycle.

## Usage Example

Include in your build configuration:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</plugin>
```

Documentation will be included in your project's site output using the `mvn site` command.

## Requirements

- Maven 3+
- Java 8+

## License

This plugin is released under the Apache License, Version 2.0.
