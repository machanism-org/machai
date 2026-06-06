package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class ProcessorPackageCoverageTest {

	@Test
	void actNotFound_exposesNameAndMessage() {
		// Arrange
		ActNotFound exception = new ActNotFound("missing-act");

		// Act + Assert
		assertEquals("missing-act", exception.getName());
		assertEquals("Act: `missing-act` not found.", exception.getMessage());
	}

	@Test
	void episodeNotFoundException_usesEpisodeNameAsMessage() {
		// Arrange
		EpisodeNotFoundException exception = new EpisodeNotFoundException("Episode A");

		// Act + Assert
		assertEquals("Episode A", exception.getMessage());
	}

	@Test
	void gwConstants_exposesExpectedValues() throws Exception {
		// Arrange
		GWConstants constants = instantiatePrivateClass(GWConstants.class);

		// Act + Assert
		assertNotNull(constants);
		assertEquals("project.dir", GWConstants.PROJECT_DIR_PROP_NAME);
		assertEquals("gw.properties", GWConstants.GW_PROPERTIES_FILE_NAME);
		assertEquals("gw.config", GWConstants.CONFIG_PROP_NAME);
		assertEquals("gw.home", GWConstants.HOME_PROP_NAME);
		assertEquals("gw.model", GWConstants.MODEL_PROP_NAME);
		assertEquals("gw.instructions", GWConstants.INSTRUCTIONS_PROP_NAME);
		assertEquals("gw.excludes", GWConstants.EXCLUDES_PROP_NAME);
		assertEquals("gw.acts", GWConstants.ACTS_LOCATION_PROP_NAME);
		assertEquals("gw.act", GWConstants.ACT_PROP_NAME);
		assertEquals("gw.threads", GWConstants.THREADS_PROP_NAME);
		assertEquals("gw.paths", GWConstants.SCAN_DIR_PROP_NAME);
		assertEquals("gw.nonRecursive", GWConstants.NONRECURSIVE_PROP_NAME);
		assertEquals("inputs", GWConstants.INPUTS_PROPERTY_NAME);
		assertEquals("gw.interactive", GWConstants.INTERACTIVE_MODE_PROP_NAME);
		assertEquals("\\", GWConstants.MULTIPLE_LINES_BREAKER);
		assertEquals(60, GWConstants.LOG_PROMPT_MAX_LENGTH);
	}

	@Test
	void episodes_supportSelectionNamingFlowAndMetadata() {
		// Arrange
		Episodes episodes = new Episodes();
		episodes.setName("sample-act");
		episodes.setEpisodes(java.util.Arrays.asList("# Start\nLine", "# Continue\nMore"));

		// Act + Assert
		assertEquals(2, episodes.size());
		assertEquals("Start", episodes.getEpisodeName(1));
		assertEquals("Continue", episodes.getEpisodeName(2));
		assertEquals(2, episodes.getEpisodeIdByName("Continue"));
		assertTrue(episodes.isRegularOrder());
		episodes.setSelectedEpisodes(Collections.singletonList(1));
		assertFalse(episodes.isRegularOrder());
		String episodeInformation = episodes.getEpisodeInformation(1);
		assertTrue(episodeInformation.contains("\"ACT_NAME\" : \"sample-act\""));
		assertTrue(episodeInformation.contains("\"EPISODE_NAME\" : \"Continue\""));
	}

	@Test
	void ghostwriter_privateHelpers_canBeInvokedReflectively() throws Exception {
		// Arrange
		Ghostwriter ghostwriter = instantiatePrivateClass(Ghostwriter.class);
		Method createOptions = Ghostwriter.class.getDeclaredMethod("createOptions");
		Method handleExitCode = Ghostwriter.class.getDeclaredMethod("handleExitCode", int.class);
		Method handleProcessingFailure = Ghostwriter.class.getDeclaredMethod("handleProcessingFailure", String.class,
				Exception.class);
		Method logAbbreviatedValue = Ghostwriter.class.getDeclaredMethod("logAbbreviatedValue", String.class,
				String.class);
		Method appendContinuedLine = Ghostwriter.class.getDeclaredMethod("appendContinuedLine", StringBuilder.class,
				String.class);
		Method formatConsole = Ghostwriter.class.getDeclaredMethod("formatConsole", java.io.Console.class,
				String.class);
		createOptions.setAccessible(true);
		handleExitCode.setAccessible(true);
		handleProcessingFailure.setAccessible(true);
		logAbbreviatedValue.setAccessible(true);
		appendContinuedLine.setAccessible(true);
		formatConsole.setAccessible(true);

		// Act
		Object options = createOptions.invoke(null);
		StringBuilder sb = new StringBuilder();
		appendContinuedLine.invoke(null, sb, "line\\");
		formatConsole.invoke(null, null, "ignored");
		logAbbreviatedValue.invoke(null, "Label", String.join("", Collections.nCopies(100, "x")));
		int exitCode = (Integer) handleProcessingFailure.invoke(null, "Error", new IllegalArgumentException("boom"));
		handleExitCode.invoke(null, 0);

		// Assert
		assertNotNull(ghostwriter);
		assertNotNull(options);
		assertEquals("line\n", sb.toString());
		assertEquals(1, exitCode);
		assertEquals(">>>", Ghostwriter.USER_INPUT_PREFIX);
	}

	private static <T> T instantiatePrivateClass(Class<T> type) throws Exception {
		java.lang.reflect.Constructor<T> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();
	}
}
