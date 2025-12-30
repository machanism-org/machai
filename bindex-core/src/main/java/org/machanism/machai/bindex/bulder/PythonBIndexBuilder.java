package org.machanism.machai.bindex.bulder;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

public class PythonBIndexBuilder extends BIndexBuilder {

	private static ResourceBundle promptBundle = ResourceBundle.getBundle("python_project_prompts");
	private static final String PROJECT_MODEL_FILE_NAME = "pyproject.toml";

	@Override
	public void projectContext() throws IOException {
		File pyprojectTomlFile = new File(getProjectDir(), PROJECT_MODEL_FILE_NAME);

		try (FileReader reader = new FileReader(pyprojectTomlFile)) {
			String prompt = MessageFormat.format(promptBundle.getString("project_build_section"),
					IOUtils.toString(reader));
			getProvider().prompt(prompt);
		}

		TomlParseResult result = Toml.parse(pyprojectTomlFile.toPath());
		String projectName = result.getString("project.name");
		if (projectName != null) {
			File sourceDir = new File(getProjectDir(), projectName.replace(".", "/"));

			File[] listFiles = sourceDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			});

			if (listFiles != null) {
				for (File file : listFiles) {
					getProvider().promptFile(file, "source_resource_section");
				}
			}
		}

		String prompt = promptBundle.getString("additional_rules");
		getProvider().prompt(prompt);
	}

	/**
	 * <pre>
	 * 1. Look for pyproject.toml, setup.py, or setup.cfg. 
	 * 2. Check for requirements.txt, Pipfile, or virtual environment directories (venv, .venv). 
	 * 3. Look for .py files or common Python project structures.
	 * </pre>
	 * 
	 * @param projectDir
	 * @return
	 */
	public static boolean isPythonProject(File projectDir) {

		boolean result = false;
		try {
			if (new File(projectDir, PROJECT_MODEL_FILE_NAME).exists()) {
				File pyprojectTomlFile = new File(projectDir, PROJECT_MODEL_FILE_NAME);
				TomlParseResult toml = Toml.parse(pyprojectTomlFile.toPath());
				String projectName = toml.getString("project.name");

				boolean privateProject = false;
				TomlArray classifiers = toml.getArray("project.classifiers");
				if (classifiers != null) {
					List<Object> classifierList = classifiers.toList();
					for (Object classifier : classifierList) {
						if (StringUtils.containsIgnoreCase((String) classifier, "Private")) {
							privateProject = true;
							break;
						}
					}
				}

				result = projectName != null && !privateProject;
			}
		} catch (IOException e) {
			result = false;
		}

		return result;
	}

	@Override
	public List<String> getSources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getDocuments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getTests() {
		// TODO Auto-generated method stub
		return null;
	}

}
