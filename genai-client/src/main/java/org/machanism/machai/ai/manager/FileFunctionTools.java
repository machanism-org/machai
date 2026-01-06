package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provides file system utility tools for use in GenAIProvider environments.
 * <p>
 * Capable of reading, writing, and recursively listing files and directories.
 * <p>
 * Usage example:
 * <pre>
 *   FileFunctionTools tools = new FileFunctionTools(new File("/tmp"));
 *   tools.applyTools(provider);
 * </pre>
 *
 * @author Viktor Tovstyi
 * @guidance
 */
public class FileFunctionTools {

    /** Logger for file tool operations and diagnostics. */
    private static Logger logger = LoggerFactory.getLogger(FileFunctionTools.class);
    private File workingDir;

    /**
     * Initializes with the target working directory.
     * @param workingDir Directory for file operations
     */
    public FileFunctionTools(File workingDir) {
        super();
        this.setWorkingDir(workingDir);
    }

    /**
     * Installs file read/write/list tools into the provided GenAIProvider.
     * @param provider GenAIProvider instance
     */
    public void applyTools(GenAIProvider provider) {
        provider.addTool("read_file_from_file_system", "Read the contents of a file from the disk.", p -> readFile(p),
                "file_path:string:required:The path to the file to be read.");
        provider.addTool("write_file_to_file_system", "Write changes to a file on the file system.", p -> writeFile(p),
                "file_path:string:required:The path to the file you want to write to or create.",
                "text:string:required:The content to be written into the file (text, code, etc.).");
        provider.addTool("list_files_in_directory", "List files and directories in a specified folder.",
                p -> listFiles(p),
                "dir_path:string:optional:The path to the directory to list contents of.");
        provider.addTool("get_recursive_file_list",
                "List files recursively in a directory (includes files in subdirectories).", p -> getRecursiveFiles(p),
                "dir_path:string:optional:Path to the folder to list contents recursively.");
    }

    /**
     * Lists files recursively in a directory.
     * @param params JsonNode containing optional "dir_path" field
     * @return String listing file paths or message if none found
     */
    private Object getRecursiveFiles(JsonNode params) {

        File workingDir = getWorkingDir();

        JsonNode jsonNode = params.get("dir_path");
        File directory;

        if (jsonNode != null) {
            String filePath = jsonNode.textValue();
            if (StringUtils.isBlank(filePath)) {
                directory = workingDir;
            } else {
                directory = new File(workingDir, filePath);
            }
        } else {
            directory = workingDir;
        }

        logger.info("List files recursively: {}", params);

        List<File> listFiles = listFilesRecursively(directory);
        StringBuilder content = new StringBuilder();
        if (!listFiles.isEmpty()) {
            for (File file : listFiles) {
                content.append(file.getAbsolutePath() + "\n");
            }
        } else {
            content.append("No files found in directory.");
        }

        return content.toString();
    }

    /**
     * Helper to collect all files recursively.
     * @param directory Directory to start with
     * @return List of files
     */
    private List<File> listFilesRecursively(File directory) {
        List<File> allFiles = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        allFiles.add(file);
                    } else if (file.isDirectory()) {
                        allFiles.addAll(listFilesRecursively(file));
                    }
                }
            }
        }
        return allFiles;
    }

    /**
     * Lists files in a directory.
     * @param params JsonNode with "dir_path" (optional)
     * @return String listing file paths
     */
    private Object listFiles(JsonNode params) {
        String filePath = params.get("dir_path").asText();
        File directory = new File(getWorkingDir(), StringUtils.defaultIfBlank(filePath, "."));

        String result;
        if (directory.isDirectory()) {
            File[] listFiles = directory.listFiles();
            StringBuilder content = new StringBuilder();
            for (File file : listFiles) {
                content.append(file.getAbsolutePath() + "\n");
            }

            result = content.toString();
        } else {
            result = "No files found in directory.";
        }
        return result;
    }

    /**
     * Writes contents to file in the working directory.
     * @param params JsonNode containing "file_path" and "text"
     * @return true if successful
     * @throws IllegalArgumentException on IO error
     */
    private Object writeFile(JsonNode params) {
        String filePath = params.get("file_path").asText();
        String text = params.get("text").asText();

        logger.info("Write file: {}", StringUtils.abbreviate(params.toString(), 80));

        File file = new File(getWorkingDir(), filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            IOUtils.write(text, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return true;
    }

    /**
     * Reads contents of a file in the working directory.
     * @param params JsonNode containing "file_path"
     * @return File content as string
     * @throws IllegalArgumentException on IO error
     */
    private Object readFile(JsonNode params) {
        String filePath = params.get("file_path").asText();

        logger.info("Read file: {}", params);

        try (FileInputStream io = new FileInputStream(new File(getWorkingDir(), filePath))) {
            return IOUtils.toString(io, "UTF8");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Gets current working directory.
     * @return Working directory
     * @throws IllegalArgumentException if not set
     */
    public File getWorkingDir() {
        if (workingDir == null) {
            throw new IllegalArgumentException("The function tool working dir is not defined.");
        }
        return workingDir;
    }

    /**
     * Sets working directory for this tool instance.
     * @param workingDir Directory for operations
     */
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }
}
