package org.machanism.machai.gw.maven;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for GW Maven plugin goals.
 *
 * <p>
 * This class centralizes shared configuration parameters and the common
 * scan/execute flow. Concrete goals typically configure goal-specific behavior
 * (for example, reactor aggregation, processing order, or threading) and then
 * delegate to {@link #scanDocuments(GuidanceProcessor)}.
 * </p>
 *
 * <h2>Common parameters</h2>
 * <ul>
 * <li>{@code -Dgw.model} - Provider/model identifier to pass to the
 * workflow.</li>
 * <li>{@code -Dgw.scanDir} - Optional scan root override. When omitted,
 * defaults to the module base directory.</li>
 * <li>{@code -Dgw.instructions} - Instruction locations (for example, file
 * paths or classpath locations) consumed by the workflow.</li>
 * <li>{@code -Dgw.guidance} - Default guidance text forwarded to the
 * workflow.</li>
 * <li>{@code -Dgw.excludes} - Exclude patterns/paths to skip while scanning
 * documentation sources.</li>
 * <li>{@code -Dgw.model.serverId} - {@code settings.xml} {@code &lt;server&gt;}
 * id used to read GenAI credentials.</li>
 * <li>{@code -DlogInputs} (default {@code false}) - Logs the list of input
 * files passed to the workflow.</li>
 * </ul>
 *
 * <h2>Credentials</h2>
 * <p>
 * If {@code -Dgenai.serverId} is provided, this goal reads credentials from
 * {@code ~/.m2/settings.xml} {@code &lt;servers&gt;&lt;server&gt;} and forwards
 * them to the workflow as {@code GENAI_USERNAME}/{@code GENAI_PASSWORD}
 * properties.
 * </p>
 *
 * <h2>Examples</h2>
 * 
 * <pre>
 * mvn gw:gw -Dgw.model=openai:gpt-4o-mini -Dgw.scanDir=src\\site -DlogInputs=true
 * </pre>
 */
public abstract class AbstractGWGoal extends AbstractMojo {

	static final Logger logger = LoggerFactory.getLogger(AbstractGWGoal.class);

	/**
	 * Provider/model identifier to pass to the workflow.
	 */
	@Parameter(property = Ghostwriter.MODEL_PROP_NAME)
	protected String model;
	/**
	 * The Maven module base directory.
	 */
	@Parameter(defaultValue = "${basedir}", required = true)
	protected File basedir;
	/**
	 * Optional scan root override.
	 */
	@Parameter(property = Ghostwriter.SCAN_DIR_PROP_NAME)
	String scanDir;
	/**
	 * Instruction locations (for example, file paths or classpath locations)
	 * consumed by the workflow.
	 */
	@Parameter(property = Ghostwriter.INSTRUCTIONS_PROP_NAME, name = "instructions")
	protected String instructions;
	/**
	 * Exclude patterns/paths that should be skipped when scanning documentation
	 * sources.
	 */
	@Parameter(property = Ghostwriter.EXCLUDES_PROP_NAME, name = "excludes")
	protected String[] excludes;
	/**
	 * The current Maven project.
	 *
	 * <p>
	 * Used to derive whether the invocation is effectively non-recursive (for
	 * example, when running against a parent POM without building the full
	 * reactor).
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
	@Parameter(property = Genai.SERVERID_PROP_NAME, required = false)
	private String serverId;
	/**
	 * Whether to log the list of input files passed to the workflow.
	 */
	@Parameter(property = Genai.LOG_INPUTS_PROP_NAME, defaultValue = "false")
	protected boolean logInputs;
	/**
	 * Reactor projects for the current Maven session.
	 */
	@Parameter(defaultValue = "${reactorProjects}", readonly = true)
	protected List<MavenProject> reactorProjects;

	protected AbstractGWGoal() {
		super();
	}

	/**
	 * Builds a {@link PropertiesConfigurator} for workflow execution.
	 *
	 * <p>
	 * When {@code genai.serverId} is set, credentials are read from Maven settings.
	 * </p>
	 *
	 * @return a configurator populated with any available workflow properties
	 * @throws MojoExecutionException if Maven settings are not available or
	 *                                configured incorrectly
	 */
	protected PropertiesConfigurator getConfiguration() throws MojoExecutionException {
		if (settings == null) {
			throw new MojoExecutionException("Maven settings are not available.");
		}

		PropertiesConfigurator config = new PropertiesConfigurator();

		if (serverId != null) {
			Server server = settings.getServer(serverId);
			if (server == null) {
				throw new MojoExecutionException("No <server> with id '" + serverId + "' found in Maven settings.xml.");
			}

			String username = server.getUsername();
			if (StringUtils.isNotBlank(username)) {
				config.set(Genai.USERNAME_PROP_NAME, username);
			}
			String password = server.getPassword();
			if (StringUtils.isNotBlank(password)) {
				config.set(Genai.PASSWORD_PROP_NAME, password);
			}

			if (server.getConfiguration() instanceof Xpp3Dom) {
				Xpp3Dom configuration = (Xpp3Dom) server.getConfiguration();
				Xpp3Dom[] children = configuration.getChildren();
				for (Xpp3Dom xpp3Dom : children) {
					config.set(xpp3Dom.getName(), xpp3Dom.getValue());
				}
			}
		}

		return config;
	}

	/**
	 * Configures and runs a {@link GuidanceProcessor} scan for the current module.
	 *
	 * @param processor processor instance to configure and run
	 * @throws MojoExecutionException if processing fails
	 */
	protected void scanDocuments(GuidanceProcessor processor) throws MojoExecutionException {

		File projectBasedir = project.getBasedir();
		if (projectBasedir == null) {
			projectBasedir = SystemUtils.getUserDir();
		}

		processor.setExcludes(excludes);

		try {
			if (instructions != null) {
				if (logger.isInfoEnabled()) {
					logger.info("Instructions: {}", StringUtils.abbreviate(instructions, 60));
				}
				processor.setInstructions(instructions);
			}

			File projectDir = new File(session.getExecutionRootDirectory());
			processor.setLogInputs(logInputs);

			if (scanDir == null) {
				scanDir = projectDir.getAbsolutePath();
			}

			processor.scanDocuments(projectBasedir, scanDir);
			logger.info("Scanning finished.");

		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("File processing failed.", e);

		} finally {
			GenaiProviderManager.logUsage();
			logger.info("File processing completed.");
		}
	}

}
