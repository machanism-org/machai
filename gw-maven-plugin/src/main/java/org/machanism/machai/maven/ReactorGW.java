package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.FileProcessor;
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
 *
 * <p>
 * This goal supports Maven reactor module processing: all submodules are
 * processed according to their dependencies, following standard Maven reactor
 * logic.
 * </p>
 *
 * <h2>Parameters</h2>
 * <ul>
 * <li><b>{@code gw.rootProjectLast}</b> / {@code &lt;rootProjectLast&gt;}
 * ({@link #rootProjectLast}): If {@code true}, delays processing of the
 * execution-root project until all other reactor projects have completed.
 * <p>
 * Default: {@code false}
 * </p>
 * </li>
 * </ul>
 *
 * <h3>Inherited parameters (from {@link AbstractGWGoal})</h3>
 * <ul>
 * <li><b>{@code gw.genai}</b> / {@code <genai>} ({@code genai}): Provider/model
 * identifier to pass to the workflow.</li>
 * <li><b>{@code ${basedir}}</b> ({@code basedir}): The Maven module base
 * directory.</li>
 * <li><b>{@code gw.scanDir}</b> / {@code <scanDir>} ({@code scanDir}): Optional
 * scan root override. When omitted, defaults to the execution root
 * directory.</li>
 * <li><b>{@code gw.instructions}</b> / {@code <instructions>}
 * ({@code instructions}): Instruction locations (for example, file paths or
 * classpath locations) consumed by the workflow.</li>
 * <li><b>{@code gw.guidance}</b> / {@code <guidance>} ({@code guidance}):
 * Default guidance text forwarded to the workflow.</li>
 * <li><b>{@code gw.excludes}</b> / {@code <excludes>} ({@code excludes}):
 * Exclude patterns/paths that should be skipped when scanning documentation
 * sources.</li>
 * <li><b>{@code gw.genai.serverId}</b> ({@code serverId}): {@code settings.xml}
 * {@code <server>} id used to read GenAI credentials.</li>
 * <li><b>{@code gw.logInputs}</b> ({@code logInputs}): Whether to log the list
 * of input files passed to the workflow.
 * <p>
 * Default: {@code false}
 * </p>
 * </li>
 * </ul>
 * 
 * <h2>Usage examples</h2>
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
 * <h3>Override scan root</h3>
 *
 * <pre>
 * mvn gw:reactor -Dgw.scanDir=src\\site
 * </pre>
 *
 * <h3>Use Maven settings credentials for the GenAI provider</h3>
 *
 * <pre>
 * mvn gw:reactor -Dgw.genai.serverId=my-genai-server
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
 *     &lt;!-- inherited from AbstractGWGoal --&gt;
 *     &lt;scanDir&gt;${project.basedir}&lt;/scanDir&gt;
 *     &lt;logInputs&gt;true&lt;/logInputs&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 */
@Mojo(name = "reactor", threadSafe = true)
public class ReactorGW extends AbstractGWGoal {

	/** Logger for this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ReactorGW.class);

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

		if (!rootProject || !pomProject || !rootProjectLast) {
			try {
				scanDocuments(documents);
			} catch (ProcessTerminationException e) {
				LOGGER.error("Process terminated: {} (exit code: {})", e.getMessage(), e.getExitCode());
				throw new MojoExecutionException(
						"Process terminated: " + e.getMessage() + " (exit code: " + e.getExitCode() + ")", e);
			}
			return;
		}

		Thread deferredRootScanThread = new Thread(() -> {
			try {
				while (!reactorProjects.isEmpty()) {
					Thread.sleep(500);
				}
				try {
					scanDocuments(documents);
				} catch (ProcessTerminationException e) {
					LOGGER.error("Process terminated in deferred execution-root processing: {} (exit code: {})",
							e.getMessage(), e.getExitCode());
					throw new RuntimeException(
							"Process terminated: " + e.getMessage() + " (exit code: " + e.getExitCode() + ")", e);
				}
			} catch (MojoExecutionException e) {
				LOGGER.error("Failed to scan documents in deferred execution-root processing.", e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error("Deferred execution-root processing was interrupted.", e);
			}
		}, "gw-reactor-root-last");
		deferredRootScanThread.start();
	}
}
