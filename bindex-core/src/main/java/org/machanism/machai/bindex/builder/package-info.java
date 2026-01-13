/**
 * Builders for producing a {@link org.machanism.machai.schema.Bindex} from a
 * {@link org.machanism.machai.project.layout.ProjectLayout}.
 * <p>
 * This package provides a small build pipeline that:
 * </p>
 * <ol>
 *   <li>Extracts project context (such as manifests, sources, and resources) via a {@code ProjectLayout}.</li>
 *   <li>Optionally incorporates an existing/origin {@code Bindex} to support incremental updates.</li>
 *   <li>Uses a {@link org.machanism.machai.ai.manager.GenAIProvider}-backed prompt sequence to generate a final
 *       {@code Bindex} document.</li>
 * </ol>
 * <p>
 * The main entry point is {@link org.machanism.machai.bindex.builder.BindexBuilder}. Concrete implementations
 * provide layout-specific context for common ecosystems:
 * </p>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} for Maven/Java projects.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} for JavaScript/TypeScript/Vue projects.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} for Python projects.</li>
 * </ul>
 * <p>
 * Example:
 * </p>
 * <pre>
 *     MavenBindexBuilder builder = new MavenBindexBuilder(layout);
 *     builder.genAIProvider(provider);
 *     Bindex bindex = builder.build();
 * </pre>
 */
package org.machanism.machai.bindex.builder;

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
