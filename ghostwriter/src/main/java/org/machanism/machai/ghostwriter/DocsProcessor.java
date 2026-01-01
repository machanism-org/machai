package org.machanism.machai.ghostwriter;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.ghostwriter.reviewer.JavaReviewer;
import org.machanism.machai.ghostwriter.reviewer.MarkdownReviewer;
import org.machanism.machai.ghostwriter.reviewer.PythonReviewer;
import org.machanism.machai.ghostwriter.reviewer.Reviewer;
import org.machanism.machai.ghostwriter.reviewer.TextReviewer;
import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Processor for project documentation generation.
 * <p>
 * Scans project sources, applies file reviewers for extracting documentation guidance,
 * and orchestrates the input preparation for large language model document generation.
 */
public class DocsProcessor extends ProjectProcessor {

    /** The guidance tag name for documentation. */
    public static final String GUIDANCE_TAG_NAME = "@guidance";
    private static final String DOCS_TEMP_DIR = ".machai/docs-inputs";
    private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

    private String chatModel = "OpenAI:gpt-5-mini";

    private GenAIProvider provider;
    private SystemFunctionTools systemFunctionTools;

    private Map<String, String> dirGuidanceMap = new HashMap<>();
    private Map<String, Reviewer> reviewMap = new HashMap<>();

    private File rootDir;

    /**
     * Constructs a DocsProcessor for documentation input preparation.
     */
    public DocsProcessor() {
        provider = GenAIProviderManager.getProvider(chatModel);
        provider.promptBundle(promptBundle);
        systemFunctionTools = new SystemFunctionTools(null);
        systemFunctionTools.applyTools(provider);

        reviewMap.put("txt", new TextReviewer(dirGuidanceMap));
        reviewMap.put("java", new JavaReviewer());
        reviewMap.put("md", new MarkdownReviewer());
        reviewMap.put("md", new PythonReviewer());
    }

    /**
     * Scans documents in the given root directory and prepares inputs for documentation generation.
     *
     * @param rootDir the root directory of the project to scan
     * @throws IOException if an error occurs reading files
     */
    public void scanDocuments(File rootDir) throws IOException {
        this.rootDir = rootDir;
        scanProjects(rootDir);
    }

    /**
     * Recursively scans projects, processing documentation inputs for all found project modules and files.
     *
     * @param projectDir the directory containing the project to be scanned
     * @throws IOException if an error occurs reading files
     */
    @Override
    public void scanProjects(File projectDir) throws IOException {
        systemFunctionTools.setWorkingDir(getRootDir(projectDir));

        ProjectLayout projectLayout = ProjectLayoutManager.detectProjectLayout(projectDir);
        List<String> modules = projectLayout.getModules();
        if (modules != null) {
            File[] files = projectDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!StringUtils.equalsAnyIgnoreCase(file.getName(), modules.toArray(new String[] {}))
                            && !StringUtils.containsAny(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {
                        if (file.isDirectory()) {
                            processProject(projectLayout, file);
                        } else {
                            processFile(projectLayout, file);
                        }
                    }
                }
            }
        }

        super.scanProjects(projectDir);
    }

    /**
     * Processes the given project layout for documentation purposes.
     *
     * @param projectLayout the detected project layout describing sources, tests, docs, and modules
     */
    @Override
    public void processProject(ProjectLayout projectLayout) {
        File projectDir = projectLayout.getProjectDir();
        processProject(projectLayout, projectDir);
    }

    // ... rest are private or package-private

    /**
     * Returns the root directory for the documentation scan.
     * Defaults to input directory if root is not set.
     *
     * @param projectDir the directory detected as project root
     * @return the effective root directory
     */
    public File getRootDir(File projectDir) {
        return rootDir != null ? rootDir : projectDir;
    }

}
