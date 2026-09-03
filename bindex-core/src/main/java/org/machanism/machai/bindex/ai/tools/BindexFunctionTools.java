package org.machanism.machai.bindex.ai.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.Resource;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.bindex.core.BindexInfo;
import org.machanism.machai.bindex.core.BindexRepository;
import org.machanism.machai.bindex.core.MongoBindexRepository;
import org.machanism.machai.bindex.core.Picker;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An AI tool set implementation of {@link FunctionTools} that provides
 * discovery, resolution, and registration actions for Bindex components to
 * large language models (LLMs).
 * <p>
 * This class exposes annotated {@link Tool}, {@link Prompt}, {@link Resource},
 * and {@link Param} capabilities for AI agents to interact with Bindex
 * repositories. Its functional AI tools are {@code get-bindex}, which retrieves
 * metadata; {@code pick-libraries}, which recommends libraries;
 * {@code register-bindex}, which registers a descriptor from a file or URL; and
 * {@code register-bindex-json}, which registers a supplied descriptor. It also
 * provides the {@code generate-bindex} prompt template and the
 * {@code file:///schema/bindex-schema-v2.json} contextual JSON Schema resource.
 * Key capabilities include:
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
	 * Functional AI Tool that retrieves Bindex metadata for a given project or
	 * library.
	 * <p>
	 * The {@code id} parameter accepts standard Bindex coordinates, a direct
	 * {@code http://} or {@code https://} URL for a remote descriptor, or a
	 * {@code file://} path for local JSON parsing and validation. Relative file
	 * paths are resolved against {@code projectDir}; all other identifiers are
	 * resolved through the configured repository.
	 * </p>
	 * <p>
	 * If a GraphQL query is provided via the {@code graphql-query} parameter, the
	 * resulting {@link Bindex} object's JSON representation will be filtered to
	 * include only the requested fields before being returned.
	 * </p>
	 *
	 * @param id           The unique Bindex identifier (for example,
	 *                     {@code groupId:artifactId:version}), a direct
	 *                     {@code http://} or {@code https://} Bindex JSON URL, or a
	 *                     {@code file://} path; must not be {@code null}.
	 * @param query        An optional GraphQL-style query to filter the response
	 *                     payload fields and minimize token consumption (e.g.,
	 *                     {@code "{ name version classification { languages } }"}).
	 * @param projectDir   The base directory used to resolve a relative
	 *                     {@code file://} descriptor path; required for relative
	 *                     file paths and otherwise may be {@code null}.
	 * @param configurator The repository configuration used when {@code id} is a
	 *                     Bindex identifier.
	 * @return The complete {@link Bindex}, or a deserialized projection when
	 *         {@code query} is supplied.
	 * @throws IOException              If the remote descriptor cannot be read or
	 *                                  serialized.
	 * @throws IllegalArgumentException If no Bindex is found for an identifier.
	 */
	@Tool(name = "get-bindex", description = "Retrieves bindex metadata for a given project or library.")
	public Bindex getBindex(
			@Param(name = "id", description = "The unique bindex ID (e.g., 'groupId:artifactId:version') or "
					+ "a direct HTTP/HTTPS URL pointing to a remote bindex.json file location, or a 'file://' path for local JSON parsing/validation.") String id,
			@Param(name = "graphql-query", description = "An optional GraphQL-style selection query "
					+ "(e.g., '{ name classification { languages } }') to filter the returned JSON structure. "
					+ "Use this to retrieve only the specific fields you need and reduce token payload size.", defaultValue = Param.NULL) String query,
			File projectDir,
			Configurator configurator) throws IOException {

		Bindex result;
		if (Strings.CS.startsWithAny(id, "http://", "https://")) {
			URL bindexUrl = new URL(id);
			result = new ObjectMapper().readValue(bindexUrl, Bindex.class);
		} else if (Strings.CS.startsWith(id, "file://")) {
			String path = StringUtils.substringAfter(id, "file://");
			// SonarQube java:S2083: constrain local descriptors to the project directory.
			File fileUrl = resolveProjectFile(path, projectDir);
			result = new ObjectMapper().readValue(fileUrl, Bindex.class);
		} else {
			BindexRepository bindexRepository = getBindexRepository(configurator);
			result = bindexRepository.getBindex(id);
		}

		if (result == null) {
			throw new IllegalArgumentException("Bindex not found, id: " + id);
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
	 * {@link MongoBindexRepository} using the supplied configuration. The same
	 * instance is returned on subsequent calls.
	 * </p>
	 *
	 * @param configurator the configuration used when a repository must be
	 *                     initialized
	 * @return the {@link BindexRepository} instance
	 */
	private BindexRepository getBindexRepository(Configurator configurator) {
		if (bindexRepository == null) {
			bindexRepository = new MongoBindexRepository(configurator);
		}
		return bindexRepository;
	}

	/**
	 * Functional AI Tool that recommends libraries based on the user's prompt or
	 * project requirements.
	 *
	 * @param prompt             A description of your project needs or
	 *                           requirements. For example, specify the
	 *                           functionality, technology stack, or features you
	 *                           want to implement.
	 * @param score              The minimum relevance score threshold for
	 *                           recommended libraries. Only libraries with a score
	 *                           equal to or higher than this value are included;
	 *                           the tool metadata defaults it to {@code 0.85}.
	 * @param vectorSearchLimits The maximum number of recommendations to retrieve
	 *                           from vector search. The value is passed to the
	 *                           configured picker as the result limit; the tool
	 *                           metadata defaults it to {@code 25}.
	 * @param configurator       The configuration used to create the repository and
	 *                           picker.
	 * @return A collection of {@link BindexInfo} objects representing recommended
	 *         libraries.
	 * @throws IOException If there is an error during recommendation.
	 */
	@Tool(name = "pick-libraries", description = "Recommends libraries based on the user's prompt or project requirements.")
	public Collection<BindexInfo> getRecommendedLibraries(
			@Param(name = "prompt", description = "The user prompt describing project needs or requirements.") String prompt,
			@Param(name = "score", description = "The minimum relevance score threshold for recommended libraries. "
					+ "Only libraries with a score equal to or higher than this value will be included. "
					+ "If not specified, a default value is used.", defaultValue = DEFAULT_SCORE_VALUE) double score,
			@Param(name = "search-limits", description = "The maximum number of relevant libraries to return. "
					+ "If not specified, the default result limit is used.", defaultValue = VECTOR_SEARCH_LIMITS) int vectorSearchLimits,
			Configurator configurator) throws IOException {

		BindexRepository bindexRepository = getBindexRepository(configurator);

		Picker picker = new Picker(bindexRepository, configurator);
		Collection<BindexInfo> bindexList = picker.pick(prompt, vectorSearchLimits, score, configurator);
		return bindexList;
	}

	/**
	 * Functional AI Tool that registers a Bindex JSON object from a file in the
	 * project directory or a direct {@code http://} or {@code https://} URL. On
	 * success it returns the Bindex ID and adds or updates the metadata available
	 * to library search and integration.
	 *
	 * @param path         The Bindex file path, which must exist within the project
	 *                     directory, or a direct HTTP(S) URL. The tool metadata
	 *                     defaults it to {@code bindex.json}.
	 * @param projectDir   The project directory; required for file registration.
	 * @param configurator The configuration used to create the repository and
	 *                     picker.
	 * @return The unique record ID assigned by the configured picker.
	 * @throws FileNotFoundException    If the specified file does not exist.
	 * @throws IOException              If there is an error reading the file.
	 * @throws IllegalArgumentException If {@code projectDir} is absent for a
	 *                                  file-based registration, or an absolute path
	 *                                  lies outside that directory.
	 */
	@Tool(name = "register-bindex", description = "Registers a Bindex JSON object either at the specified URL or from a file located in the project directory. "
			+ "Upon success, the Bindex ID is returned. Use this tool to add new or update existing Bindex metadata for your project, improving library search and integration.")
	public String registerBindex(
			@Param(name = "bindex-file-path", description = "The path of the Bindex file to register (must exist in the project directory) or URL.", defaultValue = BINDEX_JSON_FILE_NAME) String path,
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

			// SonarQube java:S2083: canonicalize before checking the trusted base path.
			File bindexFile = resolveProjectFile(path, projectDir);

			bindex = new ObjectMapper().readValue(bindexFile, Bindex.class);
		}

		bindex.set$schema(BINDEX_SCHEMA);

		Picker picker = new Picker(getBindexRepository(configurator), configurator);
		String recordId = picker.save(bindex);

		return recordId;
	}

	/**
	 * Resolves a descriptor and ensures that it remains within the project tree.
	 *
	 * @param path       the user-provided descriptor path
	 * @param projectDir the trusted project base directory
	 * @return the canonical descriptor file
	 * @throws IOException              if a path cannot be canonicalized
	 * @throws IllegalArgumentException if the base directory is absent or the
	 *                                  descriptor is outside it
	 */
	File resolveProjectFile(String path, File projectDir) throws IOException {
		if (projectDir == null) {
			throw new IllegalArgumentException("Project directory is not defined in the environment.");
		}
		File canonicalProjectDir = projectDir.getCanonicalFile();
		File requestedFile = new File(path);
		File resolvedFile = requestedFile.isAbsolute() ? requestedFile : new File(canonicalProjectDir, path);
		File canonicalFile = resolvedFile.getCanonicalFile();
		Path basePath = canonicalProjectDir.toPath();

		if (!canonicalFile.toPath().startsWith(basePath)) {
			throw new IllegalArgumentException(
					"The 'path' parameter must identify a file within the project directory.");
		}
		return canonicalFile;
	}

	/**
	 * Functional AI Tool that registers a Bindex JSON object and returns its Bindex
	 * ID on successful registration.
	 *
	 * @param bindex       The Bindex JSON object to register; must not be
	 *                     {@code null}.
	 * @param configurator The configuration used to create the repository and
	 *                     picker.
	 * @return The unique Bindex ID assigned to the registered entry.
	 * @throws NullPointerException If {@code bindex} is {@code null}.
	 */
	@Tool(name = "register-bindex-json", description = "Registers a Bindex JSON object and returns the bindexId on successful registration.")
	public String registerBindexJson(
			@Param(name = "bindex-json", description = "The Bindex JSON object to register.") Bindex bindex,
			Configurator configurator) {
		Picker picker = new Picker(getBindexRepository(configurator), configurator);
		bindex.set$schema(BINDEX_SCHEMA);

		String bindexId = picker.save(bindex);
		return bindexId;
	}

	/**
	 * Contextual Resource that retrieves the JSON Schema definition for Bindex
	 * (bundle index) validation.
	 * <p>
	 * This method loads the {@code file:///schema/bindex-schema-v2.json} resource
	 * from the classpath and returns its content as a UTF-8 encoded
	 * {@code application/json} string. The supplied URI is the contextual resource
	 * URI provided by the tool framework.
	 * </p>
	 *
	 * @param uri the resource URI supplied by the tool framework; its path is used
	 *            to locate the schema on the classpath
	 * @return the JSON Schema content for Bindex v2
	 * @throws IOException          if the schema resource cannot be read
	 * @throws NullPointerException if the configured schema resource is absent from
	 *                              the classpath
	 */
	@Resource(uri = "file:///schema/bindex-schema-v2.json", description = "The JSON schema definition used for validating Bindex (bundle index) structure, rules, and property metadata.", mimeType = "application/json")
	public String getBindexSchema(URI uri) throws IOException {
		URL resource = BindexFunctionTools.class.getResource(uri.getPath());
		String propmpt = IOUtils.toString(resource, StandardCharsets.UTF_8);
		return propmpt;
	}

	/**
	 * Prompt Template named {@code generate-bindex} that loads and returns the
	 * Markdown instructions and contextual prompts required for Bindex file
	 * generation.
	 * <p>
	 * This method reads the prompt template from the classpath resource
	 * {@code /prompts/generate_bindex.md} and returns it as a UTF-8 encoded string.
	 * </p>
	 *
	 * @return the content of the Bindex generation prompt template
	 * @throws IOException          if the prompt template resource cannot be read
	 * @throws NullPointerException if the prompt template resource is absent from
	 *                              the classpath
	 */
	@Prompt(name = "generate-bindex", description = "Loads the markdown template containing instructions and contextual prompts required to generate a Bindex file.")
	public String bindexGenerationPrompts() throws IOException {
		URL resource = BindexFunctionTools.class.getResource("/prompts/generate_bindex.md");
		String propmpt = IOUtils.toString(resource, StandardCharsets.UTF_8);
		return propmpt;
	}

}
