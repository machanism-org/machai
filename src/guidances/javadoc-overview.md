**IMPORTANT: GENERATE OR UPDATE CONTENT FOR THIS FILE: valid javadoc `overview.html` ACCORDING TO THE BEST PRACTICES AND RULES BELOW.**

# Content

1. Project Metadata & Source Integration:
   - Extract `project.name` and `project.description` from `pom.xml` to define the `[PROJECT_NAME]` and core overview purpose.
   - Aggregate detailed summaries, architecture insights, and relationships from all `package-info.java` files across source folders.
   - Explicitly mention that `[PROJECT_NAME]` supports all types of project files—including source code, documentation, project site content, and other relevant files.

2. Structure & Visuals:
   - Provide a clear, concise overview describing overall purpose, behavior, architecture, and usage of the modules/packages.
   - Embed and reference the class diagram located at `../images/class-diagram.png` with style="max-width: 100%; height: auto;" and proper alt text explaining principal types and relationships.
   - List and summarize all packages using standard definition lists (`<dl>`, `<dt>`, `<dd>`) linking to their respective `package-summary.html` pages.

3. Formatting, Syntax Safety & Build Error Prevention:
   - **Crucial Formatting Rule for Code:** When formatting code snippets or code references, do **not** wrap `{@code ...}` blocks with `<pre>` tags. Use only standard inline or block formatting, and preserve the `{@code ...}` syntax without additional HTML tags.
   - Ensure all HTML tags are well-formed and valid for Javadoc compilation.
   - Automatically correct any Javadoc build issues, missing descriptions, or invalid tags if encountered.

# Format:

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
  <HEAD>
    <TITLE>API Overview</TITLE>
  </HEAD>
  <BODY>
    Short overview of the API.
  </BODY>
</HTML>
