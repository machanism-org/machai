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
import org.machanism.machai.gw.processor.FileProcessor;
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
 * This goal is an aggregator and can be executed even when a {@code pom.xml} is not present in the current
 * directory.
 * </p>
 *
 * <h2>Processing order</h2>
 * <p>
 * The {@code gw:gw} goal processes files in reverse order, similar to the Ghostwriter CLI: sub-modules are processed
 * first, followed by parent modules. For more details, see:
 * <a href="https://www.machanism.org/guided-file-processing/index.html">Guided file processing</a>.
 * </p>
 *
 * <h2>Parameters</h2>
 *
 * <p>
 * This goal defines the following parameter in addition to those inherited from {@link AbstractGWGoal}.
 * </p>
 * <dl>
 * <dt>{@code -Dgw.threads}</dt>
 * <dd>
 * Enables or disables multi-threaded module processing. Default: {@code false}.
 * </dd>
 * </dl>
 *
 * <h3>Inherited parameters (from {@link AbstractGWGoal})</h3>
 * <p>
 * This goal also supports all shared parameters defined by {@link AbstractGWGoal}.
 * </p>
 *
 * <h4>Common parameters</h4>
 * <dl>
 * <dt>{@code -Dgw.path}</dt>
 * <dd>
 * Optional path to the base directory to scan. Default: the current Maven basedir.
 * </dd>
 *
 * <dt>{@code -Dgw.genai}</dt>
 * <dd>
 * Path to the GenAI configuration/properties file used by Ghostwriter. Default: implementation-defined by
 * {@link AbstractGWGoal}.
 * </dd>
 *
 * <dt>{@code -Dgw.profile}</dt>
 * <dd>
 * Optional processing profile name. Default: implementation-defined by {@link AbstractGWGoal}.
 * </dd>
 *
 * <dt>{@code -Dgw.dryRun}</dt>
 * <dd>
 * If {@code true}, performs a dry run (no files are modified). Default: {@code false}.
 * </dd>
 * </dl>
 *
 * <p>
 * Refer to {@link AbstractGWGoal} for the authoritative list and exact semantics of inherited parameters.
 * </p>
 *
 * <h2>Usage examples</h2>
 *
 * <p>Run in the current directory:</p>
 *
 * <pre>
 * mvn gw:gw
 * </pre>
 *
 * <p>Run without a {@code pom.xml} (this goal sets {@code requiresProject=false}):</p>
 *
 * <pre>
 * cd path\\to\\project
 * mvn gw:gw
 * </pre>
 *
 * <p>Enable multi-threaded processing:</p>
 *
 * <pre>
 * mvn gw:gw -Dgw.threads=true
 * </pre>
 *
 * <p>Disable multi-threaded processing (default):</p>
 *
 * <pre>
 * mvn gw:gw -Dgw.threads=false
 * </pre>
 *
 * <p>Run against a specific module:</p>
 *
 * <pre>
 * mvn -pl :my-module gw:gw
 * </pre>
 *
 * @see AbstractGWGoal
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true, requiresProject = false)
public class GW extends AbstractGWGoal {

	/**
	 * Enables or disables multi-threaded module processing.
	 *
	 * <p>
	 * When enabled, modules may be processed concurrently.
	 * </p>
	 */
	@Parameter(property = "gw.threads", defaultValue = "false")
	private boolean threads;

	@Override
	public void execute() throws MojoExecutionException {
		PropertiesConfigurator config = getConfiguration();

		FileProcessor processor = new FileProcessor(basedir, genai, config) {

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
