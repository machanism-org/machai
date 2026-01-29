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
 * Builds {@link org.machanism.machai.schema.Bindex} documents for a project by collecting relevant manifests and
 * files and invoking a configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The primary entry point is {@link org.machanism.machai.bindex.builder.BindexBuilder}, which defines a common
 * generation pipeline:
 * <ol>
 *   <li>define the expected {@code Bindex} output shape (prompt/schema),</li>
 *   <li>optionally provide an existing {@code Bindex} for incremental updates,</li>
 *   <li>assemble project context (manifests plus discovered source/resource/test files),</li>
 *   <li>submit the request and deserialize the response into a {@code Bindex}.</li>
 * </ol>
 *
 * <p>Concrete builders specialize context discovery for particular ecosystems, for example:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} uses an effective Maven model to gather the POM
 *       and the builds configured source/resource/test directories.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} uses {@code package.json} and common
 *       JavaScript/TypeScript project layouts to discover relevant files.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} uses {@code pyproject.toml} and an inferred
 *       source directory to discover relevant files.</li>
 * </ul>
 *
 * <p>To support an additional project type, subclass {@link org.machanism.machai.bindex.builder.BindexBuilder} and
 * override {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()} to contribute the appropriate
 * files and metadata.
 */
package org.machanism.machai.bindex.builder;
