package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Maven Mojo to execute the "update" goal for Bindex.
 * <p>
 * Updates the Bindex index and resources for the Maven project, if applicable.
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>
 * {@code
 * mvn org.machanism.machai:bindex-maven-plugin:update
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 */
@Mojo(name = "update", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Update extends AbstractBindexMojo {
    /**
     * Executes the update goal, which updates Bindex index and resources.
     *
     * @throws MojoExecutionException if an error occurs during Bindex update
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (isBindexed()) {
            createBindex(true);
        }
    }
}
