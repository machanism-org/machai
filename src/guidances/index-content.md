# Project Site Homepage File Content Generation Rules

**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.

### 1. Project Title and Overview
- Please provide a project title and a short description based on the contents of the `src/site/markdown/index.md` file (if present) or the root `pom.xml` file.
- if the artifact exists, add the badge in **one line** after the title:
  ```markdown
  [![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId]) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/[artifactId]/refs/heads/main/bindex.json)
  ```
- if the bindex.json exists, add the following badge in **one line**s:
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
- Try to read the `src/site/puml/c4-diagram.puml` file and use it to describe the project structure and use `./images/c4-diagram.png` path to show iname on the page.
- Do not include file names in the description.
- Include this image in the section to visually represent the project structure.
- If a project structure section already exists, update it with the latest diagram and description.

### 4. Introduction
- Extract and adapt content from the documentation file: `src/site/markdown/index.md` (if present).

### 5. Usage
- **Source Inspection:** Review the project's source code, main entry points (e.g., main classes, CLI interfaces, APIs), test cases, and any existing documentation or markdown files to determine how the project is used.
- **Content Requirements:** Provide comprehensive, step-by-step usage instructions tailored to the project type, including:
  - **Prerequisites / Setup:** Necessary dependencies, configurations, or environments.
  - **Code / CLI Examples:** Practical, clear code snippets or command-line examples showing how to run, integrate, or invoke the project.
- **Update Rule:** If a Usage section already exists, update and refine it with the latest and most accurate code snippets and instructions instead of duplicating or skipping it.

### **Formatting Requirements:**
- Use clean Markdown syntax for headings, lists, tables, code blocks, and links.
- Ensure clarity, conciseness, and consistent tone across all sections.
- Organize the README for optimal navigation and readability.