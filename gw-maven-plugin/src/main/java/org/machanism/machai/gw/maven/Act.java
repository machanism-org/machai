package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Maven goal {@code gw:act} that runs an interactive, predefined "action" over
 * a documentation tree.
 *
 * <p>
 * An action is a prompt (typically sourced from a resource bundle or prompt
 * file) that is applied to the scanned documents. If {@code -Dgw.act} is not
 * provided, the goal prompts the user interactively via Maven's
 * {@link Prompter} component.
 * </p>
 *
 * <h2>Parameters</h2>
 * <dl>
 * <dt><b>{@code -Dgw.act}</b> / {@code &lt;act&gt;}</dt>
 * <dd>Action text/prompt to apply. If omitted, the goal reads it interactively.
 * </dd>
 *
 * <dt><b>{@code -Dgw.acts}</b> / {@code &lt;acts&gt;}</dt>
 * <dd>Optional directory containing predefined action definitions.</dd>
 * </dl>
 *
 * <h3>Inherited parameters (from {@link AbstractGWGoal})</h3>
 * <p>
 * This goal also supports all common parameters defined by
 * {@link AbstractGWGoal} (for example {@code -Dgw.model}, {@code -Dgw.scanDir},
 * {@code -Dgw.excludes}, {@code -Dgw.genai.serverId}, and
 * {@code -Dgw.logInputs}).
 * </p>
 *
 * <h2>Usage examples</h2>
 * 
 * <pre>
 * mvn gw:act
 * </pre>
 * 
 * <pre>
 * mvn gw:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src\\site
 * </pre>
 * 
 * <pre>
 * mvn gw:act -Dgw.acts=src\\site\\acts -Dgw.logInputs=true
 * </pre>
 */
@Mojo(name = "act", aggregator = true)
public class Act extends AbstractGWGoal {

	/**
	 * Interactive prompt provider used to collect action input.
	 */
	@Component
	protected Prompter prompter;

	/**
	 * Action prompt text. If not set, the goal prompts the user interactively.
	 */
	@Parameter(property = "gw.act", required = false)
	private String act;

	/**
	 * Optional directory containing predefined action definitions.
	 */
	@Parameter(property = "gw.acts", required = false)
	private File acts;

	/**
	 * Executes the interactive action and scans documents using the configured
	 * action prompt.
	 *
	 * @throws MojoExecutionException if an I/O failure occurs while processing
	 *                                files
	 */
	@Override
	public void execute() throws MojoExecutionException {
		PropertiesConfigurator configuration = getConfiguration();

		ActProcessor actProcessor = new ActProcessor(basedir, configuration, configuration.get("gw.model", genai)) {
			@Override
			public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = super.getProjectLayout(projectDir);
				projectLayout.projectDir(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;
					mavenProjectLayout.effectivePomRequired(false);

					Model model = mavenProjectLayout.getModel();
					for (MavenProject mavenProject : session.getAllProjects()) {
						if (Strings.CS.equals(mavenProject.getArtifactId(), model.getArtifactId())) {
							mavenProjectLayout.model(mavenProject.getModel());
							break;
						}
					}
				}

				return projectLayout;
			}
		};

		process(configuration, actProcessor);
	}

	protected void process(PropertiesConfigurator configuration, ActProcessor actProcessor)
			throws MojoExecutionException {
		try {
			if (acts != null) {
				logger.info("Act directory: {}", acts);
				actProcessor.setActDir(acts);
			}
			if (excludes != null) {
				actProcessor.setExcludes(excludes);
			}
			actProcessor.setLogInputs(logInputs);

			try {
				Properties userProperties = session.getUserProperties();
				act = userProperties.getProperty("gw.act");
				if (act == null) {
					act = readText("Act");
					userProperties.setProperty("gw.act", act);
				}
				actProcessor.setDefaultPrompt(act);
				String gwScanDir = configuration.get("gw.scanDir", null);
				String scanDir = Objects.toString(super.scanDir, gwScanDir);
				scanDir = Objects.toString(scanDir, basedir.getAbsolutePath());
				logger.info("Starting scan of directory: {}", scanDir);
				actProcessor.scanDocuments(basedir, scanDir);
				logger.info("Finished scanning directory: {}", scanDir);

			} catch (PrompterException e) {
				getLog().error("Error: " + e.getMessage());
			}

		} catch (IOException e) {
			getLog().error("I/O error occurred during file processing: " + e.getMessage());
			throw new MojoExecutionException("I/O error occurred during file processing", e);

		} finally {
			GenAIProviderManager.logUsage();
		}
	}

	/**
	 * Reads multi-line input from the interactive {@link Prompter}.
	 *
	 * <p>
	 * The user can enter multiple lines by ending a line with
	 * {@link Ghostwriter#MULTIPLE_LINES_BREAKER}. Input collection stops when a
	 * line does not end with the breaker.
	 * </p>
	 *
	 * @param prompt the initial prompt label displayed to the user
	 * @return the collected text
	 * @throws PrompterException if prompting fails
	 */
	public String readText(String prompt) throws PrompterException {
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = prompter.prompt(prompt)) != null) {
			prompt = "\t";
			if (Strings.CS.endsWith(line, Ghostwriter.MULTIPLE_LINES_BREAKER)) {
				sb.append(StringUtils.substringBeforeLast(line, Ghostwriter.MULTIPLE_LINES_BREAKER)).append("\n");
			} else {
				sb.append(StringUtils.substringBeforeLast(line, Ghostwriter.MULTIPLE_LINES_BREAKER));
				break;
			}
		}

		return sb.toString();
	}
}
