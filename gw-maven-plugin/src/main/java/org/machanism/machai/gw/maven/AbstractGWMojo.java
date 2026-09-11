/* @guidance: >>> ${guidances}/def-class-javadoc.md */
package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.gw.maven.tools.ClassFunctionalTools;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Maven goals that scan project files for guidance comments and
 * delegate processing to a {@link GuidanceProcessor}.
 *
 * <p>
 * The mojo resolves Maven project/session context, optional scanner inputs, and
 * GenAI provider credentials from Maven settings before executing the scan. It
 * is intended to be extended by concrete plugin goals that create and configure
 * the processor instance.
 * </p>
 *
 * <h2>Parameters</h2>
 * <ul>
 * <li>{@code model}: Provider or model identifier supplied with {@code -D}
 * followed by {@link GWConstants#MODEL_PROP_NAME}; for example,
 * {@code mvn machai:goal -D} followed by {@link GWConstants#MODEL_PROP_NAME}
 * followed by {@code =openai:gpt-4o-mini}.</li>
 * <li>{@code basedir}: Maven module base directory. The default value is the
 * Maven expression {@code ${basedir}}; for example, Maven injects the current
 * module directory automatically during normal plugin execution.</li>
 * <li>{@code path}: Optional file, directory, glob, or pattern to scan,
 * supplied with {@code -D} followed by {@link GWConstants#PATH_PROP_NAME}; for
 * example, {@code mvn machai:goal -D} followed by
 * {@link GWConstants#PATH_PROP_NAME} followed by {@code =src/main/java}.</li>
 * <li>{@code instructions}: Additional workflow instructions, supplied with
 * {@code -D} followed by {@link GWConstants#INSTRUCTIONS_PROP_NAME}; for
 * example, {@code mvn machai:goal -D} followed by
 * {@link GWConstants#INSTRUCTIONS_PROP_NAME} followed by
 * {@code ="Keep public APIs backward compatible"}.</li>
 * <li>{@code excludes}: Paths or patterns excluded from scanning, supplied with
 * {@code -D} followed by {@link GWConstants#EXCLUDES_PROP_NAME}; for example,
 * {@code mvn machai:goal -D} followed by {@link GWConstants#EXCLUDES_PROP_NAME}
 * followed by {@code =target,build}.</li>
 * <li>{@code project}: Current Maven project injected from the Maven expression
 * {@code ${project}}; for example, this is used automatically to locate the
 * project base directory.</li>
 * <li>{@code session}: Current Maven session injected from the Maven expression
 * {@code ${session}}; for example, this is used automatically to resolve the
 * execution root directory.</li>
 * <li>{@code settings}: Maven settings injected from the Maven expression
 * {@code ${settings}}; for example, this is used automatically to look up
 * server credentials from {@code settings.xml}.</li>
 * <li>{@code serverId}: Maven server id for GenAI credentials, supplied with
 * {@code -D} followed by {@link #SERVERID_PROP_NAME}; for example,
 * {@code mvn machai:goal -D} followed by {@link #SERVERID_PROP_NAME} followed
 * by {@code =machai-ai}. The corresponding {@code <server>} entry may define
 * username, password, and custom configuration values.</li>
 * <li>{@code params}: Additional key-value configuration entries, supplied as
 * plugin configuration; for example,
 * {@code <params><endpoint>https://api.example.test</endpoint></params>} adds
 * an {@code endpoint} property to the workflow configuration.</li>
 * <li>{@code configFile}: Optional configuration file, supplied with {@code -D}
 * followed by {@link GWConstants#CONFIG_PROP_NAME}; for example,
 * {@code mvn machai:goal -D} followed by {@link GWConstants#CONFIG_PROP_NAME}
 * followed by {@code =machai.properties}. It is used when no Maven server id is
 * configured.</li>
 * </ul>
 *
 * <p>
 * When documenting XML snippets in this Javadoc, write any literal closing
 * Javadoc delimiter as {@code *&#47;} rather than the raw delimiter.
 * </p>
 *
 * @since 1.1.2
 */
/*
 * @guidance: # Generate detailed Javadoc documentation for the Maven plugin,
 * including: - A clear description of functionality and all Maven plugin
 * parameters. - Examples of usage for each parameter. - Escape the closing
 * javadoc tag in javadoc content, as it was breaking javadoc compilation.
 * 
 * @since 1.1.2
 */
public abstract class AbstractGWMojo extends AbstractMojo {

	/**
	 * Logger shared by Maven goal implementations in this package. It records
	 * configuration and scanning lifecycle events.
	 */
	static final Logger logger = LoggerFactory.getLogger(AbstractGWMojo.class);

	/**
	 * Maven property name for the target GenAI server identifier. Supply its value
	 * with {@code -Dgenai.serverId=server-id}.
	 */
	public static final String SERVERID_PROP_NAME = "genai.serverId";

	/**
	 * Provider/model identifier to pass to the workflow, optionally supplied with
	 * the {@value GWConstants#MODEL_PROP_NAME} Maven property.
	 */
	protected String model;

	/**
	 * Maven module base directory injected from the {@code ${basedir}} expression.
	 */
	protected File basedir;

	/**
	 * Optional scan root override. When absent, scanning starts at the Maven
	 * execution root directory.
	 */
	protected String path;

	/**
	 * Additional instructions passed to the workflow before scanning.
	 */
	protected String instructions;

	/**
	 * Paths or patterns skipped during scanning.
	 */
	protected String[] excludes;

	/**
	 * Current Maven project, used to determine the module base directory and
	 * whether project-aware tools should be registered.
	 */
	protected MavenProject project;

	/**
	 * Current Maven session, used to obtain execution-root and request context.
	 */
	protected MavenSession session;

	/**
	 * Maven settings used to resolve credentials and custom configuration from
	 * {@code settings.xml}.
	 */
	protected Settings settings;

	/**
	 * Maven {@code server} id used to resolve GenAI credentials and custom server
	 * configuration.
	 */
	protected String serverId;

	/**
	 * Additional key-value configuration entries merged into the processor
	 * configuration.
	 *
	 * <p>
	 * For example, plugin XML can provide
	 * {@code <params><timeout>30</timeout></params>}.
	 * </p>
	 */
	protected Map<String, String> params;

	/**
	 * Optional configuration file loaded before Maven server and explicit parameter
	 * values are applied. For example, {@code -D} followed by
	 * {@link GWConstants#CONFIG_PROP_NAME} followed by {@code =machai.properties}
	 * selects a custom configuration file.
	 */
	protected File configFile;

	/**
	 * Tool set exposed to the processor for class-related project introspection when
	 * Maven is executing with a project.
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

		PropertiesConfigurator config = new PropertiesConfigurator();

		try {
			String configPath = configFile != null ? configFile.getAbsolutePath() : GWConstants.GW_CONFIG_FILE_NAME;
			config.setConfiguration(configPath);
			logger.info("Configuration successfully loaded from: " + configPath);
		} catch (IOException e) {
			if (configFile != null) {
				throw new MojoExecutionException("Failed to load configuration from: " + configFile, e);
			}
		}

		if (serverId != null) {
			if (settings == null) {
				throw new MojoExecutionException("Maven settings are not available.");
			}

			Server server = settings.getServer(serverId);
			if (server == null) {
				throw new MojoExecutionException("No <server> with id '" + serverId + "' found in Maven settings.xml.");
			}

			String username = server.getUsername();
			if (StringUtils.isNotBlank(username)) {
				config.set(AbstractAIProvider.USERNAME_PROP_NAME, username);
			}
			String password = server.getPassword();
			if (StringUtils.isNotBlank(password)) {
				config.set(AbstractAIProvider.PASSWORD_PROP_NAME, password);
			}

			if (server.getConfiguration() instanceof Xpp3Dom) {
				Xpp3Dom configuration = (Xpp3Dom) server.getConfiguration();
				Xpp3Dom[] children = configuration.getChildren();
				for (Xpp3Dom xpp3Dom : children) {
					config.set(xpp3Dom.getName(), xpp3Dom.getValue());
				}
			}
		}

		if (params != null) {
			params.entrySet().stream().forEach(e -> config.set(e.getKey(), e.getValue()));
		}

		return config;
	}

	/**
	 * Configures and executes document scanning for the current project context.
	 *
	 * <p>
	 * This method applies configured excludes, optional instructions, input
	 * logging, and scan directory selection before invoking
	 * {@link GuidanceProcessor#scanDocuments(File, String)}. When a Maven project
	 * is present in the request, class-related helper tools are also registered
	 * with the processor.
	 * </p>
	 *
	 * @param processor the non-null processor to configure and execute
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
					logger.info("Instructions: {}",
							StringUtils.abbreviate(instructions, AbstractAIProvider.LOG_LINE_LENG));
				}
				processor.setInstructions(instructions);
			}

			File projectDir = new File(session.getExecutionRootDirectory());

			if (path == null) {
				path = projectDir.getAbsolutePath();
			}

			logger.info("Starting scan of path: `{}`", path);
			if (session.getRequest().isProjectPresent()) {
				processor.addTool(classFunctionTools);
			}

			processor.scanDocuments(projectBasedir, path);
			logger.info("Scanning finished.");

		} catch (Exception e) {
			throw new MojoExecutionException("File processing failed.", e);

		} finally {
			UsageStatistics.logUsage();
			logger.info("File processing finished.");
		}
	}

	/**
	 * Sets Maven settings used to look up the configured GenAI server.
	 *
	 * @param settings Maven settings injected by the plugin runtime; may be
	 *                 {@code null} outside normal Maven execution
	 */
	@Parameter(readonly = true, defaultValue = "${settings}")
	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	/**
	 * Sets the Maven session that supplies the execution root and request context.
	 *
	 * @param session Maven session injected by the plugin runtime
	 */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	public void setSession(MavenSession session) {
		this.session = session;
	}

	/**
	 * Sets the provider or model identifier passed to the workflow.
	 *
	 * @param model provider/model identifier, or {@code null} to use configured
	 *              defaults
	 */
	@Parameter(property = GWConstants.MODEL_PROP_NAME)
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * Sets the Maven module base directory.
	 *
	 * @param basedir module base directory injected by Maven
	 */
	@Parameter(defaultValue = "${basedir}", required = true)
	public void setBasedir(File basedir) {
		this.basedir = basedir;
	}

	/**
	 * Sets the optional file, directory, glob, or pattern to scan.
	 *
	 * @param path scan input, or {@code null} to scan from the execution root
	 */
	@Parameter(property = GWConstants.PATH_PROP_NAME, name = "path")
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Sets additional instructions for the workflow.
	 *
	 * @param instructions workflow instructions, or {@code null} when none are
	 *                     provided
	 */
	@Parameter(property = GWConstants.INSTRUCTIONS_PROP_NAME, name = "instructions")
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * Sets paths or patterns excluded from document scanning.
	 *
	 * @param excludes excluded paths or patterns, or {@code null} for no explicit
	 *                 exclusions
	 */
	@Parameter(property = GWConstants.EXCLUDES_PROP_NAME, name = "excludes")
	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	/**
	 * Sets the current Maven project.
	 *
	 * @param project Maven project injected by the plugin runtime
	 */
	@Parameter(readonly = true, defaultValue = "${project}")
	public void setProject(MavenProject project) {
		this.project = project;
	}

	/**
	 * Sets the Maven server id used to obtain GenAI credentials.
	 *
	 * @param serverId id of a {@code <server>} entry in {@code settings.xml}, or
	 *                 {@code null} to rely on file-based configuration
	 */
	@Parameter(property = SERVERID_PROP_NAME, required = false)
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/**
	 * Sets explicit key-value configuration entries. These values override values
	 * loaded from the configuration file and Maven server configuration.
	 *
	 * @param params configuration entries, or {@code null} when none are supplied
	 */
	@Parameter
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	/**
	 * Sets the optional configuration file to load.
	 *
	 * @param configFile configuration file, or {@code null} to use the default
	 *                   workflow configuration file when available
	 */
	@Parameter(property = GWConstants.CONFIG_PROP_NAME, required = false)
	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}

}
