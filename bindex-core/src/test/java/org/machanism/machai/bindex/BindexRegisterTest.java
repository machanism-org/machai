package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class BindexRegisterTest {

	@Test
	void update_setsFlagAndReturnsSameInstance() {
		// Arrange
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		try (MockedConstruction<Picker> ignored = mockConstruction(Picker.class)) {
			BindexRegister register = new BindexRegister(provider, "mongodb://localhost");

			// Act
			BindexRegister returned = register.update(true);

			// Assert
			assertSame(register, returned);
		}
	}

	@Test
	void close_delegatesToPickerClose() throws Exception {
		// Arrange
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		try (MockedConstruction<Picker> pickerConstruction = mockConstruction(Picker.class)) {
			BindexRegister register = new BindexRegister(provider, "mongodb://localhost");
			Picker picker = pickerConstruction.constructed().get(0);

			// Act
			register.close();

			// Assert
			verify(picker).close();
		}
	}

	@Test
	void close_whenPickerThrowsIOException_propagates() throws Exception {
		// Arrange
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		try (MockedConstruction<Picker> pickerConstruction = mockConstruction(Picker.class,
				(picker, context) -> doThrow(new IOException("io")).when(picker).close())) {
			BindexRegister register = new BindexRegister(provider, "mongodb://localhost");

			// Act + Assert
			assertThrows(IOException.class, register::close);
		}
	}
}
