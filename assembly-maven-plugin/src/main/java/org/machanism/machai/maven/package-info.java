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
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
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
 * Maven plugin goal implementations for AI-assisted project assembly.
 *
 * <p>
 * This package contains the Maven {@code Mojo} entry points (goals) that integrate the MachAI assembly workflow
 * into a Maven build. The primary goal coordinates:
 * </p>
 *
 * <ol>
 *   <li>Loading the assembly prompt (from a configured file or user input).</li>
 *   <li>Invoking a configured generative-AI provider to propose candidate libraries and changes.</li>
 *   <li>Optionally filtering proposals using a minimum score threshold.</li>
 *   <li>Executing the assembly workflow against the current project directory.</li>
 * </ol>
 *
 * <h2>Usage</h2>
 *
 * <p><b>Command line:</b></p>
 * <pre>
 * mvn org.machanism.machai:assembly-maven-plugin:assembly \
 *   -Dassembly.genai=OpenAI:gpt-5 \
 *   -Dpick.genai=OpenAI:gpt-5-mini
 * </pre>
 *
 * <p><b>Plugin configuration:</b></p>
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
