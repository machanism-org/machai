package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "update", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Update extends AbstractBindexMojo {
	
	@Override
	public void execute() throws MojoExecutionException {
		if (isBindexed()) {
			createBindex(inputsOnly);
		}
	}

}
