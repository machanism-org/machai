/**
 * File-format-aware components that locate embedded {@code @guidance} directives in project files and convert
 * them into normalized prompt fragments for Ghostwriter.
 *
 * <p>The main entry point is the {@link org.machanism.machai.gw.reviewer.Reviewer} service-provider interface
 * (SPI). Each {@code Reviewer} implementation targets one or more file types, applies the relevant comment and
 * string-literal conventions for that format, and produces a formatted fragment suitable for downstream
 * processing.
 *
 * <h2>How reviewers are used</h2>
 * <ul>
 *   <li>Read a supported file (typically UTF-8) and detect whether it contains an {@code @guidance} directive.</li>
 *   <li>Compute a project-relative path via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getRelativePath(java.io.File, java.io.File)} to
 *       provide stable context independent of the local filesystem layout.</li>
 *   <li>Format the final fragment using templates loaded from the {@code document-prompts} resource bundle.</li>
 * </ul>
 *
 * <h2>Provided reviewers</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.gw.reviewer.JavaReviewer} – Java source (including {@code package-info.java})</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.MarkdownReviewer} – Markdown</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.HtmlReviewer} – HTML/XML</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TypeScriptReviewer} – TypeScript</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PythonReviewer} – Python</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PumlReviewer} – PlantUML</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TextReviewer} – plain {@code @guidance.txt} files</li>
 * </ul>
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
