/**
 * File-format-aware components that locate embedded {@code @guidance} instructions and convert them into
 * normalized prompt fragments for Ghostwriter's downstream processing pipeline.
 *
 * <p>The central abstraction is the {@link org.machanism.machai.gw.reviewer.Reviewer} service-provider interface
 * (SPI). Each {@code Reviewer} targets one or more file extensions, understands the corresponding comment or
 * annotation conventions for that format, and returns a formatted fragment that can be assembled into a single
 * request to the LLM.
 *
 * <p>Implementations in this package are responsible for:
 * <ul>
 *   <li>reading supported files as UTF-8</li>
 *   <li>detecting whether {@code @guidance} is present (or, for {@code @guidance.txt}, matching by filename)</li>
 *   <li>computing a project-relative path via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getRelativePath(java.io.File, java.io.File}</li>
 *   <li>emitting a prompt fragment using templates from the {@code document-prompts} resource bundle</li>
 * </ul>
 *
 * <p>Supported formats include (but are not limited to):
 * <ul>
 *   <li>Java source ({@link org.machanism.machai.gw.reviewer.JavaReviewer})</li>
 *   <li>Markdown ({@link org.machanism.machai.gw.reviewer.MarkdownReviewer})</li>
 *   <li>HTML/XML ({@link org.machanism.machai.gw.reviewer.HtmlReviewer})</li>
 *   <li>TypeScript ({@link org.machanism.machai.gw.reviewer.TypeScriptReviewer})</li>
 *   <li>Python ({@link org.machanism.machai.gw.reviewer.PythonReviewer})</li>
 *   <li>PlantUML ({@link org.machanism.machai.gw.reviewer.PumlReviewer})</li>
 *   <li>Plain guidance files ({@link org.machanism.machai.gw.reviewer.TextReviewer})</li>
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
