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
 * <dd>
 * GenAI provider/model identifier used for AI-assisted document processing.
 * The format is provider-specific (often {@code ProviderName:ModelName}).
 * <p>
 * Examples: {@code OpenAI:gpt-5}, {@code AzureOpenAI:gpt-4o-mini}
 * </p>
 * </dd>
 *
 * <dt><b>{@code instructions}</b> (property: {@code gw.instructions})</dt>
 * <dd>
 * Optional list of instruction file locations to guide processing.
 *
 * <p>
 * Example value: {@code ${project.basedir}/.gw/instructions.md}
 * </p>
 * </dd>
 *
 * <dt><b>{@code serverId}</b> (property: {@code gw.genai.serverId}; required)</dt>
 * <dd>
 * Maven {@code settings.xml} server id that provides GenAI credentials.
 * If the resolved server contains a username/password, they are exposed to the runtime as
 * system properties {@code GENAI_USERNAME} and {@code GENAI_PASSWORD}.
 * </dd>
 *
 * <dt><b>{@code threads}</b> (property: {@code gw.threads}; default: {@code true})</dt>
 * <dd>Enable multi-threaded processing.</dd>
 *
 * <dt><b>{@code basedir}</b> (read-only; default: {@code ${basedir}})</dt>
 * <dd>Maven project base directory.</dd>
 *
 * <dt><b>{@code project}</b> (read-only; default: {@code ${project}})</dt>
 * <dd>The current {@link MavenProject} (injected by Maven; not used directly by this goal).</dd>
 *
 * <dt><b>{@code settings}</b> (read-only; default: {@code ${settings}})</dt>
 * <dd>The current Maven {@link Settings}, used to resolve {@code serverId} credentials.</dd>
 * </dl>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Run from the command line</h3>
 * <pre>
 * mvn org.machanism.machai:gw-maven-plugin:gw \
 *   -Dgw.genai=OpenAI:gpt-5 \
 *   -Dgw.genai.serverId=genai \
 *   -Dgw.threads=true \
 *   -Dgw.instructions=${project.basedir}/.gw/instructions.md
 * </pre>
 *
 * <h3>Configure in {@code pom.xml}</h3>
 * <pre>
 *  <plugin>
 *    <groupId>org.machanism.machai </groupId>
 *    <artifactId>gw-maven-plugin </artifactId>
 *    <version>${project.version} </version>
 *    <executions>
 *      <execution>
 *        <goals>
 *          <goal>gw </goal>
 *        </goals>
 *      </execution>
 *    </executions>
 *    <configuration>
 *      <genai>OpenAI:gpt-5 </genai>
 *      <serverId>genai </serverId>
 *      <threads>true </threads>
 *      <instructions>
 *        <instruction>${project.basedir}/.gw/instructions.md </instruction>
 *      </instructions>
 *    </configuration>
 *  </plugin>
 * </pre>
 *
 * <h3>Required {@code settings.xml} server entry</h3>
 * <pre>
 *  <settings>
 *    <servers>
 *      <server>
 *        <id>genai </id>
 *        <username>... </username>
 *        <password>... </password>
 *      </server>
 *    </servers>
 *  </settings>
 * </pre>
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true)
public class GW extends AbstractMojo {

	private static final Logger logger = LoggerFactory.getLogger(GW.class);

	@Parameter(property = "gw.genai")
	protected String genai;

	@Parameter(property = "gw.instructions", required = false, readonly = false, name = "instructions")
	private String[] instructions;

	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	@Parameter(readonly = true, defaultValue = "${settings}")
	private Settings settings;

	@Parameter(property = "gw.genai.serverId", required = true)
	private String serverId;

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
