package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.schema.BIndex;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BIndexBuilder {
	public static final String BINDEX_TEMP_DIR = ".bindex";
	public static final String BINDEX_SCHEMA_RESOURCE = "/schema/bindex-schema-v2.json";

	public static final String[] STARTS_WITH_EXCLUDE_DIRS = { "node_modules", ".", "_" };

	private static ResourceBundle promptBundle = ResourceBundle.getBundle("prompts");

	private File projectDir;
	private GenAIProvider provider;
	private ObjectMapper mapper = new ObjectMapper();
	private File bindexDir;
	private BIndex source;

	public BIndexBuilder provider(GenAIProvider provider) {
		this.provider = provider;
		String systemPrompt = promptBundle.getString("bindex_system_instructions");
		provider.instructions(systemPrompt);
		return this;
	}

	public BIndex build() throws IOException {
		bindexSchemaPrompt(getProvider());

		if (source != null) {
			String bindexStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(source);
			String prompt = MessageFormat.format(promptBundle.getString("update_bindex_prompt"), bindexStr);
			getProvider().prompt(prompt);
		}

		projectContext();

		String prompt = promptBundle.getString("bindex_generation_prompt");
		getProvider().prompt(prompt);

		if (bindexDir != null) {
			File dir = new File(getProjectDir(), BINDEX_TEMP_DIR);
			getProvider().saveInput(dir);
		}

		String output = getProvider().perform();

		BIndex value = null;
		if (output != null) {
			value = mapper.readValue(output, BIndex.class);
		}
		return value;
	}

	protected abstract void projectContext() throws IOException;

	public static void bindexSchemaPrompt(GenAIProvider provider) throws IOException {
		URL systemResource = BIndex.class.getResource(BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, "UTF8");
		String prompt = MessageFormat.format(promptBundle.getString("bindex_schema_section"), schema);
		provider.prompt(prompt);
	}

	public BIndexBuilder projectDir(File projectDir) {
		this.projectDir = projectDir;
		return this;
	}

	public BIndexBuilder bindexDir(File bindexDir) {
		this.bindexDir = bindexDir;
		return this;
	}

	public File getProjectDir() {
		return projectDir;
	}

	protected GenAIProvider getProvider() {
		return provider;
	}

	public List<String> getModules() throws IOException {
		return null;
	};

	public String getRelatedPath(String currentPath, File file) {
		String relativePath = file.getAbsolutePath().replace("\\", "/").replace(currentPath, "");
		if (StringUtils.startsWith(relativePath, "/")) {
			relativePath = StringUtils.substring(relativePath, 1);
		}
		return relativePath;
	}

	public BIndexBuilder source(BIndex bindex) {
		this.source = bindex;
		return this;
	}

}
