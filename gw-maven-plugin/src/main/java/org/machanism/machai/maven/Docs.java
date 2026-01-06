package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.gw.DocsProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven plugin Mojo to handle document processing in a Maven-based project.
 * <p>
 * This Mojo scans the root directory of the project, processes documentation
 * using an external AI provider and project layout structure, and facilitates
 * automated document assistance. Document scanning can be adjusted to process
 * only inputs or utilize a specific chat model as needed.
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
 *         &lt;goal&gt;docs&lt;/goal&gt;
 *       &lt;/goals&gt;
 *     &lt;/execution&gt;
 *   &lt;/executions&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@Mojo(name = "docs", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Docs extends AbstractMojo {

    /** Logger for this class. */
    private static Logger logger = LoggerFactory.getLogger(Docs.class);

    /**
     * If true, only input documents will be processed, not outputs.
     */
    @Parameter(property = "docs.inputs.only", defaultValue = "false")
    protected boolean inputsOnly;

    /**
     * The chat model to use for AI assistance in documentation generation.
     */
    @Parameter(property = "docs.chatModel", defaultValue = "OpenAI:gpt-5")
    protected String chatModel;

    /**
     * Project base directory. Set by Maven.
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;

    /**
     * The MavenProject instance provided by Maven.
     */
    @Parameter(readonly = true, defaultValue = "${project}")
    protected MavenProject project;

    /**
     * Executes the document processing workflow; scans documents, processes modules,
     * and delegates document handling to DocsProcessor.
     *
     * @throws MojoExecutionException if a document assistance process fails
     */
    @Override
    public void execute() throws MojoExecutionException {
        GenAIProvider provider = GenAIProviderManager.getProvider(inputsOnly ? null : chatModel);
        DocsProcessor documents = new DocsProcessor(provider) {
            /**
             * Provides the Maven-based project layout for document scanning.
             *
             * @param projectDir the directory where the Maven project is located
             * @return layout of Maven project including model
             * @throws FileNotFoundException if the project directory is missing
             */
            @Override
            protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
                MavenProjectLayout projectLayout = new MavenProjectLayout();
                projectLayout.projectDir(basedir);
                Model model = project.getModel();
                projectLayout.model(model);
                return projectLayout;
            }

            /**
             * Optional hook for processing project modules. Currently no-op.
             *
             * @param projectDir the project base directory
             * @param module name of the module to process
             * @throws IOException if there is an error accessing files
             */
            @Override
            protected void processModule(File projectDir, String module) throws IOException {
                // No-op for this implementation
            }
        };
        logger.info("Scanning documents in the root directory: {}", basedir);
        try {
            documents.scanDocuments(basedir);
        } catch (IOException e) {
            throw new MojoExecutionException("Document assistance process failed.", e);
        }
        logger.info("Scanning finished.");
    }
}
