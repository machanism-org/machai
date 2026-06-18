package org.machanism.machai.mcp.maven;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.machanism.machai.ai.provider.AbstractAIProvider;


/**
 * Abstract base class for MCP server Maven plugin Mojos.
 * <p>
 * Provides common configuration and utility methods for managing
 * environment variables and credentials required by the MCP server.
 * </p>
 */
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

    /**
     * Maven {@code server} id used to resolve GenAI credentials.
     */
    @Parameter(property = AbstractAIProvider.SERVERID_PROP_NAME, required = false)
    private String serverId;

    /**
     * Maven settings used to resolve credentials from {@code settings.xml}.
     */
    @Parameter(readonly = true, defaultValue = "${settings}")
    private Settings settings;

    /**
     * Constructs a new {@code AbstractMCPServerMojo}.
     */
    public AbstractMCPServerMojo() {
        super();
    }

    /**
     * Applies environment parameters and credentials to the system properties.
     * <p>
     * Sets each parameter in {@link #params} as a system property if not already set.
     * If {@link #serverId} is specified, resolves credentials from Maven {@code settings.xml}
     * and sets them as system properties. Also applies any custom configuration properties
     * found in the server configuration.
     * </p>
     *
     * @throws MojoExecutionException if the specified server ID is not found in Maven settings
     */
    public void applyParameters() throws MojoExecutionException {
        params.forEach((k, v) -> {
            String property = System.getProperty(k);
            if (property == null) {
                System.setProperty(k, v);
            }
        });

        if (serverId != null) {
            Server server = settings.getServer(serverId);
            if (server == null) {
                throw new MojoExecutionException("No <server> with id '" + serverId + "' found in Maven settings.xml.");
            }

            String username = server.getUsername();
            if (StringUtils.isNotBlank(username)) {
                System.setProperty(AbstractAIProvider.USERNAME_PROP_NAME, username);
            }
            String password = server.getPassword();
            if (StringUtils.isNotBlank(password)) {
                System.setProperty(AbstractAIProvider.PASSWORD_PROP_NAME, password);
            }

            if (server.getConfiguration() instanceof Xpp3Dom) {
                Xpp3Dom configuration = (Xpp3Dom) server.getConfiguration();
                Xpp3Dom[] children = configuration.getChildren();
                for (Xpp3Dom xpp3Dom : children) {
                    System.setProperty(xpp3Dom.getName(), xpp3Dom.getValue());
                }
            }
        }
    }
}