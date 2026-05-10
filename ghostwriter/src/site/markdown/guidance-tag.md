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

The guidance tag lets you place update instructions directly inside a file. When Ghostwriter processes that file, it reads those instructions and uses them to guide the result.

This feature is part of Machai's guided file processing approach. The main idea is simple: instead of keeping update notes in a separate document, you keep them next to the content they control. That makes file processing easier to repeat, easier to review, and easier for teams to understand.

Guidance tags are especially helpful when you want a file to describe:

- its audience,
- the tone to use,
- the kinds of changes allowed,
- and the details that must be preserved.

For a broader introduction, see [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## Purpose and overview

A guidance tag tells Ghostwriter how a specific file should be updated during AI-assisted processing.

In practical terms, it helps Ghostwriter:

- find instructions written inside the file,
- understand the kind of update you want,
- use a reviewer that matches the file type,
- and send the file for processing with the right context.

This gives users a reliable way to automate updates while still keeping control over the outcome.

### Main benefits

- Keep file-specific instructions close to the content.
- Reduce the need to rewrite the same prompt again and again.
- Make repeated updates more consistent.
- Help reviewers and teammates understand the intended result.
- Support both technical and non-technical editing workflows.

## How it works

The main class behind this feature is `GuidanceProcessor`. You do not need to read the source code to use it, but understanding its job can make the feature easier to use effectively.

At a high level, `GuidanceProcessor` scans the project, looks for supported files, asks the correct reviewer to inspect each file, and then sends the prepared request into Ghostwriter's AI processing flow.

### Simple processing flow

1. Ghostwriter scans the project folder.
2. It checks which files match the current scan settings.
3. It looks at each file type and selects a matching reviewer.
4. The reviewer reads the file and looks for a `@guidance:` block.
5. If file-specific guidance is found, Ghostwriter uses it.
6. If no file-specific guidance is found, Ghostwriter can fall back to a default prompt.
7. Ghostwriter adds its standard processing instructions.
8. The request is sent to the configured AI provider.

This matches the general guided file processing model: scan files, extract local instructions, prepare a structured request, and process each file with the right context.

### What `GuidanceProcessor` does

`GuidanceProcessor` includes several useful capabilities.

#### Scans the project structure

It walks through the project directory and checks files and folders against the active scan rules.

#### Supports multi-module projects

If the project contains modules, it processes child modules before the parent project directory. This helps keep module-specific content organized during processing.

#### Uses file-type aware reviewers

It does not handle every file in the same way. Instead, it uses reviewers registered for supported file extensions. That allows Markdown, Java, HTML, Python, text, TypeScript, and other supported file types to be read in a way that fits their format.

#### Extracts file-specific guidance

When a file contains a `@guidance:` block, the reviewer extracts that content and returns it for processing.

#### Supports default guidance

If a file does not contain its own guidance block, Ghostwriter can still process it by using a configured default prompt.

#### Prepares the final request

Before sending the request to the AI provider, Ghostwriter adds its normal processing instructions so the response fits the expected workflow.

#### Cleans up temporary files

The processor also supports removing temporary guided-processing input log files when cleanup is needed.

## How reviewers help

A reviewer is the part of the system that knows how to read guidance from a specific file type.

For example, in a Markdown file, guidance is commonly stored inside an HTML comment. A reviewer can recognize that pattern, extract the `@guidance:` block, and hand the relevant instructions back to Ghostwriter.

This makes guidance tags easy to use because you can place instructions in a format that already fits the file you are editing.

## Practical usage

You can use guidance tags for documentation pages, source files, text files, and other supported content.

### Step-by-step example

#### 1. Choose the file you want to guide

Example:

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

Run Ghostwriter from the project root using your normal workflow.

During processing, Ghostwriter will:

- scan the project,
- find the file,
- choose the correct reviewer,
- extract the guidance,
- add standard instructions,
- and send the request to the configured AI provider.

#### 4. Review the result

After processing, open the file and confirm that the update follows the guidance you provided.

If you leave the guidance block in the file, the same instructions can be reused in future runs.

## Real-world scenarios

### Writing documentation for beginners

A documentation writer can place instructions such as:

- use beginner-friendly language,
- explain unfamiliar terms,
- include one short example,
- and keep paragraphs short.

### Preserving technical detail

A technical team can add instructions such as:

- keep required configuration details,
- do not remove command examples,
- keep headings consistent,
- and organize options in bullet lists.

### Repeating a common update style

If a file is updated often, the guidance block helps keep the output consistent from one run to the next.

## What happens during processing

When Ghostwriter encounters a supported file with guidance:

- the file is matched by the active scan rules,
- the correct reviewer is selected from the file extension,
- the `@guidance:` content is extracted,
- file-processing instructions are prepared,
- and the request is sent to the configured AI provider.

If no guidance is present and a default prompt is configured, Ghostwriter can still process the file using that default guidance.

## Why use guidance tags?

Guidance tags make AI-assisted updates more controlled and more reusable.

### Advantages

- **Save time** by reusing instructions stored in the file.
- **Reduce manual work** by avoiding repeated prompt writing.
- **Improve consistency** across repeated updates.
- **Keep expectations visible** to editors, reviewers, and teammates.
- **Make automation easier** because the file carries its own guidance.

### Benefits for different users

- **Technical users** can define formatting, structure, and must-keep details near the source.
- **Non-technical users** can describe the desired audience, clarity, and tone in plain language.

Because the instructions stay with the file, the feature is useful even for people who are new to the codebase or unfamiliar with the underlying implementation.

## Tips for writing effective guidance

To get better results, keep the guidance focused and practical.

- State the main goal clearly.
- Mention anything that must stay unchanged.
- Describe the intended audience.
- Say what tone you want, such as simple, formal, or technical.
- Use short bullet points when possible.
- Keep the instructions specific enough to be useful, but simple enough to follow.

## Further resources

To learn more about the broader workflow behind this feature, visit [Guided File Processing](https://machanism.org/guided-file-processing/index.html).
