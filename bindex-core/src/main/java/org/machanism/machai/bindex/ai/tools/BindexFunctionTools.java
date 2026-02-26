package org.machanism.machai.bindex.ai.tools;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.bindex.BindexRepository;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BindexFunctionTools implements FunctionTools {

	private static final Logger logger = LoggerFactory.getLogger(BindexFunctionTools.class);

	private BindexRepository bindexRepository;

	/**
	 * Registers web content, REST API, and Bindex function tools with the provided
	 * {@link GenAIProvider}.
	 *
	 * @param provider the provider to register tools with
	 */
	public void applyTools(GenAIProvider provider) {
		provider.addTool(
				"get_bindex",
				"Retrieves bindex metadata for a given project or library.",
				this::getBindex,
				"id:string:required:The bindex id.");

		provider.addTool(
				"get_bindex_schema",
				"Retrieves the schema definition for bindex metadata.",
				this::getBindexSchema);
	}

	/**
	 * Implementation for the get_bindex function tool.
	 *
	 * @param params tool invocation parameters
	 * @return bindex metadata as a string in the requested format
	 */
	private String getBindex(Object[] params) {
		JsonNode props = (JsonNode) params[0];
		String id = props.get("id").asText();
		Bindex bindex = bindexRepository.getBindex(id);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String bindexJson = objectMapper.writeValueAsString(bindex);
			logger.info("Bindex: {}", StringUtils.abbreviate(bindexJson, 120).replace("\n", " ").replace("\r", ""));
			return bindexJson;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Returns the schema definition for bindex metadata.
	 *
	 * @param params tool invocation parameters (not used)
	 * @return the bindex schema as a string (e.g., JSON Schema)
	 */
	private String getBindexSchema(Object[] params) {
		URL systemResource = Bindex.class.getResource(BindexBuilder.BINDEX_SCHEMA_RESOURCE);
		try {
			String schema = IOUtils.toString(systemResource, "UTF8");
			logger.info("Bindex schema: {}", StringUtils.abbreviate(schema, 120).replace("\n", " ").replace("\r", ""));
			return schema;

		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Supplies configuration for resolving header placeholders.
	 *
	 * @param configurator configurator to use (may be {@code null})
	 */
	@Override
	public void setConfigurator(Configurator configurator) {
		bindexRepository = new BindexRepository(configurator);
	}
}
