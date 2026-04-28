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

`GuidanceProcessor` is the Ghostwriter class that manages guided file processing. It scans the project tree, finds files that match the current rules, selects a reviewer for each supported file type, extracts any `@guidance:` instructions, and sends the work to the configured AI provider.

In simple terms, it works like this:

1. Ghostwriter walks through the project folders.
2. `GuidanceProcessor` checks which files or directories should be included.
3. It picks a reviewer that understands the file type.
4. The reviewer reads the file and extracts the `@guidance:` text.
5. `GuidanceProcessor` prepares the processing instructions.
6. Ghostwriter sends the request to the AI provider and updates the file.

This is a scan-based process. It works by traversing the project directory and does not build the project or resolve dependencies.

## How guidance fits into Ghostwriter

Ghostwriter supports structured AI workflows for updating project files. Guidance is the feature that makes file-by-file processing repeatable and easier to manage.

Instead of writing one large prompt outside the project, you place short instructions inside each file. This means:

- the instructions stay close to the content they affect,
- future updates can follow the same rules,
- supported file types can be processed in a consistent way,
- and other people can quickly understand the purpose of the file.

This approach is especially useful for documentation and other files that benefit from clear, reusable update rules.

## Key methods in `GuidanceProcessor`

### `loadReviewers()`

Loads available reviewers by using Java's `ServiceLoader`. A reviewer knows how to inspect a supported file type and extract any `@guidance:` text.

### `normalizeExtensionKey(String extension)`

Normalizes file extensions so Ghostwriter can match them reliably. For example, it treats `.md` and `md` as the same file type.

### `match(File file, File projectDir)`

Decides whether a file or directory should be processed. It applies path-matching rules and also supports default guidance behavior when no specific guidance is found.

### `processModule(File projectDir, String module)`

Handles module directories in multi-module projects. If a scan directory is configured, it limits processing to modules that match that scope.

### `processParentFiles(ProjectLayout projectLayout)`

Processes files and folders directly under the main project directory while skipping module folders. It can also process the main project directory itself when a default prompt is configured.

### `processFile(ProjectLayout projectLayout, File file)`

This is the main per-file processing step. It checks whether the file should be processed, extracts guidance, and uses that guidance or a default prompt.

### `process(ProjectLayout projectLayout, File file, String guidance)`

Builds the final instructions sent to the AI layer. It adds standard documentation-processing instructions, includes the current operating system name, and then passes the request to the parent AI file processor.

### `parseFile(File projectDir, File file)`

Chooses the right reviewer for a file based on its extension and asks that reviewer to extract the guidance text.

### `getReviewerForExtension(String extension)`

Looks up the reviewer registered for a specific file extension.

### `deleteTempFiles(File basedir)`

Removes the temporary input-log directory created during guided processing.

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
