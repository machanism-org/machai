package org.machanism.machai.gw.maven;

import java.io.File;
import java.util.List;

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
 * GenAI provider credentials from Maven settings before executing the scan. It is
 * intended to be extended by concrete plugin goals that create and configure the
 * processor instance.
 * </p>
 *
 * <h2>Parameters</h2>
 * <ul>
 * <li>{@code model}: Provider or model identifier supplied with
 * {@code -D} followed by {@link GWConstants#MODEL_PROP_NAME}; for example,
 * {@code mvn machai:goal -D} followed by {@link GWConstants#MODEL_PROP_NAME}
 * followed by {@code =openai:gpt-4o-mini}.</li>
 * <li>{@code basedir}: Maven module base directory. The default value is the
 * Maven expression {@code ${basedir}}; for example, Maven injects the current
 * module directory automatically during normal plugin execution.</li>
 * <li>{@code path}: Optional file, directory, glob, or pattern to scan, supplied
 * with {@code -D} followed by {@link GWConstants#PATH_PROP_NAME}; for example,
 * {@code mvn machai:goal -D} followed by {@link GWConstants#PATH_PROP_NAME}
 * followed by {@code =src/main/java}.</li>
 * <li>{@code instructions}: Additional workflow instructions, supplied with
 * {@code -D} followed by {@link GWConstants#INSTRUCTIONS_PROP_NAME}; for example,
 * {@code mvn machai:goal -D} followed by
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
 * {@code ${settings}}; for example, this is used automatically to look up server
 * credentials from {@code settings.xml}.</li>
 * <li>{@code serverId}: Maven server id for GenAI credentials, supplied with
 * {@code -D} followed by {@link AbstractAIProvider#SERVERID_PROP_NAME}; for
 * example, {@code mvn machai:goal -D} followed by
 * {@link AbstractAIProvider#SERVERID_PROP_NAME} followed by {@code =machai-ai}.
 * The corresponding {@code <server>} entry may define username, password, and
 * custom configuration values.</li>
 * <li>{@code reactorProjects}: Reactor projects injected from the Maven
 * expression {@code ${reactorProjects}}; for example, this is used automatically
 * during multi-module builds.</li>
 * </ul>
 *
 * <p>
 * When documenting XML snippets in this Javadoc, write any literal closing
 * Javadoc delimiter as {@code *&#47;} rather than the raw delimiter.
 * </p>
 *
 * @since 1.1.2
 */
/*@guidance:
 * # Generate detailed Javadoc documentation for the Maven plugin, including:
 * - A clear description of functionality and all Maven plugin parameters.
 * - Examples of usage for each parameter.
 * - Escape the closing javadoc tag in javadoc content, as it was breaking javadoc compilation.
 *  @since 1.1.2
 */
public abstract class AbstractGWMojo extends AbstractMojo {

	static final Logger logger = LoggerFactory.getLogger(AbstractGWMojo.class);

	/**
	 * Provider/model identifier to pass to the workflow.
	 */
	@Parameter(property = GWConstants.MODEL_PROP_NAME)
	protected String model;

	/**
	 * The Maven module base directory.
	 */
	@Parameter(defaultValue = "${basedir}", required = true)
	protected File basedir;

	/**
	 * Optional scan root override.
	 */
	@Parameter(property = GWConstants.PATH_PROP_NAME)
	String path;

	/**
	 * Instruction locations consumed by the workflow.
	 */
	@Parameter(property = GWConstants.INSTRUCTIONS_PROP_NAME, name = "instructions")
	protected String instructions;

	/**
	 * Exclude patterns or path skipped during scanning.
	 */
	@Parameter(property = GWConstants.EXCLUDES_PROP_NAME, name = "excludes")
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
	@Parameter(property = AbstractAIProvider.SERVERID_PROP_NAME, required = false)
	private String serverId;

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
			getLog().error(e);
			throw new MojoExecutionException("File processing failed.", e);

		} finally {
			UsageStatistics.logUsage();
			logger.info("File processing finished.");
		}
	}

}
