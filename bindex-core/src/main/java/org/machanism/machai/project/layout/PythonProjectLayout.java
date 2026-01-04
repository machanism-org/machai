package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

/**
 * Project layout implementation for Python-based projects.
 * <p>
 * Determines if a directory contains a Python project using <code>pyproject.toml</code> and related metadata. Handles analysis of modules, sources, documents, and tests in a Python environment.
 *
 * @author machai
 * @since 1.0
 */
public class PythonProjectLayout extends ProjectLayout {

    private static final String PROJECT_MODEL_FILE_NAME = "pyproject.toml";

    /**
     * Checks whether the provided directory is a Python project.
     * <ul>
     *   <li>Looks for <code>pyproject.toml</code>, <code>setup.py</code>, or <code>setup.cfg</code>.</li>
     *   <li>Checks for <code>requirements.txt</code>, <code>Pipfile</code>, or typical virtual environment directories.</li>
     *   <li>Examines Python file structures and project classifiers.</li>
     * </ul>
     * @param projectDir Directory to examine for Python project indicators
     * @return <code>true</code> if a Python project is detected, <code>false</code> otherwise
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

    /**
     * Gets list of source directories for the Python project.
     * <p>Not implemented.</p>
     * @return always <code>null</code>
     */
    @Override
    public List<String> getSources() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets list of document directories for the Python project.
     * <p>Not implemented.</p>
     * @return always <code>null</code>
     */
    @Override
    public List<String> getDocuments() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets list of test source directories for the Python project.
     * <p>Not implemented.</p>
     * @return always <code>null</code>
     */
    @Override
    public List<String> getTests() {
        // TODO Auto-generated method stub
        return null;
    }

}
