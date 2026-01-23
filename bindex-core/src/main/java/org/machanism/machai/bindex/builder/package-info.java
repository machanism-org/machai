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
 * Builders that generate {@link org.machanism.machai.schema.Bindex} documents by assembling project context
 * and prompting a configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The base {@link org.machanism.machai.bindex.builder.BindexBuilder} implements the common pipeline:
 * <ol>
 *   <li>prompt the Bindex JSON schema,</li>
 *   <li>optionally include an origin {@code Bindex} to request an incremental update,</li>
 *   <li>collect project context (manifests and source/resource files),</li>
 *   <li>perform the generation request and deserialize the response.</li>
 * </ol>
 *
 * <p>Concrete builders tailor context collection for specific ecosystems:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} reads an effective Maven model,
 *       prompts a sanitized POM, and adds source/resource/test directories from the Maven build configuration.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} prompts {@code package.json} and
 *       adds JavaScript/TypeScript/Vue sources from {@code src}.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} prompts {@code pyproject.toml} and
 *       adds files from an inferred source directory based on {@code project.name}.</li>
 * </ul>
 *
 * <p>To add support for a new project type, subclass {@code BindexBuilder} and override
 * {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()}.
 */
package org.machanism.machai.bindex.builder;
