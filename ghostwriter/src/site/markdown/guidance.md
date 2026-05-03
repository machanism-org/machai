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

Guidance is a Ghostwriter feature that lets a file include its own update instructions by using the `@guidance:` tag. When Ghostwriter scans a project, `GuidanceProcessor` finds those instructions, asks the correct reviewer to read them, and sends the prepared request to the configured AI provider.

This keeps instructions close to the file they belong to. Instead of storing update notes somewhere else, the file itself can describe how it should be processed.

For a broader introduction to this workflow, see [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## What `GuidanceProcessor` does

`GuidanceProcessor` is the part of Ghostwriter that manages guided file processing.

In simple terms, it:

1. scans the project structure,
2. checks which files and folders match the current scan rules,
3. chooses a reviewer based on the file type,
4. reads any `@guidance:` instructions found in the file,
5. uses a default prompt when no file-specific guidance is available,
6. adds standard processing instructions,
7. and passes the request to the AI processing layer.

It works with both single-module and multi-module projects. In multi-module projects, child modules are processed before the parent project directory. This is a traversal process only, so it walks the project tree without building the project or resolving dependencies.

## Purpose of the guidance feature

The guidance feature lets each supported file explain how Ghostwriter should handle it during AI-assisted processing.

This helps teams:

- keep instructions next to the content they affect,
- make repeated updates more consistent,
- reuse the same file-specific rules over time,
- and make the workflow easier to understand.

Guidance is especially helpful for documentation, templates, and other files where the style, structure, or audience should stay consistent.

## How guidance fits into Ghostwriter

Ghostwriter can process project files with AI support. Guidance makes that processing more organized and easier to repeat.

Instead of creating one large external prompt for the entire project, you can place small instructions directly inside the exact file that needs attention. That means:

- the goal is visible where the content lives,
- other team members can quickly understand the file's purpose,
- future updates can follow the same rules,
- and supported file types can be handled in a more predictable way.

## Key methods in `GuidanceProcessor`

### `loadReviewers()`

Loads reviewer implementations by using Java's `ServiceLoader`. Each reviewer declares the file extensions it supports.

### `normalizeExtensionKey(String extension)`

Converts file extensions into a consistent lower-case form so matching works whether the extension is written with or without a leading dot.

### `match(File file, File projectDir)`

Checks whether a file or directory should be processed. If no path matcher is configured, it can still allow processing based on default guidance behavior.

### `processModule(File projectDir, String module)`

Handles modules in multi-module projects. When a scan directory is configured, it makes sure only relevant modules are processed.

### `processParentFiles(ProjectLayout projectLayout)`

Processes files and folders that belong directly to the main project directory while skipping module directories. It can also process the project directory itself when default guidance is available.

### `processFile(ProjectLayout projectLayout, File file)`

Handles the main file-level work. It checks whether the file matches the rules, extracts file-specific guidance through a reviewer, and uses the default prompt if needed.

### `process(ProjectLayout projectLayout, File file, String guidance)`

Prepares the final AI request. It adds standard system and document-processing instructions, includes the current operating system name, and then passes control to the parent AI file processor.

### `parseFile(File projectDir, File file)`

Looks at the file extension, finds the correct reviewer, and asks that reviewer to extract guidance from the file.

### `getReviewerForExtension(String extension)`

Returns the reviewer that is registered for a given file extension.

### `deleteTempFiles(File basedir)`

Removes the temporary input log directory created during guided processing.

## The Act feature and how it relates

Ghostwriter also includes an Act feature, implemented by `ActProcessor`. An act is a reusable prompt template stored in a `.toml` file.

While guidance puts instructions inside a specific file, an act gives Ghostwriter a reusable prompt workflow that can be applied across matching files. Both features support AI-assisted processing, but they solve different needs:

- guidance is file-focused,
- acts are template-focused,
- guidance reads instructions already stored in the file,
- and acts load instructions from predefined Act definitions.

In the overall project, both processors build on the same AI file-processing foundation. This helps Ghostwriter support both reusable workflows and file-specific instructions.

### Key methods in `ActProcessor`

#### `setAct(String act)`

Loads an Act definition, applies its prompt data, and prepares any episode-based execution settings.

#### `loadAct(String name, Map<String, Object> properties, String actsLocation)`

Loads an Act from built-in resources or a user-defined location and supports Act inheritance through the `basedOn` property.

#### `tryLoadActFromClasspath(...)`

Reads built-in Act definitions packaged with the application.

#### `tryLoadActFromDirectory(...)`

Reads custom Act definitions from a local directory or URL.

#### `applyActData(Map<String, Object> properties)`

Applies the loaded Act settings to the processor configuration.

#### `scanDocuments(File projectDir, String scanDir)`

Runs the Act processing flow, including support for episode-based execution, repeated episodes, and moves between episodes.

## Reviewers and supported file types

`GuidanceProcessor` does not parse every file format on its own. It relies on reviewer implementations that know how to detect `@guidance:` instructions in different file types.

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

If you keep the guidance block in the file, Ghostwriter can reuse those same instructions in future runs.

## Practical example

A documentation team can place update instructions directly inside important pages.

For example:

- a release notes page can ask Ghostwriter to keep entries short and grouped by version,
- a getting started page can ask for beginner-friendly language,
- and a technical reference page can ask for a more structured format.

Because the guidance stays in each file, the expected style and purpose remain visible over time.

## Why this feature is useful

Guidance makes AI-assisted updates more predictable and easier to manage. Instead of rewriting instructions for every run, you store them inside the file itself.

That gives you:

- more consistent results,
- clearer expectations for each file,
- easier reuse over time,
- and a workflow that is easier for both technical and non-technical users.

## Tips for writing effective guidance

- Focus on one main goal at a time.
- Mention anything that must be included, such as sections, links, or formatting.
- Say if the tone should be simple, technical, formal, or beginner-friendly.
- Add examples when you want the result to be easier to follow.
- Keep the instructions short and direct.
