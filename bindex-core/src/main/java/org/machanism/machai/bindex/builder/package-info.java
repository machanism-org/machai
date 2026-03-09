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
 * Builds {@link org.machanism.machai.schema.Bindex} documents by collecting project context and prompting a
 * configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The package provides a small hierarchy of builders:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.BindexBuilder} assembles the overall prompt, optionally including
 *       an <em>origin</em> {@code Bindex} for incremental updates, and performs generation/deserialization.</li>
 *   <li>Specializations contribute ecosystem-specific context via {@code projectContext()} (for example Maven,
 *       JavaScript, or Python project layouts).</li>
 * </ul>
 *
 * <p>Typical usage:
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout, "openai", config)
 *     .origin(previousBindex)
 *     .build();
 * }</pre>
 */
package org.machanism.machai.bindex.builder;
