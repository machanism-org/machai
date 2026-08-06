package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemProperties;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.gw.tools.ProcessTerminationException;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/*@guidance:
 * # Generate detailed Javadoc documentation for the Maven plugin, including:
 * - A clear description of all Maven plugin parameters.
 * - Examples of usage for each parameter.
 * - Information inherited from any superclass or parent class.
 * - Escape the closing javadoc tag in javadoc content, as it was breaking javadoc compilation.
 * - Note: When using Maven parallel execution, the modules will be processed by `gw` 
 *   instead of the Maven reactor.     
 * - **Processing Order for the `gw:act` Goal:**
 *   - The `gw:act` goal processes files in reverse order, similar to the Ghostwriter CLI.
 *   - Sub-modules are processed first, followed by parent modules.
 *   - This goal can be executed without a `pom.xml` file.
 *   - **Tip:** For improved performance, use Maven parallel execution: `mvn -T 4 gw:act`.
 *  @since 1.1.2
 */

/**
 * Maven goal that executes a Ghostwriter act against project files.
 *
 * <p>
 * This goal creates an {@link ActProcessor}, resolves configuration from Maven
 * parameters, user properties and Ghostwriter configuration files, and then
 * scans the selected path. The goal is an aggregator, is thread-safe, and can
 * run without a {@code pom.xml}. When a Maven project is present, inherited
 * behavior from {@link AbstractGWMojo} contributes common Ghostwriter
 * parameters such as the base directory, model, path, instructions, excludes
 * and shared class-scanning tools.
 * </p>
 *
 * <h2>Processing order for {@code gw:act}</h2>
 * <p>
 * Files are processed in reverse order, matching the Ghostwriter CLI behavior:
 * sub-modules are processed first and parent modules are processed afterward.
 * When Maven parallel execution is enabled, modules are processed by
 * Ghostwriter rather than by the Maven reactor. For improved performance, run
 * for example:
 * </p>
 *
 * <pre>{@code
 * mvn -T 4 gw:act
 * }</pre>
 *
 * <h2>Parameters declared by this goal</h2>
 * <ul>
 * <li>{@code gw.act}: action prompt text or predefined act name. Examples:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act="Add missing Javadocs"
 * mvn gw:act -Dgw.act=review
 * }</pre>
 * 
 * </li>
 * <li>{@code gw.acts}: optional directory or path containing predefined act
 * definitions. Examples:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.acts=.ghostwriter/acts -Dgw.act=review
 * mvn gw:act -Dgw.acts=src/gw/acts -Dgw.act=documentation
 * }</pre>
 * 
 * </li>
 * </ul>
 *
 * <h2>Common inherited parameters</h2>
 * <p>
 * The following commonly used parameters are inherited from
 * {@link AbstractGWMojo} and are honored by this goal when present:
 * </p>
 * <ul>
 * <li>{@code gw.path}: file, directory, glob or other supported path expression
 * to scan. Example:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act=review -Dgw.path=src/main/java
 * }</pre>
 * 
 * </li>
 * <li>{@code gw.model}: AI model override. Example:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act=review -Dgw.model=gpt-4.1
 * }</pre>
 * 
 * </li>
 * <li>{@code gw.instructions}: additional instructions supplied to the act.
 * Example:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act=review -Dgw.instructions="Focus on public API compatibility"
 * }</pre>
 * 
 * </li>
 * <li>{@code gw.excludes}: comma-separated exclusions. Example:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act=review -Dgw.excludes=target,*.class
 * }</pre>
 * 
 * </li>
 * <li>{@code gw.interactive}: enables or disables interactive prompting when
 * configuration is incomplete. Example:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.interactive=false -Dgw.act=review
 * }</pre>
 * 
 * </li>
 * </ul>
 *
 * <p>
 * If Javadoc text needs to mention the literal closing tag, write it as
 * {@code *&#47;} so generated documentation does not terminate the comment
 * early.
 * </p>
 *
 * @since 1.1.2
 */
