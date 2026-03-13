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
 * Builds {@link org.machanism.machai.schema.Bindex} documents by extracting project context and prompting a
 * configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>The core entry point is {@link org.machanism.machai.bindex.builder.BindexBuilder}, which assembles a prompt
 * containing project context and generation instructions, invokes a provider, and deserializes the returned JSON
 * into a {@code Bindex} instance. Implementations specialize the context gathering step for particular ecosystems
 * (for example, reading manifests/build descriptors and including relevant source/resource files).
 *
 * <h2>Key concepts</h2>
 * <ul>
 *   <li><strong>Origin Bindex</strong>: an optional existing {@code Bindex} that can be supplied to request an
 *       incremental update rather than a full regeneration.</li>
 *   <li><strong>Project context</strong>: a text payload assembled from build files and selected project files,
 *       produced by {@link org.machanism.machai.bindex.builder.BindexBuilder#projectContext()}.</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: reads {@code pom.xml}, walks Maven-configured
 *       source/resource/test directories, and includes a sanitized POM representation.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: reads {@code package.json} and walks the
 *       {@code src} tree for {@code .js}/{@code .ts}/{@code .vue} sources.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: reads {@code pyproject.toml}, infers a
 *       module directory from {@code project.name}, and includes files from that directory.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout, "openai", config)
 *     .origin(previousBindex)
 *     .build();
 * }</pre>
 */
package org.machanism.machai.bindex.builder;
