/**
 * File-format-aware scanners that locate embedded {@code @guidance} instructions in project files and convert them
 * into normalized prompt fragments for Ghostwriter's downstream processing pipeline.
 *
 * <p>The central abstraction is the {@link org.machanism.machai.gw.reviewer.Reviewer} service-provider interface
 * (SPI). Each {@code Reviewer} targets a specific file type (for example Java sources, Markdown, HTML/XML,
 * TypeScript, Python, PlantUML, or plain-text {@code @guidance.txt} files), understands that format's comment
 * conventions, and returns a formatted fragment that can be assembled into a single request for the LLM.
 *
 * <p>A typical fragment includes:
 * <ul>
 *   <li>the project-relative path computed using
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getRelativePath(java.io.File, java.io.File)}</li>
 *   <li>the original file content or an extracted guidance block containing {@code @guidance}</li>
 *   <li>a prompt template sourced from the {@code document-prompts} resource bundle</li>
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
