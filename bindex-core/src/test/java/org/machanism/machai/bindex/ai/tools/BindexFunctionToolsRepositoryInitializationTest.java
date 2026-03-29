package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.BindexRepository;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class BindexFunctionToolsRepositoryInitializationTest {

	@Test
	void getBindexRepository_shouldThrowWhenConfiguratorNotSet() throws Exception {
		// Arrange
		BindexFunctionTools tools = new BindexFunctionTools();
		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindexRepository");
		method.setAccessible(true);

		// Act + Assert
		assertThrows(Exception.class, () -> method.invoke(tools));
	}

	@Test
	void getBindexRepository_shouldLazilyCreateAndCacheRepositoryInstance() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		BindexFunctionTools tools = new BindexFunctionTools();
		tools.setConfigurator(configurator);

		Method method = BindexFunctionTools.class.getDeclaredMethod("getBindexRepository");
		method.setAccessible(true);

		BindexRepository repo = Mockito.mock(BindexRepository.class);
		try (MockedConstruction<BindexRepository> construction = Mockito.mockConstruction(BindexRepository.class,
				(mock, context) -> {
					Mockito.when(mock.getBindex(Mockito.anyString())).thenReturn(null);
				})) {

			// Act
			Object first = method.invoke(tools);
			Object second = method.invoke(tools);

			// Assert
			assertEquals(1, construction.constructed().size());
			assertEquals(first, second);
		}
	}
}
