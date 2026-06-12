package org.machanism.machai.mcp.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.machanism.machai.mcp.server.HttpStreamableMcpServer;

@Mojo(name = "streamable", aggregator = true)
public class HttpStreamableMcpServerMojo extends AbstractMCPServerMojo {

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
