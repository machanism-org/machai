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
 * Builders for generating {@link org.machanism.machai.schema.Bindex} documents from a projects on-disk structure and
 * build or packaging manifests.
 *
 * <p>This package provides the common {@link org.machanism.machai.bindex.builder.BindexBuilder} pipeline as well as
 * ecosystem-specific implementations that discover relevant inputs (for example, {@code pom.xml}, {@code package.json},
 * or {@code pyproject.toml}), collect source/resource/test files, and submit a request to a configured
 * {@link org.machanism.machai.ai.manager.GenAIProvider}. The providers response is deserialized into a {@code Bindex}
 * instance.
 *
 * <p>Typical usage starts by selecting an implementation and invoking its build operation (see
 * {@link org.machanism.machai.bindex.builder.BindexBuilder}). Concrete builders specialize project context discovery,
 * for example:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} uses the effective Maven model to locate the POM
 *       and the builds configured directories.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} uses {@code package.json} and common
 *       JavaScript/TypeScript layouts.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} uses {@code pyproject.toml} and inferred source
 *       locations.</li>
 * </ul>
 *
 * <p>To support an additional project type, subclass {@link org.machanism.machai.bindex.builder.BindexBuilder} and
 * override {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()} to contribute the appropriate
 * files and metadata.
 */
package org.machanism.machai.bindex.builder;
