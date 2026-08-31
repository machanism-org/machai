package org.machanism.machai.project.layout;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Strings;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

/**
 * Detects public Python projects described by {@code pyproject.toml}.
 *
 * <p>
 * {@link #isPythonProject(File)} accepts descriptors with a {@code project.name}
 * and rejects descriptors classified as private. This layout does not currently
 * infer module, source, documentation, or test directories; the corresponding
 * accessors return empty lists.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class PythonProjectLayout extends ProjectLayout {

    /**
     * Creates a Python project layout instance.
     */
    public PythonProjectLayout() {
        // Sonar (java:S1186): retained for reflection-based instantiation.
    }

    /** Conventional Python project descriptor used for project detection. */
    private static final String PROJECT_MODEL_FILE_NAME = "pyproject.toml";

    /**
     * Checks whether a directory contains a public Python project described by
     * {@code pyproject.toml}.
     *
     * <p>The descriptor must define {@code project.name}. Projects whose
     * {@code project.classifiers} contain {@code Private} are excluded. Invalid
     * or unreadable TOML descriptors are treated as non-Python projects.</p>
     *
     * @param projectDir directory to examine for {@code pyproject.toml}
     * @return {@code true} when a public named project is detected; {@code false}
     *         otherwise
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
                        if (Strings.CI.contains((String) classifier, "Private")) {
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
     * Returns source directories discovered for the Python project.
     *
     * @return empty list; intentionally returns a safe default until Python source discovery is implemented
     */
    @Override
    public List<String> getSources() {
        // Sonar(java:S1135): use an intentional default return instead of a task marker.
        return Collections.emptyList();
    }

    /**
     * Returns documentation directories discovered for the Python project.
     *
     * @return empty list; intentionally returns a safe default until Python documentation discovery is implemented
     */
    @Override
    public List<String> getDocuments() {
        // Sonar(java:S1135): use an intentional default return instead of a task marker.
        return Collections.emptyList();
    }

    /**
     * Returns test source directories discovered for the Python project.
     *
     * @return empty list; intentionally returns a safe default until Python test discovery is implemented
     */
    @Override
    public List<String> getTests() {
        // Sonar(java:S1135): use an intentional default return instead of a task marker.
        return Collections.emptyList();
    }

}
