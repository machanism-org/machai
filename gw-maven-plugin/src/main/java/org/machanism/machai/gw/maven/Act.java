package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.ai.manager.GenaiProviderManager;
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
 * <dt><b>{@code -Dgw.acts}</b> / {@code &lt;locations&gt;}</dt>
 * <dd>Optional directory containing predefined action definitions.</dd>
 * </dl>
 *
 * <h3>Inherited parameters (from {@link AbstractGWGoal})</h3>
 * <p>
 * This goal also supports all common parameters defined by
 * {@link AbstractGWGoal} (for example {@code -Dgw.model}, {@code -Dgw.scanDir},
 * {@code -Dgw.excludes}, {@code -Dgenai.serverId}, and {@code -DlogInputs}).
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
 * mvn gw:act -Dgw.acts=src\\site\\locations -DlogInputs=true
 * </pre>
 */
@Mojo(name = "act", aggregator = true, threadSafe = true, requiresProject = false, defaultPhase = LifecyclePhase.PACKAGE)
public class Act extends AbstractGWGoal {

	/**
	 * Interactive prompt provider used to collect action input.
	 */
	@Component
	protected Prompter prompter;

	/**
	 * Action prompt text. If not set, the goal prompts the user interactively.
	 */
	@SuppressWarnings("java:S1700")
	@Parameter(property = Ghostwriter.ACT_PROP_NAME, required = false)
	protected String act;

	/**
	 * Optional directory containing predefined action definitions.
	 */
	@Parameter(property = Ghostwriter.ACTS_LOCATION_PROP_NAME, required = false)
	private String locations;

	private static final Object MONITOR = new Object();

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

		String model = configuration.get(Ghostwriter.MODEL_PROP_NAME, this.model);
		logger.info("Model: {}", model);

		ActProcessor actProcessor = new ActProcessor(basedir, configuration, model) {
			@Override
			public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = super.getProjectLayout(projectDir);
				projectLayout.projectDir(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;

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

			@Override
			protected String input() {
				try {
					return readText("act");
				} catch (PrompterException e) {
					throw new IllegalArgumentException(e);
				}
			}
		};

		List<MavenProject> modules = session.getAllProjects();
		boolean nonRecursive = project.getModules().size() > 1 && modules.size() == 1;
		actProcessor.setNonRecursive(nonRecursive);

		boolean isParallel = session.isParallel();
		if (isParallel) {
			int data = session.getRequest().getDegreeOfConcurrency();
			actProcessor.setDegreeOfConcurrency(data);
		}

		process(actProcessor);
	}

	protected void process(ActProcessor actProcessor) throws MojoExecutionException {
		try {
			String actsLocation = actProcessor.getConfigurator().get(Ghostwriter.ACTS_LOCATION_PROP_NAME,
					this.locations);

			if (actsLocation != null) {
				logger.info("Custom acts location specified: {}", actsLocation);
				actProcessor.setActsLocation(actsLocation);
			}

			String[] excludes = null;
			String excludesStr = actProcessor.getConfigurator().get(Ghostwriter.EXCLUDES_PROP_NAME, null);
			if (excludesStr != null) {
				excludes = StringUtils.split(excludesStr, ",");
			}

			if (excludes != null) {
				actProcessor.setExcludes(this.excludes);
			} else {
				actProcessor.setExcludes(excludes);
			}

			actProcessor.setLogInputs(logInputs);

			configureAndScan(actProcessor);

		} catch (IOException e) {
			getLog().error("I/O error occurred during file processing: " + e.getMessage());
			throw new MojoExecutionException("I/O error occurred during file processing", e);
		} finally {
			GenaiProviderManager.logUsage();
		}
	}

	public void configureAndScan(ActProcessor actProcessor) throws MojoExecutionException, IOException {
		String savedAct = act;
		if (savedAct == null) {
			applyActPrompt(actProcessor.getConfigurator());
			Properties userProperties = session.getUserProperties();
			savedAct = userProperties.getProperty(Ghostwriter.ACT_PROP_NAME);
		}
		actProcessor.setDefaultPrompt(savedAct);
		scanDocuments(actProcessor);
	}

	protected void applyActPrompt(Configurator conf) throws MojoExecutionException {
		synchronized (MONITOR) {
			try {
				Properties userProperties = session.getUserProperties();
				String savedAct = userProperties.getProperty(Ghostwriter.ACT_PROP_NAME);
				if (savedAct == null) {
					String actValue = conf.get(Ghostwriter.ACT_PROP_NAME, null);
					if (actValue == null) {
						actValue = readText("act");
					}
					userProperties.setProperty(Ghostwriter.ACT_PROP_NAME, actValue);
				} else {
					logger.info("act: {}", savedAct);
				}
			} catch (PrompterException e) {
				throw new MojoExecutionException(
						"Failed to read '" + Ghostwriter.ACT_PROP_NAME + "' prompt interactively.", e);
			}
		}
	}

	protected void scanDocuments(ActProcessor actProcessor) throws IOException {
		String gwScanDir = actProcessor.getConfigurator().get(Ghostwriter.SCAN_DIR_PROP_NAME, null);
		String resolvedScanDir = Objects.toString(super.scanDir, gwScanDir);
		resolvedScanDir = Objects.toString(resolvedScanDir, basedir.getAbsolutePath());

		logger.info("Starting scan of directory: {}", resolvedScanDir);
		actProcessor.scanDocuments(basedir, resolvedScanDir);
		logger.info("Finished scanning directory: {}", resolvedScanDir);
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
				sb.append(StringUtils.substringBeforeLast(line, Ghostwriter.MULTIPLE_LINES_BREAKER))
						.append(Genai.LINE_SEPARATOR);
			} else {
				sb.append(line);
				break;
			}
		}

		return sb.toString();
	}
}
