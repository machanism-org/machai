package org.machanism.machai.bindex.core;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.EmbeddingProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Performs Bindex registration, lookup, and semantic retrieval using embeddings
 * and MongoDB.
 * <p>
 * The Picker class provides methods for:
 * <ul>
 * <li>Recommending libraries (Bindex entries) based on a natural language
 * prompt and score threshold</li>
 * <li>Registering and retrieving Bindex entries</li>
 * <li>Building and executing classification prompts using a GenAI provider</li>
 * <li>Recursively resolving dependencies for a Bindex entry</li>
 * <li>Normalizing language names for repository matching</li>
 * </ul>
 *
 */
public class Picker {

	private final Logger logger = LoggerFactory.getLogger(Picker.class);

	/**
	 * Classpath resource path for the Bindex JSON schema.
	 */
	public static final String BINDEX_SCHEMA_RESOURCE = "/schema/bindex-schema-v2.json";
	private static final String CLASSIFICATION_INSTRUCTION_PROP_NAME = "picker.classificationInstruction";
	private static final String DEFAULT_CLASSIFICATION_INSTRUCTION = "You are a system architect and must generate a\n"
			+ "JSON object with a classification having the following schema:**\n\n"
			+ "```json\n%s\n```\n\n"
			+ "You must analyze the user's request below and provide a JSON array with separate classifications for all **required levels**\n"
			+ "to find libraries that meet these requirements to build the application requested by the user.\n\n"
			+ "**User Request:**\n\n%s";

	private static final String MODEL_PROP_NAME = "pick.model";

	private Configurator configurator;
	private BindexRepository bindexRepository;
	private int dimensions = 700;

	/**
	 * Creates a Picker backed by the configured Bindex repository and a named GenAI
	 * provider.
	 *
	 * @param bindexRepository the repository used to save and retrieve Bindex data
	 * @param configurator     the project configuration used for repository and
	 *                         prompt settings
	 */
	public Picker(BindexRepository bindexRepository, Configurator configurator) {
		this.configurator = configurator;
		this.bindexRepository = bindexRepository;
	}

	/**
	 * Saves a {@link Bindex} entry to the repository, generating and storing its
	 * embedding vector.
	 *
	 * @param bindex the {@link Bindex} object to save
	 * @return the unique identifier of the saved {@link Bindex} entry
	 * @throws IllegalArgumentException if the embedding vector generation or JSON 
	 *                                  serialization fails due to a syntax issue
	 */
	public String save(Bindex bindex) {
		try {
			List<Double> embeddingBson = getEmbedding(bindex.getClassification());
			String recordId = bindexRepository.save(bindex, embeddingBson);
			
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully registered Bindex [ID: {}] in repository under database record [ID: {}]", 
						bindex.getId(), recordId);
			}

			return bindex.getId();
			
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to serialize classification embedding for Bindex ID: " + bindex.getId(), e);
		}
	}

	/**
	 * Recommends a list of {@link Bindex} entries based on the provided prompt and
	 * minimum score.
	 * <p>
	 * This method uses a GenAI provider to classify the prompt, generates an
	 * embedding, and queries the Bindex repository for matching entries.
	 *
	 *
	 * @param prompt             the natural language description of project
	 *                           requirements
	 * @param vectorSearchLimits the maximum number of results to return from vector
	 *                           search
	 * @param score              the minimum relevance score threshold for
	 *                           recommended entries
	 * @param configurator       the configuration object
	 * @return a list of recommended {@link Bindex} entries matching the criteria
	 * @throws IOException if there is an error during classification or repository
	 *                     access
	 */
	public Collection<BindexInfo> pick(String prompt, long vectorSearchLimits, double score,
			Configurator configurator)
			throws IOException {
		String classificationStr = getClassification(prompt, configurator);

		String embeddingModel = configurator.get("embedding.model");
		EmbeddingProvider embeddingProvider = GenaiProviderManager.getEmbeddingProvider(embeddingModel, configurator);

		List<Double> embedding = embeddingProvider.embedding(classificationStr, dimensions);

		if (Strings.CS.contains(classificationStr, "```json")) {
			classificationStr = StringUtils.substringBetween(classificationStr, "```json", "```");
		}

		Classification[] classifications = new ObjectMapper().readValue(classificationStr, Classification[].class);
		Collection<BindexInfo> collection = bindexRepository.find(classifications, embedding, vectorSearchLimits, score,
				configurator);

		for (BindexInfo bindexInfo : collection) {
			if (bindexInfo.getDescription() == null) {
				String description = bindexRepository.getBindex(bindexInfo.getId()).getDescription();
				bindexInfo.setDescription(description);
			}
		}

		return collection;
	}

	/**
	 * Generates the embedding vector for a classification.
	 *
	 * @param classification the classification to embed
	 * @return the embedding encoded as a list of doubles
	 * @throws JsonProcessingException  if the classification cannot be serialized
	 * @throws IllegalArgumentException if the classification is null
	 */
	private List<Double> getEmbedding(Classification classification) throws JsonProcessingException {
		if (classification == null) {
			throw new IllegalArgumentException("classification must not be null");
		}
		String text = getClassificationText(classification);
		String embeddingModel = configurator.get("embedding.model");

		EmbeddingProvider embeddingProvider = GenaiProviderManager.getEmbeddingProvider(embeddingModel, configurator);
		List<Double> descEmbedding = embeddingProvider.embedding(text, dimensions);
		return descEmbedding;
	}

	/**
	 * Serializes a classification into JSON text for prompt and embedding
	 * generation.
	 *
	 * @param classification the classification to serialize
	 * @return the serialized JSON representation
	 * @throws JsonProcessingException if serialization fails
	 */
	private String getClassificationText(Classification classification) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(classification);
	}

	/**
	 * Builds a classification prompt from the query and executes it through the
	 * configured provider.
	 *
	 * @param query        the natural-language request to classify
	 * @param configurator the configuration object
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

}
