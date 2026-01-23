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
 * Package providing {@link org.machanism.machai.bindex.builder.BindexBuilder} implementations that generate
 * {@link org.machanism.machai.schema.Bindex} documents for a project on disk by assembling project context and
 * invoking a {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The base builder defines the common pipeline for producing a {@code Bindex}:
 * <ul>
 *   <li>loads the Bindex JSON schema used for validation/constraints,</li>
 *   <li>optionally includes an existing (origin) Bindex to support incremental updates,</li>
 *   <li>collects project-specific context (manifest and relevant source/resource files),</li>
 *   <li>submits a request to the configured provider, and</li>
 *   <li>deserializes the provider response into a {@code Bindex} model.</li>
 * </ul>
 *
 * <p>Concrete implementations customize the collected context by overriding
 * {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()}.
 *
 * <h2>Typical usage</h2>
 * {@code
 * Bindex bindex = new MavenBindexBuilder(layout)
 *     .genAIProvider(provider)
 *     .build();
 * }
 */
package org.machanism.machai.bindex.builder;
