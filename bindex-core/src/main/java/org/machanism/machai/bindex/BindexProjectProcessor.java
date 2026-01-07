package org.machanism.machai.bindex;

import java.io.File;
import java.io.FileReader;

import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract class providing Bindex utilities for project processing operations.
 * <p>
 * Provides functions to fetch Bindex objects and files from project
 * directories. Extends {@link ProjectProcessor} to allow Bindex-specific
 * project work.
 *
 *
 * Usage Example:
 * 
 * <pre>
 * Bindex bindex = getBindex(new File("project-dir"));
 * File bindexFile = getBindexFile(new File("project-dir"));
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public abstract class BindexProjectProcessor extends ProjectProcessor {

	/** Name of the Bindex document file. */
	public static final String BINDEX_FILE_NAME = "bindex.json";

	/** Logger for BindexProjectProcessor class. */
	private static Logger logger = LoggerFactory.getLogger(BindexProjectProcessor.class);

	/**
	 * Loads the Bindex object from the bindex file in a given project directory.
	 *
	 * @param projectDir Project directory
	 * @return Bindex object, or null if not present
	 * @throws IllegalArgumentException If file reading/parsing fails
	 */
	public Bindex getBindex(File projectDir) {
		logger.info("Project dir: {}", projectDir);
		File bindexFile = getBindexFile(projectDir);

		Bindex bindex = null;
		try {
			if (bindexFile.exists()) {
				bindex = new ObjectMapper().readValue(new FileReader(bindexFile), Bindex.class);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return bindex;
	}

	/**
	 * Gets the File object for the Bindex document in a given project directory.
	 *
	 * @param projectDir Project directory
	 * @return File pointing to the bindex.json document location
	 */
	public File getBindexFile(File projectDir) {
		return new File(projectDir, BINDEX_FILE_NAME);
	}
}
