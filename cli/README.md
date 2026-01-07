# CLI Project

## Overview

The CLI project is a multi-module Java application designed for flexible document and library management using various commands. It supports GenAI guidance integration, library picking and assembly, bindex registration, and other project utilities to enhance development workflows.

## Installation Instructions

1. **Prerequisites:**
   - Java 17 or newer
   - Maven (recommended)
   - Git

2. **Clone the Repository:**
```shell
$ git clone https://github.com/your-org/cli.git
```
3. **Build the Project:**
```shell
$ cd cli
$ mvn clean install
```

## Usage

Example commands available within the CLI shell:

**Assembly Command**
- `pick`: Picks libraries based on user request.
- `assembly`: Creates a project via picked library set.
- `prompt`: Is used for request additional GenAI guidances.

**Bindex Command**
- `bindex`: Generates bindex files.
- `register`: Registers bindex file.

**Built-In Commands**
- `help`: Display help about available commands
- `stacktrace`: Display the full stacktrace of the last error.
- `clear`: Clear the shell screen.
- `quit`, `exit`: Exit the shell.
- `history`: Display or save the history of previously run commands
- `version`: Show version info
- `script`: Read and execute commands from a file.

**Docs Command**
- `docs`: GenAI document processing command.

## Reference

This project is part of the parent mechanism and is maintained by [machanism.org](https://machanism.org).
