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
 * Builders that generate {@link org.machanism.machai.schema.Bindex} documents for projects on disk.
 *
 * <p>This package provides implementations of {@link org.machanism.machai.bindex.builder.BindexBuilder} that collect
 * project context (for example, build manifests and source structure/content), then delegate to a configured
 * {@link org.machanism.machai.ai.manager.GenAIProvider} to produce a {@code Bindex} document.
 *
 * <p>A builder typically:
 * <ol>
 *   <li>creates a prompt that includes the Bindex JSON schema,</li>
 *   <li>optionally includes an existing (origin) Bindex for incremental updates,</li>
 *   <li>adds project-specific context such as manifest contents and source file summaries, and</li>
 *   <li>invokes the provider and deserializes the result into a {@code Bindex} instance.</li>
 * </ol>
 *
 * <p>Concrete builders add ecosystem-specific context, for example:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} (reads {@code pom.xml} and Maven layout),</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} (reads {@code package.json} and a
 *   JavaScript/TypeScript source tree),</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} (reads {@code pyproject.toml} and inferred
 *   sources).</li>
 * </ul>
 */
package org.machanism.machai.bindex.builder;
