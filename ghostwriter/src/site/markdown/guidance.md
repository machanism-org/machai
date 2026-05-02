---
<!-- @guidance: 
Create the Act page as a Project Information page for the project:
- Analyze the `src/main/java/org/machanism/machai/gw/processor/GuidanceProcessor.java` class.
- Write a general description of the GuidanceProcessor feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- Summarize the purpose of the Act feature, its key methods, and how it fits into the overall project.
- Provide easy-to-follow, step-by-step instructions or a practical example showing how to use the guidance feature in real scenarios.
- Ensure the content is accessible and helpful for all users, including those new to the codebase or without a technical background.
— Add a link to [Guided File Processing](https://machanism.org/guided-file-processing/index.html).
-->
canonical: https://machai.machanism.org/ghostwriter/guidance.html
---

# Guidance

Guidance is a Ghostwriter feature that lets you add simple file-specific instructions directly inside supported project files by using the `@guidance:` tag. When Ghostwriter scans a project, `GuidanceProcessor` finds those instructions, asks the correct reviewer to read them, and prepares the request that is sent to the configured AI provider.

This makes updates easier to repeat because the instructions stay with the file they belong to. Instead of keeping a separate prompt somewhere else, the file can describe how it should be updated.

For a broader introduction to this workflow, see [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## What `GuidanceProcessor` does

`GuidanceProcessor` is the part of Ghostwriter that manages guided file processing.

In simple terms, it does the following:

1. scans the project folders,
2. checks which files or directories match the current scan rules,
3. picks a reviewer for the file type,
4. reads any `@guidance:` instructions from the file,
5. falls back to a default prompt when no file-specific guidance is found,
6. adds standard processing instructions,
7. and sends the work to the AI processing layer.

It supports both single-module and multi-module projects. In multi-module projects, child modules are processed before the parent project directory. This is a traversal-based process, so it walks the project structure but does not build the project or resolve dependencies.

## Purpose of the feature

The purpose of guidance is to let each file describe how Ghostwriter should handle it during AI-assisted processing.

This helps teams:

- keep instructions close to the content they affect,
- make future updates more consistent,
- reuse the same file-specific rules over time,
- and make the workflow easier to understand for both technical and non-technical users.

Guidance is especially useful for documentation and other files where style, structure, or audience needs to stay consistent.

## How guidance fits into Ghostwriter

Ghostwriter can process project files with AI support. Guidance is the feature that makes that processing more organized and repeatable.

Instead of writing one large external prompt for the whole project, you can place small instructions inside the exact file that needs attention. That means:

- the intent is visible where the content lives,
- other team members can quickly see what the file is supposed to do,
- updates can follow the same rules later,
- and supported file types can be handled in a consistent way.

## Key methods in `GuidanceProcessor`

### `loadReviewers()`

Loads reviewer implementations by using Java's `ServiceLoader`. Each reviewer declares the file extensions it supports.

### `normalizeExtensionKey(String extension)`

Converts file extensions into a consistent lower-case format so matching works even if the extension is written with or without a leading dot.

### `match(File file, File projectDir)`

Checks whether a file or directory should be processed. If no path matcher is configured, it can still allow processing based on default guidance behavior.

### `processModule(File projectDir, String module)`

Handles module directories in multi-module projects. When a scan directory is configured, it makes sure only relevant modules are processed.

### `processParentFiles(ProjectLayout projectLayout)`

Processes files and folders that belong directly to the main project directory while skipping module directories. It can also process the parent project directory itself when default guidance is available.

### `processFile(ProjectLayout projectLayout, File file)`

Performs the main file-level work. It checks whether the file matches the rules, extracts file-specific guidance through a reviewer, and uses the default prompt if needed.

### `process(ProjectLayout projectLayout, File file, String guidance)`

Prepares the final AI request. It adds standard system and document-processing instructions, includes the current operating system name, and then passes control to the parent AI file processor.

### `parseFile(File projectDir, File file)`

Looks at the file extension, finds the correct reviewer, and asks that reviewer to extract guidance from the file.

### `getReviewerForExtension(String extension)`

Returns the reviewer that is registered for a given file extension.

### `deleteTempFiles(File basedir)`

Removes the temporary input log directory created during guided processing.

## Reviewers and supported file types

`GuidanceProcessor` does not understand every file format by itself. It relies on reviewer implementations that know how to detect `@guidance:` instructions in different file types.

Examples in this project include reviewers for:

- Markdown
- Java
- HTML
- PlantUML
- Python
- Text files
- TypeScript

For example, a Markdown reviewer can read an HTML comment block that contains `@guidance:` and return the extracted instructions in the format Ghostwriter expects.

## Step-by-step example

The example below shows how to use guidance for a documentation update.

### Step 1: Choose a file

Pick the file you want Ghostwriter to update.

Example:

`src/site/markdown/guidance.md`

### Step 2: Add a guidance block

Place a `@guidance:` comment in the file.

Example:

```markdown
<!-- @guidance:
Rewrite this page for first-time users.
Add a simple example.
Keep the language clear and short.
-->
```

### Step 3: Run Ghostwriter

Run Ghostwriter from the project root by using your normal project workflow.

During processing, Ghostwriter will:

- scan the project,
- find the file,
- choose the right reviewer,
- extract the guidance,
- add standard processing instructions,
- and send the request to the AI provider.

### Step 4: Review the result

Read the updated file and confirm that it follows the guidance.

If you leave the guidance block in the file, Ghostwriter can reuse those same instructions in future runs.

## Practical real-world use

A documentation team can place update instructions directly inside important pages.

For example:

- a release notes page can ask Ghostwriter to keep entries short and grouped by version,
- a getting started page can ask for beginner-friendly language,
- and a technical reference page can ask for a more formal structure.

Because the guidance stays in each file, the expected style and purpose remain visible over time.

## Why this feature is useful

Guidance makes AI-assisted updates more predictable and easier to manage. Instead of rewriting instructions for every run, you store them inside the file itself.

That gives you:

- more consistent results,
- clearer expectations for each file,
- easier reuse over time,
- and a workflow that supports both technical and non-technical users.

## Tips for writing effective guidance

- Focus on one main goal at a time.
- Mention anything that must be included, such as sections, links, or formatting.
- Say if the tone should be simple, technical, formal, or beginner-friendly.
- Add examples when you want the result to be easier to follow.
- Keep the instructions short and direct.
