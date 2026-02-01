/*-
 * @guidance:
 * **IMPORTANT: UPDATE JAVADOC OF ALL JAVA CLASSES IN THE FOLDER AND THIS `package-info.java`!**
 *	
 * - All methods, classes, and fields must have Javadoc comments that clearly explain their purpose and usage.
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * - Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 *      		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc. 
 *          Ensure that the code is properly escaped and formatted for Javadoc.  *     - Generate javadoc with a description all maven plugin parameters and examples of usage. 
 * - **Use the Java version specified in the project's `pom.xml` for all test code and configuration.**
*/

/**
 * Maven plugin goal implementations for AI-assisted project assembly.
 *
 * <p>
 * This package contains the {@code assembly} goal implementation, {@link org.machanism.machai.maven.Assembly}.
 * The goal reads (or interactively requests) an assembly prompt, asks an AI-backed picker to recommend candidate
 * libraries, and then runs an assembly workflow that applies changes to the current Maven execution base
 * directory.
 * </p>
 *
 * <h2>Goal</h2>
 * <ul>
 *   <li>{@code assembly} &ndash; recommends libraries and applies an assembly workflow driven by a prompt.</li>
 * </ul>
 *
 * <h2>Configuration parameters</h2>
 * <p>
 * Parameters can be provided either as system properties (for example, {@code -Dassembly.genai=...}) and/or via
 * plugin configuration.
 * </p>
 * <ul>
 *   <li>{@code assembly.genai} &ndash; GenAI provider id for the assembly phase (default {@code OpenAI:gpt-5}).</li>
 *   <li>{@code pick.genai} &ndash; GenAI provider id for the recommendation/picker phase (default {@code OpenAI:gpt-5-mini}).</li>
 *   <li>{@code assembly.prompt.file} &ndash; Prompt file path (default {@code project.txt}); if missing, the prompt is requested interactively.</li>
 *   <li>{@code assembly.score} &ndash; Minimum score threshold for recommended libraries (default {@code 0.9}).</li>
 *   <li>{@code bindex.register.url} &ndash; Optional registration/lookup endpoint used by the picker.</li>
 * </ul>
 *
 * <h2>Usage examples</h2>
 * <p><b>Command line:</b></p>
 * <pre>
 * mvn org.machanism.machai:assembly-maven-plugin:assembly \
 *   -Dassembly.genai=OpenAI:gpt-5 \
 *   -Dpick.genai=OpenAI:gpt-5-mini \
 *   -Dassembly.prompt.file=project.txt \
 *   -Dassembly.score=0.9
 * </pre>
 *
 * <p><b>POM configuration:</b></p>
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.machanism.machai&lt;/groupId&gt;
 *   &lt;artifactId&gt;assembly-maven-plugin&lt;/artifactId&gt;
 *   &lt;configuration&gt;
 *     &lt;assembly.genai&gt;OpenAI:gpt-5&lt;/assembly.genai&gt;
 *     &lt;pick.genai&gt;OpenAI:gpt-5-mini&lt;/pick.genai&gt;
 *     &lt;assembly.prompt.file&gt;project.txt&lt;/assembly.prompt.file&gt;
 *     &lt;assembly.score&gt;0.9&lt;/assembly.score&gt;
 *     &lt;bindex.register.url&gt;https://register.project.example&lt;/bindex.register.url&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 */
package org.machanism.machai.maven;
