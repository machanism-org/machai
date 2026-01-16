# Bindex Core
<!-- @guidance: 
Analyze the source file and create a Maven Site-style introductory home page for your project.
Do not use the horizontal rule separator between sections. -->

Welcome to **Bindex Core**, the foundational library for producing and consuming **bindex** metadata.

## Overview

Bindex Core provides core APIs and reference implementations to:

- Generate bindex metadata for Java artifacts
- Read, merge, and analyze bindex metadata produced by other tools
- Integrate with Maven-oriented models and plugins

## What is bindex metadata?

**bindex** metadata describes Java artifacts in a structured, tool-friendly form so build tools can produce it and other tools can consume it.

Common use cases include:

- Producing per-artifact or per-module metadata files
- Aggregating metadata across multi-module builds
- Merging metadata across dependency graphs for analysis

## Features

- Metadata generation for Java projects
- Aggregation across modules and dependencies
- Extensible components suitable for Maven models and plugin APIs

## Getting Started

Bindex Core is available on Maven Central.

Add it to your `pom.xml`:

```xml
<dependency>
  <groupId>org.machanism-org.machai</groupId>
  <artifactId>bindex-core</artifactId>
  <version>${bindex-core.version}</version>
</dependency>
```

## Typical Usage

You can use Bindex Core to:

- Produce bindex metadata as part of build tooling
- Consume and interpret existing bindex metadata during build analysis
- Assemble and merge metadata from multiple modules and dependency sets

For related tooling and documentation, see <https://github.com/machanism-org/machai>.

## License

Apache License, Version 2.0.

## Contact

- Viktor Tovstyi (<viktor.tovstyi@gmail.com>)
