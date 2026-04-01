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

Guidance lets you place short, human-written instructions directly inside your project files (for example: documentation, source code, or configuration). Ghostwriter scans the project, finds those instructions, and uses them to decide what to generate or update.

Because the instruction lives in the file it applies to, it’s easier to keep changes consistent over time: the “what to do” travels with the file.

For a broader introduction to this workflow, see **Guided File Processing**: [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## What `GuidanceProcessor` does

`GuidanceProcessor` is the part of Ghostwriter that performs these “guided” updates. At a high level, it:

- scans folders and files under your project directory,
- uses the right “reviewer” for each file type to read guidance from the file, and
- sends the request to your configured AI provider.

It is traversal-based: it walks the file tree and processes files. It does not build your project or try to resolve code dependencies.

## How it fits into Ghostwriter

A typical guided workflow looks like this:

1. **You add guidance to a file**
   - You write a `@guidance:` comment in the file you want to control.

2. **Ghostwriter scans the project**
   - It looks for files that match your scan settings (or the whole project if configured that way).

3. **`GuidanceProcessor` extracts guidance**
   - It chooses a reviewer based on the file extension (for example, `md` for Markdown).
   - The reviewer reads the file and returns the guidance text.

4. **`GuidanceProcessor` composes the final request**
   - It adds system-level rules (for example: documentation processing instructions and the OS name).
   - It combines those rules with the guidance from your file (or a default prompt).

5. **Ghostwriter processes the file**
   - The AI provider returns the update.
   - Ghostwriter writes changes and logs what happened.

## Key methods (in plain language)

- `loadReviewers()`
  - Finds all available `Reviewer` implementations using Java’s `ServiceLoader`.
  - Builds a lookup table so a file extension like `md` or `java` maps to the correct reviewer.

- `match(file, projectDir)`
  - Decides whether a file or folder should be included.
  - Special case: if no path filter is configured, it only processes the project directory itself when a default prompt is set.

- `processModule(projectDir, module)`
  - Controls which modules are scanned in multi-module projects.
  - If a scan directory is configured, it only processes modules that match (or contain) that scan directory.

- `processParentFiles(projectLayout)`
  - Processes files directly under the parent project directory while skipping module directories.
  - Can also apply the default prompt to the parent directory itself.

- `parseFile(projectDir, file)`
  - Chooses a reviewer based on the file extension.
  - Uses that reviewer to extract any `@guidance:` instructions from the file.

- `process(projectLayout, file, guidance)`
  - Builds the final instructions and sends them to the AI provider via the base file-processing layer.

## Practical example: guide an update to one page

### 1) Pick a file

Choose the file you want Ghostwriter to update.

Example: `src/site/markdown/guidance.md`

### 2) Add a `@guidance:` block

Add a short, specific block that tells Ghostwriter what you want.

Example:

```markdown
<!-- @guidance:
Update this page for first-time users.
Add one simple step-by-step example.
Keep the tone clear and practical.
-->
```

### 3) Run Ghostwriter

Run Ghostwriter with guidance enabled (how you run it depends on your setup).

What happens next:

- Ghostwriter scans from the project root directory.
- For each matching file, it extracts the `@guidance:` text.
- `GuidanceProcessor` assembles the full prompt (system rules + your guidance).
- The configured AI provider generates the update.

### 4) Review and repeat

- Review the result just like any other change.
- Keeping the `@guidance:` block in the file makes the workflow easy to repeat later.

## Tips for writing good guidance

- Prefer one clear goal per file.
- Mention any important constraints (tone, length, required sections, required links).
- If formatting matters (headings, lists, code blocks), say so directly.
