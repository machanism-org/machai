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
import org.machanism.machai.ghostwriter.DocsProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mojo(name = "docs", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.INSTALL)
public class Docs extends AbstractMojo {
	private static Logger logger = LoggerFactory.getLogger(Docs.class);

	@Parameter(property = "assist.inputs.only", defaultValue = "false")
	protected boolean inputsOnly;

	@Parameter(property = "assist.chatModel", defaultValue = "gpt-5")
	protected String chatModel;

	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	@Parameter(readonly = true, defaultValue = "${project}")
	protected MavenProject project;

	public void execute() throws MojoExecutionException {
		DocsProcessor documents = new DocsProcessor() {
			@Override
			protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				MavenProjectLayout projectLayout = new MavenProjectLayout();
				projectLayout.projectDir(basedir);
				Model model = project.getModel();
				projectLayout.model(model);
				return projectLayout;
			}

			@Override
			protected void processModule(File projectDir, String module) throws IOException {
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
