/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!	
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
 *      - Ensure that the package-level Javadoc is placed immediately before the `package` declaration.
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
/**
 * Provides Maven plugin goals that apply Machai Ghostwriter workflows to project
 * files and source trees.
 *
 * <p>The package contains the aggregator goals {@code gw:gw} and {@code gw:act},
 * which can traverse a multi-module build, and the per-module goals
 * {@code gw:gw-per-module} and {@code gw:act-per-module}, which participate in
 * Maven's reactor execution. {@code gw:gw} scans files containing Ghostwriter
 * guidance comments and applies the resulting workflow. {@code gw:act} executes
 * a named act or an inline action prompt. The {@code *-per-module} variants let
 * Maven schedule the goal for each selected reactor module according to its
 * normal dependency order.</p>
 *
 * <p>{@link AbstractGWMojo} supplies common Maven integration, including project
 * and session access, scan paths, instructions, exclusions, model selection,
 * Maven-settings credential resolution, configuration-file loading, usage
 * reporting, and registration of Java class-introspection tools. Concrete mojos
 * configure the appropriate Ghostwriter processor and delegate execution to it.
 * The related {@code org.machanism.machai.gw.maven.tools} package supplies the
 * classpath scanner and reflection metadata used by those tools.</p>
 *
 * <h2>Configuration</h2>
 * <p>All goals accept the common {@code gw.model}, {@code gw.path},
 * {@code gw.instructions}, {@code gw.excludes}, {@code genai.serverId}, and
 * {@code gw.config} properties. The act goals also accept {@code gw.act} and
 * {@code gw.acts}. A server identifier resolves provider credentials from the
 * matching Maven {@code settings.xml} server; otherwise, configuration is loaded
 * from the configured Ghostwriter properties file. The individual goal classes
 * document the complete parameter set and plugin-XML equivalents.</p>
 *
 * <p>Aggregator goals process a project tree themselves, whereas per-module goals
 * run in Maven's reactor and process the current module. The goals can therefore
 * be used either as direct command-line workflows or as executions in a Maven
 * plugin configuration.</p>
 *
 * <h2>Examples</h2>
 * <pre>
 * mvn gw:gw
 * mvn gw:gw -Dgw.path=src/main/java -Dgw.instructions=docs/guidance.md
 * mvn gw:act -Dgw.act="Add missing public API Javadocs"
 * mvn -T 4 gw:act -Dgw.act=review
 * mvn gw:gw-per-module -Dgw.path=src/main/java
 * mvn gw:act-per-module -Dgw.act=review
 * </pre>
 *
 * <p>Provider credentials can be read from Maven {@code settings.xml} by
 * supplying a server identifier, for example
 * {@code -Dgenai.serverId=my-ai-provider}. Additional workflow properties can
 * be supplied through Maven plugin configuration or the supported Ghostwriter
 * system properties.</p>
 */
package org.machanism.machai.gw.maven;
