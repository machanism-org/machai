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

/**
 * AI provider implementation for managing and invoking host-defined function tools.
 *
 * <p>
 * The {@code ToolsProvider} class extends {@link AbstractAIProvider} and provides infrastructure
 * for registering tool functions, handling prompts, and executing tool invocations based on
 * structured YAML input. Tools are registered in a map and can be invoked dynamically.
 * </p>
 *
 * <ul>
 *   <li>Prompts are collected and stored for later processing.</li>
 *   <li>Tool functions are registered by name and can be invoked with parameters parsed from YAML.</li>
 *   <li>Supports execution mode where the last prompt is interpreted as a YAML tool call description.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ToolsProvider provider = new ToolsProvider();
 * provider.addTool("myTool", "Description", myToolFunction, ...);
 * provider.prompt("tool: myTool\nparams:\n  key: value");
 * String result = provider.perform();
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public class ToolsProvider extends AbstractAIProvider {

    /** Logger instance for this provider. */
    static Logger logger = LoggerFactory.getLogger(ToolsProvider.class);

    /** List of collected prompt texts. */
    private List<String> prompts = new ArrayList<>();

    /** Map of registered tool functions by tool name. */
    private Map<String, ToolFunction> toolMap = new HashMap<>();

    /**
     * Adds a prompt text to the provider's prompt list.
     *
     * @param text the prompt text to add
     */
    @Override
    public void prompt(String text) {
        prompts.add(text);
    }

    /**
     * Executes the provider's perform logic.
     *
     * <p>
     * If the {@code chatModel} is set to "yaml", the last prompt is parsed as a YAML tool call description.
     * The tool name and parameters are extracted, and the corresponding tool function is invoked.
     * </p>
     *
     * @return the result of the tool invocation, or {@code null} if not applicable
     */
    @Override
    public String perform() {
        String result = null;
        if ("yaml".equals(chatModel)) {
            String yamlPrompt = prompts.get(prompts.size() - 1);
            Yaml yaml = new Yaml();
            @SuppressWarnings("rawtypes")
            Map callDescription = yaml.load(yamlPrompt);

            String toolName = (String) callDescription.get("tool");
            ToolFunction toolFunction = toolMap.get(toolName);

            JsonNode params = new ObjectMapper().valueToTree(callDescription.get("params"));
            result = safelyInvokeTool(toolName, toolFunction, params, getProjectDir());
        }
        return result;
    }

    /**
     * Registers a tool function with the provider.
     *
     * <p>
     * The tool is added to the internal map by name. Parameter descriptors are accepted but not used in this implementation.
     * </p>
     *
     * @param name        the tool name
     * @param description the tool description
     * @param function    the tool function implementation
     * @param paramsDesc  parameter descriptors for the tool (optional)
     */
    @Override
    protected void addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc) {
        toolMap.put(name, function);
    }

}