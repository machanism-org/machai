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
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.ai.manager.SystemFunctionTools;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.BIndex;

/**
 * Mojo for assembling Maven projects using AI-powered library recommendations and code generation.
 * <p>
 * This mojo provides an interactive prompt and automated selection of dependencies using a prompt
 * and AI provider. It can recommend libraries and assemble the project structure based on user input or
 * prompt files. See usage example below for typical invocation.
 * </p>
 *
 * <pre>
 * mvn org.machanism.machai.maven:assembly -Dassembly.inputs.only=false
 * </pre>
 *
 * <h2>Parameters</h2>
 * <ul>
 *   <li>assembly.inputs.only: Only input prompt (boolean)</li>
 *   <li>assembly.chatModel: Chat model for assembly (String)</li>
 *   <li>pick.chatModel: Chat model for library picking (String)</li>
 *   <li>assembly.prompt.file: Prompt file name (File)</li>
 *   <li>assembly.score: Score threshold for selection (Double)</li>
 *   <li>basedir: Project directory (File)</li>
 * </ul>
 *
 * @guidance
 */
@Mojo(name = "assembly", requiresProject = false, requiresDependencyCollection = ResolutionScope.NONE)
public class Assembly extends AbstractMojo {

    /** Prompter instance for interactive command-line input. */
    @Component
    protected Prompter prompter;

    /**
     * Indicates if only the prompt input should be processed and actual assembly should be skipped.
     */
    @Parameter(property = "assembly.inputs.only", defaultValue = "false")
    protected boolean inputsOnly;

    /**
     * Specifies the AI chat model to use for assembly-related tasks.
     */
    @Parameter(property = "assembly.chatModel", defaultValue = "gpt-5")
    protected String chatModel;

    /**
     * Specifies the AI chat model to use when recommending/picking libraries.
     */
    @Parameter(property = "pick.chatModel", defaultValue = "OpenAI:gpt-5-mini")
    protected String pickChatModel;

    /**
     * File containing the prompt for the assembly process.
     */
    @Parameter(property = "assembly.prompt.file", defaultValue = "project.txt")
    protected File assemblyPromptFile;

    /**
     * Score threshold for recommended libraries; only libraries meeting the minimum score will be offered.
     */
    @Parameter(property = "assembly.score", defaultValue = Picker.DEFAULT_MIN_SCORE)
    protected Double score;

    /**
     * Maven project base directory.
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;

    /**
     * Executes the assembly process.
     * <p>
     * Attempts to load a prompt from the file (if it exists), then uses the Picker to recommend libraries
     * and applies the AI-powered system functions to generate or update project content.
     * If inputsOnly is false, prompts the user in a loop for API interaction until they enter "exit".
     * </p>
     *
     * <h3>Parameters</h3>
     * <ul>
     *   <li>{@code assemblyPromptFile} - The prompt file for assembly</li>
     *   <li>{@code score} - Score threshold for selection</li>
     *   <li>{@code pickChatModel} - Model used for library picking</li>
     *   <li>{@code chatModel} - Model used for assembly tasks</li>
     *   <li>{@code basedir} - Project directory</li>
     *   <li>{@code prompter} - Interactive prompt command line tool</li>
     *   <li>{@code inputsOnly} - Process only prompt or assemble project</li>
     * </ul>
     *
     * @return void
     * @throws MojoExecutionException if the assembly or prompt interaction fails
     * @see Picker
     * @see ApplicationAssembly
     * @see GenAIProviderManager
     *
     * <h3>Usage Example</h3>
     * <pre>{@code
     * mvn org.machanism.machai.maven:assembly -Dassembly.inputs.only=false
     * }</pre>
     */
    @Override
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

            GenAIProvider provider = GenAIProviderManager.getProvider(pickChatModel);
            new SystemFunctionTools(basedir).applyTools(provider);

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

                    GenAIProvider assemblyProvider = GenAIProviderManager.getProvider(chatModel);
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
