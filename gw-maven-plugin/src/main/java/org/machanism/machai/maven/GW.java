package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven goal ({@code gw}) that runs the MachAI Generative Workflow (GW)
 * documentation pipeline.
 *
 * <p>
 * The goal scans documentation sources (typically under {@code src/site})
 * starting at the current module base directory ({@code ${basedir}}) and
 * delegates processing to {@link FileProcessor}.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * Parameters can be configured via {@code pom.xml} (plugin
 * {@code &lt;configuration&gt;}) and/or via system properties.
 * </p>
 * <ul>
 * <li><b>{@code genai}</b> / <b>{@code gw.genai}</b> (optional): Provider/model
 * identifier forwarded to the workflow (for example {@code OpenAI:gpt-5}).</li>
 * <li><b>{@code scanDir}</b> (optional): Explicit scan root; when not set,
 * defaults to the module base directory.</li>
 * <li><b>{@code instructions}</b> / <b>{@code gw.instructions}</b> (optional):
 * One or more instruction location strings consumed by the workflow.</li>
 * <li><b>{@code guidance}</b> / <b>{@code gw.guidance}</b> (optional): Default
 * guidance text forwarded to the workflow.</li>
 * <li><b>{@code excludes}</b> / <b>{@code gw.excludes}</b> (optional): One or
 * more exclude patterns or paths to skip during documentation scanning.</li>
 * <li><b>{@code serverId}</b> / <b>{@code gw.genai.serverId}</b> (optional):
 * Maven {@code settings.xml} {@code &lt;server&gt;} id used to load GenAI
 * credentials.</li>
 * <li><b>{@code threads}</b> / <b>{@code gw.threads}</b> (optional, default
 * {@code true}): Enables or disables multi-threaded document processing.</li>
 * <li><b>{@code logInputs}</b> / <b>{@code gw.logInputs}</b> (optional, default
 * {@code false}): Logs the list of input files provided to the workflow.</li>
 * </ul>
 *
 * <h2>Credentials</h2>
 * <p>
 * GenAI credentials are loaded from Maven {@code settings.xml} using the
 * {@code &lt;server&gt;} entry whose id is provided by
 * {@code gw.genai.serverId}. When present, credentials are exposed to the
 * workflow as configuration properties:
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
public class GW extends StandardProcess {

	private static final Logger logger = LoggerFactory.getLogger(GW.class);

	/**
	 * Enables/disables multi-threaded document processing.
	 */
	@Parameter(property = "gw.threads", defaultValue = "true", required = false)
	private boolean threads;

	/**
	 * Configures credentials and runs document scanning/processing.
	 *
	 * @throws MojoExecutionException if Maven settings are unavailable, credentials
	 *                                cannot be resolved, or document processing
	 *                                fails
	 */
	@Override
	public void execute() throws MojoExecutionException {
		PropertiesConfigurator config = getConfiguration();

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

		List<MavenProject> modules = session.getAllProjects();
		boolean nonRecursive = project.getModules().size() > 1 && modules.size() == 1;
		processor.setNonRecursive(nonRecursive);

		processor.setModuleMultiThread(threads);
		scanDocuments(processor);
	}

}
