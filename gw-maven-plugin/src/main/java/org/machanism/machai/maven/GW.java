package org.machanism.machai.maven;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.machanism.machai.gw.FileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven goal that runs the MachAI generative-workflow (GW) document processing over the current module's base directory.
 *
 * <p>
 * This mojo reads GenAI credentials from Maven {@code settings.xml} using a configured {@code <server>} entry and exposes
 * them to the running process as system properties expected by the underlying workflow:
 * </p>
 * <ul>
 *   <li>{@code GENAI_USERNAME}</li>
 *   <li>{@code GENAI_PASSWORD}</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <ul>
 *   <li><b>{@code gw.genai}</b> (optional): GenAI provider/model identifier forwarded to {@link FileProcessor}.
 *       Example: {@code OpenAI:gpt-5}.</li>
 *   <li><b>{@code gw.instructions}</b> (optional): One or more instruction location strings consumed by the workflow.</li>
 *   <li><b>{@code gw.genai.serverId}</b> (required): Maven {@code settings.xml} server id containing credentials.</li>
 *   <li><b>{@code gw.threads}</b> (optional, default {@code true}): Enables/disables multi-threaded processing.</li>
 * </ul>
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true)
public class GW extends AbstractMojo {

	private static final Logger logger = LoggerFactory.getLogger(GW.class);

	/**
	 * Optional GenAI provider/model identifier to pass to the workflow (for example {@code OpenAI:gpt-5}).
	 */
	@Parameter(property = "gw.genai") String genai;

	/**
	 * Optional instruction locations to pass to the workflow.
	 */
	@Parameter(property = "gw.instructions", required = false, readonly = false, name = "instructions")
	private String[] instructions;

	/**
	 * The Maven module base directory to scan for documentation sources.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true) File basedir;

	/**
	 * The Maven project (injected by Maven). This plugin does not currently use it directly, but Maven requires the
	 * injection point for certain build contexts.
	 */
	@Parameter(readonly = true, defaultValue = "${project}")
	@SuppressWarnings("unused") MavenProject project;

	/**
	 * The Maven settings (injected by Maven) used to resolve credentials from {@code settings.xml}.
	 */
	@Parameter(readonly = true, defaultValue = "${settings}")
	private Settings settings;

	/**
	 * Required server id used to read credentials from Maven {@code settings.xml}.
	 */
	@Parameter(property = "gw.genai.serverId", required = true)
	private String serverId;

	/**
	 * Enables/disables multi-threaded document processing.
	 */
	@Parameter(property = "gw.threads", defaultValue = "true", required = false)
	private boolean threads;

	/**
	 * Executes the {@code gw} goal by configuring credentials and delegating the scan to {@link FileProcessor}.
	 *
	 * @throws MojoExecutionException if required Maven settings/credentials are missing or the document scan fails
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

		String username = server.getUsername();
		if (username != null && !username.isBlank()) {
			System.setProperty("GENAI_USERNAME", username);
		}
		String password = server.getPassword();
		if (password != null && !password.isBlank()) {
			System.setProperty("GENAI_PASSWORD", password);
		}

		FileProcessor documents = new FileProcessor(genai);

		if (ArrayUtils.isNotEmpty(instructions)) {
			documents.setInstructionLocations(instructions);
		}

		logger.info("Scanning documents in the root directory: {}", basedir);
		try {
			documents.setModuleMultiThread(threads);
			documents.scanDocuments(basedir);
		} catch (IOException e) {
			throw new MojoExecutionException("Document assistance process failed.", e);
		}
		logger.info("Scanning finished.");
	}

}
