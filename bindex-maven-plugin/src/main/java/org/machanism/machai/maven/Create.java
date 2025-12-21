package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "create", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Create extends AbstractBindexMojo {

	public void execute() throws MojoExecutionException {
		if (isBindexed()) {
			createBindex(false);
		}
	}
}
