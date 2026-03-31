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

Guidance lets you place short, human-written instructions in your project files. Ghostwriter scans your project, finds those instructions, and uses them to decide what to generate or update.

Because the instruction lives inside the file it applies to (documentation, source code, configuration, and so on), it’s easier to keep changes consistent over time.

For a broader introduction to this workflow, see **Guided File Processing**: [Guided File Processing](https://machanism.org/guided-file-processing/index.html).

## What `GuidanceProcessor` does

`GuidanceProcessor` is the component that runs “guided” updates. It:

- walks through your project folders,
- asks the right file “reviewer” how to read each file type, and
- sends the final instructions to the configured AI provider.

It works by scanning files (a traversal). It does not build your project or try to understand dependencies.

## How it fits into Ghostwriter

Ghostwriter’s guided workflow is:

1. You add a `@guidance:` comment to a file (or configure a default prompt).
2. Ghostwriter scans the project and extracts that guidance.
3. `GuidanceProcessor` composes a complete request (system rules + your guidance).
4. The request is handed to the base AI processing layer, which calls the configured `Genai` provider.

## Key methods (in plain language)

- `loadReviewers()`
  - Finds all available `Reviewer` implementations using Java’s `ServiceLoader`.
  - Builds a lookup table so a file extension like `md` or `java` maps to the correct reviewer.

- `match(file, projectDir)`
  - Decides whether a file/folder should be included.
  - Special case: if no path matcher is configured, it only processes the project root when a default prompt is set.

- `processModule(projectDir, module)`
  - Controls whether a module is scanned in a multi-module project.
  - If a scan directory is configured, it only processes modules that match (or contain) that scan directory.

- `processParentFiles(projectLayout)`
  - Processes files directly under the parent project while excluding module directories.
  - Can also apply the default prompt to the parent directory itself.

- `parseFile(projectDir, file)`
  - Chooses a reviewer based on the file extension.
  - Uses that reviewer to extract any `@guidance:` instructions from the file.

- `process(projectLayout, file, guidance)`
  - Builds the final instructions (including documentation-processing rules and your OS name).
  - Delegates to the base AI file processor to contact the `Genai` provider.

## Practical example: update one documentation page

1. **Pick the file you want to guide**
   - Example: `src/site/markdown/guidance.md`

2. **Add a `@guidance:` block to the file**
   - Keep it short and specific.

   Example:

   ```markdown
   <!-- @guidance:
   Update this page for first-time users.
   Add a short step-by-step example.
   Keep the tone simple and practical.
   -->
   ```

3. **Run Ghostwriter with guidance enabled**
   - Ghostwriter scans from the project root directory.
   - For each file it decides to process, it extracts the `@guidance:` text.

4. **Ghostwriter applies the change**
   - `GuidanceProcessor` assembles the full prompt (system rules + your guidance).
   - The configured AI provider generates the update.

5. **Review the result**
   - Treat the output as a draft you approve or adjust.
   - Leaving the `@guidance:` block in the file makes the workflow repeatable.

## Tips for writing good guidance

- Prefer one clear goal per file.
- Mention any important constraints (tone, length, required sections, required links).
- If formatting matters (headings, lists, code blocks), say so directly.
