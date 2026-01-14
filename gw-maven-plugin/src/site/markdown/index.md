# GW Maven Plugin
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

## Overview

**GW Maven Plugin** (Ghostwriter Maven Plugin) helps keep project documentation current by generating Maven Site content from embedded `@guidance:` tags found in source and documentation files.

It integrates with the Maven build so documentation can be refreshed consistently as part of your normal workflow.

## Features

- Generates/updates Maven Site documentation based on embedded `@guidance:` tags
- Works with Maven-based Java projects
- Supports keeping documentation synchronized with code and requirements
- Runs from the command line or as part of the Maven lifecycle

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

The generated/updated content is written to your Maven Site output directory.

## Requirements

- Maven 3+
- Java 8+

## License

Licensed under the Apache License, Version 2.0.
