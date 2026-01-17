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
 * GenAI-assisted builders for generating {@link org.machanism.machai.schema.Bindex} documents.
 *
 * <p>The classes in this package collect ecosystem-specific project context (manifest/build files and relevant
 * sources/resources), submit that context to a {@link org.machanism.machai.ai.manager.GenAIProvider}, and
 * deserialize the provider output into a {@code Bindex} model.
 *
 * <h2>Key responsibilities</h2>
 * <ul>
 *   <li>Provide the {@code Bindex} JSON schema to the provider (see
 *       {@link org.machanism.machai.bindex.builder.BindexBuilder#bindexSchemaPrompt(org.machanism.machai.ai.manager.GenAIProvider)}).</li>
 *   <li>Optionally seed generation from an existing {@code Bindex} to support incremental updates.</li>
 *   <li>Gather project context via {@link org.machanism.machai.project.layout.ProjectLayout} implementations and
 *       prompt the provider with manifests and selected source/resource files.</li>
 * </ul>
 *
 * <h2>Builder variants</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.BindexBuilder} - base orchestrator that drives prompting and
 *       parses the result.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} - adds Maven context by reading
 *       {@code pom.xml} and configured build/resource directories.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} - adds JavaScript/TypeScript/Vue context
 *       by reading {@code package.json} and walking the {@code src} tree.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} - adds Python context by reading
 *       {@code pyproject.toml} and prompting selected project sources.</li>
 * </ul>
 */
package org.machanism.machai.bindex.builder;
