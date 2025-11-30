package org.machanism.machai.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.bindex.BIndexBuilder;
import org.machanism.machai.core.bindex.EffectivePomReader;
import org.machanism.machai.core.bindex.EmbeddingBuilder;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.openai.models.ChatModel;

public class Register {

	private static Logger logger = LoggerFactory.getLogger(Register.class);

	private GenAIProvider provider;

	public Register() {
		provider = new GenAIProvider(ChatModel.GPT_5);
		provider.setDebugMode(true);
		provider.setRequestDisable(true);
	}

	public void scanProject(File projectDir) throws IOException, XmlPullParserException {
		File pomFile = new File(projectDir, "pom.xml");

		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model = reader.read(new FileReader(pomFile));

		if ("pom".equals(model.getPackaging())) {
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

		BIndex bindex = new BIndexBuilder()
				.projectDir(projectDir)
				.provider(provider)
				.build();

		String bindexStr = new ObjectMapper().writeValueAsString(bindex);
		logger.info("BIndex: " + bindexStr);

		List<Float> embeddingBuilder = new EmbeddingBuilder().provider(provider).bindex(bindex).build();
		logger.info("Embedding: " + String.valueOf(embeddingBuilder));
	}

	public static void main(String[] args) throws IOException, XmlPullParserException {
		Register register = new Register();
		register.scanProject(new File("D:\\projects\\machanism.org\\macha\\core"));
	}

}
