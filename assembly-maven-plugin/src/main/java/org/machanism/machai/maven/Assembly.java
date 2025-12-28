package org.machanism.machai.maven;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.machanism.machai.core.Picker;
import org.machanism.machai.core.ai.ApplicationAssembly;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.schema.BIndex;

import com.openai.models.ChatModel;

@Mojo(name = "assembly", requiresProject = false, requiresDependencyCollection = ResolutionScope.NONE)
public class Assembly extends AbstractMojo {

	@Component
	private Prompter prompter;

	@Parameter(property = "assembly.inputs.only", defaultValue = "false")
	protected boolean inputsOnly;

	@Parameter(property = "assembly.chatModel", defaultValue = "gpt-5")
	protected String chatModel;

	@Parameter(property = "pick.chatModel", defaultValue = "gpt-5-mini")
	protected String pickChatModel;

	@Parameter(property = "assembly.prompt.file", defaultValue = "project.txt")
	protected File assemblyPromptFile;

	@Parameter(property = "assembly.score", defaultValue = Picker.DEFAULT_MIN_SCORE)
	protected Double score;

	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
	protected File basedir;

	public void execute() throws MojoExecutionException {
		try {
			String query;

			if (assemblyPromptFile.exists()) {
				try (FileReader reader = new FileReader(assemblyPromptFile)) {
					query = IOUtils.toString(reader);
				}
			} else {
				query = prompter.prompt("Please enter the project assembly prompt or specify the file name");
			}

			GenAIProvider provider = new GenAIProvider(ChatModel.of(pickChatModel));
			provider.addDefaultTools();

			try (Picker picker = new Picker(provider)) {
				picker.setScore(score);
				List<BIndex> bindexList = picker.pick(query);

				if (!bindexList.isEmpty()) {
					int i = 1;
					getLog().info("Recommended libraries:");
					for (BIndex bindex : bindexList) {
						String scoreStr = picker.getScore(bindex.getId()) != null
								? picker.getScore(bindex.getId()).toString()
								: "";
						getLog().info(String.format("%2$3s. %1s %3s", bindex.getId(), i++, scoreStr));
					}

					GenAIProvider assemblyProvider = new GenAIProvider(ChatModel.of(chatModel));
					ApplicationAssembly assembly = new ApplicationAssembly(assemblyProvider);

					getLog().info("The project directory: " + basedir);
					assembly.projectDir(basedir);
					assembly.assembly(query, bindexList, !inputsOnly);

					if (!inputsOnly) {
						String prompt;
						while (!StringUtils.equalsIgnoreCase(prompt = prompter.prompt("Prompt"), "exit")) {
							assemblyProvider.prompt(prompt);
						}
					}

				}

			}
		} catch (IOException | PrompterException e) {
			throw new MojoExecutionException("The project assembly process failed.", e);
		}
	}

}
