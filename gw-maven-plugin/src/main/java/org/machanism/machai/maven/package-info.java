/**
 * Maven plugin goals that integrate the MachAI generative-workflow (GW) document-processing pipeline into a Maven build.
 *
 * <p>
 * This package provides two Mojos:
 * </p>
 * <ul>
 *   <li>
 *     {@code gw} ({@link org.machanism.machai.maven.GW}) scans documentation sources under the current module base
 *     directory (typically {@code ${basedir}}) and runs the workflow via {@link org.machanism.machai.gw.FileProcessor}.
 *   </li>
 *   <li>
 *     {@code clean} ({@link org.machanism.machai.maven.Clean}) deletes temporary artifacts created by prior workflow runs
 *     (typically as part of the Maven {@code clean} phase).
 *   </li>
 * </ul>
 *
 * <h2>Goals and configuration</h2>
 *
 * <p>
 * Goals may be configured either via plugin configuration in {@code pom.xml} or via Java system properties passed on the
 * Maven command line.
 * </p>
 *
 * <h3>{@code gw}</h3>
 * <p>
 * The {@code gw} goal delegates discovery and processing to {@link org.machanism.machai.gw.FileProcessor}.
 * </p>
 *
 * <h4>Parameters</h4>
 * <ul>
 *   <li>
 *     <b>{@code genai}</b> / {@code -Dgw.genai=...} (optional): GenAI provider/model identifier forwarded to the workflow
 *     (for example {@code OpenAI:gpt-5}).
 *   </li>
 *   <li>
 *     <b>{@code instructions}</b> / {@code -Dgw.instructions=...} (optional): One or more instruction location strings
 *     consumed by the workflow.
 *   </li>
 *   <li>
 *     <b>{@code serverId}</b> / {@code -Dgw.genai.serverId=...} (required): Maven {@code settings.xml} {@code &lt;server&gt;}
 *     id used to read credentials for the GenAI provider. If present, the server's {@code username} and {@code password}
 *     are exposed to the workflow via the {@code GENAI_USERNAME} and {@code GENAI_PASSWORD} system properties.
 *   </li>
 *   <li>
 *     <b>{@code threads}</b> / {@code -Dgw.threads=true|false} (optional, default {@code true}): Enables/disables
 *     multi-threaded document processing.
 *   </li>
 * </ul>
 *
 * <h4>Usage examples</h4>
 *
 * <p>Run from the command line:</p>
 * <pre>
 * mvn org.machanism.machai:gw-maven-plugin:gw -Dgw.genai=OpenAI:gpt-5 -Dgw.genai.serverId=genai
 * </pre>
 *
 * <p>Configure in {@code pom.xml}:</p>
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.machanism.machai&lt;/groupId&gt;
 *   &lt;artifactId&gt;gw-maven-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;${project.version}&lt;/version&gt;
 *   &lt;configuration&gt;
 *     &lt;genai&gt;OpenAI:gpt-5&lt;/genai&gt;
 *     &lt;serverId&gt;genai&lt;/serverId&gt;
 *     &lt;threads&gt;true&lt;/threads&gt;
 *     &lt;instructions&gt;
 *       &lt;instruction&gt;src/site/machai/instructions.md&lt;/instruction&gt;
 *     &lt;/instructions&gt;
 *   &lt;/configuration&gt;
 *   &lt;executions&gt;
 *     &lt;execution&gt;
 *       &lt;goals&gt;
 *         &lt;goal&gt;gw&lt;/goal&gt;
 *         &lt;goal&gt;clean&lt;/goal&gt;
 *       &lt;/goals&gt;
 *     &lt;/execution&gt;
 *   &lt;/executions&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * <h3>{@code clean}</h3>
 * <p>
 * The {@code clean} goal deletes temporary files created by earlier workflow runs (see
 * {@link org.machanism.machai.gw.FileProcessor#deleteTempFiles(java.io.File)}). This goal has no user-configurable
 * parameters.
 * </p>
 */
package org.machanism.machai.maven;

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
 * 		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc. 
 *          Ensure that the code is properly escaped and formatted for Javadoc.  *     - Generate javadoc with a description all maven plugin parameters and examples of usage. 
 * - **Use the Java version specified in the project's `pom.xml` for all test code and configuration.**
*/
