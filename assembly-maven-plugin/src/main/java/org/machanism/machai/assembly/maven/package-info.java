/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
 *      - All code improvements and Javadoc updates must be compatible with the Java version `maven.compiler.release` specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */

/**
 * Maven plugin entry point for MachAI's AI-assisted project assembly workflow.
 *
 * <p>
 * This package provides the {@link org.machanism.machai.assembly.maven.Assembly} Maven {@code Mojo} that exposes the
 * {@code assembly} goal. The goal is intended to be run against a target directory (typically the Maven
 * {@code ${basedir}}), using a natural-language prompt to guide automated project changes.
 * </p>
 *
 * <h2>High-level workflow</h2>
 * <ol>
 *   <li>Acquire a prompt from a configured text file or via interactive input.</li>
 *   <li>Use MachAI's {@link org.machanism.machai.bindex.Picker} to recommend candidate libraries as
 *       {@link org.machanism.machai.schema.Bindex} entries, filtered by a configured score threshold.</li>
 *   <li>Run {@link org.machanism.machai.bindex.ApplicationAssembly} to apply changes to the target project directory.</li>
 * </ol>
 *
 * <h2>Configuration</h2>
 * <p>
 * Inputs are typically supplied via Maven properties and/or plugin configuration (for example,
 * {@code -Dassembly.genai=...} and {@code -Dassembly.prompt.file=...}).
 * </p>
 */
package org.machanism.machai.assembly.maven;
