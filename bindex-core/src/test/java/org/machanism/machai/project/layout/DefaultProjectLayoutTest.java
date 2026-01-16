package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

class DefaultProjectLayoutTest {

	@Test
	void projectDir_returnsSubtypeForChaining() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout();
		File dir = new File("src/test/resources/mockDefaultProject");

		// Act
		DefaultProjectLayout returned = layout.projectDir(dir);

		// Assert
		assertEquals(layout, returned);
		assertEquals(dir, layout.getProjectDir());
	}

	@Test
	void getSources_returnsNull() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout();

		// Act
		List<String> sources = layout.getSources();

		// Assert
		assertNull(sources);
	}

	@Test
	void getDocuments_returnsNull() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout();

		// Act
		List<String> docs = layout.getDocuments();

		// Assert
		assertNull(docs);
	}

	@Test
	void getTests_returnsNull() {
		// Arrange
		DefaultProjectLayout layout = new DefaultProjectLayout();

		// Act
		List<String> tests = layout.getTests();

		// Assert
		assertNull(tests);
	}
}
