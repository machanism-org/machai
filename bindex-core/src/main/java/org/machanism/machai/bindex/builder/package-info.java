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
 
 */

/**
 * Generates {@link org.machanism.machai.schema.Bindex} documents by collecting a target project's manifest and
 * relevant source files and then prompting a {@link org.machanism.machai.ai.manager.GenAIProvider} to emit JSON
 * that conforms to the Bindex schema.
 *
 * <p>This package provides a base {@link org.machanism.machai.bindex.builder.BindexBuilder} and concrete builder
 * implementations for different ecosystems. Each builder:
 * <ol>
 *   <li>Configures the provider with schema/system instructions.</li>
 *   <li>Optionally supplies an existing ("origin") Bindex as update context.</li>
 *   <li>Collects project context (manifest plus selected source/resource files) and appends it to the prompt.</li>
 *   <li>Invokes the provider and deserializes the generated JSON into a {@code Bindex} instance.</li>
 * </ol>
 *
 * <h2>Builder implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}: uses {@code pom.xml} and Maven build
 *       directories/resources to provide context.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}: uses {@code package.json} and walks the
 *       {@code src/} tree for {@code .js}, {@code .ts}, and {@code .vue} files.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}: uses {@code pyproject.toml} and includes
 *       top-level module files from the resolved project package directory.</li>
 * </ul>
 */
package org.machanism.machai.bindex.builder;
