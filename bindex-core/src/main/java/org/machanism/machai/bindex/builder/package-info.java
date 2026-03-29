/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
 * Builders that generate {@link org.machanism.machai.schema.Bindex} documents by assembling a project-specific prompt
 * and delegating generation to a configured {@link org.machanism.machai.ai.manager.Genai} provider.
 *
 * <p>
 * The central type is {@link org.machanism.machai.bindex.builder.BindexBuilder}, which:
 * <ul>
 *   <li>optionally incorporates a prior (origin) {@code Bindex} to request incremental updates,</li>
 *   <li>collects project context contributed by subclasses (manifest/build files and selected source trees),</li>
 *   <li>submits a generation request, and</li>
 *   <li>parses the provider response back into a {@code Bindex} instance.</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: reads {@code pom.xml} and includes files from
 *       Maven build directories (sources and resources).</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: reads {@code package.json} and includes
 *       {@code .js}, {@code .ts}, and {@code .vue} sources under {@code src}.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: reads {@code pyproject.toml} and includes
 *       files under a source directory inferred from {@code project.name}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout, "openai", config)
 *     .origin(previousBindex)
 *     .logInputs(true)
 *     .build();
 * }</pre>
 */
package org.machanism.machai.bindex.builder;
