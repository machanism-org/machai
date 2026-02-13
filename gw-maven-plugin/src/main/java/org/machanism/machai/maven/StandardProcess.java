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

/*
 * @guidance: Generate javadoc with a description all maven plugin parameters and examples of usage included superclass information. 
 * 
 * Example of usage without project pom.xml file:
 * 
 * ```batch
 * @echo off
 * 
 * :: Set the project home directory variable
 * SET PROJECT_HOME=...
 * 
 * SET PARAMS=^
 *  -Dgw.genai=CodeMie:gpt-5-2-2025-12-11^
 *  -Dgw.scanDir=glob:.^
 *  -Dgw.guidance=file:%PROJECT_HOME%\guidance.txt^
 *  -Dgw.instructions=file:%PROJECT_HOME%\instructions.txt^
 *  -Dgw.logInputs=true^
 * 
 * call mvn %PARAMS%^
 *  -DGENAI_USERNAME=...^
 *  -DGENAI_PASSWORD=...^
 *  org.machanism.machai:gw-maven-plugin:0.0.9:std
 * ```
 */

/**
 * Maven goal {@code gw:std} for processing guided documents.
 *
 * <p>
 * This goal runs against a single Maven module (the current project). When
 * executed from the reactor root, it can optionally defer processing of the
 * execution-root project until other reactor projects have finished.
 * </p>
 *
 * <h2>Parameters</h2>
 *
 * <p>
 * This goal defines the goal-specific parameter below and also inherits shared
 * parameters from {@link AbstractGWGoal}.
 * </p>
 *
 * <h3>Goal-specific parameters</h3>
 * <dl>
 * <dt>{@code gw.rootProjectLast} (user property: {@code -Dgw.rootProjectLast}, default:
 * {@code false})</dt>
 * <dd>
 * If {@code true}, delays processing of the execution-root project (the project
 * returned by {@code MavenSession#getExecutionRootDirectory()}) until all other
 * reactor projects have completed.
 * </dd>
 * </dl>
 *
 * <h3>Shared parameters (inherited from {@link AbstractGWGoal})</h3>
 * <p>
 * This goal inherits additional parameters from {@link AbstractGWGoal}. Refer to
 * that class for the complete list, including defaults, supported formats, and
 * examples.
 * </p>
 *
 * <h2>Usage examples</h2>
 *
 * <h3>Run for the current module</h3>
 *
 * <pre>
 * mvn gw:std
 * </pre>
 *
 * <h3>Delay the execution-root project until other modules are done</h3>
 *
 * <pre>
 * mvn gw:std -Dgw.rootProjectLast=true
 * </pre>
 *
 * <h3>Enable GenAI mode (inherited)</h3>
 *
 * <pre>
 * mvn gw:std -Dgw.genai=true
 * </pre>
 *
 * <h3>Scan a custom directory (inherited)</h3>
 *
 * <pre>
 * mvn gw:std -Dgw.scanDir=src\\site
 * </pre>
 *
 * <h3>Provide instructions and guidance files (inherited)</h3>
 *
 * <pre>
 * mvn gw:std -Dgw.instructions=path\\to\\instructions.md -Dgw.guidance=path\\to\\guidance.md
 * </pre>
 */
@Mojo(name = "std", threadSafe = true, requiresProject = false)
public class StandardProcess extends AbstractGWGoal {

	/** Logger for this class. */
	static final Logger logger = LoggerFactory.getLogger(StandardProcess.class);

	/**
	 * If {@code true}, delays processing of the execution-root project until all
	 * other reactor projects complete.
	 */
	@Parameter(property = "gw.rootProjectLast", defaultValue = "false")
	private boolean rootProjectLast;

	@Override
	public void execute() throws MojoExecutionException {
		String executionRootDirectory = session.getExecutionRootDirectory();
		boolean rootProject = executionRootDirectory.equals(rootDir.getAbsolutePath());
		boolean pomProject = "pom".equals(project.getPackaging());

		PropertiesConfigurator config = getConfiguration();

		FileProcessor documents = new FileProcessor(genai, config) {
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
