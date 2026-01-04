package org.machanism.machai.bindex;

import java.io.File;
import java.io.FileReader;

import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract class providing BIndex utilities for project processing operations.
 * <p>
 * Provides functions to fetch BIndex objects and files from project directories.
 * Extends {@link ProjectProcessor} to allow BIndex-specific project work.
 *
 *
 * Usage Example:
 * <pre>
 *     BIndex bindex = getBindex(new File("project-dir"));
 *     File bindexFile = getBindexFile(new File("project-dir"));
 * </pre>
 *
 * @author machanism.org
 * @since 1.0
 */
public abstract class BIndexProjectProcessor extends ProjectProcessor {

    /** Name of the BIndex document file. */
    public static final String BINDEX_FILE_NAME = "bindex.json";

    /** Logger for BIndexProjectProcessor class. */
    private static Logger logger = LoggerFactory.getLogger(BIndexProjectProcessor.class);

    /**
     * Loads the BIndex object from the bindex file in a given project directory.
     *
     * @param projectDir Project directory
     * @return BIndex object, or null if not present
     * @throws IllegalArgumentException If file reading/parsing fails
     */
    public BIndex getBindex(File projectDir) {
        logger.info("Project dir: {}", projectDir);
        File bindexFile = getBindexFile(projectDir);

        BIndex bindex = null;
        try {
            if (bindexFile.exists()) {
                bindex = new ObjectMapper().readValue(new FileReader(bindexFile), BIndex.class);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return bindex;
    }

    /**
     * Gets the File object for the BIndex document in a given project directory.
     *
     * @param projectDir Project directory
     * @return File pointing to the bindex.json document location
     */
    public File getBindexFile(File projectDir) {
        return new File(projectDir, BINDEX_FILE_NAME);
    }
}
