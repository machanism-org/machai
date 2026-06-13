package org.machanism.machai.mcp.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.machanism.machai.mcp.server.HttpStatelessMcpServer;

/**
 * Maven plugin Mojo for starting a stateless HTTP MCP server.
 * <p>
 * This Mojo initializes and starts an {@link HttpStatelessMcpServer} using
 * project information and configuration parameters. It is intended to be used
 * as an aggregator goal in multi-module Maven builds.
 * </p>
 */
@Mojo(name = "stateless", aggregator = true)
public class HttpStatelessServerMojo extends AbstractMCPServerMojo {

    /**
     * Executes the Mojo, starting the stateless HTTP MCP server.
     * <p>
     * Applies environment parameters, initializes the server with project name and version,
     * sets the project directory and port, and starts the server. If the server fails to start,
     * a {@link MojoExecutionException} is thrown.
     * </p>
     *
     * @throws MojoExecutionException if the server fails to start or configuration is invalid
     */
    @Override
    public void execute() throws MojoExecutionException {
        applyParameters();

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