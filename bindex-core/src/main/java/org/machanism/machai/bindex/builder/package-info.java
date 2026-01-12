/**
 * Provides builders for generating Bindex documents for various programming project layouts.
 * <p>
 * This package includes specialized builder classes for Maven, JavaScript/TypeScript/Vue, and Python projects. Each builder analyzes the project layout and aggregates manifest, source files, and resources to create a comprehensive AI-driven Bindex document.
 * <p>
 * Typical usage involves constructing an appropriate builder with a project layout, setting a GenAIProvider, and invoking the {@code build()} method to automatically generate a Bindex from the project context.
 * <p>
 * Example workflow:
 * <pre>
 *     BindexBuilder builder = new MavenBindexBuilder(layout);
 *     builder.genAIProvider(provider);
 *     Bindex bindex = builder.build();
 * </pre>
 * <p>
 * See each builder class for specialized context aggregation and prompts for GenAI.
 *
 * Classes in this package:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.BindexBuilder}</li>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder}</li>
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder}</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder}</li>
 * </ul>
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
 * -  Update Javadoc with Code Changes:
 * 		- Revise Javadoc comments whenever code is modified to ensure documentation remains accurate and up to date.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
