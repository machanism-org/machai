package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;

/**
 * Interface for file reviewers that analyze project files
 * to extract or process guidance for documentation generation.
 */
public interface Reviewer {

    /**
     * Analyzes and extracts guidance information from the specified file
     * to aid documentation generation.
     *
     * @param projectDir the root directory of the project for context
     * @param file the file to be reviewed and analyzed
     * @return the extracted guidance or documentation fragment, or {@code null} if none found
     * @throws IOException if an error occurs reading the file
     */
    String perform(File projectDir, File file) throws IOException;

    /**
     * Returns an array of supported file extensions for this reviewer.
     * These extensions determine which files the reviewer is capable of processing.
     *
     * @return an array of supported file extension strings (without dot)
     */
    String[] getSupportedFileExtentions();

}
