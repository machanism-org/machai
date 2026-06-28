package org.machanism.machai.bindex.ai.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.bindex.core.BindexInfo;
import org.machanism.machai.bindex.core.BindexRepository;
import org.machanism.machai.bindex.core.MongoBindexRepository;
import org.machanism.machai.bindex.core.Picker;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An AI tool set implementation of {@link FunctionTools} that provides
 * discovery, resolution, and registration actions for Bindex components to
 * large language models (LLMs).
 * <p>
 * This class exposes a suite of annotated capabilities with {@link Tool} and
 * {@link Param} metadata, enabling AI agents to seamlessly interact with Bindex
 * repositories. Key capabilities include:
 * </p>
 * <ul>
 * <li><b>Discovery &amp; Recommendation:</b> Finding relevant libraries based on
 * natural language project descriptions.</li>
 * <li><b>Metadata Extraction:</b> Querying comprehensive library descriptions
 * and schemas via specific IDs.</li>
 * <li><b>Descriptor Registration:</b> Adding or updating library declarations
 * from direct JSON objects, local project files, or remote URLs.</li>
 * </ul>
 * 
 * @see FunctionTools
 * @see Tool
 * @see BindexRepository
 * @see Picker
 */
public class BindexFunctionTools implements FunctionTools {

	/** Logger instance for logging diagnostic and operational messages. */
	private final Logger logger = LoggerFactory.getLogger(BindexFunctionTools.class);

	/**
	 * URL to the official Bindex JSON schema definition. Used for validating Bindex
	 * files and ensuring schema compliance.
	 */
	private static final String BINDEX_SCHEMA = "https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json";

	/**
	 * Default limit for the number of results returned by vector search operations.
	 */
	private final String VECTOR_SEARCH_LIMITS = "25";

	/**
	 * Default file name for the Bindex JSON metadata file.
	 */
	private static final String BINDEX_JSON_FILE_NAME = "bindex.json";

	/**
	 * Property name for specifying the model to use in configuration.
	 */
	public static final String MODEL_PROP_NAME = "gw.model";

	/**
	 * Property name for specifying the minimum score threshold for library picking.
	 */
	private static final String SCORE_PROP_NAME = "pick.score";

	/**
	 * Repository instance for accessing and managing Bindex records.
	 */
	private BindexRepository bindexRepository;

	/**
	 * Retrieves bindex metadata for a given project or library.
	 *
	 * @param id           The bindex id.
	 * @param configurator The configuration object.
	 * @return The {@link Bindex} object if found.
	 * @throws JsonProcessingException  If there is an error serializing the result.
	 * @throws IllegalArgumentException If the bindex is not found.
	 */
	@Tool(name = "get_bindex", description = "Retrieves bindex metadata for a given project or library.")
	public Bindex getBindex(@Param(name = "id", description = "The bindex id.") String id, Configurator configurator)
			throws JsonProcessingException {
		Bindex result = getBindexRepository(configurator).getBindex(id);
		if (logger.isInfoEnabled()) {
			if (result != null) {
				logger.info("Bindex: {}",
						StringUtils.abbreviate(new ObjectMapper().writeValueAsString(result),
								AbstractAIProvider.LOG_LINE_LENG));
			} else {
				throw new IllegalArgumentException("Bindex not found, id: " + id);
			}
		}
		return result;
	}

	/**
	 * Returns the current {@link BindexRepository} instance, initializing it if
	 * necessary.
	 * <p>
	 * If the repository has not yet been created, this method instantiates a new
	 * {@link MongoBindexRepository} using a default {@link PropertiesConfigurator}.
	 * The same instance is returned on subsequent calls.
	 * </p>
	 *
	 * @param configurator the configuration object (currently unused in this
	 *                     method)
	 * @return the {@link BindexRepository} instance
	 */
	private BindexRepository getBindexRepository(Configurator configurator) {
		if (bindexRepository == null) {
			bindexRepository = new MongoBindexRepository(configurator);
		}
		return bindexRepository;
	}

