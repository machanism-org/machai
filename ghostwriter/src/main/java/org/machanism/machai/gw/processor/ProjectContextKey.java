package org.machanism.machai.gw.processor;

/**
 * Represents the context metadata keys used for evaluating and storing project layouts.
 * <p>
 * These keys are utilized as identifiers inside the {@code ProjectContextFunctionTools} 
 * registry to associate project-specific environment and directory information with 
 * a given project directory.
 * </p>
 * 
 * @author YourName/Team
 * @version 1.2.2
 */
public enum ProjectContextKey {

    /**
     * The operating system name where the application is currently running.
     * <p>
     * <b>Source:</b> {@code SystemUtils.OS_NAME}
     * </p>
     */
    OPERATING_SYSTEM("OPERATING_SYSTEM"),

    /**
     * The user-friendly name of the project.
     * <p>
     * <b>Source:</b> {@code projectLayout.getProjectName()}
     * </p>
     */
    PROJECT_NAME("PROJECT_NAME"),

    /**
     * The unique identifier of the project.
     * <p>
     * <b>Source:</b> {@code projectLayout.getProjectId()}
     * </p>
     */
    PROJECT_ID("PROJECT_ID"),

    /**
     * The directory name of the project folder itself.
     * <p>
     * <b>Source:</b> {@code projectDir.getName()}
     * </p>
     */
    PROJECT_DIR_NAME("PROJECT_DIR_NAME"),

    /**
     * The unique identifier of the parent project, if one exists.
     * <p>
     * <b>Source:</b> {@code projectLayout.getParentId()}
     * </p>
     */
    PARENT_PROJECT_ID("PARENT_PROJECT_ID"),

    /**
     * The directory name of the parent project folder, or {@code null} if the project has no parent.
     * <p>
     * <b>Source:</b> Derived from {@code projectDir.getParentFile()}
     * </p>
     */
    PARENT_PROJECT_DIR_NAME("PARENT_PROJECT_DIR_NAME"),

    /**
     * The relative filesystem path from the root directory to the project's directory.
     * <p>
     * <b>Source:</b> {@code ProjectLayout.getRelativePath(getRootDir(), projectDir)}
     * </p>
     */
    REL_PATH_FROM_ROOT("REL_PATH_FROM_ROOT"),

    /**
     * The classification or structure style of the layout (e.g., Maven, Gradle, Custom).
     * <p>
     * <b>Source:</b> {@code projectLayout.getProjectLayoutType()}
     * </p>
     */
    LAYOUT_TYPE("LAYOUT_TYPE"),

    /**
     * A formatted list/line describing the path information for the main source and resource directories.
     * <p>
     * <b>Source:</b> Evaluated paths of {@code projectLayout.getSources()}
     * </p>
     */
    SRC_AND_RESOURCE_DIRS("SRC_AND_RESOURCE_DIRS"),

    /**
     * A formatted list/line describing the path information for test source and resource directories.
     * <p>
     * <b>Source:</b> Evaluated paths of {@code projectLayout.getTests()}
     * </p>
     */
    TEST_SRC_AND_RESOURCE_DIRS("TEST_SRC_AND_RESOURCE_DIRS"),

    /**
     * A formatted list/line describing the path information for documentation directories.
     * <p>
     * <b>Source:</b> Evaluated paths of {@code projectLayout.getDocuments()}
     * </p>
     */
    DOCS_DIRS("DOCS_DIRS"),

    /**
     * A formatted list/line describing the project sub-modules.
     * <p>
     * <b>Source:</b> Evaluated paths of {@code projectLayout.getModules()}
     * </p>
     */
    MODULES("MODULES");

    private final String key;

    /**
     * Constructs a {@code ProjectContextKey} constant mapped to its raw string representation.
     *
     * @param key the string key used in the backend registry map
     */
    ProjectContextKey(String key) {
        this.key = key;
    }

    /**
     * Gets the raw string value of the metadata key.
     *
     * @return the metadata key as a {@link String}
     */
    public String getKey() {
        return this.key;
    }
}