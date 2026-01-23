/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Maven plugin goals and supporting infrastructure for creating, updating, registering, and cleaning a Bindex index
 * during a Maven build.
 *
 * <p>
 * The classes in this package provide Maven {@code Mojo} implementations that integrate Bindex operations into a
 * project's build lifecycle.
 * </p>
 *
 * <h2>Goals</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.maven.Create} and {@link org.machanism.machai.maven.Update} generate or refresh the
 *   index for the current project.</li>
 *   <li>{@link org.machanism.machai.maven.Register} registers an existing index/metadata with the configured AI
 *   provider.</li>
 *   <li>{@link org.machanism.machai.maven.Clean} removes plugin-generated temporary artifacts.</li>
 * </ul>
 *
 * <h2>Command-line usage</h2>
 * <pre>
 * mvn org.machanism.machai:bindex-maven-plugin:create
 * mvn org.machanism.machai:bindex-maven-plugin:update
 * mvn org.machanism.machai:bindex-maven-plugin:register
 * mvn org.machanism.machai:bindex-maven-plugin:clean
 * </pre>
 */
package org.machanism.machai.maven;
