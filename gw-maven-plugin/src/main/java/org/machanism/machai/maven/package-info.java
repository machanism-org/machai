/**
 * Maven plugin goals for integrating MachAI generative-workflow (GW) document processing into a Maven build.
 *
 * <p>
 * This package provides Maven Mojos that can be invoked from the command line or bound to Maven lifecycle phases.
 * The goals run the MachAI documentation workflow over configured documentation sources and can also remove
 * workflow artifacts produced during processing.
 * </p>
 *
 * <h2>Goals</h2>
 * <ul>
 *   <li><b>{@code gw}</b> – scans configured documentation sources and runs the MachAI document workflow, optionally
 *   using a configured GenAI provider/model.</li>
 *   <li><b>{@code clean}</b> – deletes temporary workflow artifacts generated during document processing.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 *
 * <h3>Run from the command line</h3>
 * <pre>
 * mvn org.machanism.machai:gw-maven-plugin:gw -Dgw.genai=OpenAI:gpt-5
 * </pre>
 *
 * <h3>Configure in {@code pom.xml}</h3>
 * <pre>
 * &amp;lt;plugin&amp;gt;
 *   &amp;lt;groupId&amp;gt;org.machanism.machai&amp;lt;/groupId&amp;gt;
 *   &amp;lt;artifactId&amp;gt;gw-maven-plugin&amp;lt;/artifactId&amp;gt;
 *   &amp;lt;version&amp;gt;${project.version}&amp;lt;/version&amp;gt;
 *   &amp;lt;configuration&amp;gt;
 *     &amp;lt;chatModel&amp;gt;OpenAI:gpt-5&amp;lt;/chatModel&amp;gt;
 *   &amp;lt;/configuration&amp;gt;
 *   &amp;lt;executions&amp;gt;
 *     &amp;lt;execution&amp;gt;
 *       &amp;lt;goals&amp;gt;
 *         &amp;lt;goal&amp;gt;gw&amp;lt;/goal&amp;gt;
 *         &amp;lt;goal&amp;gt;clean&amp;lt;/goal&amp;gt;
 *       &amp;lt;/goals&amp;gt;
 *     &amp;lt;/execution&amp;gt;
 *   &amp;lt;/executions&amp;gt;
 * &amp;lt;/plugin&amp;gt;
 * </pre>
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
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, convert the code content to use the Javadoc `{@code ...}`inline tag instead. Ensure that the code is properly escaped and formatted for Javadoc. Only replace the code inside `<pre>` tags with `{@code ...}`; do not alter other content. `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
