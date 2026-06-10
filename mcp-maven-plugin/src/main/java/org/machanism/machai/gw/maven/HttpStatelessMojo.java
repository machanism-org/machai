package org.machanism.machai.gw.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.mcp.HttpStatelessMcpServer;

@Mojo(name = "stateless", aggregator = true)
public class HttpStatelessMojo extends AbstractMojo {

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

	@Override
	public void execute() throws MojoExecutionException {
		HttpStatelessMcpServer mcpServer = new HttpStatelessMcpServer(project.getName(), project.getVersion());

		mcpServer.setProjectDir(basedir);
		mcpServer.tools();

		mcpServer.setPort(port);
		try {
			mcpServer.start();
		} catch (Exception e) {
			throw new MojoExecutionException("HttpStateless MCP server failed.", e);
		}
	}

}
