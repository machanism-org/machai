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
 * Builder implementations that generate {@link org.machanism.machai.schema.Bindex} documents for a project on disk by
 * collecting repository context (manifests and source files) and prompting a configured
 * {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The main abstraction is {@link org.machanism.machai.bindex.builder.BindexBuilder}. Its
 * {@link org.machanism.machai.bindex.builder.BindexBuilder#build()} method prompts:
 * <ol>
 *   <li>the Bindex JSON schema,</li>
 *   <li>an optional origin Bindex (for incremental updates),</li>
 *   <li>project-specific context contributed by {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()},</li>
 *   <li>a generation instruction, then deserializes the provider output into a {@code Bindex}.</li>
 * </ol>
 *
 * <p>This package contains concrete builders for common ecosystems:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: reads and sanitizes a Maven {@code pom.xml} model
 *       and prompts files from Maven build/source/resource/test directories.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: prompts {@code package.json} and prompts
 *       JavaScript/TypeScript/Vue source files under {@code src}.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: prompts {@code pyproject.toml}, infers a source
 *       directory from {@code project.name}, and prompts regular files from that directory.</li>
 * </ul>
 *
 * <p>To support another build ecosystem, extend {@link org.machanism.machai.bindex.builder.BindexBuilder} and override
 * {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()} to add the appropriate manifest(s) and source
 * content via the provider.
 */
package org.machanism.machai.bindex.builder;