@Mojo(name = "act", aggregator = true, threadSafe = true, requiresProject = false, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ActMojo extends AbstractGWMojo {

	/**
	 * Interactive prompt provider used to collect action input.
	 */
	@SuppressWarnings("deprecation")
	@Component
	protected Prompter prompter;

	/**
	 * Action prompt text or predefined act name, supplied by the {@code gw.act}
	 * Maven property.
	 *
	 * <p>
	 * When this parameter is not supplied, the goal first checks configured
	 * properties and then prompts the user interactively. Multi-line input is
	 * supported by ending each continued line with
	 * {@link GWConstants#MULTIPLE_LINES_BREAKER}.
	 * </p>
	 *
	 * <pre>{@code
	 * mvn gw:act -Dgw.act=">Add missing Javadocs"
	 * mvn gw:act -Dgw.act=commit
	 * }</pre>
	 */
	@Parameter(property = GWConstants.ACT_PROP_NAME, required = false)
	protected String actPrompt;

	/**
	 * Optional directory or path containing predefined action definitions, supplied
	 * by the {@code gw.acts} Maven property.
	 *
	 * <p>
	 * When provided, this value overrides the default act lookup location used by
	 * {@link ActProcessor}. It may point to a project-relative directory containing
	 * reusable act templates.
	 * </p>
	 *
	 * <pre>{@code
	 * mvn gw:act -Dgw.acts=acts -Dgw.act=site
	 * mvn gw:act -Dgw.acts=https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/acts -Dgw.act=site
	 * }</pre>
	 */
	@Parameter(property = GWConstants.ACTS_LOCATION_PROP_NAME, required = false)
	private String acts;

	private static final Object MONITOR = new Object();

	/**
	 * Executes the configured act goal.
	 *
	 * <p>
	 * The method creates and configures an {@link ActProcessor}, resolves Maven and
	 * Ghostwriter configuration values, applies inherited parameters such as path,
	 * model, instructions, excludes, and interactive mode, and then scans the
	 * selected documents. A zero-code {@link ProcessTerminationException} is
	 * treated as normal termination.
	 * </p>
	 *
	 * @throws MojoExecutionException if configuration, prompting, or file
	 *                                processing fails
	 */
	@Override
	public void execute() throws MojoExecutionException {
		PropertiesConfigurator configuration = getConfiguration();
		Boolean interactive = configuration.getBoolean(GWConstants.INTERACTIVE_MODE_PROP_NAME, null);

		String model = configuration.get(GWConstants.MODEL_PROP_NAME, this.model);
		if (model != null) {
			logger.info(Ghostwriter.DEFAULT_MODEL_MSG, model);
		}
		ActProcessor actProcessor = new ActProcessor(basedir, model, configuration) {
			@Override
			public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = super.getProjectLayout(projectDir);
				projectLayout.projectDir(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;
					Model model = mavenProjectLayout.getModel();
					updateMavenProjectLayout(mavenProjectLayout, model);
				}

				return projectLayout;
			}

			@Override
			protected String input() {
				try {
					return readText(Ghostwriter.USER_INPUT_PREFIX);
				} catch (PrompterException e) {
					throw new IllegalArgumentException(e);
				}
			}
		};

		if (super.path != null) {
			actProcessor.getActProperties().put(GWConstants.PATH_PROP_NAME, super.path);
		}

		List<MavenProject> modules = session.getAllProjects();
		boolean nonRecursive = project.getModules().size() > 1 && modules.size() == 1;
		actProcessor.setNonRecursive(nonRecursive);

		boolean isParallel = session.isParallel();
		if (isParallel) {
			int threads = session.getRequest().getDegreeOfConcurrency();
			logger.info("Threads: {}", threads);
			actProcessor.setThreads(threads);
		}

		if (interactive != null) {
			actProcessor.setInteractive(interactive);
		}

		if (instructions != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Instructions: {}", StringUtils.abbreviate(instructions, AbstractAIProvider.LOG_LINE_LENG));
			}
			actProcessor.setInstructions(instructions);
		}

		if (model != null) {
			actProcessor.setModel(model);
		}

		try {
			process(actProcessor);
		} catch (ProcessTerminationException e) {
			if (e.getExitCode() != 0) {
				throw e;
			}
		}
	}

	private void updateMavenProjectLayout(MavenProjectLayout mavenProjectLayout, Model model) {
		for (MavenProject mavenProject : session.getAllProjects()) {
			if (session.getRequest().isProjectPresent()) {
				classFunctionTools.scanProjectClasses(mavenProject);
			}
			if (Strings.CS.equals(mavenProject.getArtifactId(), model.getArtifactId())) {
				mavenProjectLayout.model(mavenProject.getModel());
				break;
			}
		}
	}

	/**
	 * Applies runtime configuration to the supplied act processor and starts
	 * document scanning.
	 *
	 * @param actProcessor the act processor to configure and execute
	 * @throws MojoExecutionException if scanning fails because of I/O or prompting
	 *                                errors
	 */
	protected void process(ActProcessor actProcessor) throws MojoExecutionException {
		try {
			UsageStatistics.init();
			String actsLocation = actProcessor.getConfigurator().get(GWConstants.ACTS_LOCATION_PROP_NAME, this.acts);

			if (actsLocation != null) {
				logger.info("Custom acts location specified: {}", actsLocation);
				actProcessor.setActsLocation(actsLocation);
			}

			String[] effectiveExcludes = null;
			String excludesStr = actProcessor.getConfigurator().get(GWConstants.EXCLUDES_PROP_NAME, null);
			if (excludesStr != null) {
				effectiveExcludes = StringUtils.split(excludesStr, ",");
			}

			if (effectiveExcludes != null) {
				actProcessor.setExcludes(effectiveExcludes);
			} else {
				actProcessor.setExcludes(this.excludes);
			}

			configureAndScan(actProcessor);

		} catch (IOException e) {
			getLog().error("I/O error occurred during file processing: " + e.getMessage());
			throw new MojoExecutionException("I/O error occurred during file processing", e);

		} finally {
			UsageStatistics.logUsage();
		}
	}

	/**
	 * Resolves the effective act prompt and scans documents when an act is
	 * available.
	 *
	 * @param actProcessor the act processor that receives the resolved act
	 * @throws MojoExecutionException if interactive prompt collection fails
	 * @throws IOException            if document scanning fails
	 */
	public void configureAndScan(ActProcessor actProcessor) throws MojoExecutionException, IOException {
		String savedAct = actPrompt;
		if (savedAct == null) {
			applyActPrompt(actProcessor.getConfigurator());
			Properties userProperties = session.getUserProperties();
			savedAct = userProperties.getProperty(GWConstants.ACT_PROP_NAME);
		} else {
			logger.info("Act: {}", savedAct);
		}	
		actProcessor.setAct(savedAct);
		if (savedAct != null) {
			scanDocuments(actProcessor);
		}
	}

	/**
	 * Ensures an act prompt is stored in Maven user properties.
	 *
	 * @param conf configuration used to look up a non-interactive act value before
	 *             prompting
	 * @throws MojoExecutionException if interactive prompt collection fails
	 */
	protected void applyActPrompt(Configurator conf) throws MojoExecutionException {
		synchronized (MONITOR) {
			try {
				Properties userProperties = session.getUserProperties();
				String savedAct = userProperties.getProperty(GWConstants.ACT_PROP_NAME);
				if (savedAct == null) {
					String actValue = conf.get(GWConstants.ACT_PROP_NAME, null);
					if (actValue == null) {
						actValue = readText("Act");
						if (Strings.CS.equals(actValue.toLowerCase().trim(),
								AIFileProcessor.EXIT_SPECIAL_PROMPT_COMMAND)) {
							return;
						}
					}
					userProperties.setProperty(GWConstants.ACT_PROP_NAME, actValue);
				} else {
					logger.info("Act: {}", savedAct);
				}
			} catch (PrompterException e) {
				throw new MojoExecutionException(
						"Failed to read '" + GWConstants.ACT_PROP_NAME + "' prompt interactively.", e);
			}
		}
	}

	/**
	 * Scans the resolved project path with the configured act processor.
	 *
	 * @param actProcessor the processor used to scan documents
	 * @throws IOException if reading or writing project files fails
	 */
	protected void scanDocuments(ActProcessor actProcessor) throws IOException {
		String gwPaths = actProcessor.getConfigurator().get(GWConstants.PATH_PROP_NAME, null);
		String resolvedPaths = Objects.toString(path, gwPaths);
		resolvedPaths = Objects.toString(resolvedPaths, basedir.getAbsolutePath());

		logger.info("Starting scan of path: `{}`", resolvedPaths);
		if (session.getRequest().isProjectPresent()) {
			classFunctionTools.scanProjectClasses(project);
			actProcessor.addTool(classFunctionTools);
		}

		actProcessor.scanDocuments(basedir, resolvedPaths);
		logger.info("Finished scanning path: `{}`", resolvedPaths);
	}

	/**
	 * Reads multi-line input from the interactive {@link Prompter}.
	 *
	 * <p>
	 * The user can enter multiple lines by ending a line with
	 * {@link GWConstants#MULTIPLE_LINES_BREAKER}. Input collection stops when a
	 * line does not end with the breaker.
	 * </p>
	 *
	 * @param prompt the initial prompt label displayed to the user
	 * @return the collected text
	 * @throws PrompterException if prompting fails
	 */
	@SuppressWarnings("java:S106")
	public String readText(String prompt) throws PrompterException {
		StringBuilder sb = new StringBuilder();
		String line;
		int length = prompt.length() + 2;
		int maxlen = length;
		while ((line = prompter.prompt(prompt)) != null) {
			prompt = "\t";
			length += line.length();
			if (length > maxlen) {
				maxlen = length;
			}
			if (Strings.CS.endsWith(line, GWConstants.MULTIPLE_LINES_BREAKER)) {
				sb.append(StringUtils.substringBeforeLast(line, GWConstants.MULTIPLE_LINES_BREAKER))
						.append(AbstractAIProvider.LINE_SEPARATOR);
			} else {
				sb.append(line);
				break;
			}
			length = 8;
		}
		System.out.println(StringUtils.leftPad("― ©" + SystemProperties.getUserName(), maxlen));

		return sb.toString();
	}

}
