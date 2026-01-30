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
 * Builders for generating {@link org.machanism.machai.schema.Bindex} documents by inspecting an on-disk project and
 * prompting a configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The package provides a base {@link org.machanism.machai.bindex.builder.BindexBuilder} that:
 * <ul>
 *   <li>prompts the Bindex JSON schema,</li>
 *   <li>optionally requests an incremental update from an existing (origin) Bindex,</li>
 *   <li>delegates project-specific discovery to {@code projectContext()}, and</li>
 *   <li>deserializes the provider output back into a {@code Bindex}.</li>
 * </ul>
 *
 * <p>Concrete builders supply ecosystem-specific context:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} reads a Maven {@code pom.xml}, collects the
 *       configured sources/resources/tests, and prompts a sanitized POM representation.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} reads {@code package.json} and prompts all
 *       {@code .js}, {@code .ts}, and {@code .vue} files under {@code src}.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} reads {@code pyproject.toml}, infers a source
 *       directory from {@code project.name}, and prompts regular files in that directory.</li>
 * </ul>
 *
 * <p>To add support for another ecosystem, extend {@link org.machanism.machai.bindex.builder.BindexBuilder} and override
 * {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()} to prompt the relevant manifests and
 * project files.
 */
package org.machanism.machai.bindex.builder;
