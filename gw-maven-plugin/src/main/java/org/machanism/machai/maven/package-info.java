/**
 * Maven plugin goals (Mojos) that integrate the MachAI Generative Workflow (GW) documentation pipeline
 * into a Maven build.
 *
 * <p>
 * The goals in this package scan documentation sources (typically under {@code src/site}), invoke the
 * GW processing pipeline, and optionally clean temporary artifacts produced by workflow runs.
 * </p>
 *
 * <h2>Goals</h2>
 * <ul>
 *   <li>
 *     {@link org.machanism.machai.maven.GW} (goal {@code gw}) &ndash; scans documentation sources and
 *     invokes {@link org.machanism.machai.gw.FileProcessor} to process discovered inputs.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.maven.Clean} (goal {@code clean}) &ndash; removes workflow temporary
 *     files for the current Maven module.
 *   </li>
 * </ul>
 *
 * <h2>Goal {@code gw} parameters</h2>
 * <p>
 * Parameters can be configured via {@code pom.xml} (plugin {@code &lt;configuration&gt;}) and/or via
 * system properties. Where applicable, the system property names are shown alongside each parameter.
 * </p>
 * <ul>
 *   <li>
 *     <b>{@code genai}</b> / <b>{@code gw.genai}</b> (optional): Provider/model identifier forwarded
 *     to the workflow (for example {@code OpenAI:gpt-5}).
 *   </li>
 *   <li>
 *     <b>{@code scanDir}</b> (optional): Explicit scan root; when not set, defaults to the module base
 *     directory.
 *   </li>
 *   <li>
 *     <b>{@code instructions}</b> / <b>{@code gw.instructions}</b> (optional): One or more instruction
 *     location strings consumed by the workflow.
 *   </li>
 *   <li>
 *     <b>{@code guidance}</b> / <b>{@code gw.guidance}</b> (optional): Default guidance text forwarded
 *     to the workflow.
 *   </li>
 *   <li>
 *     <b>{@code excludes}</b> / <b>{@code gw.excludes}</b> (optional): One or more exclude patterns or
 *     paths to skip during documentation scanning.
 *   </li>
 *   <li>
 *     <b>{@code serverId}</b> / <b>{@code gw.genai.serverId}</b> (required): Maven {@code settings.xml}
 *     {@code &lt;server&gt;} id used to load GenAI credentials.
 *   </li>
 *   <li>
 *     <b>{@code threads}</b> / <b>{@code gw.threads}</b> (optional, default {@code true}): Enables or
 *     disables multi-threaded document processing.
 *   </li>
 *   <li>
 *     <b>{@code logInputs}</b> / <b>{@code gw.logInputs}</b> (optional, default {@code false}): Logs the
 *     list of input files provided to the workflow.
 *   </li>
 * </ul>
 *
 * <h2>Credentials</h2>
 * <p>
 * GenAI credentials are loaded from Maven {@code settings.xml} using the {@code &lt;server&gt;} entry
 * whose id is provided by {@code gw.genai.serverId}. When present, credentials are exposed to the
 * workflow as configuration properties:
 * </p>
 * <ul>
 *   <li>{@code GENAI_USERNAME}</li>
 *   <li>{@code GENAI_PASSWORD}</li>
 * </ul>
 *
 * <h2>Usage examples</h2>
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
