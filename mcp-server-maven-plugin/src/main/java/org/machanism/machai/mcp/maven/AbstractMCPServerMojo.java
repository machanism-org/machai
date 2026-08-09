package org.machanism.machai.mcp.maven;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.mcp.server.McpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for MCP server Maven plugin Mojos.
 * <p>
 * Provides common configuration and utility methods for managing environment
 * variables and credentials required by the MCP server.
 * </p>
 */
public abstract class AbstractMCPServerMojo extends AbstractMojo {

	static final Logger logger = LoggerFactory.getLogger(AbstractMCPServerMojo.class);

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

	/**
	 * The port on which the MCP server will run.
	 */
	@Parameter(property = "mcp.port", required = true)
	protected int port;

	/**
	 * Map of environment variables for MCP server.
	 */
	@Parameter
	protected Map<String, String> params;

	@Parameter(property = "mcp.config", required = false)
	private File configFile;

	/**
	 * Constructs a new {@code AbstractMCPServerMojo}.
	 */
	public AbstractMCPServerMojo() {
		super();
	}

	/**
	 * Applies environment parameters and credentials to the system properties.
	 * <p>
	 * Sets each parameter in {@link #params} as a system property if not already
	 * set. If {@link #serverId} is specified, resolves credentials from Maven
	 * {@code settings.xml} and sets them as system properties. Also applies any
	 * custom configuration properties found in the server configuration.
	 * </p>
	 *
	 * @throws MojoExecutionException if the specified server ID is not found in
	 *                                Maven settings
	 */
	public void applyParameters() throws MojoExecutionException {
		params.forEach((k, v) -> {
			String property = System.getProperty(k);
			if (property == null) {
				System.setProperty(k, v);
			}
		});
	}

	public PropertiesConfigurator getConfigurator() throws MojoExecutionException {
		try {
			PropertiesConfigurator configurator = McpServer.getConfigurator(configFile.getAbsolutePath());
			return configurator;

		} catch (Exception e) {
			throw new MojoExecutionException("Failed to load configuration from: " + configFile, e);
		}
	}

}