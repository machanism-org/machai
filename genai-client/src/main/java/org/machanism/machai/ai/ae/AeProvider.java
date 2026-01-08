package org.machanism.machai.ai.ae;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.ai.none.NoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganteater.ae.AEWorkspace;
import com.ganteater.ae.RecipeRunner;

/**
 * This provider offers an alternative solution when direct access to the GenAI
 * API is not possible. It functions as a gateway for interacting with web-based
 * user interfaces through a web driver, enabling seamless integration with
 * services like <a href="https://solutionshub.epam.com/solution/ai-dial">AI
 * DIAL</a> and <a href="https://www.youtube.com/@EPAMAIRunCodeMie">EPAM AI/Run
 * CodeMie</a>. Communication with web pages is automated using
 * <a href="https://ganteater.com">Anteater</a> recipes, which facilitate the
 * sending and receiving of information.
 * 
 * Please note that this provider may have certain limitations. Depending on the
 * specific recipes executed, there may be special requirements, such as
 * handling streaming security concerns or managing shared resources like the
 * clipboard. Additionally, you might need to install extra plugins when working
 * with platforms such as CodeMie. Be sure to review the instructions for your
 * target system and complete all necessary setup steps before using this
 * provider.
 * 
 * To configure the provider, use the model method to specify the Anteater
 * configuration name (e.g., CodeMie or AIDial). You can also refer to the
 * ae.xml file to view the list of supported configurations.
 * 
 * <p>
 * This class extends {@link NoneProvider} to utilize prompt-driven workflows
 * within the AE environment. It manages the AE workspace, setup nodes, and can
 * execute project recipes based on user prompts.
 * </p>
 * <h3>Usage Example</h3>
 * 
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("Ae:CodeMie");
 * </pre>
 * 
 * Thread safety: This implementation is NOT thread-safe.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
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
