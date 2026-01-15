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
 * Builders that assemble project context (filesystem layout, dependency manifests, and optional prior output) and
 * orchestrate GenAI prompting to produce a {@link org.machanism.machai.schema.Bindex} model.
 *
 * <p>Typical responsibilities in this package include:
 * <ul>
 *   <li>collecting project metadata and manifests (for example {@code pom.xml}, {@code package.json},
 *       {@code pyproject.toml});</li>
 *   <li>walking relevant source/resource folders and streaming file contents to a
 *       {@link org.machanism.machai.ai.manager.GenAIProvider};</li>
 *   <li>optionally seeding generation from an existing {@code Bindex} to support incremental updates; and</li>
 *   <li>invoking the provider to obtain and deserialize the final {@code Bindex} document.</li>
 * </ul>
 *
 * <p>{@link org.machanism.machai.bindex.builder.BindexBuilder} is the main orchestrator. Concrete subclasses add
 * ecosystem-specific context gathering, such as:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} for Maven projects,</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} for JavaScript/TypeScript projects, and</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} for Python projects.</li>
 * </ul>
 */
package org.machanism.machai.bindex.builder;
