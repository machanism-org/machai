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
 * 		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Builders that generate {@link org.machanism.machai.schema.Bindex} documents by inspecting a project on disk and
 * prompting a configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The core entry point is {@link org.machanism.machai.bindex.builder.BindexBuilder}, which prompts the Bindex JSON
 * schema, optionally includes an origin Bindex for incremental updates, delegates project discovery to
 * {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()}, and deserializes the provider output into a
 * {@code Bindex} instance.
 *
 * <p>Concrete builders provide ecosystem-specific context:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: reads a Maven {@code pom.xml} model, prompts a
 *       sanitized POM representation, and prompts files from the configured build/source/resource/test directories.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: prompts {@code package.json} and prompts all
 *       {@code .js}, {@code .ts}, and {@code .vue} files under the {@code src} directory.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: prompts {@code pyproject.toml}, infers a source
 *       directory from {@code project.name}, and prompts files located directly under that directory.</li>
 * </ul>
 *
 * <p>To add support for another ecosystem, extend {@link org.machanism.machai.bindex.builder.BindexBuilder} and override
 * {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()} to prompt the relevant manifests and source
 * files.
 */
package org.machanism.machai.bindex.builder;
