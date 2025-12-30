package org.machanism.machai.bindex.builder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JScriptBindexBuilder extends BindexBuilder {

	public JScriptBindexBuilder(ProjectLayout projectLayout) {
		super(projectLayout);
	}

	private static Logger logger = LoggerFactory.getLogger(JScriptBindexBuilder.class);
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("js_project_prompts");

	@Override
	public void projectContext() throws IOException {

		File packageFile = new File(getProjectLayout().getProjectDir(), JScriptProjectLayout.PROJECT_MODEL_FILE_NAME);
		try (FileReader reader = new FileReader(packageFile)) {
			String prompt = MessageFormat.format(promptBundle.getString("js_resource_section"),
					IOUtils.toString(reader));
			getGenAIProvider().prompt(prompt);
		}

		Path startPath = Paths.get(new File(getProjectLayout().getProjectDir(), "src").getAbsolutePath());

		if (Files.exists(startPath)) {
			Files.walk(startPath).filter(f -> FilenameUtils.isExtension(f.toFile().getName(), "ts", "vue", "js"))
					.forEach((f) -> {
						try {
							getGenAIProvider().promptFile(f.toFile(), "source_resource_section");
						} catch (IOException e) {
							logger.warn("File: {P} adding failed.", f);
						}
					});
		}

		String prompt = promptBundle.getString("additional_rules");
		getGenAIProvider().prompt(prompt);
	}

}
