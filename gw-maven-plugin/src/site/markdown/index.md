# GW Maven Plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

## Overview

The **GW Maven Plugin** (Ghostwriter Maven Plugin) provides automated documentation generation for Maven-based Java projects. Using embedded guidance tags within your source and configuration files, it scans your project, interprets instructions, and assembles project documentation that follows your specific needs and best practices. Seamlessly integrated with the Maven Site lifecycle, GW Maven Plugin helps maintain consistency, accuracy, and quality in your technical documentation.

## Features

- Automated Java project documentation
- Interprets guidance tags for custom documentation rules
- Analyzes source code for comprehensive references
- Ensures documentation stays current and relevant
- Supports modular Maven project structures

## Getting Started

To get started, add GW Maven Plugin to your `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</plugin>
```

Then execute:

```sh
mvn site
```

to produce automated documentation in the Maven site output directory.

## Requirements

- Maven 3+
- Java 8+

## License

This project is licensed under the Apache License, Version 2.0.
