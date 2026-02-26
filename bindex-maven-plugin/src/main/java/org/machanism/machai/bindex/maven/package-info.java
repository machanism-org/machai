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
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Maven plugin goals (Mojos) that integrate Machai/Bindex operations into a Maven build.
 *
 * <p>
 * This package contains the plugin entry points that:
 * </p>
 * <ul>
 *   <li>create or update a Bindex index for the current Maven module,</li>
 *   <li>optionally publish derived metadata to an external registry service, and</li>
 *   <li>clean up plugin-generated temporary artifacts.</li>
 * </ul>
 *
 * <h2>Goals</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.maven.Create create} &ndash; create a new Bindex index for the current module.</li>
 *   <li>{@link org.machanism.machai.bindex.maven.Update update} &ndash; refresh an existing Bindex index.</li>
 *   <li>{@link org.machanism.machai.bindex.maven.Register register} &ndash; scan the project and publish metadata to a registry.</li>
 *   <li>{@link org.machanism.machai.bindex.maven.Clean clean} &ndash; remove plugin-generated temporary artifacts.</li>
 * </ul>
 *
 * <h2>Execution model</h2>
 * <p>
 * Goals execute against the current {@link org.apache.maven.project.MavenProject}. Modules with {@code pom}
 * packaging (parents/aggregators) are typically skipped so the plugin runs only on buildable modules.
 * </p>
 *
 * <h2>Configuration</h2>
 * <p>
 * The AI provider/model is selected using {@code -Dbindex.genai} (for example {@code OpenAI:gpt-5}). The
 * {@code register} goal additionally uses {@code -Dbindex.register.url} to specify the registry endpoint.
 * </p>
 *
 * <h2>Command-line usage</h2>
 * <pre>
 * mvn org.machanism.machai:bindex-maven-plugin:create
 * mvn org.machanism.machai:bindex-maven-plugin:update
 * mvn org.machanism.machai:bindex-maven-plugin:register -Dbindex.register.url=http://localhost:8080
 * mvn org.machanism.machai:bindex-maven-plugin:clean
 * </pre>
 */
package org.machanism.machai.bindex.maven;
