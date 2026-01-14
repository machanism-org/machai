# GW Maven Plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

## Overview

**GW Maven Plugin** (Ghostwriter Maven Plugin) generates and updates Maven Site documentation using embedded `@guidance:` comments found across your project.

It is designed to keep documentation synchronized with the codebase and requirements by making documentation generation a repeatable part of the build.

## Features

- Generates and refreshes Maven Site pages based on embedded `@guidance:` comments
- Works with Maven-based Java projects and standard Maven Site structure
- Supports documentation that stays aligned with code, tests, and evolving requirements
- Can be run from the command line or bound into the Maven lifecycle

## Getting Started

Add the plugin to your `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</plugin>
```

Run the plugin:

```sh
mvn org.machanism.machai:gw-maven-plugin:0.0.2-SNAPSHOT:process -Dgenai=Web:CodeMie
```

The generated and updated content is written to your Maven Site output directory.

## Requirements

- Maven 3+
- Java 8+

## License

Licensed under the Apache License, Version 2.0.
