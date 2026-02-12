package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for GW Maven plugin goals.
 *
 * <p>
 * This class centralizes shared configuration parameters and the common scan/execute flow.
 * Concrete goals typically configure goal-specific behavior (for example, reactor aggregation,
 * processing order, or threading) and then delegate to {@link #scanDocuments()} or
 * {@link #scanDocuments(FileProcessor)}.
 * </p>
 *
 * <h2>Common parameters</h2>
 * <ul>
 * <li>{@code -Dgw.genai} - Provider/model identifier to pass to the workflow.</li>
 * <li>{@code -Dgw.scanDir} - Optional scan root override. When omitted, defaults to the module base directory.</li>
 * <li>{@code -Dgw.instructions} - Instruction locations (for example, file paths or classpath locations)
 * consumed by the workflow.</li>
 * <li>{@code -Dgw.guidance} - Default guidance text forwarded to the workflow.</li>
 * <li>{@code -Dgw.excludes} - Exclude patterns/paths to skip while scanning documentation sources.</li>
 * <li>{@code -Dgw.genai.serverId} - {@code settings.xml} {@code &lt;server&gt;} id used to read GenAI credentials.</li>
 * <li>{@code -Dgw.logInputs} (default {@code false}) - Logs the list of input files passed to the workflow.</li>
 * </ul>
 *
 * <h2>Credentials</h2>
 * <p>
 * If {@code -Dgw.genai.serverId} is provided, this goal reads credentials from
 * {@code ~/.m2/settings.xml} {@code &lt;servers&gt;&lt;server&gt;} and forwards them to the workflow as
 * {@code GENAI_USERNAME}/{@code GENAI_PASSWORD} properties.
 * </p>
 *
 * <h2>Examples</h2>
 * <pre>
 * mvn gw:std -Dgw.genai=openai:gpt-4o-mini -Dgw.scanDir=src\site -Dgw.logInputs=true
 * </pre>
 */
public abstract class AbstractGWGoal extends AbstractMojo {

	static final Logger logger = LoggerFactory.getLogger(AbstractGWGoal.class);

	/**
	 * Provider/model identifier to pass to the workflow.
	 */
	@Parameter(property = "gw.genai")
	protected String genai;
	/**
	 * The Maven module base directory.
	 */
	@Parameter(defaultValue = "${basedir}", required = true)
	protected File rootDir;
	/**
	 * Optional scan root override.
	 */
	@Parameter(property = "gw.scanDir")
	String scanDir;
	/**
	 * Instruction locations (for example, file paths or classpath locations) consumed by the workflow.
	 */
	@Parameter(property = "gw.instructions", name = "instructions")
	protected String instructions;
	/**
	 * Default guidance text forwarded to the workflow.
	 */
	@Parameter(property = "gw.guidance", name = "guidance")
	protected String guidance;
	/**
	 * Exclude patterns/paths that should be skipped when scanning documentation sources.
	 */
	@Parameter(property = "gw.excludes", name = "excludes")
	protected String[] excludes;
	/**
	 * The current Maven project.
	 *
	 * <p>
	 * Used to derive whether the invocation is effectively non-recursive (for example, when running against a
	 * parent POM without building the full reactor).
	 * </p>
	 */
	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;
	/**
	 * The Maven session used to access reactor projects.
	 */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	protected MavenSession session;
	/**
	 * The Maven settings used to resolve credentials from {@code settings.xml}.
	 */
	@Parameter(readonly = true, defaultValue = "${settings}")
	private Settings settings;
	/**
	 * {@code settings.xml} {@code <server>} id used to read GenAI credentials.
	 */
	@Parameter(property = "gw.genai.serverId")
	private String serverId;
	/**
	 * Whether to log the list of input files passed to the workflow.
	 */
	@Parameter(property = "gw.logInputs", defaultValue = "false")
	protected boolean logInputs;
	/**
	 * Reactor projects for the current Maven session.
	 */
	@Parameter(defaultValue = "${reactorProjects}", readonly = true)
	protected List<MavenProject> reactorProjects;

	public AbstractGWGoal() {
		super();
	}

	/**
	 * Creates a default {@link FileProcessor} and scans the current module's documents.
	 *
	 * @throws MojoExecutionException if file scanning or processing fails
	 */
	protected void scanDocuments() throws MojoExecutionException {
		PropertiesConfigurator config = getConfiguration();
		File basedir = project.getBasedir();

		FileProcessor documents = new FileProcessor(genai, config) {
			@Override
			protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				MavenProjectLayout projectLayout = new MavenProjectLayout();
				projectLayout.projectDir(projectDir);
				Model model = project.getModel();
				projectLayout.model(model);
				return projectLayout;
			}

			@Override
			protected void processModule(File projectDir, String module) throws IOException {
				// No-op for this implementation
			}
		};

		logger.info("Scanning documents in the root directory: {}", basedir);
		documents.setModuleMultiThread(false);
		scanDocuments(documents);
		logger.info("Scanning finished.");
	}

	/**
	 * Builds a {@link PropertiesConfigurator} for workflow execution.
	 *
	 * <p>
	 * When {@code gw.genai.serverId} is set, credentials are read from Maven settings.
	 * </p>
	 *
	 * @return a configurator populated with any available workflow properties
	 * @throws MojoExecutionException if Maven settings are not available or configured incorrectly
	 */
	protected PropertiesConfigurator getConfiguration() throws MojoExecutionException {
		if (settings == null) {
			throw new MojoExecutionException("Maven settings are not available.");
		}

		PropertiesConfigurator config = new PropertiesConfigurator();

		if (serverId != null) {
			Server server = settings.getServer(serverId);
			if (server == null) {
				throw new MojoExecutionException(
						"No <server> with id '" + serverId + "' found in Maven settings.xml.");
			}

			String username = server.getUsername();
			if (username != null && !username.isBlank()) {
				config.set("GENAI_USERNAME", username);
			}
			String password = server.getPassword();
			if (password != null && !password.isBlank()) {
				config.set("GENAI_PASSWORD", password);
			}
		}

		return config;
	}

	/**
	 * Configures and runs a {@link FileProcessor} scan for the current module.
	 *
	 * @param processor processor instance to configure and run
	 * @throws MojoExecutionException if processing fails
	 */
	protected void scanDocuments(FileProcessor processor) throws MojoExecutionException {
		processor.setExcludes(excludes);

		try {
			if (instructions != null) {
				logger.info("Instructions: {}", StringUtils.abbreviate(instructions, 60));
				processor.setInstructions(instructions);
			}

			if (guidance != null) {
				logger.info("Default Guidance: {}", StringUtils.abbreviate(guidance, 60));
				processor.setDefaultGuidance(guidance);
			}

			logger.info("Scanning documents in the root directory: {}", rootDir);
			processor.setLogInputs(logInputs);

			if (scanDir == null) {
				scanDir = rootDir.getAbsolutePath();
			}

			processor.scanDocuments(rootDir, scanDir);
		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("File processing failed.", e);

		} finally {
			GenAIProviderManager.logUsage();
			logger.info("File processing completed.");
		}
	}

}
