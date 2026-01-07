# CLI Project

## Overview

The CLI project is a multi-module Java application designed for flexible document and library management using various commands. It supports GenAI guidance integration, library picking and assembly, bindex registration, and other project utilities to enhance development workflows.

## Installation Instructions

### Download
1. **Prerequisites:**
   - Java 17 or newer
2. **Download the CLI Application**
   You can download the CLI application as a `.jar` file from the following link:  
   [Download machai.jar from SourceForge](https://sourceforge.net/projects/machanism/files/machai.jar/download)	
2. **Run the CLI Tool**:
   ```shell
   $ java -jar machai.jar
   ```
   This command will show you a list of available CLI commands.

3. **Assemble an Application Using CLI**:
   Provide your project's requirements as a query:
   ```text
   shell:> assembly "Create a spring application for user login by commercetool."
   ```

### Clone the Preject
1. **Prerequisites:**
   - Java 17 or newer
   - Maven (recommended)
   - Git

2. **Clone the Repository:**
```shell
$ git clone https://github.com/machanism-org/machai.git
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
