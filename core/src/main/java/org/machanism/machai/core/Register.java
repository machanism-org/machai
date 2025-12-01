package org.machanism.machai.core;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Model;
import org.bson.BsonValue;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.models.ChatModel;

public class Register implements Closeable {

	private static Logger logger = LoggerFactory.getLogger(Register.class);

	private GenAIProvider provider;

	private EmbeddingProvider embeddingProvider;

	public Register() {
		provider = new GenAIProvider(ChatModel.GPT_5);
		provider.setDebugMode(true);
		provider.setRequestDisable(true);

		embeddingProvider = new EmbeddingProvider("machanism", "bindex");
	}

	public void scanProject(File projectDir) throws IOException, XmlPullParserException {
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
				scanProject(new File(projectDir, module));
			}
		} else {
			regProject(projectDir);
		}
	}

	public void regProject(File projectDir)
			throws IOException, JsonProcessingException {
		logger.info("Project dir: " + projectDir);

		File bindexDir = new File(projectDir, "src/bindex");
		File bindexFile = new File(bindexDir, "bindex.json");

		BIndex bindex;
		if (bindexFile.exists()) {
			logger.info("BIndex file exists: " + bindexFile);
			bindex = new ObjectMapper().readValue(new FileReader(bindexFile), BIndex.class);
		} else {
			bindex = new BIndexBuilder()
					.projectDir(projectDir)
					.provider(provider)
					.bindexDir(bindexDir)
					.build();
			if (bindex != null) {
				new ObjectMapper().writeValue(bindexFile, bindex);
				logger.info("BIndex file: " + bindexFile);
			}
		}

		if (bindex != null) {
			String embeddingId;
			File embeddingFile = new File(bindexDir, "embedding.data");
			EmbeddingBuilder embeddingBuilder = new EmbeddingBuilder().provider(embeddingProvider);
			if (embeddingFile.exists()) {
				logger.info("BIndex Embedding file exists: " + embeddingFile);
				List<Double> embedding = new ObjectMapper().readValue(new FileReader(embeddingFile),
						new TypeReference<List<Double>>() {
						});
				embeddingId = embeddingBuilder.bindex(bindex)
						.embedding(embedding)
						.build();
			} else {
				embeddingId = embeddingBuilder.bindex(bindex).build();
				logger.info("BIndex Embedding file: " + embeddingFile);
			}

			System.out.println("embeddingId: " + embeddingId);
		}
	}

	@Override
	public void close() throws IOException {
		embeddingProvider.close();
	}

	public static void main(String[] args) throws IOException, XmlPullParserException {
		try (Register register = new Register()) {
			register.scanProject(new File(args[0]));
		}
	}

}
