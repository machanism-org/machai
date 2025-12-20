package org.machanism.machai.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractBindexMojo extends AbstractMojo {

	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;
	@Parameter(defaultValue = "false")
	protected boolean debug;
	@Parameter(defaultValue = "GPT_5_1")
	protected String model;
	@Parameter(defaultValue = "true")
	protected boolean update;

	public AbstractBindexMojo() {
		super();
	}

}