package org.machanism.machai.ai.provider.tools;

import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.tools.ParamDescriptor;
import org.machanism.machai.ai.tools.ToolFunction;

public class ToolsProvider extends AbstractAIProvider {

	@Override
	public void prompt(String text) {
		// TODO Auto-generated method stub
		super.prompt(text);
	}
	
	@Override
	public String perform() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void addTool(String name, String description, ToolFunction function, ParamDescriptor... paramsDesc) {
		// TODO Auto-generated method stub
		return;
	}

}
