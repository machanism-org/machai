# Machai Project

Machai is a GenAI-powered tool for automated creation, registration, and enhancement of software projects. It streamlines project assembly and library integration within the Machanism ecosystem using AI-driven semantic search and metadata management.

## Modules

| Module Name              | Description                                                      | Link                                         |
|-------------------------|------------------------------------------------------------------|----------------------------------------------|
| [genai-client](genai-client) | GenAI client library for external AI integration.                | [genai-client](./genai-client)               |
| [bindex-core](bindex-core)   | Core logic for project indexing, structure, and metadata.         | [bindex-core](./bindex-core)                 |
| [cli](cli)                   | Command Line Interface for Machai operations.                    | [cli](./cli)                                 |
| [bindex-maven-plugin](bindex-maven-plugin) | Maven plugin for project indexing and semantic metadata.         | [bindex-maven-plugin](./bindex-maven-plugin) |
| [assembly-maven-plugin](assembly-maven-plugin) | Maven plugin for advanced assembly tasks.                        | [assembly-maven-plugin](./assembly-maven-plugin) |
| [ghostwriter](ghostwriter)   | GenAI document processor and auto-reviewer for source artifacts.  | [ghostwriter](./ghostwriter)                 |
| [ghostwriter-maven-plugin](ghostwriter-maven-plugin) | Maven plugin for AI-powered documentation and review.           | [ghostwriter-maven-plugin](./ghostwriter-maven-plugin) |

## Installation Instructions

### Prerequisites
- Java 9 or higher
- Maven 3.6.0 or higher
- Git

### Cloning and Building

Clone the repository:
~~~shell
$ git clone https://github.com/machanism-org/machai.git
$ cd machai
~~~

Build with Maven:
~~~shell
$ mvn clean install
~~~

## Usage

Each module has its own usage instructions in its subdirectory README, but for general CLI operations:

~~~shell
# Run CLI (example)
$ cd cli
$ mvn spring-boot:run
~~~
For Maven plugin usage:
~~~xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>bindex-maven-plugin</artifactId>
</plugin>
~~~

## Contributing

We welcome contributions! Please follow these guidelines:
- Follow project code style conventions.
- Fork the repo and submit Pull Requests with clear descriptions.
- Use descriptive commit messages.
- Report issues via [GitHub Issues](https://github.com/machanism-org/machai/issues).

## License

This project is licensed under the [Apache License, Version 2.0](./LICENSE.txt).

## Contact and Support

Project lead: Viktor Tovstyi   
Email: viktor.tovstyi@gmail.com

- [Project homepage](https://machanism.org/machai)
- [GitHub repository](https://github.com/machanism-org/machai)
- For support and questions, use GitHub Issues or contact by email.