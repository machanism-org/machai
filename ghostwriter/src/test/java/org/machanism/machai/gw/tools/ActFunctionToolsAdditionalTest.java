package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ActFunctionToolsAdditionalTest {

	@TempDir
	Path tempDir;

	@Test
	void getActDetails_whenCustomFalse_thenLoadsFromClasspathEvenIfCustomExists() throws Exception {
		// Arrange
		Path actsDir = tempDir.resolve("acts");
		Files.createDirectories(actsDir);
		Files.write(actsDir.resolve("help.toml"), "description='CUSTOM HELP'\n".getBytes(StandardCharsets.UTF_8));

		PropertiesConfigurator configurator = new PropertiesConfigurator();
		configurator.set("gw.acts", actsDir.toFile().getAbsolutePath());

		ActFunctionTools tools = new ActFunctionTools();
		tools.setConfigurator(configurator);

		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("actName", "help");
		node.put("custom", "false");

		// Act
		Object result = invokeGetActDetails(tools, node);

		// Assert
		assertInstanceOf(Map.class, result);
		Map<?, ?> map = (Map<?, ?>) result;
		assertNotEquals("CUSTOM HELP", map.get("description"), "Expected classpath act, not custom override");
		assertFalse(map.isEmpty());
	}

	@Test
	void getBaseActList_whenNotJarOrZip_thenReturnsEmpty() throws IOException {
		// Arrange
		ActFunctionTools tools = new ActFunctionTools();
		tools.setConfigurator(new PropertiesConfigurator());

		// Act
		assertTrue(tools.getBaseActList().isEmpty());
	}

	private static Object invokeGetActDetails(ActFunctionTools tools, ObjectNode json)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		java.lang.reflect.Method method = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		method.setAccessible(true);
		return method.invoke(tools, new Object[] { new Object[] { json } });
	}
}
