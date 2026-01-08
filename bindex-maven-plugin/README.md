# bindex-maven-plugin

Multi-module Java project for Maven integration and automated document processing in OSGi-based environments.

## Overview

The `bindex-maven-plugin` project enables automated indexing of Java packages for OSGi bundle generation. It aims to simplify Maven workflows by providing custom plugins and utilities for manifest creation, validation, and metadata extraction. Main features include:
- Maven plugin(s) for OSGi bundle index creation
- Manifest generation and validation automation
- Support for multi-module Java project structures
- Integration with standard Maven goals

## Usage

Clone the repository and run the plugin using Maven commands:

~~~bash
mvn clean install
mvn bindex:index
~~~

To use the plugin in your project, include it as a dependency in your `pom.xml`:

~~~xml
<plugin>
  <groupId>org.apache.felix</groupId>
  <artifactId>bindex-maven-plugin</artifactId>
  <version>YOUR_VERSION_HERE</version>
</plugin>
~~~

For more advanced usage, refer to each module's documentation and their respective configuration in your `pom.xml` files.

## Reference

This project is part of a larger parent project. For complete setup instructions and additional modules, refer to the parent project's repository and documentation.
