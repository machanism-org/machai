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

A guidance tag is a plain-language instruction placed inside a project file so Machai Ghostwriter knows what you want done with that file. It lets you describe the desired result directly where the work happens, instead of keeping instructions in a separate chat, note, or issue.

In Machai, guidance tags are part of guided file processing. This approach treats human instructions as a maintainable project asset: they can be reviewed, updated, versioned, and reused just like documentation or source code. The goal is simple: help users automate file updates while keeping them in control.

Use guidance tags to describe things such as:

- the audience for a document,
- the tone or style to use,
- sections that must be added or preserved,
- coding or documentation rules to follow,
- and repetitive project maintenance tasks that should be handled consistently.

## Purpose and overview

Guidance tag processing helps Machai Ghostwriter understand what each file needs. A tag can tell Ghostwriter to improve documentation, add examples, update comments, generate tests, summarize code behavior, or follow a team-specific writing style.

The main benefits are:

- **Less repeated prompting:** write instructions once and keep them with the file.
- **More consistent results:** reuse the same guidance across future processing runs.
- **Better traceability:** review guidance changes in your normal version-control workflow.
- **Accessible automation:** describe work in everyday language, even without deep technical knowledge.
- **User control:** Ghostwriter acts on explicit guidance instead of guessing what should happen.

Guidance annotations do not change how your application runs. They are comments or guidance files used by Ghostwriter during processing.

## How it works

Guided file processing starts with the project area you choose. Ghostwriter scans that area, finds supported files, reads guidance instructions, and sends the prepared request to the configured AI provider.

From a user perspective, the process is:

1. You add a `@guidance:` instruction to a supported file, or provide guidance for a folder.
2. Ghostwriter scans the selected root directory or scanning path.
3. It finds files that are in scope for processing.
4. For each supported file type, it uses a reviewer that understands that file's comment style.
5. If a file has a guidance tag, that file-specific guidance is used.
6. If no file-level guidance is present, an optional `defaultGuidance` value can be used as fallback guidance.
7. Ghostwriter adds its standard processing instructions and sends the request to the AI provider.
8. You review the updated output, adjust the guidance if needed, and run the process again.

This follows the guided file processing model: every action begins with user-provided guidance, and users review and refine the result.

## Key features

### Root directory and scanning path

Ghostwriter starts from a root directory. This can be a single project, a parent folder that contains several projects, or a larger workspace.

You can also use a scanning path to focus processing on a smaller part of the project, such as:

- a documentation folder,
- a source folder,
- a test folder,
- or one module in a larger repository.

This helps large projects run faster and prevents unrelated files from being included.

### Multi-module processing

For projects with multiple modules, Ghostwriter can process child modules before parent files. This is useful when project-level documentation depends on information from deeper modules.

### File-type-aware reviewers

The `GuidanceProcessor` uses reviewer classes for different file types. These reviewers know how guidance is written in each format.

Supported reviewer types in the project include:

- HTML-style files,
- Java files,
- Markdown files,
- PlantUML files,
- Python files,
- plain text files,
- and TypeScript files.

This means you can write guidance in the natural comment format for the file you are editing.

### File-level guidance

File-level guidance is placed inside the file that should be processed. It is the most specific form of guidance and takes priority for that file.

For example, a Markdown file can include:

```markdown
<!-- @guidance:
Rewrite this page for first-time users.
Add one practical example.
Keep the language simple and friendly.
-->
```

### Folder-level guidance

A folder can contain an `@guidance.txt` file. This file provides instructions for work in that folder and is useful when many files should follow the same rules.

Example uses include:

- creating tests for a package,
- applying a shared documentation style,
- keeping examples consistent,
- or defining review rules for a specific area of the project.

### Java package guidance

Java packages can use `package-info.java` for package-level guidance. This is helpful when all classes in a package should follow the same documentation or review requirements.

### Default guidance

The optional `defaultGuidance` setting provides fallback instructions. If a file or folder is in scope but does not contain its own guidance tag, Ghostwriter can still process it using the default guidance.

If a file contains its own `@guidance:` instruction, that file-specific guidance is used instead.

### Processing reports and cleanup

During processing, Ghostwriter records which files were handled and the message returned for each one. Temporary processing logs can also be removed when they are no longer needed.

## Practical usage

### Add guidance to a Markdown file

1. Open the file you want Ghostwriter to process.
2. Add a guidance comment near the top of the file.
3. Write clear instructions in plain language.
4. Run Ghostwriter using your normal workflow.
5. Review the result and adjust the guidance if needed.

Example:

```markdown
<!-- @guidance:
Update this README for new users.
Include installation, a short usage example, and support information.
Do not remove existing license details.
-->
```

### Add guidance to a Java or TypeScript file

For Java and TypeScript files, place guidance in a multiline comment, not in Javadoc or TSDoc.

```java
/* @guidance:
 * Add clear documentation for public methods.
 * Include one simple usage example where helpful.
 * Do not change method names or behavior.
 */
```

### Add guidance to a Python file

For Python files, use a multiline comment block near the top of the file or near the section being processed.

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
Create high-quality unit tests for the classes in this package.
Use descriptive test names.
Cover edge cases and error handling.
Follow the Java version configured by the project.
```

This is useful when a whole folder should share the same processing instructions.

## Real-world scenarios

### Improve documentation for new users

A documentation writer can add guidance such as:

- explain the feature in beginner-friendly language,
- avoid project-specific jargon,
- include a short example,
- and keep paragraphs concise.

### Keep technical details accurate

A development team can add guidance such as:

- preserve configuration names,
- keep command examples up to date,
- do not remove required warnings,
- and organize options in tables or bullet lists.

### Generate or update tests

A team can place `@guidance.txt` in a test folder to describe expected test quality, naming style, coverage goals, and mocking rules.

### Standardize repeated updates

If a file is updated often, guidance can preserve the expected structure and tone so future runs follow the same pattern.

## What happens during processing

When Ghostwriter processes a guided file:

- it checks whether the file is inside the selected processing scope,
- it chooses a reviewer based on the file extension,
- it reads the `@guidance:` instruction when one is present,
- it uses `defaultGuidance` when configured and no file-level guidance exists,
- it combines the guidance with standard processing instructions,
- it sends the request to the configured AI provider,
- and it records the result for review.

After processing, you should always review the updated file. If the result is not what you wanted, improve the guidance and process the file again.

## Why use guidance tags?

Guidance tags make AI-assisted file processing more predictable, repeatable, and transparent.

They help you:

- **Save time** by reusing instructions across processing runs.
- **Reduce manual work** by avoiding repeated copy-and-paste prompts.
- **Improve consistency** across documentation, source comments, tests, and project files.
- **Keep work traceable** because guidance can be committed with the project.
- **Support collaboration** because teammates can read and improve the same instructions.
- **Help non-technical users participate** by allowing plain-language descriptions of desired outcomes.
- **Help technical users enforce rules** such as API documentation requirements, test structure, and formatting expectations.

## Tips for writing effective guidance

Good guidance is clear, specific, and easy to review.

- Start with the main goal.
- Say who the output is for.
- Mention anything that must stay unchanged.
- List required sections, examples, or checks.
- Use short bullet points for multiple requirements.
- Avoid vague instructions such as "make it better" unless you also explain what better means.
- Review the output and refine the guidance over time.

## Further resources

To learn more about the broader workflow, root directory, scanning path, default guidance, and folder-level guidance, visit [Guided File Processing](https://machanism.org/guided-file-processing/index.html).
