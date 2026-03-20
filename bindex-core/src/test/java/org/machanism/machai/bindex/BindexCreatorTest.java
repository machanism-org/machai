package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class BindexCreatorTest {

	@Test
	void constructor_throwsOnNullGenai() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new BindexCreator(null, config));

		// Assert
		assertEquals("model must not be null", ex.getMessage());
	}

	@Test
	void constructor_throwsOnNullConfig() {
		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new BindexCreator("openai", null));

		// Assert
		assertEquals("config must not be null", ex.getMessage());
	}

	@Test
	void processFolder_throwsOnNullProjectLayout() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);
		BindexCreator creator = new BindexCreator("openai", config);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> creator.processFolder(null));

		// Assert
		assertEquals("projectLayout must not be null", ex.getMessage());
	}
}
