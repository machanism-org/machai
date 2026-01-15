package org.machanism.machai.project;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.*;
import java.io.File;
import java.io.FileNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

class ProjectLayoutManagerTest {

	@Test
	void testDetectProjectLayoutForMaven() throws FileNotFoundException {
		// Arrange
		File mavenProjectDir = new File("src/test/resources/mockMavenProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(mavenProjectDir);

		// Assert
		assertNotNull(layout);
		assertTrue(layout instanceof MavenProjectLayout);
	}

	@Test
	void testDetectProjectLayoutForJScript() throws FileNotFoundException {
		// Arrange
		File jsProjectDir = new File("src/test/resources/mockJsProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(jsProjectDir);

		// Assert
		assertNotNull(layout);
		assertTrue(layout instanceof JScriptProjectLayout);
	}

	@Test
	@Disabled
	void testDetectProjectLayoutForPython() throws FileNotFoundException {
		// Arrange
		File pythonProjectDir = new File("src/test/resources/mockPythonProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(pythonProjectDir);

		// Assert
		assertNotNull(layout);
		assertTrue(layout instanceof PythonProjectLayout);
	}

	@Test
	void testDetectProjectLayoutForDefault() throws FileNotFoundException {
		// Arrange
		File defaultProjectDir = new File("src/test/resources/mockDefaultProject");

		// Act
		ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(defaultProjectDir);

		// Assert
		assertNotNull(layout);
		assertTrue(layout instanceof DefaultProjectLayout);
	}

	@Test
	void testDetectProjectLayoutThrowsFileNotFoundException() {
		// Arrange
		File nonExistentDir = new File("src/test/resources/nonExistentDir");

		// Act & Assert
		assertThrows(FileNotFoundException.class, () -> {
			ProjectLayoutManager.detectProjectLayout(nonExistentDir);
		});
	}
}