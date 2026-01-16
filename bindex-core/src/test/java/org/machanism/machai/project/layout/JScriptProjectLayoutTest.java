package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JScriptProjectLayoutTest {

	@Test
	void isPackageJsonPresent_whenPresent_returnsTrue() {
		// Arrange
		File projectDir = new File("src/test/resources/mockJsProject");

		// Act
		boolean present = JScriptProjectLayout.isPackageJsonPresent(projectDir);

		// Assert
		assertTrue(present);
	}

	@Test
	@Disabled
	void getModules_whenWorkspacesArray_collectsWorkspaceModuleDirectories() throws IOException {
		// Arrange
		File projectDir = new File("src/test/resources/mockJsProject");
		JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(projectDir);

		// Act
		List<String> modules = layout.getModules();

		// Assert
		assertNotNull(modules);
		assertEquals(1, modules.size());
		assertEquals("workspaceA", modules.get(0));
	}

	@Test
	void getSources_returnsNull() {
		// Arrange
		JScriptProjectLayout layout = new JScriptProjectLayout();

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertNull(sources);
	}

	@Test
	void getDocuments_returnsNull() {
		// Arrange
		JScriptProjectLayout layout = new JScriptProjectLayout();

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertNull(docs);
	}

	@Test
	void getTests_returnsNull() {
		// Arrange
		JScriptProjectLayout layout = new JScriptProjectLayout();

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertNull(tests);
	}
}
