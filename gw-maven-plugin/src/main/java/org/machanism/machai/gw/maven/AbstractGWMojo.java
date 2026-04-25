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
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.gw.maven.tools.ClassFunctionalTools;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for GWMojo Maven plugin goals.
 *
 * <p>
 * This class centralizes shared configuration parameters and the common
 * scan/execute flow. Concrete goals typically configure goal-specific behavior
 * and delegate to {@link #scanDocuments(GuidanceProcessor)}.
 * </p>
 *
 * <p>
 * It also resolves optional GenAI credentials from Maven {@code settings.xml}
 * and exposes them to the processing pipeline through a
 * {@link PropertiesConfigurator}.
 * </p>
 */
public abstract class AbstractGWMojo extends AbstractMojo {

	static final Logger logger = LoggerFactory.getLogger(AbstractGWMojo.class);

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
	 * Instruction locations consumed by the workflow.
	 */
	@Parameter(property = Ghostwriter.INSTRUCTIONS_PROP_NAME, name = "instructions")
	protected String instructions;
	/**
	 * Exclude patterns or paths skipped during scanning.
	 */
	@Parameter(property = Ghostwriter.EXCLUDES_PROP_NAME, name = "excludes")
	protected String[] excludes;
	/**
	 * The current Maven project.
	 */
	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;
	/**
	 * The current Maven session.
	 */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	protected MavenSession session;
	/**
	 * Maven settings used to resolve credentials from {@code settings.xml}.
	 */
	@Parameter(readonly = true, defaultValue = "${settings}")
	private Settings settings;
	/**
	 * Maven {@code server} id used to resolve GenAI credentials.
	 */
	@Parameter(property = Genai.SERVERID_PROP_NAME, required = false)
	private String serverId;
	/**
	 * Whether to log the list of workflow input files.
	 */
	@Parameter(property = Genai.LOG_INPUTS_PROP_NAME, defaultValue = "false")
	protected boolean logInputs;
	/**
	 * Reactor projects available in the current Maven session.
	 */
	@Parameter(defaultValue = "${reactorProjects}", readonly = true)
	protected List<MavenProject> reactorProjects;

	/**
	 * Tool set exposed to the processor for class-related project introspection.
	 */
	protected ClassFunctionalTools classFunctionTools = new ClassFunctionalTools();

	/**
	 * Creates the base mojo instance.
	 */
	protected AbstractGWMojo() {
		super();
	}

	/**
	 * Builds the processor configuration.
	 *
	 * <p>
	 * If a Maven server id is configured, this method reads the matching server
	 * entry from {@code settings.xml} and copies its username, password, and any
	 * custom XML configuration values into the returned configurator.
	 * </p>
	 *
	 * @return configuration for downstream workflow execution
	 * @throws MojoExecutionException if Maven settings are unavailable or the
	 *                                configured server cannot be found
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
	 * Configures and executes document scanning for the current project context.
	 *
	 * <p>
	 * This method applies configured excludes, optional instructions, input logging,
	 * and scan directory selection before invoking
	 * {@link GuidanceProcessor#scanDocuments(File, String)}. When a Maven project is
	 * present in the request, class-related helper tools are also registered with
	 * the processor.
	 * </p>
	 *
	 * @param processor the processor to configure and execute
	 * @throws MojoExecutionException if scanning or processing fails
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

			logger.info("Starting scan of path: {}", scanDir);
			if (session.getRequest().isProjectPresent()) {
				processor.addTool(classFunctionTools);
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
