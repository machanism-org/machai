/**
 * Maven plugin mojos for integrating the MachAI generative-workflow (GW) document-processing pipeline into a Maven
 * build.
 *
 * <p>
 * This package provides two goals:
 * </p>
 * <ul>
 *   <li>
 *     {@code gw} ({@link org.machanism.machai.maven.GW}) scans documentation sources and runs the MachAI GW workflow.
 *     This goal is an aggregator mojo and executes once per reactor build.
 *   </li>
 *   <li>
 *     {@code clean} ({@link org.machanism.machai.maven.Clean}) deletes temporary artifacts created by prior workflow
 *     runs.
 *   </li>
 * </ul>
 *
 * <h2>Goal: {@code gw}</h2>
 * <p>
 * The {@code gw} goal starts at the module base directory (typically {@code ${basedir}}), scans for documentation
 * content supported by the workflow, and delegates processing to
 * {@link org.machanism.machai.gw.FileProcessor}. When executed in a multi-module build, it is declared as an
 * aggregator mojo and coordinates processing across the reactor.
 * </p>
 *
 * <h3>Credentials</h3>
 * <p>
 * The {@code gw} goal reads GenAI credentials from Maven {@code settings.xml} using a {@code &lt;server&gt;} entry
 * identified by the required parameter {@code gw.genai.serverId}. When present, credentials are exposed to the
 * workflow via system properties:
 * </p>
 * <ul>
 *   <li>{@code GENAI_USERNAME}</li>
 *   <li>{@code GENAI_PASSWORD}</li>
 * </ul>
 *
 * <h3>Parameters</h3>
 * <ul>
 *   <li>
 *     <b>{@code gw.genai}</b> (optional): GenAI provider/model identifier forwarded to the workflow (for example
 *     {@code OpenAI:gpt-5}).
 *   </li>
 *   <li>
 *     <b>{@code gw.instructions}</b> (optional): One or more instruction location strings consumed by the workflow.
 *   </li>
 *   <li>
 *     <b>{@code gw.excludes}</b> (optional): One or more exclude patterns/paths that are skipped during scanning.
 *   </li>
 *   <li>
 *     <b>{@code gw.genai.serverId}</b> (required): Maven {@code settings.xml} {@code &lt;server&gt;} id used to read
 *     credentials.
 *   </li>
 *   <li>
 *     <b>{@code gw.threads}</b> (optional, default {@code true}): Enables/disables multi-threaded document processing.
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
 * &amp;lt;plugin&amp;gt;
 *   &amp;lt;groupId&amp;gt;org.machanism.machai&amp;lt;/groupId&amp;gt;
 *   &amp;lt;artifactId&amp;gt;gw-maven-plugin&amp;lt;/artifactId&amp;gt;
 *   &amp;lt;version&amp;gt;${project.version}&amp;lt;/version&amp;gt;
 *   &amp;lt;configuration&amp;gt;
 *     &amp;lt;genai&amp;gt;OpenAI:gpt-5&amp;lt;/genai&amp;gt;
 *     &amp;lt;serverId&amp;gt;genai&amp;lt;/serverId&amp;gt;
 *     &amp;lt;threads&amp;gt;true&amp;lt;/threads&amp;gt;
 *     &amp;lt;instructions&amp;gt;
 *       &amp;lt;instruction&amp;gt;src/site/machai/instructions.md&amp;lt;/instruction&amp;gt;
 *     &amp;lt;/instructions&amp;gt;
 *     &amp;lt;excludes&amp;gt;
 *       &amp;lt;exclude&amp;gt;node_modules&amp;lt;/exclude&amp;gt;
 *     &amp;lt;/excludes&amp;gt;
 *   &amp;lt;/configuration&amp;gt;
 * &amp;lt;/plugin&amp;gt;
 * </pre>
 *
 * <h2>Goal: {@code clean}</h2>
 * <p>
 * The {@code clean} goal deletes temporary files created by earlier workflow runs (see
 * {@link org.machanism.machai.gw.FileProcessor#deleteTempFiles(java.io.File)}).
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
