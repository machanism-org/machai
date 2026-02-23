/**
 * Maven plugin goals (Mojos) and shared infrastructure for running MachAI Ghostwriter guided workflow (GW)
 * document processing as part of a Maven build.
 *
 * <p>
 * The goals in this package configure a {@link org.machanism.machai.gw.processor.FileProcessor} and then scan a
 * documentation source tree (commonly {@code src/site}). The scan root is typically Maven's execution root directory
 * but can be overridden with {@code -Dgw.scanDir}. During scanning, include/exclude behavior is applied by the
 * processor and, if enabled, the plugin forwards credentials from Maven {@code settings.xml}.
 * </p>
 *
 * <h2>Goals</h2>
 * <ul>
 *   <li>
 *     {@link org.machanism.machai.gw.maven.GW} ({@code gw:gw})
 *     - Aggregator goal that can run without a {@code pom.xml}. It processes modules in reverse order (sub-modules
 *     first, then parent modules), similar to the Ghostwriter CLI.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.maven.ReactorGW} ({@code gw:reactor})
 *     - Processes modules using standard Maven reactor dependency ordering, with an option to defer processing of the
 *     execution-root project.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.maven.Clean} ({@code gw:clean})
 *     - Deletes temporary artifacts created by GW processing (typically bound to Maven's {@code clean} lifecycle).
 *   </li>
 * </ul>
 *
 * <h2>Shared infrastructure</h2>
 * <ul>
 *   <li>
 *     {@link org.machanism.machai.gw.maven.AbstractGWGoal}
 *     - Base class defining shared parameters (for example, instructions, default guidance, scan directory, excludes,
 *     and optional credentials lookup) and the common scan/execute flow.
 *   </li>
 * </ul>
 *
 * <h2>Configuration and credentials</h2>
 * <p>
 * In addition to standard Maven plugin parameters, GenAI credentials can optionally be sourced from
 * {@code ~/.m2/settings.xml} by providing {@code -Dgw.genai.serverId=&lt;serverId&gt;}. When configured,
 * the goal reads the matching {@code &lt;server&gt;} credentials and forwards them to the workflow.
 * </p>
 *
 * <h2>Usage examples</h2>
 * <pre>
 * mvn gw:gw
 * </pre>
 * <pre>
 * mvn gw:reactor -Dgw.scanDir=src\\site
 * </pre>
 */
package org.machanism.machai.gw.maven;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 *
 * - Update Existing Javadoc and Add Missing Javadoc:
 *      - Review all classes in the folder.
 *      - Update any existing Javadoc to ensure it is accurate, comprehensive, and follows best practices.
 *      - Add Javadoc to any classes, methods, or fields where it is missing.
 *      - Ensure that all Javadoc is up-to-date and provides clear, meaningful documentation.
 * - Use Clear and Concise Descriptions:
 *      - Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 *      - Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * - Include Usage Examples Where Helpful:
 *      - Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * - Maintain Consistency and Formatting:
 *      - Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *      - Review the Java class source code and include comprehensive Javadoc comments for all classes,
 *           methods, and fields, adhering to established best practices.
 *      - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *           and any exceptions thrown.
 *      - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;`
 *           and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc.
 *      - Do not use escaping in `{@code ...}` tags.    
 * - Use the Java Version Defined in `pom.xml`:
 *      - All code improvements and Javadoc updates must be compatible with the Java version `maven.compiler.release` specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
