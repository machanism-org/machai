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

A guidance tag is a simple way to place instructions directly inside a file so Machai Ghostwriter knows how that file should be processed. Instead of keeping processing notes in a separate chat, document, or ticket, you keep the instructions close to the content they control.

In Machai, guided file processing treats natural-language instructions as part of the project workflow. In other words, your written guidance acts like task instructions that Ghostwriter can interpret during processing. This helps users automate repetitive work, improve consistency, and keep project knowledge easy to review in version control.

Guidance tags are especially helpful when you want to describe:

- who the content is for,
- what must be added, updated, or preserved,
- what tone or style should be used,
- what format the result should follow,
- and what rules must be respected during processing.

For a broader introduction, see [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## Purpose and overview

The purpose of a guidance tag is to give Ghostwriter clear, file-specific instructions during AI-assisted processing.

Instead of rewriting the same prompt every time, you can store the instructions in the file itself. This makes updates easier to repeat, easier to review, and easier to maintain across a team.

Guided file processing is designed to help users stay in control. Processing does not happen because the system guesses what you want. It happens because you provide explicit guidance in a file or in a supported guidance file such as `@guidance.txt`.

### Main benefits

Guidance tags help you:

- keep instructions close to the files they affect,
- reduce repeated manual prompt writing,
- make updates more consistent across runs,
- support review and traceability through version control,
- and describe work in plain language, even if you are not a developer.

## How it works

The main class behind this feature is `GuidanceProcessor`. You do not need to understand the Java code to use it, but its role is easy to describe in everyday language.

`GuidanceProcessor` scans the selected part of the project, checks which files are in scope, chooses the right file reader for each supported file type, extracts any `@guidance:` instructions it finds, and sends the prepared request into Ghostwriter’s AI processing flow.

This matches the guided file processing model described in the public documentation:

- users embed instructions in files or guidance files,
- Ghostwriter scans the chosen project area,
- the system extracts those instructions,
- and processing happens only because the user supplied guidance.

### Simple processing flow

1. Ghostwriter starts from the configured root directory.
2. It uses the selected scanning path, if one is set, to limit where processing begins.
3. It scans files and folders within that scope.
4. In multi-module projects, child modules are processed before the parent project.
5. For each supported file, Ghostwriter chooses a reviewer based on the file extension.
6. The reviewer looks for a `@guidance:` annotation in the correct comment style for that file type.
7. If file-level guidance is found, Ghostwriter uses it for that file.
8. If no file-level guidance is found, Ghostwriter can use `defaultGuidance` as a fallback.
9. Ghostwriter adds its standard processing instructions and environment details such as the operating system.
10. The final request is sent to the configured AI provider, and the result can then be reviewed.

### What `GuidanceProcessor` does for users

#### Scans the right area

`GuidanceProcessor` works from a root directory and can also use a scanning directory or pattern. This lets you process a whole project or focus on a smaller area, such as a documentation folder, a single module, or a source folder.

#### Handles multi-module projects carefully

If your project contains multiple modules, the processor handles deeper modules before the parent project. This supports a bottom-up workflow and matches the guided file processing approach described in the documentation.

#### Uses file-type-aware reviewers

Different file types store guidance in different ways. `GuidanceProcessor` loads reviewers that understand supported file formats and uses them to read guidance correctly.

Examples include:

- Markdown files,
- Java files,
- HTML and XML files,
- Python files,
- TypeScript files,
- and other supported text-based formats.

#### Extracts file-specific guidance

When a supported file contains a `@guidance:` block, the reviewer reads that guidance and returns it for processing. This allows each file to describe its own processing rules.

#### Supports fallback guidance

If a file does not contain embedded guidance, Ghostwriter can still process it by using a configured `defaultGuidance` value. According to the guided file processing documentation, this fallback can help ensure that files and folders within the selected scope are never left without direction.

#### Prepares the request sent to AI

Before the request is sent, Ghostwriter adds standard system instructions and processing instructions. `GuidanceProcessor` also includes environment details such as the operating system so the resulting request is complete and consistent.

#### Records processing results

The processor keeps a simple report showing which file was processed and what result message was returned. This makes it easier to review what happened during a run.

#### Supports temporary-file cleanup

After processing, the system can remove temporary guided-processing log files when they are no longer needed.

## How guidance is stored

Guidance can be stored in different places depending on the file type and the scope you want.

### File-level guidance

Common examples include:

- Markdown, HTML, and XML files using comment blocks,
- Java and TypeScript files using multiline comments,
- Python files using multi-line comment blocks.

For example, in a Markdown file the guidance is typically placed inside an HTML comment.

### Folder-level guidance

You can also place an `@guidance.txt` file in a project, source, or test folder. This provides instructions for the whole folder and helps maintain consistent processing across many files.

### Java package guidance

For Java packages, package-level guidance can be placed in `package-info.java`. This is useful for package-wide requirements such as documentation rules.

## Practical usage

You can use guidance tags in documentation, source files, test files, and other supported project files.

### Step-by-step example

#### 1. Choose the file you want to process

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

#### 3. Run Ghostwriter with your normal workflow

During processing, Ghostwriter will:

- scan the selected project area,
- find the file,
- choose the correct reviewer for the file type,
- extract the guidance,
- use fallback guidance if needed,
- prepare the final processing request,
- and send it to the configured AI provider.

#### 4. Review the updated result

After processing, open the file and confirm that the result follows the guidance you provided.

If needed, update the guidance and run the process again. The guided file processing documentation encourages this review-and-iteration approach so results can improve over time.

### Real-world scenarios

#### Writing beginner-friendly documentation

A documentation writer might add guidance such as:

- use beginner-friendly language,
- explain unfamiliar terms,
- include one short example,
- and keep paragraphs short.

#### Preserving important technical information

A technical team might add guidance such as:

- keep required configuration details,
- do not remove command examples,
- keep headings consistent,
- and organize options in bullet lists.

#### Applying rules to a whole folder

A team can place an `@guidance.txt` file in a test folder to describe how tests should be created or updated for everything in that folder.

#### Reusing a preferred update style

If a file is updated often, the guidance block can preserve the preferred structure, tone, and formatting so later runs stay consistent.

## What happens during processing

When Ghostwriter encounters a supported file in scope:

- the file is checked against the active scan rules,
- the correct reviewer is selected using the file extension,
- the `@guidance:` content is extracted when present,
- fallback guidance may be used when no file-level guidance exists,
- standard instructions are added,
- and the final request is sent to the configured AI provider.

After that, users review the result, adjust the guidance if needed, and rerun processing until the output matches their goals.

## Why use guidance tags?

Guidance tags make AI-assisted updates more controlled, reusable, and transparent.

### Advantages

- **Save time** by reusing instructions stored in the file or folder.
- **Reduce manual work** by avoiding repeated prompt writing.
- **Improve consistency** across generated or updated content.
- **Support traceability** because instructions can be versioned with the project.
- **Keep users in control** because processing happens only when explicit guidance is provided.
- **Help standardize maintenance** across documentation, code comments, tests, and project files.

### Benefits for different users

- **Technical users** can define structure, constraints, and must-keep details close to the source.
- **Non-technical users** can describe audience, tone, and expected results in plain language.

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
