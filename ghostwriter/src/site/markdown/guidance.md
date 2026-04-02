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

Guidance lets you place short, human-written instructions inside files in your project (documentation, source code, or configuration). Ghostwriter scans your project, finds those `@guidance:` instructions, and uses them to decide what to generate or update.

Because the instruction lives inside the file it applies to, the “what to do” stays with the content over time. This makes it easier to re-run Ghostwriter later and get consistent results.

For an overview of this workflow, see: [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## What `GuidanceProcessor` does

`GuidanceProcessor` is the part of Ghostwriter that runs “guided file processing”. It scans your project folders, finds files that contain `@guidance:` instructions, and then asks the configured AI provider to update those files.

It is traversal-based: it reads files and folders. It does not build your project or resolve dependencies.

In simple terms, it:

- walks through your project directories,
- picks a “reviewer” for each file type (based on its file extension, such as `.md` or `.java`),
- uses that reviewer to extract any `@guidance:` text from the file,
- combines your guidance with standard processing rules,
- sends the final request to the configured AI provider,
- and applies the returned update.

## How it fits into Ghostwriter

Ghostwriter can work in different ways (for example, running an “Act” template or processing files). Guidance is the mechanism that makes file processing repeatable: the instructions live in the file itself.

A typical guided workflow looks like this:

1. Add a `@guidance:` comment block to a file you want updated.
2. Run Ghostwriter against your project root.
3. `GuidanceProcessor` scans for files to consider and chooses a reviewer based on file extension.
4. The reviewer extracts the guidance text from the file.
5. `GuidanceProcessor` builds a final prompt (system instructions + documentation rules + your guidance, or a default prompt).
6. Ghostwriter calls your configured AI provider and writes the result back.

### Reviewers (how guidance is discovered)

Guidance is not hard-coded per file type. Instead, Ghostwriter uses `Reviewer` implementations:

- Each reviewer declares which file extensions it supports.
- `GuidanceProcessor` loads reviewers using Java’s `ServiceLoader`.
- It keeps a lookup map keyed by extension (for example, `md` or `java`).
- If no reviewer exists for a file type, that file is skipped (unless a default prompt is configured).

## Key methods (what they do)

- `loadReviewers()`
  - Finds all `Reviewer` implementations via `ServiceLoader`.
  - Registers each reviewer by the file extensions it supports.
  - Normalizes extensions so `.MD`, `md`, and `.md` all match the same way.

- `match(file, projectDir)`
  - Decides whether a file/folder should be considered.
  - If no path filter is configured, it only processes the project directory itself when a default prompt is present.

- `processModule(projectDir, module)`
  - In multi-module projects, decides which modules should be scanned.
  - If a scan directory is configured, it only scans modules that match (or contain) that scan directory.

- `processParentFiles(projectLayout)`
  - Processes files directly under the parent project directory while skipping module directories.
  - If a default prompt is configured and the project directory matches, it can also process the project directory itself.

- `parseFile(projectDir, file)`
  - Chooses a reviewer based on file extension.
  - Uses that reviewer to extract `@guidance:` instructions from the file.

- `process(projectLayout, file, guidance)`
  - Builds the final instruction text.
  - Adds standard documentation-processing rules (including the operating system name, such as `Windows 11`) before handing off to the base AI file-processing layer.

## Practical example: update a documentation page

### Step 1: Choose a file

Pick the file you want Ghostwriter to help update.

Example: `src/site/markdown/guidance.md`

### Step 2: Add a `@guidance:` block

Add a short instruction block near the top of the file. Keep it specific.

Example:

```markdown
<!-- @guidance:
Update this page for first-time users.
Add one step-by-step example.
Keep the tone clear and practical.
-->
```

### Step 3: Run Ghostwriter

Run Ghostwriter in whatever way you normally run the project (for example, via your preferred command line, IDE run configuration, or build tool integration).

What happens:

- Ghostwriter scans from your configured project root.
- For each matching file, `GuidanceProcessor` selects a reviewer and extracts the `@guidance:` text.
- If the file has guidance, that guidance is used to build the prompt.
- If the file does not have guidance but a default prompt is configured, the default prompt is used instead.
- The configured AI provider is called and returns the updated content.

### Step 4: Review the result

Review the change like any other code or documentation change.

If you leave the `@guidance:` block in place, you can run Ghostwriter again later and it will follow the same instructions.

## Tips for writing good guidance

- Prefer one clear goal per file.
- Mention important constraints (tone, length, required sections, required links).
- If formatting matters (headings, lists, code blocks), say so directly.
