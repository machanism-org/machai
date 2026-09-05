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

	@Parameter(property = "mcp.config", required = false, defaultValue = "mcp.properties")
	private File configFile;

	/**
	 * Constructs a new {@code AbstractMCPServerMojo}.
	 */
	protected AbstractMCPServerMojo() {
		super();
	}

	/**
	 * Applies environment parameters and credentials to the system properties.
	 * <p>
	 * Sets each parameter in {@link #params} as a system property if not already
	 * set.
	 * </p>
	 *
	 * @throws MojoExecutionException if applying the parameters fails
	 */
	public void applyParameters() throws MojoExecutionException {
		if (params != null) {
			params.forEach((k, v) -> {
				if (v != null) {
					String property = System.getProperty(k);
					if (property == null) {
						System.setProperty(k, v);
					}
				}
			});
		}
	}

	public PropertiesConfigurator getConfigurator() throws MojoExecutionException {
		try {
			return loadConfigurator(configFile.getAbsolutePath());

		} catch (ConfigurationLoadingException | RuntimeException exception) {
			throw new MojoExecutionException("Failed to load configuration from: " + configFile, exception);
		}
	}

	/**
	 * Loads the configurator used by this Mojo. Kept as a separate operation so
	 * transport Mojos can be tested without starting the static server bootstrap.
	 *
	 * @param configurationPath absolute path of the configuration file
	 * @return the loaded configurator
	 */
	protected PropertiesConfigurator loadConfigurator(String configurationPath) throws ConfigurationLoadingException {
		try {
			return McpServer.getConfigurator(configurationPath);
		} catch (Exception exception) {
			// Sonar java:S112: expose a domain-specific failure rather than a generic
			// exception.
			throw new ConfigurationLoadingException("Unable to load MCP server configuration", exception);
		}
	}

	static final class ConfigurationLoadingException extends Exception {
		private static final long serialVersionUID = 1L;

		ConfigurationLoadingException(String message, Exception cause) {
			super(message, cause);
		}
	}

}
