package org.machanism.machai.mcp.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.machanism.machai.mcp.HttpStatelessMcpServer;

@Mojo(name = "stateless", aggregator = true)
public class HttpStatelessServerMojo extends AbstractMCPServerMojo {

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
