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
 * Builders for generating {@link org.machanism.machai.schema.Bindex} documents from projects on disk.
 *
 * <p>The central entry point is {@link org.machanism.machai.bindex.builder.BindexBuilder}, which orchestrates
 * the prompt flow for a configured {@link org.machanism.machai.ai.manager.GenAIProvider}:
 * <ol>
 *   <li>prompt the Bindex JSON schema,</li>
 *   <li>optionally prompt an existing (origin) Bindex for incremental updates,</li>
 *   <li>prompt project context supplied by the builder implementation,</li>
 *   <li>trigger generation and parse the provider output into a {@code Bindex}.</li>
 * </ol>
 *
 * <p>Concrete builders enrich the prompt with ecosystem-specific context:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: reads the effective Maven model
 *       from {@code pom.xml}, prompts source/resources/test files based on the POM build configuration, and
 *       prompts a sanitized POM representation.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: prompts {@code package.json} and
 *       source files under {@code src} for JavaScript/TypeScript/Vue projects.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: prompts {@code pyproject.toml} and
 *       source files inferred from {@code project.name}.</li>
 * </ul>
 *
 * <p>Builders are typically created by a factory and configured with a provider before calling
 * {@link org.machanism.machai.bindex.builder.BindexBuilder#build()}.
 */
package org.machanism.machai.bindex.builder;
