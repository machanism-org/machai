package org.machanism.machai.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.machanism.machai.core.bindex.PomReader;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ScanProject {
	private static Logger logger = LoggerFactory.getLogger(ScanProject.class);

	public String scanProjects(File projectDir) throws IOException, XmlPullParserException {
		File pomFile = new File(projectDir, "pom.xml");
		Model model = PomReader.getProjectModel(pomFile, false);

		String regBindex = null;
		if ("pom".equals(model.getPackaging())) {
			try {
				model = PomReader.getProjectModel(pomFile, true);
			} catch (Exception e) {
				// ignore.
			}
			List<String> modules = model.getModules();
			for (String module : modules) {
				scanProjects(new File(projectDir, module));
			}
		} else {
			regBindex = processProject(projectDir);
		}

		return regBindex;
	}

	public abstract String processProject(File projectDir);

	public BIndex getBindex(File projectDir) throws IOException {
		logger.info("Project dir: " + projectDir);
		File bindexFile = getBindexFile(projectDir);

		try {
		BIndex bindex = null;
		if (bindexFile.exists()) {
			bindex = new ObjectMapper().readValue(new FileReader(bindexFile), BIndex.class);
		}
		return bindex;
		}catch (Exception e) {
			throw new IllegalArgumentException("Bindex: " + bindexFile, e);
		}
	}

	public File getBindexFile(File projectDir) {
		return new File(projectDir, "src/bindex/bindex.json");
	}

}