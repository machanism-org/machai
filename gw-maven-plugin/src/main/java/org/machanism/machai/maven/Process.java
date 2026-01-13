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
 * Mojo to process project files using GenAI provider.
 * 
 * <p>
 * This Maven plugin scans documents in the project, facilitated by AI
 * assistance for documentation generation and processing. It allows
 * configuration of the chat model provider and manages document scanning
 * lifecycle.
 * </p>
 *
 * <h2>Parameters</h2>
 * <ul>
 * <li><b>gw.genai</b> - The chat model to use for AI assistance in
 * documentation generation (e.g., "OpenAI:gpt-5"). Optional, defaults to
 * "None".</li>
 * <li><b>basedir</b> - Project base directory. Set by Maven. Required and
 * readonly.</li>
 * <li><b>project</b> - The MavenProject instance. Set by Maven. Readonly.</li>
 * <li><b>session</b> - The Maven Session. Required and readonly.</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * mvn org.machanism.machai:machai-maven-plugin:process -Dgw.genai=OpenAI:gpt-5
 * }</pre>
 * 
 */
@Mojo(name = "process", threadSafe = false)
public class Process extends AbstractMojo {

	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(Process.class);

	/**
	 * The chat model to use for AI assistance in documentation generation. <br>
	 * Example: "OpenAI:gpt-5"<br>
	 * Default: "None"
	 */
	@Parameter(property = "gw.genai")
	protected String chatModel;

	/**
	 * Project base directory. Set by Maven.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/**
	 * The MavenProject instance provided by Maven.
	 */
	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	/**
	 * The Maven Session object.
	 */
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
