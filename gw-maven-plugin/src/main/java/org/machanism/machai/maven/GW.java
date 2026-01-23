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

/*@guidance: Generate javadoc with a description all maven plugin parameters and examples of usage. */

/**
 * Maven goal that scans and processes project documents.
 *
 * <h2>Parameters</h2>
 * <dl>
 * <dt><b>{@code genai}</b> (property: {@code gw.genai})</dt>
 * <dd>GenAI provider/model identifier used for AI-assisted document processing.
 * <p>
 * The value format is provider-specific, commonly {@code ProviderName:ModelName}.
 * </p>
 * <p>
 * Examples: {@code OpenAI:gpt-5}, {@code AzureOpenAI:gpt-4o-mini}
 * </p>
 * </dd>
 *
 * <dt><b>{@code instructions}</b> (property: {@code gw.instructions})</dt>
 * <dd>Optional list of instruction file locations to guide processing.
 * <p>
 * This parameter may be provided multiple times on the command line depending on your shell,
 * or configured as a list in {@code pom.xml}.
 * </p>
 * </dd>
 *
 * <dt><b>{@code serverId}</b> (property: {@code gw.genai.serverId}; required)</dt>
 * <dd>Maven {@code settings.xml} server id that provides GenAI credentials.
 * <p>
 * If the resolved server contains a username/password, they are exposed to the runtime as
 * {@code GENAI_USERNAME} and {@code GENAI_PASSWORD} system properties.
 * </p>
 * </dd>
 *
 * <dt><b>{@code threads}</b> (property: {@code gw.threads}; default: {@code true})</dt>
 * <dd>Enable multi-threaded processing.</dd>
 *
 * <dt><b>{@code basedir}</b> (read-only; default: {@code ${basedir}})</dt>
 * <dd>Maven project base directory.</dd>
 *
 * <dt><b>{@code project}</b> (read-only; default: {@code ${project}})</dt>
 * <dd>The current {@link MavenProject}.</dd>
 *
 * <dt><b>{@code settings}</b> (read-only; default: {@code ${settings}})</dt>
 * <dd>The current Maven {@link Settings}.</dd>
 * </dl>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Run from the command line</h3>
 *
 * <pre>
 * mvn org.machanism.machai:gw-maven-plugin:gw \
 *   -Dgw.genai=OpenAI:gpt-5 \
 *   -Dgw.genai.serverId=genai
 * </pre>
 *
 * <h3>Configure in {@code pom.xml}</h3>
 *
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.machanism.machai&lt;/groupId&gt;
 *   &lt;artifactId&gt;gw-maven-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;${project.version}&lt;/version&gt;
 *   &lt;executions&gt;
 *     &lt;execution&gt;
 *       &lt;goals&gt;
 *         &lt;goal&gt;gw&lt;/goal&gt;
 *       &lt;/goals&gt;
 *     &lt;/execution&gt;
 *   &lt;/executions&gt;
 *   &lt;configuration&gt;
 *     &lt;genai&gt;OpenAI:gpt-5&lt;/genai&gt;
 *     &lt;serverId&gt;genai&lt;/serverId&gt;
 *     &lt;threads&gt;true&lt;/threads&gt;
 *     &lt;instructions&gt;
 *       &lt;instruction&gt;${project.basedir}/.gw/instructions.md&lt;/instruction&gt;
 *     &lt;/instructions&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * <h3>Required {@code settings.xml} server entry</h3>
 *
 * <pre>
 * &lt;settings&gt;
 *   &lt;servers&gt;
 *     &lt;server&gt;
 *       &lt;id&gt;genai&lt;/id&gt;
 *       &lt;username&gt;...&lt;/username&gt;
 *       &lt;password&gt;...&lt;/password&gt;
 *     &lt;/server&gt;
 *   &lt;/servers&gt;
 * &lt;/settings&gt;
 * </pre>
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true)
public class GW extends AbstractMojo {

	/** Logger for this class. */
	private static final Logger logger = LoggerFactory.getLogger(GW.class);

	/**
	 * GenAI provider/model identifier used for AI-assisted document processing.
	 *
	 * <p>
	 * May be provided as a system property: {@code -Dgw.genai=...}.
	 * </p>
	 */
	@Parameter(property = "gw.genai")
	protected String genai;

	/**
	 * Array of instruction file locations to guide processing.
	 */
	@Parameter(property = "gw.instructions", required = false, readonly = false, name = "instructions")
	private String[] instructions;

	/** Maven project base directory (read-only). */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/** Maven project (read-only). */
	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	/** Maven settings (read-only). */
	@Parameter(property = "settings", readonly = true, defaultValue = "${settings}")
	private Settings settings;

	/**
	 * Maven {@code settings.xml} server id containing GenAI credentials.
	 */
	@Parameter(property = "gw.genai.serverId", required = true)
	private String serverId;

	/**
	 * Enable multi-threaded processing.
	 */
	@Parameter(property = "gw.threads", defaultValue = "true", required = false)
	private boolean threads;

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
		if (username != null) {
			System.setProperty("GENAI_USERNAME", username);
		}
		String password = server.getPassword();
		if (password != null) {
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
