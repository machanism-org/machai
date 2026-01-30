package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;

/**
 * Service-provider interface (SPI) for components that scan project files and extract {@code @guidance}
 * instructions for downstream processing.
 *
 * <p>Implementations typically target a specific file format (for example, Java, Markdown, or HTML), understand
 * that format's comment conventions, and produce a normalized prompt fragment that includes path context.
 */
public interface Reviewer {

    /**
     * Reviews a file and returns a formatted fragment (often including file content and/or extracted guidance)
     * for use by the Ghostwriter pipeline.
     *
     * @param projectDir the project root directory used to compute related paths for context
     * @param file the file to analyze
     * @return a formatted prompt fragment, or {@code null} when no relevant guidance is present
     * @throws IOException if an error occurs reading the file
     */
    String perform(File projectDir, File file) throws IOException;

    /**
     * Returns the file extensions (without the dot) that this reviewer can process.
     *
     * @return supported file extensions
     */
    String[] getSupportedFileExtensions();

}
