# Bindex Core
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

Welcome to **Bindex Core**, the core library for generating and consuming *bindex* metadata.

## Overview

Bindex Core provides the APIs and reference implementations to:

- Generate bindex metadata for Java-based builds and libraries.
- Read, merge, and analyze bindex metadata produced by other tools.
- Support integration points commonly used by Maven-oriented models and plugins.

## Key Concepts

Bindex metadata describes Java artifacts in a structured form that build tools can produce and other tools can consume. Typical inputs and outputs include:

- Generated metadata files for an artifact or module.
- Aggregated metadata assembled across multi-module builds and dependency graphs.

## Features

- Metadata generation for Java projects.
- Aggregation across modules and dependencies.
- Extensible integration points for Maven models and plugin APIs.

## Getting Started

Bindex Core is available on Maven Central. Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>bindex-core</artifactId>
  <version>${bindex-core.version}</version>
</dependency>
```

## Typical Usage

You can use Bindex Core to:

- Produce bindex metadata as part of build tooling.
- Consume and interpret existing bindex metadata during build analysis.
- Assemble metadata from multiple modules and dependency sets.

For related tooling and documentation, see <https://github.com/machanism-org/machai>.

## License

Apache License, Version 2.0.

## Contact

- Viktor Tovstyi (<viktor.tovstyi@gmail.com>)
