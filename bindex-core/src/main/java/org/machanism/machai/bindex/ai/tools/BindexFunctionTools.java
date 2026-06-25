package org.machanism.machai.bindex.ai.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
 * Registers Bindex-related function tools for a {@link Genai}.
 *
 * <p>
 * The tools exposed by this class are intended to be consumed by LLM-assisted
 * workflows so they can retrieve additional context (a Bindex document or the
 * Bindex JSON schema) on demand.
 * </p>
 *
 * <h2>Exposed tools</h2>
 * <ul>
 * <li>{@code get_bindex}: Fetches a registered {@link Bindex} by its id.</li>
 * <li>{@code pick_libraries}: Recommends libraries based on the user's prompt
 * or project requirements.</li>
 * <li>{@code register_bindex}: Registers a Bindex record from a file in the
 * project directory.</li>
 * <li>{@code register_bindex_json}: Registers a Bindex record from a JSON
 * object.</li>
 * </ul>
 *
 * <p>
 * A {@link MongoBindexRepository} is created when
 * {@link #setConfigurator(Configurator)} is invoked.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class BindexFunctionTools implements FunctionTools {

	private static final String BINDEX_SCHEMA = "https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex-core/src/main/resources/schema/bindex-schema-v2.json";

	private final String VECTOR_SEARCH_LIMITS = "25";

	private static final String BINDEX_JSON_FILE_NAME = "bindex.json";

	public static final String MODEL_PROP_NAME = "gw.model";

	private static final String SCORE_PROP_NAME = "pick.score";

	private final Logger logger = LoggerFactory.getLogger(BindexFunctionTools.class);

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

	private BindexRepository getBindexRepository(Configurator configurator) {
		if (bindexRepository == null) {
			bindexRepository = new MongoBindexRepository(new PropertiesConfigurator());
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
	 * @return A list of {@link BindexElement} objects representing recommended
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
	 * @param fileName     The path of the Bindex file to register (must exist in
	 *                     the project directory). Default: "bindex.json".
	 * @param projectDir   The project directory.
	 * @param configurator The configuration object.
	 * @return A map containing the record ID if registration is successful, or an
	 *         error message if the file is not found.
	 * @throws FileNotFoundException If the specified file does not exist.
	 * @throws IOException           If there is an error reading the file.
	 */
	@Tool(name = "register_bindex", description = "Registers a Bindex record from a file in the project directory.")
	public Map<String, String> registerBindex(
			@Param(name = "path", description = "The path of the Bindex file to register (must exist in the project directory). Default: "
					+ BINDEX_JSON_FILE_NAME, defaultValue = BINDEX_JSON_FILE_NAME) String fileName,
			File projectDir,
			Configurator configurator) throws IOException {

		if (projectDir == null) {
			throw new IllegalArgumentException(
					"Project dir is not defined by the environment, use `register_bindex_json` tool to bindex json registration.");
		}

		Picker picker = new Picker(getBindexRepository(configurator), configurator);
		File bindexFile = new File(projectDir, fileName);

		Map<String, String> result = new HashMap<>();
		if (bindexFile.exists()) {
			try (Reader reader = new FileReader(bindexFile)) {
				Bindex bindex = new ObjectMapper().readValue(reader, Bindex.class);
				bindex.set$schema(BINDEX_SCHEMA);
				String recordId = picker.save(bindex);
				result.put("RecordId", recordId);
			}
		} else {
			throw new FileNotFoundException("Bindex file not found: " + bindexFile);
		}

		return result;
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
			@Param(name = "bindex_json", description = "The Bindex json.") Bindex bindex, Configurator configurator) {
		Picker picker = new Picker(getBindexRepository(configurator), configurator);
		bindex.set$schema(BINDEX_SCHEMA);

		String recordId = picker.save(bindex);
		Map<String, String> result = new HashMap<>();
		result.put("RecordId", recordId);

		return result;
	}

}
