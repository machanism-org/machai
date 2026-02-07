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

/**
 * Maven goal ({@code gw}) that runs the MachAI generative-workflow (GW)
 * document-processing pipeline.
 *
 * <p>
 * The mojo scans documentation sources starting at the current module base
 * directory (typically {@code ${basedir}}) and delegates processing to
 * {@link FileProcessor}.
 * </p>
 *
 * <h2>Parameters</h2>
 * <ul>
 * <li><b>{@code gw.genai}</b> ({@link #genai}, optional): GenAI provider/model
 * identifier forwarded to the workflow (for example {@code OpenAI:gpt-5}).</li>
 * <li><b>{@code gw.instructions}</b> ({@link #instructions}, optional): One or
 * more instruction location strings consumed by the workflow.</li>
 * <li><b>{@code gw.excludes}</b> ({@link #excludes}, optional): Exclude
 * patterns/paths to skip during scanning.</li>
 * <li><b>{@code gw.genai.serverId}</b> ({@link #serverId}, required): Maven
 * {@code settings.xml} {@code &lt;server&gt;} id used to resolve GenAI
 * credentials.</li>
 * <li><b>{@code gw.threads}</b> ({@link #threads}, optional, default
 * {@code true}): Enables/disables multi-threaded processing.</li>
 * </ul>
 *
 * <h2>Credentials</h2>
 * <p>
 * GenAI credentials are read from Maven {@code settings.xml} using the
 * configured {@code &lt;server&gt;} entry identified by {@link #serverId}. When
 * present, they are exposed to the workflow as configuration properties:
 * </p>
 * <ul>
 * <li>{@code GENAI_USERNAME}</li>
 * <li>{@code GENAI_PASSWORD}</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * Run from the command line:
 * </p>
 * 
 * <pre>
 * mvn org.machanism.machai:gw-maven-plugin:gw -Dgw.genai=OpenAI:gpt-5 -Dgw.genai.serverId=genai
 * </pre>
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true)
public class GW extends AbstractMojo {

	private static final Logger logger = LoggerFactory.getLogger(GW.class);

	/**
	 * GenAI provider/model identifier to pass to the workflow.
	 */
	@Parameter(property = "gw.genai")
	String genai;

	/**
	 * The Maven module base directory used as the scan root.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = false)
	File rootDir;

	@Parameter(required = false, readonly = false)
	String scanDir;

	/**
	 * Instruction locations (for example, file paths or classpath locations)
	 * consumed by the workflow.
	 */
	@Parameter(property = "gw.instructions", required = false, readonly = false, name = "instructions")
	private String instructions;

	@Parameter(property = "gw.guidance", required = false, readonly = false, name = "guidance")
	private String guidance;

	/**
	 * Exclude patterns/paths that should be skipped when scanning documentation
	 * sources.
	 */
	@Parameter(property = "gw.excludes", required = false, readonly = false, name = "excludes")
	private String[] excludes;

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
	MavenProject project;

	/**
	 * The Maven session used to access reactor projects.
	 */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	/**
	 * The Maven settings used to resolve credentials from {@code settings.xml}.
	 */
	@Parameter(readonly = true, defaultValue = "${settings}")
	private Settings settings;

	/**
	 * {@code settings.xml} {@code &lt;server&gt;} id used to read GenAI
	 * credentials.
	 */
	@Parameter(property = "gw.genai.serverId", required = true)
	private String serverId;

	/**
	 * Enables/disables multi-threaded document processing.
	 */
	@Parameter(property = "gw.threads", defaultValue = "true", required = false)
	private boolean threads;

	@Parameter(property = "gw.logInputs", defaultValue = "false", required = false)
	private boolean logInputs;

	/**
	 * Configures credentials and runs document scanning/processing starting from
	 * {@link #rootDir}.
	 *
	 * @throws MojoExecutionException if required Maven settings/credentials are
	 *                                missing or if document processing fails
	 */
	@Override
	public void execute() throws MojoExecutionException {
		if (settings == null) {
			throw new MojoExecutionException("Maven settings are not available.");
		}
		if (serverId == null || serverId.trim().isEmpty()) {
			throw new MojoExecutionException("Parameter gw.genai.serverId is required.");
		}

		Server server = settings.getServer(serverId);
		if (server == null) {
			throw new MojoExecutionException("No <server> with id '" + serverId + "' found in Maven settings.xml.");
		}

		PropertiesConfigurator config = new PropertiesConfigurator();

		String username = server.getUsername();
		if (username != null && !username.isBlank()) {
			config.set("GENAI_USERNAME", username);
		}
		String password = server.getPassword();
		if (password != null && !password.isBlank()) {
			config.set("GENAI_PASSWORD", password);
		}

		List<MavenProject> modules = session.getAllProjects();
		boolean nonRecursive = project.getModules().size() > 1 && modules.size() == 1;

		FileProcessor processor = new FileProcessor(genai, config) {
			/**
			 * Creates a project layout for a Maven module, enriching it with the reactor
			 * model when available.
			 *
			 * @param projectDir directory containing the Maven project
			 * @return the resolved project layout
			 * @throws FileNotFoundException if the project directory does not exist
			 */
			@Override
			protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = super.getProjectLayout(projectDir);
				projectLayout.projectDir(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;

					mavenProjectLayout.effectivePomRequired(false);
					Model model = mavenProjectLayout.getModel();
					List<MavenProject> projects = session.getAllProjects();
					for (MavenProject mavenProject : projects) {
						if (StringUtils.equals(mavenProject.getArtifactId(), model.getArtifactId())) {
							mavenProjectLayout.model(mavenProject.getModel());
							break;
						}
					}
				}

				return projectLayout;
			}
		};

		processor.setExcludes(excludes);
		processor.setNonRecursive(nonRecursive);

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
			processor.setModuleMultiThread(threads);
			processor.setLogInputs(logInputs);

			if (scanDir == null) {
				scanDir = rootDir.getAbsolutePath();
			}

			processor.scanDocuments(rootDir, scanDir);
		} catch (IOException e) {
			throw new MojoExecutionException("Document assistance process failed.", e);
		}
		
		GenAIProviderManager.logUsage();
		
		logger.info("Scanning finished.");
	}

}
