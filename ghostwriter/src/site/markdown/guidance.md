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

Guidance lets you place short, human-written instructions directly inside your project files (documentation, source code, or configuration). Ghostwriter scans the project, finds those instructions, and uses them to decide what to generate or update.

Because the instruction lives in the file it applies to, it’s easier to keep changes consistent over time: the “what to do” travels with the file.

For a broader introduction to this workflow, see: [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## What `GuidanceProcessor` does

`GuidanceProcessor` is the component that performs “guided” file updates.

In simple terms, it:

- walks through your project folders,
- picks the right file “reviewer” based on file type,
- extracts any `@guidance:` instructions from each file, and
- sends the resulting request to your configured AI provider.

It is traversal-based: it does not build your project or resolve code dependencies.

## How it fits into Ghostwriter

A typical guided workflow looks like this:

1. You add a `@guidance:` block to a file.
2. Ghostwriter scans the project.
3. `GuidanceProcessor` uses a `Reviewer` for the file type to read the guidance.
4. If guidance is found (or a default prompt is configured), `GuidanceProcessor` dispatches a prompt to the AI provider.
5. Ghostwriter writes the result and logs what happened.

### Reviewers (how guidance is read)

Guidance is not hard-coded for each file type. Instead, Ghostwriter uses `Reviewer` implementations that know how to read guidance comments from specific kinds of files.

`GuidanceProcessor` discovers reviewers using Java’s `ServiceLoader`, then builds a lookup so an extension like `md` or `java` maps to the correct reviewer.

## Key methods (plain-language summary)

- `loadReviewers()`
  - Finds all available `Reviewer` implementations.
  - Registers them by supported file extension.

- `match(file, projectDir)`
  - Decides whether a file/folder should be included.
  - If no path filter is configured, it limits processing to the project directory itself unless a default prompt is not set.

- `processModule(projectDir, module)`
  - For multi-module projects, decides which modules to scan.
  - When a scan directory is configured, it only processes modules that match or contain that scan directory.

- `processParentFiles(projectLayout)`
  - Processes files directly under the parent project directory while skipping module directories.
  - Can also run a “default guidance” prompt against the project directory.

- `parseFile(projectDir, file)`
  - Picks the reviewer based on file extension.
  - Uses the reviewer to extract `@guidance:` instructions from the file.

- `process(projectLayout, file, guidance)`
  - Builds the final instructions (including documentation-processing rules and your OS name).
  - Calls the base file-processing layer to run the AI provider.

## Practical example: guide an update to one page

### Step 1: Choose the file

Pick the file you want Ghostwriter to update.

Example: `src/site/markdown/guidance.md`

### Step 2: Add a `@guidance:` block

Add a short, specific block that tells Ghostwriter what you want.

Example:

```markdown
<!-- @guidance:
Update this page for first-time users.
Add one simple step-by-step example.
Keep the tone clear and practical.
-->
```

### Step 3: Run Ghostwriter

Run Ghostwriter with guidance enabled (the exact command depends on your setup).

What happens next:

- Ghostwriter scans from the project root directory.
- For each matching file, `GuidanceProcessor` extracts the `@guidance:` text using the file’s reviewer.
- If no `@guidance:` is present but a default prompt is configured, it uses that default instead.
- `GuidanceProcessor` assembles the request and sends it to the configured AI provider.

### Step 4: Review the result

Review the generated change like any other update. Keeping the `@guidance:` block in the file makes it easy to repeat the workflow later.

## Tips for writing good guidance

- Prefer one clear goal per file.
- Mention any important constraints (tone, length, required sections, required links).
- If formatting matters (headings, lists, code blocks), say so directly.
