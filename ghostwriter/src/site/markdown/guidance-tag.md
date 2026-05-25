---
<!-- @guidance: 
Create a user manual web page titled "Guidance Tag" that explains the guidance tag processing feature.
**Instructions:**
1. **Purpose and Overview**
   - Begin with a clear, user-friendly introduction to what the guidance tag is and its role in the Machai system.
   - Summarize the main purpose and benefits of guidance tag processing, focusing on how it helps users automate and enhance file processing.
2. **How It Works**
   - Review the `src/main/java/org/machanism/machai/gw/processor/GuidanceProcessor.java` class and describe its functionality in simple, accessible language.
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
---

# Guidance Tag

A guidance tag is a built-in way to place clear instructions directly inside a file so Ghostwriter knows how that file should be processed. In Machai, this makes file processing more practical because the rules stay with the content instead of being stored in a separate note, ticket, or prompt history.

Guided file processing treats natural-language instructions as part of the project workflow. In simple terms, your written guidance becomes the "source" that Ghostwriter interprets during processing. This helps users automate updates, improve consistency, and keep documentation or code-related instructions easy to find and review.

Guidance tags are useful when you want a file to describe things like:

- who the content is for,
- what tone or style to use,
- what information must be preserved,
- what kind of update should be made,
- and what output format is expected.

