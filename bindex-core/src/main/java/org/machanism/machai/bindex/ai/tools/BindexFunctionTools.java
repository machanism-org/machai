package org.machanism.machai.bindex.ai.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.bindex.core.BindexInfo;
import org.machanism.machai.bindex.core.BindexRepository;
import org.machanism.machai.bindex.core.MongoBindexRepository;
import org.machanism.machai.bindex.core.Picker;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <li><b>Discovery &amp; Recommendation:</b> Finding relevant libraries based
 * on natural language project descriptions.</li>
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

	/**
	 * Creates a new Bindex function tool set.
	 */
	public BindexFunctionTools() {
		// Default constructor.
	}

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
	private static final String VECTOR_SEARCH_LIMITS = "25";

	/**
	 * The default similarity score threshold used to filter out low-confidence
	 * search results during vector search queries. Only results with a cosine
	 * similarity score greater than or equal to this value will be returned.
	 * <p>
	 * Range: [0.0, 1.0] where 1.0 is an exact semantic match. Default: {@code 0.85}
	 * </p>
	 */
	private static final String DEFAULT_SCORE_VALUE = "0.85";

	/**
	 * Default file name for the Bindex JSON metadata file.
	 */
	private static final String BINDEX_JSON_FILE_NAME = "bindex.json";

	/**
	 * Property name for specifying the model to use in configuration.
	 */
	public static final String MODEL_PROP_NAME = "gw.model";

	/**
	 * Repository instance for accessing and managing Bindex records.
	 */
	private BindexRepository bindexRepository;

	/**
	 * Retrieves bindex metadata for a given project or library.
	 * <p>
	 * The identifier can be either a standard Bindex ID coordinates string or a
	 * remote URL pointing directly to a Bindex JSON descriptor file.
	 * </p>
	 * <p>
	 * If a GraphQL query is provided via the {@code graphql_query} parameter, the
	 * resulting {@link Bindex} object's JSON representation will be filtered to
	 * include only the requested fields before being returned.
	 * </p>
	 *
	 * @param id           The unique Bindex identifier (e.g.,
	 *                     'groupId:artifactId:version') or a remote HTTP/HTTPS URL
	 *                     location pointing to a Bindex JSON file; must not be
	 *                     {@code null}.
	 * @param query        An optional GraphQL-style query to filter the response
	 *                     payload fields and minimize token consumption (e.g.,
	 *                     {@code "{ name version classification { languages } }"}).
	 * @param configurator The configuration object.
	 * @return The filtered or complete {@link Bindex} object if found.
	 * @throws IOException              If the remote descriptor cannot be read or
	 *                                  serialized.
	 * @throws IllegalArgumentException If the bindex cannot be found, or if reading
	 *                                  from the provided URL fails.
	 */
	@Tool(name = "get_bindex", description = "Retrieves bindex metadata for a given project or library.")
	public Bindex getBindex(
			@Param(name = "id", description = "The unique bindex ID (e.g., 'groupId:artifactId:version') or "
					+ "a direct HTTP/HTTPS URL pointing to a remote bindex.json file location.") String id,
			@Param(name = "graphql_query", description = "An optional GraphQL-style selection query "
					+ "(e.g., '{ name classification { languages } }') to filter the returned JSON structure. "
					+ "Use this to retrieve only the specific fields you need and reduce token payload size.", defaultValue = Param.NULL) String query,
			Configurator configurator) throws IOException {

		Bindex result;
		if (Strings.CS.startsWithAny(id, "http://", "https://")) {
			URL bindexUrl = new URL(id);
			result = new ObjectMapper().readValue(bindexUrl, Bindex.class);
		} else {
			result = getBindexRepository(configurator).getBindex(id);
		}

		if (result == null) {
			throw new IllegalArgumentException("Bindex not found, id: " + id);
		}

		if (logger.isInfoEnabled()) {
			logger.info("Bindex: {}",
					StringUtils.abbreviate(new ObjectMapper().writeValueAsString(result),
							AbstractAIProvider.LOG_LINE_LENG));
		}

		if (query != null) {
			result = new ObjectMapper().treeToValue(GraphqlJsonFilter.filterJson(result, query), Bindex.class);
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
	 * @param prompt             A description of your project needs or
	 *                           requirements. For example, specify the
	 *                           functionality, technology stack, or features you
	 *                           want to implement.
	 * @param score              The minimum relevance score threshold for
	 *                           recommended libraries. Only libraries with a score
	 *                           equal to or higher than this value will be
	 *                           included. If not specified, a default value is
	 *                           used.
	 * @param vectorSearchLimits The maximum number of recommendations to retrieve
	 *                           from vector search.
	 * @param configurator       The configuration object.
	 * @return A collection of {@link BindexInfo} objects representing recommended
	 *         libraries.
	 * @throws IOException If there is an error during recommendation.
	 */
	@Tool(name = "pick_libraries", description = "Recommends libraries based on the user's prompt or project requirements.")
	public Collection<BindexInfo> getRecommendedLibraries(
			@Param(name = "prompt", description = "The user prompt describing project needs or requirements.") String prompt,
			@Param(name = "score", description = "The minimum relevance score threshold for recommended libraries. "
					+ "Only libraries with a score equal to or higher than this value will be included. "
					+ "If not specified, a default value is used.", defaultValue = DEFAULT_SCORE_VALUE) double score,
			@Param(name = "search_limits", description = "The minimum relevance score threshold for recommended libraries. "
					+ "Only libraries with a score equal to or higher than this value will be included. "
					+ "If not specified, a default value is used.", defaultValue = VECTOR_SEARCH_LIMITS) int vectorSearchLimits,
			Configurator configurator) throws IOException {

		BindexRepository bindexRepository = getBindexRepository(configurator);

		Picker picker = new Picker(bindexRepository, configurator);
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
	@Tool(name = "register_bindex", description = "Registers a Bindex JSON object either at the specified URL or from a file located in the project directory. "
			+ "Upon success, the Bindex ID is returned. Use this tool to add new or update existing Bindex metadata for your project, improving library search and integration.")
	public String registerBindex(
			@Param(name = "bindex_file_path", description = "The path of the Bindex file to register (must exist in the project directory) or URL.", defaultValue = BINDEX_JSON_FILE_NAME) String path,
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
	 * @return The unique Bindex ID assigned to the registered entry.
	 */
	@Tool(name = "register_bindex_json", description = "Registers a Bindex JSON object and returns the bindexId on successful registration.")
	public String registerBindexJson(
			@Param(name = "bindex_json", description = "The Bindex JSON object to register.") Bindex bindex,
			Configurator configurator) {
		Picker picker = new Picker(getBindexRepository(configurator), configurator);
		bindex.set$schema(BINDEX_SCHEMA);

		String bindexId = picker.save(bindex);
		return bindexId;
	}

	/**
	 * Loads and returns the system instructions and prompts required for Bindex
	 * generation.
	 * <p>
	 * This method reads the prompt template from the classpath resource
	 * {@code /prompts/bindex-generation.md} and returns it as a UTF-8 encoded
	 * string.
	 * </p>
	 *
	 * @return the content of the Bindex generation prompt template
	 * @throws IOException if the prompt template resource cannot be found or read
	 */
	@Prompt(name = "generate_bindex", description = "Loads the markdown template containing instructions and contextual prompts required to generate a Bindex file.")
	public String bindexGenerationPrompts() throws IOException {
		URL resource = BindexFunctionTools.class.getResource("/prompts/bindex-generator.md");
		String propmpt = IOUtils.toString(resource, StandardCharsets.UTF_8);
		return propmpt;
	}

}
