# Machai CLI
<!--
@guidance:
Content:

1. **Project Title and Overview:**  
   - Provide the project name and a brief description of its purpose and main features.

2. **Installation Instructions:**  
   - Describe how to clone the repository and build the project (e.g., using Maven or Gradle).
   - Include prerequisites such as Java version and build tools.
   - Add the Machai CLI application jar download link: [Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai.jar/download) to the installation section.

3. **Usage:**  
   - Provide example commands by help:
		Assemby Command
		       pick: Picks libraries based on user request.
		       assembly: Creates a project via picked librariy set.
		       prompt: Is used for request additional GenAI guidances.
		Bindex Command
		       bindex: Generates bindex files.
		       register: Registers bindex file.
		Built-In Commands
		       help: Display help about available commands
		       stacktrace: Display the full stacktrace of the last error.
		       clear: Clear the shell screen.
		       quit, exit: Exit the shell.
		       history: Display or save the history of previously run commands
		       version: Show version info
		       script: Read and execute commands from a file.
		Docs Command
		       docs: GenAI document processing command.

7. **Reference to the parent project and machanism.org**

**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

A Java CLI application for intelligent library management, GenAI-powered document processing, and automation.

## Overview

Machai CLI is a Java CLI with an interactive shell that helps you assemble projects, manage library/module metadata, and run GenAI-assisted document processing.

Key features:

- Modular, guided Java project assembly
- Bindex file generation and registration for module dependency management
- Built-in interactive shell for automation and scripting
- GenAI document processing utilities

## Installation

### Prerequisites

- Java 17 or newer
- Maven 3.8+
- Git

### Clone and build

```bash
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -pl cli -am clean install
```

### Download the Machai CLI application jar

[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai.jar/download)

## Usage

Example commands (as shown by `help`):

### Assembly commands

- `pick`: Picks libraries based on user request.
- `assembly`: Creates a project via picked library set.
- `prompt`: Is used for request additional GenAI guidances.

### Bindex commands

- `bindex`: Generates bindex files.
- `register`: Registers bindex file.

### Built-in commands

- `help`: Display help about available commands.
- `stacktrace`: Display the full stacktrace of the last error.
- `clear`: Clear the shell screen.
- `quit`, `exit`: Exit the shell.
- `history`: Display or save the history of previously run commands.
- `version`: Show version info.
- `script`: Read and execute commands from a file.

### Docs commands

- `docs`: GenAI document processing command.

## Reference

- Parent project: https://github.com/machanism-org/machai
- Machanism: https://machanism.org
