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

Guidance is a Ghostwriter feature that lets you place simple instructions directly inside a file by using the `@guidance:` tag. When Ghostwriter scans a project, it reads those instructions and uses them to decide how that file should be processed.

This makes updates easier to repeat. The instructions stay with the file, so you do not need to keep a separate prompt somewhere else.

For a broader introduction to this workflow, see [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## What `GuidanceProcessor` does

`GuidanceProcessor` is the Ghostwriter component that manages guided file processing. It scans the project, checks which files should be included, chooses a reviewer that understands each supported file type, reads any `@guidance:` instructions, and sends the request to the configured AI provider.

In simple terms, it works like this:

1. Ghostwriter scans the project folders.
2. `GuidanceProcessor` checks which files or directories match the current scan rules.
3. It selects a reviewer for the file type.
4. The reviewer reads the file and detects the `@guidance:` content.
5. `GuidanceProcessor` prepares the final processing request.
6. Ghostwriter sends the task to the AI provider and applies the update.

This is a traversal-based process. It walks through the project structure and does not build the project or resolve dependencies.

## How guidance fits into Ghostwriter

Ghostwriter supports structured AI-assisted processing for project files. Guidance is the feature that makes this processing easier to repeat on a file-by-file basis.

Instead of writing one large prompt outside the project, you place short instructions inside the file that needs attention. This means:

- the rules stay close to the content they affect,
- future updates can follow the same guidance,
- supported file types can be handled in a consistent way,
- and team members can quickly understand the purpose of the file.

This approach is especially useful for documentation, source files, and other project content that benefits from clear, reusable instructions.

## Key methods in `GuidanceProcessor`

### `loadReviewers()`

Loads reviewer implementations through Java's `ServiceLoader`. Each reviewer supports one or more file types and knows how to find `@guidance:` instructions in that format.

### `normalizeExtensionKey(String extension)`

Normalizes file extensions so matching is consistent. For example, `.md` and `md` are treated as the same extension.

### `match(File file, File projectDir)`

Checks whether a file or directory should be processed. It uses the current path-matching rules and also supports default processing behavior when a default prompt is configured.

### `processModule(File projectDir, String module)`

Handles module folders in multi-module projects. When a scan directory is set, it makes sure only matching modules are processed.

### `processParentFiles(ProjectLayout projectLayout)`

Processes files and directories directly under the main project folder while skipping module directories. It can also process the main project directory itself when default guidance is available.

### `processFile(ProjectLayout projectLayout, File file)`

Performs the main file-level work. It checks whether the file should be processed, asks a reviewer to extract guidance, and falls back to a default prompt when needed.

### `process(ProjectLayout projectLayout, File file, String guidance)`

Builds the final request sent to the AI layer. It adds standard processing instructions, includes the current operating system name, and then delegates the work to the parent AI file processor.

### `parseFile(File projectDir, File file)`

Finds the right reviewer for a file based on its extension and asks that reviewer to inspect the file.

### `getReviewerForExtension(String extension)`

Returns the reviewer registered for a specific file extension.

### `deleteTempFiles(File basedir)`

Removes the temporary input log directory used during guided processing.

## Reviewers and supported file types

`GuidanceProcessor` does not read every file format by itself. Instead, it uses reviewer implementations that understand how guidance is written in different kinds of files.

Examples in this project include reviewers for:

- Markdown
- Java
- HTML
- PlantUML
- Python
- Text files
- TypeScript

For example, the Markdown reviewer looks for an HTML comment block that contains `@guidance:` and then returns the file content in the format needed by Ghostwriter.

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

Run Ghostwriter from the project root using your normal workflow.

During processing, Ghostwriter will:

- scan the project,
- find the file,
- choose the correct reviewer,
- extract the guidance text,
- prepare the final AI request,
- and generate an updated version of the file.

### Step 4: Review the result

Read the updated file and confirm that it follows the guidance.

If you keep the guidance block in place, Ghostwriter can reuse the same instructions in future runs.

## Practical real-world use

A documentation team can keep update instructions directly in important pages.

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
