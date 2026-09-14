/* @guidance: >>> ${guidances}/package-info-javadoc.md */
/**
 * Provides the Maven-plugin integration layer for Machai Ghostwriter workflows.
 *
 * <p>The package exposes Maven goals that configure Ghostwriter from the active
 * Maven execution and then apply either guidance-driven processing or an explicit
 * act to project files. It bridges Maven's project, reactor, session, settings,
 * and dependency-resolution facilities to Ghostwriter processors, enabling
 * project-aware workflows while retaining support for aggregator execution outside
 * a {@code pom.xml}-based build.</p>
 *
 * <h2>Goal architecture</h2>
 * <p>{@link AbstractGWMojo} supplies the common integration behavior. It accepts
 * Maven context and configuration parameters, builds the effective Ghostwriter
 * configuration from properties, plugin parameters, and an optional Maven
 * {@code settings.xml} server, applies scan paths, instructions, and exclusions,
 * and reports usage. When Maven is executing a project, it also registers the
 * class-introspection tools supplied by the related
 * {@code org.machanism.machai.gw.maven.tools} package.</p>
 *
 * <p>Configuration is assembled from the selected Ghostwriter properties file,
 * an optional Maven {@code settings.xml} server entry, and plugin {@code params};
 * later sources override earlier values. The mojos then create the appropriate
 * processor, attach Maven-aware project-layout and class-introspection support
 * when a project is available, and delegate file traversal and workflow execution
 * to the processor layer. This separation keeps Maven parameter binding and
 * reactor integration in this package while the processor package owns guidance
 * and act execution.</p>
 *
 * <p>{@link GWMojo} and {@link GWPerModuleMojo} create a
 * {@link org.machanism.machai.gw.processor.GuidanceProcessor} that locates files
 * containing Ghostwriter guidance comments and executes their workflows.
 * {@link AbstractActMojo} extends the shared infrastructure for an explicit
 * Ghostwriter act: it resolves an act name or inline prompt, can collect
 * multi-line interactive input, locates reusable act definitions, and delegates
 * processing to an {@link org.machanism.machai.gw.processor.ActProcessor}.
 * {@link ActMojo} implements the aggregator act goal; {@link AbstractActPerModuleMojo}
 * provides the reactor-aware behavior used by {@link ActPerModuleMojo}.</p>
 *
 * <h2>Public API relationships</h2>
 * <p>The concrete goal classes are intentionally thin Maven entry points:
 * {@link GWMojo} and {@link GWPerModuleMojo} run guidance discovery, while
 * {@link ActMojo} and {@link ActPerModuleMojo} run a selected act. Their shared
 * base classes centralize Maven parameter binding and processor setup so that
 * aggregator and per-module variants apply the same provider configuration,
 * exclusions, instructions, and project-aware tools. The accompanying
 * {@code org.machanism.machai.gw.maven.tools} package supplies the
 * class-information functions registered for executions that have Maven project
 * context.</p>
 *
 * <h2>Execution models</h2>
 * <dl>
 * <dt>{@code gw:gw} and {@code gw:act}</dt>
 * <dd>Thread-safe aggregator goals that can traverse a project tree, including a
 * multi-module tree, and do not require a {@code pom.xml}. Ghostwriter performs
 * traversal in reverse order, processing child modules before parent modules.
 * During a Maven parallel build, Ghostwriter coordinates that traversal.</dd>
 * <dt>{@code gw:gw-per-module} and {@code gw:act-per-module}</dt>
 * <dd>Thread-safe, non-aggregator goals invoked by Maven for every selected
 * reactor module. They suppress nested module traversal because Maven supplies
 * module scheduling in its standard dependency order.</dd>
 * </dl>
 *
 * <h2>Configuration</h2>
 * <p>All goals support the common {@code gw.model}, {@code gw.path},
 * {@code gw.instructions}, {@code gw.excludes}, {@code genai.serverId}, and
 * {@code gw.config} properties, together with additional plugin configuration
 * entries through {@code params}. Act goals additionally accept {@code gw.act}
 * and {@code gw.acts}. A {@code genai.serverId} value selects a matching Maven
 * {@code settings.xml} server, whose credentials and custom XML configuration
 * become processor settings. In the absence of a server identifier, Ghostwriter
 * loads its default or explicitly selected properties file. Concrete mojo classes
 * document the complete parameter and plugin-XML forms.</p>
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
 * # Let Maven invoke a non-aggregator goal for selected reactor modules.
 * mvn gw:gw-per-module -Dgw.path=src/main/java
 * mvn gw:act-per-module -Dgw.act=review
 *
 * # Resolve provider credentials from a Maven settings server.
 * mvn gw:gw -Dgenai.serverId=my-ai-provider
 * }</pre>
 *
 * @see AbstractGWMojo
 * @see GWMojo
 * @see ActMojo
 */
package org.machanism.machai.gw.maven;
