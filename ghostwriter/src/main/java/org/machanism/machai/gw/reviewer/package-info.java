/**
 * File-format-specific reviewers that scan project files for embedded {@code @guidance} directives and transform
 * them into normalized prompt fragments for Ghostwriter processing.
 *
 * <p>The package centers on the {@link org.machanism.machai.gw.reviewer.Reviewer} service-provider interface
 * (SPI). Each implementation supports one or more file extensions, understands the comment or literal syntax of
 * its target format, detects whether the file contains the
 * {@link org.machanism.machai.gw.processor.GuidanceProcessor#GUIDANCE_TAG_NAME @guidance} marker, and produces a
 * formatted fragment using templates from the {@code document-prompts} resource bundle.
 *
 * <h2>Reviewer workflow</h2>
 * <ol>
 *   <li>Read a candidate file, typically as UTF-8 text.</li>
 *   <li>Detect a supported embedded guidance form such as line comments, block comments, HTML comments,
 *       triple-quoted strings, or a dedicated {@code @guidance.txt} file.</li>
 *   <li>Compute a stable project-relative path via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getRelativePath(java.io.File, java.io.File)}.</li>
 *   <li>Format and return a prompt fragment for downstream Ghostwriter stages, or return {@code null} when the
 *       file does not contain relevant guidance.</li>
 * </ol>
 *
 * <h2>Included implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.gw.reviewer.JavaReviewer} for Java sources, including
 *       {@code package-info.java}</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.MarkdownReviewer} for Markdown documents with embedded HTML
 *       comments</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.HtmlReviewer} for HTML and XML files</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TypeScriptReviewer} for TypeScript sources</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PythonReviewer} for Python sources</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PumlReviewer} for PlantUML diagrams</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TextReviewer} for standalone guidance text files</li>
 * </ul>
 *
 * <p>Together, these reviewers provide the format-aware extraction layer that bridges source artifacts and the
 * prompt-generation pipeline.
 *
 * @see org.machanism.machai.gw.reviewer.Reviewer
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
