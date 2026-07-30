---
<!-- @guidance: 
Create a user manual web page titled "Guidance Tag" that explains the guidance tag processing feature.
**Instructions:**
1. **Purpose and Overview**
   - Begin with a clear, user-friendly introduction to what the guidance tag is and its role in the Machai system.
   - Summarize the main purpose and benefits of guidance tag processing, focusing on how it helps users automate and enhance file processing.
2. **How It Works**
   - Review the `src/main/java/org/machanism/machai/gw/processor/GuidanceProcessor.java` and classes in folder: `src/main/java/org/machanism/machai/gw/reviewer`.
   - Describe the supported features from a user perspective using simple, accessible language.
   - Explain the key features of the GuidanceProcessor, avoiding technical jargon.
   - Highlight how users can take advantage of these features in their own projects.
   - Use information from the web page https://machanism.org/guided-file-processing/index.html (css selector: `.md-content`) to describe how it work.
3. **Practical Usage**
   - Provide step-by-step instructions or real-world scenarios showing how to use the guidance tag feature.
   - Include practical examples, such as how to add a guidance tag to a file and what happens during processing.
   - Use bullet points or numbered lists to make instructions easy to follow.
4. **Why Use Guidance Tags?**
   - Clearly explain the advantages of using guidance tags, such as saving time, reducing manual work, and ensuring consistency in file processing.
   - Mention how this feature can benefit both technical and non-technical users.
