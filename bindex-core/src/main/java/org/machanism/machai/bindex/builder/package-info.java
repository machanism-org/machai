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
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside <pre> tags, escape < and > as &lt; 
 *          and &gt; in <pre> content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Builders that generate {@link org.machanism.machai.schema.Bindex} documents by prompting a configured
 * {@link org.machanism.machai.ai.manager.GenAIProvider} with the Bindex JSON schema plus project-specific
 * context (such as manifest files and source/resource contents).
 *
 * <p>The package centers around {@link org.machanism.machai.bindex.builder.BindexBuilder}, which defines the
 * common generation workflow:
 * <ol>
 *   <li>prompt the Bindex JSON schema,</li>
 *   <li>optionally provide an origin {@code Bindex} to request an incremental update,</li>
 *   <li>add project context via a {@code projectContext()} implementation,</li>
 *   <li>perform the generation request and deserialize the response into a {@code Bindex} instance.</li>
 * </ol>
 *
 * <p>Specialized builders contribute ecosystem-specific context:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: prompts a sanitized Maven POM model and
 *       sources/resources declared by the Maven build configuration.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: prompts {@code package.json} and
 *       walks {@code src} to include {@code .js}/{@code .ts}/{@code .vue} files.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: prompts {@code pyproject.toml} and
 *       includes files under an inferred source directory based on {@code project.name}.</li>
 * </ul>
 *
 * <p>Typical usage:
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout)
 *     .genAIProvider(provider)
 *     .build();
 * }</pre>
 */
package org.machanism.machai.bindex.builder;
