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
 * Builders for generating {@link org.machanism.machai.schema.Bindex} documents for a project.
 *
 * <p>This package provides the {@link org.machanism.machai.bindex.builder.BindexBuilder} base type and concrete
 * builders for different project layouts. A builder is responsible for assembling a generation prompt (schema plus
 * project context), optionally merging an existing (origin) {@code Bindex} for incremental updates, delegating the
 * generation step to a configured {@link org.machanism.machai.ai.manager.GenAIProvider}, and then deserializing the
 * provider response into a {@code Bindex}.
 *
 * <h2>Provided implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: collects Maven build context from
 *       {@code pom.xml} and includes files from the Maven-configured source/resource/test directories.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: reads {@code package.json} and includes
 *       {@code .js}/{@code .ts}/{@code .vue} sources under {@code src}.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: reads {@code pyproject.toml} and includes
 *       files under a source directory inferred from {@code project.name}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout, "openai", config)
 *     .origin(previousBindex)
 *     .build();
 * }</pre>
 */
package org.machanism.machai.bindex.builder;
