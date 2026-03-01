package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

@Mojo(name = "act", aggregator = true)
public class Act extends AbstractGWGoal {

	/**
	 * Interactive prompt provider used to collect the action input.
	 */
	@Component
	protected Prompter prompter;

	@Parameter(property = "gw.act", required = false)
	private String act;

	/**
	 * Executes the interactive action.
	 *
	 * @throws MojoExecutionException if an I/O failure occurs while processing
	 *                                files
	 */
	@Override
	public void execute() throws MojoExecutionException {
		try {
			ActProcessor fileProcessor = new ActProcessor(basedir, getConfiguration(), genai) {
				@Override
				public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
					ProjectLayout projectLayout = super.getProjectLayout(projectDir);
					projectLayout.projectDir(projectDir);

					if (projectLayout instanceof MavenProjectLayout) {
						MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;
						mavenProjectLayout.effectivePomRequired(false);

						Model model = mavenProjectLayout.getModel();
						for (MavenProject mavenProject : session.getAllProjects()) {
							if (Strings.CS.equals(mavenProject.getArtifactId(), model.getArtifactId())) {
								mavenProjectLayout.model(mavenProject.getModel());
								break;
							}
						}
					}

					return projectLayout;
				}
			};

			fileProcessor.setExcludes(excludes);
			fileProcessor.setLogInputs(logInputs);

			try {
				if (act == null) {
					String action;
					while (StringUtils.isNoneBlank(action = prompter.prompt("Act"))) {
						fileProcessor.setDefaultGuidance(action);
						fileProcessor.scanDocuments(basedir, scanDir);
					}
				} else {
					fileProcessor.setDefaultGuidance(act);
					fileProcessor.scanDocuments(basedir, scanDir);
				}

			} catch (PrompterException e) {
				getLog().error("Error: " + e.getMessage());
			}

		} catch (IOException e) {
			getLog().error("I/O error occurred during file processing: " + e.getMessage());
			throw new MojoExecutionException("I/O error occurred during file processing", e);

		} finally {
			GenAIProviderManager.logUsage();
		}
	}

}
