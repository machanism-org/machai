package org.machanism.machai.bindex.builder;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.machanism.machai.project.layout.ProjectLayout;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

public class PythonBindexBuilder extends BindexBuilder {
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("python_project_prompts");
	private static final String PROJECT_MODEL_FILE_NAME = "pyproject.toml";

	public PythonBindexBuilder(ProjectLayout projectLayout) {
		super(projectLayout);
	}

	@Override
	public void projectContext() throws IOException {
		File pyprojectTomlFile = new File(getProjectLayout().getProjectDir(), PROJECT_MODEL_FILE_NAME);

		try (FileReader reader = new FileReader(pyprojectTomlFile)) {
			String prompt = MessageFormat.format(promptBundle.getString("project_build_section"),
					IOUtils.toString(reader));
			getGenAIProvider().prompt(prompt);
		}

		TomlParseResult result = Toml.parse(pyprojectTomlFile.toPath());
		String projectName = result.getString("project.name");
		if (projectName != null) {
			File sourceDir = new File(getProjectLayout().getProjectDir(), projectName.replace(".", "/"));

			File[] listFiles = sourceDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			});

			if (listFiles != null) {
				for (File file : listFiles) {
					getGenAIProvider().promptFile(file, "source_resource_section");
				}
			}
		}

		String prompt = promptBundle.getString("additional_rules");
		getGenAIProvider().prompt(prompt);
	}

}
