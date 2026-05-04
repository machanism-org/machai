package org.machanism.machai.gw.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.FunctionToolsLoader;
import org.machanism.machai.gw.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIFileProcessor extends AbstractFileProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AIFileProcessor.class);

	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	public static final String NOT_DEFINED_VALUE = "<NOT_DEFINED_VALUE>";

	private static final String EMPTY_VALUE = "<EMPTY>";

	public static final String GW_TEMP_DIR = "docs-inputs";

	private String model;

	private boolean logInputs;

	private String instructions = "You are a highly skilled software engineer and developer, with expertise in all major programming languages, frameworks, and platforms.";

	private String defaultPrompt;

	private boolean interactive;

	private List<FunctionTools> toolFunctions = new ArrayList<>();

	public AIFileProcessor(File projectDir, Configurator configurator, String genai) {
		super(projectDir, configurator);
		this.model = genai;
	}

	public String process(ProjectLayout projectLayout, File file, String prompt) {
		return process(projectLayout, file, getInstructions(), prompt);
	}

	protected String process(ProjectLayout projectLayout, File file, String instructions, String prompt) {
		logger.info("Processing path: `{}`", file);
		String perform = null;
		if (StringUtils.isNoneBlank(prompt)) {
			try {
				Genai provider = GenaiProviderManager.getProvider(getModel(), getConfigurator());
				FunctionToolsLoader.getInstance().applyTools(provider);
				toolFunctions.forEach(ft -> ft.applyTools(provider));

				File projectDir = projectLayout.getProjectDir();
				provider.setWorkingDir(projectDir);

				String sysInstructions = promptBundle.getString("sys_instructions");
				String finalInstructions = String.format(sysInstructions, instructions);

				provider.instructions(finalInstructions);

				String projectInfo = getProjectStructureDescription(projectLayout, file);
				provider.prompt(projectInfo);

				String promptLines = parseLines(prompt);
				provider.prompt(promptLines);

				perform = perform(file, provider);

			} catch (ProcessTerminationException e) {
				if (e.getExitCode() != 0) {
					throw e;
				}
				perform = e.getMessage();

			} finally {
				logger.info("Finished processing path: {}", file.getAbsolutePath());

			}
		} else {
			logger.info("Received an empty prompt. Skipping processing.");
		}
		return perform;
	}

	private String perform(File file, Genai provider) {
		if (isLogInputs()) {
			String inputsFileName = ProjectLayout.getRelativePath(getProjectDir(), file);
			File docsTempDir = new File(getProjectDir(), MACHAI_TEMP_DIR + File.separator + GW_TEMP_DIR);
			File inputsFile = new File(docsTempDir, inputsFileName + ".txt");
			File parentDir = inputsFile.getParentFile();
			if (parentDir != null) {
				try {
					Files.createDirectories(parentDir.toPath());
				} catch (Exception e) {
					throw new IllegalStateException("Failed to create inputs log directory: " + parentDir, e);
				}
			}
			provider.inputsLog(inputsFile);
		}

		String perform = provider.perform();
		if (perform != null && interactive) {
			logger.info(">>>: {}", perform);
			String input = input();
			if (input != null) {
				if (!Strings.CS.equals(input.toLowerCase().trim(), "exit")) {
					provider.prompt(input);
					perform = perform(file, provider);
				} else {
					perform = null;
				}
			}
		}
		return perform;
	}

	protected String input() {
		return null;
	}

	public String getProjectStructureDescription(ProjectLayout projectLayout, File file) {
		List<String> content = new ArrayList<>();

		File projectDir = projectLayout.getProjectDir();
		String parentId = projectLayout.getParentId();
		File parentDir = projectLayout.getProjectDir().getParentFile();

		Collection<String> sources = projectLayout.getSources();
		Collection<String> tests = projectLayout.getTests();
		Collection<String> documents = projectLayout.getDocuments();
		Collection<String> modules = projectLayout.getModules();

		content.add(SystemUtils.OS_NAME);
		content.add(projectLayout.getProjectName() != null ? projectLayout.getProjectName() : NOT_DEFINED_VALUE);
		content.add(projectLayout.getProjectId());
		content.add(projectDir.getName());
		content.add(Objects.toString(parentId, NOT_DEFINED_VALUE));
		content.add(parentDir != null ? parentDir.getName() : NOT_DEFINED_VALUE);

		String relativePath = ProjectLayout.getRelativePath(getProjectDir(), projectDir);
		content.add(relativePath);

		content.add(projectLayout.getProjectLayoutType());
		content.add(getDirInfoLine(sources, projectDir));
		content.add(getDirInfoLine(tests, projectDir));
		content.add(getDirInfoLine(documents, projectDir));
		content.add(getDirInfoLine(modules, projectDir));

		String relativeFile = ProjectLayout.getRelativePath(projectDir, file);
		content.add(relativeFile);

		if (!interactive) {
			content.add(
					"- This is an automated process.\n- Do not include explanations or any additional output.\n");
		} else {
			content.add("- This is an interactive process.\n"
					+ "- If the task is completed successfully, call the `terminate_process` function with exit code = 0.");
		}

		Object[] array = content.toArray(new String[0]);
		String projectInformation = promptBundle.getString("project_information");
		projectInformation = String.format(projectInformation, array);
		return projectInformation + Genai.LINE_SEPARATOR;
	}

	String getDirInfoLine(Collection<String> sources, File projectDir) {
		String line = null;
		if (sources != null) {
			if (!sources.isEmpty()) {
				List<String> dirs = sources.stream().filter(t -> t != null && new File(projectDir, t).exists())
						.map(e -> "`" + e + "`").collect(Collectors.toList());
				line = StringUtils.join(dirs, ", ");
			} else {
				line = EMPTY_VALUE;
			}
		}

		if (StringUtils.isBlank(line)) {
			line = NOT_DEFINED_VALUE;
		}
		return line;
	}

	public boolean isLogInputs() {
		return logInputs;
	}

	public void setLogInputs(boolean logInputs) {
		this.logInputs = logInputs;
	}

	public void setInstructions(String instructions) {
		this.instructions = parseLines(instructions);
	}

	public String getInstructions() {
		return instructions;
	}

	public String parseLines(String data) {
		if (data == null) {
			return StringUtils.EMPTY;
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new StringReader(data))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String normalizedLine = StringUtils.stripToNull(line);
				if (normalizedLine == null) {
					sb.append(Genai.LINE_SEPARATOR);
					continue;
				}

				String content = tryToGetInstructionsFromReference(normalizedLine);
				if (content != null) {
					sb.append(content);
				}
				sb.append(Genai.LINE_SEPARATOR);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		return sb.toString();
	}

	String tryToGetInstructionsFromReference(String data) throws java.io.IOException {
		if (data == null) {
			return null;
		}

		String trimmed = data.trim();
		if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
			return parseLines(readFromHttpUrl(trimmed));
		}

		if (Strings.CS.startsWith(trimmed, "file:")) {
			String filePath = StringUtils.substringAfter(trimmed, "file:");
			filePath = StringSubstitutor.replaceSystemProperties(filePath);
			return parseLines(readFromFilePath(filePath));
		}

		return data;
	}

	static String readFromHttpUrl(String urlString) throws java.io.IOException {
		URL url = URI.create(urlString).toURL();
		try (InputStream in = url.openStream()) {
			String result = IOUtils.toString(in, StandardCharsets.UTF_8);
			logger.info("Included: `{}`", urlString);
			return result;
		}
	}

	String readFromFilePath(String filePath) {
		File file = new File(filePath);
		if (!file.isAbsolute()) {
			file = new File(getProjectDir(), filePath);
		}

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			String result = IOUtils.toString(reader);
			logger.info("Included file: `{}`", file);
			return result;
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException("Failed to read file: " + file.getAbsolutePath() + ", Error: "
					+ e.getMessage(), e);
		}
	}

	public void scanDocuments(File projectDir, String scanDir) throws java.io.IOException {
		FunctionToolsLoader.getInstance().setConfiguration(getConfigurator());

		if (projectDir == null) {
			throw new IllegalArgumentException("projectDir must not be null");
		}
		if (StringUtils.isBlank(scanDir)) {
			throw new IllegalArgumentException("scanDir must not be blank");
		}

		if (!Strings.CS.equals(projectDir.getAbsolutePath(), scanDir)) {
			if (!isPathPattern(scanDir)) {
				scanDir = parseScanDir(projectDir, scanDir);
			}
			super.setPathMatcher(FileSystems.getDefault().getPathMatcher(scanDir));
		} else {
			setScanDir(projectDir);
		}

		scanFolder(projectDir);
	}

	String parseScanDir(File projectDir, String scanDir) {
		File scanDirFile = new File(scanDir);
		if (!scanDirFile.isAbsolute()) {
			if (".".equals(scanDir)) {
				scanDirFile = getProjectDir();
			} else {
				scanDirFile = new File(getProjectDir(), scanDir);
			}
		}
		String relativePath = ProjectLayout.getRelativePath(projectDir, scanDirFile);
		if (relativePath == null) {
			relativePath = ".";
			scanDirFile = getProjectDir();
		}
		super.setScanDir(scanDirFile);

		if (getDefaultPrompt() == null) {
			scanDir = "glob:" + relativePath + "{,/**}";
		} else {
			scanDir = "glob:" + relativePath;
		}
		return scanDir;
	}

	public String getDefaultPrompt() {
		return defaultPrompt;
	}

	public void setDefaultPrompt(String defaultPrompt) {
		this.defaultPrompt = defaultPrompt;
	}

	@Override
	public void processFolder(ProjectLayout projectLayout) {
		try {
			String perform = process(projectLayout, projectLayout.getProjectDir(), getDefaultPrompt());
			if (perform != null) {
				logger.info(">>> {}", perform);
			}

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public String getModel() {
		return model;
	}

	public void setModel(String genai) {
		this.model = genai;
	}

	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	public boolean isInteractive() {
		return interactive;
	}

	/**
	 * FalsePositive Backward-compatible alias kept for configuration/property
	 * naming; delegating to getModel() is intentional.
	 */
	@SuppressWarnings("java:S4144")
	public String getProvider() {
		return String.valueOf(getModel());
	}

	/**
	 * FalsePositive Backward-compatible alias kept for configuration/property
	 * naming; delegating to setModel() is intentional.
	 */
	@SuppressWarnings("java:S4144")
	public void setProvider(String genai) {
		setModel(genai);
	}

	public void addTool(FunctionTools toolFunction) {
		logger.debug("FunctionTools: {}", toolFunction.getClass().getName());
		toolFunctions.add(toolFunction);
	}
}
