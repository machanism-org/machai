package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

public class PythonProjectLayout extends ProjectLayout {

	private static final String PROJECT_MODEL_FILE_NAME = "pyproject.toml";

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
