/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */

/**
 * Builders that assemble project context and use a {@link org.machanism.machai.ai.manager.GenAIProvider} to
 * generate {@link org.machanism.machai.schema.Bindex} documents.
 *
 * <p>This package provides a small hierarchy centered on {@link org.machanism.machai.bindex.builder.BindexBuilder}.
 * A builder is configured with a {@link org.machanism.machai.project.layout.ProjectLayout} describing a target
 * project on disk and a {@link org.machanism.machai.ai.manager.GenAIProvider} responsible for prompt execution.
 * The builder gathers manifests and selected sources/resources, prompts the provider with the Bindex JSON schema
 * and project context, and deserializes the provider output into a {@code Bindex} model.
 *
 * <h2>Typical flow</h2>
 * <ol>
 *   <li>Construct a builder for a project layout.</li>
 *   <li>Configure the {@code GenAIProvider} via
 *       {@link org.machanism.machai.bindex.builder.BindexBuilder#genAIProvider(org.machanism.machai.ai.manager.GenAIProvider)}.</li>
 *   <li>Optionally seed the generation via
 *       {@link org.machanism.machai.bindex.builder.BindexBuilder#origin(org.machanism.machai.schema.Bindex)} to
 *       request an incremental update.</li>
 *   <li>Call {@link org.machanism.machai.bindex.builder.BindexBuilder#build()}.</li>
 * </ol>
 *
 * <h2>Builder variants</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} prompts {@code pom.xml} and configured
 *       Maven source/resource/test directories.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} prompts {@code package.json} and walks
 *       {@code src} for {@code .js}/{@code .ts}/{@code .vue} files.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} prompts {@code pyproject.toml} and
 *       project sources derived from {@code project.name}.</li>
 * </ul>
 */
package org.machanism.machai.bindex.builder;
