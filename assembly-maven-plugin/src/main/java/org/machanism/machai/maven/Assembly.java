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
import org.machanism.machai.ai.provider.none.NoneProvider;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;

/**
 * <p>
 * Implements the <code>assembly</code> goal for AI-driven project setup and intelligent dependency selection.
 * This Maven Mojo interacts with generative AI models to automate and streamline
 * the process of assembling a Java project. It parses prompts, recommends libraries,
 * and uses score-based filtering for proposed dependencies.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * mvn org.machanism.machai:assembly-maven-plugin:assembly -Dassembly.genai=OpenAI:gpt-5 -Dpick.genai=OpenAI:gpt-5-mini
 * </pre>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>assembly.genai</b>: Chat model for assembly-related tasks. Default: "OpenAI:gpt-5"</li>
 *   <li><b>pick.genai</b>: Chat model for library picking recommendations. Default: "OpenAI:gpt-5-mini"</li>
 *   <li><b>assembly.prompt.file</b>: Prompt file for the assembly process. Default: "project.txt"</li>
 *   <li><b>assembly.score</b>: Minimum score for recommended libraries.</li>
 *   <li><b>bindex.register.url</b>: Registration database URL for project metadata.</li>
 * </ul>
 *
 * <p><b>Behavior:</b></p>
 * <ul>
 *   <li>Reads assembly prompt from a file or interactively via command-line.</li>
 *   <li>Uses the specified GenAI provider to process prompts and recommend libraries.</li>
 *   <li>Filters recommendations based on score and assembles the application in the project directory.</li>
 *   <li>Supports interactive prompting for further configuration until "exit" is entered.</li>
 * </ul>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>Throws MojoExecutionException if the assembly process fails due to I/O or prompt errors.</li>
 * </ul>
 *
 */
@Mojo(name = "assembly", requiresProject = false, requiresDependencyCollection = ResolutionScope.NONE)
public class Assembly extends AbstractMojo {

    /** Prompter instance for interactive command-line input. */
    @Component
    protected Prompter prompter;

    /**
     * Specifies the AI chat model to use for assembly-related tasks.
     * Default: "OpenAI:gpt-5".
     */
    @Parameter(property = "assembly.genai", defaultValue = "OpenAI:gpt-5")
    protected String chatModel;

    /**
     * Specifies the AI chat model to use when recommending/picking libraries.
     * Default: "OpenAI:gpt-5-mini".
     */
    @Parameter(property = "pick.genai", defaultValue = "OpenAI:gpt-5-mini")
    protected String pickChatModel;

    /**
     * File containing the prompt for the assembly process.
     * Default: "project.txt".
     */
    @Parameter(property = "assembly.prompt.file", defaultValue = "project.txt")
    protected File assemblyPromptFile;

    /**
     * Score threshold for recommended libraries; only libraries meeting the minimum
     * score will be offered.
     */
    @Parameter(property = "assembly.score", defaultValue = "0.9")
    protected Double score;

    /**
     * URL of the registration database for storing project metadata.
     */
    @Parameter(property = "bindex.register.url")
    protected String registerUrl;

    /**
     * Maven project base directory.
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;

    /**
     * Executes the assembly Mojo, handling prompt acquisition, library recommendation,
     * and project assembly using AI models.
     *
     * @throws MojoExecutionException if file I/O or prompt acquisition fails
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
            provider.setWorkingDir(basedir);
            new SystemFunctionTools().applyTools(provider);

            try (Picker picker = new Picker(provider, registerUrl)) {
                picker.setScore(score);
                List<Bindex> bindexList = picker.pick(query);

                if (!bindexList.isEmpty()) {
                    int i = 1;
                    getLog().info("Recommended libraries:");
                    for (Bindex bindex : bindexList) {
                        String scoreStr = picker.getScore(bindex.getId()) != null
                                ? picker.getScore(bindex.getId()).toString()
                                : "";
                        getLog().info(String.format("%2$3s. %1s %3s", bindex.getId(), i++, scoreStr));
                    }

                    GenAIProvider assemblyProvider = GenAIProviderManager.getProvider(chatModel);
                    ApplicationAssembly assembly = new ApplicationAssembly(assemblyProvider);

                    getLog().info("The project directory: " + basedir);
                    assembly.projectDir(basedir);
                    assembly.assembly(query, bindexList);

                    if (!(assemblyProvider instanceof NoneProvider)) {
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
