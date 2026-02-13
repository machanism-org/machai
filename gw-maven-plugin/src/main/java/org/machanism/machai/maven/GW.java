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
 *   - It can be run without pom.xml.
 */

/**
 * Maven goal {@code gw:gw} that runs Ghostwriter guided file processing for a project.
 *
 * <p>
 * This goal is an aggregator and can be executed even when a {@code pom.xml} is not present in the
 * current directory.
 * </p>
 *
 * <h2>Processing order</h2>
 * <p>
 * The {@code gw:gw} goal processes files in reverse order, similar to the Ghostwriter CLI.
 * Sub-modules are processed first, followed by parent modules.
 * For more details, see:
 * <a href="https://www.machanism.org/guided-file-processing/index.html">Guided file processing</a>.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * This goal defines the following parameters in addition to those inherited from
 * {@link AbstractGWGoal}:
 * </p>
 * <ul>
 * <li>
 * {@code -Dgw.threads} ({@link #threads}): Enables or disables multi-threaded module processing.
 * Default: {@code false}.
 * </li>
 * </ul>
 *
 * <p>
 * See {@link AbstractGWGoal} for additional shared parameters, such as configuring the GenAI
 * provider and selecting documents to scan.
 * </p>
 *
 * <h2>Usage examples</h2>
 * <pre>
 * mvn gw:gw
 * </pre>
 *
 * <pre>
 * mvn gw:gw -Dgw.threads=false
 * </pre>
 *
 * <pre>
 * mvn -pl :my-module gw:gw
 * </pre>
 *
 * <pre>
 * mvn gw:gw -Dgw.threads=true
 * </pre>
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true, requiresProject = false)
public class GW extends AbstractGWGoal {

	/**
	 * Enables or disables multi-threaded module processing.
	 *
	 * <p>
	 * When enabled, module processing may occur concurrently.
	 * </p>
	 */
	@Parameter(property = "gw.threads", defaultValue = "false")
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
