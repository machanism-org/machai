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
		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
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
 * Maven plugin goals for AI-assisted project assembly.
 *
 * <p>
 * This package provides the Maven {@code Mojo} entry points that integrate the MachAI assembly workflow into a
 * Maven build. The main goal reads or prompts for an assembly description, obtains library recommendations from a
 * configured generative-AI provider, and then applies the selected assembly steps to the current project directory.
 * </p>
 *
 * <h2>Typical flow</h2>
 * <ol>
 *   <li>Load an assembly prompt from {@code assembly.prompt.file} (default {@code project.txt}) or prompt the user.</li>
 *   <li>Run the picker model ({@code pick.genai}) to recommend candidate libraries (optionally filtered by score).</li>
 *   <li>Run the assembly model ({@code assembly.genai}) to update the project in {@code ${basedir}}.</li>
 * </ol>
 *
 * <h2>Usage</h2>
 * <p><strong>Command line:</strong></p>
 * <pre>
 * mvn org.machanism.machai:assembly-maven-plugin:assembly \
 *   -Dassembly.genai=OpenAI:gpt-5 \
 *   -Dpick.genai=OpenAI:gpt-5-mini \
 *   -Dassembly.prompt.file=project.txt
 * </pre>
 */
package org.machanism.machai.maven;
