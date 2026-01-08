package org.machanism.machai.ai.ae;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.none.NoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganteater.ae.AEWorkspace;
import com.ganteater.ae.RecipeRunner;

/**
 * Provides integration with AE Workspace for running prompt-to-code tasks.
 * <p>
 * This class extends {@link NoneProvider} to utilize prompt-driven workflows
 * within the AE environment. It manages the AE workspace, setup nodes, and can
 * execute project recipes based on user prompts.
 * </p>
 * <h3>Usage Example</h3>
 * 
 * <pre>
 * AeProvider provider = new AeProvider();
 * provider.model("CodeMie");
 * provider.setWorkingDir(new File("/path/to/project"));
 * String result = provider.perform();
 * </pre>
 *
 * @author machanism.org
 * @since 1.0
 */
public class AeProvider extends NoneProvider {
	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(AeProvider.class);

	/** AEWorkspace instance used for task execution. */
	private static AEWorkspace workspace = new AEWorkspace();

	/** Root directory for workspace operations. */
	private File rootDir;

	/** Name of the configuration file to be loaded. */
	private String configName;

	static {
		// Ensure Java AWT headless mode is appropriately set for AE tasks
		System.setProperty("java.awt.headless", "false");
	}

	/**
	 * Runs the AE workspace task using the input prompts.
	 *
	 * @return Result string of the AE task execution.
	 * @throws IllegalArgumentException If AE task execution fails. Wraps any
	 *                                  Exception.
	 */
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

	/**
	 * Sets the working directory for AE workspace operations, initializes
	 * configuration and runs setup nodes if not already initialized.
	 *
	 * @param workingDir The working directory to be set.
	 * @throws IllegalArgumentException If initialization fails.
	 */
	@Override
	public void setWorkingDir(File workingDir) {
		if (rootDir == null) {
			rootDir = workingDir;

			File startDir = workingDir;
			File externalAeConfigFile = new File(workingDir, "src/ae/ae.xml");
			if (externalAeConfigFile.exists()) {
				startDir = externalAeConfigFile.getParentFile();
			}
			workspace.setStartDir(startDir);

			workspace.getSystemVariables().put("PROJECT DIR", workingDir);
			workspace.loadConfiguration(configName, false);

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

	/**
	 * Sets the configuration name for the AE workspace, ensuring it's changed from
	 * previous value.
	 *
	 * @param configName The name of the configuration file.
	 * @throws IllegalArgumentException If configuration change is requested while
	 *                                  already in use.
	 */
	@Override
	public void model(String configName) {
		if (this.configName != null && StringUtils.equals(this.configName, configName)) {
			throw new IllegalArgumentException("Configuration change detected. Requested: `" + configName
					+ "`; currently in use: `" + this.configName + "`.");
		}
		this.configName = StringUtils.trimToNull(configName);
	}

}
