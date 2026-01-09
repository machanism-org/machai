# CLI Project

A multi-module Java CLI application for advanced document processing and library management, featuring assembly, bindex operations, built-in shell commands, and GenAI-powered documentation utilities.

## Installation Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Git

### Clone and Build
```sh
git clone <repository-url>
cd cli
mvn clean install
```

## Usage

### Assembly Command
- `pick`: Picks libraries based on user request.
- `assembly`: Creates a project via picked library set.
- `prompt`: Is used for additional GenAI guidances.

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

This project is developed as part of the parent project and the [machanism.org](https://machanism.org) initiative.