	/**
	 * Recommends libraries based on the user's prompt or project requirements.
	 *
	 * @param prompt       A description of your project needs or requirements. For
	 *                     example, specify the functionality, technology stack, or
	 *                     features you want to implement.
	 * @param score        The minimum relevance score threshold for recommended
	 *                     libraries. Only libraries with a score equal to or higher
	 *                     than this value will be included. If not specified, a
	 *                     default value is used.
	 * @param configurator The configuration object.
	 * @return A collection of {@link BindexInfo} objects representing recommended
	 *         libraries.
	 * @throws IOException If there is an error during recommendation.
	 */
	@Tool(name = "pick_libraries", description = "Recommends libraries based on the user's prompt or project requirements.")
	public Collection<BindexInfo> getRecommendedLibraries(
			@Param(name = "prompt", description = "The user prompt describing project needs or requirements.") String prompt,
			@Param(name = "score", description = "The minimum relevance score threshold for recommended libraries. "
					+ "Only libraries with a score equal to or higher than this value will be included. "
					+ "If not specified, a default value is used.", defaultValue = Param.NULL) Double score,
			@Param(name = "search_limits", description = "The minimum relevance score threshold for recommended libraries. "
					+ "Only libraries with a score equal to or higher than this value will be included. "
					+ "If not specified, a default value is used. Default: "
					+ VECTOR_SEARCH_LIMITS, defaultValue = VECTOR_SEARCH_LIMITS) int vectorSearchLimits,
			Configurator configurator)
			throws IOException {

		Picker picker = new Picker(getBindexRepository(configurator), configurator);
		score = configurator.getDouble(SCORE_PROP_NAME, score);

		Collection<BindexInfo> bindexList = picker.pick(prompt, vectorSearchLimits, score, configurator);

		if (logger.isInfoEnabled()) {
			logger.info("Number of recommended libraries picked: {}. Artifacts: {}", bindexList.size(),
					StringUtils.abbreviate(bindexList.toString(), AbstractAIProvider.LOG_LINE_LENG));
		}
		logger.debug("Detailed picked artifacts: {}", bindexList);

		return bindexList;
	}

	/**
	 * Registers a Bindex record from a file in the project directory.
	 *
	 * @param path         The path of the Bindex file to register (must exist in
	 *                     the project directory). Default: "bindex.json".
	 * @param projectDir   The project directory.
	 * @param configurator The configuration object.
	 * @return A map containing the record ID if registration is successful, or an
	 *         error message if the file is not found.
	 * @throws FileNotFoundException If the specified file does not exist.
	 * @throws IOException           If there is an error reading the file.
	 */
	@Tool(name = "register_bindex", description = "Registers a Bindex JSON either from a specified URL or from a file located in the project directory. "
			+ "On success, returns the unique RecordId assigned to the registered Bindex entry. "
			+ "Use this tool to add new or update existing Bindex metadata for your project, enabling enhanced library discovery and integration.")
	public String registerBindex(
			@Param(name = "bindex_file_path", description = "The path of the Bindex file to register (must exist in the project directory) or URL. Default: "
					+ BINDEX_JSON_FILE_NAME, defaultValue = BINDEX_JSON_FILE_NAME) String path,
			File projectDir,
			Configurator configurator) throws IOException {

		Bindex bindex = null;
		if (Strings.CS.startsWithAny(path, "http://", "https://")) {
			URL bindexFile = new URL(path);
			bindex = new ObjectMapper().readValue(bindexFile, Bindex.class);

		} else {
			if (projectDir == null) {
				throw new IllegalArgumentException(
						"Project directory is not defined in the environment. Only registration by URL is supported in this context.");
			}

			File bindexFile = new File(path);
			if (!bindexFile.isAbsolute()) {
				bindexFile = new File(projectDir, path);
			} else {
				String relativ = projectDir.toURI().relativize(new File(path).toURI()).getPath();
				if (new File(relativ).isAbsolute()) {
					throw new IllegalArgumentException(
							"The 'path' parameter must be specified as a relative path within the project directory.");
				}
				bindexFile = new File(projectDir, relativ);
			}

			bindex = new ObjectMapper().readValue(bindexFile, Bindex.class);
		}

		bindex.set$schema(BINDEX_SCHEMA);

		Picker picker = new Picker(getBindexRepository(configurator), configurator);
		String recordId = picker.save(bindex);

		return recordId;
	}

	/**
	 * Registers a Bindex record from a JSON object.
	 *
	 * @param bindex       The Bindex JSON object.
	 * @param configurator The configuration object.
	 * @return A map containing the record ID after successful registration.
	 * @throws JsonProcessingException If there is an error processing the JSON.
	 */
	@Tool(name = "register_bindex_json", description = "Registers a Bindex json.")
	public Map<String, String> registerBindexJson(
			@Param(name = "bindex_json", description = "The Bindex json.") Bindex bindex,
			Configurator configurator) {
		Picker picker = new Picker(getBindexRepository(configurator), configurator);
		bindex.set$schema(BINDEX_SCHEMA);

		String recordId = picker.save(bindex);
		Map<String, String> result = new HashMap<>();
		result.put("RecordId", recordId);

		return result;
	}

}
