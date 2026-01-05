package org.machanism.machai.bindex.builder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.BIndex;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BindexBuilder {

	public static final String BINDEX_TEMP_DIR = ".machai/bindex-inputs.txt";
	public static String BINDEX_SCHEMA_RESOURCE = "/schema/bindex-schema-v2.json";
	private static ResourceBundle promptBundle = ResourceBundle.getBundle("prompts");

	private BIndex origin;

	private GenAIProvider genAIProvider;
	private ProjectLayout projectLayout;

	public BindexBuilder(ProjectLayout projectLayout) {
		this.projectLayout = projectLayout;
	}

	public BIndex build(boolean callLLM) throws IOException {
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

		BIndex value = null;
		if (output != null) {
			value = new ObjectMapper().readValue(output, BIndex.class);
		}
		return value;
	}

	protected void projectContext() throws IOException {
	}

	public static void bindexSchemaPrompt(GenAIProvider provider) throws IOException {
		URL systemResource = BIndex.class.getResource(BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, "UTF8");
		String prompt = MessageFormat.format(promptBundle.getString("bindex_schema_section"), schema);
		provider.prompt(prompt);
	}

	public BindexBuilder origin(BIndex bindex) {
		this.origin = bindex;
		return this;
	}

	public BIndex getOrigin() {
		return origin;
	}

	public GenAIProvider getGenAIProvider() {
		return genAIProvider;
	}

	public BindexBuilder genAIProvider(GenAIProvider genAIProvider) {
		this.genAIProvider = genAIProvider;
		String systemPrompt = promptBundle.getString("bindex_system_instructions");
		this.genAIProvider.instructions(systemPrompt);
		return this;
	}

	public ProjectLayout getProjectLayout() {
		return projectLayout;
	}
}
