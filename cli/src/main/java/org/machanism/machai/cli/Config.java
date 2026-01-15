package org.machanism.machai.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Config {

	private static final String CONF_PROPERTIES_FILE_NAME = "machai.properties";
	private static final String SCORE_PRP_NAME = "score";
	private static final String WORKINGDIR_PROP_NAME = "dir";
	private static final String GENAI_PROP_NAME = "genai";

	private static Logger logger = LoggerFactory.getLogger(Config.class);

	private static String chatModel;
	private static File workingDir;
	private static double score;

	private static Properties properties = new Properties();

	static {
		try {
			loadSystemProperties();
			chatModel = properties.getProperty(GENAI_PROP_NAME, "OpenAI:gpt-5-mini");
			workingDir = new File(
					properties.getProperty(WORKINGDIR_PROP_NAME, SystemUtils.getUserDir().getAbsolutePath()));
			score = Double.parseDouble(properties.getProperty(SCORE_PRP_NAME, "0.9"));

		} catch (IOException e) {
			logger.debug("Configuration properties not found.");
		}
	}

	private Config() {
	}

	protected static void setDefaultChatModel(String chatModel) {
		if (chatModel != null) {
			Config.chatModel = chatModel;
			save(GENAI_PROP_NAME, chatModel);
		} else {
			logger.info("Default GenAI Service: {}", Config.chatModel);
		}
	}

	public static String getChatModel(String chatModel) {
		return chatModel != null ? chatModel : Config.chatModel;
	}

	public static void setWorkingDir(File dir) {
		if (dir == null) {
			logger.info("Working directory path: {}", workingDir.getAbsolutePath());
		} else {
			workingDir = dir;
			save(WORKINGDIR_PROP_NAME, workingDir.getAbsolutePath());
			dir.mkdirs();
		}
	}

	private static void save(String name, String value) {
		properties.put(name, value);
		File conf = getConfFile();
		try (OutputStream stream = new FileOutputStream(conf)) {
			properties.store(stream, null);
		} catch (IOException e) {
			logger.error("Failed to save the configuration properties file.");
		}
	}

	public static File getWorkingDir(File dir) {
		return dir != null ? dir : workingDir;
	}

	public static void setScore(Double score) {
		if (score != null) {
			save(SCORE_PRP_NAME, String.valueOf(score));
			Config.score = score;
		} else {
			logger.info("Default minimum score for semantic search: {}", Config.score);
		}
	}

	public static double getScore(Double score) {
		return score != null ? score : Config.score;
	}

	private static void loadSystemProperties() throws IOException {
		File conf = getConfFile();
		if (conf.exists()) {
			try (FileInputStream propFile = new FileInputStream(conf)) {
				properties.load(propFile);
			}
		}
	}

	private static File getConfFile() {
		String configFile = System.getProperty("config");
		
		File conf;
		if (configFile != null) {
			conf = new File(configFile);
		} else {
			conf = new File(CONF_PROPERTIES_FILE_NAME);
		}
		return conf;
	}
}
