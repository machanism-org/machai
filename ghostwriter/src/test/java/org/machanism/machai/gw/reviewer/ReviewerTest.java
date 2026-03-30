package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class ReviewerTest {

	private static final class TestReviewer implements Reviewer {
		@Override
		public String perform(File projectDir, File file) throws IOException {
			throw new UnsupportedOperationException("not used");
		}

		@Override
		public String[] getSupportedFileExtensions() {
			return new String[] { "a", "b" };
		}
	}

	@Test
	void getSupportedFileExtensions_returnsExpectedArray() {
		// Arrange
		Reviewer reviewer = new TestReviewer();

		// Act
		String[] result = reviewer.getSupportedFileExtensions();

		// Assert
		assertArrayEquals(new String[] { "a", "b" }, result);
	}

	@Test
	void perform_defaultImplementationContract_isInvokableByImplementations() {
		// Arrange
		Reviewer reviewer = new TestReviewer();

		// Act + Assert
		assertThrows(UnsupportedOperationException.class, () -> reviewer.perform(new File("."), new File("x")));
	}
}
