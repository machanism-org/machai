package org.machanism.machai.ai.manager;

import java.io.File;

public class SystemFunctionTools {

	private FileFunctionTools fileFunctionTools;
	private CommandFunctionTools commandFunctionTools;

	public SystemFunctionTools(File workingDir) {
		super();
		fileFunctionTools = new FileFunctionTools(workingDir);
		commandFunctionTools = new CommandFunctionTools(workingDir);
	}

	public void applyTools(GenAIProvider provider) {
		fileFunctionTools.applyTools(provider);
		commandFunctionTools.applyTools(provider);
	}

	public void setWorkingDir(File workingDir) {
		fileFunctionTools.setWorkingDir(workingDir);
		commandFunctionTools.setWorkingDir(workingDir);
	}
}
