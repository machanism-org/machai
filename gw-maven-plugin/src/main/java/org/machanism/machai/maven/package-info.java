/**
 * Maven plugin mojos that integrate the MachAI generative-workflow (GW) document-processing pipeline into a Maven
 * build.
 *
 * <p>
 * The package provides two goals:
 * </p>
 * <ul>
 *   <li>
 *     {@code gw} ({@link org.machanism.machai.maven.GW}): scans the current module's base directory (typically
 *     {@code ${basedir}}) for documentation sources and runs the GW workflow.
 *   </li>
 *   <li>
 *     {@code clean} ({@link org.machanism.machai.maven.Clean}): deletes temporary artifacts created by prior workflow
 *     runs.
 *   </li>
 * </ul>
 *
 * <h2>Goal: {@code gw}</h2>
 * <p>
 * The {@code gw} goal reads GenAI credentials from Maven {@code settings.xml} using the configured
 * {@code &lt;server&gt;} entry and exposes them to the workflow via system properties:
 * </p>
 * <ul>
 *   <li>{@code GENAI_USERNAME}</li>
 *   <li>{@code GENAI_PASSWORD}</li>
 * </ul>
 *
 * <h3>Configuration parameters</h3>
 * <ul>
 *   <li>
 *     <b>{@code genai}</b> / {@code -Dgw.genai=...} (optional): GenAI provider/model identifier forwarded to the
 *     workflow (for example {@code OpenAI:gpt-5}).
 *   </li>
 *   <li>
 *     <b>{@code instructions}</b> / {@code -Dgw.instructions=...} (optional): One or more instruction location strings
 *     consumed by the workflow.
 *   </li>
 *   <li>
 *     <b>{@code serverId}</b> / {@code -Dgw.genai.serverId=...} (required): Maven {@code settings.xml}
 *     {@code &lt;server&gt;} id used to read credentials for the GenAI provider.
 *   </li>
 *   <li>
 *     <b>{@code threads}</b> / {@code -Dgw.threads=true|false} (optional, default {@code true}): Enables or disables
 *     multi-threaded document processing.
 *   </li>
 * </ul>
 *
 * <h3>Usage examples</h3>
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
 * &lt;/plugin&gt;
 * </pre>
 *
 * <h2>Goal: {@code clean}</h2>
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
