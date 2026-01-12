![](src/site/resources/images/machai-logo.png)

# Machai

Machai is a modular, AI-driven toolkit for automating software project setup, semantic indexing, build orchestration, and lifecycle management. Powered by GenAI agents (Ghostwriter, semantic search, metadata indexing, and automated code/documentation generation), Machai streamlines project integration and assembly. While it is designed to work seamlessly within the Machanism ecosystem, Machai can also be used independently for a wide range of software automation tasks.

## Modules

- [genai-client](/genai-client): Provides remote GenAI endpoint interfaces for contextual automations and AI integrations.
- [bindex-core](/bindex-core): Supplies core utilities for semantic indexing, metadata management, and project data operations.
- [cli](/cli): CLI entrypoint for interactive project initialization and Machai task execution.
- [bindex-maven-plugin](/bindex-maven-plugin): Maven plugin for project semantic indexing and metadata enrichment.
- [assembly-maven-plugin](/assembly-maven-plugin): Maven plugin for artifact assembly, packaging, and release handling.
- [ghostwriter](/ghostwriter): Integrates with GenAI for code and documentation generation automation.
- [gw-maven-plugin](/gw-maven-plugin): Maven extension for incorporating Ghostwriter outputs into the build lifecycle.

## Installation Instructions

To clone and build Machai:

```bash
# Prerequisites:
# - Java 9 or newer
# - Maven 3.6 or newer

git clone https://github.com/machanism-org/machai.git
cd machai
mvn clean install
```

## Usage

To run the CLI:

```bash
cd cli
mvn exec:java
```

To use Machai Maven plugins, add to your `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</plugin>
```

Module directories provide individual usage documentation and examples.

## Contributing

- Follow code style and commit conventions adopted in the repository.
- Reference issues in all pull requests when relevant.
- Submit bugs or feature requests via [GitHub Issues](https://github.com/machanism-org/machai/issues).
- Ensure all contributions are documented and comprehensively tested.

## License

Machai is distributed under the [Apache License, Version 2.0](LICENSE.txt).

## Contact and Support

- Homepage: [machanism.org/machai](https://machanism.org/machai)
- Issues: [GitHub Issues](https://github.com/machanism-org/machai/issues)
- Maintainer: Viktor Tovstyi (<viktor.tovstyi@gmail.com>)
