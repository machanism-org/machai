package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven goal ({@code gw}) that runs the MachAI generative-workflow (GW)
 * document-processing pipeline for the current module.
 *
 * <p>
 * The goal scans the module base directory (typically {@code ${basedir}}) for
 * documentation sources and delegates the processing to {@link FileProcessor}.
 * </p>
 *
 * <h2>Credentials</h2>
 * <p>
 * GenAI credentials are read from Maven {@code settings.xml} using the
 * configured {@code &lt;server&gt;} entry identified by {@link #serverId}. If
 * present, they are exposed to the workflow via system properties:
 * </p>
 * <ul>
 * <li>{@code GENAI_USERNAME}</li>
 * <li>{@code GENAI_PASSWORD}</li>
 * </ul>
 *
 * <h2>Parameters</h2>
 * <ul>
 * <li>{@code gw.genai} ({@link #genai}, optional): GenAI provider/model
 * identifier forwarded to the workflow (for example {@code OpenAI:gpt-5}).</li>
 * <li>{@code gw.instructions} ({@link #instructions}, optional): One or more
 * instruction location strings consumed by the workflow.</li>
 * <li>{@code gw.genai.serverId} ({@link #serverId}, required): Maven
 * {@code settings.xml} {@code &lt;server&gt;} id used to read credentials.</li>
 * <li>{@code gw.threads} ({@link #threads}, optional, default {@code true}):
 * Enables/disables multi-threaded document processing.</li>
 * </ul>
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true)
public class GW extends AbstractMojo {

	private static final Logger logger = LoggerFactory.getLogger(GW.class);

	/**
	 * Optional GenAI provider/model identifier to pass to the workflow.
	 */
	@Parameter(property = "gw.genai")
	String genai;

	/**
	 * Optional instruction locations to pass to the workflow.
	 */
	@Parameter(property = "gw.instructions", required = false, readonly = false, name = "instructions")
	private String[] instructions;

	/**
	 * Optional instruction locations to pass to the workflow.
	 */
	@Parameter(property = "gw.excludes", required = false, readonly = false, name = "excludes")
	private String[] excludes;

	/**
	 * The Maven module base directory to scan for documentation sources.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	File basedir;

	/**
	 * The current Maven project.
	 *
	 * <p>
	 * This mojo primarily operates on the filesystem and the session project list;
	 * however, Maven injects the project instance and it is used to derive the
	 * non-recursive behavior.
	 * </p>
	 */
	@Parameter(readonly = true, defaultValue = "${project}")
	@SuppressWarnings("unused")
	MavenProject project;

	/**
	 * The Maven session used to access the reactor projects.
	 */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	/**
	 * The Maven settings used to resolve credentials from {@code settings.xml}.
	 */
	@Parameter(readonly = true, defaultValue = "${settings}")
	private Settings settings;

	/**
	 * Required {@code settings.xml} {@code &lt;server&gt;} id used to read GenAI
	 * credentials.
	 */
	@Parameter(property = "gw.genai.serverId", required = true)
	private String serverId;

	/**
	 * Enables/disables multi-threaded document processing.
	 */
	@Parameter(property = "gw.threads", defaultValue = "true", required = false)
	private boolean threads;

	/**
	 * Configures credentials and runs document scanning/processing for the current
	 * module.
	 *
	 * @throws MojoExecutionException if Maven settings/credentials are missing or
	 *                                document processing fails
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
			if (ArrayUtils.isNotEmpty(instructions)) {
				processor.setInstructionLocations(instructions);
			}

			logger.info("Scanning documents in the root directory: {}", basedir);
			processor.setModuleMultiThread(threads);
			processor.scanDocuments(basedir);
		} catch (IOException e) {
			throw new MojoExecutionException("Document assistance process failed.", e);
		}
		logger.info("Scanning finished.");
	}

}
