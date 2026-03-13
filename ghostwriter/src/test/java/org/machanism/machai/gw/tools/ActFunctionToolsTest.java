package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ActFunctionToolsTest {

	private final ObjectMapper mapper = new ObjectMapper();

	private ActFunctionTools tools;
	private Configurator configurator;

	@BeforeEach
	void setUp() {
		tools = new ActFunctionTools();
		configurator = mock(Configurator.class);
		tools.setConfigurator(configurator);
	}

	@Test
	void applyTools_registersExpectedTools() {
		// Arrange
		org.machanism.machai.ai.manager.GenAIProvider provider = mock(
				org.machanism.machai.ai.manager.GenAIProvider.class);

		// Act
		tools.applyTools(provider);

		// Assert
		verify(provider).addTool(eq("list_acts"), anyString(), any());
		verify(provider).addTool(eq("load_act_details"), anyString(), any(), any(), any());
		verifyNoMoreInteractions(provider);
	}

	@Test
	void listTomlFiles_whenActsPathNull_returnsEmptySet() throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(null);
		Method m = ActFunctionTools.class.getDeclaredMethod("listTomlFiles");
		m.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		Set<String> out = (Set<String>) m.invoke(tools);

		// Assert
		assertNotNull(out);
		assertTrue(out.isEmpty());
	}

	@Test
	void listTomlFiles_whenActsPathDoesNotExist_returnsEmptySet(@TempDir File tempDir) throws Exception {
		// Arrange
		File missingDir = new File(tempDir, "missing");
		assertFalse(missingDir.exists());
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(missingDir);
		Method m = ActFunctionTools.class.getDeclaredMethod("listTomlFiles");
		m.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		Set<String> out = (Set<String>) m.invoke(tools);

		// Assert
		assertNotNull(out);
		assertTrue(out.isEmpty());
	}

	@Test
	void listTomlFiles_whenActsPathIsNotDirectory_returnsEmptySet(@TempDir File tempDir) throws Exception {
		// Arrange
		File actsFile = new File(tempDir, "acts.txt");
		Files.write(actsFile.toPath(), "x".getBytes(StandardCharsets.UTF_8));
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(actsFile);
		Method m = ActFunctionTools.class.getDeclaredMethod("listTomlFiles");
		m.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		Set<String> out = (Set<String>) m.invoke(tools);

		// Assert
		assertNotNull(out);
		assertTrue(out.isEmpty());
	}

	@Test
	void listTomlFiles_whenDirectoryEmpty_returnsEmptySet(@TempDir File tempDir) throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(tempDir);
		Method m = ActFunctionTools.class.getDeclaredMethod("listTomlFiles");
		m.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		Set<String> out = (Set<String>) m.invoke(tools);

		// Assert
		assertNotNull(out);
		assertTrue(out.isEmpty());
	}

	@Test
	void listTomlFiles_whenTomlPresent_returnsSet(@TempDir File tempDir) throws Exception {
		// Arrange
		File toml = new File(tempDir, "maybe.toml");
		Files.write(toml.toPath(), "description=\"x\"".getBytes(StandardCharsets.UTF_8));
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(tempDir);
		Method m = ActFunctionTools.class.getDeclaredMethod("listTomlFiles");
		m.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		Set<String> out = (Set<String>) m.invoke(tools);

		// Assert
		assertNotNull(out);
		assertFalse(out.isEmpty());
		assertTrue(out.iterator().next().contains("maybe"));
	}

	@Test
	void getActList_whenActsDirectoryMissing_includesBothSections() throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(null);
		Method m = ActFunctionTools.class.getDeclaredMethod("getActList", Object[].class);
		m.setAccessible(true);

		// Act
		Object out = m.invoke(tools, (Object) new Object[0]);

		// Assert
		assertNotNull(out);
		String text = out.toString();
		assertAll(
				() -> assertTrue(text.contains("# Custom Act List")),
				() -> assertTrue(text.contains("# Base Act List")));
	}

	@Test
	void getActDetails_whenCustomNotProvided_andActMissing_throwsFromActProcessor() throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(null);
		JsonNode props = mapper.readTree("{\"actName\":\"__missing__\"}");
		Method m = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		m.setAccessible(true);

		// Act
		InvocationTargetException ex = assertThrows(InvocationTargetException.class,
				() -> m.invoke(tools, (Object) new Object[] { props }));

		// Assert
		assertNotNull(ex.getCause());
	}

	@Test
	void getActDetails_whenCustomTrue_returnsMap() throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(null);
		JsonNode props = mapper.readTree("{\"actName\":\"__missing__\",\"custom\":true}");
		Method m = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		m.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		Map<String, Object> out = (Map<String, Object>) m.invoke(tools, (Object) new Object[] { props });

		// Assert
		assertNotNull(out);
	}

	@Test
	void getActDetails_whenCustomFalse_returnsMap() throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(new File("."));
		JsonNode props = mapper
				.readTree("{\"actName\":\"__definitely_missing_act__\",\"custom\":false}");
		Method m = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		m.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		Map<String, Object> out = (Map<String, Object>) m.invoke(tools, (Object) new Object[] { props });

		// Assert
		assertNotNull(out);
	}

	@Test
	void getBaseActList_returnsNonNullSet() throws Exception {
		// Arrange

		// Act
		Set<String> out = tools.getBaseActList();

		// Assert
		assertNotNull(out);
	}

	@Test
	void getActDetails_whenBuiltInActExists_returnsPropertiesMap() throws Exception {
		// Arrange
		Set<String> baseActs = tools.getBaseActList();
		if (baseActs.isEmpty()) {
			return;
		}
		String firstAct = baseActs.iterator().next();
		int start = firstAct.indexOf('`') + 1;
		int end = firstAct.indexOf('`', start);
		String actName = firstAct.substring(start, end);

		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(new File("."));
		JsonNode props = mapper.readTree("{\"actName\":\"" + actName + "\",\"custom\":false}");
		Method m = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		m.setAccessible(true);

		// Act
		@SuppressWarnings("unchecked")
		Map<String, Object> out = (Map<String, Object>) m.invoke(tools, (Object) new Object[] { props });

		// Assert
		assertNotNull(out);
		assertTrue(out.containsKey("description"));
	}
}
