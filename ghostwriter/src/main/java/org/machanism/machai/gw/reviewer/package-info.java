/**
 * File format reviewers used by Ghostwriter to discover embedded {@code @guidance} instructions and
 * normalize them into prompt fragments.
 *
 * <p>This package provides {@link org.machanism.machai.gw.reviewer.Reviewer} implementations for a
 * variety of source and documentation formats. Each reviewer is responsible for:
 * <ul>
 *   <li>detecting whether a file contains the {@code @guidance} tag, and</li>
 *   <li>emitting a formatted fragment (usually including the file name, a project-relative path, and
 *       either the full file content or extracted guidance text) that can be appended to the
 *       assembled prompt.</li>
 * </ul>
 *
 * <p>Project-relative paths are computed via
 * {@link org.machanism.machai.project.layout.ProjectLayout} so the resulting fragments remain stable
 * across environments.
 *
 * <h2>Supported formats</h2>
 * <ul>
 *   <li>Java source ({@code .java}, including {@code package-info.java})</li>
 *   <li>TypeScript ({@code .ts})</li>
 *   <li>Python ({@code .py})</li>
 *   <li>HTML/XML ({@code .html}, {@code .htm}, {@code .xml})</li>
 *   <li>Markdown ({@code .md})</li>
 *   <li>Guidance text files named {@code @guidance.txt}</li>
 * </ul>
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
