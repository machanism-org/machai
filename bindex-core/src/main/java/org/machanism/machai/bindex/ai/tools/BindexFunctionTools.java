package org.machanism.machai.bindex.ai.tools;

import java.util.Arrays;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.bindex.BindexRepository;
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
				"artifactId:string:required:The path to the project or library for which to retrieve bindex metadata.");
	}

	/**
	 * Implementation for the get_bindex function tool.
	 *
	 * @param params tool invocation parameters
	 * @return bindex metadata as a string in the requested format
	 */
	private String getBindex(Object[] params) {
		logger.info("Get Bindex: {}", Arrays.toString(params));
		JsonNode props = (JsonNode) params[0];
		String artifactId = props.get("artifactId").asText();
		Bindex bindex = bindexRepository.getBindex(artifactId);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String bindexJson = objectMapper.writeValueAsString(bindex);
			return bindexJson;
		} catch (JsonProcessingException e) {
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
