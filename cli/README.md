# CLI Project
<!--
@guidance:
**IMPORTENT*: REGENERATE THE CONTENT OF THIS FILE

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

# CLI Project

A multi-module Java CLI application for advanced document processing and intelligent library management. Main features include:
- Assembly of project structures from selected libraries
- Bindex operations for modularization
- Built-in shell and utility commands
- GenAI-powered document processing utilities

## Installation Instructions

**Clone and Build:**
~~~bash
# Clone the repository
$ git clone https://github.com/example/cli-project.git
$ cd cli-project

# Build using Maven
$ mvn clean install
~~~

**Prerequisites:**
- Java 17 or higher
- Maven 3.8+
- Git

**Machai CLI Application Jar:**
[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/machai.jar/download)

## Usage

### Assembly Command
- `pick`: Picks libraries based on user request
- `assembly`: Creates a project via picked library set
- `prompt`: Is used for request additional GenAI guidances

### Bindex Command
- `bindex`: Generates bindex files
- `register`: Registers bindex file

### Docs Command
- `docs`: GenAI document processing command

### Built-In Commands
- `help`: Display help about available commands
- `stacktrace`: Display the full stacktrace of the last error
- `clear`: Clear the shell screen
- `quit`, `exit`: Exit the shell
- `history`: Display or save the history of previously run commands
- `version`: Show version info
- `script`: Read and execute commands from a file

## Reference

This project is developed as part of the parent project of [machanism.org](https://machanism.org).
