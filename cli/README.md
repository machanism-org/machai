# CLI Multi-Module Java Project

## Project Overview

The CLI project is a multi-module Java repository designed for building flexible command-line tooling platforms. It integrates core and extendable commands supporting project assembly, library selection, GenAI-based document processing guidance, and bindex file operations. This solution lays the foundation for developer productivity and automation, powered by machanism.org.

## Installation Instructions

### Prerequisites
- Java 17 or higher
- Maven (recommended) or Gradle
- Git

### Clone and Build
```sh
git clone https://github.com/your-org/cli.git
cd cli
mvn clean install # or use your preferred build tool
```

## Usage

### Assembly Command
- `pick`: Picks libraries based on user request.
- `assembly`: Creates a project via picked library set.
- `prompt`: Is used for request additional GenAI guidances.

### Bindex Command
- `bindex`: Generates bindex files.
- `register`: Registers bindex file.

### Built-In Commands
- `help`: Display help about available commands
- `stacktrace`: Display the full stacktrace of the last error.
- `clear`: Clear the shell screen.
- `quit`, `exit`: Exit the shell.
- `history`: Display or save the history of previously run commands
- `version`: Show version info
- `script`: Read and execute commands from a file.

### Docs Command
- `docs`: GenAI document processing command.

## Reference

For the parent project and more details, visit [machanism.org](https://machanism.org)
