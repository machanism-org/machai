/**
 * Maven plugin goals and shared infrastructure for generating, updating, and publishing a
 * Bindex index as part of a Maven build.
 *
 * <p>
 * The classes in this package are implemented as Mojos (Maven goals). Each goal extends
 * {@link org.machanism.machai.maven.AbstractBindexMojo}, which provides access to the current
 * {@code MavenProject}, resolves the configured GenAI provider, and delegates index generation to
 * {@code BindexCreator} using a {@code MavenProjectLayout}.
 * </p>
 *
 * <h2>Provided goals</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.maven.Create} &mdash; generate a new index for the current project.</li>
 *   <li>{@link org.machanism.machai.maven.Update} &mdash; regenerate/update an existing index.</li>
 *   <li>{@link org.machanism.machai.maven.Register} &mdash; publish/register the generated index/metadata.</li>
 *   <li>{@link org.machanism.machai.maven.Clean} &mdash; remove plugin-generated output.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 * {@code
 * mvn org.machanism.machai:bindex-maven-plugin:create
 * mvn org.machanism.machai:bindex-maven-plugin:update
 * mvn org.machanism.machai:bindex-maven-plugin:register
 * mvn org.machanism.machai:bindex-maven-plugin:clean
 * }
 * </pre>
 */
package org.machanism.machai.maven;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
