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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose and usage.
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
 * Maven plugin goal for running an AI-assisted project assembly workflow.
 *
 * <p>
 * This package contains the {@link org.machanism.machai.maven.Assembly} Maven {@code Mojo}, which implements the
 * {@code assembly} goal. The goal reads (or prompts for) a project description, uses an AI-powered picker to
 * recommend libraries, and then runs an AI-driven assembly process against the Maven execution base directory.
 * </p>
 *
 * <h2>Workflow</h2>
 * <ol>
 *   <li>Acquire an assembly prompt from {@code project.txt} (by default) or request it interactively.</li>
 *   <li>Use a picker model to recommend libraries (as {@link org.machanism.machai.schema.Bindex} entries).</li>
 *   <li>Filter recommendations by score threshold.</li>
 *   <li>Run the application assembly workflow against the Maven execution base directory.</li>
 * </ol>
 *
 * <h2>Goal</h2>
 * <ul>
 *   <li>{@code assembly} &ndash; Recommend libraries and run an AI-driven assembly process against a project folder.</li>
 * </ul>
 *
 * <h2>Plugin parameters</h2>
 * <p>
 * Parameters may be supplied via system properties (for example, {@code -Dassembly.genai=...}) and/or via Maven
 * plugin configuration.
 * </p>
 * <ul>
 *   <li>{@code assembly.genai} (default {@code OpenAI:gpt-5}) &ndash; GenAI provider id for the assembly phase.</li>
 *   <li>{@code pick.genai} (default {@code OpenAI:gpt-5-mini}) &ndash; GenAI provider id for the recommendation
 *   (picker) phase.</li>
 *   <li>{@code assembly.prompt.file} (default {@code project.txt}) &ndash; Prompt file path; if missing, the prompt is
 *   requested interactively.</li>
 *   <li>{@code assembly.score} (default {@code 0.9}) &ndash; Minimum score threshold for recommended libraries.</li>
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
