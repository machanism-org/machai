package org.machanism.machai.mcp.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.machanism.machai.mcp.server.HttpStreamableMcpServer;

/**
 * Maven plugin Mojo for starting a streamable HTTP MCP server.
 * <p>
 * This Mojo initializes and starts an {@link HttpStreamableMcpServer} using
 * project information and configuration parameters. It is intended to be used
 * as an aggregator goal in multi-module Maven builds.
 * </p>
 */
@Mojo(name = "streamable", aggregator = true)
public class HttpStreamableMcpServerMojo extends AbstractMCPServerMojo {

	/**
	 * Executes the Mojo, starting the streamable HTTP MCP server.
	 * <p>
	 * Applies environment parameters, initializes the server with project name and
	 * version, sets the project directory and port, and starts the server. If the
	 * server fails to start, a {@link MojoExecutionException} is thrown.
	 * </p>
	 *
	 * @throws MojoExecutionException if the server fails to start or configuration
	 *                                is invalid
	 */
	@Override
	public void execute() throws MojoExecutionException {
		applyParameters();

		HttpStreamableMcpServer mcpServer = new HttpStreamableMcpServer(project.getName(), project.getVersion());

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