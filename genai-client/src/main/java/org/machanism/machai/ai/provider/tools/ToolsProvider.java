package org.machanism.machai.ai.provider.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ToolsProvider extends AbstractAIProvider {

	/** Logger instance for this provider. */
	static Logger logger = LoggerFactory.getLogger(ToolsProvider.class);

	private List<String> prompts = new ArrayList<>();

	private Map<String, ToolFunction> toolMap = new HashMap<>();

	@Override
	public void prompt(String text) {
		prompts.add(text);
	}

	@Override
	public String perform() {
		if ("yaml".equals(chatModel)) {
			String yamlPrompt = prompts.get(prompts.size() - 1);
			Yaml yaml = new Yaml();
			Map callDescription = yaml.load(yamlPrompt);

			String toolName = (String) callDescription.get("tool");
			ToolFunction toolFunction = toolMap.get(toolName);

			JsonNode params = new ObjectMapper().valueToTree(callDescription.get("params"));

			safelyInvokeTool(toolName, toolFunction, params, getProjectDir());

			System.out.println(toolFunction);
		}
		return null;
	}

	@Override
	protected void addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc) {
		toolMap.put(name, function);
		logger.info("Registered tool '{}'", name);
	}

}
