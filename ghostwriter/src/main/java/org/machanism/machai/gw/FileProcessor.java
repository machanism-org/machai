package org.machanism.machai.gw;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.gw.reviewer.Reviewer;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileProcessor extends ProjectProcessor {
    /** Logger for documentation input processing events. */
    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);

    /** Tag name for guidance comments. */
    public static final String GUIDANCE_TAG_NAME = "@guidance:";

    /** Temporary directory for documentation inputs. */
    public static final String GW_TEMP_DIR = "docs-inputs";

    /** Resource bundle supplying prompt templates for generators. */
    private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

    /** Provider for AI document generation. */
    private final GenAIProvider provider;
    /** Function tool utility for environment setup. */
    private final SystemFunctionTools systemFunctionTools;

    /** Directory-level guidance mappings. */
    private final Map<String, String> dirGuidanceMap = new HashMap<>();
    /** Reviewer type associations. */
    private final Map<String, Reviewer> reviewMap = new HashMap<>();

    /** Root scanning directory for the current documentation run. */
    private File rootDir;
    /** Indicates if guidance inheritance across directories is enabled. */
    private boolean inheritance;
    /** Indicates if parent directory guidances should be used during scan. */
    private boolean useParentsGuidances;

    /**
     * Constructs a FileProcessor for documentation input preparation.
     *
     * @param provider the AI provider for document generation
     */
    public FileProcessor(GenAIProvider provider) {
        this.provider = provider;

        systemFunctionTools = new SystemFunctionTools();
        systemFunctionTools.applyTools(provider);

        loadReviewers();
    }

    /**
     * Loads file reviewers via the ServiceLoader registry, mapping supported file
     * extensions. Associates each reviewer with its extension support in the
     * reviewMap.
     * @guidance: loadReviewers method must be accurately implemented according to the latest guidelines.
     */
    private void loadReviewers() {
        ServiceLoader<Reviewer> reviewerServiceLoader = ServiceLoader.load(Reviewer.class);

        for (Reviewer reviewer : reviewerServiceLoader) {
            reviewer.setDirGuidanceMap(dirGuidanceMap);
            String[] extensions = reviewer.getSupportedFileExtentions();
            if (extensions == null) {
                continue;
            }
            for (String extension : extensions) {
                if (StringUtils.isBlank(extension)) {
                    continue;
                }
                reviewMap.put(StringUtils.lowerCase(extension.trim()), reviewer);
            }
        }
    }

    /**
     * Scans documents in the given root directory and prepares inputs for
     * documentation generation. This overload defaults the scan start directory to
     * rootDir.
     *
     * @param basedir root directory to scan
     * @throws IOException if an error occurs reading files
     * @guidance: scanDocuments method needs to comply with the latest scanning procedures.
     */
    public void scanDocuments(File basedir) throws IOException {
        scanDocuments(basedir, basedir);
    }

    /**
     * Scans documents in the given root directory and start subdirectory, preparing
     * inputs for documentation generation.
     *
     * @param rootDir the root directory of the project to scan
     * @param dir     the directory to begin scanning
     * @throws IOException if an error occurs reading files
     */
    public void scanDocuments(File rootDir, File dir) throws IOException {
        this.rootDir = rootDir;
        scanFolder(dir);
    }

    /**
     * Recursively scans project folders, processing documentation inputs for all
     * found modules and files.
     * @guidance: scanFolder method implementation must match the required recursive scanning technique.
     *
     * @param projectDir the directory containing the project/module to be scanned
     * @throws IOException if an error occurs reading files
     */
    @Override
    public void scanFolder(File projectDir) throws IOException {
        provider.setWorkingDir(getRootDir(projectDir));

        ProjectLayout projectLayout = getProjectLayout(projectDir);
        List<String> modules = projectLayout.getModules();
        if (modules != null) {
            File[] files = projectDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!StringUtils.equalsAnyIgnoreCase(file.getName(), modules.toArray(new String[] {}))
                            && !StringUtils.containsAny(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {
                        if (file.isDirectory()) {
                            processProjectDir(projectLayout, file);
                        } else {
                            String result = processFile(projectLayout, file);
                            if (StringUtils.isNoneBlank(result)) {
                                logger.info(result);
                            }
                        }
                    }
                }
            }
        }

        super.scanFolder(projectDir);
    }

    /**
     * Processes the project layout for documentation gathering.
     * @guidance: processFolder method should be implemented in accordance with guidelines for folder processing.
     *
     * @param projectLayout layout describing sources, tests, docs, and modules
     */
    @Override
    public void processFolder(ProjectLayout projectLayout) {
        File projectDir = projectLayout.getProjectDir();
        processProjectDir(projectLayout, projectDir);
    }

    /**
     * Processes the selected project directory for documentation guidance
     * extraction. Finds files, applies reviewer logic, and logs results.
     *
     * @param projectLayout layout against which files are processed
     * @param scanDir       directory to scan for files
     */
    private void processProjectDir(ProjectLayout projectLayout, File scanDir) {
        try {
            List<File> files = findFiles(scanDir);
            if (!files.isEmpty()) {
                for (File file : files) {
                    String result = processFile(projectLayout, file);
                    if (StringUtils.isNoneBlank(result)) {
                        logger.info(result);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Processes the given file using the configured reviewers for documentation
     * input preparation.
     * @guidance: processFile method needs to be compliant with latest guidelines for file processing.
     *
     * @param projectLayout the project layout instance
     * @param file          the file to process
     * @return extracted guidance result, or null if not applicable
     * @throws IOException if file reading fails
     */
    private String processFile(ProjectLayout projectLayout, File file) throws IOException {
        File projectDir = projectLayout.getProjectDir();
        String guidance = parseFile(projectDir, file);

        String result = null;
        if (guidance != null) {
            provider.instructions(promptBundle.getString("sys_instractions"));
            provider.prompt(promptBundle.getString("docs_processing_instractions"));

            if (useParentsGuidances) {
                List<String> parentsGuidances = getParentsGuidances(projectLayout, file);
                for (String dirGuidance : parentsGuidances) {
                    provider.prompt(dirGuidance);
                }
            }

            String projectInfo = getProjectStructureDescription(projectLayout);
            provider.prompt(projectInfo);
            provider.prompt(guidance);
            provider.prompt(promptBundle.getString("output_format"));

            String inputsFileName = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), file);
            File docsTempDir = new File(projectDir, MACHAI_TEMP_DIR + "/" + GW_TEMP_DIR);
            File inputsFile = new File(docsTempDir, inputsFileName + ".txt");

            provider.inputsLog(inputsFile);
            result = provider.perform();
        }

        return result;
    }

    /**
     * Returns a list of parent guidances based on layout and file hierarchy. Used
     * for context propagation when scanning directories.
     *
     * @param projectLayout project layout for context acquisition
     * @param file          file whose parent guidance is sought
     * @return list of parent guidance texts
     */
    private List<String> getParentsGuidances(ProjectLayout projectLayout, File file) {
        String projectPath = ProjectLayout.getRelatedPath(rootDir, projectLayout.getProjectDir(), true);
        int skipNumber = StringUtils.split(projectPath, "/").length;

        String parentsPath = ProjectLayout.getRelatedPath(getRootDir(projectLayout.getProjectDir()), file.getParentFile(),
                true);

        StringBuilder path = new StringBuilder();
        String[] parents = StringUtils.split(parentsPath, "/");
        List<String> guidances = new ArrayList<String>();

        for (String parent : parents) {
            if (!".".equals(parent)) {
                path.append("/");
            }
            path.append(parent);
            if (skipNumber-- <= 0 || inheritance) {
                if (!StringUtils.equals(path, parentsPath)) {
                    String dirGuidance = dirGuidanceMap.get(path.toString());
                    if (StringUtils.isNotBlank(dirGuidance)) {
                        guidances.add(dirGuidance);
                    }
                }
            }
        }
        return guidances;
    }

    /**
     * Returns a textual description of the current project structure using prompt
     * templates.
     *
     * @param projectLayout the layout to describe
     * @return formatted structure description for prompts
     * @throws IOException if template resources are unavailable
     */
    private String getProjectStructureDescription(ProjectLayout projectLayout) throws IOException {
        List<String> content = new ArrayList<String>();

        String path = ProjectLayout.getRelatedPath(rootDir, projectLayout.getProjectDir());

        content.add(path);
        File projectDir = projectLayout.getProjectDir();
        content.add(getDirInfoLine(projectLayout.getSources(), projectDir));
        content.add(getDirInfoLine(projectLayout.getTests(), projectDir));
        content.add(getDirInfoLine(projectLayout.getDocuments(), projectDir));
        content.add(getDirInfoLine(projectLayout.getModules(), projectDir));

        return MessageFormat.format(promptBundle.getString("project_information"), content.toArray());
    }

    /**
     * Returns a comma-separated string of directories for sources, tests, or
     * modules. Only directories that exist in the file system are listed.
     *
     * @param sources    list of directory names
     * @param projectDir base directory
     * @return formatted line for prompt or "not defined" if no entries found
     */
    private String getDirInfoLine(List<String> sources, File projectDir) {
        String line = null;
        if (sources != null && !sources.isEmpty()) {
            List<String> dirs = sources.stream().filter(t -> {
                boolean exists = false;
                if (t != null) {
                    File file = new File(projectDir, t);
                    exists = file.exists();
                }
                return exists;
            }).map(e -> {
                String path = ProjectLayout.getRelatedPath(rootDir, new File(projectDir, e));
                return "`" + path + "`";
            }).collect(Collectors.toList());
            line = StringUtils.join(dirs, ", ");
        }

        if (StringUtils.isBlank(line)) {
            line = "not defined";
        }
        return line;
    }

    /**
     * Runs the review process for a file using matching reviewer, extracting
     * guidance.
     * @guidance: parseFile method needs to support latest review procedures.
     *
     * @param projectDir root directory
     * @param file       file to be processed
     * @return guidance string, or null if not applicable
     * @throws IOException if reviewer encounters a file error
     */
    private String parseFile(File projectDir, File file) throws IOException {
        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        Reviewer reviewer = reviewMap.get(StringUtils.lowerCase(extension));

        String result = null;
        if (reviewer != null) {
            result = reviewer.perform(getRootDir(projectDir), file);
        }

        return result;
    }

    /**
     * Recursively finds all files (excluding EXCLUDE_DIRS) in a directory
     * structure.
     * @guidance: findFiles method should be correctly implemented following the recursive file finding guidelines.
     *
     * @param projectDir directory to search
     * @return list of files found
     */
    private List<File> findFiles(File projectDir) {
        List<File> result = new ArrayList<>();
        if (projectDir != null && projectDir.isDirectory()) {
            File[] files = projectDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!StringUtils.equalsAnyIgnoreCase(file.getName(), ProjectLayout.EXCLUDE_DIRS)) {
                        if (file.isDirectory()) {
                            result.addAll(findFiles(file));
                        } else {
                            result.add(file);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the root directory for documentation scanning, falling back to input
     * if unset.
     *
     * @param projectDir the directory detected as project root
     * @return the effective root directory
     */
    public File getRootDir(File projectDir) {
        return rootDir != null ? rootDir : projectDir;
    }

    /**
     * Returns true if directory guidance inheritance is enabled.
     *
     * @return inheritance enabled flag
     */
    public boolean isInheritance() {
        return inheritance;
    }

    /**
     * Sets whether directory guidance inheritance is enabled.
     *
     * @param inheritance true to enable inheritance
     */
    public void setInheritance(boolean inheritance) {
        this.inheritance = inheritance;
    }

    /**
     * Returns true if parent guidances are used in document preparation.
     *
     * @return parent guidance enabled flag
     */
    public boolean isUseParentsGuidances() {
        return useParentsGuidances;
    }

    /**
     * Sets whether parent guidances are included in documentation preparation.
     *
     * @param useParentsGuidances true to enable use of parent guidances
     */
    public void setUseParentsGuidances(boolean useParentsGuidances) {
        this.useParentsGuidances = useParentsGuidances;
    }

    public static boolean deleteTempFiles(File basedir) {
        File file = new File(basedir, FileProcessor.MACHAI_TEMP_DIR + "/" + FileProcessor.GW_TEMP_DIR);
        logger.info("Removing '{}' inputs log file.", file);
        return FileUtils.deleteQuietly(file);
    }
}
