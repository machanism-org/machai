package org.machanism.machai.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.machanism.machai.core.bindex.BIndexBuilder;
import org.machanism.machai.core.bindex.BIndexBuilderFactory;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ScanProject {
	private static Logger logger = LoggerFactory.getLogger(ScanProject.class);

	public void scanProjects(File basedir) throws IOException {
		scanProjects(basedir, true);
	}

	public String scanProjects(File projectDir, boolean create) throws IOException {
		BIndexBuilder bindexBuilder = BIndexBuilderFactory.builder(projectDir, create);
		bindexBuilder.projectDir(projectDir);
		List<String> modules = bindexBuilder.getModules();

		String regBindex = null;
		if (modules != null) {
			for (String module : modules) {
				scanProjects(new File(projectDir, module), create);
			}
		} else {
			bindexBuilder = BIndexBuilderFactory.builder(projectDir, create);
			try {
				regBindex = processProject(projectDir, bindexBuilder);
			} catch (Exception e) {
				logger.error("Project dir: {}, Error: {}", projectDir, StringUtils.abbreviate(e.getMessage(), 120));
			}
		}

		return regBindex;
	}

	public abstract String processProject(File projectDir, BIndexBuilder bindexBuilder);

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
		return new File(projectDir, "bindex.json");
	}
}