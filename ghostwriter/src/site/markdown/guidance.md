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

Guidance is a Ghostwriter feature that lets you place simple instructions directly inside a file by using the `@guidance:` tag. When Ghostwriter scans the project, it reads those instructions and uses them to decide how that file should be updated.

This makes AI-assisted updates easier to repeat and easier to understand. The instructions stay in the file, so people can see what the file is supposed to do without needing a separate prompt document.

For a broader introduction to this workflow, see [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## What `GuidanceProcessor` does

`GuidanceProcessor` is the Ghostwriter class that manages guided file processing. It scans the project, finds supported files, reads their `@guidance:` instructions through file reviewers, and passes the processing request to the configured AI provider.

In simple terms, it works like this:

1. Ghostwriter walks through the project folders.
2. `GuidanceProcessor` checks which files or directories should be included.
3. It picks a reviewer that understands the file type.
4. The reviewer reads the file and extracts the `@guidance:` text.
5. `GuidanceProcessor` prepares the final processing instructions.
6. Ghostwriter sends the request to the AI provider and updates the file.

This process is based on scanning files in the project tree. It does not build the project or resolve dependencies.

## How guidance fits into Ghostwriter

Ghostwriter supports workflows that help apply AI updates in a structured way. Guidance is the feature that makes file-by-file updates repeatable.

Instead of writing one large prompt outside the project, you place short instructions inside each file. This means:

- the instructions stay close to the content they affect,
- future updates can follow the same rules,
- and other people can quickly understand the purpose of the file.

This approach is especially useful for documentation, configuration files, and source files that benefit from clear, repeatable update rules.

## Key methods in `GuidanceProcessor`

### `loadReviewers()`

Loads all available reviewers through Java's `ServiceLoader`. A reviewer knows how to inspect a supported file type and extract any `@guidance:` text.

### `normalizeExtensionKey(String extension)`

Normalizes file extensions so Ghostwriter can match them consistently. For example, it treats `.md` and `md` as the same type.

### `match(File file, File projectDir)`

Decides whether a file or directory should be processed. It applies the configured path-matching rules and the default prompt behavior.

### `processModule(File projectDir, String module)`

Handles module directories in multi-module projects. When a scan directory is configured, it limits processing to matching modules.

### `processParentFiles(ProjectLayout projectLayout)`

Processes files directly under the main project directory while skipping module folders. It can also apply a default prompt to the main project directory itself.

### `processFile(ProjectLayout projectLayout, File file)`

This is the main per-file processing step. It checks whether the file should be processed, extracts guidance, and uses that guidance or a default prompt.

### `process(ProjectLayout projectLayout, File file, String guidance)`

Builds the final system instructions and passes the request to the AI file-processing layer. It also adds standard documentation-processing instructions, including the current operating system name.

### `parseFile(File projectDir, File file)`

Chooses the right reviewer for a file based on its extension and asks that reviewer to extract the guidance text.

### `getReviewerForExtension(String extension)`

Looks up the reviewer registered for a specific file extension.

### `deleteTempFiles(File basedir)`

Removes the temporary input-log folder created during guided processing.

## Step-by-step example

The example below shows how to use guidance to improve a documentation page.

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

Run Ghostwriter from the project root using your normal workflow.

During processing, Ghostwriter will:

- scan the project,
- find the file,
- choose the correct reviewer,
- extract the guidance text,
- prepare the final request,
- and generate an updated version of the file.

### Step 4: Review the result

Read the updated content and confirm that it matches the guidance.

If you leave the guidance block in the file, Ghostwriter can use the same instructions again in a future run.

## Why this feature is useful

Guidance helps make AI-assisted updates more predictable. Instead of rewriting instructions every time, you keep them in the file itself.

That gives you:

- more consistent results,
- clearer expectations for each file,
- easier reuse over time,
- and a workflow that is friendly to both technical and non-technical users.

## Tips for writing effective guidance

- Keep the instruction focused on one main goal.
- Mention anything that must be included, such as sections, links, or formatting.
- Say if the tone should be simple, technical, formal, or beginner-friendly.
- Add examples when you want the output to be easier to follow.
