package org.machanism.machai.ai.provider.web;

import java.io.File;

import org.apache.commons.lang3.Strings;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.provider.none.NoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ganteater.ae.AEWorkspace;
import com.ganteater.ae.RecipeRunner;

/**
 * {@link GenAIProvider} implementation that obtains model responses by automating a target GenAI service through its
 * web user interface.
 *
 * <p>Automation is executed via <a href="https://ganteater.com">Anteater</a> workspace recipes. The provider loads a
 * workspace configuration (see {@link #model(String)}), initializes the workspace with a project directory (see
 * {@link #setWorkingDir(File)}), and submits the current prompt list by running the {@code "Submit Prompt"} recipe (see
 * {@link #perform()}).
 *
 * <h2>Thread safety and lifecycle</h2>
 * <ul>
 *   <li>This provider is not thread-safe.</li>
 *   <li>Workspace state is stored in static fields; the working directory cannot be changed once initialized in the
 *       current JVM instance.</li>
 *   <li>{@link #close()} closes the underlying workspace.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
 * provider.model("config.yaml");
 * provider.setWorkingDir(new File("C:\\path\\to\\project"));
 * String response = provider.perform();
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class WebProvider extends NoneProvider {
	/** Logger for this class. */
	private static final Logger logger = LoggerFactory.getLogger(WebProvider.class);

	/** Shared Anteater workspace instance used for automation task execution. */
	private static AEWorkspace workspace = new AEWorkspace();

	/** Root directory for workspace operations (set once per JVM instance). */
	private static File rootDir;

	/** Name of the Anteater configuration to be loaded. */
	private static String configName;

	/** Disables headless mode to allow UI automation in environments that require a visible browser. */
	static {
		System.setProperty("java.awt.headless", "false");
	}

	/**
	 * Submits the current prompts to the configured web UI by executing the {@code "Submit Prompt"} recipe.
	 *
	 * <p>The list of prompts is passed to the workspace as a system variable named {@code INPUTS}. The recipe is expected
	 * to place the final response text into a variable named {@code result}.
	 *
	 * @return the response captured by the automation recipe, or {@code null} if the recipe produced no result
	 * @throws IllegalArgumentException if the underlying automation fails for any reason
	 */
	@Override
	public synchronized String perform() {
		try {
			workspace.getSystemVariables().put("INPUTS", super.getPrompts());
			RecipeRunner runTask = workspace.runTask("Submit Prompt", false);
			Object result = runTask.getProcessor().variables.get("result");

			clear();
			return result == null ? null : String.valueOf(result);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Initializes the Anteater workspace for the given project directory.
	 *
	 * <p>This method is intended to be called once per JVM instance. If called again with a different directory, it fails
	 * fast.
	 *
	 * <p>The workspace start directory is determined as follows:
	 * <ol>
	 *   <li>Use {@code workingDir} by default.</li>
	 *   <li>If a directory (or file) exists under {@code workingDir} at the path provided by system property
	 *       {@code recipes} (default: {@code genai-client\src\main\resources}), that location is used instead.</li>
	 * </ol>
	 *
	 * @param workingDir project root directory used for workspace variables and to resolve recipe locations
	 * @throws IllegalArgumentException if {@code workingDir} is {@code null}, initialization fails, or a different
	 *                                  working directory is requested
	 */
	@Override
	public void setWorkingDir(File workingDir) {
		if (workingDir == null) {
			throw new IllegalArgumentException("workingDir must not be null");
		}

		if (rootDir == null) {
			rootDir = workingDir;

			File startDir = workingDir;
			String recipes = System.getProperty("recipes", "genai-client\\src\\main\\resources");
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
		} else if (!Strings.CS.equals(rootDir.getAbsolutePath(), workingDir.getAbsolutePath())) {
			throw new IllegalArgumentException("WorkingDir change detected. Requested: `" + workingDir.getAbsolutePath()
					+ "`; currently in use: `" + rootDir.getAbsolutePath() + "`");
		}
	}

	/**
	 * Indicates whether this provider can be used concurrently by multiple threads.
	 *
	 * @return {@code false} because this provider keeps shared static workspace state
	 */
	@Override
	public boolean isThreadSafe() {
		return false;
	}

	/**
	 * Releases resources held by the underlying Anteater workspace.
	 *
	 * <p>This method is idempotent.
	 */
	@Override
	public void close() {
		workspace.close();
	}
}
