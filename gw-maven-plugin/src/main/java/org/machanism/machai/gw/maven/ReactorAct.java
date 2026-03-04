package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Maven goal {@code gw:act-reactor} that runs an action against the execution-root project
 * using Maven's standard reactor build context.
 *
 * <p>
 * Unlike {@link Act} (which is an aggregator and can discover/scan modules itself), this goal
 * executes as part of a standard reactor build. It typically targets the execution-root project
 * only and delegates the scan to {@link ActProcessor}.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * This goal does not introduce additional parameters beyond those supported by
 * {@link Act} and {@link AbstractGWGoal}.
 * </p>
 *
 * <h3>Inherited parameters (from {@link Act})</h3>
 * <dl>
 * <dt><b>{@code -Dgw.act}</b> / {@code &lt;act&gt;}</dt>
 * <dd>Action text/prompt to apply. If omitted, the goal reads it interactively.</dd>
 *
 * <dt><b>{@code -Dgw.acts}</b> / {@code &lt;acts&gt;}</dt>
 * <dd>Optional directory containing predefined action definitions.</dd>
 * </dl>
 *
 * <h3>Inherited parameters (from {@link AbstractGWGoal})</h3>
 * <dl>
 * <dt><b>{@code -Dgw.model}</b> / {@code &lt;model&gt;}</dt>
 * <dd>Provider/model identifier forwarded to the workflow. Example: {@code openai:gpt-4o-mini}.</dd>
 *
 * <dt><b>{@code -Dgw.scanDir}</b> / {@code &lt;scanDir&gt;}</dt>
 * <dd>Optional scan root override. When omitted, defaults to the execution-root directory.</dd>
 *
 * <dt><b>{@code -Dgw.excludes}</b> / {@code &lt;excludes&gt;}</dt>
 * <dd>Exclude patterns/paths to skip while scanning documentation sources.</dd>
 *
 * <dt><b>{@code -Dgw.genai.serverId}</b> / {@code &lt;serverId&gt;}</dt>
 * <dd>{@code settings.xml} {@code &lt;server&gt;} id used to read GenAI credentials.</dd>
 *
 * <dt><b>{@code -Dgw.logInputs}</b> / {@code &lt;logInputs&gt;}</dt>
 * <dd>Whether to log the list of input files passed to the workflow.</dd>
 * </dl>
 *
 * <h2>Usage examples</h2>
 * <pre>
 * mvn gw:act-reactor
 * </pre>
 * <pre>
 * mvn gw:act-reactor -Dgw.act="Rewrite headings" -Dgw.scanDir=src\\site
 * </pre>
 */
@Mojo(name = "act-reactor", threadSafe = true)
public class ReactorAct extends Act {

	@Override
	public void execute() throws MojoExecutionException {
		PropertiesConfigurator configuration = getConfiguration();
		File basedir = new File(session.getExecutionRootDirectory());

		ActProcessor actProcessor = new ActProcessor(basedir, configuration, configuration.get("gw.model", model)) {

			@Override
			public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = ProjectLayoutManager.detectProjectLayout(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;
					mavenProjectLayout.projectDir(projectDir);
					Model model = project.getModel();
					mavenProjectLayout.model(model);
				}

				return projectLayout;
			}

			@Override
			protected void processModule(File projectDir, String module) throws IOException {
				// No-op for this implementation
			}
		};
		actProcessor.setNonRecursive(true);
		actProcessor.setModuleMultiThread(false);

		process(configuration, actProcessor);
	}

}
