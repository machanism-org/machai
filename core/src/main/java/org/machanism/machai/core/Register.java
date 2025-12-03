package org.machanism.machai.core;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.bindex.BIndexBuilder;
import org.machanism.machai.core.bindex.PomReader;
import org.machanism.machai.core.embedding.EmbeddingBuilder;
import org.machanism.machai.core.embedding.EmbeddingProvider;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Register implements Closeable {

	private static Logger logger = LoggerFactory.getLogger(Register.class);

	private GenAIProvider provider;
	private EmbeddingProvider embeddingProvider;
	private boolean rewriteMode;

	public Register(GenAIProvider provider2) {
		embeddingProvider = new EmbeddingProvider("machanism", "bindex");
	}

	public void regProjects(File projectDir) throws IOException, XmlPullParserException {
		File pomFile = new File(projectDir, "pom.xml");

		Model model = PomReader.getProjectModel(pomFile, false);

		if ("pom".equals(model.getPackaging())) {
			try {
				model = PomReader.getProjectModel(pomFile, true);
			} catch (Exception e) {
				// ignore.
			}
			List<String> modules = model.getModules();
			for (String module : modules) {
				regProjects(new File(projectDir, module));
			}
		} else {
			regBindex(projectDir);
		}
	}

	public String regBindex(File bindexFile)
			throws IOException, JsonProcessingException {
		BIndex bindex = getBindex(bindexFile, rewriteMode);

		String regId = null;
		if (bindex != null) {
			EmbeddingBuilder embeddingBuilder = new EmbeddingBuilder().provider(embeddingProvider);
			regId = embeddingBuilder.bindex(bindex).build();
			logger.info("embeddingId: " + regId);
		}

		return regId;
	}

	public BIndex getBindex(File projectDir, boolean rewriteMode)
			throws IOException, StreamReadException, DatabindException, FileNotFoundException, StreamWriteException {
		logger.info("Project dir: " + projectDir);

		File bindexDir = new File(projectDir, "src/bindex");
		File bindexFile = new File(bindexDir, "bindex.json");

		BIndex bindex;
		if (!rewriteMode && bindexFile.exists()) {
			logger.info("BIndex file exists: " + bindexFile);
			bindex = new ObjectMapper().readValue(new FileReader(bindexFile), BIndex.class);
		} else {
			bindex = new BIndexBuilder()
					.projectDir(projectDir)
					.bindexDir(bindexDir)
					.provider(provider)
					.build();
			if (bindex != null) {
				bindexFile.getParentFile().mkdirs();
				new ObjectMapper().writeValue(bindexFile, bindex);
				logger.info("BIndex file: " + bindexFile);
			}
		}
		return bindex;
	}

	@Override
	public void close() throws IOException {
		embeddingProvider.close();
	}

	public void setRewriteMode(boolean rewriteMode) {
		this.rewriteMode = rewriteMode;
	}

}
