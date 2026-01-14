package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*@guidance: Generate javadoc with a description all maven plugin parameters and examples of usage. */

/**
 * Maven Mojo that scans and processes project documents using an optional GenAI provider.
 *
 * <p>
 * This goal delegates document scanning and processing to {@link FileProcessor}. If a GenAI provider is
 * configured via {@link #chatModel}, the processor may use it to assist with document workflows.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * Parameters can be configured via system properties (using {@code -D...}) or within the plugin
 * configuration in the {@code pom.xml}.
 * </p>
 *
 * <dl>
 * <dt><b>{@code gw.genai}</b> (configuration: {@code chatModel})</dt>
 * <dd>
 * The GenAI chat model identifier used for AI-assisted document processing.
 * <p>
 * Value format is provider-specific, typically {@code ProviderName:ModelName}.
 * </p>
 * <p>
 * Example: {@code OpenAI:gpt-5}
 * </p>
 * </dd>
 *
 * <dt><b>{@code basedir}</b></dt>
 * <dd>
 * The Maven project base directory (read-only). Set by Maven to {@code ${basedir}}.
 * </dd>
 *
 * <dt><b>{@code project}</b></dt>
 * <dd>
 * The current {@link MavenProject} (read-only). Set by Maven to {@code ${project}}.
 * </dd>
 *
 * <dt><b>{@code session}</b></dt>
 * <dd>
 * The current {@link MavenSession} (read-only). Set by Maven to {@code ${session}}.
 * </dd>
 * </dl>
 *
 * <h2>Example Usage</h2>
 *
 * <h3>Run from the command line</h3>
 * <pre>
 * mvn org.machanism.machai:gw-maven-plugin:gw -Dgw.genai=OpenAI:gpt-5
 * </pre>
 *
 * <h3>Configure in {@code pom.xml}</h3>
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.machanism.machai&lt;/groupId&gt;
 *   &lt;artifactId&gt;gw-maven-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;${project.version}&lt;/version&gt;
 *   &lt;configuration&gt;
 *     &lt;chatModel&gt;OpenAI:gpt-5&lt;/chatModel&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 */
@Mojo(name = "gw", threadSafe = true)
public class GW extends AbstractMojo {

	/** Logger for this class. */
	private static final Logger logger = LoggerFactory.getLogger(GW.class);

	/**
	 * The GenAI chat model identifier to use for AI assistance.
	 *
	 * <p>
	 * This can be provided as a system property {@code -Dgw.genai=...}.
	 * </p>
	 */
	@Parameter(property = "gw.genai")
	protected String chatModel;

	/** Project base directory (read-only), provided by Maven. */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/** Maven project model (read-only), provided by Maven. */
	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	/** Maven session (read-only), provided by Maven. */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Override
	public void execute() throws MojoExecutionException {

		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel);

		FileProcessor documents = new FileProcessor(provider) {
			/**
			 * Provides the Maven-based project layout for document scanning.
			 *
			 * @param projectDir the directory where the Maven project is located
			 * @return layout of Maven project including model
			 * @throws FileNotFoundException if the project directory is missing
			 */
			@Override
			protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				MavenProjectLayout projectLayout = new MavenProjectLayout();
				projectLayout.projectDir(basedir);
				Model model = project.getModel();
				projectLayout.model(model);
				return projectLayout;
			}

			/**
			 * Optional hook for processing project modules. Currently no-op.
			 *
			 * @param projectDir the project base directory
			 * @param module     name of the module to process
			 * @throws IOException if there is an error accessing files
			 */
			@Override
			protected void processModule(File projectDir, String module) throws IOException {
				// No-op for this implementation
			}
		};
		logger.info("Scanning documents in the root directory: {}", basedir);
		try {
			File rootDir = new File(session.getExecutionRootDirectory());
			documents.scanDocuments(rootDir, basedir);
		} catch (IOException e) {
			throw new MojoExecutionException("Document assistance process failed.", e);
		}
		logger.info("Scanning finished.");
	}
}
