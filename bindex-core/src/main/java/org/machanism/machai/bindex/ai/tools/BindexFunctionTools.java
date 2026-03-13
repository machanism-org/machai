package org.machanism.machai.bindex.ai.tools;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

/**
 * Registers Bindex-related function tools for a {@link GenAIProvider}.
 *
 * <p>
 * The tools exposed by this type are intended to be consumed by LLM-assisted
 * workflows so they can retrieve additional context (a Bindex document or the
 * Bindex JSON schema) on demand.
 *
 * <h2>Exposed tools</h2>
 * <ul>
 * <li>{@code get_bindex}: Fetches a registered {@link Bindex} by its id.</li>
 * <li>{@code get_bindex_schema}: Returns the JSON schema that defines the
 * {@link Bindex} document shape.</li>
 * </ul>
 *
 * <p>
 * A {@link BindexRepository} is created when
 * {@link #setConfigurator(Configurator)} is invoked.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class BindexFunctionTools implements FunctionTools {

	private static final Logger logger = LoggerFactory.getLogger(BindexFunctionTools.class);

	private BindexRepository bindexRepository;

	/**
	 * Registers Bindex function tools with the provided {@link GenAIProvider}.
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
	 * Implementation for the {@code get_bindex} function tool.
	 *
	 * @param params tool invocation parameters; the first element is expected to be
	 *               a JSON node containing the tool arguments
	 * @return the serialized {@link Bindex} as JSON, or {@code null} if not found
	 * @throws JsonProcessingException
	 * @throws IllegalStateException   if the repository has not been configured yet
	 */
	private String getBindex(Object[] params) throws JsonProcessingException {
		if (bindexRepository == null) {
			throw new IllegalStateException("BindexRepository is not initialized. Call setConfigurator(...) first.");
		}
		JsonNode props = (JsonNode) params[0];
		String id = props.get("id").asText();
		Bindex bindex = bindexRepository.getBindex(id);
		ObjectMapper objectMapper = new ObjectMapper();
		String bindexJson = objectMapper.writeValueAsString(bindex);
		// Sonar java:S2629 - avoid eager abbreviate/replace when INFO is disabled.
		if (logger.isInfoEnabled()) {
			logger.info("Bindex: {}", StringUtils.abbreviate(bindexJson, 120).replace("\n", " ").replace("\r", ""));
		}
		return bindexJson;
	}

	/**
	 * Implementation for the {@code get_bindex_schema} function tool.
	 *
	 * @param params tool invocation parameters (not used)
	 * @return the Bindex schema resource content as JSON string
	 * @throws IOException 
	 */
	private String getBindexSchema(Object[] params) throws IOException {
		URL systemResource = Bindex.class.getResource(BindexBuilder.BINDEX_SCHEMA_RESOURCE_PATH);
		// Sonar java:S4719 - use StandardCharsets.UTF_8 instead of charset name.
		String schema = IOUtils.toString(systemResource, StandardCharsets.UTF_8);
		// Sonar java:S2629 - avoid eager replace/abbreviate work when INFO is disabled.
		if (logger.isInfoEnabled()) {
			logger.info("Bindex schema: {}",
					StringUtils.abbreviate(schema, 120).replace("\n", " ").replace("\r", ""));
		}
		return schema;
	}

	/**
	 * Supplies configuration used to initialize the underlying
	 * {@link BindexRepository}.
	 *
	 * @param configurator configurator to use (may be {@code null})
	 */
	@Override
	public void setConfigurator(Configurator configurator) {
		bindexRepository = new BindexRepository(configurator);
	}
}
