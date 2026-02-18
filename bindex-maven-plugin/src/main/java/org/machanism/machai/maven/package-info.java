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
 * This package contains the plugin entry points that create or update a <em>Bindex</em> index for the current
 * Maven module, optionally register derived metadata with an external registry service, and clean temporary
 * artifacts created during execution.
 * </p>
 *
 * <h2>Included goals</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.maven.Create} &ndash; Create a new index for the current module.</li>
 *   <li>{@link org.machanism.machai.maven.Update} &ndash; Update/refresh an existing index for the current module.</li>
 *   <li>{@link org.machanism.machai.maven.Register} &ndash; Scan and publish metadata to a registry endpoint.</li>
 *   <li>{@link org.machanism.machai.maven.Clean} &ndash; Remove plugin-generated temporary artifacts.</li>
 * </ul>
 *
 * <h2>Execution model</h2>
 * <p>
 * Goals execute against the current {@link org.apache.maven.project.MavenProject}. Modules with {@code pom}
 * packaging (parents/aggregators) are typically skipped so the plugin only runs on buildable modules.
 * </p>
 *
 * <h2>Configuration</h2>
 * <ul>
 *   <li>
 *     AI provider/model selection: {@code -Dbindex.genai=Provider:Model}
 *     (for example {@code -Dbindex.genai=OpenAI:gpt-5}).
 *   </li>
 *   <li>
 *     Registry endpoint (for {@code register}): {@code -Dbindex.register.url=http://host:port}.
 *   </li>
 * </ul>
 *
 * <h2>Command-line usage</h2>
 * <pre>
 * mvn org.machanism.machai:bindex-maven-plugin:create
 * mvn org.machanism.machai:bindex-maven-plugin:update
 * mvn org.machanism.machai:bindex-maven-plugin:register -Dbindex.register.url=http://localhost:8080
 * mvn org.machanism.machai:bindex-maven-plugin:clean
 * </pre>
 */
package org.machanism.machai.maven;
