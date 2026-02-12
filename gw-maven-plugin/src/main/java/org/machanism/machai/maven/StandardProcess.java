package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*@guidance: Generate javadoc with a description all maven plugin parameters and examples of usage included superclass information. */

/**
 * Maven goal {@code gw:std} for processing guided documents within a single Maven module.
 *
 * <p>
 * By default, this goal processes the current module immediately. When executed from the reactor root it can
 * optionally delay processing of the execution-root project until other reactor projects have completed.
 * </p>
 *
 * <h2>Parameters</h2>
 *
 * <p>
 * This goal defines the parameter below and also inherits the shared parameters from {@link AbstractGWGoal}.
 * </p>
 *
 * <h3>Goal-specific parameters</h3>
 * <dl>
 * <dt>{@code gw.rootProjectLast} (user property: {@code -Dgw.rootProjectLast}, default: {@code false})</dt>
 * <dd>
 * If {@code true}, delays processing of the execution-root project (the project returned by
 * {@code MavenSession#getExecutionRootDirectory()}) until all other reactor projects have completed.
 * </dd>
 * </dl>
 *
 * <h3>Shared parameters (inherited from {@link AbstractGWGoal})</h3>
 * <p>
 * This goal inherits additional parameters from {@link AbstractGWGoal}. See that class for the complete list,
 * including defaults and supported formats.
 * </p>
 *
 * <h2>Usage</h2>
 *
 * <h3>Run for the current module</h3>
 * <pre>
 * mvn gw:std
 * </pre>
 *
 * <h3>Delay the execution-root project until other modules are done</h3>
 * <pre>
 * mvn gw:std -Dgw.rootProjectLast=true
 * </pre>
 *
 * <h3>Enable GenAI mode (inherited)</h3>
 * <pre>
 * mvn gw:std -Dgw.genai=true
 * </pre>
 *
 * <h3>Scan a custom directory (inherited)</h3>
 * <pre>
 * mvn gw:std -Dgw.scanDir=src\\site
 * </pre>
 *
 * <h3>Provide instructions and guidance files (inherited)</h3>
 * <pre>
 * mvn gw:std -Dgw.instructions=path\\to\\instructions.md -Dgw.guidance=path\\to\\guidance.md
 * </pre>
 */
@Mojo(name = "std", threadSafe = true)
public class StandardProcess extends AbstractGWGoal {

	/** Logger for this class. */
	static final Logger logger = LoggerFactory.getLogger(StandardProcess.class);

	/**
	 * If {@code true}, delays processing of the execution-root project until all other reactor projects complete.
	 */
	@Parameter(property = "gw.rootProjectLast", defaultValue = "false")
	private boolean rootProjectLast;

	@Override
	public void execute() throws MojoExecutionException {
		String executionRootDirectory = session.getExecutionRootDirectory();
		if (!executionRootDirectory.equals(rootDir.getAbsolutePath()) || rootProjectLast) {
			scanDocuments();
			return;
		}

		new Thread(() -> {
			try {
				while (!reactorProjects.isEmpty()) {
					Thread.sleep(500);
				}
				scanDocuments();
			} catch (MojoExecutionException e) {
				getLog().error(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				getLog().error(e);
			}
		}, "gw-std-root-last").start();
	}
}
