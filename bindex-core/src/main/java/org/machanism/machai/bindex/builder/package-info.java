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
 * Builders for generating {@link org.machanism.machai.schema.Bindex} documents by prompting a configured
 * {@link org.machanism.machai.ai.manager.GenAIProvider} with the Bindex JSON schema and project-specific
 * context (for example build manifests and selected source/resource files).
 *
 * <p>The package is centered around {@link org.machanism.machai.bindex.builder.BindexBuilder}, which defines
 * a common workflow:
 * <ol>
 *   <li>Prompt the provider with the Bindex JSON schema.</li>
 *   <li>Optionally include an existing {@code Bindex} to request an incremental update.</li>
 *   <li>Add project context by prompting manifests and relevant files.</li>
 *   <li>Run the generation request and deserialize the provider response into a {@code Bindex}.</li>
 * </ol>
 *
 * <p>Concrete builders contribute ecosystem-specific context:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: reads and sanitizes {@code pom.xml} and
 *       prompts files declared by Maven build directories/resources.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: prompts {@code package.json} and scans
 *       {@code src} for {@code .js}, {@code .ts}, and {@code .vue} source files.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: prompts {@code pyproject.toml} and
 *       includes files under a source directory inferred from {@code project.name}.</li>
 * </ul>
 *
 * <p>Typical usage:
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout)
 *     .genAIProvider(provider)
 *     .build();
 * }</pre>
 */
package org.machanism.machai.bindex.builder;
