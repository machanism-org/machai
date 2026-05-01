package org.machanism.machai.bindex.ai.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.bindex.BindexRepository;
import org.machanism.machai.bindex.Picker;
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

	public static final String MODEL_PROP_NAME = "gw.model";

	private final Logger logger = LoggerFactory.getLogger(BindexFunctionTools.class);

	private static final int MAXWIDTH = 160;

	private BindexRepository bindexRepository;

	private Configurator configurator;

	public class BindexElement {
		public BindexElement(String id, String description) {
			super();
			this.id = id;
			this.description = description;
		}

		private String id;
		private String description;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return id;
		}
	}

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

		provider.addTool(
				"register_bindex",
				"Registers a Bindex record from a file in the working directory.",
				this::registerBindex,
				"fileName:string:required:The name of the Bindex file to register (must exist in the working directory).");
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
	public String getBindex(JsonNode props, File workingDir) throws JsonProcessingException {
		String id = props.get("id").asText();
		Bindex bindex = getBindexRepository().getBindex(id);
		ObjectMapper objectMapper = new ObjectMapper();
		String bindexJson = bindex == null ? "<not found>" : objectMapper.writeValueAsString(bindex);
		if (logger.isInfoEnabled()) {
			if (bindex != null) {
				logger.info("Bindex: {}", StringUtils.abbreviate(bindexJson, MAXWIDTH));
			} else {
				logger.info("Bindex not found, id: {}", id);
			}
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
	public String getBindexSchema(JsonNode props, File workingDir) throws IOException {
		URL systemResource = Bindex.class.getResource(BindexRepository.BINDEX_SCHEMA_RESOURCE);
		String schema = IOUtils.toString(systemResource, StandardCharsets.UTF_8);
		if (logger.isInfoEnabled()) {
			logger.info("Bindex schema: {}",
					StringUtils.abbreviate(schema, 120).replace(Genai.LINE_SEPARATOR, " ").replace("\r", ""));
		}
		return schema;
	}

	public List<BindexElement> getRecommendedLibraries(JsonNode props, File workingDir) throws IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Picking for: {}", StringUtils.abbreviate(String.valueOf(props), MAXWIDTH));
		}
		logger.debug("Picking for: {}", props);

		String prompt = props.get("prompt").asText();

		String model = configurator.get(Picker.MODEL_PROP_NAME, configurator.get(MODEL_PROP_NAME));
		Double score = configurator.getDouble(Picker.SCORE_PROP_NAME, Picker.DEFAULT_SCORE_VALUE);
		String registerUrl = configurator.get("BINDEX_REPO_URL", null);

		Picker picker = new Picker(model, registerUrl, configurator);
		picker.setScore(score);

		List<Bindex> bindexList = picker.pick(prompt);

		List<BindexElement> result = new ArrayList<>();

		for (Bindex bindex : bindexList) {
			if (bindex != null) {
				result.add(new BindexElement(bindex.getId(), bindex.getDescription()));
			}
		}

		if (logger.isInfoEnabled()) {
			logger.info("Number of recommended libraries picked: {}. Artifacts: {}", result.size(),
					StringUtils.abbreviate(result.toString(), MAXWIDTH));
		}
		logger.debug("Detailed picked artifacts: {}", result);

		return result;
	}

	public String registerBindex(JsonNode props, File workingDir) throws JsonProcessingException {
		String fileName = props.get("fileName").asText();

		if (logger.isInfoEnabled()) {
			logger.info("Register Bindex: {}, {}", props, workingDir);
		}

		String model = configurator.get(MODEL_PROP_NAME);
		Picker picker = new Picker(model, null, configurator);
		File bindexFile = new File(workingDir, fileName);

		String result;
		if (bindexFile.exists()) {
			try (Reader reader = new FileReader(bindexFile)) {
				Bindex bindex = new ObjectMapper().readValue(reader, Bindex.class);

				String recordId = picker.create(bindex);
				result = "RecordId: " + recordId;
			} catch (IOException e) {
				logger.error("registerBindex failed.", e);
				result = "Error: " + e.getMessage();
			}
		} else {
			result = "file not found";
			logger.error("Bindex file not found: {}", bindexFile);
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
