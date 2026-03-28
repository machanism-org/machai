package org.machanism.machai.bindex.ai.tools;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.bindex.BindexRepository;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Registers Bindex-related function tools for a {@link Genai}.
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

	private final Logger logger = LoggerFactory.getLogger(BindexFunctionTools.class);

	private static final int MAXWIDTH = 160;

	private BindexRepository bindexRepository;

	private Configurator configurator;

	/**
	 * Registers Bindex function tools with the provided {@link Genai}.
	 *
	 * @param provider the provider to register tools with
	 */
	public void applyTools(Genai provider) {
		provider.addTool(
				"get_bindex",
				"Retrieves bindex metadata for a given project or library.",
				this::getBindex,
				"id:string:required:The bindex id.");

		provider.addTool(
				"get_bindex_schema",
				"Retrieves the schema definition for bindex metadata.",
				this::getBindexSchema);

		provider.addTool(
				"pick_libraries",
				"Recommends libraries based on the user's prompt or project requirements.",
				this::getRecommendedLibraries,
				"prompt:string:required:The user prompt describing project needs or requirements.");
	}

	/**
	 * Implementation for the {@code get_bindex} function tool.
	 *
	 * @param params tool invocation parameters; the first element is expected to be
	 *               a JSON node containing the tool arguments
	 * @return the serialized {@link Bindex} as JSON, or the literal {@code null}
	 *         when not found
	 * @throws JsonProcessingException
	 * @throws IllegalStateException   if the repository has not been configured yet
	 */
	private String getBindex(Object[] params) throws JsonProcessingException {
		JsonNode props = (JsonNode) params[0];
		String id = props.get("id").asText();
		Bindex bindex = getBindexRepository().getBindex(id);
		ObjectMapper objectMapper = new ObjectMapper();
		String bindexJson = bindex == null ? "null" : objectMapper.writeValueAsString(bindex);
		if (logger.isInfoEnabled()) {
			logger.info("Retrieved bindex: {}",
					StringUtils.abbreviate(bindexJson, 120).replace(Genai.LINE_SEPARATOR, " ").replace("\r",
							""));
		}
		return bindexJson;
	}

	private BindexRepository getBindexRepository() {
		if (bindexRepository == null) {
			bindexRepository = new BindexRepository(configurator);
		}
		return bindexRepository;
	}

	/**
	 * Implementation for the {@code get_bindex_schema} function tool.
	 *
	 * @param params tool invocation parameters (not used)
	 * @return the Bindex schema resource content as JSON string
	 * @throws IOException
	 */
	private String getBindexSchema(Object[] params) throws IOException {
		URL systemResource = Bindex.class.getResource(BindexBuilder.BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, StandardCharsets.UTF_8);
		if (logger.isDebugEnabled()) {
			logger.debug("Bindex schema: {}",
					StringUtils.abbreviate(schema, 120).replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""));
		}
		return schema;
	}

	private String getRecommendedLibraries(Object[] params) throws IOException {
		JsonNode props = (JsonNode) params[0];
		if (logger.isInfoEnabled()) {
			logger.info("Picking for: {}", StringUtils.abbreviate(String.valueOf(params[0]), MAXWIDTH));
		}
		logger.debug("Picking for: {}", params[0]);

		String prompt = props.get("prompt").asText();

		String model = configurator.get(Picker.MODEL_PROP_NAME, Picker.DEFAULT_MODEL);
		Double score = configurator.getDouble("score", Picker.DEFAULT_SCORE_VALUE);
		String registerUrl = configurator.get("BINDEX_REPO_URL", null);

		Picker picker = new Picker(model, registerUrl, configurator);
		picker.setScore(score);

		List<Bindex> bindexList = picker.pick(prompt);

		StringBuilder picked = new StringBuilder("```md\n# Recommended Artifacts\n"
				+ "\n"
				+ "| Library Id | Description |\n"
				+ "|------------|-------------|\n"
				+ "");
		for (Bindex bindex : bindexList) {
			if (bindex != null) {
				picked.append("| ").append(bindex.getId()).append(" | ").append(bindex.getDescription()).append(" |")
						.append(Genai.LINE_SEPARATOR);
			}
		}
		picked.append("```\n");

		String result = picked.toString();
		if (logger.isInfoEnabled()) {
			logger.info("Recommended Artifacts: \n{}", StringUtils.abbreviate(result, MAXWIDTH));
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Recommended Artifacts: \n{}", result);
		}
		return result;
	}

	/**
	 * Supplies configuration used to initialize the underlying
	 * {@link BindexRepository}.
	 *
	 * @param configurator configurator to use (may be {@code null})
	 */
	@Override
	public void setConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}
}
