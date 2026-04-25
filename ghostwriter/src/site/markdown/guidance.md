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

Guidance is the part of Ghostwriter that lets you place short instructions directly inside a project file. These instructions use the `@guidance:` tag. When Ghostwriter runs, it finds those instructions and uses them to decide how that file should be updated.

This makes file updates easier to repeat. The instructions stay with the file, so the purpose of the file and the expected changes are documented in the same place.

For a broader introduction to this workflow, see [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## What `GuidanceProcessor` does

`GuidanceProcessor` is the Ghostwriter class that manages guided file processing. Its job is to scan the project, identify files that can contain guidance, read the `@guidance:` text, and send the resulting request to the configured AI provider.

In simple terms, it works like this:

1. It walks through the project folders.
2. It decides which files or directories should be considered.
3. It selects a reviewer based on the file type, such as Markdown or Java.
4. The reviewer reads the file and extracts any `@guidance:` instructions.
5. `GuidanceProcessor` prepares the instructions used for AI processing.
6. Ghostwriter updates the file using the configured AI provider.

This process is based on file traversal. It does not build the project or try to resolve project dependencies.

## How guidance fits into Ghostwriter

Ghostwriter supports different ways of working with AI-generated content. Guidance is the feature that makes file-by-file updates structured and repeatable.

Instead of writing one large prompt outside the project, you place small instructions inside each file. That means:

- the instructions stay close to the content they affect,
- future updates can follow the same rules,
- and team members can understand the purpose of the file more easily.

This approach is especially useful for documentation, configuration files, and source files that need clear, repeatable updates.

## Key methods in `GuidanceProcessor`

### `loadReviewers()`

This method loads all available reviewers using Java's `ServiceLoader` mechanism. A reviewer knows how to inspect a certain type of file and extract guidance from it.

### `normalizeExtensionKey(String extension)`

This helper method cleans up file extensions so Ghostwriter can match them consistently. For example, it treats `.md` and `md` as the same file type.

### `match(File file, File projectDir)`

This method decides whether a file or directory should be processed. It applies the current path-matching rules and default-prompt behavior.

### `processModule(File projectDir, String module)`

For multi-module projects, this method decides whether a module should be scanned. If a scan directory is configured, only matching modules are processed.

### `processParentFiles(ProjectLayout projectLayout)`

This method processes files directly under the main project directory while skipping module folders. It can also apply a default prompt to the project directory itself when configured.

### `processFile(ProjectLayout projectLayout, File file)`

This is the main per-file processing step. It checks whether the file matches, extracts guidance, and either uses that guidance or falls back to the default prompt.

### `process(ProjectLayout projectLayout, File file, String guidance)`

This method prepares the final processing instructions and hands them off to the base AI file-processing layer. It also adds standard documentation-processing rules, including the current operating system name.

### `parseFile(File projectDir, File file)`

This method chooses the correct reviewer for a file based on its extension. The reviewer then reads the file and extracts the guidance text.

### `getReviewerForExtension(String extension)`

This method looks up the reviewer associated with a given file extension.

### `deleteTempFiles(File basedir)`

This utility method removes the temporary folder used for input logs created during guided processing.

## Practical example

The example below shows how someone might use guidance to update a documentation page.

### Step 1: Pick a file

Choose the file you want Ghostwriter to improve.

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

Run Ghostwriter from the project root using your normal workflow.

During processing, Ghostwriter will:

- scan the project,
- find the file,
- choose the correct reviewer,
- extract the guidance text,
- build the final request,
- and generate an updated version of the file.

### Step 4: Review the result

Check the updated content just as you would review any documentation or code change.

If you keep the guidance block in the file, Ghostwriter can follow the same instructions again in a future run.

## Why this feature is useful

Guidance is helpful because it makes AI-assisted updates more predictable. Instead of rewriting instructions every time, you keep them in the file itself.

That gives you:

- more consistent results,
- clearer expectations for each file,
- easier repeat use over time,
- and a workflow that is friendly to both technical and non-technical users.

## Tips for writing effective guidance

- Keep the instruction focused on one main goal.
- Mention anything that must be included, such as sections, links, or formatting.
- Say if the tone should be simple, technical, formal, or beginner-friendly.
- Add examples when you want the output to be easier to follow.
