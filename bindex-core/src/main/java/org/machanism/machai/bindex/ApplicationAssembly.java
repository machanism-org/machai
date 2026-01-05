package org.machanism.machai.bindex;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.SystemUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles assembly operations for BIndex projects using GenAIProvider and prompts.
 * 
 * <p>Typical usage example:
 * <pre>
 *     ApplicationAssembly assembly = new ApplicationAssembly(provider);
 *     assembly.assembly(prompt, bindexList, true);
 * </pre>
 *
 * @author machanism.org
 * @since 1.0
 */
public class ApplicationAssembly {

    /** Logger instance for the class. */
    private static Logger logger = LoggerFactory.getLogger(ApplicationAssembly.class);
    /** ResourceBundle for prompt configuration. */
    private static ResourceBundle promptBundle = ResourceBundle.getBundle("prompts");

    /** Path to temp directory for assembly. */
    private static final String ASSEMBLY_TEMP_DIR = ".machai/assembly-inputs.txt";

    private GenAIProvider provider;
    private File projectDir = SystemUtils.getUserDir();

    /**
     * Constructs an ApplicationAssembly using the specified GenAIProvider.
     * @param provider The GenAIProvider to use for prompt processing.
     */
    public ApplicationAssembly(GenAIProvider provider) {
        super();
        this.provider = provider;
    }

    /**
     * Assemble a BIndex project based on given prompts and instructions.
     * @param prompt The assembly prompt or command.
     * @param bindexList List of BIndex objects involved in the assembly.
     * @param callLLM Whether to call LLM for assembly execution.
     * @throws IllegalArgumentException If an I/O error occurs during assembly.
     */
    public void assembly(final String prompt, List<BIndex> bindexList) {
        String systemPrompt = promptBundle.getString("assembly_system_instructions");
        provider.instructions(systemPrompt);

        String assemblyInstructions = promptBundle.getString("assembly_instructions");
        provider.prompt(assemblyInstructions);

        try {
            BindexBuilder.bindexSchemaPrompt(provider);

            for (BIndex bindex : bindexList) {
                String bindexStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(bindex);
                String bindexPrompt = MessageFormat.format(promptBundle.getString("recommended_library_section"),
                        bindex.getId(), bindexStr);
                provider.prompt(bindexPrompt);
            }

            provider.prompt(prompt);

            File bindexTempDir = new File(projectDir, ASSEMBLY_TEMP_DIR);
            provider.inputsLog(bindexTempDir);
            String response = provider.perform();
            if (response != null) {
                logger.info(response);
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Specifies the project directory for assembly operations.
     * @param projectDir The new project directory.
     * @return this ApplicationAssembly instance.
     */
    public ApplicationAssembly projectDir(File projectDir) {
        this.projectDir = projectDir;
        return this;
    }

}
