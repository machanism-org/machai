package org.machanism.machai.mcp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.provider.GenaiAdapter;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.core.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

class StdioGenaiAdapter extends GenaiAdapter {
	private final Logger log = LoggerFactory.getLogger(StdioGenaiAdapter.class);

	private final List<SyncToolSpecification> toolSpecifications;

	StdioGenaiAdapter(List<SyncToolSpecification> toolSpecifications) {
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

		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, Object> schema = new HashMap<>();
		schema.put("type", "object");
		schema.put("properties", properties);
		schema.put("required", required);

		log.info("schema: {}", schema);

		BiFunction<McpSyncServerExchange, CallToolRequest, McpSchema.CallToolResult> callHandler = (exchange, args) -> {
			String result;
			try {
				JsonNode params = mapper.convertValue(args, JsonNode.class);
				File workingDir = new File((String) args.arguments().get("workingDir"));
				result = Objects.toString(function.apply(params, workingDir));
			} catch (Exception e) {
				log.error("Error", e);
				result = e.getMessage();
			}

			return McpSchema.CallToolResult.builder()
					.addTextContent(result)
					.isError(false)
					.build();
		};
		toolSpecifications.add(SyncToolSpecification.builder()
				.tool(io.modelcontextprotocol.spec.McpSchema.Tool.builder(name, schema).build())
				.callHandler(callHandler)
				.build());
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
