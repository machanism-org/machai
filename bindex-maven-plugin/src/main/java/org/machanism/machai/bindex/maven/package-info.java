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
 *      - Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&lt;` 
 *          and `&gt;` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Maven plugin goals for creating, updating, cleaning, and registering Bindex
 * metadata for a Maven project.
 *
 * <p>
 * This package provides several Mojos that integrate the Machai/Bindex indexing
 * workflow into the Maven lifecycle:
 * </p>
 *
 * <ul>
 * <li>{@link org.machanism.machai.bindex.maven.Create} - create a new Bindex
 * index and related resources.</li>
 * <li>{@link org.machanism.machai.bindex.maven.Update} - refresh an existing
 * Bindex index.</li>
 * <li>{@link org.machanism.machai.bindex.maven.Register} - scan the project and
 * publish Bindex metadata to an external registry.</li>
 * <li>{@link org.machanism.machai.bindex.maven.Clean} - remove temporary
 * artifacts (such as the inputs log file) created by the tooling.</li>
 * </ul>
 *
 * <p>
 * All goals share common configuration implemented by
 * {@link org.machanism.machai.bindex.maven.AbstractBindexMojo}, including:
 * </p>
 *
 * <ul>
 * <li>Selection of the AI provider/model via the {@code bindex.model}
 * property.</li>
 * <li>Optional credential resolution from {@code settings.xml} using
 * {@code gw.genai.serverId}.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 *
 * <pre>
 * mvn org.machanism.machai:bindex-maven-plugin:create -Dbindex.model=OpenAI:gpt-5
 * mvn org.machanism.machai:bindex-maven-plugin:update -Dbindex.model=OpenAI:gpt-5
 * mvn org.machanism.machai:bindex-maven-plugin:register -Dbindex.model=OpenAI:gpt-5 -Dbindex.register.url=http://localhost:8080
 * mvn org.machanism.machai:bindex-maven-plugin:clean
 * </pre>
 */
package org.machanism.machai.bindex.maven;
