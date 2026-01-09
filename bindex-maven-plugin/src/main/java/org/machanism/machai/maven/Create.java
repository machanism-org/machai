package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Maven Mojo to execute the "create" goal for Bindex.
 * <p>
 * This goal creates a new Bindex index and associated resources for the Maven project when applicable.
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>
 * {@code
 * mvn org.machanism.machai:bindex-maven-plugin:create
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 */
@Mojo(name = "create", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Create extends AbstractBindexMojo {

    /**
     * Executes the create goal, which generates a new Bindex index if the project is suitable.
     *
     * @throws MojoExecutionException if an error occurs during Bindex creation
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (isBindexed()) {
            createBindex(false);
        }
    }
}
