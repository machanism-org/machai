package org.machanism.machai.ai.ae;

import java.io.File;

import org.machanism.machai.ai.none.NoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganteater.ae.AEWorkspace;
import com.ganteater.ae.RecipeRunner;

public class AeProvider extends NoneProvider {
	private static Logger logger = LoggerFactory.getLogger(AeProvider.class);

	private static AEWorkspace workspace = new AEWorkspace();

	private File rootDir;

	@Override
	public String perform() {
		try {
			workspace.getSystemVariables().put("INPUTS", super.getPrompts());
			RecipeRunner runTask = workspace.runTask("Prompt To CodeMie", false);
			String result = (String) runTask.getProcessor().variables.get("result");

			clear();
			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void setWorkingDir(File workingDir) {
		if (rootDir == null) {
			rootDir = workingDir;
			workspace.setStartDir(new File(workingDir, "src/ae"));

			workspace.getSystemVariables().put("PROJECT DIR", workingDir);
			workspace.loadConfiguration(true);
			try {
				workspace.runSetupNodes();
			} catch (Exception e) {
				logger.error("The execution of the starting recipes failed.", e);
				
				workspace.close();
				workspace = new AEWorkspace();
				rootDir = null;
				throw new IllegalArgumentException(e);
			}
		}
	}
}
