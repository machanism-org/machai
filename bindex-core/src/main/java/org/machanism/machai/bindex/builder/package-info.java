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
 * Provides {@link org.machanism.machai.schema.Bindex} generation builders that assemble project context and prompt a
 * configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The core type is {@link org.machanism.machai.bindex.builder.BindexBuilder}, which constructs a prompt by:
 * <ol>
 *   <li>optionally including an existing {@code Bindex} (the <em>origin</em>) to request an incremental update,</li>
 *   <li>adding project-specific context supplied by {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()},</li>
 *   <li>invoking the provider to generate JSON, and</li>
 *   <li>deserializing the response into a {@code Bindex}.</li>
 * </ol>
 *
 * <p>Specialized builders tailor the context collection for specific project ecosystems:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: reads and sanitizes {@code pom.xml}, then
 *       includes files from Maven build source/resource/test directories.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: reads {@code package.json} and includes
 *       {@code .js}, {@code .ts}, and {@code .vue} files under {@code src}.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: reads {@code pyproject.toml} and includes
 *       files under a source directory inferred from {@code project.name}.</li>
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
