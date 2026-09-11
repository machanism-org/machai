/* @guidance: >>> ${guidances}/package-info-javadoc.md */
/**
 * Provides the Maven-plugin integration layer for Machai Ghostwriter workflows.
 * The goals in this package configure Ghostwriter processors from Maven state and
 * apply AI-assisted guidance or named acts to files in a project tree.
 *
 * <h2>Goal architecture</h2>
 * <p>{@link AbstractGWMojo} is the common integration base. It receives Maven's
 * project, session, base-directory, and settings objects; builds the effective
 * Ghostwriter configuration; applies scan paths, instructions, and exclusions;
 * reports usage; and registers class-introspection tools when a Maven project is
 * available. {@link GWMojo} and {@link GWPerModuleMojo} create a
 * {@link org.machanism.machai.gw.processor.GuidanceProcessor} to find files that
 * contain Ghostwriter guidance comments and execute the resulting workflow.</p>
 *
 * <p>{@link AbstractActMojo} extends that infrastructure for an explicit
 * Ghostwriter act. It resolves an act name or inline prompt, optionally reads
 * multi-line interactive input, locates reusable act definitions, and delegates
 * processing to an {@link org.machanism.machai.gw.processor.ActProcessor}.
 * {@link ActMojo} is the aggregator act goal, while
 * {@link AbstractActPerModuleMojo} provides the per-module act behavior used by
 * {@link ActPerModuleMojo}. The related
 * {@code org.machanism.machai.gw.maven.tools} package provides the classpath
 * scanning and reflection metadata exposed to processor tools.</p>
 *
 * <h2>Execution models</h2>
 * <dl>
 * <dt>{@code gw:gw} and {@code gw:act}</dt>
 * <dd>Aggregator goals that can traverse a project tree, including a
 * multi-module tree, and do not require a {@code pom.xml}. Ghostwriter performs
 * the traversal; guidance and act processing use reverse ordering, with child
 * modules handled before their parents. Under Maven parallel execution,
 * Ghostwriter coordinates this traversal.</dd>
 * <dt>{@code gw:gw-per-module} and {@code gw:act-per-module}</dt>
 * <dd>Non-aggregator goals that Maven invokes for each selected reactor module.
 * Nested module traversal is suppressed because Maven supplies the module
 * scheduling and standard dependency order.</dd>
 * </dl>
 *
 * <h2>Configuration</h2>
 * <p>All goals support the common {@code gw.model}, {@code gw.path},
 * {@code gw.instructions}, {@code gw.excludes}, {@code genai.serverId}, and
 * {@code gw.config} properties, as well as plugin configuration entries through
 * {@code params}. The act goals additionally accept {@code gw.act} and
 * {@code gw.acts}. {@code genai.serverId} selects a matching Maven
 * {@code settings.xml} server, from which credentials and custom server
 * configuration are read. When no server identifier is configured, Ghostwriter
 * loads its normal or explicitly selected properties file. Concrete mojo classes
 * document their complete parameter and plugin-XML forms.</p>
 *
 * <h2>Usage examples</h2>
 * <pre>{@code
 * # Scan files containing Ghostwriter guidance comments.
 * mvn gw:gw -Dgw.path=src/main/java -Dgw.instructions=docs/guidance.md
 *
 * # Execute either an inline action or a named act definition.
 * mvn gw:act -Dgw.act="Add missing public API Javadocs"
 * mvn -T 4 gw:act -Dgw.act=review
 *
 * # Let Maven invoke the goal for selected reactor modules.
 * mvn gw:gw-per-module -Dgw.path=src/main/java
 * mvn gw:act-per-module -Dgw.act=review
 *
 * # Resolve provider credentials from a Maven settings server.
 * mvn gw:gw -Dgenai.serverId=my-ai-provider
 * }</pre>
 */
package org.machanism.machai.gw.maven;
