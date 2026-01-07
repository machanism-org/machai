# CLI Multi-Module Java Project

## Overview

The CLI project provides a flexible, modular command-line interface for Java-based solutions. It features a set of commands for project assembly, library picking, GenAI guidance, bindex file generation and registration, and built-in utilities for enhanced developer productivity. Designed for extensibility, this repository serves as a foundation for tooling powered by machanism.org.

## Installation Instructions

### Prerequisites
- Java 17 or higher
- Maven (recommended) or Gradle
- Git

### Clone and Build
```sh
git clone https://github.com/your-org/cli.git
cd cli
mvn clean install  # or use your preferred build tool
```

## Usage

Example shell commands:

### Assembly Command
- `pick`: Picks libraries based on user request.
- `assembly`: Creates a project via picked library set.
- `prompt`: Is used for requesting additional GenAI guidances.

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

For more information and updates, see the parent project and visit [machanism.org](https://machanism.org).
