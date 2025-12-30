package org.machanism.machai.documents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

import com.openai.models.ChatModel;

public class DocsProcessor extends ProjectProcessor {
	public String[] GUIDANCE_FILE_NAMES = { "guidance.txt", "package-info.java" };

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	public String loadGuidanceFileContent(File file) throws IOException {
		for (String fileName : GUIDANCE_FILE_NAMES) {
			Path path = Paths.get(fileName);
			if (Files.exists(path)) {
				return Files.readString(path);
			}
		}
		return null;
	}

	@Override
	public void processProject(ProjectLayout projectLayout) {

		File projectDir = projectLayout.getProjectDir();

		List<String> sources = projectLayout.getSources();
		List<String> documents = projectLayout.getDocuments();
		List<String> tests = projectLayout.getTests();

		GenAIProvider provider = new GenAIProvider(ChatModel.GPT_5_1_MINI);
		provider.promptBundle(promptBundle);
		provider.addDefaultTools();

		System.out.println();

	}

	public static void main(String[] args) throws IOException {
		DocsProcessor documents = new DocsProcessor();
		documents.scanProjects(new File("D:\\projects\\machanism.org\\macha\\core\\commons\\configurator"));
	}

}
