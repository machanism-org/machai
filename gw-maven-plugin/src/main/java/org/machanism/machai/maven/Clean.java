package org.machanism.machai.maven;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven Mojo to clean up temporary AI-generated documentation inputs.
 * <p>
 * This plugin step removes the `.machai/docs-inputs` log file from the Maven project directory,
 * allowing a fresh documentation generation workflow in subsequent runs.
 * </p>
 *
 * <pre>
 * Example Usage in POM:
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.machanism.machai&lt;/groupId&gt;
 *   &lt;artifactId&gt;gw-maven-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;${project.version}&lt;/version&gt;
 *   &lt;executions&gt;
 *     &lt;execution&gt;
 *       &lt;goals&gt;
 *         &lt;goal&gt;clean&lt;/goal&gt;
 *       &lt;/goals&gt;
 *     &lt;/execution&gt;
 *   &lt;/executions&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@Mojo(name = "clean", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.CLEAN)
public class Clean extends AbstractMojo {

    /** Logger for this class. */
    private static Logger logger = LoggerFactory.getLogger(Clean.class);

    /** Name of the directory holding temporary documentation inputs. */
    public static final String MACHAI_TEMP_DIR = ".machai";

    /**
     * The Maven project base directory.
     *
     * @parameter defaultValue = "${basedir}", required = true, readonly = true
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;

    /**
     * Deletes the temporary documentation input log file from the project directory.
     *
     * @throws MojoExecutionException if an error occurs when deleting the file
     */
    @Override
    public void execute() throws MojoExecutionException {
        File file = new File(basedir, MACHAI_TEMP_DIR + "/docs-inputs");
        logger.info("Removing '{}' inputs log file.", file);
        boolean delete = FileUtils.deleteQuietly(file);
        if (delete) {
            logger.info("Cleanup process finished.");
        }
    }
}
