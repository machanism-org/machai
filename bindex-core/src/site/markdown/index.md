# Bindex Core
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

Welcome to **Bindex Core**, a library for generating and consuming *bindex* metadata.

## Overview

Bindex Core provides the core APIs and implementations used to generate and consume bindex metadata for Java-based builds and libraries.

## Features

- Generate bindex metadata for Java projects
- Assemble metadata across modules and dependencies
- Extensible integration points for Maven models and plugin APIs

## Getting Started

Bindex Core is available on Maven Central. Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>bindex-core</artifactId>
  <version>${bindex-core.version}</version>
</dependency>
```

## Usage

Use Bindex Core to:

- Create bindex metadata as part of your build tooling
- Read and interpret existing bindex metadata at runtime or during build analysis
- Integrate metadata generation/consumption with Maven-oriented models and plugins

For related tooling and documentation, see <https://github.com/machanism-org/machai>.

## License

Apache License, Version 2.0.

## Contact

- Viktor Tovstyi (<viktor.tovstyi@gmail.com>)
