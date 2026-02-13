/**
 * Maven plugin goals (Mojos) for running MachAI's generative-workflow (GW) guided document processing.
 *
 * <p>
 * This package contains the Maven {@code Mojo} implementations that:
 * </p>
 * <ul>
 *   <li>scan documentation sources (for example, {@code src\site}),</li>
 *   <li>select files based on the plugin configuration, and</li>
 *   <li>invoke the MachAI GW processing pipeline via {@link org.machanism.machai.gw.FileProcessor}.</li>
 * </ul>
 *
 * <p>
 * Configuration is typically supplied using Maven plugin parameters (often via {@code -Dgw.*} system properties)
 * or POM configuration.
 * </p>
 *
 * <h2>Goals</h2>
 * <ul>
 *   <li>{@code gw:std} - Standard per-module scan and process goal.</li>
 *   <li>{@code gw:gw} - Aggregator goal that processes a reactor build in reverse order (sub-modules first).</li>
 *   <li>{@code gw:clean} - Deletes temporary artifacts created by GW processing.</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * mvn gw:std
 * }</pre>
 *
 * <pre>{@code
 * mvn gw:gw
 * }</pre>
 */
package org.machanism.machai.maven;

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
 * - Use the Java Version Defined in `pom.xml`:
 *      - All code improvements and Javadoc updates must be compatible with the Java version `maven.compiler.release` specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
