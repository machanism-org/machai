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
 * Generates {@link org.machanism.machai.schema.Bindex} documents for a project on disk by collecting build-manifest and
 * source context and prompting a configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The central abstraction is {@link org.machanism.machai.bindex.builder.BindexBuilder}, which orchestrates a build by:
 * <ol>
 *   <li>prompting the Bindex JSON schema,</li>
 *   <li>optionally including an existing (origin) Bindex for incremental updates,</li>
 *   <li>adding project context (implemented by subclasses), and</li>
 *   <li>performing generation and deserializing the provider output to a {@code Bindex}.</li>
 * </ol>
 *
 * <p>Concrete builders included here add ecosystem-specific context:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} (reads {@code pom.xml} and Maven source/resources),</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} (reads {@code package.json} and {@code src} tree),</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} (reads {@code pyproject.toml} and inferred sources).</li>
 * </ul>
 *
 * <p>To support another ecosystem, extend {@link org.machanism.machai.bindex.builder.BindexBuilder} and override
 * {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()} to prompt the relevant manifest(s) and file
 * contents.
 */
package org.machanism.machai.bindex.builder;
