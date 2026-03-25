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
 * Maven plugin Mojos that integrate Machai/Bindex workflows into a Maven build.
 *
 * <p>
 * The classes in this package provide goals for creating or updating a Bindex index
 * for the current project, registering the project's metadata in an external
 * registry, and cleaning up temporary artifacts produced during execution.
 * </p>
 *
 * <h2>Goals</h2>
 *
 * <ul>
 * <li>{@link org.machanism.machai.bindex.maven.Create} - generates a new Bindex index and related resources.</li>
 * <li>{@link org.machanism.machai.bindex.maven.Update} - refreshes an existing index.</li>
 * <li>{@link org.machanism.machai.bindex.maven.Register} - scans the project and publishes metadata to a registry.</li>
 * <li>{@link org.machanism.machai.bindex.maven.Clean} - removes temporary artifacts (for example the inputs log file).</li>
 * </ul>
 *
 * <p>
 * Shared configuration and Maven integration helpers are implemented by
 * {@link org.machanism.machai.bindex.maven.AbstractBindexMojo}. Common properties include:
 * </p>
 *
 * <ul>
 * <li>{@code bindex.model} (required) - AI provider/model identifier used by Bindex (for example {@code OpenAI:gpt-5}).</li>
 * <li>{@code genai.serverId} (optional) - {@code settings.xml} server id used to resolve GenAI credentials.</li>
 * <li>{@code bindex.register.url} (optional) - registry endpoint URL used by the {@code register} goal.</li>
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
