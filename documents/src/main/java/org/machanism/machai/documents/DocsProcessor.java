package org.machanism.machai.documents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.project.ProjectProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

public class DocsProcessor extends ProjectProcessor {
	public String[] GUIDANCE_FILE_NAMES = { "guidance.txt", "package-info.java" };

	private ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");
	private String chatModel = "OpenAI:gpt-5-mini";

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

		GenAIProvider provider = GenAIProviderManager.getProvider(chatModel);
		provider.promptBundle(promptBundle);
		new SystemFunctionTools(projectDir).applyTools(provider);

		String prompt = "";
		provider.prompt(prompt);
		provider.perform(false);

		System.out.println();

	}

	public static void main(String[] args) throws IOException {
		DocsProcessor documents = new DocsProcessor();
		documents.scanProjects(SystemUtils.getUserDir());
	}

}
