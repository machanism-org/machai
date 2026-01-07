package org.machanism.machai.bindex.builder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema. Bindex;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Builds and generates  Bindex documents from a supplied ProjectLayout using GenAI.
 * <p>
 * Supports schema prompt, project context retrieval, and AI-driven  Bindex creation.
 * <p>
 * Usage example:
 * <pre>
 *     BindexBuilder builder = new BindexBuilder(layout);
 *     builder.genAIProvider(provider);
 *      Bindex bindex = builder.build();
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see org.machanism.machai.bindex.BindexBuilderFactory
 * @see org.machanism.machai.project.layout.ProjectLayout
 * @guidance: Comprehensive Javadoc required for all public/protected members in this package.
 */
public class BindexBuilder {

    /** Temp directory path for assembly inputs. */
    public static final String BINDEX_TEMP_DIR = ".machai/bindex-inputs.txt";
    /** Path to the  Bindex schema resource file. */
    public static String BINDEX_SCHEMA_RESOURCE = "/schema/bindex-schema-v2.json";
    private static ResourceBundle promptBundle = ResourceBundle.getBundle("prompts");

    /** Optional origin  Bindex from which the builder can update. */
    private  Bindex origin;

    /** GenAIProvider for AI prompts and assembly. */
    private GenAIProvider genAIProvider;
    /** Project layout used for  Bindex generation. */
    private ProjectLayout projectLayout;

    /**
     * Constructs a BindexBuilder for the specified ProjectLayout.
     * @param projectLayout A project layout to analyze for  Bindex generation.
     */
    public BindexBuilder(ProjectLayout projectLayout) {
        this.projectLayout = projectLayout;
    }

    /**
     * Builds a new  Bindex, using possible schema prompt, origin update, and AI generation.
     * @return Newly created  Bindex object, or null on failure.
     * @throws IOException When prompt or log operations fail, or result cannot be parsed.
     */
    public  Bindex build() throws IOException {
        bindexSchemaPrompt(genAIProvider);

        if (origin != null) {
            String bindexStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(origin);
            String prompt = MessageFormat.format(promptBundle.getString("update_bindex_prompt"), bindexStr);
            genAIProvider.prompt(prompt);
        }

        projectContext();

        String prompt = promptBundle.getString("bindex_generation_prompt");
        getGenAIProvider().prompt(prompt);

        File tmpBindexDir = new File(projectLayout.getProjectDir(), BINDEX_TEMP_DIR);
        genAIProvider.inputsLog(tmpBindexDir);
        String output = genAIProvider.perform();

         Bindex value = null;
        if (output != null) {
            value = new ObjectMapper().readValue(output,  Bindex.class);
        }
        return value;
    }

    /**
     * Defines project context if needed for the assembly (to be overridden).
     * @throws IOException if project context cannot be established.
     */
    protected void projectContext() throws IOException {
    }

    /**
     * Issues a schema prompt to GenAIProvider for  Bindex schema setup.
     * @param provider GenAIProvider to receive schema instructions.
     * @throws IOException On prompt or IO errors.
     */
    public static void bindexSchemaPrompt(GenAIProvider provider) throws IOException {
        URL systemResource =  Bindex.class.getResource(BINDEX_SCHEMA_RESOURCE);
        String schema = IOUtils.toString(systemResource, "UTF8");
        String prompt = MessageFormat.format(promptBundle.getString("bindex_schema_section"), schema);
        provider.prompt(prompt);
    }

    /**
     * Sets the origin  Bindex for update operations.
     * @param bindex Origin  Bindex instance for updates.
     * @return This BindexBuilder instance for chain usage.
     */
    public BindexBuilder origin( Bindex bindex) {
        this.origin = bindex;
        return this;
    }

    /**
     * Gets the origin  Bindex instance.
     * @return The currently set origin  Bindex (may be null).
     */
    public  Bindex getOrigin() {
        return origin;
    }

    /**
     * Returns the GenAIProvider for building.
     * @return GenAIProvider used for prompts.
     */
    public GenAIProvider getGenAIProvider() {
        return genAIProvider;
    }

    /**
     * Sets the GenAIProvider for this builder.
     * @param genAIProvider Provider instance.
     * @return This BindexBuilder for chain calls.
     */
    public BindexBuilder genAIProvider(GenAIProvider genAIProvider) {
        this.genAIProvider = genAIProvider;
        String systemPrompt = promptBundle.getString("bindex_system_instructions");
        this.genAIProvider.instructions(systemPrompt);
        return this;
    }

    /**
     * Returns the ProjectLayout for this builder.
     * @return ProjectLayout used for analysis.
     */
    public ProjectLayout getProjectLayout() {
        return projectLayout;
    }
}
