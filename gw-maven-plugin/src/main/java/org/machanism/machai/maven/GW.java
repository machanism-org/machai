package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/*@guidance: 
 * - Generate javadoc with a description all maven plugin parameters and examples of usage included superclass information.. 
 * - **Processing Order for `gw:gw`:**
 *   - The `gw:gw` goal processes files in reverse order, similar to the Ghostwriter CLI.
 *   - Sub-modules are processed first, followed by parent modules.
 *   - For more details, see: https://www.machanism.org/guided-file-processing/index.html 
 */

/**
 * Maven goal {@code gw:gw} for processing guided documents in a Maven project.
 * <p>
 * <strong>Processing order:</strong> This goal processes files in reverse order, similar to
 * the Ghostwriter CLI. Sub-modules are processed first, followed by parent modules.
 * For details, see: https://www.machanism.org/guided-file-processing/index.html
 * </p>
 *
 * <h2>Parameters</h2>
 * <ul>
 * <li>{@code -Dgw.threads} (default {@code true}) - Enables/disables multi-threaded processing
 * of module documents.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 *
 * <h3>Run with defaults</h3>
 * <pre>
 * mvn gw:gw
 * </pre>
 *
 * <h3>Disable multi-threaded module processing</h3>
 * <pre>
 * mvn gw:gw -Dgw.threads=false
 * </pre>
 *
 * <p>
 * See {@link AbstractGWGoal} for additional shared configuration, parameters, and behavior.
 * </p>
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true)
public class GW extends AbstractGWGoal {

	/**
	 * Enables/disables multi-threaded document processing.
	 */
	@Parameter(property = "gw.threads", defaultValue = "true")
	private boolean threads;

	@Override
	public void execute() throws MojoExecutionException {
		PropertiesConfigurator config = getConfiguration();

		FileProcessor processor = new FileProcessor(genai, config) {

			@Override
			protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = super.getProjectLayout(projectDir);
				projectLayout.projectDir(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;
					mavenProjectLayout.effectivePomRequired(false);

					Model model = mavenProjectLayout.getModel();
					for (MavenProject mavenProject : session.getAllProjects()) {
						if (StringUtils.equals(mavenProject.getArtifactId(), model.getArtifactId())) {
							mavenProjectLayout.model(mavenProject.getModel());
							break;
						}
					}
				}

				return projectLayout;
			}
		};

		List<MavenProject> modules = session.getAllProjects();
		boolean nonRecursive = project.getModules().size() > 1 && modules.size() == 1;
		processor.setNonRecursive(nonRecursive);

		processor.setModuleMultiThread(threads);
		scanDocuments(processor);
	}

}
