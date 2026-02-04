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
 * This package provides the executable goals that drive Bindex index creation, updates, cleanup, and metadata
 * publication during a Maven build. Goals execute in the context of the current
 * {@link org.apache.maven.project.MavenProject} and can be bound to standard lifecycle phases.
 * Projects using {@code pom} packaging (typically aggregator/parent modules) are generally skipped to avoid
 * running module-specific work at the root.
 * </p>
 *
 * <h2>Provided goals</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.maven.Create} - Create a new Bindex index for the current module.</li>
 *   <li>{@link org.machanism.machai.maven.Update} - Update (refresh) an existing index for the current module.</li>
 *   <li>{@link org.machanism.machai.maven.Register} - Scan the module and publish its metadata to a registry endpoint.</li>
 *   <li>{@link org.machanism.machai.maven.Clean} - Remove plugin-generated temporary artifacts.</li>
 * </ul>
 *
 * <h2>Common configuration</h2>
 * <p>
 * Provider/model selection can be controlled via {@code -Dbindex.genai=Provider:Model} (for example,
 * {@code -Dbindex.genai=OpenAI:gpt-5}). Shared parameters and common behavior are implemented by
 * {@link org.machanism.machai.maven.AbstractBindexMojo}.
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
package org.machanism.machai.maven;
