# GW Maven Plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

## Overview

The **GW Maven Plugin** (Ghostwriter Maven Plugin) is a powerful documentation automation solution for Java-based Maven projects. Leveraging embedded guidance tags and intelligent synthesis, it enables automatic scanning, analysis, and assembly of project documentation. With its seamless integration into Maven build lifecycles, the plugin ensures your documentation is accurate, consistent, and always aligned with current best practices.

## Features

- Automated documentation generation for Java projects
- Interprets @guidance tags to follow custom and best-practice instructions
- Analyzes Java source for comprehensive technical and API references
- Ensures documentation continuity and maintainability
- Built for modular and multi-module Maven project structures

## Getting Started

To use GW Maven Plugin, add it to your Maven project's `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</plugin>
```

Run `mvn site` to generate updated, automated documentation into your project's site output.

## Requirements

- Maven 3 or higher
- Java 8 or higher

## License

Distributed under the Apache License, Version 2.0.
