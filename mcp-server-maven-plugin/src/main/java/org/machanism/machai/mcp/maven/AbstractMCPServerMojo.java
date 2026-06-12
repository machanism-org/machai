package org.machanism.machai.mcp.maven;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractMCPServerMojo extends AbstractMojo {

	/**
	 * The Maven module base directory.
	 */
	@Parameter(defaultValue = "${basedir}", required = true)
	protected File basedir;

	/**
	 * The current Maven project.
	 */
	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	@Parameter(property = "mcp.port", required = true)
	protected int port;

	/**
	 * Map of environment variables for MCP server.
	 */
	@Parameter
	protected Map<String, String> params;

	public AbstractMCPServerMojo() {
		super();
	}

	public void applyParameters() {
		params.forEach((k, v) -> {
			String property = System.getProperty(k);
			if (property == null) {
				System.setProperty(k, v);
			}
		});
	}
}