package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* @guidance:
Generate Javadoc that includes descriptions for all Maven plugin parameters, along with usage examples. Be sure to incorporate information from any superclasses.
ProcessModules supports Maven reactor for module processing. All submodules will be processed according to their dependencies, following standard Maven reactor logic.
*/

/**
 * Maven goal that processes documents across a multi-module (reactor) build.
 * <p>
 * This goal supports Maven reactor module processing: all submodules are
 * processed according to their dependencies, following standard Maven reactor
 * logic.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * This goal inherits additional common parameters from {@link AbstractGWGoal}.
 * Refer to that class for the complete list and their usage.
 * </p>
 * <ul>
 * <li><b>{@code gw.rootProjectLast}</b> ({@link #rootProjectLast}): If
 * {@code true}, delays processing of the execution-root project until all other
 * reactor projects have completed.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 *
 * <h3>Command line</h3>
 *
 * <pre>
 * mvn gw:reactor
 * </pre>
 *
 * <h3>Delay execution-root project processing</h3>
 *
 * <pre>
 * mvn gw:reactor -Dgw.rootProjectLast=true
 * </pre>
 *
 * <h3>Plugin configuration</h3>
 *
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.machanism&lt;/groupId&gt;
 *   &lt;artifactId&gt;gw-maven-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;...&lt;/version&gt;
 *   &lt;configuration&gt;
 *     &lt;rootProjectLast&gt;true&lt;/rootProjectLast&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * <h2>Inherited parameters</h2>
 * <p>
 * In addition to {@link #rootProjectLast}, this goal supports all parameters
 * from {@link AbstractGWGoal}. Common examples include:
 * </p>
 * <ul>
 * <li><b>{@code gw.scanDir}</b>: Base directory used to locate source
 * documents. Can be overridden from the command line via
 * {@code -Dgw.scanDir=...} or in plugin configuration using
 * {@code &lt;scanDir&gt;...&lt;/scanDir&gt;}.</li>
 * </ul>
 */
@Mojo(name = "reactor", threadSafe = true)
public class ReactorGW extends AbstractGWGoal {

	/** Logger for this class. */
	static final Logger logger = LoggerFactory.getLogger(ReactorGW.class);

	/**
	 * If {@code true}, delays processing of the execution-root project until all
	 * other reactor projects complete.
	 */
	@Parameter(property = "gw.rootProjectLast", defaultValue = "false")
	private boolean rootProjectLast;

	@Override
	public void execute() throws MojoExecutionException {
		String executionRootDirectory = session.getExecutionRootDirectory();
		boolean rootProject = executionRootDirectory.equals(basedir.getAbsolutePath());
		boolean pomProject = "pom".equals(project.getPackaging());

		PropertiesConfigurator config = getConfiguration();

		FileProcessor documents = new FileProcessor(new File(executionRootDirectory), genai, config) {

			@Override
			protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = ProjectLayoutManager.detectProjectLayout(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					projectLayout.projectDir(projectDir);
					Model model = project.getModel();
					((MavenProjectLayout) projectLayout).model(model);
				}

				return projectLayout;
			}

			@Override
			protected void processModule(File projectDir, String module) throws IOException {
				// No-op for this implementation
			}

		};
		documents.setModuleMultiThread(false);

		if (scanDir == null) {
			scanDir = basedir.getAbsolutePath();
		}

		if (!rootProject || (rootProject && !pomProject) || (rootProject && !rootProjectLast)) {
			scanDocuments(documents);
			return;
		}

		new Thread(() -> {
			try {
				while (!reactorProjects.isEmpty()) {
					Thread.sleep(500);
				}
				scanDocuments(documents);
			} catch (MojoExecutionException e) {
				getLog().error(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				getLog().error(e);
			}
		}, "gw-std-root-last").start();
	}
}
