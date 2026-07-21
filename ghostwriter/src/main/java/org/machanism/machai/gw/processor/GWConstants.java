package org.machanism.machai.gw.processor;

/**
 * Central constants used by Ghostwriter configuration and runtime processing.
 */
public final class GWConstants {

	private GWConstants() {
	}

	/** Project directory configuration property name. */
	public static final String PROJECT_DIR_PROP_NAME = "project.dir";
	/** Default Ghostwriter properties file name. */
	public static final String GW_PROPERTIES_FILE_NAME = "gw.properties";
	/** System property used to override the configuration file name. */
	public static final String CONFIG_PROP_NAME = "gw.config";
	/** System property holding the Ghostwriter home directory. */
	public static final String HOME_PROP_NAME = "gw.home";
	/** Configuration property for the active model/provider. */
	public static final String MODEL_PROP_NAME = "gw.model";
	/** Configuration property for system instructions. */
	public static final String INSTRUCTIONS_PROP_NAME = "gw.instructions";
	/** Configuration property for excluded paths. */
	public static final String EXCLUDES_PROP_NAME = "gw.excludes";
	/** Configuration property for the custom acts location. */
	public static final String ACTS_LOCATION_PROP_NAME = "gw.acts";
	/** Configuration property for the default act. */
	public static final String ACT_PROP_NAME = "gw.act";
	/** Configuration property for worker thread count. */
	public static final String THREADS_PROP_NAME = "gw.threads";
	/** Configuration property for the scan path/pattern. */
	public static final String PATH_PROP_NAME = "gw.path";
	/** Configuration property controlling recursive module traversal. */
	public static final String NONRECURSIVE_PROP_NAME = "gw.nonRecursive";
	/** TOML property name containing prompt inputs/episodes. */
	public static final String INPUTS_PROPERTY_NAME = "inputs";
	/** Configuration property controlling interactive mode. */
	public static final String INTERACTIVE_MODE_PROP_NAME = "gw.interactive";

	/** Line continuation marker used for multi-line console input. */
	public static final String MULTIPLE_LINES_BREAKER = "\\";

}
