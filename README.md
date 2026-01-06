# Machai Project

Machai is a GenAI-powered tool for automated creation, registration, and enhancement of software projects. It streamlines project assembly and library integration within the Machanism ecosystem using AI-driven semantic search and metadata management.

## Project Modules

- [GenAI Client](genai-client)  
  _A Java library designed for seamless integration with Generative AI providers. Provides foundational prompt management and embedding capabilities for AI-powered features._

- [Bindex Core](bindex-core)  
  _Core engine for bindex metadata management, supporting generation, registration, library selection, and project assembly._

- [Machai CLI](cli)  
  _Command-line tool for generating, registering, and managing library metadata with GenAI-powered project assembly and semantic search._

- [Bindex Maven Plugin](bindex-maven-plugin)  
  _Automates the generation and registration of bindex metadata for Maven projects, enabling semantic library discovery and integration._

- [Assembly Maven Plugin](assembly-maven-plugin)  
  _Automates project assembly by integrating libraries based on bindex metadata and GenAI semantic search._

- [Ghostwriter](ghostwriter)  
  _Documentation engine that automatically scans, analyzes, and assembles project documentation using AI-powered synthesis and embedded guidance tags._

- [Ghostwriter Maven Plugin](ghostwriter-maven-plugin)  
  _Documentation automation plugin that scans, analyzes, and assembles project documentation. Ensures best practices and up-to-date content across all Java modules._

## Installation Instructions

**Prerequisites**  
- Java 9 or higher (Java 17+ recommended for CLI)
- Apache Maven 3.6.0+
- Git

**Clone the Repository**
```sh
git clone https://github.com/machanism-org/machai.git
cd machai
```

**Build the Project (All Modules)**
```sh
mvn clean install
```

**Generate Project Documentation**
```sh
mvn site
```

## Usage

Each module can be used independently or together as part of the Machanism ecosystem.

### Command-Line Interface
Run the Machai CLI (after building):
```sh
cd cli
java -jar target/machai.jar
```

For help:
```sh
>java -jar machai.jar
        _ . __  __            _           _
    \`"' ' |  \/  | __ _  ___| |__   __ _(_)
   /'`\\\  | |\/| |/ _` |/ __| '_ \ / _` | |
  /<"\ \\\ | |  | | (_| | (__| | | | (_| | |
 /::_.-.  .|_|  |_|\__,_|\___|_| |_|\__,_|_|
 `-\\ / | The Machai CLI 0.0.2-SNAPSHOT
    \ `-' www.machanism.org

Starting MachaiCLI using Java 24.0.2 with PID 33888 (C:\projects\machanism.org\machai\cli\target\machai.jar started by ViktorTovstyi in C:\projects\machanism.org\machai\cli\target)
No active profile set, falling back to 1 default profile: "default"

Unable to create a system terminal, creating a dumb terminal (enable debug logging for more information)
Started MachaiCLI in 2.159 seconds (process running for 3.276)
shell:> help
```

### Maven Plugins
- **Bindex Maven Plugin** and **Assembly Maven Plugin** can be added to your Maven project and invoked via Maven goals.
    - Example usage:
      ```sh
      mvn org.machanism.machai:assembly-maven-plugin:assembly
      mvn org.machanism.machai:bindex-maven-plugin:register
      ```

### Documentation Automation
- **Ghostwriter** and its plugin can automatically review and assemble project documentation.

## Contributing

- Fork this repository and create a branch for your feature or bugfix.
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
- Submit a clear pull request with a detailed description.
- Report issues or feature requests through [GitHub Issues](https://github.com/machanism-org/machai/issues).

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE.txt).

## Contact and Support

- Maintainer: Viktor Tovstyi ([viktor.tovstyi@gmail.com](mailto:viktor.tovstyi@gmail.com))
- GitHub: [https://github.com/machanism-org/machai](https://github.com/machanism-org/machai)
- Issues: [https://github.com/machanism-org/machai/issues](https://github.com/machanism-org/machai/issues)

