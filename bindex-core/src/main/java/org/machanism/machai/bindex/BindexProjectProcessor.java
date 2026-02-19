package org.machanism.machai.bindex;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Shared utilities for working with {@code bindex.json} in project processing workflows.
 *
 * <p>This base class provides helper methods to locate and parse Bindex documents from a project * directory. It is used by concrete processors such as {@link BindexCreator} and * {@link BindexRegister}.
 *
 * <h2>Example</h2>
 *
 * <pre>
 * File projectDir = new File("C:\\work\\my-project");
 * Bindex bindex = getBindex(projectDir);
 * File bindexFile = getBindexFile(projectDir);
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public abstract class BindexProjectProcessor extends ProjectProcessor {

	/** The default Bindex document file name. */
	public static final String BINDEX_FILE_NAME = "bindex.json";

	/** Logger for this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BindexProjectProcessor.class);

	/**
	 * Loads a {@link Bindex} from {@code bindex.json} in the given project directory.
	 *
	 * @param projectDir project directory that may contain {@code bindex.json}
	 * @return parsed Bindex instance, or {@code null} when the file does not exist
	 * @throws IllegalArgumentException if parsing fails
	 */
	public Bindex getBindex(File projectDir) {
		LOGGER.info("Project dir: {}", projectDir);
		File bindexFile = getBindexFile(projectDir);

		if (!bindexFile.exists()) {
			return null;
		}

		try (Reader reader = new FileReader(bindexFile)) {
			return new ObjectMapper().readValue(reader, Bindex.class);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Returns the {@link File} location for {@code bindex.json} in the provided project directory.
	 *
	 * @param projectDir project directory
	 * @return file pointing to {@code bindex.json}
	 */
	public File getBindexFile(File projectDir) {
		return new File(projectDir, BINDEX_FILE_NAME);
	}
}
