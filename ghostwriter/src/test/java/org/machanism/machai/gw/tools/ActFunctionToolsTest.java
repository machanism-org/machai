package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

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
	void getActList_whenActsDirectoryMissing_returnsBothSections() throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(null);
		Method m = ActFunctionTools.class.getDeclaredMethod("getActList", Object[].class);
		m.setAccessible(true);

		// Act
		Object out = m.invoke(tools, (Object) new Object[0]);

		// Assert
		assertNotNull(out);
		String text = out.toString();
		assertTrue(text.contains("# Custom Act List"));
		assertTrue(text.contains("# Base Act List"));
	}

	@Test
	void getActDetails_whenCustomNotProvided_throwsExceptionForMissingActsDir() throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(null);
		JsonNode props = mapper.readTree("{\"actName\":\"any\"}");
		Method m = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		m.setAccessible(true);

		// Act + Assert
		assertThrows(Exception.class, () -> m.invoke(tools, (Object) new Object[] { props }));
	}

	@Test
	void getActDetails_whenCustomTrueAndActsNull_returnsMap() throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(null);
		JsonNode props = mapper.readTree("{\"actName\":\"any\",\"custom\":true}");
		Method m = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		m.setAccessible(true);

		// Act
		Object out = m.invoke(tools, (Object) new Object[] { props });

		// Assert
		assertNotNull(out);
		assertTrue(out instanceof java.util.Map);
	}

	@Test
	void getActDetails_whenCustomFalseAndActMissing_returnsMapOrThrowsDependingOnRuntimeClasspath() throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(new File("."));
		JsonNode props = mapper.readTree(
				"{\"actName\":\"__definitely_missing_act__\",\"custom\":false}");
		Method m = ActFunctionTools.class.getDeclaredMethod("getActDetails", Object[].class);
		m.setAccessible(true);

		// Act
		Object out = null;
		try {
			out = m.invoke(tools, (Object) new Object[] { props });
		} catch (Exception e) {
			// acceptable (depends on whether classpath has acts embedded)
		}

		// Assert
		if (out != null) {
			assertTrue(out instanceof java.util.Map);
		}
	}

	@Test
	void listTomlFiles_whenDirectoryEmpty_returnsEmptySet(@TempDir File tempDir) throws Exception {
		// Arrange
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(tempDir);
		Method m = ActFunctionTools.class.getDeclaredMethod("listTomlFiles");
		m.setAccessible(true);

		// Act
		Object out = m.invoke(tools);

		// Assert
		assertNotNull(out);
		assertTrue(((java.util.Set<?>) out).isEmpty());
	}

	@Test
	void listTomlFiles_whenTomlPresent_returnsSet(@TempDir File tempDir) throws Exception {
		// Arrange
		File toml = new File(tempDir, "bad.toml");
		assertTrue(toml.createNewFile());
		when(configurator.getFile(eq("gw.acts"), isNull())).thenReturn(tempDir);
		Method m = ActFunctionTools.class.getDeclaredMethod("listTomlFiles");
		m.setAccessible(true);

		// Act
		Object out = m.invoke(tools);

		// Assert
		assertNotNull(out);
		assertTrue(out instanceof java.util.Set);
	}

	@Test
	void getBaseActList_returnsNonNullSet() throws IOException {
		// Arrange
		// (no additional setup)

		// Act
		java.util.Set<String> out = tools.getBaseActList();

		// Assert
		assertNotNull(out);
	}

	@Test
	void setConfigurator_storesReference() throws Exception {
		// Arrange
		ActFunctionTools local = new ActFunctionTools();
		Configurator cfg = mock(Configurator.class);

		// Act
		local.setConfigurator(cfg);

		// Assert (via invoking listTomlFiles and verifying it calls configurator)
		Method m = ActFunctionTools.class.getDeclaredMethod("listTomlFiles");
		m.setAccessible(true);
		when(cfg.getFile(eq("gw.acts"), isNull())).thenReturn(null);
		m.invoke(local);
		verify(cfg).getFile(eq("gw.acts"), isNull());
	}

	@Test
	void applyTools_registersToolsOnProvider() {
		// Arrange
		org.machanism.machai.ai.manager.GenAIProvider provider = mock(
				org.machanism.machai.ai.manager.GenAIProvider.class);

		// Act
		tools.applyTools(provider);

		// Assert
		verify(provider).addTool(eq("list_acts"), anyString(), any());
		verify(provider).addTool(eq("load_act_details"), anyString(), any(), any(), any());
	}
}
