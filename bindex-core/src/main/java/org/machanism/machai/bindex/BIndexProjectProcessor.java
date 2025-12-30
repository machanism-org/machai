package org.machanism.machai.bindex;

import java.io.File;
import java.io.FileReader;

import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BIndexProjectProcessor extends ProjectProcessor {

	public static final String BINDEX_FILE_NAME = "bindex.json";

	private static Logger logger = LoggerFactory.getLogger(BIndexProjectProcessor.class);

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

	public File getBindexFile(File projectDir) {
		return new File(projectDir, BINDEX_FILE_NAME);
	}
}
