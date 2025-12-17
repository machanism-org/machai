package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

public class PythonBIndexBuilder extends BIndexBuilder {

	private static final String PROJECT_MODEL_FILE_NAME = "pyproject.toml";

	@Override
	protected void projectContext() throws IOException {
		File pyprojectTomlFile = new File(getProjectDir(), PROJECT_MODEL_FILE_NAME);
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
					getProvider().promptFile("source_resource_section", file);
				}
			}
		}
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
				result = projectName != null;
			}
		} catch (IOException e) {
			result = false;
		}

		return result;
	}

}
