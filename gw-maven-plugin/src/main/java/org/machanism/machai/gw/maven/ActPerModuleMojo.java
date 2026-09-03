package org.machanism.machai.gw.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.machanism.machai.gw.processor.ActProcessor;

/**
 * Maven goal {@code gw:act-per-module} that runs an action against the
 * execution-root project using Maven's standard reactor build context.
 *
 * <p>
 * Unlike {@link ActMojo} (which is an aggregator and can discover/scan modules
 * itself), this goal executes as part of a standard reactor build. It typically
 * targets the execution-root project only and delegates the scan to
 * {@link ActProcessor}.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * This goal does not introduce additional parameters beyond those supported by
 * {@link ActMojo} and {@link AbstractGWMojo}.
 * </p>
 *
 * <h3>Inherited parameters (from {@link ActMojo})</h3>
 * <dl>
 * <dt><b>{@code -Dgw.act}</b> / {@code &lt;act&gt;}</dt>
 * <dd>Action text/prompt to apply. If omitted, the goal reads it
 * interactively.</dd>
 *
 * <dt><b>{@code -Dgw.acts}</b> / {@code &lt;acts&gt;}</dt>
 * <dd>Optional directory containing predefined action definitions.</dd>
 * </dl>
 *
 * <h3>Inherited parameters (from {@link AbstractGWMojo})</h3>
 * <dl>
 * <dt><b>{@code -Dgw.model}</b> / {@code &lt;model&gt;}</dt>
 * <dd>Provider/model identifier forwarded to the workflow. Example:
 * {@code openai:gpt-4o-mini}.</dd>
 *
 * <dt><b>{@code -Dgw.path}</b> / {@code &lt;path&gt;}</dt>
 * <dd>Optional scan root override. When omitted, defaults to the execution-root
 * directory.</dd>
 *
 * <dt><b>{@code -Dgw.excludes}</b> / {@code &lt;excludes&gt;}</dt>
 * <dd>Exclude patterns/path to skip while scanning documentation sources.</dd>
 *
 * <dt><b>{@code -Dgenai.serverId}</b> / {@code &lt;serverId&gt;}</dt>
 * <dd>{@code settings.xml} {@code &lt;server&gt;} id used to read GenAI
 * credentials.</dd>
 * </dl>
 *
 * <h2>Usage examples</h2>
 *
 * <pre>
 * mvn gw:act-per-module
 * </pre>
 *
 * <pre>
 * mvn gw:act-per-module -Dgw.act="Rewrite headings" -Dgw.path=src\\site
 * </pre>
 */
@Mojo(name = "act-per-module", aggregator = false, threadSafe = true, requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ActPerModuleMojo extends AbstractActPerModuleMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		performAct(actPrompt);
	}

	/**
	 * Executes this mojo for programmatic callers.
	 *
	 * @param ignored retained for source compatibility
	 * @throws MojoExecutionException if processing fails
	 * @throws MojoFailureException if Maven reports a goal failure
	 */
	public void execute(ActPerModuleMojo ignored) throws MojoExecutionException, MojoFailureException {
		execute();
	}

}
