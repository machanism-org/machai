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
 * Maven plugin Mojo that implements the {@code assembly} goal.
 *
 * <p>
 * The goal drives an AI-assisted project assembly workflow:
 * </p>
 * <ul>
 *   <li>Acquires an assembly prompt (from a configured file or interactively).</li>
 *   <li>Uses a configured generative-AI provider to recommend candidate libraries.</li>
 *   <li>Optionally filters recommendations by a minimum score threshold.</li>
 *   <li>Invokes the project assembly workflow in the Maven base directory.</li>
 * </ul>
 *
 * <p><b>Command-line example:</b></p>
 * <pre>
 * mvn org.machanism.machai:assembly-maven-plugin:assembly \
 *   -Dassembly.genai=OpenAI:gpt-5 \
 *   -Dpick.genai=OpenAI:gpt-5-mini
 * </pre>
 */
@Mojo(name = "assembly", requiresProject = false, requiresDependencyCollection = ResolutionScope.NONE)
public class Assembly extends AbstractMojo {

    /** Prompter used to request interactive input when a prompt file is not present. */
    @Component
    protected Prompter prompter;

    /** GenAI provider identifier used for the assembly workflow (e.g., {@code OpenAI:gpt-5}). */
    @Parameter(property = "assembly.genai", defaultValue = "OpenAI:gpt-5")
    protected String chatModel;

    /** GenAI provider identifier used for library recommendation/picking (e.g., {@code OpenAI:gpt-5-mini}). */
    @Parameter(property = "pick.genai", defaultValue = "OpenAI:gpt-5-mini")
    protected String pickChatModel;

    /** Prompt file for the assembly workflow. If it exists, it is read and used as the prompt. */
    @Parameter(property = "assembly.prompt.file", defaultValue = "project.txt")
    protected File assemblyPromptFile;

    /** Minimum score threshold; only libraries meeting this score will be offered. */
    @Parameter(property = "assembly.score", defaultValue = "0.9")
    protected Double score;

    /** Optional registration URL used by the picker for metadata lookups/registration. */
    @Parameter(property = "bindex.register.url")
    protected String registerUrl;

    /** Maven project base directory where changes are applied. */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;

    /**
     * Executes the {@code assembly} goal.
     *
     * @throws MojoExecutionException if prompt acquisition or the assembly workflow fails
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
