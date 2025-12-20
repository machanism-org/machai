package org.machanism.machai.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractBindexMojo extends AbstractMojo {

	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;
	
	@Parameter(property = "machai.debug", defaultValue = "false")
	protected boolean debug;
	
	@Parameter(property = "machai.model", defaultValue = "GPT_5")
	protected String model;

	public AbstractBindexMojo() {
		super();
	}

}