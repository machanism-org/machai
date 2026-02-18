package org.machanism.machai.gw.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.machanism.machai.gw.processor.FileProcessor;

/**
 * Maven goal that deletes temporary artifacts created by the MachAI generative-workflow (GW) document processing.
 *
 * <p>
 * This goal is typically bound to Maven's {@code clean} phase.
 * </p>
 */
@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class Clean extends AbstractMojo {

	/**
	 * The Maven module base directory.
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	/**
	 * Deletes workflow temporary files under the current module base directory.
	 *
	 * @throws MojoExecutionException if an error occurs while deleting temporary files
	 */
	@Override
	public void execute() throws MojoExecutionException {
		try {
			FileProcessor.deleteTempFiles(basedir);
		} catch (RuntimeException e) {
			throw new MojoExecutionException("Failed to delete workflow temporary files.", e);
		}
	}

}
