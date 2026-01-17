package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
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
 * The value format is provider-specific, commonly
 * {@code ProviderName:ModelName}.
 * </p>
 * <p>
 * Examples: {@code OpenAI:gpt-5}, {@code AzureOpenAI:gpt-4o-mini}
 * </p>
 * </dd>
 *
 * <dt><b>{@code basedir}</b> (read-only; default: {@code ${basedir}})</dt>
 * <dd>Maven project base directory.</dd>
 *
 * <dt><b>{@code project}</b> (read-only; default: {@code ${project}})</dt>
 * <dd>The current {@link MavenProject}.</dd>
 *
 * <dt><b>{@code session}</b> (read-only; default: {@code ${session}})</dt>
 * <dd>The current {@link MavenSession}.</dd>
 * </dl>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Run from the command line</h3>
 *
 * <pre>
 * mvn org.machanism.machai:gw-maven-plugin:gw -Dgw.genai=OpenAI:gpt-5
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
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 */
@Mojo(name = "gw", threadSafe = true)
public class GW extends AbstractMojo {

	/** Logger for this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(GW.class);

	/**
	 * GenAI provider/model identifier used for AI-assisted document processing.
	 *
	 * <p>
	 * May be provided as a system property: {@code -Dgw.genai=...}.
	 * </p>
	 *
	 * <p>
	 * Example values: {@code OpenAI:gpt-5}, {@code AzureOpenAI:gpt-4o-mini}
	 * </p>
	 */
	@Parameter(property = "gw.genai")
	protected String genai;

	@Parameter(property = "gw.instructions")
	protected File instructions;

	/** Maven project base directory (read-only). */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/** Maven project (read-only). */
	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	/** Maven session (read-only). */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Override
	public void execute() throws MojoExecutionException {
		FileProcessor documents = new FileProcessor(genai) {
			@Override
			protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				MavenProjectLayout projectLayout = new MavenProjectLayout();
				projectLayout.projectDir(basedir);
				Model model = project.getModel();
				projectLayout.model(model);
				return projectLayout;
			}

			@Override
			protected void processModule(File projectDir, String module) throws IOException {
				// No-op for this implementation
			}
		};

		if (instructions != null) {
			try {
				documents.setInstructions(Files.readString(instructions.toPath()));
			} catch (IOException e) {
				LOGGER.info("Failed to read instructions from file: {}", instructions.getAbsolutePath());
			}
		}

		LOGGER.info("Scanning documents in the root directory: {}", basedir);
		try {
			File rootDir = new File(session.getExecutionRootDirectory());
			documents.scanDocuments(rootDir, basedir);
		} catch (IOException e) {
			throw new MojoExecutionException("Document assistance process failed.", e);
		}
		LOGGER.info("Scanning finished.");
	}
}
