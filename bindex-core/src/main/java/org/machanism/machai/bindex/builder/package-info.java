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
 * Builders that assemble a {@link org.machanism.machai.schema.Bindex} by prompting a configured
 * {@link org.machanism.machai.ai.manager.GenAIProvider} with a JSON schema and project-specific context.
 *
 * <p>The primary workflow is implemented by {@link org.machanism.machai.bindex.builder.BindexBuilder}:
 * <ol>
 *   <li>prompt the Bindex JSON schema,</li>
 *   <li>optionally include an origin {@code Bindex} for incremental updates,</li>
 *   <li>add project context (manifests and source/resource files),</li>
 *   <li>execute generation and deserialize the provider output into a {@code Bindex}.</li>
 * </ol>
 *
 * <p>Specialized builders provide ecosystem-specific context:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} for Maven projects (POM model and
 *       files configured in the Maven build section),</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} for JavaScript/TypeScript/Vue
 *       projects (reads {@code package.json} and prompts sources under {@code src}),</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} for Python projects (reads
 *       {@code pyproject.toml} and prompts inferred sources).</li>
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
