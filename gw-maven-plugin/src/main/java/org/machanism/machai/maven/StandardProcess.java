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
import org.apache.maven.plugins.annotations.Mojo;
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

/*@guidance: Generate javadoc with a description all maven plugin parameters and examples of usage. */

/**
 * Standard Maven goal that scans project documentation sources and processes them
 * using the configured GenAI workflow.
 *
 * <h2>Goal</h2>
 * <pre>
 * mvn gw:std
 * </pre>
 *
 * <h2>Parameters</h2>
 * <p>
 * Parameters can be configured either via system properties (as shown below) or
 * via the plugin configuration in your {@code pom.xml}.
 * </p>
 *
 * <h3>{@code genai} / {@code gw.genai}</h3>
 * <p>
 * Provider/model identifier forwarded to the workflow.
 * </p>
 * <pre>
 * mvn gw:std -Dgw.genai=openai:gpt-4.1-mini
 * </pre>
 *
 * <h3>{@code rootDir}</h3>
 * <p>
 * Maven module base directory. Defaults to {@code ${basedir}}.
 * </p>
 *
 * <h3>{@code scanDir} / {@code gw.scanDir}</h3>
 * <p>
 * Optional scan root override. If not set, defaults to the current module base
 * directory ({@code ${basedir}}).
 * </p>
 * <pre>
 * mvn gw:std -Dgw.scanDir=src\site
 * </pre>
 *
 * <h3>{@code instructions} / {@code gw.instructions}</h3>
 * <p>
 * Instruction locations (for example, file paths or classpath locations)
 * consumed by the workflow.
 * </p>
 * <pre>
 * mvn gw:std -Dgw.instructions=src\site\instructions.md
 * </pre>
 *
 * <h3>{@code guidance} / {@code gw.guidance}</h3>
 * <p>
 * Default guidance text forwarded to the workflow.
 * </p>
 * <pre>
 * mvn gw:std -Dgw.guidance="Write concise release notes."
 * </pre>
 *
 * <h3>{@code excludes} / {@code gw.excludes}</h3>
 * <p>
 * Exclude patterns/paths that should be skipped when scanning documentation
 * sources.
 * </p>
 * <pre>
 * mvn gw:std -Dgw.excludes=**\target\**,**\node_modules\**
 * </pre>
 *
 * <h3>{@code serverId} / {@code gw.genai.serverId}</h3>
 * <p>
 * {@code settings.xml} {@code <server>} id used to read GenAI credentials.
 * When set, the plugin reads {@code username} and {@code password} from the
 * matching {@code <server>} and exposes them to the workflow as
 * {@code GENAI_USERNAME} and {@code GENAI_PASSWORD}.
 * </p>
 * <pre>
 * mvn gw:std -Dgw.genai.serverId=my-genai
 * </pre>
 *
 * <h3>{@code logInputs} / {@code gw.logInputs}</h3>
 * <p>
 * Whether to log the list of input files passed to the workflow.
 * </p>
 * <pre>
 * mvn gw:std -Dgw.logInputs=true
 * </pre>
 *
 * <h2>Example plugin configuration</h2>
 * <pre>{@code
 * <plugin>
 *   <groupId>org.machanism</groupId>
 *   <artifactId>gw-maven-plugin</artifactId>
 *   <version>...</version>
 *   <configuration>
 *     <genai>openai:gpt-4.1-mini</genai>
 *     <scanDir>${project.basedir}\\src\\site</scanDir>
 *     <instructions>${project.basedir}\\src\\site\\instructions.md</instructions>
 *     <guidance>Write concise release notes.</guidance>
 *     <logInputs>false</logInputs>
 *   </configuration>
 * </plugin>
 * }</pre>
 */
@Mojo(name = "std", threadSafe = true)
public class StandardProcess extends AbstractMojo {

	/** Logger for this class. */
	private static final Logger logger = LoggerFactory.getLogger(StandardProcess.class);

	/**
	 * Provider/model identifier to pass to the workflow.
	 */
	@Parameter(property = "gw.genai")
	String genai;

	/**
	 * The Maven module base directory.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = false)
	File rootDir;

	/**
	 * Optional scan root override.
	 */
	@Parameter(property = "gw.scanDir", required = false, readonly = false)
	String scanDir;

	/**
	 * Instruction locations (for example, file paths or classpath locations)
	 * consumed by the workflow.
	 */
	@Parameter(property = "gw.instructions", required = false, readonly = false, name = "instructions")
	protected String instructions;

	/**
	 * Default guidance text forwarded to the workflow.
	 */
	@Parameter(property = "gw.guidance", required = false, readonly = false, name = "guidance")
	protected String guidance;

	/**
	 * Exclude patterns/paths that should be skipped when scanning documentation
	 * sources.
	 */
	@Parameter(property = "gw.excludes", required = false, readonly = false, name = "excludes")
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
	@Parameter(property = "gw.genai.serverId", required = false)
	private String serverId;

	/**
	 * Whether to log the list of input files passed to the workflow.
	 */
	@Parameter(property = "gw.logInputs", defaultValue = "false", required = false)
	protected boolean logInputs;

	@Parameter(defaultValue = "${reactorProjects}", readonly = true)
	private List<MavenProject> reactorProjects;

	@Override
	public void execute() throws MojoExecutionException {
		String executionRootDirectory = session.getExecutionRootDirectory();
		if (!executionRootDirectory.equals(rootDir.getAbsolutePath())) {
			scanDocuments();
		} else {
			new Thread() {
				@Override
				public void run() {
					try {
						while (!reactorProjects.isEmpty()) {
							sleep(500);
						}

						scanDocuments();
					} catch (MojoExecutionException | InterruptedException e) {
						getLog().error(e);
					}
				}
			}.start();
		}
	}

	private void scanDocuments() throws MojoExecutionException {
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

	protected void scanDocuments(FileProcessor processor) throws MojoExecutionException {
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
