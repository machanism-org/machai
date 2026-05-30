package org.machanism.machai.mcp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.provider.GenaiAdapter;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.core.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class GenaiAdapterExt extends GenaiAdapter {
	private final Logger log = LoggerFactory.getLogger(GenaiAdapterExt.class);

	private final List<SyncToolSpecification> toolSpecifications;

	GenaiAdapterExt(List<SyncToolSpecification> toolSpecifications) {
		this.toolSpecifications = toolSpecifications;
	}

	@Override
	public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
		log.info("addTool: " + name);

		Map<String, JsonValue> properties = new HashMap<>();
		List<String> required = new ArrayList<>();

		if (paramsDesc != null) {
			for (String pDesc : paramsDesc) {
				addPropDescription(properties, required, pDesc);
			}
			addPropDescription(properties, required,
					"workingDir:string:required:The absolute path to the current project directory.");
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, Object> value = new HashMap<>();
			value.put("type", "object");
			value.put("properties", properties);
			value.put("required", required);

			String schema = mapper.writeValueAsString(value);

			log.info("schema: {}", schema);

			toolSpecifications.add(new McpServerFeatures.SyncToolSpecification(
					new McpSchema.Tool(name, description, schema),
					(exchange, args) -> {
						Object result;
						try {
							JsonNode params = mapper.convertValue(args, JsonNode.class);
							File workingDir = new File((String) args.get("workingDir"));
							result = function.apply(params, workingDir);
						} catch (Exception e) {
							log.error("Error", e);
							result = e.getMessage();
						}

						return McpSchema.CallToolResult.builder()
								.addContent(new TextContent(Objects.toString(result)))
								.isError(false)
								.build();
					}));
		} catch (JsonProcessingException e) {
			log.error("Error", e);
			throw new IllegalArgumentException(e);
		}
	}

	private void addPropDescription(Map<String, JsonValue> properties, List<String> required, String pDesc) {
		String[] desc = StringUtils.splitPreserveAllTokens(pDesc, ":");
		if (desc.length >= 3
				&& StringUtils.defaultString(desc[2]).toLowerCase(Locale.ROOT).equals("required")) {
			required.add(desc[0]);
		}
		Map<String, String> value = new HashMap<>();
		value.put("type", desc[1]);
		value.put("description", desc.length > 3 ? desc[3] : StringUtils.EMPTY);

		JsonValue requiredVal = JsonValue.from(value);
		properties.put(desc[0], requiredVal);
	}
}
