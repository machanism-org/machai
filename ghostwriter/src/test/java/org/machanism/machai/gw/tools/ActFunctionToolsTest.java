package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ActFunctionToolsTest {

	@TempDir
	Path tempDir;

	@Test
	void getActDetails_whenCustomTrue_thenLoadsFromActsDirectory() throws Exception {
		// Arrange
		Path actsDir = tempDir.resolve("acts");
		Files.createDirectories(actsDir);
		Path actFile = actsDir.resolve("myact.toml");
		Files.write(actFile, Arrays.asList("description='My act'"), StandardCharsets.UTF_8);

		PropertiesConfigurator configurator = new PropertiesConfigurator();
		configurator.set("gw.acts", actsDir.toFile().getAbsolutePath());

		ActFunctionTools tools = new ActFunctionTools();
		tools.setConfigurator(configurator);

		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("actName", "myact");
		node.put("custom", "true");

		// Act
		Object result = invokeGetActDetails(tools, node);

		// Assert
		assertTrue(result instanceof Map);
		Map<?, ?> map = (Map<?, ?>) result;
		assertEquals("My act", map.get("description"));
	}

	@Test
	void getActDetails_whenCustomMissing_thenLoadsEffectiveAndReturnsMap() throws Exception {
		// Arrange
		ActFunctionTools tools = new ActFunctionTools();
		PropertiesConfigurator configurator = new PropertiesConfigurator();
		// Let built-in acts resolve (help should exist in resources)
		tools.setConfigurator(configurator);

		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("actName", "help");

		// Act
		Object result = invokeGetActDetails(tools, node);

		// Assert
		assertTrue(result instanceof Map);
		Map<?, ?> map = (Map<?, ?>) result;
		assertFalse(map.isEmpty(), "Expected some properties from built-in 'help' act");
	}

	@Test
	void getBaseActList_whenRunningFromClassesDir_thenReturnsEmptyAndDoesNotThrow() throws IOException {
		// Arrange
		ActFunctionTools tools = new ActFunctionTools();
		tools.setConfigurator(new PropertiesConfigurator());

		// Act
		Set<String> acts = tools.getBaseActList();

		// Assert
		assertNotNull(acts);
		assertTrue(acts.isEmpty(), "Expected empty set when not running from a jar/zip");
	}

	@Test
	void getActDetails_whenConfiguratorNotSet_thenThrowsNullPointerExceptionAsCause() throws Exception {
		// Arrange
		ActFunctionTools tools = new ActFunctionTools();
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("actName", "help");

		// Act
		InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> invokeGetActDetails(tools, node));

		// Assert
		assertInstanceOf(NullPointerException.class, ex.getCause());
	}

	// Sonar java:S1130 - remove declaration of overly broad checked exception.
	private static Object invokeGetActDetails(ActFunctionTools tools, ObjectNode json)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		java.lang.reflect.Method method = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		method.setAccessible(true);
		return method.invoke(tools, new Object[] { new Object[] { json } });
	}
}
