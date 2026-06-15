package org.machanism.machai.bindex.ai.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.bindex.BindexRepository;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
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
	 * Implementation for the {@code get_bindex} function tool.
	 * 
	 * @param configurator
	 *
	 * @param params       tool invocation parameters; the first element is expected
	 *                     to be a JSON node containing the tool arguments
	 * @return the serialized {@link Bindex} as JSON, or the literal {@code null}
	 *         when not found
	 * @throws JsonProcessingException
	 * @throws IllegalStateException   if the repository has not been configured yet
	 */
	@Tool(name = "get_bindex", description = "Retrieves bindex metadata for a given project or library.")
	public Object getBindex(@Param(name = "id", description = "The bindex id.") String id, Configurator configurator)
			throws JsonProcessingException {
		Object result = new BindexRepository(configurator).getBindex(id);
		if (logger.isInfoEnabled()) {
			if (result != null) {
				logger.info("Bindex: {}",
						StringUtils.abbreviate(new ObjectMapper().writeValueAsString(result),
								AbstractAIProvider.LOG_LINE_LENG));
			} else {
				result = "Bindex not found, id: " + id;
				logger.info((String) result);
			}
		}
		return result;
	}

	@Tool(name = "pick_libraries", description = "Recommends libraries based on the user's prompt or project requirements.")
	public List<BindexElement> getRecommendedLibraries(
			@Param(name = "prompt", description = "The user prompt describing project needs or requirements.") String prompt,
			Configurator configurator)
			throws IOException {
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
					StringUtils.abbreviate(result.toString(), AbstractAIProvider.LOG_LINE_LENG));
		}
		logger.debug("Detailed picked artifacts: {}", result);

		return result;
	}

	@Tool(name = "register_bindex", description = "Registers a Bindex record from a file in the working directory.")
	public String registerBindex(
			@Param(name = "file_name", description = "The name of the Bindex file to register (must exist in the working directory).") String fileName,
			@Param(name = "project_dir", description = "The project dir.") File projectDir, Configurator configurator)
			throws JsonProcessingException {
		String model = configurator.get(MODEL_PROP_NAME);
		Picker picker = new Picker(model, null, configurator);
		File bindexFile = new File(projectDir, fileName);

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

	@Tool(name = "register_bindex_json", description = "Registers a Bindex json.")
	public String registerBindexFile(
			@Param(name = "bindex_json", description = "The Bindex json.") Bindex bindex, Configurator configurator)
			throws JsonProcessingException {
		String model = configurator.get(MODEL_PROP_NAME);
		Picker picker = new Picker(model, null, configurator);

		String recordId = picker.create(bindex);
		String result = "RecordId: " + recordId;

		return result;
	}

}
