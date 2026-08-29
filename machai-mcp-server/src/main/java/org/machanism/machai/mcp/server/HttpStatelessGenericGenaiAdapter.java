package org.machanism.machai.mcp.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;
import org.machanism.machai.mcp.server.AbstractMcpServer.ToolSpecificationBuilder;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

/**
 * Adapts Machai tools, prompts, and resources to the stateless HTTP MCP
 * transport.
 *
 * @since 1.2.0
 */
public class HttpStatelessGenericGenaiAdapter extends
		AbstractPromptGenaiAdapter<McpTransportContext, SyncToolSpecification, McpStatelessServerFeatures.SyncPromptSpecification> {

	/** Resource specifications registered by this adapter. */
	private final List<McpStatelessServerFeatures.SyncResourceSpecification> resourceSpecifications = new ArrayList<>();

	/**
	 * Creates an adapter backed by the supplied tool specification collection.
	 *
	 * @param toolSpecifications collection receiving generated tool specifications
	 * @param builder builder that creates transport-specific tool specifications
	 */
	HttpStatelessGenericGenaiAdapter(List<SyncToolSpecification> toolSpecifications,
			ToolSpecificationBuilder<McpTransportContext> builder) {
		super(toolSpecifications, builder);
	}

	@Override
	protected McpStatelessServerFeatures.SyncPromptSpecification buildPromptSpecification(McpSchema.Prompt prompt,
			BiFunction<McpTransportContext, McpSchema.GetPromptRequest, McpSchema.GetPromptResult> promptHandler) {
		return new McpStatelessServerFeatures.SyncPromptSpecification(prompt, promptHandler);
	}

	@Override
	protected void addResource(URI uri, String description, String mimeType, ToolFunction function,
			ParamDescriptor... paramsDesc) {
		String name = StringUtils.substringAfterLast(uri.getPath(), "/");
		McpSchema.Resource resource = McpSchema.Resource.builder(uri.toString(), name).build();
		BiFunction<McpTransportContext, McpSchema.ReadResourceRequest, McpSchema.ReadResourceResult> readHandler = (context,
				request) -> {
			List<ResourceContents> contents = new ArrayList<>();
			try {
				Object result = function.apply(null, projectDir, getConfigurator(), uri);
				contents.add(TextResourceContents.builder(uri.toString(), String.valueOf(result)).mimeType(mimeType).build());
			} catch (Exception exception) {
				throw new IllegalArgumentException(exception);
			}
			return McpSchema.ReadResourceResult.builder(contents).build();
		};

		resourceSpecifications.add(new McpStatelessServerFeatures.SyncResourceSpecification(resource, readHandler));
	}

	/**
	 * Returns the resources registered with this adapter.
	 *
	 * @return mutable list of stateless resource specifications
	 */
	public List<McpStatelessServerFeatures.SyncResourceSpecification> getResources() {
		return resourceSpecifications;
	}
}
