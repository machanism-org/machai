/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
/**
 * Provides Maven plugin mojos that run Machai Ghostwriter guidance and act workflows against Maven project files.
 *
 * <p>
 * The package contains aggregator goals, such as {@code gw:gw} and {@code gw:act}, and per-module goals, such as
 * {@code gw:gw-per-module} and {@code gw:act-per-module}. Aggregator goals can discover and process project modules
 * through Ghostwriter, while per-module goals execute within Maven's standard reactor lifecycle for each module.
 * </p>
 *
 * <p>
 * Shared behavior is implemented by {@link org.machanism.machai.gw.maven.AbstractGWMojo}, including Maven session and
 * project access, scan path selection, excludes, additional instructions, model selection, Maven settings credential
 * resolution, usage logging, and registration of class-introspection tools for Java projects. Concrete mojos create the
 * appropriate processor type and delegate scanning or action execution to the Ghostwriter processing layer.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * mvn gw:gw
 * mvn gw:gw -Dgw.path=src/main/java -Dgw.instructions=docs/gw-instructions.md
 * mvn gw:act -Dgw.act="Add missing public API Javadocs"
 * mvn -T 4 gw:act -Dgw.act=review
 * </pre>
 *
 * <p>
 * GenAI provider credentials may be supplied through Maven {@code settings.xml} by passing a configured server id, for
 * example {@code -Dgenai.serverId=my-ai-provider}. Goals also support common Ghostwriter properties such as model,
 * path, instructions, excludes, and act-specific parameters where applicable.
 * </p>
 */
package org.machanism.machai.gw.maven;