5. **Further Resources**
   - Include a link to [Guided File Processing](https://machanism.org/guided-file-processing/index.html) for users who want to learn more.
6. **Accessibility and Readability**
   - Organize the page with clear headings and concise paragraphs.
   - Use bullet points, lists, and examples to improve readability.
   - Ensure the content is approachable for users of all backgrounds, including those new to the codebase or without technical experience.
**Important:**  
- Do not assume prior knowledge of the Machai project or its codebase.
- Focus on clarity, simplicity, and practical value for end users.
-->
canonical: https://machai.machanism.org/ghostwriter/guidance-tag.html
title: Guidance Tag
---

# Guidance Tag

A guidance tag is a plain-language instruction that you place in a file to tell Machai Ghostwriter what you want done with that file. Instead of repeatedly describing the same task in a chat window, you keep the instruction close to the content, code, or configuration that needs attention.

In the Machai system, guidance tags are part of **Guided File Processing**. This means Ghostwriter scans selected files, reads your instructions, and uses them to help update documentation, source comments, tests, project metadata, or other supported files. The guidance itself does not change how your application runs; it is used only during processing.

## Purpose and overview

Guidance tag processing helps users automate file work while staying in control. You describe the desired result in normal language, and Ghostwriter uses that instruction as the starting point for processing.

You can use guidance tags to ask Ghostwriter to:

- improve a document for a specific audience,
- add or update code comments,
- generate or refine tests,
- keep README files and project documentation consistent,
- extract useful information from source or test code,
- preserve important sections while updating the rest of a file,
- or apply a shared writing, documentation, or review style.

The main benefits are:

- **Less repeated prompting:** write instructions once and reuse them.
- **More consistent output:** keep rules with the files that need them.
- **Better traceability:** review guidance changes in version control.
- **Accessible automation:** use plain language instead of a complex scripting language.
- **Human control:** Ghostwriter acts on explicit instructions that users can read, edit, and review.

Guided File Processing treats prompts as a maintainable project asset. Like source code or documentation, guidance can be stored in the project, reviewed by teammates, improved over time, and committed to the repository.

## How it works

Guided File Processing begins with the project area you choose. Ghostwriter scans that area, finds files it can process, reads any guidance instructions, and prepares a request for the configured AI provider.

From a user perspective, the workflow is:

1. You add an `@guidance:` instruction to a file, or provide folder-level guidance.
2. You run Ghostwriter for a selected project, folder, or scanning path.
3. Ghostwriter finds supported files inside that scope.
4. For each file, Ghostwriter uses a reviewer that understands the file type and its comment style.
5. If a file contains its own guidance tag, that instruction is used for that file.
6. If no file-level guidance exists, folder guidance or optional default guidance may be used.
7. Ghostwriter combines your guidance with its processing rules and sends the request to the AI provider.
8. You review the updated files, adjust the guidance if needed, and run the process again.

This process is intentionally guidance-driven. Ghostwriter does not decide tasks on its own; it works from instructions that you provide.

## Key features

### Root directory

The root directory is the base folder where Ghostwriter begins understanding your project. It may be:

- the root of a single project,
- a parent folder that contains several projects,
- or a monorepo with multiple modules.

Choosing the correct root directory helps Ghostwriter find source code, tests, documentation, configuration files, and submodules.

### Scanning path

A scanning path lets you focus processing on a smaller part of the root directory. This is useful when you only want to process one module, one documentation folder, or one source package.

Use a scanning path to:

- speed up processing in large repositories,
- avoid changing unrelated files,
- run targeted documentation or review tasks,
- or process only the files that are part of a current task.

The scanning path must be inside the configured root directory.

### Multi-module processing

In multi-module projects, Ghostwriter can process deeper child modules before parent-level files. This helps project-level documentation or summaries use information from the most specific modules first.

### File-type-aware reviewers

Ghostwriter uses reviewer classes that understand how different file types express comments and guidance. Supported reviewer types include:

- HTML-style files,
- Java files,
- Markdown files,
- PlantUML files,
- Python files,
- plain text files,
- and TypeScript files.

This allows you to write guidance in a style that feels natural for the file you are editing.

### File-level guidance

File-level guidance is placed directly in the file that should be processed. It is the most specific instruction and takes priority for that file.

Example for Markdown, HTML, or XML-style files:

```markdown
<!-- @guidance:
Rewrite this page for first-time users.
Add one practical example.
Keep the language simple and friendly.
-->
```

### Folder-level guidance

A folder can include an `@guidance.txt` file. This file contains instructions for that folder and is useful when many files should follow the same rules.

Folder guidance is helpful for tasks such as:

- creating tests for a package,
- applying a shared documentation style,
- defining review rules for a source folder,
- or keeping examples consistent across related files.

### Java package guidance

Java packages can use `package-info.java` for package-level guidance. This is useful when all classes in a package should follow the same documentation, Javadoc, or review requirements.

### Default guidance

The optional `defaultGuidance` setting provides fallback instructions. If a file or folder is in scope but does not contain its own guidance, Ghostwriter can still process it using the default guidance.

When a file contains an embedded `@guidance:` instruction, that file-specific instruction is used for that file.

### Processing reports

During processing, Ghostwriter records which files were handled and the result message for each one. These reports help you review what happened and decide whether guidance should be refined.

## Practical usage

### Add guidance to a Markdown page

1. Open the Markdown file you want to improve.
2. Add a guidance comment near the top of the file.
3. Describe the desired result in clear, simple language.
4. Run Ghostwriter using your normal Maven plugin, CLI, or application workflow.
5. Review the updated file.
6. If the result is not quite right, improve the guidance and run the process again.

Example:

```markdown
<!-- @guidance:
Update this README for new users.
Include installation, a short usage example, and support information.
Do not remove existing license details.
-->
```

### Add guidance to a Java or TypeScript file

For Java and TypeScript files, place the guidance in a multiline comment block. Do not place it inside Javadoc or TSDoc comments.

```java
/* @guidance:
 * Add clear documentation for public methods.
 * Include one simple usage example where helpful.
 * Do not change method names or behavior.
 */
```

TypeScript example:

```typescript
/*
 * @guidance:
 * - Document all exported classes, interfaces, functions, and constants using TSDoc.
 * - Provide clear descriptions and parameter details.
 * - Do not change runtime behavior.
 */
```

### Add guidance to a Python file

For Python files, use a multiline comment near the top of the file or near the section being processed.

```python
'''
@guidance:
- Follow PEP 257 for docstrings.
- Document public classes and functions.
- Keep explanations concise.
'''
```

### Use folder guidance

Create a file named `@guidance.txt` in the folder you want to guide.

Example content:

```text
Create high-quality unit tests in this folder.
Use descriptive test names.
Cover edge cases and error handling.
Follow the Java version configured by the project.
```

This approach is useful when an entire folder should share the same processing instructions.

## Real-world scenarios

### Improve documentation for new users

A documentation writer can add guidance that asks Ghostwriter to:

- explain the feature in beginner-friendly language,
- avoid internal jargon,
- include a short example,
- and organize content with clear headings.

### Keep project documentation in sync

A team can use guidance tags in source files, tests, documentation pages, and README files so that project descriptions are based on current code and examples.

### Generate or update tests

A development team can place an `@guidance.txt` file in a test folder to describe the expected test style, coverage goals, naming rules, and mocking approach.

### Standardize repeated updates

If a file is updated often, guidance can preserve the expected structure, tone, and required sections so future processing runs remain consistent.

## What happens during processing

When Ghostwriter processes a guided file, it typically:

- checks whether the file is inside the selected processing scope,
- chooses a reviewer based on the file type,
- reads the `@guidance:` instruction when one is present,
- applies folder-level or default guidance when appropriate,
- combines the guidance with standard processing instructions,
- sends the prepared request to the configured AI provider,
- records the result for review,
- and leaves you responsible for reviewing and accepting the final changes.

Review is an important part of the workflow. If the output needs improvement, update the guidance and process the file again.

## Why use guidance tags?

Guidance tags make AI-assisted file processing more predictable, repeatable, and transparent.

They help you:

- **Save time** by avoiding repeated manual prompts.
- **Reduce manual work** by automating common file updates.
- **Improve consistency** across documentation, comments, tests, and configuration files.
- **Keep instructions visible** because guidance lives inside the project.
- **Support collaboration** because teammates can review and improve the same instructions.
- **Help non-technical users participate** by using everyday language.
- **Help technical users enforce standards** for documentation, testing, formatting, and project metadata.

## Tips for writing effective guidance

Good guidance is clear, specific, and easy to review.

- Start with the main goal.
- Identify the intended audience.
- State anything that must not be changed.
- List required sections, examples, or checks.
- Use bullet points for multiple requirements.
- Avoid vague instructions such as "make it better" unless you explain what better means.
- Review the output and refine the guidance over time.

## Further resources

To learn more about the broader workflow, root directory, scanning path, default guidance, folder-level guidance, and related tools, visit [Guided File Processing](https://machanism.org/guided-file-processing/index.html).
