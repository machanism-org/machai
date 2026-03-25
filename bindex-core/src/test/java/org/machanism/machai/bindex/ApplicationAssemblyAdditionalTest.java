package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.machanism.machai.schema.Bindex;
import org.mockito.MockedStatic;

class ApplicationAssemblyAdditionalTest {

	@Test
	void constructor_whenNullGenAi_throwsIllegalArgumentException() {
		// Arrange
		Configurator configurator = mock(Configurator.class);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new ApplicationAssembly(null, configurator, new File(".")));
	}

	@Test
	void constructor_whenNullConfig_throwsIllegalArgumentException() {
		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new ApplicationAssembly("openai", null, new File(".")));
	}

	@Test
	void constructor_whenNullDir_throwsIllegalArgumentException() {
		// Arrange
		Configurator configurator = mock(Configurator.class);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> new ApplicationAssembly("openai", configurator, null));
	}

	@Test
	void projectDir_whenNull_throwsIllegalArgumentException() {
		// Arrange
		ApplicationAssembly assembly = createAssemblyWithMockedProvider(mock(GenAIProvider.class));

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> assembly.projectDir(null));
	}

	@Test
	void setLogInputsAndIsInputsLog_roundTrip() {
		// Arrange
		ApplicationAssembly assembly = createAssemblyWithMockedProvider(mock(GenAIProvider.class));

		// Act
		assembly.setLogInputs(true);

		// Assert
		assertTrue(assembly.isInputsLog());
	}

	@Test
	void assembly_whenInputsLogEnabled_writesInputsLogToProjectDirAndPerforms() {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		ApplicationAssembly assembly = createAssemblyWithMockedProvider(provider);

		File projectDir = new File("target/test-projectDir");
		assembly.projectDir(projectDir);
		assembly.setLogInputs(true);

		Bindex b1 = new Bindex();
		b1.setId("id1");
		b1.setDescription("desc1");
		Bindex b2 = new Bindex();
		b2.setId("id2");
		b2.setDescription("desc2");

		// Act
		assembly.assembly("do stuff", Arrays.asList(b1, null, b2));

		// Assert
		verify(provider).instructions(any(String.class));
		verify(provider).prompt(argThat(p -> p.contains("| id1 | desc1 |") && p.contains("| id2 | desc2 |")));
		verify(provider).inputsLog(eq(new File(projectDir, ".machai/assembly-inputs.txt")));
		verify(provider).perform();
	}

	@Test
	void assembly_whenInputsLogDisabled_doesNotWriteInputsLogButPerforms() {
		// Arrange
		GenAIProvider provider = mock(GenAIProvider.class);
		ApplicationAssembly assembly = createAssemblyWithMockedProvider(provider);

		assembly.setLogInputs(false);

		Bindex b = new Bindex();
		b.setId("idx");
		b.setDescription("desc");

		// Act
		assembly.assembly("do stuff", Collections.singletonList(b));

		// Assert
		verify(provider, never()).inputsLog(any(File.class));
		verify(provider).perform();
	}

	@Test
	void assembly_whenPromptNull_throwsIllegalArgumentException() {
		// Arrange
		ApplicationAssembly assembly = createAssemblyWithMockedProvider(mock(GenAIProvider.class));

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> assembly.assembly(null, Collections.emptyList()));
	}

	@Test
	void assembly_whenBindexListNull_throwsIllegalArgumentException() {
		// Arrange
		ApplicationAssembly assembly = createAssemblyWithMockedProvider(mock(GenAIProvider.class));

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> assembly.assembly("prompt", null));
	}

	private static ApplicationAssembly createAssemblyWithMockedProvider(GenAIProvider provider) {
		Configurator configurator = mock(Configurator.class);
		File dir = new File(".");

		ApplicationAssembly assembly;
		try (MockedStatic<GenAIProviderManager> managerMock = mockStatic(GenAIProviderManager.class)) {
			managerMock.when(() -> GenAIProviderManager.getProvider(eq("openai"), eq(configurator))).thenReturn(provider);
			assembly = new ApplicationAssembly("openai", configurator, dir);
		}

		// Avoid mocking FunctionToolsLoader (not mockable on this runtime). Force provider back in.
		try {
			Field providerField = ApplicationAssembly.class.getDeclaredField("provider");
			providerField.setAccessible(true);
			providerField.set(assembly, provider);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return assembly;
	}
}
