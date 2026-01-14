package org.machanism.machai.ai.provider.web;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.provider.none.NoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganteater.ae.AEWorkspace;
import com.ganteater.ae.RecipeRunner;

/**
 * The {@code WebProvider} class serves as a gateway for interacting with
 * web-based user interfaces via a web driver when direct access to the GenAI
 * API is not feasible. It automates communication with supported services such
 * as <a href="https://solutionshub.epam.com/solution/ai-dial">AI DIAL</a> and
 * <a href="https://www.youtube.com/@EPAMAIRunCodeMie">EPAM AI/Run CodeMie</a>,
 * utilizing recipes from <a href="https://ganteater.com">Anteater</a> for
 * sending and receiving information.
 * <p>
 * <b>Limitations:</b> Configuration and usage of this class may require
 * additional plugins or handling of resources such as the clipboard, especially
 * for platforms like CodeMie. Please refer to target platform instructions
 * prior to use.
 * </p>
 *
 * <h3>Usage Example</h3>
 * 
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
 * </pre>
 *
 * <h4>Thread Safety</h4> This implementation is <b>not</b> thread-safe.
 *
 * <h4>Parameters and Methods</h4>
 * <ul>
 * <li><b>perform()</b> - Executes the AE workspace task using input
 * prompts.</li>
 * <li><b>setWorkingDir(File workingDir)</b> - Initializes workspace with
 * configuration and runs setup nodes.</li>
 * <li><b>model(String configName)</b> - Sets the AE workspace configuration
 * name.</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class WebProvider extends NoneProvider {
	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(WebProvider.class);

	/** AEWorkspace instance used for task execution. */
	private static AEWorkspace workspace = new AEWorkspace();

	/** Root directory for workspace operations. */
	private static File rootDir;

	/** Name of the configuration file to be loaded. */
	private static String configName;

	static {
		// Ensure Java AWT headless mode is appropriately set for AE tasks
		System.setProperty("java.awt.headless", "false");
	}

	/**
	 * Executes the AE workspace task using provided input prompts.
	 *
	 * @return the result string from AE task execution
	 * @throws IllegalArgumentException if AE task execution fails (wraps any
	 *                                  Exception)
	 * 
	 *                                  <pre>
	 * &lt;code&gt;
	 * String result = provider.perform();
	 * &lt;/code&gt;
	 *                                  </pre>
	 */
	@Override
	public synchronized String perform() {
		try {
			workspace.getSystemVariables().put("INPUTS", super.getPrompts());
			RecipeRunner runTask = workspace.runTask("Submit Prompt", false);
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
	 * @param workingDir the working directory to be set
	 * @throws IllegalArgumentException if initialization fails
	 */
	@Override
	public void setWorkingDir(File workingDir) {
		if (rootDir == null) {
			rootDir = workingDir;

			File startDir = workingDir;
			String recipes = System.getProperty("recipes", "genai-client/src/main/resources");
			File externalAeConfigFile = new File(workingDir, recipes);
			if (externalAeConfigFile.exists()) {
				startDir = externalAeConfigFile;
			}
			workspace.setStartDir(startDir);
			workspace.getSystemVariables().put("PROJECT_DIR", workingDir);
			workspace.loadConfiguration(configName, false);
			try {
				workspace.runSetupNodes();
			} catch (Exception e) {
				logger.error("The execution of the starting recipes failed: {}", e.getMessage());
				workspace.close();
				workspace = new AEWorkspace();
				rootDir = null;
				throw new IllegalArgumentException(e);
			}
		} else {
			if (!StringUtils.equals(rootDir.getAbsolutePath(), workingDir.getAbsolutePath())) {
				throw new IllegalArgumentException("WorkingDir change detected. Requested: `"
						+ workingDir.getAbsolutePath() + "`; currently in use: `" + rootDir.getAbsolutePath() + "`");
			}
		}
	}

	/**
	 * Sets the AE workspace configuration name, validating change from previous
	 * value.
	 *
	 * @param configName the name of the configuration file
	 * @throws IllegalArgumentException if configuration change is requested while
	 *                                  already in use
	 */
	@Override
	public void model(String configName) {
		if (WebProvider.configName != null && StringUtils.equals(WebProvider.configName, configName)) {
			throw new IllegalArgumentException("Configuration change detected. Requested: `" + configName
					+ "`; currently in use: `" + WebProvider.configName + "`.");
		}
		WebProvider.configName = StringUtils.trimToNull(configName);
	}

	@Override
	public void close() {
		workspace.close();
	}
}
