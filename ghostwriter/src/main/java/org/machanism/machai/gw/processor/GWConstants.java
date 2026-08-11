package org.machanism.machai.gw.processor;

/**
 * Central constants used by Ghostwriter configuration and runtime processing.
 */
public final class GWConstants {

	private GWConstants() {
	}

	/**
	 * Property name for the project directory configuration.
	 * <p>
	 * The project directory is the base folder from which Ghostwriter begins
	 * analyzing the project's structure. It may point to:
	 * <ul>
	 * <li>The root of a single project (e.g., a Maven or Gradle project root),
	 * or</li>
	 * <li>A folder containing multiple projects or modules (e.g., a monorepo or a
	 * parent directory for several subprojects).</li>
	 * </ul>
	 * Ghostwriter supports a variety of project types and layouts, so this property
	 * can be configured flexibly to match different setups.
	 */
	public static final String PROJECT_DIR_PROP_NAME = "project.dir";

	/**
	 * Default Ghostwriter properties file name.
	 * <p>
	 * This is the name of the configuration properties file that can be used as one
	 * of the configuration data sources.
	 * <p>
	 * It can be overridden by the {@link #CONFIG_PROP_NAME} property.
	 */
	public static final String GW_CONFIG_FILE_NAME = "gw.properties";

	/**
	 * System property used to override the default configuration file name.
	 * <p>
	 * When set, its value replaces {@link #GW_CONFIG_FILE_NAME} as the
	 * configuration properties file to load.
	 */
	public static final String CONFIG_PROP_NAME = "gw.config";

	/** Configuration property for the active model/provider. */
	public static final String MODEL_PROP_NAME = "gw.model";

	/** Configuration property for system instructions. */
	public static final String INSTRUCTIONS_PROP_NAME = "gw.instructions";

	/** Configuration property for excluded paths. */
	public static final String EXCLUDES_PROP_NAME = "gw.excludes";

	/**
	 * Configuration property specifying the location of external act definition
	 * files ({@code *.toml}).
	 * <p>
	 * The value may be an absolute path, a path relative to the
	 * {@linkplain AbstractFileProcessor#getRootDir() root directory}, or a URL
	 * (starting with {@code http://} or {@code https://}) pointing to a remote
	 * source.
	 *
	 * @see ActProcessor#setActsLocation(String)
	 */
	public static final String ACTS_LOCATION_PROP_NAME = "gw.acts";

	/**
	 * System property used to specify the default action (Act) to run when no
	 * explicit act string is provided.
	 * <p>
	 * The value is parsed using the same rules described in the act-parsing
	 * lifecycle (task shorthand expansion, episode slicing, argument extraction,
	 * etc.), so it accepts any format valid for a raw command string (e.g.,
	 * {@code "bindex"}, {@code "bindex/java/mvn-project#2!"},
	 * {@code ">add javadoc"}).
	 */
	public static final String ACT_PROP_NAME = "gw.act";

	/**
	 * Configuration property specifying the number of threads to use for
	 * multi-threaded module processing.
	 * <p>
	 * The value must be a positive integer; non-positive values are rejected.
	 *
	 * @see AbstractFileProcessor#setThreads(int)
	 */
	public static final String THREADS_PROP_NAME = "gw.threads";

	/**
	 * Configuration property specifying the file, directory, or path pattern used
	 * to determine which files are scanned within the project directory.
	 * <p>
	 * The value may be an absolute or relative path to a single file, an absolute
	 * or relative path to a directory, a glob pattern (e.g.,
	 * {@code "glob:**&#47;*.java"}), or a regex pattern (e.g.,
	 * {@code "regex:.*\\.java"}).
	 *
	 * @see AIFileProcessor#scanDocuments(java.io.File, String)
	 */
	public static final String PATH_PROP_NAME = "gw.path";

	/** Configuration property controlling recursive module traversal. */
	public static final String NONRECURSIVE_PROP_NAME = "gw.nonRecursive";

	/** Configuration property controlling interactive mode. */
	public static final String INTERACTIVE_MODE_PROP_NAME = "gw.interactive";

	/** Line continuation marker used for multi-line console input. */
	public static final char MULTIPLE_LINES_BREAKER = '\\';

	public static final int LOG_LINE_LENGTH = 73;

}
