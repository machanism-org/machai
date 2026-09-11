# README File Content Generation Rules

**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.

### 0. Root Folder Detection & Standard Clone Guide *(Conditional Rule)*
- **Condition:** Check for the presence of the `.git` folder or file. If it is not the root folder, skip this section.
- **Requirement:** Include a standardized **"Cloning and Getting Started"** section immediately following the Project Title and Badges overview.
- **Standard Clone Guide Template:**
  ```markdown
  ## Cloning and Getting Started

  To clone and set up this project locally, follow these steps:

  1. **Clone the repository:**
     ```bash
     git clone https://github.com/machanism-org/[artifactId].git
     cd [artifactId]
     ```
  2. **Build the project using Maven:**
     ```bash
     mvn clean install
     ```
  ```
  *(Replace `[artifactId]` dynamically with the actual artifact ID parsed from the root `pom.xml`).*

### 1. Project Title and Overview
- Please provide a project title and a short description based on the contents of the `src/site/markdown/index.md` file (if present) or the root `pom.xml` file.
- Add the following badges in **one line** after the title as a new paragraph:
  ```markdown
  [![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId]) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/[artifactId]/refs/heads/main/bindex.json)
  ```

### 2. Module List
- **Condition:** This section should be skipped if the project is not parent.
- Generate a table listing all modules in the project with the following columns:
  - **Name**: Display the module name as a clickable link in the format `[name]([artifactId]/)`. Obtain `[name]` and `[artifactId]` from each module's `pom.xml` file.
  - **Description**: Try to read the `[module_dir]/src/site/markdown/index.md` file and provide a comprehensive description for each module or use `pom.xml` file for it.
- If a module list already exists, update it to reflect any new, removed, or changed modules and descriptions.

### 3. Project Structure
- Try to read the `src/site/puml/c4-diagram.puml` file and use it to describe the project structure.
- If `c4-diagram.png` file not found in `src/site/resources/images` then don't use it on th epage content.
- Do not include file names in the description.
- Include this image in the section to visually represent the project structure.
- If a project structure section already exists, update it with the latest diagram and description.

### 4. Introduction
- Extract and adapt content from the documentation file: `src/site/markdown/index.md` (if present).

### 5. Usage
- Extract and adapt usage guidelines from the documentation file: `src/site/markdown/index.md`.

### **Formatting Requirements:**
- Use clean Markdown syntax for headings, lists, tables, code blocks, and links.
- Ensure clarity, conciseness, and consistent tone across all sections.
- Organize the README for optimal navigation and readability.