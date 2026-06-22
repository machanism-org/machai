package org.machanism.machai.bindex;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Language;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Performs Bindex registration, lookup, and semantic retrieval using embeddings
 * and MongoDB.
 */
public class Picker {

	private static final String CLASSIFICATION_INSTRUCTION_PROP_NAME = "picker.classificationInstruction";
	private static final String DEFAULT_CLASSIFICATION_INSTRUCTION = "You are a system architect and must generate a\n"
			+ "JSON object with a classification having the following schema:**\n\n"
			+ "```json\n%s\n```\n\n"
			+ "You must analyze the user's request below and provide a JSON array with separate classifications for all **required levels**\n"
			+ "to find libraries that meet these requirements to build the application requested by the user.\n\n"
			+ "**User Request:**\n\n%s";

	private static final String MODEL_PROP_NAME = "pick.model";

	private final Map<String, Double> scoreMap = new HashMap<>();

	private BindexRepository bindexRepository;

	public static final String BINDEX_SCHEMA_RESOURCE = "/schema/bindex-schema-v2.json";

	/**
	 * Creates a picker backed by the configured Bindex repository and a named GenAI
	 * provider.
	 *
	 * @param config the project configuration used for repository and prompt
	 *               settings
	 */
	public Picker(Configurator configurator) {
		bindexRepository = new MongoBindexRepository(configurator);
	}

	/**
	 * Builds a classification prompt from the query and executes it through the
	 * configured provider.
	 *
	 * @param query        the natural-language request to classify
	 * @param configurator
	 * @return the raw provider response containing classification JSON
	 * @throws IOException if the schema resource cannot be loaded or parsed
	 */
	private String getClassification(String query, Configurator configurator) throws IOException {
		URL systemResource = Bindex.class.getResource(BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, StandardCharsets.UTF_8);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode schemaJson = objectMapper.readTree(schema);
		JsonNode jsonNode = schemaJson.get("properties").get("classification");
		String classificationSchema = objectMapper.writeValueAsString(jsonNode);

		String instructionTemplate = configurator.get(CLASSIFICATION_INSTRUCTION_PROP_NAME,
				DEFAULT_CLASSIFICATION_INSTRUCTION);
		String classificationQuery = String.format(instructionTemplate, classificationSchema, query);

		String genai = configurator.get(Picker.MODEL_PROP_NAME, configurator.get("gw.model"));
		if (genai == null) {
			throw new IllegalArgumentException("genai must not be null");
		}
		Genai provider = GenaiProviderManager.getProvider(genai, configurator);

		provider.prompt(classificationQuery);
		return provider.perform();
	}

	public List<Bindex> pick(String prompt, Double score, Configurator configurator) throws IOException {
		String classificationStr = getClassification(prompt, configurator);
		return bindexRepository.find(classificationStr, score, configurator);
	}

	/**
	 * Recursively adds the dependencies of the given Bindex to the provided set.
	 *
	 * @param dependencies the target set that accumulates dependency identifiers
	 * @param bindexId     the root Bindex identifier whose dependency graph is
	 *                     traversed
	 */
	void addDependencies(Set<String> dependencies, String bindexId) {
		Bindex bindex = bindexRepository.getBindex(bindexId);
		if (bindex != null) {
			String id = bindex.getId();
			if (!dependencies.contains(id)) {
				dependencies.add(id);
				for (String dependencyId : bindex.getDependencies()) {
					addDependencies(dependencies, dependencyId);
				}
			}
		}
	}

	/**
	 * Normalizes a language name for repository matching.
	 *
	 * @param language the language to normalize
	 * @return the normalized lowercase language name without any parenthetical
	 *         suffix
	 */
	static String getNormalizedLanguageName(Language language) {
		String lang = language.getName().toLowerCase().trim();
		return StringUtils.substringBefore(lang, "(").trim();
	}

	/**
	 * Returns the last recorded score for a Bindex identifier.
	 *
	 * @param id the Bindex identifier
	 * @return the recorded score, or {@code null} if none is available
	 */
	public Double getScore(String id) {
		return scoreMap.get(id);
	}

	public Bindex getBindex(String id) {
		return bindexRepository.getBindex(id);
	}

	public String save(Bindex bindex) {
		return bindexRepository.save(bindex);
	}
}
