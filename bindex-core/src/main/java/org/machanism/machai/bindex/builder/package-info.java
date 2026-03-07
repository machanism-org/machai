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
 * Builders for generating {@link org.machanism.machai.schema.Bindex} documents by collecting project context and
 * prompting a configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The base type, {@link org.machanism.machai.bindex.builder.BindexBuilder}, assembles a prompt that can include:
 * <ul>
 *   <li>an optional <em>origin</em> {@code Bindex} to request an incremental update,</li>
 *   <li>package-specific project context produced by {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()},</li>
 *   <li>a generation instruction that asks the provider to return JSON conforming to the Bindex schema.</li>
 * </ul>
 *
 * <p>Concrete builders tailor context collection for different ecosystems:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: scans Maven build source/resource/test paths
 *       and includes a sanitized {@code pom.xml} model.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: includes {@code package.json} and
 *       JavaScript/TypeScript/Vue sources under {@code src}.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: includes {@code pyproject.toml} and files
 *       from a source directory inferred from {@code project.name}.</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout, "openai", config)
 *     .origin(previousBindex)
 *     .build();
 * }</pre>
 */
package org.machanism.machai.bindex.builder;
