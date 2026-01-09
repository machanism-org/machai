package org.machanism.machai.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mojo for cleaning up the Bindex plugin's temporary files.
 * <p>
 * Removes the log file containing Bindex inputs generated during the build process.
 * This helps keep the workspace clean, especially before new builds.
 *
 * <strong>Example Usage:</strong>
 * <pre>
 * {@code
 * mvn bindex:clean
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 */
@Mojo(name = "clean", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.CLEAN)
public class Clean extends AbstractMojo {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(Clean.class);

    /**
     * Name of the temporary directory used by Machai/Bindex.
     */
    public static final String MACHAI_TEMP_DIR = ".machai";

    /**
     * Project base directory. Set by Maven.
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;

    /**
     * Deletes the Bindex inputs log file inside the temporary directory.
     *
     * @throws MojoExecutionException if an exception occurs while cleaning up files
     */
    @Override
    public void execute() throws MojoExecutionException {
        File file = new File(basedir, MACHAI_TEMP_DIR + "/bindex-inputs.txt");
        logger.info("Removing '{}' inputs log file.", file);
        boolean delete = file.delete();
        if (delete) {
            logger.info("Cleanup process finished.");
        }
    }
}