For a broader introduction, see [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## Purpose and overview

The purpose of a guidance tag is to give Ghostwriter file-specific instructions during AI-assisted processing.

Instead of repeating the same request every time, you can save guidance in the file itself. This makes processing easier to repeat, easier to review in version control, and easier for teams to maintain over time.

In practice, guidance tags help Ghostwriter:

- find instructions written inside files,
- apply those instructions to the correct file,
- process supported file types in an appropriate way,
- use fallback guidance when a file has no embedded guidance,
- and keep updates aligned with the project's preferred workflow.

### Main benefits

- Keep instructions close to the content they control.
- Reduce repeated manual prompt writing.
- Make updates more consistent across runs.
- Support review, collaboration, and traceability.
- Help both technical and non-technical users describe what they want in plain language.

## How it works

The main class behind this feature is `GuidanceProcessor`. You do not need to understand the code to use it, but its behavior is straightforward when described in everyday language.

`GuidanceProcessor` scans the project, checks which files should be included, chooses a reviewer based on the file type, reads any guidance it finds, and then sends the prepared request into Ghostwriter's AI processing flow.

This fits the broader guided file processing model used by Machai:

- users place instructions in files or guidance files,
- Ghostwriter scans the selected project area,
- the system extracts those instructions,
- and processing happens only because the user provided explicit guidance.

This means guidance-driven processing stays under user control. Ghostwriter does not invent a workflow on its own. It follows the instructions it is given.

### Simple processing flow

1. Ghostwriter starts from the selected root and scan directory.
2. It scans the project structure for files and folders within scope.
3. In multi-module projects, sub-modules are handled before the parent project.
4. For each supported file, Ghostwriter chooses a reviewer that understands that file format.
5. The reviewer looks for a `@guidance:` annotation or other supported guidance location.
6. If guidance is found, Ghostwriter uses that file-specific guidance.
7. If guidance is not found, Ghostwriter can use configured default guidance as a fallback.
8. Ghostwriter adds standard processing instructions, including environment details such as the operating system.
9. The final request is sent to the configured AI provider.
10. The updated output can then be reviewed and refined if needed.

### What `GuidanceProcessor` does

`GuidanceProcessor` includes several user-facing capabilities.

#### Scans the project structure

It walks through the project directory and checks files and folders against the active scan settings. This helps limit processing to the area you want Ghostwriter to work on.

#### Supports multi-module projects

If your project has modules, child modules are processed before the parent directory. This matches the guided file processing approach described in the documentation, where deeper project areas are handled first.

#### Uses file-type aware reviewers

Different file types can store guidance in different ways. `GuidanceProcessor` uses reviewers registered for supported file extensions so each file can be read using rules that fit its format.

Examples include:

- Markdown files,
- Java files,
- HTML or XML files,
- Python files,
- TypeScript files,
- and other supported text-based formats.

#### Extracts file-specific guidance

When a file contains a `@guidance:` block, the reviewer extracts that text and returns it to Ghostwriter for processing.

#### Supports default guidance

If a file has no embedded guidance, Ghostwriter can still process it using a configured `defaultGuidance` value. This gives you a baseline instruction set for files or folders that do not yet have their own guidance.

#### Prepares the final request

Before sending work to the AI provider, Ghostwriter adds its normal system and file-processing instructions. This helps ensure the result follows the expected workflow for the current environment.

#### Cleans up temporary files

The processor also supports cleanup of temporary guided-processing input log files when they are no longer needed.

## How reviewers help

A reviewer is the part of Ghostwriter that knows how to read guidance from a specific file type.

For example:

- in a Markdown file, guidance is usually placed inside an HTML comment,
- in Java or TypeScript files, guidance is placed inside a multiline comment,
- and in Python files, guidance can be stored in a multi-line comment block.

Because of this, users can place guidance in a format that fits naturally with the file they are editing.

## Practical usage

You can use guidance tags in documentation, source files, tests, and other supported project files.

### Step-by-step example

#### 1. Choose the file you want to guide

Example file:

`src/site/markdown/guidance.md`

#### 2. Add a guidance block to the file

Example:

```markdown
<!-- @guidance:
Rewrite this page for first-time users.
Add a short example.
Keep the language simple and clear.
-->
```

#### 3. Run Ghostwriter

Run Ghostwriter from the project root using your usual workflow.

During processing, Ghostwriter will:

- scan the selected project area,
- find the file,
- choose the correct reviewer,
- extract the guidance,
- fall back to default guidance if needed,
- add standard processing instructions,
- and send the request to the configured AI provider.

#### 4. Review the result

After processing, open the file and confirm that the update follows the guidance you provided.

If needed, refine the guidance and run the process again. This review-and-iteration approach is part of the normal guided file processing workflow.

### Real-world scenarios

#### Writing documentation for beginners

A documentation writer might add guidance such as:

- use beginner-friendly language,
- explain unfamiliar terms,
- include one short example,
- and keep paragraphs short.

#### Preserving important technical detail

A technical team might add guidance such as:

- keep required configuration details,
- do not remove command examples,
- keep headings consistent,
- and organize options in bullet lists.

#### Using folder-level guidance

In addition to file-level annotations, projects can also use `@guidance.txt` files at the folder level. These files provide mandatory instructions for the corresponding folder and help apply consistent direction across multiple files.

#### Repeating a common update style

If a file is updated often, the guidance block can preserve the preferred style and structure so later runs stay consistent.

## What happens during processing

When Ghostwriter encounters a supported file:

- the file is checked against the active scan rules,
- the correct reviewer is selected from the file extension,
- the `@guidance:` content is extracted when present,
- fallback guidance may be used when no file-level guidance exists,
- processing instructions are prepared,
- and the request is sent to the configured AI provider.

After that, users review the result, adjust the guidance if needed, and rerun processing until the output matches their goals.

## Where guidance can be added

Guidance can be stored in several practical locations depending on the file type and scope.

### File-level guidance

Common examples include:

- Markdown, HTML, and XML files using comment blocks,
- Java and TypeScript files using multiline comments,
- Python files using multi-line comment blocks.

### Folder-level guidance

You can also place an `@guidance.txt` file in a project, source, or test folder to provide instructions for that whole area.

### Package-level guidance for Java

For Java packages, guidance can be placed in `package-info.java` to describe package-wide requirements such as documentation expectations.

## Why use guidance tags?

Guidance tags make AI-assisted updates more controlled, reusable, and transparent.

### Advantages

- **Save time** by reusing instructions stored in the file or folder.
- **Reduce manual work** by avoiding repeated prompt writing.
- **Improve consistency** across generated or updated content.
- **Support traceability** because instructions can be versioned with the project.
- **Keep users in control** because processing happens only when explicit guidance is provided.
- **Help standardize project maintenance** across documentation, code comments, tests, and configuration-related content.

### Benefits for different users

- **Technical users** can define structure, constraints, and must-keep details close to the source.
- **Non-technical users** can describe audience, tone, and expected outcomes in plain language.

Because the instructions stay with the file or folder, the process remains understandable even for people who are new to the codebase.

## Tips for writing effective guidance

To get better results, keep the guidance clear, specific, and practical.

- State the main goal first.
- Mention anything that must not be removed or changed.
- Describe the intended audience.
- Explain the tone you want, such as simple, formal, or technical.
- Use short bullet points when possible.
- Keep the instructions easy to review and update over time.

## Further resources

To learn more about the broader workflow behind this feature, visit [Guided File Processing](https://machanism.org/guided-file-processing/index.html).
