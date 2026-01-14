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
 * Maven plugin Mojo(s) for AI-assisted project assembly.
 *
 * <p>
 * This package provides the {@link org.machanism.machai.maven.Assembly} Mojo, which exposes the {@code assembly}
 * goal. The goal reads an assembly prompt (from a configured file or via interactive input), uses a configured
 * generative-AI provider to recommend candidate libraries (optionally filtered by a minimum score), and then
 * invokes the project assembly workflow to apply the selected recommendations in the Maven base directory.
 * </p>
 *
 * <p><b>Example configuration:</b></p>
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.machanism.machai&lt;/groupId&gt;
 *   &lt;artifactId&gt;assembly-maven-plugin&lt;/artifactId&gt;
 *   &lt;configuration&gt;
 *     &lt;assembly.genai&gt;OpenAI:gpt-5&lt;/assembly.genai&gt;
 *     &lt;pick.genai&gt;OpenAI:gpt-5-mini&lt;/pick.genai&gt;
 *     &lt;assembly.prompt.file&gt;project.txt&lt;/assembly.prompt.file&gt;
 *     &lt;assembly.score&gt;0.8&lt;/assembly.score&gt;
 *     &lt;bindex.register.url&gt;https://register.project.example&lt;/bindex.register.url&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 */
package org.machanism.machai.maven;
