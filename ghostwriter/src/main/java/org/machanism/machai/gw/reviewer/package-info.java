/**
 * Provides file-format-specific reviewers that discover embedded {@code @guidance} directives and
 * translate matching project artifacts into normalized prompt fragments for the Ghostwriter review
 * pipeline.
 *
 * <p>The package is built around the {@link org.machanism.machai.gw.reviewer.Reviewer} service-provider
 * interface. A reviewer receives the project root directory and a candidate file, determines whether
 * that file contains actionable guidance for its format, and returns a formatted prompt fragment or
 * {@code null} when no relevant guidance is present. Implementations use project-relative paths from
 * {@link org.machanism.machai.project.layout.ProjectLayout} so generated prompts can refer to files in
 * a stable, repository-oriented form.
 *
 * <p>Included reviewers cover the file types commonly used to document or configure a project:
 * Java source, Markdown, HTML/XML, TypeScript, Python, PlantUML, and standalone text guidance files.
 * Format-specific implementations recognize the comment or marker syntax appropriate for their target
 * file type, read content as UTF-8, and format output with templates from the {@code document-prompts}
 * resource bundle.
 *
 * <p>Java package documentation receives special handling through
 * {@link org.machanism.machai.gw.reviewer.JavaReviewer}: when a guided {@code package-info.java} file
 * is reviewed, the generated prompt targets the package rather than a single class. Standalone
 * {@code @guidance.txt} files are handled by {@link org.machanism.machai.gw.reviewer.TextReviewer},
 * which treats the containing directory as the contextual target for the guidance.
 *
 * @see org.machanism.machai.gw.reviewer.Reviewer
 * @see org.machanism.machai.gw.reviewer.JavaReviewer
 * @see org.machanism.machai.gw.reviewer.TextReviewer
 */
package org.machanism.machai.gw.reviewer;

/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE OR ADD JAVADOC FOR ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 *
 * - Update Existing Javadoc and Add Missing Javadoc:
 *      - Review all classes in the folder.
 *      - Update any existing Javadoc to ensure it is accurate, comprehensive, and follows best practices.
 *      - Add Javadoc to any classes, methods, or fields where it is missing.
 *      - Ensure that all Javadoc is up-to-date and provides clear, meaningful documentation.
 * - Use Clear and Concise Descriptions:
 *      - Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 *      - Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * - Include Usage Examples Where Helpful:
 *      - Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * - Maintain Consistency and Formatting:
 *      - Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *      - Review the Java class source code and include comprehensive Javadoc comments for all classes,
 *           methods, and fields, adhering to established best practices.
 *      - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *           and any exceptions thrown.
 *      - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;`
 *           and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc.
 *      - Do not use escaping in `{@code ...}` tags.    
 * - Use the Java Version Defined in `pom.xml`:
 *      - All code improvements and Javadoc updates must be compatible with the Java version specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
