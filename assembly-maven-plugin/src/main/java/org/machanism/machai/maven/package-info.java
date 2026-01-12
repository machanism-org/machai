/**
 * <p>
 * Provides Maven plugin support for intelligent AI-powered project assembly,
 * interactive dependency management, and streamlined setup workflows. The
 * <code>org.machanism.machai.maven</code> package contains classes such as Mojo
 * implementations and helpers for using generative AI models in the project initialization,
 * assembly, recommendation, and metadata registration tasks.
 * </p>
 *
 * <p><b>Main Features:</b></p>
 * <ul>
 *   <li>Defines Maven Mojo(s) to automate project assembly and dependency picking using AI chat models.</li>
 *   <li>Supports interactive configuration via command line and prompt files.</li>
 *   <li>Integrates with GenAI providers for library recommendation and score-based filtering.</li>
 *   <li>Registers project metadata and facilitates optimal model selection for different assembly and picking purposes.</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
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
 * <p>
 * Classes in this package include comprehensive Javadoc describing their roles, parameters, and
 * example usages for assembling and managing Java projects with AI-driven workflows.
 * </p>
 */
package org.machanism.machai.maven;

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

